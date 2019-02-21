package logbook.gui.widgets;

import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import logbook.config.ShipGroupConfig;
import logbook.config.bean.ShipGroupBean;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.ItemDto;
import logbook.dto.ShipFilterDto;
import logbook.dto.ShipFilterItemDto;
import logbook.gui.ShipFilterGroupDialog;
import logbook.gui.ShipTable;
import logbook.gui.WindowBase;
import logbook.gui.logic.LayoutLogic;
import logbook.gui.logic.ShipGroupListener;
import logbook.gui.logic.ShipGroupObserver;
import logbook.internal.MasterData;
import logbook.util.SwtUtils;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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

    private Composite etcExCompo;
    private Composite filterComposite;
    private Combo filterCombo;
    private ScrolledComposite filterScroll;
    private Composite filterMainFrame;
    private ArrayList<ShipFilterItemDto> filterList = new ArrayList<>();
    private List<Composite> compositeList = new ArrayList<>();
    private List<Button> filterBtnList = new ArrayList<>();

    private Label fleetExp;

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

    private void addFilterContents(ShipFilterItemDto filter) {
        addFilterContents(filter.type);
        int index = this.compositeList.size() - 1;
        Control[] controls = this.compositeList.get(index).getChildren();
        switch(filter.type){
            case ID:
            case LV:
            case COND:
            case REPAIR:
            case FIRE_POWER:
            case TORPEDO:
            case AA:
            case ARMOR:
            case NIGHT_BATTLE:
            case ASW:
            case EVASION:
            case LOS:
            case LUCK:
                ((Combo) controls[1]).setText(filter.sign.getText());
                ((Spinner) controls[2]).setSelection(filter.value);
                this.filterList.get(index).sign = filter.sign;
                this.filterList.get(index).value = filter.value;
                break;
            case LOCK:
            case DOCK:
            case EXPEDITION:
            case EXPANSION:
            case SHIP_TYPE:
            case FLEET:
            case DAMAGED:
            case SPEED:
            case SALLY_AREA:
                for (int i = 0;i < controls.length;i++) {
                    Button button = (Button) controls[i];
                    button.setSelection(filter.enabledType.get(i));
                    this.filterList.get(index).enabledType.set(i, filter.enabledType.get(i));
                }
                break;
        }
    }

    private void addFilterContents(ShipFilterItemDto.FilterType type) {
        switch(type){
            case ID:
            case LV:
            case COND:
            case REPAIR:
            case FIRE_POWER:
            case TORPEDO:
            case AA:
            case ARMOR:
            case NIGHT_BATTLE:
            case ASW:
            case EVASION:
            case LOS:
            case LUCK:
                Composite c = new Composite(this.filterMainFrame, SWT.NONE);
                c.setLayoutData(new GridData(GridData.FILL_BOTH));
                c.setLayout(new GridLayout(3,true));
                c.setData(type);
                c.pack();

                Label label = new Label(c,SWT.NULL);
                label.setText(type.getText());
                label.setSize(label.computeSize(30,SWT.DEFAULT));
                label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                Combo compCombo = new Combo(c, SWT.READ_ONLY);
                Arrays.stream(ShipFilterItemDto.EqualSign.values()).map(value -> value.getText()).forEach(str -> compCombo.add(str));
                compCombo.select(0);
                compCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                compCombo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Combo combo = (Combo) e.widget;
                        ShipFilterItemDto item = filterList.get(compositeList.indexOf(combo.getParent()));
                        item.sign = ShipFilterItemDto.EqualSign.codeOf(combo.getText());
                        ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter());
                    }
                });
                Spinner spinner = new Spinner(c, SWT.NONE);
                spinner.setSelection(1);
                spinner.setMinimum(0);
                spinner.setMaximum(Spinner.LIMIT);
                spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                spinner.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Spinner spinner = (Spinner) e.widget;
                        ShipFilterItemDto item = filterList.get(compositeList.indexOf(spinner.getParent()));
                        item.value = Integer.parseInt(spinner.getText());
                        ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter());
                    }
                });
                this.compositeList.add(c);
                this.filterList.add(new ShipFilterItemDto(type, ShipFilterItemDto.EqualSign.codeOf(compCombo.getText()), Integer.parseInt(spinner.getText())));
                break;
            case LOCK:
            case DOCK:
            case EXPEDITION:
            case EXPANSION:
                Composite c2 = new Composite(this.filterMainFrame, SWT.NONE);
                c2.setLayoutData(new GridData(GridData.FILL_BOTH));
                c2.setData(type);
                c2.pack();

                Button b = new Button(c2, SWT.CHECK);
                b.setText(type.getText());
                b.pack();
                b.setSelection(true);
                b.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Button b = (Button) e.widget;
                        ShipFilterItemDto item = filterList.get(compositeList.indexOf(b.getParent()));
                        item.enabledType.set(0, !item.enabledType.get(0));
                        ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter());
                    }
                });
                this.compositeList.add(c2);
                this.filterList.add(new ShipFilterItemDto(type, new ArrayList<>(Arrays.asList(b.getSelection()))));
                break;
            case SHIP_TYPE:
            case FLEET:
            case DAMAGED:
            case SPEED:
            case SALLY_AREA:
                Group group = new Group(this.filterMainFrame, SWT.NONE);
                group.setText(type.getText());
                group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                group.setLayout(new RowLayout(SWT.HORIZONTAL));
                group.setData(type);
                group.pack();

                List<String> list = ((Supplier<List<String>>) () -> {
                    switch (type) {
                        case SHIP_TYPE:
                            return MasterData.get().getStart2().getStype().stream().filter(stype -> {
                                int key = stype.getId();
                                return !(AppConstants.SHIP_TYPE_INFO.containsKey(key) && AppConstants.SHIP_TYPE_INFO.get(key).equals("#"));
                            }).map(stype -> {
                                int key = stype.getId();
                                if (AppConstants.SHIP_TYPE_INFO.containsKey(key)) {
                                    return AppConstants.SHIP_TYPE_INFO.get(key);
                                }
                                return stype.getName();
                            }).collect(Collectors.toList());
                        case FLEET:
                            return Arrays.asList("なし","第一艦隊", "第二艦隊", "第三艦隊", "第四艦隊");
                        case DAMAGED:
                            return Arrays.asList("健在", "小破", "中破", "大破");
                        case SPEED:
                            return Arrays.asList("低速", "高速", "高速+", "最速");
                        case SALLY_AREA:
                            return Arrays.asList("札なし","札A", "札B", "札C", "札D", "札E", "札F");
                    }
                    return new ArrayList<>();
                }).get();
                list.forEach(str -> {
                    Button button = new Button(group, SWT.CHECK);
                    button.setText(str);
                    button.setSelection(true);
                    button.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            Button b = (Button) e.widget;
                            ShipFilterItemDto item = filterList.get(compositeList.indexOf(b.getParent()));
                            Control[] controls = b.getParent().getChildren();
                            int index = Arrays.asList(controls).indexOf(b);
                            item.enabledType.set(index, !item.enabledType.get(index));
                            ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter());
                        }
                    });
                    button.pack();
                });
                this.compositeList.add(group);
                this.filterList.add(new ShipFilterItemDto(type, new ArrayList<>(list.stream().map(f -> new Boolean(true)).collect(Collectors.toList()))));
                break;
        }
        Button btnRemove = new Button(this.filterMainFrame, SWT.NONE);
        btnRemove.setData(this.compositeList.size() - 1);
        this.filterBtnList.add(btnRemove);
        btnRemove.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = (int)e.widget.getData();
                compositeList.get(index).dispose();
                compositeList.remove(index);
                filterList.remove(index);
                filterBtnList.get(index).dispose();
                filterBtnList.remove(index);
                IntStream.range(index, filterBtnList.size()).boxed().map(i -> filterBtnList.get(i)).forEach(button ->
                        button.setData((int)button.getData() - 1));
                filterMainFrame.layout();
                filterScroll.setMinHeight(filterMainFrame.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
                ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter());
            }
        });
        btnRemove.setImage(SWTResourceManager.getImage(ShipFilterGroupDialog.class, AppConstants.R_ICON_DELETE));
        btnRemove.pack();
        btnRemove.setLayoutData(new GridData(SWT.CENTER, SWT.RIGHT, false, false, 1, 1));
        this.filterMainFrame.layout();
        this.filterScroll.setMinHeight(filterMainFrame.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
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

        this.etcExCompo = new Composite(this.switchCompo, SWT.NONE);
        this.etcExCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        this.etcExCompo.setLayout(SwtUtils.makeGridLayout(1, 0, 0, 0, 0));

        this.filterComposite = new Composite(this.etcExCompo, SWT.NONE);
        this.filterComposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true, true));
        GridLayout mainLayout = new GridLayout(2, false);
        mainLayout.verticalSpacing = 1;
        mainLayout.marginWidth = 1;
        mainLayout.marginHeight = 1;
        mainLayout.marginBottom = 1;
        mainLayout.horizontalSpacing = 1;
        this.filterComposite.setLayout(mainLayout);

        this.filterCombo = new Combo(this.filterComposite, SWT.READ_ONLY);
        Arrays.stream(ShipFilterItemDto.FilterType.values()).map(value -> value.getText()).forEach(str -> this.filterCombo.add(str));
        this.filterCombo.select(0);
        SwtUtils.makeGridLayout(4,1,1,0,0);
        this.filterCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        Button btnAdd = new Button(this.filterComposite, SWT.NONE);
        btnAdd.setImage(SWTResourceManager.getImage(ShipFilterGroupDialog.class, AppConstants.R_ICON_ADD));

        this.filterScroll = new ScrolledComposite(this.filterComposite, SWT.V_SCROLL | SWT.BORDER);
        GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true,2,1);
        gd2.heightHint = 120;
        this.filterScroll.setLayoutData(gd2);
        this.filterScroll.setLayout(new FillLayout());
        this.filterScroll.setExpandHorizontal(true);
        this.filterScroll.setExpandVertical(true);
        this.filterScroll.setAlwaysShowScrollBars(true);

        this.filterMainFrame = new Composite(this.filterScroll, SWT.NONE);
        this.filterMainFrame.setLayout(new GridLayout(2, false));
        this.filterMainFrame.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        this.filterScroll.setContent(this.filterMainFrame);
        this.filterScroll.setMinHeight(this.filterMainFrame.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

        btnAdd.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addFilterContents(ShipFilterItemDto.FilterType.codeOf(filterCombo.getText()));
                ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter());
            }
        });

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

        this.fleetExp = new Label(etcSelectCompo, SWT.NONE);

        //-----------　フリーワード
        Composite namegroup = new Composite(this.etcCompo, SWT.NONE);
        namegroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        namegroup.setLayout(new RowLayout(SWT.HORIZONTAL));

        this.nametext = new Combo(namegroup, SWT.BORDER);
        this.nametext.setLayoutData(new RowData(150, SWT.DEFAULT));
        this.nametext.addModifyListener(e -> {
            if (ShipFilterComposite.this.changeEnabled)
                ShipFilterComposite.this.shipTable.updateFilter(ShipFilterComposite.this.createFilter(), false);
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
        gdBtnRight.widthHint = SwtUtils.DPIAwareWidth(24);
        gdBtnRight.heightHint = SwtUtils.DPIAwareHeight(24);
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
        this.addListener(SWT.Dispose, event ->
            ShipGroupObserver.removeListener(shipGroupListner));
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
        Set<String> items = new TreeSet<>();
        for (ItemDto entry : GlobalContext.getItemMap().values()) {
            items.add(entry.getName());
        }
        this.nametext.remove(0, this.nametext.getItemCount() - 1);
        for (String name : items) {
            this.nametext.add(name);
        }
        this.recreateGroupButtons();
        this.recreateShipTypeButtonos();
        this.recreateEtcFilterList();
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
        LayoutLogic.hide(this.etcExCompo, this.groupMode != 2);
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

    public void setFleetExp(String text) {
        this.fleetExp.setText(text);
        this.fleetExp.getParent().layout();
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
            if (this.shipTable.getFilter().group == group) {
                button.setSelection(true);
            }
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
        for (MasterData.ShipTypeDto type : MasterData.get().getStart2().getStype()) {
            String name = type.getName();
            int key = type.getId();
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

    private void recreateEtcFilterList() {
        Arrays.stream(this.filterMainFrame.getChildren()).forEach(control -> control.dispose());
        this.compositeList = new ArrayList<>();
        this.filterList = new ArrayList<>();
        this.filterMainFrame.layout();
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

        Optional.ofNullable(filter.filterList).ifPresent(list ->
            list.forEach(item -> addFilterContents(item)));

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

        filter.filterList = this.filterList;

        return filter;
    }
}