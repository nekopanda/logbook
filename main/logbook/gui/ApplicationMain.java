package logbook.gui;

import static logbook.config.GlobalConfig.*;
import logbook.config.GlobalConfig;
import logbook.gui.background.AsyncExecApplicationMain;
import logbook.gui.background.AsyncExecApplicationMainConsole;
import logbook.gui.listener.CreateItemReportAdapter;
import logbook.gui.listener.CreateShipReportAdapter;
import logbook.gui.listener.DropReportAdapter;
import logbook.gui.listener.HelpEventListener;
import logbook.gui.listener.ItemListReportAdapter;
import logbook.gui.listener.ShellEventAdapter;
import logbook.gui.listener.ShipListReportAdapter;
import logbook.gui.listener.TraySelectionListener;
import logbook.gui.logic.Sound;
import logbook.server.proxy.ProxyServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

/**
 * メイン画面
 *
 */
public final class ApplicationMain {

    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(ApplicationMain.class);

    /** シェル */
    private Shell shell;

    /**
     * Launch the application.
     * @param args
     */
    public static void main(String[] args) {
        try {
            ApplicationMain window = new ApplicationMain();
            window.open();
        } catch (Exception e) {
            LOG.fatal(e);
        }
    }

    /**
     * Open the window.
     */
    public void open() {
        Display display = Display.getDefault();
        this.createContents();
        this.shell.open();
        this.shell.layout();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * 画面レイアウトを作成します
     */
    public void createContents() {

        // プロキシサーバーを開始する
        ProxyServer.start(getConfig().getListenPort());

        final Display display = Display.getDefault();
        this.shell = new Shell(SWT.SHELL_TRIM | getConfig().getOnTop());
        this.shell.setSize(getConfig().getWidth(), getConfig().getHeight());
        this.shell.setText("航海日誌 " + GlobalConfig.VERSION);
        this.shell.setLayout(new GridLayout(1, false));

        // キーが押された時に呼ばれるリスナーを追加します
        this.shell.addHelpListener(new HelpEventListener(this.shell));

        // トレイアイコンを追加します
        final Tray tray = display.getSystemTray();
        final TrayItem item = new TrayItem(tray, SWT.NONE);
        final Image image = display.getSystemImage(SWT.ICON_INFORMATION);
        item.setImage(image);
        item.addListener(SWT.Selection, new TraySelectionListener(this.shell));

        // 閉じるときに呼ばれるリスナーを追加します
        this.shell.addShellListener(new ShellEventAdapter(item, image));

        // コマンドボタン
        Composite command = new Composite(this.shell, SWT.NONE);
        command.setLayout(new RowLayout());
        command.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Button dropResult = new Button(command, SWT.PUSH);
        dropResult.setText("ドロップ報告書");
        dropResult.addSelectionListener(new DropReportAdapter(this.shell));
        Button createShip = new Button(command, SWT.PUSH);
        createShip.setText("建造報告書");
        createShip.addSelectionListener(new CreateShipReportAdapter(this.shell));
        Button createItem = new Button(command, SWT.PUSH);
        createItem.setText("開発報告書");
        createItem.addSelectionListener(new CreateItemReportAdapter(this.shell));
        Button itemList = new Button(command, SWT.PUSH);
        itemList.setText("所有装備一覧(0)");
        itemList.addSelectionListener(new ItemListReportAdapter(this.shell));
        Button shipList = new Button(command, SWT.PUSH);
        shipList.setText("所有艦娘一覧(0)");
        shipList.addSelectionListener(new ShipListReportAdapter(this.shell));

        // 遠征
        Group deckGroup = new Group(this.shell, SWT.NONE);
        deckGroup.setText("遠征");
        deckGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout glDeckGroup = new GridLayout(2, true);
        glDeckGroup.verticalSpacing = 2;
        glDeckGroup.marginWidth = 2;
        glDeckGroup.marginHeight = 2;
        glDeckGroup.horizontalSpacing = 2;
        deckGroup.setLayout(glDeckGroup);

        Button deckNotice = new Button(deckGroup, SWT.CHECK);
        deckNotice.setSelection(true);
        deckNotice.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        deckNotice.setText("帰投する1分前になったら通知する");

        Label deck1name = new Label(deckGroup, SWT.NONE);
        deck1name.setText("ここに艦隊2の艦隊名が入ります");
        deck1name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text deck1time = new Text(deckGroup, SWT.SINGLE | SWT.BORDER);
        deck1time.setText("艦隊2の帰投時間");
        deck1time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label deck2name = new Label(deckGroup, SWT.NONE);
        deck2name.setText("ここに艦隊3の艦隊名が入ります");
        deck2name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text deck2time = new Text(deckGroup, SWT.SINGLE | SWT.BORDER);
        deck2time.setText("艦隊3の帰投時間");
        deck2time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label deck3name = new Label(deckGroup, SWT.NONE);
        deck3name.setText("ここに艦隊4の艦隊名が入ります");
        deck3name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text deck3time = new Text(deckGroup, SWT.SINGLE | SWT.BORDER);
        deck3time.setText("艦隊4の帰投時間");
        deck3time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // 入渠
        Group ndockGroup = new Group(this.shell, SWT.NONE);
        ndockGroup.setText("入渠");
        ndockGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout glNdockGroup = new GridLayout(2, true);
        glNdockGroup.verticalSpacing = 2;
        glNdockGroup.marginWidth = 2;
        glNdockGroup.marginHeight = 2;
        glNdockGroup.horizontalSpacing = 2;
        ndockGroup.setLayout(glNdockGroup);

        Button ndockNotice = new Button(ndockGroup, SWT.CHECK);
        ndockNotice.setSelection(true);
        ndockNotice.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        ndockNotice.setText("お風呂から上がる1分前になったら通知する");

        Label ndock1name = new Label(ndockGroup, SWT.NONE);
        ndock1name.setText("ドッグ1に浸かっている艦娘の名前");
        ndock1name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text ndock1time = new Text(ndockGroup, SWT.SINGLE | SWT.BORDER);
        ndock1time.setText("お風呂から上がる時間");
        ndock1time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label ndock2name = new Label(ndockGroup, SWT.NONE);
        ndock2name.setText("ドッグ2に浸かっている艦娘の名前");
        ndock2name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text ndock2time = new Text(ndockGroup, SWT.SINGLE | SWT.BORDER);
        ndock2time.setText("お風呂から上がる時間");
        ndock2time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label ndock3name = new Label(ndockGroup, SWT.NONE);
        ndock3name.setText("ドッグ3に浸かっている艦娘の名前");
        ndock3name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text ndock3time = new Text(ndockGroup, SWT.SINGLE | SWT.BORDER);
        ndock3time.setText("お風呂から上がる時間");
        ndock3time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label ndock4name = new Label(ndockGroup, SWT.NONE);
        ndock4name.setText("ドッグ4に浸かっている艦娘の名前");
        ndock4name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Text ndock4time = new Text(ndockGroup, SWT.SINGLE | SWT.BORDER);
        ndock4time.setText("お風呂から上がる時間");
        ndock4time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // コンソール
        Composite consolec = new Composite(this.shell, SWT.NONE);
        GridLayout loglayout = new GridLayout(1, false);
        loglayout.verticalSpacing = 2;
        loglayout.marginWidth = 2;
        loglayout.marginHeight = 2;
        loglayout.horizontalSpacing = 2;
        consolec.setLayout(loglayout);
        consolec.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

        org.eclipse.swt.widgets.List console = new org.eclipse.swt.widgets.List(consolec, SWT.BORDER | SWT.V_SCROLL);
        console.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

        // 非同期で画面を更新するスレッド
        Thread asyncExecUpdateThread = new AsyncExecApplicationMain(display, this.shell, item, itemList, shipList,
                deckNotice, deck1name, deck1time, deck2name, deck2time, deck3name, deck3time, ndockNotice, ndock1name,
                ndock1time, ndock2name, ndock2time, ndock3name, ndock3time, ndock4name, ndock4time);
        asyncExecUpdateThread.setDaemon(true);
        asyncExecUpdateThread.start();
        // 非同期でログを出すスレッド
        Thread asyncExecConsoleThread = new AsyncExecApplicationMainConsole(display, console);
        asyncExecConsoleThread.setDaemon(true);
        asyncExecConsoleThread.start();
        // サウンドを出すスレッド
        Thread player = new Sound.PlayerThread();
        player.setDaemon(true);
        player.start();

    }
}
