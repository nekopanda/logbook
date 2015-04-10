package logbook.gui.widgets;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

import javax.annotation.CheckForNull;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.DockDto;
import logbook.dto.ItemInfoDto;
import logbook.dto.ShipDto;
import logbook.gui.ApplicationMain;
import logbook.gui.logic.SakutekiString;
import logbook.gui.logic.Sound;
import logbook.gui.logic.TimeLogic;
import logbook.gui.logic.TimeString;
import logbook.internal.CondTiming;
import logbook.internal.EvaluateExp;
import logbook.internal.SeaExp;
import logbook.util.CalcExpUtils;
import logbook.util.SwtUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 艦隊タブのウィジェットです
 *
 */
public class FleetComposite extends Composite {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(FleetComposite.class);

    /** 警告 */
    private static final int WARN = 1;
    /** 致命的 */
    private static final int FATAL = 2;
    /** 1艦隊に編成できる艦娘の数 */
    private static final int MAXCHARA = 6;
    /** フォント大きい */
    private final static int LARGE = 2;
    /** フォント小さい */
    private final static int SMALL = -1;

    /** HPゲージ幅 */
    private static final int GAUGE_WIDTH = 50;
    /** HPゲージ高さ */
    private static final int GAUGE_HEIGHT = 12;
    /** 経験値ゲージ高さ */
    private static final int EXP_GAUGE_HEIGHT = 4;
    /** HPゲージ最小色 */
    private static final RGB GAUGE_EMPTY = new RGB(0xff, 0, 0);
    /** HPゲージ中間色 */
    private static final RGB GAUGE_HALF = new RGB(0xff, 0xd7, 0);
    /** HPゲージ最大色 */
    private static final RGB GAUGE_FULL = new RGB(0, 0xd7, 0);
    /** 経験値ゲージ色 */
    private static final RGB EXP_GAUGE = new RGB(0, 0x80, 0xff);

    /** タブ */
    private final CTabItem tab;
    /** メイン画面 */
    private final ApplicationMain main;
    /** 艦隊 */
    private DockDto dock;

    private final Composite fleetGroup;

    /** タブアイコン表示 */
    private final BitSet state = new BitSet();
    /** コンディション最小値(メッセージ表示用) */
    private int cond;
    /** 疲労回復時間(メッセージ表示用) */
    private String clearDate;
    /** 大破している */
    private boolean badlyDamage;

    /** アイコンラベル */
    private final Label[] iconLabels = new Label[MAXCHARA];
    /** 名前ラベル */
    private final Label[] nameLabels = new Label[MAXCHARA];
    /** Lvラベル */
    private final Label[] lvLabels = new Label[MAXCHARA];
    /** HP */
    private final Label[] hpLabels = new Label[MAXCHARA];
    /** HPゲージ */
    private final Label[] hpgaugeLabels = new Label[MAXCHARA];
    /** HPゲージイメージ */
    private final Image[] hpgaugeImages = new Image[MAXCHARA];
    /** HPメッセージ */
    private final Label[] hpmsgLabels = new Label[MAXCHARA];
    /** コンディション */
    private final Label[] condLabels = new Label[MAXCHARA];
    /** コンディションステータス */
    private final Label[] condstLabels = new Label[MAXCHARA];
    /** 弾ステータス */
    private final Label[] bullstLabels = new Label[MAXCHARA];
    /** 燃料ステータス */
    private final Label[] fuelstLabels = new Label[MAXCHARA];
    /** ダメコン or 泊地修理 */
    private final Label[] etc1Labels = new Label[MAXCHARA];
    /** レベリング or 疲労回復 */
    private final Label[] etc2Labels = new Label[MAXCHARA];
    /** メッセージ */
    private final StyledText message;

