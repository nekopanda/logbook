package logbook.gui.listener;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import logbook.data.context.GlobalContext;
import logbook.dto.DeckMissionDto;
import logbook.dto.NdockDto;
import logbook.dto.ShipDto;
import logbook.gui.BattleAggDialog;
import logbook.gui.ResourceChartDialog;
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
    /**　Shell */
    private final Shell shell;
    /** メニュー */
    private Menu menu;

    /**
     * コンストラクター
     */
    public TrayItemMenuListener(Shell shell) {
        this.shell = shell;
    }

    @Override
    public void menuDetected(MenuDetectEvent e) {
        // メニュー
        if (this.menu != null) {
            this.menu.dispose();
        }
        this.menu = new Menu(this.shell);
        // 装備数
        int itemCount = GlobalContext.getItemMap().size();
        // 最大保有可能 装備数
        int itemMax = GlobalContext.maxSlotitem();
        // 艦娘数
        int shipCount = GlobalContext.getShipMap().size();
        // 最大保有可能 艦娘数
        int shipMax = GlobalContext.maxChara();

        // 所有装備一覧
        MenuItem itemlist = new MenuItem(this.menu, SWT.NONE);
        itemlist.setText("所有装備一覧(&I) (" + itemCount + "/" + itemMax + ")");
        itemlist.addSelectionListener(new ItemListReportAdapter(this.shell));
        // 所有艦娘一覧
        MenuItem shiplist = new MenuItem(this.menu, SWT.NONE);
        shiplist.setText("所有艦娘一覧(&S) (" + shipCount + "/" + shipMax + ")");
        shiplist.addSelectionListener(new ShipListReportAdapter(this.shell));
        new MenuItem(this.menu, SWT.SEPARATOR);

        // 報告書
        MenuItem reportItem = new MenuItem(this.menu, SWT.CASCADE);
        reportItem.setText("報告書(&R)");
        Menu reportMenu = new Menu(reportItem);
        reportItem.setMenu(reportMenu);
        // 報告書-ドロップ報告書
        MenuItem drop = new MenuItem(reportMenu, SWT.NONE);
        drop.setText("ドロップ報告書(&D)");
        drop.addSelectionListener(new DropReportAdapter(this.shell));
        // 報告書-建造報告書
        MenuItem createship = new MenuItem(reportMenu, SWT.NONE);
        createship.setText("建造報告書(&B)");
        createship.addSelectionListener(new CreateShipReportAdapter(this.shell));
        // 報告書-開発報告書
        MenuItem createitem = new MenuItem(reportMenu, SWT.NONE);
        createitem.setText("開発報告書(&E)");
        createitem.addSelectionListener(new CreateItemReportAdapter(this.shell));
        // 報告書-遠征報告書
        MenuItem missionresult = new MenuItem(reportMenu, SWT.NONE);
        missionresult.setText("遠征報告書(&T)");
        missionresult.addSelectionListener(new MissionResultReportAdapter(this.shell));

        // 統計
        MenuItem calcItem = new MenuItem(this.menu, SWT.CASCADE);
        calcItem.setText("計算機(&C)");
        Menu calcMenu = new Menu(calcItem);
        calcItem.setMenu(calcMenu);
        // 経験値計算
        MenuItem calcexp = new MenuItem(calcMenu, SWT.NONE);
        calcexp.setText("経験値計算機(&C)");
        calcexp.addSelectionListener(new CalcExpAdapter(this.shell));

        // 統計
        MenuItem statItem = new MenuItem(this.menu, SWT.CASCADE);
        statItem.setText("統計(&A)");
        Menu statMenu = new Menu(statItem);
        statItem.setMenu(statMenu);
        // 資材チャート
        MenuItem resourceChart = new MenuItem(statMenu, SWT.NONE);
        resourceChart.setText("資材チャート(&R)");
        resourceChart.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ResourceChartDialog(TrayItemMenuListener.this.shell).open();
            }
        });
        // 出撃統計
        MenuItem battleCounter = new MenuItem(statMenu, SWT.NONE);
        battleCounter.setText("出撃統計(&A)");
        battleCounter.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new BattleAggDialog(TrayItemMenuListener.this.shell).open();
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
        Map<Long, ShipDto> shipMap = GlobalContext.getShipMap();
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
        config.setText("設定(&P)");
        config.addSelectionListener(new ConfigDialogAdapter(this.shell));
        // 終了
        final MenuItem dispose = new MenuItem(this.menu, SWT.NONE);
        dispose.setText("終了(&X)");
        dispose.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TrayItemMenuListener.this.shell.close();
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