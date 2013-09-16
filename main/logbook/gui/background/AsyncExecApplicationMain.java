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

import logbook.data.context.GlobalContext;
import logbook.dto.DeckMissionDto;
import logbook.dto.ShipDto;
import logbook.gui.logic.Sound;
import logbook.gui.logic.TimeLogic;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 非同期にメイン画面を更新します
 */
public final class AsyncExecApplicationMain extends Thread {
    private static final Logger LOG = LogManager.getLogger(AsyncExecApplicationMain.class);

    private static final int ONE_SECONDS_FORMILIS = 1000;
    private static final int ONE_MINUTES = 60;

    private final Shell shell;
    private final TrayItem item;

    private final Button itemList;
    private final Button shipList;
    private final Button deckNotice;
    private final Label deck1name;
    private final Text deck1time;
    private final Label deck2name;
    private final Text deck2time;
    private final Label deck3name;
    private final Text deck3time;
    private final Button ndockNotice;
    private final Label ndock1name;
    private final Text ndock1time;
    private final Label ndock2name;
    private final Text ndock2time;
    private final Label ndock3name;
    private final Text ndock3time;
    private final Label ndock4name;
    private final Text ndock4time;

    private final boolean[] flagNoticeDeck = { false, false, false };
    private final boolean[] flagNoticeNdock = { false, false, false, false };

    /**
     * 非同期にメイン画面を更新するスレッドのコンストラクター
     * 
     * @param display
     * @param shell
     * @param item
     * @param itemList
     * @param shipList
     * @param deckNotice
     * @param deck1name
     * @param deck1time
     * @param deck2name
     * @param deck2time
     * @param deck3name
     * @param deck3time
     * @param ndockNotice
     * @param ndock1name
     * @param ndock1time
     * @param ndock2name
     * @param ndock2time
     * @param ndock3name
     * @param ndock3time
     * @param ndock4name
     * @param ndock4time
     */
    public AsyncExecApplicationMain(Shell shell, TrayItem item, Button itemList, Button shipList,
            Button deckNotice, Label deck1name, Text deck1time, Label deck2name, Text deck2time, Label deck3name,
            Text deck3time, Button ndockNotice, Label ndock1name, Text ndock1time, Label ndock2name, Text ndock2time,
            Label ndock3name, Text ndock3time, Label ndock4name, Text ndock4time) {
        this.shell = shell;
        this.item = item;

        this.itemList = itemList;
        this.shipList = shipList;
        this.deckNotice = deckNotice;
        this.deck1name = deck1name;
        this.deck1time = deck1time;
        this.deck2name = deck2name;
        this.deck2time = deck2time;
        this.deck3name = deck3name;
        this.deck3time = deck3time;
        this.ndockNotice = ndockNotice;
        this.ndock1name = ndock1name;
        this.ndock1time = ndock1time;
        this.ndock2name = ndock2name;
        this.ndock2time = ndock2time;
        this.ndock3name = ndock3name;
        this.ndock3time = ndock3time;
        this.ndock4name = ndock4name;
        this.ndock4time = ndock4time;
    }

