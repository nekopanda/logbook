package logbook.gui;

import java.util.ArrayList;
import java.util.List;

import logbook.config.ShipGroupConfig;
import logbook.config.bean.ShipGroupBean;
import logbook.data.context.GlobalContext;
import logbook.dto.ShipDto;
import logbook.dto.ShipFilterDto;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
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
        // セパレータ
        new MenuItem(this.tablemenu, SWT.SEPARATOR);
        // 右クリックメニューに追加する
        final MenuItem filtertable = new MenuItem(this.tablemenu, SWT.NONE);
        filtertable.setText("フィルター(&F)");
        filtertable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ShipFilterDialog(ShipTable.this.shell, ShipTable.this, ShipTable.this.filter).open();
            }
        });

        List<ShipGroupBean> groups = ShipGroupConfig.get().getGroup();

        MenuItem groupFilterCascade = new MenuItem(this.tablemenu, SWT.CASCADE);
        groupFilterCascade.setText("グループフィルター(&G)");
        Menu groupFilterMenu = new Menu(groupFilterCascade);
        groupFilterCascade.setMenu(groupFilterMenu);
        for (ShipGroupBean groupBean : groups) {
            final MenuItem groupItem = new MenuItem(groupFilterMenu, SWT.NONE);
            groupItem.setText(groupBean.getName());
            groupItem.setData(groupBean);
            groupItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ShipGroupBean bean = (ShipGroupBean) e.widget.getData();
                    ShipFilterDto filter = ShipTable.this.getFilter();
                    filter.group = bean;
                    ShipTable.this.updateFilter(filter);
                }
            });
        }

        MenuItem addGroupCascade = new MenuItem(this.tablemenu, SWT.CASCADE);
        addGroupCascade.setText("選択した艦娘をグループに追加(&A)");
        Menu addGroupMenu = new Menu(addGroupCascade);
        addGroupCascade.setMenu(addGroupMenu);
        for (ShipGroupBean groupBean : groups) {
            final MenuItem groupItem = new MenuItem(addGroupMenu, SWT.NONE);
            groupItem.setText(groupBean.getName());
            groupItem.setData(groupBean);
            groupItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    TableItem[] tableItems = ShipTable.this.table.getSelection();
                    if (tableItems.length > 0) {
                        List<ShipDto> ships = new ArrayList<>();
                        List<String> name = new ArrayList<>();
                        for (int i = 0; i < tableItems.length; i++) {
                            long id = Long.parseLong(tableItems[i].getText(1));
                            ShipDto ship = GlobalContext.getShipMap().get(id);
                            if (ship != null) {
                                ships.add(ship);
                                name.add(ship.getName());
                            }
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
                        }
                    }
                }
            });
        }

        MenuItem removeGroupCascade = new MenuItem(this.tablemenu, SWT.CASCADE);
        removeGroupCascade.setText("選択した艦娘をグループから除去(&D)");
        Menu removeGroupMenu = new Menu(removeGroupCascade);
        removeGroupCascade.setMenu(removeGroupMenu);
        for (ShipGroupBean groupBean : groups) {
            final MenuItem groupItem = new MenuItem(removeGroupMenu, SWT.NONE);
            groupItem.setText(groupBean.getName());
            groupItem.setData(groupBean);
            groupItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    TableItem[] tableItems = ShipTable.this.table.getSelection();
                    if (tableItems.length > 0) {
                        List<ShipDto> ships = new ArrayList<>();
                        List<String> name = new ArrayList<>();
                        for (int i = 0; i < tableItems.length; i++) {
                            long id = Long.parseLong(tableItems[i].getText(1));
                            ShipDto ship = GlobalContext.getShipMap().get(id);
                            if (ship != null) {
                                ships.add(ship);
                                name.add(ship.getName());
                            }
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
                        }
                    }
                }
            });
        }
    }

    @Override
    protected String getTitle() {
        if ((this.filter != null) && (this.filter.group != null)) {
            return "所有艦娘一覧 (" + this.filter.group.getName() + ")";
        }
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
}