    /**
     * @param parent 艦隊タブの親
     * @param tabItem 艦隊タブ
     */
    public FleetComposite(CTabFolder parent, CTabItem tabItem, ApplicationMain main) {
        super(parent, SWT.NONE);
        this.tab = tabItem;
        this.main = main;
        this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        GridLayout glParent = new GridLayout(1, false);
        glParent.horizontalSpacing = 0;
        glParent.marginTop = 0;
        glParent.marginWidth = 0;
        glParent.marginHeight = 0;
        glParent.marginBottom = 0;
        glParent.verticalSpacing = 0;
        this.setLayout(glParent);

        this.fleetGroup = new Composite(this, SWT.NONE);
        this.fleetGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout glShipGroup = new GridLayout(3, false);
        glShipGroup.horizontalSpacing = 0;
        glShipGroup.marginTop = 0;
        glShipGroup.marginWidth = 1;
        glShipGroup.marginHeight = 0;
        glShipGroup.marginBottom = 4;
        glShipGroup.verticalSpacing = 0;
        this.fleetGroup.setLayout(glShipGroup);
        this.init();

        // セパレーター
        Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        // メッセージ
        this.message = new StyledText(this, SWT.READ_ONLY | SWT.WRAP);
        this.message.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        this.message.setWordWrap(true);
        this.message.setBackground(this.getBackground());

        this.fleetGroup.layout();
    }

