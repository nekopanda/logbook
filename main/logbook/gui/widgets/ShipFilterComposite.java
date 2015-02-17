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
import logbook.gui.logic.ShipGroupListener;
import logbook.gui.logic.ShipGroupObserver;
import logbook.internal.ShipStyle;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

/**
 * 所有艦娘一覧で使用するフィルターダイアログ
 * 
 */
public final class ShipFilterComposite extends Composite {

    private final ShipTable shipTable;

    /** 変更イベントを発生させるか？ */
    private boolean changeEnabled = false;

    private Group groupGroup;

    private Button groupAllButton;
    private Button groupShipTypeButton;

    private final List<Button> groupButtons = new ArrayList<>();

    private ShipGroupBean selectedGroup;

    /** 名前 */
    private Combo nametext;
    /** 名前.正規表現を使用する */
    private Button regexp;

    private Group shiptypegroup;

    private final Map<Integer, Button> shipTypeButtons = new TreeMap<>();
    private int maxTypeId;

    /** 全て選択 */
    private Button selectall;

    /** 鍵付き */
    private Button lockedAny;
    /** 鍵付き */
    private Button lockedOnly;
    /** 鍵付きではない */
    private Button lockedNo;
    /** 艦隊に所属 */
    private Button onlyOnFleet;
    /** 遠征中 */
    public Button exceptOnMission;
    /** 要修理 */
    public Button needBath;

