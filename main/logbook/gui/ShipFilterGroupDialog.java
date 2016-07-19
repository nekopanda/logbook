package logbook.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import logbook.config.ShipGroupConfig;
import logbook.config.bean.ShipGroupBean;
import logbook.config.bean.ShipGroupListBean;
import logbook.constants.AppConstants;
import logbook.data.Data;
import logbook.data.DataType;
import logbook.data.context.GlobalContext;
import logbook.dto.DockDto;
import logbook.dto.ShipDto;
import logbook.gui.logic.ShipGroupListener;
import logbook.gui.logic.ShipGroupObserver;
import logbook.gui.logic.TableItemCreator;
import logbook.gui.logic.TableRowHeader;
import logbook.internal.CondTiming;
import logbook.scripting.TableItemCreatorProxy;
import logbook.util.ReportUtils;
import logbook.util.SwtUtils;
import logbook.util.SwtUtils.TableDragAndDropListener;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * グループエディター
 *
 */
public final class ShipFilterGroupDialog extends AbstractTableDialog implements ShipGroupListener {

    private Text text;
    private SashForm sashForm;
    private Composite sideComposite;
    private Composite mainComposite;
    private Combo shipcombo;
    private Table groupTable;
    private Button btnAddShip;
    private Button btnRemoveShip;

    private final Map<String, ShipDto> shipmap = new HashMap<String, ShipDto>();

    /** 現在表示しているグループ */
    private GroupProperty property;

    /**
     * Create the dialog.
     * @param parent
     * @param menuItem
     */
    public ShipFilterGroupDialog(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
    }

