package logbook.gui;

import java.util.ArrayList;
import java.util.List;

import logbook.config.AppConfig;
import logbook.config.ShipGroupConfig;
import logbook.config.bean.ShipGroupBean;
import logbook.dto.ShipDto;
import logbook.dto.ShipFilterDto;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.ShipGroupListener;
import logbook.gui.logic.ShipGroupObserver;
import logbook.gui.logic.TableItemCreator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * 所有艦娘一覧テーブル
 *
 */
public final class ShipTable extends AbstractTableDialog implements ShipGroupListener {

    /** 何番目か */
    private final int index;

    /** 成長余地 */
    private boolean specdiff = false;

    /** フィルター */
    private ShipFilterDto filter = null;

    private MenuItem groupFilterCascade;
    private MenuItem addGroupCascade;
    private MenuItem removeGroupCascade;
    private final List<MenuItem> currentGroupItems = new ArrayList<MenuItem>();

    /**
     * @param parent
     */
    public ShipTable(Shell parent, MenuItem menuItem, int index) {
        super(parent, menuItem);
        this.index = index;
    }

    /**
     * フィルターを設定する
     * @param filter フィルター
     */
    public void updateFilter(ShipFilterDto filter) {
        this.filter = filter;
        this.reloadTable();
        this.shell.setText(this.getTitle());
    }

