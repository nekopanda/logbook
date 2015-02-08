package logbook.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logbook.config.ShipGroupConfig;
import logbook.config.bean.ShipGroupBean;
import logbook.config.bean.ShipGroupListBean;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.ShipDto;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * グループエディター
 *
 */
public final class ShipFilterGroupDialog extends AbstractTableDialog {

    private Text text;
    private SashForm sashForm;
    private Composite sideComposite;
    private Composite mainComposite;
    private Combo shipcombo;
    private Tree tree;
    private TreeItem treeItem;
    private Button btnAddShip;
    private Button btnRemoveShip;

    private final Map<String, ShipDto> shipmap = new HashMap<String, ShipDto>();

    /** 現在表示しているグループ */
    private GroupProperty property;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public ShipFilterGroupDialog(Shell parent) {
        super(parent);
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

        this.tree = new Tree(this.sideComposite, SWT.BORDER);
        this.tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        this.tree.addSelectionListener(new TreeSelectionAdapter(this));

        this.treeItem = new TreeItem(this.tree, SWT.NONE);
        this.treeItem.setImage(SWTResourceManager.getImage(ShipFilterGroupDialog.class, AppConstants.R_ICON_STAR));
        this.treeItem.setText("グループ");

        ShipGroupListBean shipGroupList = ShipGroupConfig.get();

        for (ShipGroupBean bean : shipGroupList.getGroup()) {
            TreeItem groupItem = new TreeItem(this.treeItem, SWT.NONE);
            groupItem.setImage(SWTResourceManager
                    .getImage(ShipFilterGroupDialog.class, AppConstants.R_ICON_FOLDER));
            groupItem.setText(bean.getName());
            groupItem.setData(new GroupProperty(bean, groupItem));
        }
        this.treeItem.setExpanded(true);

        this.mainComposite = new Composite(this.sashForm, SWT.NONE);
        GridLayout mainLayout = new GridLayout(3, false);
        mainLayout.verticalSpacing = 1;
        mainLayout.marginWidth = 1;
        mainLayout.marginHeight = 1;
        mainLayout.marginBottom = 1;
        mainLayout.horizontalSpacing = 1;
        this.mainComposite.setLayout(mainLayout);

        this.text = new Text(this.mainComposite, SWT.BORDER);
        this.text.addModifyListener(new ModifyNameAdapter(this));
        this.text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        this.text.setEnabled(false);
    }

    /**
     * Create contents of the dialog.
     */
    @Override
    protected void createContents() {
        this.table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
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
        this.sashForm.setWeights(new int[] { 2, 5 });
    }

    @Override
    protected Composite getTableParent() {
        return this.mainComposite;
    }

    @Override
    protected String getTitle() {
        return "グループエディター";
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return new String[] { "", "ID", "艦隊", "名前", "艦種", "Lv", "疲労" };
    }

    @Override
    protected void updateTableBody() {
        List<String[]> body = new ArrayList<String[]>();
        if (this.property != null) {
            List<ShipDto> shipList = this.property.getShipList();
            for (int i = 0; i < shipList.size(); i++) {
                ShipDto ship = shipList.get(i);
                body.add(new String[] {
                        Integer.toString(i + 1),
                        Long.toString(ship.getId()),
                        ship.getFleetid(),
                        ship.getName(),
                        ship.getType(),
                        Long.toString(ship.getLv()),
                        Long.toString(ship.getCond())
                });
            }
        }
        this.body = body;
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
                    ShipFilterGroupDialog.this.sortTableItems((TableColumn) e.getSource());
                }
            }
        };
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
        long maxshipid = 0;
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            maxshipid = Math.max(ship.getId(), maxshipid);
        }
        int padlength = Long.toString(maxshipid).length();
        // 表示用文字列と艦娘の紐付けを追加
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            this.shipmap.put(this.getShipLabel(ship, padlength), ship);
        }
        // 艦娘を経験値順でソート
        List<ShipDto> ships = new ArrayList<ShipDto>(this.shipmap.values());
        Collections.sort(ships, new Comparator<ShipDto>() {
            @Override
            public int compare(ShipDto o1, ShipDto o2) {
                return Long.compare(o2.getExp(), o1.getExp());
            }
        });
        // コンボボックスに追加
        for (int i = 0; i < ships.size(); i++) {
            String key = this.getShipLabel(ships.get(i), padlength);
            this.shipcombo.add(key);
        }
        // コントロールを再配置
        this.shipcombo.pack();
        this.shipcombo.getParent().pack();
    }

    /**
     * 艦娘のプルダウン表示用文字列を作成します
     * 
     * @param ship
     * @param padlength
     * @return
     */
    private String getShipLabel(ShipDto ship, int padlength) {
        return new StringBuilder().append(StringUtils.leftPad(Long.toString(ship.getId()), padlength, '0'))
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

        @Override
        public void widgetSelected(SelectionEvent e) {
            List<ShipGroupBean> shipGroupList = ShipGroupConfig.get().getGroup();

            ShipGroupBean bean = new ShipGroupBean();
            bean.setName("新規グループ");
            shipGroupList.add(bean);

            TreeItem item = new TreeItem(this.dialog.treeItem, SWT.NONE);
            item.setImage(SWTResourceManager.getImage(ShipFilterGroupDialog.class, AppConstants.R_ICON_FOLDER));
            item.setText(bean.getName());
            item.setData(new GroupProperty(bean, item));
            this.dialog.treeItem.setExpanded(true);
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
                String text = this.dialog.text.getText();
                this.dialog.property.getTreeItem().setText(text);
                ShipGroupBean group = this.dialog.property.getShipGroupBean();
                group.setName(text);
            }
        }
    }

    /**
     * ツリーをクリックした時に呼び出されるアダプター
     *
     */
    private static final class TreeSelectionAdapter extends SelectionAdapter {

        /** ダイアログ */
        private final ShipFilterGroupDialog dialog;

        /**
         * コンストラクター
         */
        public TreeSelectionAdapter(ShipFilterGroupDialog dialog) {
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
                    this.dialog.reloadTable();
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
                ShipGroupBean target = this.dialog.property.getShipGroupBean();
                TableItem[] items = this.dialog.table.getSelection();
                for (TableItem tableItem : items) {
                    String text = tableItem.getText(1);
                    if (StringUtils.isNumeric(text)) {
                        Long id = Long.valueOf(text);
                        target.getShips().remove(id);
                    }
                }
                this.dialog.reloadTable();
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
        private final TreeItem item;

        /**
         * コンストラクター
         * 
         * @param shipGroup 所有艦娘のグループ
         * @param item TreeItem
         */
        public GroupProperty(ShipGroupBean shipGroup, TreeItem item) {
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
        public TreeItem getTreeItem() {
            return this.item;
        }

        /**
         * 艦娘のリストを取得します
         * 
         * @return 艦娘のリスト
         */
        public List<ShipDto> getShipList() {
            List<ShipDto> ships = new ArrayList<ShipDto>();
            Map<Long, ShipDto> shipMap = GlobalContext.getShipMap();
            for (Long id : this.shipGroup.getShips()) {
                ShipDto ship = shipMap.get(id);
                if (ship != null) {
                    ships.add(ship);
                }
            }
            return ships;
        }
    }
}