    /**
     * Create the dialog.
     * 
     * @param parent シェル
     * @param shipTable 呼び出し元
     * @param filter 初期値
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
        SelectionListener listener = new ApplyFilterSelectionAdapter();
        SelectionListener groupListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ShipFilterComposite.this.groupButtonSelected((Button) e.getSource(), null);
            }
        };

        this.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout glComposite = new GridLayout(2, false);
        glComposite.verticalSpacing = 0;
        glComposite.horizontalSpacing = 0;
        glComposite.marginHeight = 0;
        glComposite.marginWidth = 0;
        this.setLayout(glComposite);

        this.groupGroup = new Group(this, SWT.NONE);
        this.groupGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        this.groupGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        this.groupGroup.setText("グループ");

        this.groupAllButton = new Button(this.groupGroup, SWT.RADIO);
        this.groupAllButton.setText("すべて");
        this.groupAllButton.addSelectionListener(groupListener);

        this.groupShipTypeButton = new Button(this.groupGroup, SWT.RADIO);
        this.groupShipTypeButton.setText("艦種");
        this.groupShipTypeButton.addSelectionListener(groupListener);

        this.shiptypegroup = new Group(this, SWT.NONE);
        this.shiptypegroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        this.shiptypegroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        this.shiptypegroup.setText("艦種");

        this.selectall = new Button(this.shiptypegroup, SWT.CHECK);
        this.selectall.setText("全て選択");
        this.selectall.addSelectionListener(new SelectAllSelectionAdapter());

        ///////////////////////////////////

        Group etcgroup = new Group(this, SWT.NONE);
        etcgroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        GridLayout glEtcgroup = new GridLayout(3, false);
        glEtcgroup.verticalSpacing = 0;
        glEtcgroup.horizontalSpacing = 0;
        glEtcgroup.marginHeight = 0;
        glEtcgroup.marginWidth = 0;
        etcgroup.setLayout(glEtcgroup);
        etcgroup.setText("その他");

        this.lockedAny = new Button(etcgroup, SWT.RADIO);
        this.lockedAny.setText("すべて");
        this.lockedAny.addSelectionListener(listener);

        this.lockedOnly = new Button(etcgroup, SWT.RADIO);
        this.lockedOnly.setText("鍵付き");
        this.lockedOnly.addSelectionListener(listener);

        this.lockedNo = new Button(etcgroup, SWT.RADIO);
        this.lockedNo.setText("鍵付きではない");
        this.lockedNo.addSelectionListener(listener);

        this.onlyOnFleet = new Button(etcgroup, SWT.CHECK);
        this.onlyOnFleet.setText("艦隊に所属");
        this.onlyOnFleet.setSelection(false);
        this.onlyOnFleet.addSelectionListener(listener);

        this.exceptOnMission = new Button(etcgroup, SWT.CHECK);
        this.exceptOnMission.setText("遠征中を除外");
        this.exceptOnMission.setSelection(false);
        this.exceptOnMission.addSelectionListener(listener);

        this.needBath = new Button(etcgroup, SWT.CHECK);
        this.needBath.setText("お風呂に入りたい艦娘");
        this.needBath.setSelection(false);
        this.needBath.addSelectionListener(listener);

        //-----------　フリーワード
        Group namegroup = new Group(this, SWT.NONE);
        namegroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        namegroup.setLayout(new RowLayout());
        namegroup.setText("フリーワード検索(半角SPでAND検索)");

        this.nametext = new Combo(namegroup, SWT.BORDER);
        this.nametext.setLayoutData(new RowData(180, SWT.DEFAULT));
        this.nametext.addModifyListener(new ApplyFilterModifyAdapter());
        this.nametext.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // 装備から選択された場合は正規表現をオフ
                ShipFilterComposite.this.regexp.setSelection(false);
            }
        });

        this.regexp = new Button(namegroup, SWT.CHECK);
        this.regexp.setText("正規表現");
        this.regexp.addSelectionListener(listener);
        //------------------

        final ShipGroupListener shipGroupListner = new ShipGroupListener() {
            @Override
            public void listChanged() {
                ShipFilterComposite.this.setRedraw(false);
                ShipFilterComposite.this.recreateGroupButtons();
                ShipFilterComposite.this.groupGroup.layout();
                ShipFilterComposite.this.setRedraw(true);
            }

            @Override
            public void groupNameChanged(ShipGroupBean group) {
                int idx = ShipGroupConfig.get().getGroup().indexOf(group);
                if (idx != -1) {
                    ShipFilterComposite.this.groupButtons.get(idx).setText(group.getName());
                    ShipFilterComposite.this.groupGroup.layout();
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
        this.changeEnabled = true;
    }

    /**
     * 艦種ボタンを有効・無効化
     * @param enable
     */
    private void enableTypeButtons(boolean enable) {
        for (Button button : this.shipTypeButtons.values()) {
            button.setEnabled(enable);
        }
        this.selectall.setEnabled(enable);
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
        SelectionListener listener = new GroupButtonSelectionAdapter();
        for (ShipGroupBean group : ShipGroupConfig.get().getGroup()) {
            Button button = new Button(this.groupGroup, SWT.RADIO);
            button.setText(group.getName());
            button.setData(group);
            button.addSelectionListener(listener);
            this.groupButtons.add(button);
        }
        this.groupGroup.layout();
    }