    /**
     * 現在のメイン画面を更新します
     */
    @Override
    public void run() {

        try {
            while (true) {
                GlobalContext.updateContext();

                Display.getDefault().asyncExec(new Runnable() {
                    private final Logger log = LogManager.getLogger(AsyncExecApplicationMain.class);

                    @Override
                    public void run() {
                        // 現在時刻
                        Date now = Calendar.getInstance().getTime();

                        List<String> notice = new ArrayList<String>();

                        // 保有アイテム数を更新する
                        this.updateItemCount();
                        // 保有艦娘数を更新する
                        this.updateShipCount();

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
                                ToolTip tip = new ToolTip(AsyncExecApplicationMain.this.shell, SWT.BALLOON
                                        | SWT.ICON_INFORMATION);
                                tip.setText("遠征・入渠");
                                tip.setMessage(StringUtils.join(notice, "\r\n"));
                                AsyncExecApplicationMain.this.item.setToolTip(tip);
                                tip.setVisible(true);
                            }
                        } catch (Exception e) {
                            this.log.warn("お知らせの表示に失敗しました", e);
                        }
                    }

                    /**
                     * 保有アイテム数を更新する
                     */
                    private void updateItemCount() {
                        Button itemList = AsyncExecApplicationMain.this.itemList;
                        String setText = "所有装備一覧(" + GlobalContext.getItemMap().size() + ")";
                        if (!setText.equals(itemList.getText())) {
                            itemList.setText(setText);
                            itemList.getParent().layout();
                        }
                    }

                    /**
                     * 保有艦娘数を更新する
                     */
                    private void updateShipCount() {
                        Button shipList = AsyncExecApplicationMain.this.shipList;
                        String setText = "所有艦娘一覧(" + GlobalContext.getShipMap().size() + ")";
                        if (!setText.equals(shipList.getText())) {
                            shipList.setText(setText);
                            shipList.getParent().layout();
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

                        Label[] deckNameLabels = { AsyncExecApplicationMain.this.deck1name,
                                AsyncExecApplicationMain.this.deck2name, AsyncExecApplicationMain.this.deck3name
                        };
                        Text[] deckTimeTexts = { AsyncExecApplicationMain.this.deck1time,
                                AsyncExecApplicationMain.this.deck2time, AsyncExecApplicationMain.this.deck3time
                        };

                        DeckMissionDto[] deckMissions = { GlobalContext.getDeck1Mission(),
                                GlobalContext.getDeck2Mission(), GlobalContext.getDeck3Mission() };

                        for (int i = 0; i < 3; i++) {
                            String time = "";
                            String dispname = "";
                            if ((deckMissions[i] != null) && (deckMissions[i].getMission() != null)) {
                                dispname = deckMissions[i].getName() + " (" + deckMissions[i].getMission() + ")";
                                if (deckMissions[i].getTime() != null) {
                                    long rest = getRest(now, deckMissions[i].getTime());
                                    // 20分前、10分前、5分前になったら背景色を変更する
                                    if (rest <= (ONE_MINUTES * 5)) {
                                        deckTimeTexts[i].setBackground(SWTResourceManager.getColor(255, 215, 0));
                                    } else if (rest <= (ONE_MINUTES * 10)) {
                                        deckTimeTexts[i].setBackground(SWTResourceManager.getColor(255, 239, 153));
                                    } else if (rest <= (ONE_MINUTES * 20)) {
                                        deckTimeTexts[i].setBackground(SWTResourceManager.getColor(255, 247, 203));
                                    } else {
                                        deckTimeTexts[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
                                    }
                                    if (AsyncExecApplicationMain.this.deckNotice.getSelection()) {
                                        if (((rest <= ONE_MINUTES) && !AsyncExecApplicationMain.this.flagNoticeDeck[i])) {
                                            notice.add(dispname + " がまもなく帰投します");
                                            noticeflg = true;
                                            AsyncExecApplicationMain.this.flagNoticeDeck[i] = true;
                                        } else if (rest > ONE_MINUTES) {
                                            AsyncExecApplicationMain.this.flagNoticeDeck[i] = false;
                                        }
                                    } else {
                                        AsyncExecApplicationMain.this.flagNoticeDeck[i] = false;
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

                        Map<String, ShipDto> shipMap = GlobalContext.getShipMap();

                        Label[] ndockNameLabels = { AsyncExecApplicationMain.this.ndock1name,
                                AsyncExecApplicationMain.this.ndock2name, AsyncExecApplicationMain.this.ndock3name,
                                AsyncExecApplicationMain.this.ndock4name };
                        Text[] ndockTimeTexts = { AsyncExecApplicationMain.this.ndock1time,
                                AsyncExecApplicationMain.this.ndock2time, AsyncExecApplicationMain.this.ndock3time,
                                AsyncExecApplicationMain.this.ndock4time };

                        long[] ids = { GlobalContext.getNdock1id(), GlobalContext.getNdock2id(),
                                GlobalContext.getNdock3id(), GlobalContext.getNdock4id() };
                        Date[] times = { GlobalContext.getNdock1time(), GlobalContext.getNdock2time(),
                                GlobalContext.getNdock3time(), GlobalContext.getNdock4time() };

                        for (int i = 0; i < times.length; i++) {
                            String name = "";
                            String time = "";

                            if (ids[i] != 0) {
                                ShipDto ship = shipMap.get(Long.toString(ids[i]));
                                if (ship != null) {
                                    name = ship.getName() + " (Lv" + ship.getLv() + ")";
                                    long rest = getRest(now, times[i]);
                                    // 20分前、10分前、5分前になったら背景色を変更する
                                    if (rest <= (ONE_MINUTES * 5)) {
                                        ndockTimeTexts[i].setBackground(SWTResourceManager.getColor(255, 215, 0));
                                    } else if (rest <= (ONE_MINUTES * 10)) {
                                        ndockTimeTexts[i].setBackground(SWTResourceManager.getColor(255, 239, 153));
                                    } else if (rest <= (ONE_MINUTES * 20)) {
                                        ndockTimeTexts[i].setBackground(SWTResourceManager.getColor(255, 247, 203));
                                    } else {
                                        ndockTimeTexts[i].setBackground(SWTResourceManager
                                                .getColor(SWT.COLOR_WHITE));
                                    }
                                    if (AsyncExecApplicationMain.this.ndockNotice.getSelection()) {

                                        if ((rest <= ONE_MINUTES) && !AsyncExecApplicationMain.this.flagNoticeNdock[i]) {
                                            notice.add(name + " がまもなくお風呂からあがります");
                                            noticeflg = true;
                                            AsyncExecApplicationMain.this.flagNoticeNdock[i] = true;
                                        } else if (rest > ONE_MINUTES) {
                                            AsyncExecApplicationMain.this.flagNoticeNdock[i] = false;
                                        }
                                    } else {
                                        AsyncExecApplicationMain.this.flagNoticeNdock[i] = false;
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
                });
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
}
