/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.background;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import logbook.config.GlobalConfig;
import logbook.data.context.GlobalContext;
import logbook.dto.DeckMissionDto;
import logbook.dto.DockDto;
import logbook.dto.NdockDto;
import logbook.dto.ShipDto;
import logbook.gui.ApplicationMain;
import logbook.gui.logic.Sound;
import logbook.gui.logic.TimeLogic;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 非同期にメイン画面を更新します
 */
public final class AsyncExecApplicationMain extends Thread {
    private static final Logger LOG = LogManager.getLogger(AsyncExecApplicationMain.class);

    private static final int ONE_SECONDS_FORMILIS = 1000;
    private static final int ONE_MINUTES = 60;

    private final ApplicationMain main;

    /**
     * 非同期にメイン画面を更新するスレッドのコンストラクター
     * 
     * @param main メイン画面
     */
    public AsyncExecApplicationMain(ApplicationMain main) {
        this.main = main;
        this.setName("logbook_async_exec_application_main");
    }

    /**
     * 現在のメイン画面を更新します
     */
    @Override
    public void run() {

        try {
            while (true) {
                GlobalContext.updateContext();
                // 保有アイテム数を更新する
                Display.getDefault().asyncExec(new UpdateItemCountTask(this.main));
                // 保有艦娘数を更新する
                Display.getDefault().asyncExec(new UpdateShipCountTask(this.main));
                // 遠征と入渠を更新する
                Display.getDefault().asyncExec(new UpdateDeckNdockTask(this.main));
                // 艦隊タブを更新する
                Display.getDefault().asyncExec(new UpdateFleetTabTask(this.main));

                Thread.sleep(ONE_SECONDS_FORMILIS);
            }
        } catch (Exception e) {
            LOG.fatal("スレッドが異常終了しました", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 2つの日付から残り時間を計算する
     * 
     * @param date1
     * @param date2
     * @return
     */
    private static long getRest(Date date1, Date date2) {
        return ((date2.getTime() - date1.getTime()) / ONE_SECONDS_FORMILIS);
    }

    /**
     * 保有アイテム数を更新する
     */
    private static final class UpdateItemCountTask implements Runnable {

        private final ApplicationMain main;

        /**
         * コンストラクター
         */
        public UpdateItemCountTask(ApplicationMain main) {
            this.main = main;
        }

        @Override
        public void run() {
            Button itemList = this.main.getItemList();
            String setText = "所有装備(" + GlobalContext.getItemMap().size() + "/"
                    + GlobalContext.maxSlotitem() + ")";
            if (!setText.equals(itemList.getText())) {
                itemList.setText(setText);
                itemList.getParent().layout();
            }
        }
    }

    /**
     * 保有艦娘数を更新する
     */
    private static final class UpdateShipCountTask implements Runnable {

        private final ApplicationMain main;

        /**
         * コンストラクター
         */
        public UpdateShipCountTask(ApplicationMain main) {
            this.main = main;
        }

        @Override
        public void run() {
            Button shipList = this.main.getShipList();
            String setText = "所有艦娘(" + GlobalContext.getShipMap().size() + "/" + GlobalContext.maxChara()
                    + ")";
            if (!setText.equals(shipList.getText())) {
                shipList.setText(setText);
                shipList.getParent().layout();
            }
        }
    }

    /**
     * 遠征と入渠を更新する
     */
    private static final class UpdateDeckNdockTask implements Runnable {

        private static final Logger LOG = LogManager.getLogger(UpdateDeckNdockTask.class);

        private static final boolean[] FLAG_NOTICE_DECK = { false, false, false };
        private static final boolean[] FLAG_NOTICE_NDOCK = { false, false, false, false };

        private final ApplicationMain main;

        /**
         * コンストラクター
         */
        public UpdateDeckNdockTask(ApplicationMain main) {
            this.main = main;
        }

        @Override
        public void run() {
            // 現在時刻
            Date now = Calendar.getInstance().getTime();
            List<String> notice = new ArrayList<String>();
            // 遠征を更新する
            if (this.updateDeck(now, notice)) {
                Sound.randomExpeditionSoundPlay();
            }
            // 入渠を更新する
            if (this.updateNdock(now, notice)) {
                Sound.randomDockSoundPlay();
            }
            try {
                // 遠征・入渠のお知らせ
                if (notice.size() > 0) {
                    ToolTip tip = new ToolTip(this.main.getShell(), SWT.BALLOON
                            | SWT.ICON_INFORMATION);
                    tip.setText("遠征・入渠");
                    tip.setMessage(StringUtils.join(notice, "\r\n"));
                    this.main.getTrayItem().setToolTip(tip);
                    this.main.getTabFolder().setSelection(0);
                    tip.setVisible(true);
                }
            } catch (Exception e) {
                LOG.warn("お知らせの表示に失敗しました", e);
            }
        }

        /**
         * 遠征を更新する
         * 
         * @param now
         * @param notice
         * @return
         */
        private boolean updateDeck(Date now, List<String> notice) {
            boolean noticeflg = false;

            Label[] deckNameLabels = { this.main.getDeck1name(), this.main.getDeck2name(), this.main.getDeck3name() };
            Text[] deckTimeTexts = { this.main.getDeck1time(), this.main.getDeck2time(), this.main.getDeck3time() };

            DeckMissionDto[] deckMissions = GlobalContext.getDeckMissions();

            for (int i = 0; i < deckMissions.length; i++) {
                String time = "";
                String dispname = "";
                if (deckMissions[i].getMission() != null) {
                    dispname = deckMissions[i].getName() + " (" + deckMissions[i].getMission() + ")";

                    if (deckMissions[i].getTime() != null) {
                        long rest = getRest(now, deckMissions[i].getTime());
                        // 20分前、10分前、5分前になったら背景色を変更する
                        if (rest <= (ONE_MINUTES * 5)) {
                            deckTimeTexts[i].setBackground(SWTResourceManager
                                    .getColor(GlobalConfig.TIME_IN_5_MIN));
                        } else if (rest <= (ONE_MINUTES * 10)) {
                            deckTimeTexts[i].setBackground(SWTResourceManager
                                    .getColor(GlobalConfig.TIME_IN_10_MIN));
                        } else if (rest <= (ONE_MINUTES * 20)) {
                            deckTimeTexts[i].setBackground(SWTResourceManager
                                    .getColor(GlobalConfig.TIME_IN_20_MIN));
                        } else {
                            deckTimeTexts[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
                        }
                        if (this.main.getDeckNotice().getSelection()) {
                            if (((rest <= ONE_MINUTES) && !FLAG_NOTICE_DECK[i])) {
                                notice.add(dispname + " がまもなく帰投します");
                                noticeflg = true;
                                FLAG_NOTICE_DECK[i] = true;
                            } else if (rest > ONE_MINUTES) {
                                FLAG_NOTICE_DECK[i] = false;
                            }
                        } else {
                            FLAG_NOTICE_DECK[i] = false;
                        }
                        time = TimeLogic.toDateRestString(rest);
                        if (time == null) {
                            time = "まもなく帰投します";
                        }
                    }
                } else {
                    deckTimeTexts[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
                }
                deckNameLabels[i].setText(dispname);
                deckTimeTexts[i].setText(time);
            }
            return noticeflg;
        }

        /**
         * 入渠を更新する
         * 
         * @param now
         * @param notice
         * @return
         */
        private boolean updateNdock(Date now, List<String> notice) {
            boolean noticeflg = false;

            Map<Long, ShipDto> shipMap = GlobalContext.getShipMap();

            Label[] ndockNameLabels = { this.main.getNdock1name(), this.main.getNdock2name(),
                    this.main.getNdock3name(), this.main.getNdock4name() };
            Text[] ndockTimeTexts = { this.main.getNdock1time(), this.main.getNdock2time(), this.main.getNdock3time(),
                    this.main.getNdock4time() };

            NdockDto[] ndocks = GlobalContext.getNdocks();

            for (int i = 0; i < ndocks.length; i++) {
                String name = "";
                String time = "";

                if (ndocks[i].getNdockid() != 0) {
                    ShipDto ship = shipMap.get(Long.valueOf(ndocks[i].getNdockid()));
                    if (ship != null) {
                        name = ship.getName() + " (Lv" + ship.getLv() + ")";
                        long rest = getRest(now, ndocks[i].getNdocktime());
                        // 20分前、10分前、5分前になったら背景色を変更する
                        if (rest <= (ONE_MINUTES * 5)) {
                            ndockTimeTexts[i].setBackground(SWTResourceManager
                                    .getColor(GlobalConfig.TIME_IN_5_MIN));
                        } else if (rest <= (ONE_MINUTES * 10)) {
                            ndockTimeTexts[i].setBackground(SWTResourceManager
                                    .getColor(GlobalConfig.TIME_IN_10_MIN));
                        } else if (rest <= (ONE_MINUTES * 20)) {
                            ndockTimeTexts[i].setBackground(SWTResourceManager
                                    .getColor(GlobalConfig.TIME_IN_20_MIN));
                        } else {
                            ndockTimeTexts[i].setBackground(SWTResourceManager
                                    .getColor(SWT.COLOR_WHITE));
                        }
                        if (this.main.getNdockNotice().getSelection()) {

                            if ((rest <= ONE_MINUTES) && !FLAG_NOTICE_NDOCK[i]) {
                                notice.add(name + " がまもなくお風呂からあがります");
                                noticeflg = true;
                                FLAG_NOTICE_NDOCK[i] = true;
                            } else if (rest > ONE_MINUTES) {
                                FLAG_NOTICE_NDOCK[i] = false;
                            }
                        } else {
                            FLAG_NOTICE_NDOCK[i] = false;
                        }
                        time = TimeLogic.toDateRestString(rest);
                        if (time == null) {
                            time = "まもなくお風呂からあがります";
                        }
                    }
                } else {
                    ndockTimeTexts[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
                }
                ndockNameLabels[i].setText(name);
                ndockTimeTexts[i].setText(time);
            }
            return noticeflg;
        }
    }

    /**
     * 艦隊タブを更新する
     */
    private static final class UpdateFleetTabTask implements Runnable {

        private static String[] dockname = new String[4];
        private static CTabItem[] tabItems = new CTabItem[4];
        private static FleetComposite[] dockComposites = new FleetComposite[4];

        private final ApplicationMain main;

        /**
         * コンストラクター
         */
        public UpdateFleetTabTask(ApplicationMain main) {
            this.main = main;
        }

        @Override
        public void run() {
            // タブを更新する
            for (int i = 0; i < 4; i++) {
                DockDto dock = GlobalContext.getDock(Integer.toString(i + 1));
                if (dock != null) {
                    FleetComposite tabComposite = dockComposites[i];
                    CTabItem tabItem = tabItems[i];

                    if (tabItem == null) {

                        tabItem = new CTabItem(this.main.getTabFolder(), SWT.NONE);
                        tabItem.setText(dock.getName());

                        // メインコンポジット
                        tabComposite = new FleetComposite(this.main.getTabFolder());
                        tabItem.setControl(tabComposite);

                        tabItems[i] = tabItem;
                        dockComposites[i] = tabComposite;
                    }
                    if (!dock.getName().equals(dockname[i])) {
                        dockname[i] = dock.getName();
                    }
                    tabComposite.updateFleet(dock);
                    tabItem.setText(dock.getName());
                }
            }
        }
    }

    /**
     * 艦隊タブのウィジェットです
     */
    public static final class FleetComposite extends Composite {

        private static final int MAXCHARA = 6;

        private final Font large1;
        private final Font large2;

        private DockDto dock;

        private final Composite fleetGroup;

        /** 名前ラベル */
        private final Label[] nameLabels = new Label[MAXCHARA];
        /** 今のHP */
        private final Label[] nowhpLabels = new Label[MAXCHARA];
        /** 最大HP */
        private final Label[] maxhpLabels = new Label[MAXCHARA];
        /** HPステ */
        private final Label[] hpmsgLabels = new Label[MAXCHARA];
        /** コンディション */
        private final Label[] condLabels = new Label[MAXCHARA];
        /** コンディションステータス */
        private final Label[] condstLabels = new Label[MAXCHARA];
        /** 弾ステータス */
        private final Label[] bullstLabels = new Label[MAXCHARA];
        /** 燃料ステータス */
        private final Label[] fuelstLabels = new Label[MAXCHARA];

        /**
         * @param parent
         */
        public FleetComposite(CTabFolder parent) {
            super(parent, SWT.NONE);
            this.setLayout(new GridLayout(1, false));
            this.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            FontData normalfd = parent.getShell().getFont().getFontData()[0];
            FontData largefd1 = new FontData(normalfd.getName(), normalfd.getHeight() + 2, normalfd.getStyle());
            FontData largefd2 = new FontData(normalfd.getName(), normalfd.getHeight() + 2, normalfd.getStyle());

            this.large1 = new Font(Display.getCurrent(), largefd1);
            this.large2 = new Font(Display.getCurrent(), largefd2);

            this.fleetGroup = new Composite(this, SWT.NONE);
            this.fleetGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            GridLayout glShipGroup = new GridLayout(2, false);
            glShipGroup.horizontalSpacing = 1;
            glShipGroup.marginTop = 0;
            glShipGroup.marginHeight = 0;
            glShipGroup.marginBottom = 0;
            glShipGroup.verticalSpacing = 1;
            this.fleetGroup.setLayout(glShipGroup);

            for (int i = 0; i < MAXCHARA; i++) {

                // 名前
                Label namelabel = new Label(this.fleetGroup, SWT.NONE);
                namelabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                namelabel.setFont(this.large1);
                namelabel.setText("名前");

                // HP
                Composite hpComposite = new Composite(this.fleetGroup, SWT.NONE);
                GridLayout glHp = new GridLayout(3, false);
                glHp.horizontalSpacing = 1;
                glHp.marginTop = 0;
                glHp.marginHeight = 0;
                glHp.marginBottom = 0;
                glHp.verticalSpacing = 1;
                hpComposite.setLayout(glHp);
                hpComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

                Label nowhp = new Label(hpComposite, SWT.NONE);
                nowhp.setFont(this.large2);
                nowhp.setText("99");
                Label maxhp = new Label(hpComposite, SWT.NONE);
                maxhp.setText("/99");
                Label hpmsg = new Label(hpComposite, SWT.NONE);
                hpmsg.setText("(健在)");

                // ステータス
                Composite stateComposite = new Composite(this.fleetGroup, SWT.NONE);
                GridLayout glState = new GridLayout(3, false);
                glState.horizontalSpacing = 1;
                glState.marginTop = 0;
                glState.marginHeight = 0;
                glState.marginBottom = 0;
                glState.verticalSpacing = 1;
                stateComposite.setLayout(glState);
                stateComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

                Label condst = new Label(stateComposite, SWT.NONE);
                condst.setText("疲");
                Label fuelst = new Label(stateComposite, SWT.NONE);
                fuelst.setText("燃");
                Label bullst = new Label(stateComposite, SWT.NONE);
                bullst.setText("弾");

                // 疲労
                Label cond = new Label(this.fleetGroup, SWT.NONE);
                cond.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
                cond.setText("49 cond.");

                this.nameLabels[i] = namelabel;
                this.nowhpLabels[i] = nowhp;
                this.maxhpLabels[i] = maxhp;
                this.hpmsgLabels[i] = hpmsg;
                this.condLabels[i] = cond;
                this.condstLabels[i] = condst;
                this.bullstLabels[i] = bullst;
                this.fuelstLabels[i] = fuelst;
            }

            this.fleetGroup.layout(true);
        }

        /**
         * 艦隊を更新します
         * 
         * @param dock
         */
        public void updateFleet(DockDto dock) {
            if (this.dock == dock) {
                return;
            }
            List<ShipDto> ships = dock.getShips();
            for (int i = ships.size(); i < MAXCHARA; i++) {
                this.nameLabels[i].setText("");
                this.nowhpLabels[i].setText("");
                this.maxhpLabels[i].setText("");
                this.hpmsgLabels[i].setText("");
                this.condLabels[i].setText("");
                this.condstLabels[i].setText("");
                this.bullstLabels[i].setText("");
                this.fuelstLabels[i].setText("");
            }
            for (int i = 0; i < ships.size(); i++) {
                ShipDto ship = ships.get(i);

                String name = ship.getName();
                // HP
                long nowhp = ship.getNowhp();
                // MaxHP
                long maxhp = ship.getMaxhp();
                // HP比率
                float hpratio = (float) nowhp / (float) maxhp;
                // 疲労
                long cond = ship.getCond();
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

                // 体力メッセージ
                if (hpratio <= GlobalConfig.BADLY_DAMAGE) {
                    this.hpmsgLabels[i].setText("(大破)");
                    this.hpmsgLabels[i].setBackground(SWTResourceManager.getColor(GlobalConfig.COND_RED_COLOR));
                    this.hpmsgLabels[i].setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
                } else if (hpratio <= GlobalConfig.HALF_DAMAGE) {
                    this.hpmsgLabels[i].setText("(中破)");
                    this.hpmsgLabels[i].setBackground(SWTResourceManager.getColor(GlobalConfig.COND_ORANGE_COLOR));
                    this.hpmsgLabels[i].setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
                } else if (hpratio <= GlobalConfig.SLIGHT_DAMAGE) {
                    this.hpmsgLabels[i].setText("(小破)");
                    this.hpmsgLabels[i].setBackground(null);
                    this.hpmsgLabels[i].setForeground(null);
                } else {
                    this.hpmsgLabels[i].setText("(健在)");
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
                    this.fuelstLabels[i].setEnabled(true);
                    if (fuelraito <= GlobalConfig.EMPTY_SUPPLY) {
                        this.fuelstLabels[i].setForeground(SWTResourceManager.getColor(GlobalConfig.COND_RED_COLOR));
                    } else if (fuelraito <= GlobalConfig.LOW_SUPPLY) {
                        this.fuelstLabels[i].setForeground(SWTResourceManager.getColor(GlobalConfig.COND_ORANGE_COLOR));
                    }
                }
                // ステータス.弾
                this.bullstLabels[i].setText("弾");
                if (bullraito >= 1f) {
                    this.bullstLabels[i].setEnabled(false);
                    this.bullstLabels[i].setBackground(null);
                    this.bullstLabels[i].setForeground(null);
                } else {
                    this.bullstLabels[i].setEnabled(true);
                    if (bullraito <= GlobalConfig.EMPTY_SUPPLY) {
                        this.bullstLabels[i].setForeground(SWTResourceManager.getColor(GlobalConfig.COND_RED_COLOR));
                    } else if (bullraito <= GlobalConfig.LOW_SUPPLY) {
                        this.bullstLabels[i].setForeground(SWTResourceManager.getColor(GlobalConfig.COND_ORANGE_COLOR));
                    }
                }
                // コンディション
                if (cond <= GlobalConfig.COND_RED) {
                    this.condLabels[i].setForeground(SWTResourceManager.getColor(GlobalConfig.COND_RED_COLOR));
                    this.condstLabels[i].setForeground(SWTResourceManager.getColor(GlobalConfig.COND_RED_COLOR));
                } else if (cond <= GlobalConfig.COND_ORANGE) {
                    this.condLabels[i].setForeground(SWTResourceManager.getColor(GlobalConfig.COND_ORANGE_COLOR));
                    this.condstLabels[i].setForeground(SWTResourceManager.getColor(GlobalConfig.COND_ORANGE_COLOR));
                } else {
                    this.condLabels[i].setForeground(null);
                    this.condstLabels[i].setForeground(null);
                }

                this.nameLabels[i].setText(name);
                this.nowhpLabels[i].setText(Long.toString(nowhp));
                this.maxhpLabels[i].setText("/" + maxhp);
                this.condLabels[i].setText(ship.getCond() + " cond.");
                this.bullstLabels[i].getParent().layout(true);
            }
            this.dock = dock;
            this.fleetGroup.layout(true);
        }

        @Override
        public void dispose() {
            super.dispose();
            this.large1.dispose();
            this.large2.dispose();
        }
    }
}
