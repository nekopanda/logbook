package logbook.gui;

import java.util.Map;

import logbook.config.ShipGroupConfig;
import logbook.config.bean.ShipGroupBean;
import logbook.data.context.GlobalContext;
import logbook.dto.ShipDto;
import logbook.dto.ShipFilterDto;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * 所有艦娘一覧テーブル
 *
 */
public final class ShipTable extends AbstractTableDialog {

    /** 成長余地 */
    private boolean specdiff = false;

    /** フィルター */
    private ShipFilterDto filter = new ShipFilterDto();

    /**
     * @param parent
     */
    public ShipTable(Shell parent) {
        super(parent);
    }

    /**
     * フィルターを設定する
     * @param filter フィルター
     */
    public void updateFilter(ShipFilterDto filter) {
        this.filter = filter;
        this.reloadTable();
    }

    @Override
    protected void createContents() {
        // メニューバーに追加する
        // フィルターメニュー
        SelectionListener groupListener = new GroupFilterSelectionAdapter(this);
        MenuItem groupCascade = new MenuItem(this.opemenu, SWT.CASCADE);
        groupCascade.setText("グループフィルター(&G)");
        Menu groupMenu = new Menu(groupCascade);
        groupCascade.setMenu(groupMenu);
        MenuItem nullGroupItem = new MenuItem(groupMenu, SWT.RADIO);
        nullGroupItem.setText("選択なし\tF6");
        nullGroupItem.setAccelerator(SWT.F6);
        nullGroupItem.setSelection(true);
        nullGroupItem.addSelectionListener(groupListener);
        for (int i = 0; i < ShipGroupConfig.get().getGroup().size(); i++) {
            ShipGroupBean group = ShipGroupConfig.get().getGroup().get(i);
            MenuItem groupItem = new MenuItem(groupMenu, SWT.RADIO);
            if ((SWT.KEYCODE_BIT + 16 + i) <= SWT.F20) {
                groupItem.setText(group.getName() + "\t" + "F" + (i + 7));
                groupItem.setAccelerator(SWT.KEYCODE_BIT + 16 + i);
            } else {
                groupItem.setText(group.getName());
            }
            groupItem.setData(group);
            groupItem.addSelectionListener(groupListener);
        }
        final MenuItem filter = new MenuItem(this.opemenu, SWT.PUSH);
        filter.setText("フィルター(&F)\tCtrl+F");
        filter.setAccelerator(SWT.CTRL + 'F');
        filter.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ShipFilterDialog(ShipTable.this.shell, ShipTable.this, ShipTable.this.filter).open();
            }
        });
        // セパレータ
        new MenuItem(this.opemenu, SWT.SEPARATOR);
        // 成長の余地を表示メニュー
        final MenuItem switchdiff = new MenuItem(this.opemenu, SWT.CHECK);
        switchdiff.setText("成長の余地を表示");
        switchdiff.setSelection(this.specdiff);
        switchdiff.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ShipTable.this.specdiff = switchdiff.getSelection();
                ShipTable.this.reloadTable();
            }
        });
        // 右クリックメニューに追加する
        final MenuItem filtertable = new MenuItem(this.tablemenu, SWT.NONE);
        filtertable.setText("フィルター(&F)");
        filtertable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ShipFilterDialog(ShipTable.this.shell, ShipTable.this, ShipTable.this.filter).open();
            }
        });
        MenuItem idCopy = new MenuItem(this.tablemenu, SWT.NONE);
        idCopy.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                StringBuilder sb = new StringBuilder();
                for (TableItem item : ShipTable.this.table.getSelection()) {
                    sb.append(item.getData()).append(", ");
                }
                Clipboard clipboard = new Clipboard(Display.getDefault());
                clipboard.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
            }
        });
        idCopy.setText("艦娘個人IDをコピー(&1)");
        MenuItem shipCopy = new MenuItem(this.tablemenu, SWT.NONE);
        shipCopy.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                StringBuilder sb = new StringBuilder();
                Map<Long, ShipDto> shipMap = GlobalContext.getShipMap();
                for (TableItem item : ShipTable.this.table.getSelection()) {
                    sb.append("\"").append(shipMap.get(item.getData()).getName()).append("\", ");
                }
                Clipboard clipboard = new Clipboard(Display.getDefault());
                clipboard.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
            }
        });
        shipCopy.setText("艦娘の名前をコピー(&2)");

    }

    @Override
    protected String getTitle() {
        return "所有艦娘一覧";
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
        this.body = CreateReportLogic.getShipListBody(this.specdiff, this.filter);
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
                    ShipTable.this.sortTableItems((TableColumn) e.getSource());
                }
            }
        };
    }

    /**
     * フィルターを取得します。
     * @return フィルター
     */
    public ShipFilterDto getFilter() {
        return this.filter;
    }

    /**
     * グループフィルターを選択した時に呼び出されるアダプター
     *
     */
    private static final class GroupFilterSelectionAdapter extends SelectionAdapter {

        /** ダイアログ */
        private final ShipTable dialog;

        public GroupFilterSelectionAdapter(ShipTable dialog) {
            this.dialog = dialog;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.widget instanceof MenuItem) {
                MenuItem item = (MenuItem) e.widget;
                if (item.getSelection()) {
                    ShipGroupBean group = (ShipGroupBean) item.getData();
                    this.dialog.getFilter().group = group;
                    this.dialog.reloadTable();
                    if (group != null) {
                        this.dialog.shell.setText(group.getName() + " - " + this.dialog.getTitle());
                    } else {
                        this.dialog.shell.setText(this.dialog.getTitle());
                    }
                }
            }
        }
    }
}
