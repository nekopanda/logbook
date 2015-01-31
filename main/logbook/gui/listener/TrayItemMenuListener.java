package logbook.gui.listener;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import logbook.config.AppConfig;
import logbook.data.context.GlobalContext;
import logbook.dto.DeckMissionDto;
import logbook.dto.NdockDto;
import logbook.dto.ShipDto;
import logbook.gui.ApplicationMain;
import logbook.gui.ConfigDialog;
import logbook.gui.ResourceChartDialog;
import logbook.gui.WindowBase;
import logbook.gui.logic.TimeLogic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * トレイアイコンにメニューを表示するリスナー
 *
 */
public final class TrayItemMenuListener implements MenuDetectListener {
    /** メニュー */
    private Menu menu;

    /**
     * コンストラクター
     */
    public TrayItemMenuListener() {
    }

    @Override
    public void menuDetected(MenuDetectEvent e) {
        // メニュー
        if (this.menu != null) {
            this.menu.dispose();
        }
        final Shell shell = ApplicationMain.main.getShell();
        this.menu = new Menu(shell);
        // 装備数
        int itemCount = GlobalContext.getItemMap().size();
        // 最大保有可能 装備数
        int itemMax = GlobalContext.maxSlotitem();
        // 艦娘数
        int shipCount = GlobalContext.getShipMap().size();
        // 最大保有可能 艦娘数
        int shipMax = GlobalContext.maxChara();

        String[] shpiTableNames = AppConfig.get().getShipTableNames();

        WindowBase[] winList = ApplicationMain.main.getWindowList();
        String[] nameList = new String[] {
                "キャプチャ(&C)", // 0
                "ドロップ報告書(&D)", // 1
                "建造報告書(&Y)", // 2
                "開発報告書(&E)", // 3
                "遠征報告書(&T)", // 4
                "所有装備一覧(&X) (" + itemCount + "/" + itemMax + ")", // 5
                shpiTableNames[0] + "(&S) (" + shipCount + "/" + shipMax + ")", // 6
                shpiTableNames[1] + "(&2)", // 7
                shpiTableNames[2] + "(&3)", // 8
                shpiTableNames[3] + "(&4)", // 9
                "お風呂に入りたい艦娘(&N)", // 10
                "任務一覧(&Q)", // 11
                "戦況(&B)", // 12
                "戦況-横(&H)", // 13
                "自軍敵軍パラメータ(&P)", // 14
                "経験値計算機(&C)", // 15
                "演習経験値計算機(&V)", // 16
                "グループエディタ(&G)", // 17
                "出撃統計(&A)", // 18
                "ロー" // 最後は自分
        };
        boolean[] hasSeparator = new boolean[] {
                false,
                true,
                false,
                false,
                false,
                true,
                true,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
        };

        for (int i = 0; i < (winList.length - 1); i++) {
            if (hasSeparator[i]) {
                new MenuItem(this.menu, SWT.SEPARATOR);
            }
            final WindowBase win = winList[i];
            final MenuItem menuItem = new MenuItem(this.menu, SWT.PUSH);
            menuItem.setText(nameList[i]);
            menuItem.setSelection(win.getMenuItem().getSelection());
            menuItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    boolean open = menuItem.getSelection();
                    if (open) {
                        win.open();
                        win.getShell().setActive();
                    }
                    else {
                        win.hideWindow();
                    }
                }
            });
        }

        // 資材チャート
        MenuItem resourceChart = new MenuItem(this.menu, SWT.NONE);
        resourceChart.setText("資材チャート(&R)");
        resourceChart.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ResourceChartDialog(shell).open();
            }
        });
        new MenuItem(this.menu, SWT.SEPARATOR);
        // 遠征
        MenuItem missionItem = new MenuItem(this.menu, SWT.CASCADE);
        missionItem.setText("遠征(&M)");
        Menu missionMenu = new Menu(missionItem);
        missionItem.setMenu(missionMenu);
        DeckMissionDto[] missions = GlobalContext.getDeckMissions();
        for (DeckMissionDto missionDto : missions) {
            if ((missionDto != null) && (missionDto.getTime() != null)) {
                MenuItem item = new MenuItem(missionMenu, SWT.NONE);
                String text = missionDto.getName() + " (" + missionDto.getMission() + ")";
                long rest = getRest(Calendar.getInstance().getTime(), missionDto.getTime());
                if (rest <= 0) {
                    item.setText(text + "\tまもなく帰投します");
                } else {
                    item.setText(text + "\t" + TimeLogic.toDateRestString(rest));
                }
            }
        }
        // 入渠
        MenuItem ndockItem = new MenuItem(this.menu, SWT.CASCADE);
        ndockItem.setText("入渠(&M)");
        Menu ndockMenu = new Menu(ndockItem);
        ndockItem.setMenu(ndockMenu);
        Map<Integer, ShipDto> shipMap = GlobalContext.getShipMap();
        NdockDto[] ndocks = GlobalContext.getNdocks();
        for (NdockDto ndockDto : ndocks) {
            if ((ndockDto != null) && (shipMap.get(ndockDto.getNdockid()) != null)) {
                ShipDto ship = shipMap.get(ndockDto.getNdockid());
                MenuItem item = new MenuItem(ndockMenu, SWT.NONE);
                String text = ship.getName() + " (Lv" + ship.getLv() + ")";
                long rest = getRest(Calendar.getInstance().getTime(), ndockDto.getNdocktime());
                if (rest <= 0) {
                    item.setText(text + "\tまもなくお風呂から上がります");
                } else {
                    item.setText(text + "\t" + TimeLogic.toDateRestString(rest));
                }
            }
        }

        new MenuItem(this.menu, SWT.SEPARATOR);
        // 設定
        MenuItem config = new MenuItem(this.menu, SWT.NONE);
        config.setText("設定(&O)");
        config.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ConfigDialog(ApplicationMain.main).open();
            }
        });
        // 終了
        final MenuItem dispose = new MenuItem(this.menu, SWT.NONE);
        dispose.setText("終了");
        dispose.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });
        this.menu.setVisible(true);
    }

    /**
     * 2つの日付から残り時間を計算する
     * 
     * @param date1
     * @param date2
     * @return
     */
    private static long getRest(Date date1, Date date2) {
        return TimeUnit.MILLISECONDS.toSeconds(date2.getTime() - date1.getTime());
    }
}