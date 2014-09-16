package logbook.gui;

import logbook.config.AppConfig;
import logbook.config.ItemConfig;
import logbook.config.ItemMasterConfig;
import logbook.config.MasterDataConfig;
import logbook.config.ShipConfig;
import logbook.config.ShipGroupConfig;
import logbook.config.bean.WindowConfigBean;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.gui.background.AsyncExecApplicationMain;
import logbook.gui.background.AsyncExecUpdateCheck;
import logbook.gui.listener.HelpEventListener;
import logbook.gui.listener.MainShellAdapter;
import logbook.gui.listener.TraySelectionListener;
import logbook.gui.logic.LayoutLogic;
import logbook.gui.logic.MainAppListener;
import logbook.gui.logic.Sound;
import logbook.gui.widgets.FleetComposite;
import logbook.internal.EnemyData;
import logbook.server.proxy.DatabaseClient;
import logbook.server.proxy.ProxyServer;
import logbook.server.web.WebServer;
import logbook.thread.ThreadManager;
import logbook.thread.ThreadStateObserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * メイン画面
 *
 */
public final class ApplicationMain extends WindowBase {

    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(ApplicationMain.class);

    /**
     * <p>
     * 終了処理を行います
     * </p>
     */
    private static final class ShutdownHookThread implements Runnable {

        /** ロガー */
        private static final Logger LOG = LogManager.getLogger(ShutdownHookThread.class);

        @Override
        public void run() {
            try {
                // リソースを開放する
                SWTResourceManager.dispose();
                // プロキシサーバーをシャットダウンする
                ProxyServer.end();
                WebServer.end();
                DatabaseClient.end();

                // 設定を書き込みます
                AppConfig.store();
                ShipConfig.store();
                ShipGroupConfig.store();
                ItemMasterConfig.store();
                ItemConfig.store();
                MasterDataConfig.store();
                EnemyData.store();
            } catch (Exception e) {
                LOG.fatal("シャットダウンスレッドで異常終了しました", e);
            }
        }
    }

    /** ベースクラスの持っているshellと同じ */
    private Shell shell;
    /** 表示しない親ウィンドウ */
    private Shell dummyHolder;

    /** トレイ */
    private TrayItem trayItem;

    /** ウィンドウたち */
    private CaptureDialog captureWindow;
    /** ドロップ報告書 */
    private DropReportTable dropReportWindow;
    /** 建造報告書 */
    private CreateShipReportTable createShipReportWindow;
    /** 開発報告書 */
    private CreateItemReportTable createItemReportWindow;
    /** 遠征報告書 */
    private MissionResultTable missionResultWindow;
    /** 所有装備一覧 */
    private ItemTable itemTableWindow;
    /** 艦娘一覧1-4 */
    private final ShipTable[] shipTableWindows = new ShipTable[4];
    /** お風呂に入りたい艦娘 */
    private BathwaterTableDialog bathwaterTablwWindow;
    /** 任務一覧 */
    private QuestTable questTableWindow;
    /** 戦況 */
    private BattleWindow battleWindow;
    /** 自軍敵軍パラメータ */
    private BattleShipWindow battleShipWindow;
    /** 経験値計算 */
    private CalcExpDialog calcExpWindow;
    /** グループエディター */
    private ShipFilterGroupDialog shipFilterGroupWindow;

