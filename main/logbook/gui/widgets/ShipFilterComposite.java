package logbook.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import logbook.config.ShipGroupConfig;
import logbook.config.bean.ShipGroupBean;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.ItemDto;
import logbook.dto.ShipFilterDto;
import logbook.gui.ShipTable;
import logbook.gui.WindowBase;
import logbook.gui.logic.LayoutLogic;
import logbook.gui.logic.ShipGroupListener;
import logbook.gui.logic.ShipGroupObserver;
import logbook.internal.ShipStyle;
import logbook.util.SwtUtils;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 所有艦娘一覧で使用するフィルターダイアログ
 * 
 */
public final class ShipFilterComposite extends Composite {

    private final ShipTable shipTable;

    /** 変更イベントを発生させるか？ */
    private boolean changeEnabled = false;
    private boolean panelVisible = true;
    private boolean etcVisible = true;
    private int groupMode = 0;

    private Menu switchMenu;
    private MenuItem groupMenuItem;
    private MenuItem typeMenuItem;
    private MenuItem etcMenuItem;

    private Composite switchCompo;

    private Composite groupCompo;
    private Button groupAllButton;
    private final List<Button> groupButtons = new ArrayList<>();
    private ShipGroupBean selectedGroup;

    private Composite typeCompo;
    private Composite typeCheckCompo;
    private final Map<Integer, Button> typeButtons = new TreeMap<>();
    private int maxTypeId;

    private Composite etcCompo;
    /** 名前 */
    private Combo nametext;
    /** 名前.正規表現を使用する */
    private Button regexp;

    /** 全て選択 */
    private Button selectall;

    private Composite lockedGroup;
    /** 鍵付き */
    private Button lockedAny;
    /** 鍵付き */
    private Button lockedOnly;
    /** 鍵付きではない */
    private Button lockedNo;

    private Composite fleetGroup;
    /** 艦隊所属 */
    private Button fleetAny;
    /** 艦隊所属 */
    private Button fleetOnly;
    /** 艦隊所属でない */
    private Button fleetNo;

    /** 遠征中を除外 */
    public Button ignoreOnMission;
    /** 要修理 */
    public Button needBath;