    /**
     * 艦種ボタンを削除して再作成
     */
    private void recreateShipTypeButtonos() {
        for (Button button : this.shipTypeButtons.values()) {
            button.setMenu(null);
            button.dispose();
        }
        this.shipTypeButtons.clear();
        SelectionListener listener = new ApplyFilterSelectionAdapter();
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
            Button button = new Button(this.shiptypegroup, SWT.CHECK);
            button.setText(name);
            button.setData(key);
            button.setSelection(true);
            button.addSelectionListener(listener);
            this.shipTypeButtons.put(key, button);
            this.maxTypeId = Math.max(key, this.maxTypeId);
        }
        this.shiptypegroup.layout();
    }

    /**
     * フィルタデータをパネルに反映
     * グループや艦種などが作られている必要がある
     * @param filter
     */
    private void applyFilter(ShipFilterDto filter) {
        // 選択状態を初期化
        this.groupAllButton.setSelection(false);
        this.groupShipTypeButton.setSelection(false);
        for (Button button : this.groupButtons) {
            button.setSelection(false);
        }
        for (Button button : this.shipTypeButtons.values()) {
            button.setSelection(true);
        }
        this.lockedNo.setSelection(false);
        this.lockedOnly.setSelection(false);
        this.lockedAny.setSelection(false);

        // 選択すべきグループボタン
        Button selectedGroupButton = null;

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
                if (this.shipTypeButtons.containsKey(i)) {
                    if (filter.enabledType[i] == false) {
                        allselected = false;
                    }
                    this.shipTypeButtons.get(i).setSelection(filter.enabledType[i]);
                }
            }
        }
        this.selectall.setSelection(allselected);
        if (filter.typeEnabled) {
            selectedGroupButton = this.groupShipTypeButton;
        }
        else {
            selectedGroupButton = this.groupAllButton;
        }

        if (filter.group != null) {
            // グループ
            int idx = ShipGroupConfig.get().getGroup().indexOf(filter.group);
            if (idx != -1) {
                selectedGroupButton = this.groupButtons.get(idx);
            }
            this.selectedGroup = filter.group;
        }

        // グループボタン
        selectedGroupButton.setSelection(true);
        this.enableTypeButtons(selectedGroupButton == this.groupShipTypeButton);

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
        this.onlyOnFleet.setSelection(!filter.notonfleet);
        // 遠征中を除外
        this.exceptOnMission.setSelection(!filter.mission);
        // お風呂に入りたい
        this.needBath.setSelection(!filter.notneedbath);
    }

    public Combo getSearchCombo() {
        return this.nametext;
    }

    private void groupButtonSelected(Button button, Object data) {
        // ラジオボタンはOFFになった時もSelectedが呼ばれるのでONになったものだけ処理する
        if (this.changeEnabled && button.getSelection()) {
            this.selectedGroup = null;
            if ((button != this.groupAllButton) &&
                    (button != this.groupShipTypeButton))
            {
                this.selectedGroup = (ShipGroupBean) data;
            }
            this.enableTypeButtons(button == this.groupShipTypeButton);
            this.shipTable.updateFilter(this.createFilter());
        }
    }

    /**
     * フィルターを構成する
     * 
     * @return フィルター
     */
    private ShipFilterDto createFilter() {
        ShipFilterDto filter = this.shipTable.getFilter();
        filter.nametext = this.nametext.getText();
        filter.regexp = this.regexp.getSelection();

        filter.enabledType = new boolean[this.maxTypeId + 1];
        for (Button button : this.shipTypeButtons.values()) {
            Integer id = (Integer) button.getData();
            filter.enabledType[id] = button.getSelection();
        }
        filter.typeEnabled = this.selectall.getEnabled();

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
        filter.onfleet = true;
        filter.notonfleet = !this.onlyOnFleet.getSelection();
        filter.mission = !this.exceptOnMission.getSelection();
        filter.notmission = true;
        filter.needbath = true;
        filter.notneedbath = !this.needBath.getSelection();

        return filter;
    }

    /**
     * フィルターを適用する
     */
    private final class SelectAllSelectionAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            boolean select = ShipFilterComposite.this.selectall.getSelection();
            for (Button button : ShipFilterComposite.this.shipTypeButtons.values()) {
                button.setSelection(select);
            }
            if (ShipFilterComposite.this.changeEnabled)
                ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter());
        }
    }

    /**
     * フィルターを適用する
     */
    private final class ApplyFilterModifyAdapter implements ModifyListener {
        @Override
        public void modifyText(ModifyEvent e) {
            if (ShipFilterComposite.this.changeEnabled)
                ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter());
        }
    }

    /**
     * フィルターを適用する
     */
    private final class ApplyFilterSelectionAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            Button button = (Button) e.getSource();
            if (button.getSelection() == false) {
                ShipFilterComposite.this.selectall.setSelection(false);
            }
            if (ShipFilterComposite.this.changeEnabled)
                ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter());
        }
    }

    /**
     * フィルターを適用する
     */
    private final class GroupButtonSelectionAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            Button button = (Button) e.getSource();
            if (button.getSelection()) {
                ShipFilterComposite.this.groupButtonSelected(button, button.getData());
            }
        }
    }
}