    /** コマンドボタン */
    private Composite commandComposite;
    /** 縮小表示メニューアイテム */
    private MenuItem dispsize;
    /** 所有装備 */
    private Button itemList;
    /** 所有艦娘 */
    private Button shipList;
    /** タブ */
    private CTabFolder tabFolder;
    /** メインコンポジット */
    private Composite mainComposite;
    /** 遠征グループ */
    private Group deckGroup;
    /** 1分前に通知する(遠征) */
    private Button deckNotice;
    /** 遠征.艦隊2の艦隊名 */
    private Label deck1name;
    /** 遠征.艦隊2の帰投時間 */
    private Text deck1time;
    /** 遠征.艦隊3の艦隊名 */
    private Label deck2name;
    /** 遠征.艦隊3の帰投時間 */
    private Text deck2time;
    /** 遠征.艦隊4の艦隊名 */
    private Label deck3name;
    /** 遠征.艦隊4の帰投時間 */
    private Text deck3time;
    /** 入渠グループ **/
    private Group ndockGroup;
    /** 1分前に通知する(入渠) */
    private Button ndockNotice;
    /** 入渠.ドッグ1.艦娘の名前 **/
    private Label ndock1name;
    /** 入渠.ドッグ1.お風呂から上がる時間 **/
    private Text ndock1time;
    /** 入渠.ドッグ2.艦娘の名前 **/
    private Label ndock2name;
    /** 入渠.ドッグ2.お風呂から上がる時間 **/
    private Text ndock2time;
    /** 入渠.ドッグ3.艦娘の名前 **/
    private Label ndock3name;
    /** 入渠.ドッグ3.お風呂から上がる時間 **/
    private Text ndock3time;
    /** 入渠.ドッグ4.艦娘の名前 **/
    private Label ndock4name;
    /** 入渠.ドッグ4.お風呂から上がる時間 **/
    private Text ndock4time;
    /** コンソールコンポジット **/
    private Composite consoleComposite;
    /** コンソール **/
    private List console;