    @Override
    protected void createContents() {
        // メニューバーに追加する
        // フィルターメニュー
        final MenuItem filter = new MenuItem(this.opemenu, SWT.PUSH);
        filter.setText("フィルター(&F)\tCtrl+F");
        filter.setAccelerator(SWT.CTRL + 'F');
        filter.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ShipFilterDialog(ShipTable.this, ShipTable.this.filter).open();
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
        // セパレータ
        new MenuItem(this.tablemenu, SWT.SEPARATOR);
        // 右クリックメニューに追加する
        final MenuItem filtertable = new MenuItem(this.tablemenu, SWT.NONE);
        filtertable.setText("フィルター(&F)");
        filtertable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ShipFilterDialog(ShipTable.this, ShipTable.this.filter).open();
            }
        });

        // グループメニュー作成
        this.groupFilterCascade = new MenuItem(this.tablemenu, SWT.CASCADE);
        this.groupFilterCascade.setText("グループフィルター(&G)");
        this.addGroupCascade = new MenuItem(this.tablemenu, SWT.CASCADE);
        this.addGroupCascade.setText("選択した艦娘をグループに追加(&A)");
        this.removeGroupCascade = new MenuItem(this.tablemenu, SWT.CASCADE);
        this.removeGroupCascade.setText("選択した艦娘をグループから除去(&D)");

        MenuItem idCopy = new MenuItem(this.tablemenu, SWT.NONE);
        idCopy.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                StringBuilder sb = new StringBuilder();
                for (TableItem item : ShipTable.this.table.getSelection()) {
                    sb.append(String.valueOf(((ShipDto) item.getData()).getId())).append(", ");
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
                for (TableItem item : ShipTable.this.table.getSelection()) {
                    sb.append("\"").append(((ShipDto) item.getData()).getName()).append("\", ");
                }
                Clipboard clipboard = new Clipboard(Display.getDefault());
                clipboard.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
            }
        });
        shipCopy.setText("艦娘の名前をコピー(&2)");

        // フィルタを復元
        ShipFilterDto[] shipFilters = AppConfig.get().getShipFilters();
        if ((shipFilters != null) && (shipFilters.length > this.index)) {
            this.filter = shipFilters[this.index];
        }
        if (this.filter == null) {
            this.filter = new ShipFilterDto();
        }
        if ((this.filter.group == null) && (this.filter.groupId != 0)) {
            for (ShipGroupBean group : ShipGroupConfig.get().getGroup()) {
                if (group.getId() == this.filter.groupId) {
                    this.filter.group = group;
                    break;
                }
            }
        }
        this.listChanged();

        ShipGroupObserver.addListener(this);
        this.table.addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ShipGroupObserver.removeListener(ShipTable.this);
            }
        });

        // ウィンドウタイトルを設定
        this.shell.setText(this.getTitle());
    }

    @Override
    protected String getTitleMain() {
        String name = AppConfig.get().getShipTableNames()[this.index];
        if ((this.filter != null) && (this.filter.group != null)) {
            return name + " (" + this.filter.group.getName() + ")";
        }
        return name;
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
    public String getWindowId() {
        return this.getClass().getName() + ((this.index == 0) ? "" : String.valueOf(this.index));
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

    public void windowTitleChanged() {
        if (this.shell != null) {
            this.shell.setText(this.getTitle());
        }
    }

    private static Menu recreateCascadeMenu(MenuItem menuItem) {
        Menu menu = menuItem.getMenu();
        if (menu != null) {
            menu.dispose();
        }
        menu = new Menu(menuItem);
        menuItem.setMenu(menu);
        return menu;
    }

    private void groupWidgetSelected(SelectionEvent e) {
        MenuItem selectedItem = (MenuItem) e.widget;
        boolean selection = selectedItem.getSelection();
        if (selection) {
            ShipGroupBean bean = (ShipGroupBean) e.widget.getData();
            ShipFilterDto filter = this.getFilter();
            filter.group = bean;
            filter.groupId = bean.getId();
            // これだけ残してあとはオフる
            for (MenuItem item : this.currentGroupItems) {
                if (selectedItem != item) {
                    item.setSelection(false);
                }
            }
        }
        else {
            this.filter.group = null;
            this.filter.groupId = 0;
        }
        ShipTable.this.updateFilter(this.filter);
    }

    @Override
    public void listChanged() {
        List<ShipGroupBean> groups = ShipGroupConfig.get().getGroup();

        Menu groupFilterMenu = recreateCascadeMenu(this.groupFilterCascade);
        this.currentGroupItems.clear();
        for (ShipGroupBean groupBean : groups) {
            final MenuItem groupItem = new MenuItem(groupFilterMenu, SWT.CHECK);
            groupItem.setText(groupBean.getName());
            groupItem.setData(groupBean);
            groupItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ShipTable.this.groupWidgetSelected(e);
                }
            });
            if (this.filter.group == groupBean) {
                groupItem.setSelection(true);
            }
            this.currentGroupItems.add(groupItem);
        }

        Menu addGroupMenu = recreateCascadeMenu(this.addGroupCascade);
        for (ShipGroupBean groupBean : groups) {
            final MenuItem groupItem = new MenuItem(addGroupMenu, SWT.NONE);
            groupItem.setText(groupBean.getName());
            groupItem.setData(groupBean);
            groupItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    TableItem[] tableItems = ShipTable.this.table.getSelection();
                    if (tableItems.length > 0) {
                        List<String> name = new ArrayList<>();
                        for (int i = 0; i < tableItems.length; i++) {
                            name.add(((ShipDto) tableItems[i].getData()).getName());
                        }
                        MessageBox box = new MessageBox(ShipTable.this.shell, SWT.YES | SWT.NO
                                | SWT.ICON_QUESTION);
                        box.setText("選択した艦娘をグループに追加");
                        box.setMessage("「" + StringUtils.join(name, ",") + "」をグループに追加しますか？");

                        if (box.open() == SWT.YES) {
                            ShipGroupBean bean = (ShipGroupBean) e.widget.getData();
                            for (int i = 0; i < tableItems.length; i++) {
                                int id = Integer.parseInt(tableItems[i].getText(1));
                                bean.getShips().add(id);
                            }
                            ShipGroupObserver.groupShipChanged(bean);
                        }
                    }
                }
            });
        }

        Menu removeGroupMenu = recreateCascadeMenu(this.removeGroupCascade);
        for (ShipGroupBean groupBean : groups) {
            final MenuItem groupItem = new MenuItem(removeGroupMenu, SWT.NONE);
            groupItem.setText(groupBean.getName());
            groupItem.setData(groupBean);
            groupItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    TableItem[] tableItems = ShipTable.this.table.getSelection();
                    ShipGroupBean bean = (ShipGroupBean) e.widget.getData();
                    for (int i = 0; i < tableItems.length; i++) {
                        int id = Integer.parseInt(tableItems[i].getText(1));
                        bean.getShips().remove(id);
                    }
                    ShipGroupObserver.groupShipChanged(bean);
                }
            });
        }
    }

    @Override
    public void save() {
        if (this.filter != null) {
            ShipFilterDto[] shipFilters = AppConfig.get().getShipFilters();
            if ((shipFilters == null) || (shipFilters.length != 4)) {
                shipFilters = new ShipFilterDto[4];
            }
            shipFilters[this.index] = this.filter;
            AppConfig.get().setShipFilters(shipFilters);
        }
        super.save();
    }

    @Override
    public void groupNameChanged(ShipGroupBean group) {
        if (this.filter.group == group) {
            this.shell.setText(this.getTitle());
        }
    }

    @Override
    public void groupShipChanged(ShipGroupBean group) {
        if (this.filter.group == group) {
            this.reloadTable();
        }

    }
}