    /**
     * Create contents of the dialog.
     */
    @Override
    protected void createContentsBefore() {
        GridLayout shellLayout = new GridLayout(1, false);
        shellLayout.verticalSpacing = 1;
        shellLayout.marginWidth = 1;
        shellLayout.marginHeight = 1;
        shellLayout.marginBottom = 1;
        shellLayout.horizontalSpacing = 1;
        this.shell.setLayout(shellLayout);

        this.sashForm = new SashForm(this.shell, SWT.SMOOTH);
        this.sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        this.sideComposite = new Composite(this.sashForm, SWT.NONE);
        GridLayout sideLayout = new GridLayout(2, false);
        sideLayout.verticalSpacing = 1;
        sideLayout.marginWidth = 1;
        sideLayout.marginHeight = 1;
        sideLayout.marginBottom = 1;
        sideLayout.horizontalSpacing = 1;
        this.sideComposite.setLayout(sideLayout);

        Button btnAdd = new Button(this.sideComposite, SWT.NONE);
        btnAdd.addSelectionListener(new AddGroupAdapter(this));
        btnAdd.setImage(SWTResourceManager.getImage(ShipFilterGroupDialog.class, AppConstants.R_ICON_ADD));

        Button btnRemove = new Button(this.sideComposite, SWT.NONE);
        btnRemove.addSelectionListener(new RemoveGroupAdapter(this));
        btnRemove.setImage(SWTResourceManager.getImage(ShipFilterGroupDialog.class, AppConstants.R_ICON_DELETE));

        final Table groupTable = this.groupTable = new Table(this.sideComposite, SWT.BORDER);
        this.groupTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        this.groupTable.addSelectionListener(new GroupTableSelectionAdapter(this));
        final TableColumn groupTableColumn = new TableColumn(this.groupTable, SWT.LEFT);
        groupTableColumn.setText("グループ");
        groupTableColumn.setMoveable(false);
        groupTableColumn.setResizable(false);
        this.groupTable.setHeaderVisible(false);
        this.groupTable.addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event e) {
                groupTableColumn.setWidth(groupTable.getClientArea().width);
            }
        });
        SwtUtils.addItemDragAndDropMoveSupport(new Table[] { this.groupTable }, new TableDragAndDropListener() {

            @Override
            public boolean canDragSource(TableItem source) {
                return true;
            }

            @Override
            public TableItem create(Table table, TableItem source, int index) {
                // 実際のリストを操作
                List<ShipGroupBean> list = ShipGroupConfig.get().getGroup();
                GroupProperty prop = (GroupProperty) source.getData();
                int oldIndex = list.indexOf(prop.getShipGroupBean());
                list.add(index, prop.getShipGroupBean());
                list.remove(oldIndex < index ? oldIndex : oldIndex + 1);

                // テーブルアイテム作成
                TableItem item = new TableItem(groupTable, SWT.NONE, index);
                prop.setTreeItem(item);
                item.setText(source.getText());
                item.setData(prop);
                return item;
            }

            @Override
            public void finished(TableItem newItem) {
                ShipGroupObserver.listChanged();
            }

            @Override
            public String tableItemToString(TableItem item) {
                return item.getText();
            }

        });

        ShipGroupListBean shipGroupList = ShipGroupConfig.get();

        for (ShipGroupBean bean : shipGroupList.getGroup()) {
            TableItem groupItem = new TableItem(this.groupTable, SWT.NONE);
            //groupItem.setImage(SWTResourceManager
            //        .getImage(ShipFilterGroupDialog.class, AppConstants.R_ICON_FOLDER));
            groupItem.setText(bean.getName());
            groupItem.setData(new GroupProperty(bean, groupItem));
        }

        this.mainComposite = new Composite(this.sashForm, SWT.NONE);
        GridLayout mainLayout = new GridLayout(4, false);
        mainLayout.verticalSpacing = 1;
        mainLayout.marginWidth = 1;
        mainLayout.marginHeight = 1;
        mainLayout.marginBottom = 1;
        mainLayout.horizontalSpacing = 1;
        this.mainComposite.setLayout(mainLayout);

        this.text = new Text(this.mainComposite, SWT.BORDER);
        this.text.addModifyListener(new ModifyNameAdapter(this));
        this.text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
        this.text.setEnabled(false);
    }

    /**
     * Create contents of the dialog.
     */
    @Override
    protected void createContents() {
        this.table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
        this.shipcombo = new Combo(this.mainComposite, SWT.READ_ONLY);
        this.setShipComboData();
        this.shipcombo.setEnabled(false);

        this.btnAddShip = new Button(this.mainComposite, SWT.NONE);
        this.btnAddShip.setText("追加");
        this.btnAddShip.addSelectionListener(new AddShipAdapter(this));
        this.btnAddShip.setEnabled(false);
        this.btnRemoveShip = new Button(this.mainComposite, SWT.NONE);
        this.btnRemoveShip.setText("除去");
        this.btnRemoveShip.addSelectionListener(new RemoveShipAdapter(this));
        this.btnRemoveShip.setEnabled(false);
        Button updateShip = new Button(this.mainComposite, SWT.NONE);
        updateShip.setText("更新");
        updateShip.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ShipFilterGroupDialog.this.setShipComboData();
            }
        });
        this.sashForm.setWeights(new int[] { 2, 5 });

        new MenuItem(this.tablemenu, SWT.SEPARATOR);

        final MenuItem[] addFleetShipItems = new MenuItem[4];
        for (int i = 1; i <= 4; ++i) {
            MenuItem addFleetMenu = addFleetShipItems[i - 1] = new MenuItem(this.tablemenu, SWT.NONE);
            addFleetMenu.setText("第" + i + "艦隊の艦娘を追加");
            addFleetMenu.addSelectionListener(new AddFleetShipAdapter(this, Integer.toString(i)));
        }

        this.table.addListener(SWT.MenuDetect, new Listener() {
            @Override
            public void handleEvent(Event event) {
                for (int i = 1; i <= 4; ++i) {
                    DockDto dto = GlobalContext.getDock(Integer.toString(i));
                    addFleetShipItems[i - 1].setEnabled(
                            (ShipFilterGroupDialog.this.property != null)
                                    && (dto != null)
                                    && (dto.getShips().size() > 0));
                }
            }
        });

        SwtUtils.addItemDragAndDropMoveSupport(new Table[] { this.table }, new TableDragAndDropListener() {

            @Override
            public boolean canDragSource(TableItem source) {
                // ソート順なし かつ １つのみ選択状態
                return (ShipFilterGroupDialog.this.property != null) &&
                        (ShipFilterGroupDialog.this.config.getSortKeys()[0] == null) &&
                        (ShipFilterGroupDialog.this.table.getSelection().length == 1);
            }

            @Override
            public TableItem create(Table table, TableItem source, int index) {
                // 実際のリストを操作
                if (ShipFilterGroupDialog.this.property != null) {
                    Set<Integer> set = ShipFilterGroupDialog.this.property.getShipGroupBean().getShips();
                    ShipDto ship = (ShipDto) source.getData();
                    Integer[] origarray = set.toArray(new Integer[0]);
                    set.clear();
                    for (int i = 0; i < origarray.length; ++i) {
                        if (i == index) {
                            set.add(ship.getId());
                        }
                        if (origarray[i] != ship.getId()) {
                            set.add(origarray[i]);
                        }
                    }
                    set.add(ship.getId()); // 最後に追加する場合
                }
                // reloadが走るのでここではダミーを返しておく
                return source;
            }

            @Override
            public void finished(TableItem newItem) {
                if (ShipFilterGroupDialog.this.property != null) {
                    ShipGroupObserver.groupShipChanged(ShipFilterGroupDialog.this.property.getShipGroupBean());
                }
            }

            @Override
            public String tableItemToString(TableItem item) {
                return ((ShipDto) item.getData()).getFullName();
            }

        });

        ShipGroupObserver.addListener(this);
        this.table.addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ShipGroupObserver.removeListener(ShipFilterGroupDialog.this);
            }
        });
    }

    @Override
    protected Composite getTableParent() {
        return this.mainComposite;
    }

    @Override
    protected String getTitleMain() {
        return "グループエディター";
    }

    @Override
    protected Point getSize() {
        return SwtUtils.DPIAwareSize(new Point(600, 350));
    }

    @Override
    protected String[] getTableHeader() {
        return new String[] { "", "ID", "艦隊", "名前", "艦種", "Lv", "疲労" };
    }

    @Override
    protected void updateTableBody() {
        List<Comparable[]> body = new ArrayList<Comparable[]>();
        CondTiming condTiming = GlobalContext.getCondTiming();
        if (this.property != null) {
            List<ShipDto> shipList = this.property.getShipList();
            for (int i = 0; i < shipList.size(); i++) {
                ShipDto ship = shipList.get(i);
                body.add(new Comparable[] {
                        new TableRowHeader(i + 1, ship),
                        ship.getId(),
                        ship.getFleetid(),
                        ship.getName(),
                        ship.getType(),
                        ship.getLv(),
                        ship.getEstimatedCond(condTiming)
                });
            }
        }
        this.body = body;
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        return TableItemCreatorProxy.get(AppConstants.SHIPGROUPTABLE_PREFIX);
        /*
        return new TableItemCreator() {

            @Override
            public void init() {
            }

            @Override
            public TableItem create(Table table, Comparable[] text, int count) {
                ShipDto ship = (ShipDto) ((TableRowHeader) text[0]).get();
                TableItem item = new TableItem(table, SWT.NONE);
                // 偶数行に背景色を付ける
                if ((count % 2) != 0) {
                    item.setBackground(SWTResourceManager.getColor(AppConstants.ROW_BACKGROUND));
                }
                item.setBackground(6, CreateReportLogic.getTableCondColor(ship.getEstimatedCond()));
                item.setText(ReportUtils.toStringArray(text));
                return item;
            }
        };
        */
    }

    @Override
    protected void reloadTable() {
        super.reloadTable();
        // 名前・ボタンの状態をセットします
        if (this.property != null) {
            ShipGroupBean group = this.property.getShipGroupBean();
            this.text.setText(group.getName());
            this.text.setEnabled(true);
            this.shipcombo.setEnabled(true);
            this.btnAddShip.setEnabled(true);
            this.btnRemoveShip.setEnabled(true);
        } else {
            this.text.setText("");
            this.text.setEnabled(false);
            this.shipcombo.setEnabled(false);
            this.btnAddShip.setEnabled(false);
            this.btnRemoveShip.setEnabled(false);
        }
    }

    /**
     * コンボボックスに艦娘をセットします
     * 
     * @param combo
     */
    private void setShipComboData() {
        // コンボボックスから全ての艦娘を削除
        this.shipcombo.removeAll();
        // 表示用文字列と艦娘の紐付けを削除
        this.shipmap.clear();
        // 艦娘IDの最大を取得してゼロ埋め長さを算出
        int maxshipid = 0;
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            maxshipid = Math.max(ship.getId(), maxshipid);
        }
        int padlength = String.valueOf(maxshipid).length();
        // 表示用文字列と艦娘の紐付けを追加
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            this.shipmap.put(this.getShipLabel(ship, padlength), ship);
        }
        // 艦娘を経験値順でソート
        List<ShipDto> ships = new ArrayList<ShipDto>(this.shipmap.values());
        Collections.sort(ships, new Comparator<ShipDto>() {
            @Override
            public int compare(ShipDto o1, ShipDto o2) {
                return Integer.compare(o2.getExp(), o1.getExp());
            }
        });
        // コンボボックスに追加
        for (int i = 0; i < ships.size(); i++) {
            String key = this.getShipLabel(ships.get(i), padlength);
            this.shipcombo.add(key);
        }
        // コントロールを再配置
        this.shipcombo.pack();
        this.shipcombo.getParent().layout();
    }

    /**
     * 艦娘のプルダウン表示用文字列を作成します
     * 
     * @param ship
     * @param padlength
     * @return
     */
    private String getShipLabel(ShipDto ship, int padlength) {
        return new StringBuilder().append(StringUtils.leftPad(String.valueOf(ship.getId()), padlength, '0'))
                .append(": ").append(ship.getName()).append(" (Lv").append(ship.getLv() + ")").toString();
    }

    /**
     * グループを追加するときに呼び出されるアダプター
     * 
     */
    private static final class AddGroupAdapter extends SelectionAdapter {

        /** ダイアログ */
        private final ShipFilterGroupDialog dialog;

        /**
         * コンストラクター
         */
        public AddGroupAdapter(ShipFilterGroupDialog dialog) {
            this.dialog = dialog;
        }

        private ShipGroupBean createNewGroup(List<ShipGroupBean> list) {
            Set<Integer> idSet = new HashSet<Integer>();
            for (ShipGroupBean shipGroup : list) {
                idSet.add(shipGroup.getId());
            }
            Random rnd = new Random(System.currentTimeMillis());
            // 一意のIDを生成する
            int newId = rnd.nextInt();
            while (idSet.contains(newId) || (newId == 0))
                newId = rnd.nextInt();
            ShipGroupBean bean = new ShipGroupBean();
            bean.setId(newId);
            bean.setName("新規グループ");
            list.add(bean);
            return bean;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            List<ShipGroupBean> shipGroupList = ShipGroupConfig.get().getGroup();

            ShipGroupBean bean = this.createNewGroup(shipGroupList);

            TableItem item = new TableItem(this.dialog.groupTable, SWT.NONE);
            //item.setImage(SWTResourceManager.getImage(ShipFilterGroupDialog.class, AppConstants.R_ICON_FOLDER));
            item.setText(bean.getName());
            item.setData(new GroupProperty(bean, item));

            ShipGroupObserver.listChanged();
        }
    }

    /**
     * グループを除去するときに呼び出されるアダプター
     *
     */
    private static final class RemoveGroupAdapter extends SelectionAdapter {

        /** ダイアログ */
        private final ShipFilterGroupDialog dialog;

        /**
         * コンストラクター
         */
        public RemoveGroupAdapter(ShipFilterGroupDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (this.dialog.property != null) {

                ShipGroupBean target = this.dialog.property.getShipGroupBean();

                MessageBox box = new MessageBox(this.dialog.shell, SWT.YES | SWT.NO
                        | SWT.ICON_QUESTION);
                box.setText("グループを除去");
                box.setMessage("「" + target.getName() + "」を除去しますか？");
                if (box.open() == SWT.YES) {
                    List<ShipGroupBean> shipGroupList = ShipGroupConfig.get().getGroup();
                    for (int i = 0; i < shipGroupList.size(); i++) {
                        if (shipGroupList.get(i) == target) {
                            shipGroupList.remove(i);
                        }
                    }
                    this.dialog.text.setText("");
                    this.dialog.property.getTreeItem().dispose();
                    this.dialog.property = null;

                    ShipGroupObserver.listChanged();
                }
            }
        }
    }

    /**
     * 名前を変更した時に呼び出されるアダプター
     * 
     */
    private static final class ModifyNameAdapter implements ModifyListener {

        /** ダイアログ */
        private final ShipFilterGroupDialog dialog;

        /**
         * コンストラクター
         */
        public ModifyNameAdapter(ShipFilterGroupDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void modifyText(ModifyEvent e) {
            if (this.dialog.property != null) {
                ShipGroupBean group = this.dialog.property.getShipGroupBean();
                String text = this.dialog.text.getText();
                String groupName = group.getName();
                if (!text.equals(groupName)) { // 変わった時だけ
                    this.dialog.property.getTreeItem().setText(text);
                    group.setName(text);
                    ShipGroupObserver.groupNameChanged(group);
                }
            }
        }
    }

    /**
     * ツリーをクリックした時に呼び出されるアダプター
     *
     */
    private static final class GroupTableSelectionAdapter extends SelectionAdapter {

        /** ダイアログ */
        private final ShipFilterGroupDialog dialog;

        /**
         * コンストラクター
         */
        public GroupTableSelectionAdapter(ShipFilterGroupDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.item != null) {
                Object data = e.item.getData();
                if (data instanceof GroupProperty) {
                    this.dialog.property = (GroupProperty) data;
                } else {
                    this.dialog.property = null;
                }
                this.dialog.reloadTable();
            }
        }
    }

    /**
     * 艦娘を追加するときに呼び出されるアダプター
     *
     */
    private static final class AddShipAdapter extends SelectionAdapter {

        /** ダイアログ */
        private final ShipFilterGroupDialog dialog;

        /**
         * コンストラクター
         */
        public AddShipAdapter(ShipFilterGroupDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (this.dialog.property != null) {
                if (this.dialog.shipcombo.getSelectionIndex() >= 0) {
                    ShipGroupBean target = this.dialog.property.getShipGroupBean();
                    ShipDto ship = this.dialog.shipmap.get(this.dialog.shipcombo.getItem(this.dialog.shipcombo
                            .getSelectionIndex()));
                    target.getShips().add(ship.getId());
                    ShipGroupObserver.groupShipChanged(target);
                }
            }
        }
    }

    /**
     * 艦娘を除去するときに呼び出されるアダプター
     *
     */
    private static final class RemoveShipAdapter extends SelectionAdapter {

        /** ダイアログ */
        private final ShipFilterGroupDialog dialog;

        /**
         * コンストラクター
         */
        public RemoveShipAdapter(ShipFilterGroupDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (this.dialog.property != null) {
                ShipGroupBean bean = this.dialog.property.getShipGroupBean();
                TableItem[] items = this.dialog.table.getSelection();
                List<Integer> ids = new ArrayList<>();
                List<String> name = new ArrayList<>();
                for (TableItem tableItem : items) {
                    ShipDto ship = (ShipDto) tableItem.getData();
                    ids.add(ship.getId());
                    name.add(ship.getFriendlyName());
                }
                MessageBox box = new MessageBox(this.dialog.shell, SWT.YES | SWT.NO
                        | SWT.ICON_QUESTION);
                box.setText("選択した艦娘をグループから除去");
                box.setMessage("「" + StringUtils.join(name, ",") + "」をグループから除去しますか？");

                if (box.open() == SWT.YES) {
                    for (Integer id : ids) {
                        bean.getShips().remove(id);
                    }
                    ShipGroupObserver.groupShipChanged(bean);
                }
            }
        }
    }

    /**
     * 艦娘を除去するときに呼び出されるアダプター
     *
     */
    private static final class AddFleetShipAdapter extends SelectionAdapter {

        /** ダイアログ */
        private final ShipFilterGroupDialog dialog;

        private final String fleetid;

        /**
         * コンストラクター
         */
        public AddFleetShipAdapter(ShipFilterGroupDialog dialog, String fleetid) {
            this.dialog = dialog;
            this.fleetid = fleetid;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (this.dialog.property != null) {
                ShipGroupBean bean = this.dialog.property.getShipGroupBean();
                DockDto dto = GlobalContext.getDock(this.fleetid);
                if ((dto != null) && (dto.getShips().size() > 0)) {
                    List<Integer> ids = new ArrayList<>();
                    List<String> name = new ArrayList<>();
                    for (ShipDto ship : dto.getShips()) {
                        ids.add(ship.getId());
                        name.add(ship.getFriendlyName());
                    }
                    MessageBox box = new MessageBox(this.dialog.shell, SWT.YES | SWT.NO
                            | SWT.ICON_QUESTION);
                    box.setText("選択した艦娘をグループに追加");
                    box.setMessage("「" + StringUtils.join(name, ",") + "」をグループに追加しますか？");

                    if (box.open() == SWT.YES) {
                        for (Integer id : ids) {
                            bean.getShips().add(id);
                        }
                        ShipGroupObserver.groupShipChanged(bean);
                    }
                }
            }
        }
    }

    /**
     * テーブルに表示する所有艦娘のグループ
     *
     */
    private static final class GroupProperty {

        /** 所有艦娘のグループ */
        private final ShipGroupBean shipGroup;

        /** TreeItem */
        private TableItem item;

        /**
         * コンストラクター
         * 
         * @param shipGroup 所有艦娘のグループ
         * @param item TreeItem
         */
        public GroupProperty(ShipGroupBean shipGroup, TableItem item) {
            this.shipGroup = shipGroup;
            this.item = item;
        }

        /**
         * 所有艦娘のグループを取得します
         * 
         * @return 所有艦娘のグループ
         */
        public ShipGroupBean getShipGroupBean() {
            return this.shipGroup;
        }

        /**
         * TreeItemを取得します
         * @return TreeItem
         */
        public TableItem getTreeItem() {
            return this.item;
        }

        /**
         * @param item2
         */
        public void setTreeItem(TableItem item2) {
            this.item = item2;
        }

        /**
         * 艦娘のリストを取得します
         * 
         * @return 艦娘のリスト
         */
        public List<ShipDto> getShipList() {
            List<ShipDto> ships = new ArrayList<ShipDto>();
            Map<Integer, ShipDto> shipMap = GlobalContext.getShipMap();
            for (int id : this.shipGroup.getShips()) {
                ShipDto ship = shipMap.get(id);
                if (ship != null) {
                    ships.add(ship);
                }
            }
            return ships;
        }
    }

    @Override
    public void listChanged() {
        // 自分で変更しているので何もしない
    }

    @Override
    public void groupNameChanged(ShipGroupBean group) {
        // 自分で変更しているので何もしない
    }

    @Override
    public void groupShipChanged(ShipGroupBean group) {
        if ((this.property != null) && (this.property.getShipGroupBean() == group)) {
            this.reloadTable();
        }
    }

    /**
     * 更新する必要のあるデータ
     */
    @Override
    public void update(DataType type, Data data) {
        if (ReportUtils.isShipUpdate(type)) {
            this.needsUpdate = true;
        }
    }
}