    /**
     * Create the dialog.
     * 
     * @param shipTable 呼び出し元
     */
    public ShipFilterComposite(ShipTable shipTable) {
        super(shipTable.getShell(), SWT.NONE);
        this.shipTable = shipTable;
        this.createContents();
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        // ただ反映するだけ
        SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ShipFilterComposite.this.changeEnabled) {
                    ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter(), true);
                }
            }
        };
        SelectionListener groupListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ShipFilterComposite.this.groupButtonSelected((Button) e.getSource(), null);
            }
        };

        this.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.setLayout(SwtUtils.makeGridLayout(2, 0, 0, 0, 0));

        this.switchCompo = new Composite(this, SWT.NONE);
        this.switchCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        this.switchCompo.setLayout(SwtUtils.makeGridLayout(1, 0, 0, 0, 0));

        // グループタブ
        this.groupCompo = new Composite(this.switchCompo, SWT.BORDER);
        this.groupCompo.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));
        this.groupCompo.setLayout(SwtUtils.makeRowLayout(true, 0, 0, true));

        this.groupAllButton = new Button(this.groupCompo, SWT.RADIO);
        this.groupAllButton.setText("すべて");
        this.groupAllButton.addSelectionListener(groupListener);

        // 艦種タブ
        this.typeCompo = new Composite(this.switchCompo, SWT.NONE);
        this.typeCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        this.typeCompo.setLayout(SwtUtils.makeGridLayout(1, 0, 0, 0, 0));

        // 艦種カテゴリボタン
        this.typeCheckCompo = new Composite(this.typeCompo, SWT.NONE);
        this.typeCheckCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        this.typeCheckCompo.setLayout(SwtUtils.makeRowLayout(true, 1, 0, true));

        this.selectall = new Button(this.typeCheckCompo, SWT.CHECK);
        this.selectall.setText("全て選択");
        this.selectall.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean select = ShipFilterComposite.this.selectall.getSelection();
                for (Button button : ShipFilterComposite.this.typeButtons.values()) {
                    button.setSelection(select);
                }
                if (ShipFilterComposite.this.changeEnabled) {
                    ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter(), true);
                }
            }
        });

        Composite typeSelectorCompo = new Composite(this.typeCompo, SWT.NONE);
        typeSelectorCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        typeSelectorCompo.setLayout(new RowLayout(SWT.HORIZONTAL));

        SelectionListener categoryListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ShipFilterComposite.this.categoryButtonSelected((Button) e.getSource());
            }
        };
        String[] categoryNames = AppConstants.SHIP_CATEGORY_NAMES;
        int[][] categoryTypes = AppConstants.SHIP_CATEGORY_TYPES;
        for (int i = 0; i < categoryNames.length; ++i) {
            Button button = new Button(typeSelectorCompo, SWT.NONE);
            button.setText(categoryNames[i]);
            button.setData(categoryTypes[i]);
            button.addSelectionListener(categoryListener);
        }

        // その他パネル
        this.etcCompo = new Composite(this.switchCompo, SWT.NONE);
        this.etcCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        this.etcCompo.setLayout(SwtUtils.makeGridLayout(2, 0, 0, 0, 0));

        Composite etcSelectCompo = new Composite(this.etcCompo, SWT.NONE);
        etcSelectCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        etcSelectCompo.setLayout(SwtUtils.makeRowLayout(true, 2, 0, true));

        {
            this.lockedGroup = new Composite(etcSelectCompo, SWT.BORDER);
            this.lockedGroup.setLayout(SwtUtils.makeRowLayout(true, 0, 0, false));

            this.lockedAny = new Button(this.lockedGroup, SWT.RADIO);
            this.lockedAny.setText("すべて");
            this.lockedAny.addSelectionListener(listener);

            this.lockedOnly = new Button(this.lockedGroup, SWT.RADIO);
            this.lockedOnly.setText("鍵付き");
            this.lockedOnly.addSelectionListener(listener);

            this.lockedNo = new Button(this.lockedGroup, SWT.RADIO);
            this.lockedNo.setText("鍵付きでない");
            this.lockedNo.addSelectionListener(listener);
        }

        {
            this.fleetGroup = new Composite(etcSelectCompo, SWT.BORDER);
            this.fleetGroup.setLayout(SwtUtils.makeRowLayout(true, 0, 0, false));

            this.fleetAny = new Button(this.fleetGroup, SWT.RADIO);
            this.fleetAny.setText("すべて");
            this.fleetAny.addSelectionListener(listener);

            this.fleetOnly = new Button(this.fleetGroup, SWT.RADIO);
            this.fleetOnly.setText("艦隊所属");
            this.fleetOnly.addSelectionListener(listener);

            this.fleetNo = new Button(this.fleetGroup, SWT.RADIO);
            this.fleetNo.setText("艦隊所属でない");
            this.fleetNo.addSelectionListener(listener);
        }

        this.ignoreOnMission = new Button(etcSelectCompo, SWT.CHECK);
        this.ignoreOnMission.setText("遠征中を除外");
        this.ignoreOnMission.setSelection(false);
        this.ignoreOnMission.addSelectionListener(listener);

        this.needBath = new Button(etcSelectCompo, SWT.CHECK);
        this.needBath.setText("お風呂に入りたい艦娘");
        this.needBath.setSelection(false);
        this.needBath.addSelectionListener(listener);

        //-----------　フリーワード
        Composite namegroup = new Composite(this.etcCompo, SWT.NONE);
        namegroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        namegroup.setLayout(new RowLayout(SWT.HORIZONTAL));

        this.nametext = new Combo(namegroup, SWT.BORDER);
        this.nametext.setLayoutData(new RowData(150, SWT.DEFAULT));
        this.nametext.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (ShipFilterComposite.this.changeEnabled)
                    ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter(), false);
            }
        });
        this.nametext.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // 装備から選択された場合は正規表現をオフ
                ShipFilterComposite.this.regexp.setSelection(false);
            }
        });
        this.nametext.setToolTipText("フリーワード検索(半角SPでAND検索)");

        this.regexp = new Button(namegroup, SWT.CHECK);
        this.regexp.setToolTipText("正規表現:オフ");
        this.regexp.addSelectionListener(listener);
        this.regexp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button button = (Button) e.widget;
                String tooltip = "正規表現:" + (button.getSelection() ? "オン" : "オフ");
                button.setToolTipText(tooltip);
            }
        });

        //-----------------------------

        //------------------ 切り替えボタン

        SelectionListener arrowButtonListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int next = (ShipFilterComposite.this.groupMode + 1) % 3;
                ShipFilterComposite.this.setGroupMode(next);
                //ShipFilterComposite.this.shipTable.getFilterMenu().setGroupMode(ShipFilterComposite.this.groupMode);
                ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter(), true);
                ShipFilterComposite.this.shipTable.getShell().layout();
            }
        };

        Button btnRight = new Button(this, SWT.NONE);
        GridData gdBtnRight = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
        gdBtnRight.widthHint = 24;
        gdBtnRight.heightHint = 24;
        btnRight.setLayoutData(gdBtnRight);
        btnRight.addSelectionListener(arrowButtonListener);
        SwtUtils.setButtonImage(btnRight, SWTResourceManager.getImage(WindowBase.class, AppConstants.R_ICON_RIGHT));

        //------------------

        this.switchMenu = new Menu(this);

        SelectionListener switchSelectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ShipFilterComposite.this.switchMenuSelected(e);
            }
        };

        this.groupMenuItem = new MenuItem(this.switchMenu, SWT.CHECK);
        this.groupMenuItem.setText("グループ");
        this.groupMenuItem.setSelection(true);
        this.groupMenuItem.addSelectionListener(switchSelectionListener);

        this.typeMenuItem = new MenuItem(this.switchMenu, SWT.CHECK);
        this.typeMenuItem.setText("艦種");
        this.typeMenuItem.addSelectionListener(switchSelectionListener);

        // セパレータ
        new MenuItem(this.switchMenu, SWT.SEPARATOR);

        this.etcMenuItem = new MenuItem(this.switchMenu, SWT.CHECK);
        this.etcMenuItem.setText("その他");
        this.etcMenuItem.setSelection(true);
        this.etcMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean panelVisible = ShipFilterComposite.this.panelVisible;
                boolean etcVisible = ShipFilterComposite.this.etcMenuItem.getSelection();
                ShipFilterComposite.this.setPanelVisible(panelVisible, etcVisible);
                ShipFilterComposite.this.shipTable.getFilterMenu().setPanelVisible(panelVisible, etcVisible);
                ShipFilterComposite.this.shipTable.getShell().layout();
            }
        });

        setMenu(this, this.switchMenu);
        this.setData("disable-window-menu", new Object());

        this.switchPanel();

        final ShipGroupListener shipGroupListner = new ShipGroupListener() {
            @Override
            public void listChanged() {
                ShipFilterComposite.this.setRedraw(false);
                ShipFilterComposite.this.recreateGroupButtons();
                ShipFilterComposite.this.groupCompo.layout();
                ShipFilterComposite.this.setRedraw(true);
            }

            @Override
            public void groupNameChanged(ShipGroupBean group) {
                int idx = ShipGroupConfig.get().getGroup().indexOf(group);
                if (idx != -1) {
                    ShipFilterComposite.this.groupButtons.get(idx).setText(group.getName());
                    ShipFilterComposite.this.groupCompo.layout();
                }
            }

            /* (非 Javadoc)
             * @see logbook.gui.logic.ShipGroupListener#groupShipChanged(logbook.config.bean.ShipGroupBean)
             */
            @Override
            public void groupShipChanged(ShipGroupBean group) {
                // TODO 自動生成されたメソッド・スタブ

            }
        };
        ShipGroupObserver.addListener(shipGroupListner);
        this.addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ShipGroupObserver.removeListener(shipGroupListner);
            }
        });
    }

    /**
     * 現在のデータでパネル表示内容を更新
     * @param filter
     */
    public void updateContents(ShipFilterDto filter) {
        this.updateContents(filter, this.panelVisible, this.etcVisible);
    }

    public void updateContents(ShipFilterDto filter, boolean panelVisible, boolean etcVisible) {
        this.changeEnabled = false;
        Set<String> items = new TreeSet<String>();
        for (ItemDto entry : GlobalContext.getItemMap().values()) {
            items.add(entry.getName());
        }
        this.nametext.remove(0, this.nametext.getItemCount() - 1);
        for (String name : items) {
            this.nametext.add(name);
        }
        this.recreateGroupButtons();
        this.recreateShipTypeButtonos();
        this.applyFilter(filter);
        this.setPanelVisible(panelVisible, etcVisible);
        this.changeEnabled = true;
    }

    private void switchMenuSelected(SelectionEvent e) {
        MenuItem selectedItem = (MenuItem) e.widget;
        int mode = !selectedItem.getSelection() ? 2 :
                (selectedItem == this.groupMenuItem) ? 0 : 1;
        this.setGroupMode(mode);
        //this.shipTable.getFilterMenu().setGroupMode(this.groupMode);
        if (this.changeEnabled) {
            this.shipTable.updateFilter(this.createFilter(), true);
            this.shipTable.getShell().layout();
        }
    }

    private void switchPanel() {
        LayoutLogic.hide(this.groupCompo, this.groupMode != 0);
        LayoutLogic.hide(this.typeCompo, this.groupMode != 1);
        LayoutLogic.hide(this.etcCompo, !this.etcVisible);
    }

    public int getGroupMode() {
        return this.groupMode;
    }

    public void setGroupMode(int mode) {
        if (this.groupMode != mode) {
            this.groupMode = mode;
            this.groupMenuItem.setSelection(mode == 0);
            this.typeMenuItem.setSelection(mode == 1);
            if (this.panelVisible) {
                this.switchPanel();
                //this.switchCompo.layout();
                this.layout();
            }
        }
    }

    public boolean getPanelVisible() {
        return this.panelVisible;
    }

    public boolean getEtcVisible() {
        return this.etcVisible;
    }

    public void setPanelVisible(boolean panelVisible, boolean etcVisible) {
        if ((this.etcVisible != etcVisible) || (this.panelVisible != panelVisible)) {
            this.etcVisible = etcVisible;
            this.panelVisible = panelVisible;
            this.etcMenuItem.setSelection(etcVisible);
            LayoutLogic.hide(this, !panelVisible);
            if (panelVisible) {
                this.switchPanel();
                this.layout();
            }
        }
    }

    private static void setMenu(Control c, Menu ma) {
        if (c instanceof Composite) {
            for (final Control cc : ((Composite) c).getChildren()) {
                setMenu(cc, ma);
            }
        }
        c.setMenu(ma);
    }

    /**
     * 艦種カテゴリボタンが押された
     */
    private void categoryButtonSelected(Button source) {
        this.typeCompo.setRedraw(false);
        // まずはすべてオフ
        for (Button check : this.typeButtons.values()) {
            check.setSelection(false);
        }
        this.selectall.setSelection(false);

        // 指定されたものだけオン
        int[] types = (int[]) source.getData();
        for (int type : types) {
            Button button = this.typeButtons.get(type);
            if (button != null) {
                button.setSelection(true);
            }
        }
        this.typeCompo.setRedraw(true);

        if (this.changeEnabled)
            this.shipTable.updateFilter(this.createFilter(), true);
    }

    /**
     * グループボタンを削除して再作成
     */
    private void recreateGroupButtons() {
        for (Button button : this.groupButtons) {
            button.setMenu(null);
            button.dispose();
        }
        this.groupButtons.clear();
        SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button button = (Button) e.getSource();
                if (button.getSelection()) {
                    ShipFilterComposite.this.groupButtonSelected(button, button.getData());
                }
            }
        };
        for (ShipGroupBean group : ShipGroupConfig.get().getGroup()) {
            Button button = new Button(this.groupCompo, SWT.RADIO);
            button.setText(group.getName());
            button.setData(group);
            button.addSelectionListener(listener);
            button.setMenu(this.switchMenu);
            this.groupButtons.add(button);
        }
        this.groupCompo.layout();
    }

    /**
     * 艦種ボタンを削除して再作成
     */
    private void recreateShipTypeButtonos() {
        for (Button button : this.typeButtons.values()) {
            button.setMenu(null);
            button.dispose();
        }
        this.typeButtons.clear();
        SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button button = (Button) e.getSource();
                if (button.getSelection() == false) {
                    // 艦種のどれかがOFFになったらオフにする
                    ShipFilterComposite.this.selectall.setSelection(false);
                }
                if (ShipFilterComposite.this.changeEnabled)
                    ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter(), true);
            }
        };
        for (Map.Entry<Integer, String> entry : ShipStyle.getMap().entrySet()) {
            String name = entry.getValue();
            int key = entry.getKey();
            if (AppConstants.SHIP_TYPE_INFO.containsKey(key)) {
                name = AppConstants.SHIP_TYPE_INFO.get(key);
                if (name.equals("#")) {
                    // この艦種は表示しない
                    continue;
                }
            }
            Button button = new Button(this.typeCheckCompo, SWT.CHECK);
            button.setText(name);
            button.setData(key);
            button.setSelection(true);
            button.addSelectionListener(listener);
            button.setMenu(this.switchMenu);
            this.typeButtons.put(key, button);
            this.maxTypeId = Math.max(key, this.maxTypeId);
        }
        this.typeCheckCompo.layout();
    }

    /**
     * フィルタデータをパネルに反映
     * グループや艦種などが作られている必要がある
     * @param filter
     */
    public void applyFilter(ShipFilterDto filter) {
        // 選択状態を初期化
        this.groupAllButton.setSelection(false);
        for (Button button : this.groupButtons) {
            button.setSelection(false);
        }
        for (Button button : this.typeButtons.values()) {
            button.setSelection(true);
        }
        this.lockedNo.setSelection(false);
        this.lockedOnly.setSelection(false);
        this.lockedAny.setSelection(false);

        // 名前
        if (!StringUtils.isEmpty(filter.nametext)) {
            this.nametext.setText(filter.nametext);
        }
        // 名前.正規表現を使用する
        this.regexp.setSelection(filter.regexp);

        // 艦種設定
        boolean allselected = true;
        if (filter.enabledType != null) {
            for (int i = 0; i < filter.enabledType.length; ++i) {
                if (this.typeButtons.containsKey(i)) {
                    if (filter.enabledType[i] == false) {
                        allselected = false;
                    }
                    this.typeButtons.get(i).setSelection(filter.enabledType[i]);
                }
            }
        }
        this.selectall.setSelection(allselected);

        // グループ
        Button selectedGroupButton = this.groupAllButton;
        if (filter.group != null) {
            int idx = ShipGroupConfig.get().getGroup().indexOf(filter.group);
            if (idx != -1) {
                selectedGroupButton = this.groupButtons.get(idx);
            }
        }
        this.selectedGroup = filter.group;
        selectedGroupButton.setSelection(true);

        // 鍵付き？
        if (filter.locked == false) {
            this.lockedNo.setSelection(true);
        }
        else if (filter.notlocked == false) {
            this.lockedOnly.setSelection(true);
        }
        else {
            this.lockedAny.setSelection(true);
        }
        // 艦隊に所属
        if (filter.onfleet == false) {
            this.fleetNo.setSelection(true);
        }
        else if (filter.notonfleet == false) {
            this.fleetOnly.setSelection(true);
        }
        else {
            this.fleetAny.setSelection(true);
        }
        // 遠征中を除外
        this.ignoreOnMission.setSelection(!filter.mission);
        // お風呂に入りたい
        this.needBath.setSelection(!filter.notneedbath);

        // タブ選択
        this.setGroupMode(filter.groupMode);
    }

    public Combo getSearchCombo() {
        return this.nametext;
    }

    private void groupButtonSelected(Button button, Object data) {
        // ラジオボタンはOFFになった時もSelectedが呼ばれるのでONになったものだけ処理する
        if (this.changeEnabled && button.getSelection()) {
            this.selectedGroup = null;
            if (button != this.groupAllButton) {
                this.selectedGroup = (ShipGroupBean) data;
            }
            this.shipTable.updateFilter(this.createFilter(), true);
        }
    }

    /**
     * フィルターを構成する
     * 
     * @return フィルター
     */
    public ShipFilterDto createFilter() {
        ShipFilterDto filter = this.shipTable.getFilter();
        filter.nametext = this.nametext.getText();
        filter.regexp = this.regexp.getSelection();

        filter.enabledType = new boolean[this.maxTypeId + 1];
        for (Button button : this.typeButtons.values()) {
            Integer id = (Integer) button.getData();
            filter.enabledType[id] = button.getSelection();
        }

        filter.group = this.selectedGroup;

        if (this.lockedAny.getSelection()) {
            filter.locked = filter.notlocked = true;
        }
        else if (this.lockedOnly.getSelection()) {
            filter.locked = true;
            filter.notlocked = false;
        }
        else {
            filter.locked = false;
            filter.notlocked = true;
        }
        if (this.fleetAny.getSelection()) {
            filter.onfleet = filter.notonfleet = true;
        }
        else if (this.fleetOnly.getSelection()) {
            filter.onfleet = true;
            filter.notonfleet = false;
        }
        else {
            filter.onfleet = false;
            filter.notonfleet = true;
        }
        filter.mission = !this.ignoreOnMission.getSelection();
        filter.notmission = true;
        filter.needbath = true;
        filter.notneedbath = !this.needBath.getSelection();
        filter.groupMode = this.groupMode;

        return filter;
    }
}