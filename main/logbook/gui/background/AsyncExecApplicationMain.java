package logbook.gui.background;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import logbook.config.AppConfig;
import logbook.config.ShipGroupConfig;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.BasicInfoDto;
import logbook.dto.DeckMissionDto;
import logbook.dto.DockDto;
import logbook.dto.NdockDto;
import logbook.dto.ShipDto;
import logbook.gui.ApplicationMain;
import logbook.gui.logic.LayoutLogic;
import logbook.gui.logic.PushNotify;
import logbook.gui.logic.Sound;
import logbook.gui.logic.TimeLogic;
import logbook.gui.widgets.FleetComposite;
import logbook.internal.CondTiming;
import logbook.internal.EnemyData;
import logbook.internal.MasterData;
import logbook.util.SwtUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TaskItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 非同期にメイン画面を更新します
 */
public final class AsyncExecApplicationMain extends Thread {
    private static final Logger LOG = LogManager.getLogger(AsyncExecApplicationMain.class);

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
            long nextUpdateTime = 0;
            final ApplicationMain main = this.main;

            while (true) {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        // 保有アイテム数を更新する
                        new UpdateItemCountTask(main).run();
                        // 保有艦娘数を更新する
                        new UpdateShipCountTask(main).run();
                        // 艦隊タブを更新する
                        new UpdateFleetTabTask(main).run();
                        // 遠征と入渠を更新する
                        new UpdateDeckNdockTask(main).run();

                        try {
                            // 更新日時が実装されているファイルたちはすぐに保存
                            ShipGroupConfig.store();
                            MasterData.store();
                            EnemyData.store();
                        } catch (IOException e) {
                            LOG.fatal("ファイル更新に失敗しました", e);
                        }
                    }
                });

                long currentTime = Calendar.getInstance().getTimeInMillis();
                // 次のアップデートは1秒後
                nextUpdateTime += TimeUnit.SECONDS.toMillis(1);
                if (nextUpdateTime < currentTime)
                    nextUpdateTime = currentTime + TimeUnit.SECONDS.toMillis(1);

                Thread.sleep(nextUpdateTime - currentTime);
            }
        } catch (Exception e) {
            LOG.fatal("スレッドが異常終了しました", e);
            throw new RuntimeException(e);
        }
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
            if (this.main.getShell().isDisposed()) {
                return;
            }
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
            if (this.main.getShell().isDisposed()) {
                return;
            }
            Button shipList = this.main.getShipList();
            String setText = "所有艦娘(" + GlobalContext.getShipMap().size() + "/" + GlobalContext.maxChara()
                    + ")";
            if (setText.equals(shipList.getText())) {
                return;
            }

            shipList.setText(setText);
            shipList.getParent().layout();

            if (AppConfig.get().isUseTaskbarNotify()) {
                TaskItem item = SwtUtils.getTaskBarItem(this.main.getShell());
                if (item != null) {
                    int max = GlobalContext.maxChara();
                    int size = GlobalContext.getShipMap().size();
                    int locked = 0;
                    for (Entry<Integer, ShipDto> entry : GlobalContext.getShipMap().entrySet()) {
                        if (entry.getValue().getLocked()) {
                            locked++;
                        }
                    }
                    int r = Math.round(((float) (size - locked) / (float) (max - locked)) * 100);

                    item.setProgress(r);

                    if (max <= (size + AppConfig.get().getNotifyFully())) {
                        item.setProgressState(SWT.PAUSED);
                    } else {
                        item.setProgressState(SWT.NORMAL);
                    }
                }
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
        private static final boolean[] FLAG_NOTICE_COND = { false, false, false, false };
        private static final Map<Integer, Boolean> FLAG_NOTICE_AKASHI = new TreeMap<>();

        private final ApplicationMain main;

        /** 日付フォーマット */
        private final SimpleDateFormat format = new SimpleDateFormat(AppConstants.DATE_SHORT_FORMAT);

        private final List<String> noticeMission = new ArrayList<String>();
        private final List<String> noticeNdock = new ArrayList<String>();
        private final List<String> noticeCond = new ArrayList<String>();
        private final List<String> noticeAkashi = new ArrayList<String>();
        private final Date now = new Date();

        /**
         * コンストラクター
         */
        public UpdateDeckNdockTask(ApplicationMain main) {
            this.main = main;
        }

        @Override
        public void run() {
            if (this.main.getShell().isDisposed()) {
                return;
            }
            // 現在時刻
            boolean visibleHome = false;
            // 遠征を更新する
            this.updateDeck();
            // 入渠を更新する
            this.updateNdock();
            // その他の時間を更新
            this.updateOtherTimer();

            // 遠征通知
            if (this.noticeMission.size() > 0) {
                Sound.randomExpeditionSoundPlay();
                visibleHome |= AppConfig.get().isVisibleOnReturnMission();

                ApplicationMain.sysPrint("遠征通知");

                // Push通知 遠征
                if (AppConfig.get().getPushMission()) {
                    PushNotify.add(StringUtils.join(this.noticeMission, "\r\n"), "遠征",
                            AppConfig.get().getPushPriorityMission());
                }
            }

            // 入渠通知
            if (this.noticeNdock.size() > 0) {
                Sound.randomDockSoundPlay();
                visibleHome |= AppConfig.get().isVisibleOnReturnBathwater();

                ApplicationMain.sysPrint("入渠通知");

                // Push通知 入渠
                if (AppConfig.get().getPushNdock()) {
                    PushNotify.add(StringUtils.join(this.noticeNdock, "\r\n"), "入渠",
                            AppConfig.get().getPushPriorityNdock());
                }
            }

            // 疲労回復通知
            if (this.noticeCond.size() > 0) {
                Sound.randomCondSoundPlay();

                ApplicationMain.sysPrint("疲労通知");

                // Push通知 疲労回復
                if (AppConfig.get().isPushCond()) {
                    PushNotify.add(StringUtils.join(this.noticeCond, "\r\n"), "疲労回復",
                            AppConfig.get().getPushPriorityCond());
                }
            }

            // 泊地修理通知
            if (this.noticeAkashi.size() > 0) {
                Sound.randomAkashiSoundPlay();

                ApplicationMain.sysPrint("泊地修理通知");

                // Push通知 泊地修理
                if (AppConfig.get().isPushAkashi()) {
                    PushNotify.add(StringUtils.join(this.noticeAkashi, "\r\n"), "泊地修理",
                            AppConfig.get().getPushPriorityAkashi());
                }
            }

            if (visibleHome) {
                this.main.getTabFolder().setSelection(0);
            }

            if (AppConfig.get().isUseBalloon()) {
                // バルーンツールチップを表示する
                try {
                    // 遠征・入渠のお知らせ
                    List<String> notice = new ArrayList<String>();
                    notice.addAll(this.noticeMission);
                    notice.addAll(this.noticeNdock);
                    if (notice.size() > 0) {
                        ToolTip tip = new ToolTip(this.main.getShell(), SWT.BALLOON
                                | SWT.ICON_INFORMATION);
                        tip.setText("遠征・入渠");
                        tip.setMessage(StringUtils.join(notice, "\r\n"));
                        this.main.getTrayItem().setToolTip(tip);
                        tip.setVisible(true);
                    }
                } catch (Exception e) {
                    LOG.warn("お知らせの表示に失敗しました", e);
                }
            }

            // エラー表示を更新（入渠遠征とは関係ないけど）
            this.updateErrorLabel();
        }

        private static Color getBackgroundColor(long rest) {
            // 20分前、10分前、5分前になったら背景色を変更する
            if (rest <= (ONE_MINUTES * 5)) {
                return SWTResourceManager
                        .getColor(AppConstants.TIME_IN_5_MIN);
            } else if (rest <= (ONE_MINUTES * 10)) {
                return SWTResourceManager
                        .getColor(AppConstants.TIME_IN_10_MIN);
            } else if (rest <= (ONE_MINUTES * 20)) {
                return SWTResourceManager
                        .getColor(AppConstants.TIME_IN_20_MIN);
            }
            return SWTResourceManager.getColor(SWT.COLOR_WHITE);
        }

        private static Color getCondBackgroundColor(long rest) {
            if (rest <= (ONE_MINUTES * 3)) {
                return SWTResourceManager
                        .getColor(AppConstants.COND_IN_3_MIN);
            }
            return SWTResourceManager.getColor(AppConstants.COND_WAITING);
        }

        private void updateNoticeDeck(String dispname, int index, long rest) {
            if (this.main.getDeckNotice().getSelection()) {
                if ((rest <= ONE_MINUTES) && !FLAG_NOTICE_DECK[index]) {
                    this.noticeMission.add(dispname + " がまもなく帰投します");
                    FLAG_NOTICE_DECK[index] = true;
                } else if (AppConfig.get().isMissionRemind() && (rest < -1)
                        && ((rest % AppConfig.get().getRemindInterbal()) == 0)) {
                    // 定期的にリマインドする
                    this.noticeMission.add(dispname + " がまもなく帰投します");
                } else if (rest > ONE_MINUTES) {
                    FLAG_NOTICE_DECK[index] = false;
                }
            } else {
                FLAG_NOTICE_DECK[index] = false;
            }
        }

        private void updateNoticeAkashi(String dispname, int index, long rest) {
            Boolean notice = FLAG_NOTICE_AKASHI.get(index);
            if (notice == null) {
                notice = new Boolean(false);
            }
            if (this.main.getAkashiNotice().getSelection()) {
                if ((rest <= 0) && !notice) {
                    this.noticeAkashi.add(dispname + " がまもなく修理完了します");
                    notice = true;
                } else if (rest > 0) {
                    notice = false;
                }
            } else {
                notice = false;
            }
            FLAG_NOTICE_AKASHI.put(index, notice);
        }

        private void updateNoticeCond(String dispname, int index, long rest) {
            if (this.main.getCondNotice().getSelection()) {
                if ((rest <= 0) && !FLAG_NOTICE_COND[index]) {
                    this.noticeCond.add(dispname + " 疲労回復しました");
                    FLAG_NOTICE_COND[index] = true;
                } else if (rest > 0) {
                    FLAG_NOTICE_COND[index] = false;
                }
            } else {
                FLAG_NOTICE_COND[index] = false;
            }
        }

        /**
         * 遠征を更新する
         * 
         * @param now
         * @param notice
         * @return
         */
        private void updateDeck() {
            BasicInfoDto basicDto = GlobalContext.getBasicInfo();
            if (AppConfig.get().isNameOnTitlebar() && (basicDto != null)) {
                String titlebarText = basicDto.getNickname() + " - 航海日誌";
                this.main.setTitleText(titlebarText);
            }
            else {
                this.main.setTitleText(AppConstants.TITLEBAR_TEXT);
            }

            Label[] deckNameLabels = {
                    this.main.getDeck1name(), this.main.getDeck2name(),
                    this.main.getDeck3name(), this.main.getDeck4name() };
            Text[] deckTimeTexts = {
                    this.main.getDeck1time(), this.main.getDeck2time(),
                    this.main.getDeck3time(), this.main.getDeck4time() };

            DeckMissionDto[] deckMissions = GlobalContext.getDeckMissions();

            for (int i = 0; i < 4; i++) {
                String time = "";
                String dispname = "";
                String tooltip = null;
                Color backColor = SWTResourceManager.getColor(SWT.COLOR_WHITE);

                DockDto dock = GlobalContext.getDock(String.valueOf(i + 1));

                if (dock != null) {
                    String dockName = dock.getName();
                    if ((i >= 1) && (deckMissions[i - 1].getMission() != null)) {
                        // 遠征中
                        DeckMissionDto mission = deckMissions[i - 1];
                        dispname = dockName + " (" + mission.getMission() + ")";

                        if (mission.getTime() != null) {
                            long rest = TimeLogic.getRest(this.now, mission.getTime());

                            // ツールチップテキストで時刻を表示する
                            tooltip = this.format.format(mission.getTime());

                            // 20分前、10分前、5分前になったら背景色を変更する
                            backColor = getBackgroundColor(rest);

                            // 通知生成
                            this.updateNoticeDeck(dispname, i - 1, rest);

                            time = TimeLogic.toDateRestString(rest);
                            if (time == null) {
                                time = "まもなく帰投します";
                            }
                        }
                    }
                    else {
                        // 遠征中でない
                        // 疲労回復時刻を計算
                        CondTiming condTiming = GlobalContext.getCondTiming();
                        Date condClearTime = null;
                        long condRest = -1;
                        for (ShipDto ship : dock.getShips()) {
                            if (ship.getCond() < AppConfig.get().getOkCond()) {
                                Date clearTime = ship.getCondClearTime(condTiming, AppConfig.get().getOkCond());
                                if ((condClearTime == null) || condClearTime.before(clearTime)) {
                                    condClearTime = clearTime;
                                }
                                condRest = Math.max(condRest, TimeLogic.getRest(this.now, clearTime));
                            }
                        }
                        if (condClearTime != null) {
                            // 疲労回復通知生成
                            this.updateNoticeCond(dockName, i, condRest);
                        }

                        if (dock.isAkashiRepairing()) {
                            // 泊地修理中
                            Date repairStartTime = GlobalContext.getAkashiRepairStart();
                            dispname = dockName + " (泊地修理中)";
                            long past = TimeLogic.getRest(repairStartTime, this.now);
                            time = TimeLogic.toDateRestString(past, true);
                            backColor = SWTResourceManager.getColor(AppConstants.AKASHI_REPAIR_COLOR);

                            // ツールチップで詳細表示
                            List<ShipDto> ships = dock.getShips();
                            for (int p = 0; p < ships.size(); ++p) {
                                ShipDto ship = ships.get(p);
                                if (!ship.isHalfDamage() && (ship.getNowhp() != ship.getMaxhp())) {
                                    long needs = Math.max(AppConstants.AKASHI_REPAIR_MINIMUM, ship.getDocktime());
                                    Date finish = new Date(repairStartTime.getTime() + needs);
                                    long rest = TimeLogic.getRest(this.now, finish);

                                    String txt = ship.getFriendlyName();

                                    // 通知生成
                                    this.updateNoticeAkashi(txt, (i * 6) + p, rest);

                                    String remainStr = TimeLogic.toDateRestString(rest);
                                    if (remainStr == null) {
                                        txt += ":まもなく修理完了します";
                                    }
                                    else {
                                        txt += ":残り" + remainStr + "(" + this.format.format(finish) + ")";
                                    }

                                    if (tooltip == null) {
                                        tooltip = txt;
                                    }
                                    else {
                                        tooltip += "\n" + txt;
                                    }
                                }
                            }
                        }
                        else if (condClearTime != null) {
                            dispname = dockName + " (疲労回復中)";

                            // ツールチップテキストで時刻を表示する
                            tooltip = this.format.format(condClearTime);

                            // 20分前、10分前、5分前になったら背景色を変更する
                            backColor = getCondBackgroundColor(condRest);

                            time = TimeLogic.toDateRestString(condRest);
                            if (time == null) {
                                time = "疲労回復しました";
                            }
                        }
                    }
                }

                deckNameLabels[i].setText(dispname);
                deckTimeTexts[i].setText(time);
                deckTimeTexts[i].setToolTipText(tooltip);
                deckTimeTexts[i].setBackground(backColor);
            }
        }

        private void updateNdockNotice(String name, int index, long rest) {
            if (this.main.getNdockNotice().getSelection()) {

                if ((rest <= ONE_MINUTES) && !FLAG_NOTICE_NDOCK[index]) {
                    this.noticeNdock.add(name + " がまもなくお風呂からあがります");
                    FLAG_NOTICE_NDOCK[index] = true;
                } else if (rest > ONE_MINUTES) {
                    FLAG_NOTICE_NDOCK[index] = false;
                }
            } else {
                FLAG_NOTICE_NDOCK[index] = false;
            }
        }

        /**
         * 入渠を更新する
         * 
         * @param now
         * @param notice
         * @return
         */
        private void updateNdock() {
            Map<Integer, ShipDto> shipMap = GlobalContext.getShipMap();

            Label[] ndockNameLabels = { this.main.getNdock1name(), this.main.getNdock2name(),
                    this.main.getNdock3name(), this.main.getNdock4name() };
            Text[] ndockTimeTexts = { this.main.getNdock1time(), this.main.getNdock2time(), this.main.getNdock3time(),
                    this.main.getNdock4time() };

            NdockDto[] ndocks = GlobalContext.getNdocks();

            for (int i = 0; i < ndocks.length; i++) {
                String name = "";
                String time = "";

                if (ndocks[i].getNdockid() != 0) {
                    ShipDto ship = shipMap.get(ndocks[i].getNdockid());
                    if (ship != null) {
                        name = ship.getFriendlyName();
                        long rest = TimeLogic.getRest(this.now, ndocks[i].getNdocktime());

                        // ツールチップテキストで時刻を表示する
                        ndockTimeTexts[i].setToolTipText(this.format.format(ndocks[i].getNdocktime()));

                        // 20分前、10分前、5分前になったら背景色を変更する
                        ndockTimeTexts[i].setBackground(getBackgroundColor(rest));

                        // 通知生成
                        this.updateNdockNotice(name, i, rest);

                        time = TimeLogic.toDateRestString(rest);
                        if (time == null) {
                            time = "まもなくお風呂からあがります";
                        }
                    }
                } else {
                    ndockTimeTexts[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
                    ndockTimeTexts[i].setToolTipText(null);
                }
                ndockNameLabels[i].setText(name);
                ndockTimeTexts[i].setText(time);
            }
        }

        private void updateOtherTimer() {
            Label condTimerLabel = this.main.getCondTimerLabel();
            Text condTimerText = this.main.getCondTimerTime();

            CondTiming timing = GlobalContext.getCondTiming();
            Date nextUpdateTime = timing.getNextUpdateTime(this.now);

            if (nextUpdateTime == null) {
                condTimerLabel.setText("");
                condTimerText.setToolTipText(null);
                condTimerText.setText("");
            }
            else {
                int accuracy = (int) timing.getCurrentAccuracy() / 2000;
                condTimerLabel.setText("次の疲労回復まで(±" + accuracy + "秒)");

                long rest = TimeLogic.getRest(this.now, nextUpdateTime);

                // ツールチップテキストで時刻を表示する
                condTimerText.setToolTipText(this.format.format(nextUpdateTime));
                condTimerText.setText(TimeLogic.toDateRestString(rest, true));
            }
        }

        /** エラー表示を更新 */
        private void updateErrorLabel() {
            int state = GlobalContext.getState();
            Label errorLabel = ApplicationMain.main.getErrorLabel();
            boolean printLabel = false;
            String errorText = "正常";
            if ((state != 0) && (state != 1)) {
                errorText = "データが不完全です（理由不明）";
                if (state == 2) {
                    errorText = "艦これのリロードが必要です";
                }
                else if (state == 3) {
                    errorText = "航海日誌の再起動が必要です\r\n（アカウントが変更されたため）";
                }
                printLabel = true;
            }
            if ((errorLabel.getText().equals(errorText) == false) || (errorLabel.getVisible() != printLabel)) {
                errorLabel.setText(errorText);
                LayoutLogic.hide(errorLabel, !printLabel);
                errorLabel.setVisible(printLabel);
                ApplicationMain.main.getMainComposite().layout();
            }
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
            if (this.main.getShell().isDisposed()) {
                return;
            }
            // タブを更新する
            CTabItem maintab = this.main.getTabFolder().getItem(0);
            maintab.setToolTipText(
                    "装備:" + GlobalContext.getItemMap().size() + "/"
                            + GlobalContext.maxSlotitem()
                            + " 艦娘:" + GlobalContext.getShipMap().size() + "/"
                            + GlobalContext.maxChara());

            boolean combinedFleetBadlyDamaed = false;
            if (GlobalContext.isCombined()) {
                combinedFleetBadlyDamaed =
                        GlobalContext.getDock("1").isBadlyDamaged() ||
                                GlobalContext.getDock("2").isBadlyDamaged();
            }

            for (int i = 0; i < 4; i++) {
                DockDto dock = GlobalContext.getDock(Integer.toString(i + 1));
                if (dock != null) {
                    FleetComposite tabComposite = dockComposites[i];
                    CTabItem tabItem = tabItems[i];

                    if (tabItem == null) {

                        tabItem = new CTabItem(this.main.getTabFolder(), SWT.NONE);
                        tabItem.setText(dock.getName());

                        // メインコンポジット
                        tabComposite = new FleetComposite(this.main.getTabFolder(), tabItem, this.main);
                        tabItem.setControl(tabComposite);

                        tabItems[i] = tabItem;
                        dockComposites[i] = tabComposite;
                    }
                    if (!dock.getName().equals(dockname[i])) {
                        dockname[i] = dock.getName();
                    }

                    tabComposite.updateFleet(dock, (i < 2) ? combinedFleetBadlyDamaed : false);
                    tabItem.setText(dock.getName());
                    dock.setUpdate(false);
                }
            }
        }
    }
}
