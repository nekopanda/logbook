package logbook.gui;

import logbook.config.bean.TableConfigBean;
import logbook.dto.ShipFilterDto;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * お風呂に入りたい艦娘
 * 
 */
public final class BathwaterTableDialog extends AbstractTableDialog {

    /** フィルター */
    private final ShipFilterDto filter = new ShipFilterDto();

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public BathwaterTableDialog(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
    }

    @Override
    protected void createContents() {
        this.filter.notneedbath = false;

        final MenuItem resetVisibles = new MenuItem(this.opemenu, SWT.NONE);
        resetVisibles.setText("表示・非表示をリセット");
        resetVisibles.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BathwaterTableDialog.this.getConfig().setVisibleColumn(
                        CreateReportLogic.getBathTableDefaultVisibles());
                BathwaterTableDialog.this.restoreColumnWidth(false);
            }
        });

        final MenuItem removecheck = new MenuItem(this.opemenu, SWT.CHECK);
        removecheck.setText("遠征中の艦娘を外す");
        removecheck.setSelection(this.filter.mission);
        removecheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BathwaterTableDialog.this.filter.mission = removecheck.getSelection();
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
        return CreateReportLogic.SHIP_LIST_TABLE_ITEM_CREATOR;
    }

    @Override
    protected SelectionListener getHeaderSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.getSource() instanceof TableColumn) {
                    BathwaterTableDialog.this.sortTableItems((TableColumn) e.getSource());
                }
            }
        };
    }

    @Override
    protected int[] defaultColumnOrder() {
        return CreateReportLogic.getBathTableDefaultColumnOrder();
    }

    @Override
    protected TableConfigBean getDefaultTableConfig() {
        TableConfigBean config = super.getDefaultTableConfig();
        config.setVisibleColumn(CreateReportLogic.getBathTableDefaultVisibles());
        config.setColumnOrder(CreateReportLogic.getBathTableDefaultColumnOrder());
        return config;
    }
}
