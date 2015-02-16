package logbook.gui;

import java.util.ArrayList;
import java.util.List;

import logbook.config.AppConfig;
import logbook.config.ShipGroupConfig;
import logbook.config.bean.ShipGroupBean;
import logbook.dto.ShipDto;
import logbook.dto.ShipFilterDto;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.LayoutLogic;
import logbook.gui.logic.ShipGroupListener;
import logbook.gui.logic.ShipGroupObserver;
import logbook.gui.logic.TableItemCreator;
import logbook.gui.widgets.ShipFilterComposite;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

    private ShipFilterComposite filterCompo;

    private MenuItem filterMenu;
    private MenuItem switchdiff;
    private MenuItem switchdiff2;
    private MenuItem addGroupCascade;
    private MenuItem removeGroupCascade;

    /**
     * @param parent
     */
    public ShipTable(Shell parent, MenuItem menuItem, int index) {
        super(parent, menuItem);
        this.index = index;
    }

    /**
     * Open the dialog.
     */
    @Override
    public void open() {
        if (this.isWindowInitialized()) {
            // フィルタパネルの内容を更新しておく
            this.filterCompo.setRedraw(false);
            this.filterCompo.updateContents(this.filter);
            this.filterCompo.layout();
            this.filterCompo.setRedraw(true);
        }
        super.open();
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
    protected void createContentsBefore() {
        GridLayout shellLayout = new GridLayout(1, false);
        shellLayout.verticalSpacing = 1;
        shellLayout.marginWidth = 1;
        shellLayout.marginHeight = 1;
        shellLayout.marginBottom = 1;
        shellLayout.horizontalSpacing = 1;
        this.shell.setLayout(shellLayout);

        this.filterCompo = new ShipFilterComposite(this);
        this.filterCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    }

    @Override
    protected void createContents() {
        SelectionListener switchDiffListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ShipTable.this.switchSpecDiff((MenuItem) e.getSource());
            }
        };

        this.table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        // メニューバーに追加する
        // フィルターメニュー
        this.filterMenu = new MenuItem(this.opemenu, SWT.CHECK);
        this.filterMenu.setText("フィルターパネル(&D)\tCtrl+D");
        this.filterMenu.setAccelerator(SWT.CTRL + 'D');
        this.filterMenu.setSelection(true);
        this.filterMenu.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ShipTable.this.filterMenuSelected();
            }
        });

        // 検索（キーボードショートカットのため）
        MenuItem searchMenu = new MenuItem(this.opemenu, SWT.NONE);
        searchMenu.setText("検索(&F)\tCtrl+F");
        searchMenu.setAccelerator(SWT.CTRL + 'F');
        searchMenu.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ShipTable.this.focusOnSearchBox();
            }
        });

        // セパレータ
        new MenuItem(this.opemenu, SWT.SEPARATOR);

        // 成長の余地を表示メニュー
        this.switchdiff = new MenuItem(this.opemenu, SWT.CHECK);
        this.switchdiff.setText("成長の余地を表示");
        this.switchdiff.setSelection(this.specdiff);
        this.switchdiff.addSelectionListener(switchDiffListener);

        // セパレータ
        new MenuItem(this.tablemenu, SWT.SEPARATOR);

        // 成長の余地を表示メニュー
        this.switchdiff2 = new MenuItem(this.tablemenu, SWT.CHECK);
        this.switchdiff2.setText("成長の余地を表示");
        this.switchdiff2.setSelection(this.specdiff);
        this.switchdiff2.addSelectionListener(switchDiffListener);

        // セパレータ
        new MenuItem(this.tablemenu, SWT.SEPARATOR);

        // グループメニュー作成
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
        this.filterCompo.updateContents(this.filter);
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

    private void switchSpecDiff(MenuItem source) {
        this.specdiff = source.getSelection();
        this.switchdiff.setSelection(this.specdiff);
        this.switchdiff2.setSelection(this.specdiff);
        this.reloadTable();
    }

    private void focusOnSearchBox() {
        if (this.filterMenu.getSelection() == false) {
            // まずは表示する
            this.filterMenu.setSelection(true);
            this.filterMenuSelected();
        }
        this.filterCompo.getSearchCombo().setFocus();
    }

    private void filterMenuSelected() {
        LayoutLogic.hide(this.filterCompo, !this.filterMenu.getSelection());
        this.getShell().layout();
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

    private List<ShipDto> getSelection() {
        List<ShipDto> ships = new ArrayList<>();
        TableItem[] tableItems = ShipTable.this.table.getSelection();
        for (int i = 0; i < tableItems.length; i++) {
            ShipDto dto = (ShipDto) tableItems[i].getData();
            ships.add(dto);
        }
        return ships;
    }

    @Override
    public void listChanged() {
        List<ShipGroupBean> groups = ShipGroupConfig.get().getGroup();

        Menu addGroupMenu = recreateCascadeMenu(this.addGroupCascade);
        for (ShipGroupBean groupBean : groups) {
            final MenuItem groupItem = new MenuItem(addGroupMenu, SWT.NONE);
            groupItem.setText(groupBean.getName());
            groupItem.setData(groupBean);
            groupItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    List<ShipDto> ships = ShipTable.this.getSelection();
                    if (ships.size() > 0) {
                        List<String> name = new ArrayList<>();
                        for (ShipDto ship : ships) {
                            name.add(ship.getName());
                        }
                        MessageBox box = new MessageBox(ShipTable.this.shell, SWT.YES | SWT.NO
                                | SWT.ICON_QUESTION);
                        box.setText("選択した艦娘をグループに追加");
                        box.setMessage("「" + StringUtils.join(name, ",") + "」をグループに追加しますか？");

                        if (box.open() == SWT.YES) {
                            ShipGroupBean bean = (ShipGroupBean) e.widget.getData();
                            for (ShipDto ship : ships) {
                                bean.getShips().add(ship.getId());
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
                    List<ShipDto> ships = ShipTable.this.getSelection();
                    if (ships.size() > 0) {
                        List<String> name = new ArrayList<>();
                        for (ShipDto ship : ships) {
                            name.add(ship.getName());
                        }
                        MessageBox box = new MessageBox(ShipTable.this.shell, SWT.YES | SWT.NO
                                | SWT.ICON_QUESTION);
                        box.setText("選択した艦娘をグループから除去");
                        box.setMessage("「" + StringUtils.join(name, ",") + "」をグループから除去しますか？");

                        if (box.open() == SWT.YES) {
                            ShipGroupBean bean = (ShipGroupBean) e.widget.getData();
                            for (ShipDto ship : ships) {
                                bean.getShips().remove(ship.getId());
                            }
                            ShipGroupObserver.groupShipChanged(bean);
                        }
                    }
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