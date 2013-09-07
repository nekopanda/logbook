package logbook.gui.background;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import logbook.data.context.GlobalContext;
import logbook.dto.ShipDto;

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

/**
 * 非同期にメイン画面を更新します
 */
public final class AsyncExecApplicationMain extends Thread {
    private static final Logger LOG = LogManager.getLogger(AsyncExecApplicationMain.class);

    private static final int ONE_SECONDS_FORMILIS = 1000;
    private static final int ONE_MINUTES = 60;
    private static final int ONE_HOURS = 60 * 60;

    private final Display display;
    private final Shell shell;
    private final TrayItem item;

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
     * @param deckNotice
     * @param deck1name
     * @param deck1mission
     * @param deck1time
     * @param deck2name
     * @param deck2mission
     * @param deck2time
     * @param deck3name
     * @param deck3mission
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
    public AsyncExecApplicationMain(Display display, Shell shell, TrayItem item, Button deckNotice, Label deck1name,
            Text deck1time, Label deck2name, Text deck2time, Label deck3name, Text deck3time, Button ndockNotice,
            Label ndock1name, Text ndock1time, Label ndock2name, Text ndock2time, Label ndock3name, Text ndock3time,
            Label ndock4name, Text ndock4time) {
        this.display = display;
        this.shell = shell;
        this.item = item;

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

                this.display.asyncExec(new Runnable() {
                    private final Logger log = LogManager.getLogger(AsyncExecApplicationMain.class);

                    @Override
                    public void run() {
                        // 現在時刻
                        Date now = Calendar.getInstance().getTime();

                        List<String> notice = new ArrayList<String>();

                        // 遠征を更新する
                        this.updateDeck(now, notice);
                        // 入渠を更新する
                        this.updateNdock(now, notice);

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
                     * 遠征を更新する
                     * @param now
                     */
                    private void updateDeck(Date now, List<String> notice) {

                        Label[] deckNameLabels = { AsyncExecApplicationMain.this.deck1name,
                                AsyncExecApplicationMain.this.deck2name, AsyncExecApplicationMain.this.deck3name
                        };
                        Text[] deckTimeTexts = { AsyncExecApplicationMain.this.deck1time,
                                AsyncExecApplicationMain.this.deck2time, AsyncExecApplicationMain.this.deck3time
                        };
                        String[] deckNames = { GlobalContext.getDeck1Name(), GlobalContext.getDeck2Name(),
                                GlobalContext.getDeck3Name() };
                        String[] deckMissions = { GlobalContext.getDeck1mission(), GlobalContext.getDeck2mission(),
                                GlobalContext.getDeck3mission() };
                        Date[] deckTimes = { GlobalContext.getDeck1Time(), GlobalContext.getDeck2Time(),
                                GlobalContext.getDeck3Time() };

                        for (int i = 0; i < deckTimes.length; i++) {
                            String time = "";
                            String dispname = "";
                            if (!StringUtils.isEmpty(deckMissions[i])) {
                                dispname = deckNames[i] + " (" + deckMissions[i] + ")";
                                if (deckTimes[i] != null) {
                                    long rest = getRest(now, deckTimes[i]);
                                    if (AsyncExecApplicationMain.this.deckNotice.getSelection()) {
                                        if (((rest <= ONE_MINUTES) && !AsyncExecApplicationMain.this.flagNoticeDeck[i])) {
                                            notice.add(dispname + " がまもなく帰投します");
                                            AsyncExecApplicationMain.this.flagNoticeDeck[i] = true;
                                        } else if (rest > ONE_MINUTES) {
                                            AsyncExecApplicationMain.this.flagNoticeDeck[i] = false;
                                        }
                                    } else {
                                        AsyncExecApplicationMain.this.flagNoticeDeck[i] = false;
                                    }
                                    time = toDateRestString(rest);
                                    if (time == null) {
                                        time = "まもなく帰投します";
                                    }
                                }
                            }
                            deckNameLabels[i].setText(dispname);
                            deckTimeTexts[i].setText(time);
                        }
                    }

                    /**
                     * 入渠を更新する
                     * @param now
                     */
                    private void updateNdock(Date now, List<String> notice) {
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

                                    if (AsyncExecApplicationMain.this.ndockNotice.getSelection()) {
                                        if ((rest <= ONE_MINUTES) && !AsyncExecApplicationMain.this.flagNoticeNdock[i]) {
                                            notice.add(name + " がまもなくお風呂からあがります");
                                            AsyncExecApplicationMain.this.flagNoticeNdock[i] = true;
                                        } else if (rest > ONE_MINUTES) {
                                            AsyncExecApplicationMain.this.flagNoticeNdock[i] = false;
                                        }
                                    } else {
                                        AsyncExecApplicationMain.this.flagNoticeNdock[i] = false;
                                    }
                                    time = toDateRestString(rest);
                                    if (time == null) {
                                        time = "まもなくお風呂からあがります";
                                    }
                                }
                            }
                            ndockNameLabels[i].setText(name);
                            ndockTimeTexts[i].setText(time);
                        }
                    }
                });
                Thread.sleep(ONE_SECONDS_FORMILIS);
            }
        } catch (Exception e) {
            LOG.fatal("スレッドが異常終了しました", e);
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
        return (date2.getTime() - date1.getTime()) / ONE_SECONDS_FORMILIS;
    }

    /**
     * 残り時間を見やすい形式に整形する
     * 
     * @param rest
     * @return
     */
    private static String toDateRestString(long rest) {
        if (rest > 0) {
            if (rest > ONE_HOURS) {
                return (rest / ONE_HOURS) + "時間" + ((rest % ONE_HOURS) / ONE_MINUTES) + "分";
            } else if (rest > ONE_MINUTES) {
                return (rest / ONE_MINUTES) + "分" + (rest % ONE_MINUTES) + "秒";
            } else {
                return rest + "秒";
            }
        } else {
            return null;
        }
    }
}
