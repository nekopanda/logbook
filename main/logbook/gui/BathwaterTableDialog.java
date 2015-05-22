package logbook.gui;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import logbook.config.bean.TableConfigBean;
import logbook.constants.AppConstants;
import logbook.data.Data;
import logbook.data.DataType;
import logbook.dto.ShipFilterDto;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;
import logbook.internal.LoggerHolder;
import logbook.scripting.TableItemCreatorProxy;
import logbook.util.ReportUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * お風呂に入りたい艦娘
 * 
 */
public final class BathwaterTableDialog extends AbstractTableDialog {

    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(BathwaterTableDialog.class);

    /** フィルター */
    private final ShipFilterDto filter = new ShipFilterDto();

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public BathwaterTableDialog(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
        this.filter.notneedbath = false;
        this.filter.groupMode = 0;
    }

    @Override
    protected void createContents() {

        final MenuItem resetVisibles = new MenuItem(this.opemenu, SWT.NONE);
        resetVisibles.setText("表示・非表示をリセット");
        resetVisibles.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BathwaterTableDialog.this.getConfig().setVisibleColumn(
                        BathwaterTableDialog.this.getBathTableDefaultVisibles());
                BathwaterTableDialog.this.restoreColumnWidth(false);
            }
        });

        final MenuItem removecheck = new MenuItem(this.opemenu, SWT.CHECK);
        removecheck.setText("遠征中の艦娘を外す");
        removecheck.setSelection(!this.filter.mission);
        removecheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BathwaterTableDialog.this.filter.mission = !removecheck.getSelection();
                BathwaterTableDialog.this.reloadTable();
            }
        });
    }

    @Override
    protected String getTitleMain() {
        return "お風呂に入りたい艦娘";
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getShipListHeader();
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getShipListBody(false, this.filter);
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        return TableItemCreatorProxy.get(AppConstants.SHIPTABLE_PREFIX);
    }

    @Override
    protected int[] defaultColumnOrder() {
        Map<String, Integer> colMap = AppConstants.BATHTABLE_COLUMN_MAP;
        Map<Integer, Integer> orderMap = new TreeMap<>();
        int[] order = new int[this.headerId.length];
        for (int i = 0; i < order.length; ++i) {
            if (colMap.containsKey(this.headerId[i])) {
                orderMap.put(colMap.get(this.headerId[i]), 0);
            }
        }
        int hiddenColIndex = 0;
        for (Entry<Integer, Integer> entry : orderMap.entrySet()) {
            entry.setValue(hiddenColIndex++);
        }
        if (hiddenColIndex != colMap.size()) {
            LOG.get().warn("BATHTABLE_COLUMN_MAPに実際にはないカラムがあるようです");
        }
        for (int i = 0; i < order.length; ++i) {
            if (colMap.containsKey(this.headerId[i])) {
                order[orderMap.get(colMap.get(this.headerId[i]))] = i;
            }
            else {
                order[hiddenColIndex++] = i;
            }
        }
        return order;
    }

    @Override
    protected TableConfigBean getDefaultTableConfig() {
        TableConfigBean config = super.getDefaultTableConfig();
        config.setVisibleColumn(this.getBathTableDefaultVisibles());
        config.setColumnOrder(this.defaultColumnOrder());
        return config;
    }

    private boolean[] getBathTableDefaultVisibles() {
        Map<String, Integer> colMap = AppConstants.BATHTABLE_COLUMN_MAP;
        boolean[] visibles = new boolean[this.headerId.length];
        for (int i = 0; i < visibles.length; ++i) {
            visibles[i] = colMap.containsKey(this.headerId[i]);
        }
        return visibles;
    }

    /**
     * 更新する必要のあるデータ
     */
    @SuppressWarnings("incomplete-switch")
    @Override
    public void update(DataType type, Data data) {
        if (ReportUtils.isShipUpdate(type)) {
            this.needsUpdate = true;
        }
    }
}