    /**
     * 初期化
     */
    private void init() {
        for (int i = 0; i < MAXCHARA; i++) {
            // アイコン
            Label iconlabel = new Label(this.fleetGroup, SWT.NONE);
            GridData gdIconlabel = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
            gdIconlabel.widthHint = 16;
            iconlabel.setLayoutData(gdIconlabel);
            // 名前
            Composite nameComposite = new Composite(this.fleetGroup, SWT.NONE);
            GridLayout glName = new GridLayout(2, false);
            glName.horizontalSpacing = 0;
            glName.marginTop = 0;
            glName.marginWidth = 1;
            glName.marginHeight = 0;
            glName.marginBottom = 0;
            glName.verticalSpacing = 0;
            nameComposite.setLayout(glName);
            nameComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

            Label namelabel = new Label(nameComposite, SWT.NONE);
            SwtUtils.initLabel(namelabel, "名前", LARGE, new GridData());

            Label lvlabel = new Label(nameComposite, SWT.NONE);
            SwtUtils.initLabel(lvlabel, "Lv.0", SMALL, 1.4, new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1));
            // HP
            Composite hpComposite = new Composite(this.fleetGroup, SWT.NONE);
            GridLayout glHp = new GridLayout(3, false);
            glHp.horizontalSpacing = 0;
            glHp.marginTop = 0;
            glHp.marginWidth = 1;
            glHp.marginHeight = 0;
            glHp.marginBottom = 0;
            glHp.verticalSpacing = 0;
            hpComposite.setLayout(glHp);
            hpComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

            Label hp = new Label(hpComposite, SWT.NONE);
            SwtUtils.initLabel(hp, "/", SMALL, new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
            Label hpgauge = new Label(hpComposite, SWT.NONE);
            hpgauge.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1));
            Label hpmsg = new Label(hpComposite, SWT.NONE);
            SwtUtils.initLabel(hpmsg, "健在", new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));

            // ステータス
            Label skip1 = new Label(this.fleetGroup, SWT.NONE);
            SwtUtils.initLabel(skip1, "", new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));

            Composite stateComposite = new Composite(this.fleetGroup, SWT.NONE);
            GridLayout glState = new GridLayout(6, false);
            glState.horizontalSpacing = 0;
            glState.marginTop = 0;
            glState.marginWidth = 0;
            glState.marginHeight = 0;
            glState.marginBottom = 0;
            glState.verticalSpacing = 0;
            stateComposite.setLayout(glState);
            stateComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Label condst = new Label(stateComposite, SWT.NONE);
            SwtUtils.initLabel(condst, "疲", new GridData());
            Label fuelst = new Label(stateComposite, SWT.NONE);
            SwtUtils.initLabel(fuelst, "燃", new GridData());
            Label bullst = new Label(stateComposite, SWT.NONE);
            SwtUtils.initLabel(bullst, "弾", new GridData());
            Label etc1 = new Label(stateComposite, SWT.NONE);
            SwtUtils.initLabel(etc1, "ダ", new GridData());
            Label etc2 = new Label(stateComposite, SWT.NONE);
            SwtUtils.initLabel(etc2, "ダ", new GridData());

            // 疲労
            Label cond = new Label(this.fleetGroup, SWT.NONE);
            SwtUtils.initLabel(cond, "49 cond.", new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

            this.iconLabels[i] = iconlabel;
            this.nameLabels[i] = namelabel;
            this.lvLabels[i] = lvlabel;
            this.hpLabels[i] = hp;
            this.hpgaugeLabels[i] = hpgauge;
            this.hpmsgLabels[i] = hpmsg;
            this.condLabels[i] = cond;
            this.condstLabels[i] = condst;
            this.bullstLabels[i] = bullst;
            this.fuelstLabels[i] = fuelst;
            this.etc1Labels[i] = etc1;
            this.etc2Labels[i] = etc2;
        }
    }

    /**
     * 艦隊を更新します
     *
     * @param dock
     * @param combinedFleetBadlyDamaed 連合艦隊の他の艦隊の艦が大破している
     */
    public void updateFleet(DockDto dock, boolean combinedFleetBadlyDamaed) {
        if ((this.dock == dock) && !this.dock.isUpdate()) {
            return;
        }

        this.getShell().setRedraw(false);

        this.dock = dock;
        this.state.set(WARN, false);
        this.state.set(FATAL, false);
        this.cond = AppConfig.get().getOkCond();
        this.clearDate = null;
        this.badlyDamage = false;
        this.message.setText("");

        List<ShipDto> ships = dock.getShips();
        boolean[] escaped = dock.getEscaped();
        for (int i = ships.size(); i < MAXCHARA; i++) {
            this.iconLabels[i].setImage(null);
            this.nameLabels[i].setText("");
            this.lvLabels[i].setText("");
            this.hpLabels[i].setText("");
            this.hpgaugeLabels[i].setImage(null);
            this.hpmsgLabels[i].setText("");
            this.condLabels[i].setText("");
            this.condstLabels[i].setText("");
            this.bullstLabels[i].setText("");
            this.fuelstLabels[i].setText("");
            this.etc1Labels[i].setText("");
            this.etc2Labels[i].setText("");
        }
        // 艦隊合計Lv
        int totallv = 0;

        int akashiCapacity = dock.getAkashiCapacity();
        Date now = new Date();
        Date repairStartTime = GlobalContext.getAkashiRepairStart();
        CondTiming condTiming = GlobalContext.getCondTiming();

        for (int i = 0; i < ships.size(); i++) {
            ShipDto ship = ships.get(i);
            // 艦娘のステータス
            BitSet shipstatus = new BitSet();
            // HP
            int nowhp = ship.getNowhp();
            // MaxHP
            int maxhp = ship.getMaxhp();
            // HP割合
            float hpratio = (float) nowhp / (float) maxhp;
            // 経験値ゲージの割合
            float expraito = ship.getExpraito();
            // 疲労
            int cond = ship.getCond();
            // 弾
            int bull = ship.getBull();
            // 弾Max
            int bullmax = ship.getBullMax();
            // 残弾比
            float bullraito = bullmax != 0 ? (float) bull / (float) bullmax : 1f;
            // 燃料
            int fuel = ship.getFuel();
            // 燃料Max
            int fuelmax = ship.getFuelMax();
            // 残燃料比
            float fuelraito = fuelmax != 0 ? (float) fuel / (float) fuelmax : 1f;
            // 艦隊合計Lv
            totallv += ship.getLv();

            // 疲労している艦娘がいる場合メッセージを表示
            Date condClearDate = ship.getCondClearTime(condTiming, AppConfig.get().getOkCond());
            if (this.cond > cond) {
                this.cond = cond;
                this.clearDate = new TimeString(condClearDate).toString();
            }

            // 体力メッセージ
            boolean isEscaped = ((escaped != null) && escaped[i]);
            if (isEscaped) {
                this.hpmsgLabels[i].setText("退避");
                this.hpmsgLabels[i].setBackground(SWTResourceManager.getColor(AppConstants.ESCAPED_SHIP_COLOR));
                this.hpmsgLabels[i].setForeground(null);
            }
            else if (ship.isBadlyDamage()) {
                if (AppConfig.get().isFatalBybadlyDamage()) {
                    // 大破で致命的アイコン
                    this.state.set(FATAL);
                    shipstatus.set(FATAL);
                }
                // 大破している艦娘がいる場合メッセージを表示
                this.badlyDamage = true;
                if (ship.isSunk()) {
                    this.hpmsgLabels[i].setText("轟沈");
                }
                else {
                    this.hpmsgLabels[i].setText("大破");
                }
                this.hpmsgLabels[i].setBackground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
                this.hpmsgLabels[i].setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            } else if (ship.isHalfDamage()) {
                if (AppConfig.get().isWarnByHalfDamage()) {
                    // 中破で警告アイコン
                    this.state.set(WARN);
                    shipstatus.set(WARN);
                }

                this.hpmsgLabels[i].setText("中破");
                this.hpmsgLabels[i].setBackground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
                this.hpmsgLabels[i].setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            } else if (ship.isSlightDamage()) {
                this.hpmsgLabels[i].setText("小破");
                this.hpmsgLabels[i].setBackground(null);
                this.hpmsgLabels[i].setForeground(null);
            } else {
                this.hpmsgLabels[i].setText("健在");
                this.hpmsgLabels[i].setBackground(null);
                this.hpmsgLabels[i].setForeground(null);
            }

            // ステータス
            // ステータス.疲労
            this.condstLabels[i].setText("疲");
            if (cond >= 49) {
                this.condstLabels[i].setEnabled(false);
            } else {
                this.condstLabels[i].setEnabled(true);
            }
            // ステータス.燃料
            this.fuelstLabels[i].setText("燃");
            if (fuelraito >= 1f) {
                this.fuelstLabels[i].setEnabled(false);
                this.fuelstLabels[i].setForeground(null);
            } else {
                if (AppConfig.get().isWarnByNeedSupply()) {
                    // 補給不足で警告アイコン
                    this.state.set(WARN);
                    shipstatus.set(WARN);
                }
                this.fuelstLabels[i].setEnabled(true);
                if (fuelraito <= AppConstants.EMPTY_SUPPLY) {
                    // 補給赤
                    this.fuelstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
                } else if (fuelraito <= AppConstants.LOW_SUPPLY) {
                    // 補給橙
                    this.fuelstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
                }
            }
            // ステータス.弾
            this.bullstLabels[i].setText("弾");
            if (bullraito >= 1f) {
                this.bullstLabels[i].setEnabled(false);
                this.bullstLabels[i].setBackground(null);
                this.bullstLabels[i].setForeground(null);
            } else {
                if (AppConfig.get().isWarnByNeedSupply()) {
                    // 補給不足で警告アイコン
                    this.state.set(WARN);
                    shipstatus.set(WARN);
                }
                this.bullstLabels[i].setEnabled(true);
                if (bullraito <= AppConstants.EMPTY_SUPPLY) {
                    this.bullstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
                } else if (bullraito <= AppConstants.LOW_SUPPLY) {
                    this.bullstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
                }
            }

            // ステータス.ダメコン
            List<ItemInfoDto> item = ship.getItem();
            int dmgcsty = 0;
            int dmgcstm = 0;
            for (ItemInfoDto itemDto : item) {
                if (itemDto != null) {
                    if (itemDto.getName().equals("応急修理要員")) {
                        dmgcsty++;
                    } else if (itemDto.getName().equals("応急修理女神")) {
                        dmgcstm++;
                    }
                }
            }

            String dmgcstr = "";
            boolean isRepairing = (i < akashiCapacity) && !ship.isHalfDamage() && (nowhp != maxhp);
            if (isRepairing) {
                // 泊地修理中
                long needs = Math.max(AppConstants.AKASHI_REPAIR_MINIMUM, ship.getDocktime());
                Date finish = new Date(repairStartTime.getTime() + needs);
                long rest = TimeLogic.getRest(now, finish);
                dmgcstr = "あと" + TimeLogic.toDateRestString(rest) + "で完了";
            }
            else {
                // ダメコン
                if (dmgcsty > 0) {
                    dmgcstr += "要員x" + dmgcsty + " ";
                }
                if (dmgcstm > 0) {
                    dmgcstr += "女神x" + dmgcstm + " ";
                }
            }
            if (dmgcstr.length() > 0) {
                this.etc1Labels[i].setText(dmgcstr);
                this.etc1Labels[i].setEnabled(true);
                this.etc1Labels[i].setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN));
            }
            else {
                this.etc1Labels[i].setText("");
                this.etc1Labels[i].setEnabled(false);
                this.etc1Labels[i].setForeground(null);
            }

            // ステータス.あと何回
            String statusstr = "";
            if (AppConfig.get().isDisplayCount()) {
                Integer nextcount = this.getNextCount(ship, i == 0);
                if (nextcount != null) {
                    statusstr = MessageFormat.format("あと{0}回", nextcount);
                }
            }
            else {
                long rest = TimeLogic.getRest(now, condClearDate);
                statusstr = "あと" + TimeLogic.toDateRestString(rest) + "で回復";
            }
            this.etc2Labels[i].setText(statusstr);

            // コンディション
            if (cond <= AppConstants.COND_RED) {
                // 疲労19以下
                if (AppConfig.get().isWarnByCondState()) {
                    // 疲労状態で警告アイコン
                    this.state.set(WARN);
                    shipstatus.set(WARN);
                }
                this.condLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
                this.condstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
            } else if (cond <= AppConstants.COND_ORANGE) {
                // 疲労29以下
                if (AppConfig.get().isWarnByCondState()) {
                    // 疲労状態で警告アイコン
                    this.state.set(WARN);
                    shipstatus.set(WARN);
                }
                this.condLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
                this.condstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
            } else if ((cond >= AppConstants.COND_DARK_GREEN) && (cond < AppConstants.COND_GREEN)) {
                // 疲労50以上
                this.condLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_DARK_GREEN_COLOR));
                this.condstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_DARK_GREEN_COLOR));
            } else if (cond >= AppConstants.COND_GREEN) {
                // 疲労53以上
                this.condLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_GREEN_COLOR));
                this.condstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_GREEN_COLOR));
            } else {
                this.condLabels[i].setForeground(null);
                this.condstLabels[i].setForeground(null);
            }

            // 艦娘の状態アイコンを更新
            if (shipstatus.get(FATAL)) {
                this.iconLabels[i].setImage(SWTResourceManager.getImage(FleetComposite.class,
                        AppConfig.get().isMonoIcon()
                                ? AppConstants.R_ICON_EXCLAMATION_MONO
                                : AppConstants.R_ICON_EXCLAMATION));
            } else if (shipstatus.get(WARN)) {
                this.iconLabels[i].setImage(SWTResourceManager.getImage(FleetComposite.class,
                        AppConfig.get().isMonoIcon()
                                ? AppConstants.R_ICON_ERROR_MONO
                                : AppConstants.R_ICON_ERROR));
            } else {
                this.iconLabels[i].setImage(null);
            }

            // ラベルを更新する
            // 名前
            this.nameLabels[i].setText(ship.getName());
            this.nameLabels[i].setToolTipText(MessageFormat.format(AppConstants.TOOLTIP_FLEETTAB_SHIP, nowhp, maxhp,
                    fuel, fuelmax, bull, bullmax, ship.getNext()));
            this.lvLabels[i].setText(MessageFormat.format("(Lv.{0})", ship.getLv()));
            // HP
            this.hpLabels[i].setText(MessageFormat.format("{0}/{1} ", nowhp, maxhp));
            // HPゲージ
            Image gauge = this.getHpGaugeImage(hpratio, expraito);
            this.hpgaugeLabels[i].setImage(gauge);
            if (this.hpgaugeImages[i] != null) {
                // 古いイメージを破棄
                this.hpgaugeImages[i].dispose();
            }
            this.hpgaugeImages[i] = gauge;
            // コンディション
            this.condLabels[i].setText(MessageFormat.format("{0} cond.", cond));
            this.bullstLabels[i].getParent().layout();

        }
        // メッセージを更新する
        // 入渠中の艦娘を探す
        boolean isBathwater = false;
        for (ShipDto shipDto : ships) {
            if (GlobalContext.isNdock(shipDto)) {
                isBathwater = true;
                break;
            }
        }
        boolean isAkashi = dock.isAkashiRepairing();

        // 制空値を計算
        int seiku = 0;
        for (ShipDto shipDto : ships) {
            seiku += shipDto.getSeiku();
        }
        // ドラム缶、大発の合計
        int dram = 0;
        int dramKanmusu = 0;
        int daihatsu = 0;
        for (ShipDto shipDto : ships) {
            if (shipDto.getDram() > 0) {
                dramKanmusu++;
                dram += shipDto.getDram();
            }
            daihatsu += shipDto.getDaihatsu();
        }
        //大発による遠征効率UPの上限
        int daihatsuUp = daihatsu * 5;
        if (daihatsuUp > 20) {
            daihatsuUp = 20;
        }

        StyleRange messageStyle = new StyleRange();
        messageStyle.fontStyle = SWT.BOLD;
        messageStyle.foreground = SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE);
        StyleRange taihaStyle = new StyleRange();
        taihaStyle.fontStyle = SWT.BOLD;
        taihaStyle.underline = true;
        taihaStyle.underlineStyle = SWT.UNDERLINE_SQUIGGLE;
        taihaStyle.underlineColor = SWTResourceManager.getColor(SWT.COLOR_RED);
        taihaStyle.foreground = SWTResourceManager.getColor(SWT.COLOR_RED);

        if (GlobalContext.isMission(this.dock.getId())) {
            // 遠征中
            this.addStyledText(this.message, AppConstants.MESSAGE_MISSION, messageStyle);
        }
        else if (GlobalContext.isSortie(this.dock.getId())) {
            // 出撃中
            this.addStyledText(this.message, AppConstants.MESSAGE_SORTIE, messageStyle);
            if (this.badlyDamage) {
                // 大破
                this.addStyledText(this.message, AppConstants.MESSAGE_STOP_SORTIE, taihaStyle);
            }
            else if (combinedFleetBadlyDamaed) {
                // 連合艦隊の他の艦隊に大破艦がある
                this.addStyledText(this.message, AppConstants.MESSAGE_IN_COMBINED + AppConstants.MESSAGE_STOP_SORTIE,
                        taihaStyle);
            }
            else {
                // 進撃可能
                this.addStyledText(this.message, AppConstants.MESSAGE_GO_NEXT, messageStyle);
            }
        }
        else if (this.badlyDamage) {
            // 大破
            this.addStyledText(this.message,
                    MessageFormat.format(AppConstants.MESSAGE_BAD, AppConstants.MESSAGE_BADLY_DAMAGE), taihaStyle);
        }
        else if (combinedFleetBadlyDamaed) {
            // 連合艦隊の他の艦隊に大破艦がある
            this.addStyledText(this.message, AppConstants.MESSAGE_IN_COMBINED +
                    MessageFormat.format(AppConstants.MESSAGE_BAD, AppConstants.MESSAGE_BADLY_DAMAGE), taihaStyle);
        }
        else if (isBathwater) {
            // 入渠中
            this.addStyledText(this.message,
                    MessageFormat.format(AppConstants.MESSAGE_BAD, AppConstants.MESSAGE_BATHWATER), messageStyle);
        }
        else if (isAkashi) {
            // 泊地修理中
            this.addStyledText(this.message, "泊地修理中。" + AppConstants.MESSAGE_GOOD, messageStyle);
        }
        else {
            // 出撃可能
            this.addStyledText(this.message, AppConstants.MESSAGE_GOOD, messageStyle);
        }
        if ((Integer.parseInt(this.dock.getId()) <= 2) && GlobalContext.isCombined()) {
            // 連合艦隊
            this.addStyledText(this.message, AppConstants.MESSAGE_COMBINED, messageStyle);
        }
        if (this.clearDate != null) {
            this.addStyledText(this.message, MessageFormat.format(AppConstants.MESSAGE_COND, this.clearDate), null);
        }
        // 索敵 計算
        SakutekiString fleetStatus = new SakutekiString(ships, GlobalContext.hqLevel());
        // 制空
        this.addStyledText(this.message, MessageFormat.format(AppConstants.MESSAGE_SEIKU, seiku), null);
        // 索敵
        this.addStyledText(this.message,
                MessageFormat.format(AppConstants.MESSAGE_SAKUTEKI, fleetStatus.toString()), null);
        // 合計Lv
        this.addStyledText(this.message, MessageFormat.format(AppConstants.MESSAGE_TOTAL_LV, totallv), null);

        if (dram > 0) {
            // ドラム缶合計数
            this.addStyledText(this.message, MessageFormat.format(AppConstants.MESSAGE_TOTAL_DRAM, dram, dramKanmusu),
                    null);
        }
        if (daihatsu > 0) {
            // 大発合計数
            this.addStyledText(this.message,
                    MessageFormat.format(AppConstants.MESSAGE_TOTAL_DAIHATSU, daihatsu, daihatsuUp), null);
        }

        this.updateTabIcon();
        this.postFatal();

        this.fleetGroup.layout();

        this.getShell().setRedraw(true);
    }

    /**
     * 艦隊タブのアイコンを更新します
     */
    private void updateTabIcon() {
        if (this.state.get(FATAL)) {
            this.tab.setImage(SWTResourceManager.getImage(FleetComposite.class,
                    AppConfig.get().isMonoIcon()
                            ? AppConstants.R_ICON_EXCLAMATION_MONO
                            : AppConstants.R_ICON_EXCLAMATION));
        } else if (this.state.get(WARN)) {
            this.tab.setImage(SWTResourceManager.getImage(FleetComposite.class,
                    AppConfig.get().isMonoIcon()
                            ? AppConstants.R_ICON_ERROR_MONO
                            : AppConstants.R_ICON_ERROR));
        } else {
            this.tab.setImage(null);
        }
    }

    /**
     * 艦隊が出撃中で大破した場合に警告を行います
     */
    private void postFatal() {
        if (this.badlyDamage && GlobalContext.isSortie(this.dock.getId())) {
            if (AppConfig.get().isBalloonBybadlyDamage()) {
                List<ShipDto> ships = this.dock.getShips();
                StringBuilder sb = new StringBuilder();
                sb.append(AppConstants.MESSAGE_STOP_SORTIE);
                sb.append("\n");
                for (ShipDto shipDto : ships) {
                    if (shipDto.isBadlyDamage()) {
                        sb.append(shipDto.getName());
                        sb.append("(" + shipDto.getLv() + ")");
                        sb.append(" : ");
                        List<ItemInfoDto> items = shipDto.getItem();
                        List<String> names = new ArrayList<String>();
                        for (ItemInfoDto itemDto : items) {
                            if (itemDto != null) {
                                names.add(itemDto.getName());
                            }
                        }
                        sb.append(StringUtils.join(names, ","));
                        sb.append("\n");
                    }
                }
                ToolTip tip = new ToolTip(this.getShell(), SWT.BALLOON
                        | SWT.ICON_ERROR);
                tip.setText("大破警告");
                tip.setMessage(sb.toString());

                this.main.getTrayItem().setToolTip(tip);
                tip.setVisible(true);
            }
            // 大破時にサウンドを再生する
            Sound.randomBadlySoundPlay();
        }
    }

    /**
     * HPゲージのイメージを取得します
     * @param hpratio HP割合
     * @return HPゲージのイメージ
     */
    private Image getHpGaugeImage(float hpratio, float expraito) {
        Image image = new Image(Display.getDefault(), GAUGE_WIDTH, GAUGE_HEIGHT);
        GC gc = new GC(image);
        gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        gc.fillRectangle(0, 0, GAUGE_WIDTH, GAUGE_HEIGHT);
        gc.setBackground(SWTResourceManager.getColor(gradation(hpratio, GAUGE_EMPTY, GAUGE_HALF, GAUGE_FULL)));
        gc.fillRectangle(0, 0, (int) (GAUGE_WIDTH * hpratio), GAUGE_HEIGHT);
        gc.setBackground(SWTResourceManager.getColor(EXP_GAUGE));
        gc.fillRectangle(0, GAUGE_HEIGHT - EXP_GAUGE_HEIGHT, (int) (GAUGE_WIDTH * expraito), EXP_GAUGE_HEIGHT);
        gc.drawImage(image, 0, 0);
        gc.dispose();
        return image;
    }

    /**
     * スタイル付きテキストを設定します
     *
     * @param text StyledText
     * @param str 文字
     * @param style スタイル
     */
    private void addStyledText(StyledText text, String str, StyleRange style) {
        StyleRange[] oldranges = text.getStyleRanges();
        String beforeText = text.getText();
        StyleRange addStyle = style;
        if (addStyle == null) {
            addStyle = new StyleRange();
        }
        addStyle.start = beforeText.length();
        addStyle.length = str.length();

        StyleRange[] ranges = new StyleRange[oldranges.length + 1];
        for (int i = 0; i < oldranges.length; i++) {
            ranges[i] = oldranges[i];
        }
        ranges[oldranges.length] = addStyle;

        text.setText(beforeText + str);
        text.setStyleRanges(ranges);
    }

    /**
     * あと何回戦闘すればよいかを取得します
     *
     * @param ship 艦娘
     * @param isFlagship 旗艦
     * @return 回数
     */
    @CheckForNull
    private Integer getNextCount(ShipDto ship, boolean isFlagship) {
        // 次のレベルに必要な経験値
        Integer nextexp = CalcExpUtils.getNextLvExp(ship.getLv());
        if (nextexp != null) {
            // 必要経験値
            int needexp = nextexp - ship.getExp();
            // 海域Exp
            Integer baseexp = SeaExp.get().get(AppConfig.get().getDefaultSea());
            // 評価倍率
            Double eval = EvaluateExp.get().get(AppConfig.get().getDefaultEvaluate());
            if ((baseexp != null) && (eval != null)) {
                // 得られる経験値
                int getexpd = CalcExpUtils.getExp(baseexp, eval, isFlagship, false);
                // 戦闘回数
                int count = CalcExpUtils.getCount(needexp, getexpd);
                return Integer.valueOf(count);
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Image image : this.hpgaugeImages) {
            if (image != null) {
                image.dispose();
            }
        }
    }

    /**
     * 複数の色の中間色を取得する
     * 
     * @param raito 割合
     * @param rgbs 色たち
     * @return 色
     */
    private static RGB gradation(float raito, RGB... rgbs) {
        if (raito <= 0.0f) {
            return rgbs[0];
        }
        if (raito >= 1.0f) {
            return rgbs[rgbs.length - 1];
        }
        int length = rgbs.length - 1;

        // 開始色
        int start = (int) (length * raito);
        // 終了色
        int end = start + 1;
        // 開始色と終了色の割合を算出
        float startPer = (float) start / length;
        float endPer = (float) end / length;
        float subPer = (raito - startPer) / (endPer - startPer);
        return gradation(subPer, rgbs[start], rgbs[end]);
    }

    /**
     * 2つの色の中間色を取得する
     * 
     * @param raito 割合
     * @param start 開始色
     * @param end 終了色
     * @return 色
     */
    private static RGB gradation(float raito, RGB start, RGB end) {
        int r = (int) (start.red + ((end.red - start.red) * raito));
        int g = (int) (start.green + ((end.green - start.green) * raito));
        int b = (int) (start.blue + ((end.blue - start.blue) * raito));
        return new RGB(r, g, b);
    }
}