    /**
     * Launch the application.
     * @param args
     */
    public static void main(String[] args) {
        try {
            // 設定読み込み
            AppConfig.load();
            ShipConfig.load();
            MasterDataConfig.load();
            ShipGroupConfig.load();
            ItemMasterConfig.load();
            ItemConfig.load();
            EnemyData.load();
            // シャットダウンフックを登録します
            Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookThread()));
            // アプリケーション開始
            ApplicationMain window = new ApplicationMain();
            window.restore();
        } catch (Error e) {
            LOG.fatal("メインスレッドが異常終了しました", e);
        } catch (Exception e) {
            LOG.fatal("メインスレッドが異常終了しました", e);
        }
        // 
        new Thread(new ShutdownHookThread()).start();
    }

    /**
     * Open the window.
     */
    @Override
    public void open() {
        Display display = Display.getDefault();
        this.createContents();
        this.registerEvents();
        this.restoreWindows();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        this.dummyHolder.dispose();
        this.trayItem.dispose();
    }

    /**
     * 画面レイアウトを作成します
     */
    public void createContents() {
        final Display display = Display.getDefault();
        int shellStyle = 0;
        if (AppConfig.get().isOnTop()) {
            shellStyle |= SWT.ON_TOP;
        }
        this.dummyHolder = new Shell(display, shellStyle | SWT.TOOL);
        super.createContents(display, shellStyle | SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE, false);
        this.shell = this.getShell();
        this.shell.setText(AppConstants.TITLEBAR_TEXT);
        this.shell.setAlpha(AppConfig.get().getAlpha());
        GridLayout glShell = new GridLayout(1, false);
        glShell.horizontalSpacing = 1;
        glShell.marginTop = 0;
        glShell.marginWidth = 0;
        glShell.marginHeight = 0;
        glShell.marginBottom = 0;
        glShell.verticalSpacing = 1;
        this.shell.setLayout(glShell);
        this.shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                // 終了の確認でウインドウ位置を記憶
                ApplicationMain.this.saveWindows();

                if (AppConfig.get().isCheckDoit()) {
                    MessageBox box = new MessageBox(ApplicationMain.this.shell, SWT.YES | SWT.NO
                            | SWT.ICON_QUESTION);
                    box.setText("終了の確認");
                    box.setMessage("航海日誌を終了しますか？");
                    if (box.open() == SWT.YES) {
                        e.doit = true;
                    } else {
                        e.doit = false;
                    }
                }
            }
        });

        // メニューバー
        Menu menubar = new Menu(this.shell, SWT.BAR);
        this.shell.setMenuBar(menubar);
        MenuItem cmdmenuroot = new MenuItem(menubar, SWT.CASCADE);
        cmdmenuroot.setText("コマンド");
        Menu cmdmenu = new Menu(cmdmenuroot);
        cmdmenuroot.setMenu(cmdmenu);
        MenuItem calcmenuroot = new MenuItem(menubar, SWT.CASCADE);
        calcmenuroot.setText("計算機");
        Menu calcmenu = new Menu(calcmenuroot);
        calcmenuroot.setMenu(calcmenu);
        MenuItem etcroot = new MenuItem(menubar, SWT.CASCADE);
        etcroot.setText("その他");
        Menu etcmenu = new Menu(etcroot);
        etcroot.setMenu(etcmenu);

        // メニュー
        // コマンド-キャプチャ
        MenuItem capture = new MenuItem(cmdmenu, SWT.CHECK);
        capture.setText("キャプチャ(&C)");
        this.captureWindow = new CaptureDialog(this.dummyHolder, capture);
        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);
        // コマンド-ドロップ報告書
        MenuItem cmddrop = new MenuItem(cmdmenu, SWT.CHECK);
        cmddrop.setText("ドロップ報告書(&D)\tCtrl+D");
        cmddrop.setAccelerator(SWT.CTRL + 'D');
        this.dropReportWindow = new DropReportTable(this.dummyHolder, cmddrop);
        // コマンド-建造報告書
        MenuItem cmdcreateship = new MenuItem(cmdmenu, SWT.CHECK);
        cmdcreateship.setText("建造報告書(&B)\tCtrl+B");
        cmdcreateship.setAccelerator(SWT.CTRL + 'B');
        this.createShipReportWindow = new CreateShipReportTable(this.dummyHolder, cmdcreateship);
        // コマンド-開発報告書
        MenuItem cmdcreateitem = new MenuItem(cmdmenu, SWT.CHECK);
        cmdcreateitem.setText("開発報告書(&E)\tCtrl+E");
        cmdcreateitem.setAccelerator(SWT.CTRL + 'E');
        this.createItemReportWindow = new CreateItemReportTable(this.dummyHolder, cmdcreateitem);
        // コマンド-遠征報告書
        MenuItem cmdmissionresult = new MenuItem(cmdmenu, SWT.CHECK);
        cmdmissionresult.setText("遠征報告書(&T)\tCtrl+T");
        cmdmissionresult.setAccelerator(SWT.CTRL + 'T');
        this.missionResultWindow = new MissionResultTable(this.dummyHolder, cmdmissionresult);
        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);
        // コマンド-所有装備一覧
        MenuItem cmditemlist = new MenuItem(cmdmenu, SWT.CHECK);
        cmditemlist.setText("所有装備一覧(&I)\tCtrl+I");
        cmditemlist.setAccelerator(SWT.CTRL + 'I');
        this.itemTableWindow = new ItemTable(this.dummyHolder, cmditemlist);
        // コマンド-所有艦娘一覧
        for (int i = 0; i < 4; ++i) {
            MenuItem cmdshiplist = new MenuItem(cmdmenu, SWT.CHECK);
            cmdshiplist.setAccelerator(SWT.CTRL + ('1' + i));
            this.shipTableWindows[i] = new ShipTable(this.dummyHolder, cmdshiplist, i);
        }

        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);
        // コマンド-お風呂に入りたい艦娘
        MenuItem cmdbathwaterlist = new MenuItem(cmdmenu, SWT.CHECK);
        cmdbathwaterlist.setText("お風呂に入りたい艦娘(&N)\tCtrl+N");
        cmdbathwaterlist.setAccelerator(SWT.CTRL + 'N');
        this.bathwaterTablwWindow = new BathwaterTableDialog(this.dummyHolder, cmdbathwaterlist);
        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);
        // コマンド-任務一覧
        MenuItem questlist = new MenuItem(cmdmenu, SWT.CHECK);
        questlist.setText("任務一覧(&Q)\tCtrl+Q");
        questlist.setAccelerator(SWT.CTRL + 'Q');
        this.questTableWindow = new QuestTable(this.dummyHolder, questlist);
        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);

        // 表示-戦況ウィンドウ 
        MenuItem battleWinMenu = new MenuItem(cmdmenu, SWT.CHECK);
        battleWinMenu.setText("戦況(&W)\tCtrl+W");
        battleWinMenu.setAccelerator(SWT.CTRL + 'W');
        this.battleWindow = new BattleWindow(this.dummyHolder, battleWinMenu);

        // 表示-敵味方パラメータ
        MenuItem battleShipWinMenu = new MenuItem(cmdmenu, SWT.CHECK);
        battleShipWinMenu.setText("自軍敵軍パラメータ(&P)\tCtrl+P");
        battleShipWinMenu.setAccelerator(SWT.CTRL + 'P');
        this.battleShipWindow = new BattleShipWindow(this.dummyHolder, battleShipWinMenu);

        // 表示-縮小表示
        // ウィンドウの右クリックメニューに追加
        new MenuItem(this.getMenu(), SWT.SEPARATOR);
        this.dispsize = new MenuItem(this.getMenu(), SWT.CHECK);
        this.dispsize.setText("縮小表示(&M)\tCtrl+M");
        this.dispsize.setAccelerator(SWT.CTRL + 'M');
        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);
        // 終了
        final MenuItem dispose = new MenuItem(cmdmenu, SWT.NONE);
        dispose.setText("終了(&X)");
        dispose.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ApplicationMain.this.shell.close();
            }
        });

        // 計算機-経験値計算
        MenuItem calcexp = new MenuItem(calcmenu, SWT.CHECK);
        calcexp.setText("経験値計算機(&C)\tCtrl+C");
        calcexp.setAccelerator(SWT.CTRL + 'C');
        this.calcExpWindow = new CalcExpDialog(this.dummyHolder, calcexp);

        // その他-グループエディター
        MenuItem shipgroup = new MenuItem(etcmenu, SWT.CHECK);
        shipgroup.setText("グループエディター(&G)");
        this.shipFilterGroupWindow = new ShipFilterGroupDialog(this.dummyHolder, shipgroup);
        // その他-資材チャート
        MenuItem resourceChart = new MenuItem(etcmenu, SWT.NONE);
        resourceChart.setText("資材チャート(&R)");
        resourceChart.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ResourceChartDialog(ApplicationMain.this.dummyHolder).open();
            }
        });
        // その他-自動プロキシ構成スクリプトファイル生成
        MenuItem pack = new MenuItem(etcmenu, SWT.NONE);
        pack.setText("自動プロキシ構成スクリプト");
        pack.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new CreatePacFileDialog(ApplicationMain.this.dummyHolder).open();
            }
        });
        // セパレータ
        new MenuItem(etcmenu, SWT.SEPARATOR);
        // その他-ウィンドウをディスプレイ内に移動
        MenuItem movewindows = new MenuItem(etcmenu, SWT.NONE);
        movewindows.setText("ウィンドウを呼び戻す");
        movewindows.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ApplicationMain.this.moveWindowsIntoDisplay();
            }
        });
        // セパレータ
        new MenuItem(etcmenu, SWT.SEPARATOR);
        // その他-設定
        MenuItem config = new MenuItem(etcmenu, SWT.NONE);
        config.setText("設定(&P)");
        config.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ConfigDialog(ApplicationMain.this).open();
            }
        });
        // その他-バージョン情報
        MenuItem version = new MenuItem(etcmenu, SWT.NONE);
        version.setText("バージョン情報(&A)");
        version.addSelectionListener(new HelpEventListener(this.shell));

        // シェルイベント
        this.shell.addShellListener(new MainShellAdapter());
        // キーが押された時に呼ばれるリスナーを追加します
        this.shell.addHelpListener(new HelpEventListener(this.shell));

        this.trayItem = this.addTrayItem(display);

        // コマンドボタン
        this.commandComposite = new Composite(this.shell, SWT.NONE);
        this.commandComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
        this.commandComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.itemList = new Button(this.commandComposite, SWT.PUSH);
        this.itemList.setText("所有装備(0/0)");
        this.itemList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ApplicationMain.this.itemTableWindow.open();
                ApplicationMain.this.itemTableWindow.getShell().setActive();
            }
        });
        this.shipList = new Button(this.commandComposite, SWT.PUSH);
        this.shipList.setText("所有艦娘(0/0)");
        this.shipList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ApplicationMain.this.shipTableWindows[0].open();
                ApplicationMain.this.shipTableWindows[0].getShell().setActive();
            }
        });

        // タブフォルダー
        this.tabFolder = new CTabFolder(this.shell, SWT.NONE);
        this.tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
        this.tabFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
        this.tabFolder.setTabHeight(26);
        this.tabFolder.marginWidth = 0;
        this.tabFolder.setMinimumCharacters(2);

        // 母港タブ
        CTabItem mainItem = new CTabItem(this.tabFolder, SWT.NONE);
        mainItem.setFont(SWTResourceManager.getBoldFont(this.shell.getFont()));
        this.tabFolder.setSelection(mainItem);
        mainItem.setText("母港");

        // メインコンポジット
        this.mainComposite = new Composite(this.tabFolder, SWT.NONE);
        GridLayout glMain = new GridLayout(1, false);
        glMain.horizontalSpacing = 1;
        glMain.marginTop = 0;
        glMain.marginWidth = 0;
        glMain.marginHeight = 0;
        glMain.marginBottom = 0;
        glMain.verticalSpacing = 1;
        this.mainComposite.setLayout(glMain);
        this.mainComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mainItem.setControl(this.mainComposite);

        // 遠征
        this.deckGroup = new Group(this.mainComposite, SWT.NONE);
        this.deckGroup.setText("遠征");
        this.deckGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout glDeckGroup = new GridLayout(2, false);
        glDeckGroup.verticalSpacing = 1;
        glDeckGroup.marginTop = 0;
        glDeckGroup.marginWidth = 0;
        glDeckGroup.marginHeight = 0;
        glDeckGroup.marginBottom = 0;
        glDeckGroup.horizontalSpacing = 1;
        this.deckGroup.setLayout(glDeckGroup);

        this.deckNotice = new Button(this.deckGroup, SWT.CHECK);
        this.deckNotice.setSelection(AppConfig.get().isNoticeDeckmission());
        this.deckNotice.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        this.deckNotice.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setNoticeDeckmission(ApplicationMain.this.deckNotice.getSelection());
            }
        });
        this.deckNotice.setText("1分前に通知する");

        this.deck1name = new Label(this.deckGroup, SWT.NONE);
        this.deck1name.setText("ここに艦隊2の艦隊名が入ります");
        this.deck1name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.deck1time = new Text(this.deckGroup, SWT.SINGLE | SWT.BORDER);
        this.deck1time.setText("艦隊2の帰投時間");
        GridData gddeck1time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gddeck1time.widthHint = 75;
        this.deck1time.setLayoutData(gddeck1time);

        this.deck2name = new Label(this.deckGroup, SWT.NONE);
        this.deck2name.setText("ここに艦隊3の艦隊名が入ります");
        this.deck2name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.deck2time = new Text(this.deckGroup, SWT.SINGLE | SWT.BORDER);
        this.deck2time.setText("艦隊3の帰投時間");
        GridData gddeck2time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gddeck2time.widthHint = 75;
        this.deck2time.setLayoutData(gddeck2time);

        this.deck3name = new Label(this.deckGroup, SWT.NONE);
        this.deck3name.setText("ここに艦隊4の艦隊名が入ります");
        this.deck3name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.deck3time = new Text(this.deckGroup, SWT.SINGLE | SWT.BORDER);
        this.deck3time.setText("艦隊4の帰投時間");
        GridData gddeck3time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gddeck3time.widthHint = 75;
        this.deck3time.setLayoutData(gddeck3time);

        // 入渠
        this.ndockGroup = new Group(this.mainComposite, SWT.NONE);
        this.ndockGroup.setText("入渠");
        this.ndockGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout glNdockGroup = new GridLayout(2, false);
        glNdockGroup.verticalSpacing = 1;
        glNdockGroup.marginTop = 0;
        glNdockGroup.marginWidth = 0;
        glNdockGroup.marginHeight = 0;
        glNdockGroup.marginBottom = 0;
        glNdockGroup.horizontalSpacing = 1;
        this.ndockGroup.setLayout(glNdockGroup);

        this.ndockNotice = new Button(this.ndockGroup, SWT.CHECK);
        this.ndockNotice.setSelection(AppConfig.get().isNoticeNdock());
        this.ndockNotice.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        this.ndockNotice.setText("1分前に通知する");
        this.ndockNotice.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setNoticeNdock(ApplicationMain.this.ndockNotice.getSelection());
            }
        });

        this.ndock1name = new Label(this.ndockGroup, SWT.NONE);
        this.ndock1name.setText("ドッグ1に浸かっている艦娘の名前");
        this.ndock1name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.ndock1time = new Text(this.ndockGroup, SWT.SINGLE | SWT.BORDER);
        this.ndock1time.setText("お風呂から上がる時間");
        GridData gdndock1time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdndock1time.widthHint = 75;
        this.ndock1time.setLayoutData(gdndock1time);

        this.ndock2name = new Label(this.ndockGroup, SWT.NONE);
        this.ndock2name.setText("ドッグ2に浸かっている艦娘の名前");
        this.ndock2name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.ndock2time = new Text(this.ndockGroup, SWT.SINGLE | SWT.BORDER);
        this.ndock2time.setText("お風呂から上がる時間");
        GridData gdndock2time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdndock2time.widthHint = 75;
        this.ndock2time.setLayoutData(gdndock2time);

        this.ndock3name = new Label(this.ndockGroup, SWT.NONE);
        this.ndock3name.setText("ドッグ3に浸かっている艦娘の名前");
        this.ndock3name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.ndock3time = new Text(this.ndockGroup, SWT.SINGLE | SWT.BORDER);
        this.ndock3time.setText("お風呂から上がる時間");
        GridData gdndock3time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdndock3time.widthHint = 75;
        this.ndock3time.setLayoutData(gdndock3time);

        this.ndock4name = new Label(this.ndockGroup, SWT.NONE);
        this.ndock4name.setText("ドッグ4に浸かっている艦娘の名前");
        this.ndock4name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.ndock4time = new Text(this.ndockGroup, SWT.SINGLE | SWT.BORDER);
        this.ndock4time.setText("お風呂から上がる時間");
        GridData gdndock4time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdndock4time.widthHint = 75;
        this.ndock4time.setLayoutData(gdndock4time);

        // コンソール
        this.consoleComposite = new Composite(this.mainComposite, SWT.NONE);
        GridLayout loglayout = new GridLayout(1, false);
        loglayout.verticalSpacing = 1;
        loglayout.marginTop = 0;
        loglayout.marginWidth = 0;
        loglayout.marginHeight = 0;
        loglayout.marginBottom = 0;
        loglayout.horizontalSpacing = 1;
        this.consoleComposite.setLayout(loglayout);
        this.consoleComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

        this.console = new List(this.consoleComposite, SWT.BORDER | SWT.V_SCROLL);
        this.console.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

        // 初期設定 縮小表示が有効なら縮小表示にする
        if (AppConfig.get().isMinimumLayout()) {
            this.shell.setRedraw(false);
            ApplicationMain.this.hide(true, this.commandComposite, this.deckNotice, this.deck1name, this.deck2name,
                    this.deck3name, this.ndockNotice, this.ndock1name, this.ndock2name, this.ndock3name,
                    this.ndock4name, this.consoleComposite);
            this.dispsize.setSelection(true);
            this.shell.pack();
            this.shell.setRedraw(true);
        }

        // 縮小表示チェック時の動作
        this.dispsize.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = ApplicationMain.this.shell;
                shell.setRedraw(false);
                boolean minimum = ApplicationMain.this.dispsize.getSelection();
                // コントロールを隠す
                ApplicationMain.this.hide(minimum, ApplicationMain.this.commandComposite,
                        ApplicationMain.this.deckNotice, ApplicationMain.this.deck1name,
                        ApplicationMain.this.deck2name, ApplicationMain.this.deck3name,
                        ApplicationMain.this.ndockNotice, ApplicationMain.this.ndock1name,
                        ApplicationMain.this.ndock2name, ApplicationMain.this.ndock3name,
                        ApplicationMain.this.ndock4name, ApplicationMain.this.consoleComposite);

                // タブを整理する
                ApplicationMain.this.tabFolder.setSelection(0);

                shell.setRedraw(true);
                // ウインドウサイズを調節
                if (minimum) {
                    ApplicationMain.this.tabFolder.setSingle(true);
                    CTabItem[] tabitems = ApplicationMain.this.tabFolder.getItems();
                    for (CTabItem tabitem : tabitems) {
                        Control control = tabitem.getControl();
                        if (control instanceof FleetComposite) {
                            ApplicationMain.this.hide(true, control);
                        }
                    }

                    shell.pack();

                    ApplicationMain.this.tabFolder.setSingle(false);
                    for (CTabItem tabitem : tabitems) {
                        Control control = tabitem.getControl();
                        if (control instanceof FleetComposite) {
                            ApplicationMain.this.hide(false, control);
                        }
                    }
                } else {
                    WindowConfigBean config = ApplicationMain.this.getWindowConfig();
                    shell.setSize(config.getWidth(), config.getHeight());
                }
                // 設定を保存
                AppConfig.get().setMinimumLayout(minimum);
            }
        });

        this.configUpdated();

        this.startThread();
    }

    @Override
    public void restore() {
        // メインウィンドウは常に開く
        this.getWindowConfig().setOpened(true);
        super.restore();
    }

    /** ウィンドウ配置を復元 */
    private void restoreWindows() {
        // まずはメインウィンドウを表示する
        this.setVisible(true);
        for (WindowBase window : this.getWindowList()) {
            window.restore();
        }
    }

    private void saveWindows() {
        this.save();
        for (WindowBase window : this.getWindowList()) {
            window.save();
        }
    }

    private void moveWindowsIntoDisplay() {
        for (WindowBase window : this.getWindowList()) {
            window.moveIntoDisplay();
        }
    }

    private WindowBase[] getWindowList() {
        return new WindowBase[] {
                this.dropReportWindow,
                this.createShipReportWindow,
                this.createItemReportWindow,
                this.missionResultWindow,
                this.itemTableWindow,
                this.shipTableWindows[0],
                this.shipTableWindows[1],
                this.shipTableWindows[2],
                this.shipTableWindows[3],
                this.bathwaterTablwWindow,
                this.questTableWindow,
                this.battleWindow,
                this.battleShipWindow,
                this.calcExpWindow,
                this.shipFilterGroupWindow,
        };
    }

    /**
     * トレイアイコンを追加します
     * 
     * @param display
     * @return
     */
    private TrayItem addTrayItem(final Display display) {
        // トレイアイコンを追加します
        Tray tray = display.getSystemTray();
        TrayItem item = new TrayItem(tray, SWT.NONE);
        Image image = display.getSystemImage(SWT.ICON_INFORMATION);
        item.setImage(image);
        item.addListener(SWT.Selection, new TraySelectionListener(this.shell));
        return item;
    }

    /**
     * 縮小表示と通常表示とを切り替えます
     * 
     * @param minimum
     * @param controls 隠すコントロール
     */
    private void hide(boolean minimum, Control... controls) {
        for (Control control : controls) {
            LayoutLogic.hide(control, minimum);
        }
        this.shell.layout();
    }

    /**
     * スレッドを開始します
     */
    private void startThread() {
        // ログ表示リスナをセット
        GlobalContext.setConsoleListener(new MainAppListener(this));

        // プロキシサーバーを開始する
        ProxyServer.start(AppConfig.get().getListenPort());

        // Webサーバーを開始する
        WebServer.start(AppConfig.get().getListenPort() + 1);

        // 非同期で画面を更新するスレッド
        ThreadManager.regist(new AsyncExecApplicationMain(this));
        // サウンドを出すスレッド
        ThreadManager.regist(new Sound.PlayerThread());
        // スレッドを監視するスレッド
        ThreadManager.regist(new ThreadStateObserver(this.shell));

        ThreadManager.start();

        // アップデートチェックする
        if (AppConfig.get().isCheckUpdate()) {
            new AsyncExecUpdateCheck(this.shell).start();
        }
    }

    @Override
    protected boolean shouldSaveWindowSize() {
        return !AppConfig.get().isMinimumLayout();
    }

    @Override
    protected Point getDefaultSize() {
        return new Point(280, 420);
    }

    /**
     * 設定が更新された
     */
    public void configUpdated() {
        String[] shipNames = AppConfig.get().getShipTableNames();
        for (int i = 0; i < shipNames.length; ++i) {
            int number = i + 1;
            if ((shipNames[i] == null) || (shipNames[i].length() == 0)) {
                shipNames[i] = "艦娘一覧 " + number;
            }
            this.shipTableWindows[i].getMenuItem().setText(shipNames[i] + "(&" + number + ")\tCtrl+" + number);
            this.shipTableWindows[i].getShell().setText(this.shipTableWindows[i].getTitle());
        }
    }

    /**
     * @return トレイアイコン
     */
    public TrayItem getTrayItem() {
        return this.trayItem;
    }

    /**
     * @return コマンドボタン
     */
    public Composite getCommandComposite() {
        return this.commandComposite;
    }

    /**
     * @return 所有装備
     */
    public Button getItemList() {
        return this.itemList;
    }

    /**
     * @return 所有艦娘
     */
    public Button getShipList() {
        return this.shipList;
    }

    /**
     * @return タブフォルダ
     */
    public CTabFolder getTabFolder() {
        return this.tabFolder;
    }

    /**
     * @return メインコンポジット
     */
    public Composite getMainComposite() {
        return this.mainComposite;
    }

    /**
     * @return 遠征グループ
     */
    public Group getDeckGroup() {
        return this.deckGroup;
    }

    /**
     * @return 1分前に通知する(遠征)
     */
    public Button getDeckNotice() {
        return this.deckNotice;
    }

    /**
     * @return 遠征.艦隊2の艦隊名
     */
    public Label getDeck1name() {
        return this.deck1name;
    }

    /**
     * @return 遠征.艦隊2の帰投時間
     */
    public Text getDeck1time() {
        return this.deck1time;
    }

    /**
     * @return 遠征.艦隊3の艦隊名
     */
    public Label getDeck2name() {
        return this.deck2name;
    }

    /**
     * @return 遠征.艦隊3の帰投時間
     */
    public Text getDeck2time() {
        return this.deck2time;
    }

    /**
     * @return 遠征.艦隊4の艦隊名
     */
    public Label getDeck3name() {
        return this.deck3name;
    }

    /**
     * @return 遠征.艦隊4の帰投時間
     */
    public Text getDeck3time() {
        return this.deck3time;
    }

    /**
     * @return 入渠グループ
     */
    public Group getNdockGroup() {
        return this.ndockGroup;
    }

    /**
     * @return 1分前に通知する(入渠)
     */
    public Button getNdockNotice() {
        return this.ndockNotice;
    }

    /**
     * @return 入渠.ドッグ1.艦娘の名前
     */
    public Label getNdock1name() {
        return this.ndock1name;
    }

    /**
     * @return 入渠.ドッグ1.お風呂から上がる時間
     */
    public Text getNdock1time() {
        return this.ndock1time;
    }

    /**
     * @return 入渠.ドッグ2.艦娘の名前
     */
    public Label getNdock2name() {
        return this.ndock2name;
    }

    /**
     * @return 入渠.ドッグ2.お風呂から上がる時間
     */
    public Text getNdock2time() {
        return this.ndock2time;
    }

    /**
     * @return 入渠.ドッグ3.艦娘の名前
     */
    public Label getNdock3name() {
        return this.ndock3name;
    }

    /**
     * @return 入渠.ドッグ3.お風呂から上がる時間
     */
    public Text getNdock3time() {
        return this.ndock3time;
    }

    /**
     * @return 入渠.ドッグ4.艦娘の名前
     */
    public Label getNdock4name() {
        return this.ndock4name;
    }

    /**
     * @return 入渠.ドッグ4.お風呂から上がる時間
     */
    public Text getNdock4time() {
        return this.ndock4time;
    }

    /**
     * @return コンソールコンポジット
     */
    public Composite getConsoleComposite() {
        return this.consoleComposite;
    }

    public BattleWindow getBattleWindow() {
        return this.battleWindow;
    }

    public BattleShipWindow getBattleShipWindow() {
        return this.battleShipWindow;
    }

    /**
     * @return コンソール
     */
    public List getConsole() {
        return this.console;
    }
}