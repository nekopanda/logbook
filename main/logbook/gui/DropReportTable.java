package logbook.gui;

import logbook.dto.BattleExDto;
import logbook.dto.BattleResultDto;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;
import logbook.gui.logic.TableRowHeader;
import logbook.internal.BattleResultFilter;
import logbook.internal.BattleResultServer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * ドロップ報告書
 *
 */
public final class DropReportTable extends AbstractTableDialog {

    private BattleResultFilter filter = new BattleResultFilter();

    private final BattleDetailDialog detailDialog;

    /**
     * @param parent
     */
    public DropReportTable(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
        this.detailDialog = new BattleDetailDialog(this);
    }

    public void updateFilter(BattleResultFilter filter) {
        this.filter = filter;
        this.reloadTable();
    }

    @Override
    protected void createContents() {
        this.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                TableItem[] items = DropReportTable.this.table.getSelection();
                int selected = DropReportTable.this.table.getSelectionIndex();
                if (selected != -1) {
                    TableRowHeader rowHeader = (TableRowHeader) DropReportTable.this.body.get(selected)[0];
                    BattleResultDto result = (BattleResultDto) rowHeader.get();
                    BattleExDto detail = BattleResultServer.get().getBattleDetail(result);
                    DropReportTable.this.detailDialog.setBattle(result, detail);
                    DropReportTable.this.detailDialog.open();
                }
            }
        });
        // フィルターメニュー
        final MenuItem filter = new MenuItem(this.opemenu, SWT.PUSH);
        filter.setText("フィルター(&F)\tCtrl+F");
        filter.setAccelerator(SWT.CTRL + 'F');
        filter.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new BattleFilterDialog(DropReportTable.this).open();
            }
        });
        // セパレータ
        new MenuItem(this.tablemenu, SWT.SEPARATOR);
        // 右クリックメニューに追加する
        final MenuItem filtertable = new MenuItem(this.tablemenu, SWT.NONE);
        filtertable.setText("フィルター(&F)");
        filtertable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new BattleFilterDialog(DropReportTable.this).open();
            }
        });
    }

    public BattleResultFilter getFilter() {
        return this.filter;
    }

    @Override
    protected String getTitle() {
        return "ドロップ報告書";
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getBattleResultHeader();
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getBattleResultBody(this.filter);
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        return CreateReportLogic.DEFAULT_TABLE_ITEM_CREATOR;
    }

    @Override
    protected SelectionListener getHeaderSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.getSource() instanceof TableColumn) {
                    DropReportTable.this.sortTableItems((TableColumn) e.getSource());
                }
            }
        };
    }
}
