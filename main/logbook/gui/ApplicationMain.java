package logbook.gui;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import logbook.config.AppConfig;
import logbook.config.ShipGroupConfig;
import logbook.config.UserDataConfig;
import logbook.config.bean.AppConfigBean;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.*;
import logbook.gui.background.AsyncExecApplicationMain;
import logbook.gui.background.AsyncExecUpdateCheck;
import logbook.gui.background.BackgroundInitializer;
import logbook.gui.listener.HelpEventListener;
import logbook.gui.listener.MainShellAdapter;
import logbook.gui.listener.TrayItemMenuListener;
import logbook.gui.listener.TraySelectionListener;
import logbook.gui.logic.ColorManager;
import logbook.gui.logic.DeckBuilder;
import logbook.gui.logic.FleetFormatter;
import logbook.gui.logic.ItemFormatter;
import logbook.gui.logic.LayoutLogic;
import logbook.gui.logic.PushNotify;
import logbook.gui.logic.Sound;
import logbook.gui.widgets.FleetComposite;
import logbook.internal.*;
import logbook.internal.Item;
import logbook.scripting.ScriptData;
import logbook.server.proxy.DatabaseClient;
import logbook.server.proxy.ProxyServer;
import logbook.thread.ThreadManager;
import logbook.thread.ThreadStateObserver;
import logbook.util.JIntellitypeWrapper;
import logbook.util.SwtUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.wb.swt.SWTResourceManager;

import com.melloware.jintellitype.HotkeyListener;

/**
 * メイン画面
 *
 */
public final class ApplicationMain extends WindowBase {

    private static final long startTime = System.currentTimeMillis();

    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(ApplicationMain.class);

    private static final int MAX_LOG_LINES = 200;
    private static final DateFormat LOG_DATE_FORMAT = new SimpleDateFormat(AppConstants.DATE_SHORT_FORMAT);

    private static final LoggerHolder userLogger = new LoggerHolder("user");

    public static void sysPrint(String mes) {
        System.out.println(mes + ": " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public static void timeLogPrint(String mes) {
        logPrint(mes + ": " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public static void logPrint(final String mes) {
        if (main.display.getThread() == Thread.currentThread()) {
            main.printMessage(mes);
        }
        else {
            main.display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        main.printMessage(mes);
                    }
                    catch (Exception e) {
                        LOG.get().warn("ログの追加に失敗", e);
                    }
                }
            });
        }
    }

    public static ApplicationMain main;
    public static boolean disableUpdate;
    private static ApplicationLock applicationLock = new ApplicationLock();

    private static final class ApplicationLock {
        private FileOutputStream fos;
        private FileChannel fchan;
        private FileLock flock;
        private boolean isError;

        public ApplicationLock() {
            try {
                File dir = AppConstants.LOCK_FILE.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                this.fos = new FileOutputStream(AppConstants.LOCK_FILE);
                this.fchan = this.fos.getChannel();
                this.flock = this.fchan.tryLock();
            } catch (IOException e) {
                this.isError = true;
                LOG.get().warn("ファイルロックでエラー", e);
            }
        }

        public boolean isLocked() {
            return (this.flock != null);
        }

        public boolean isError() {
            return this.isError;
        }

        public void release() {
            try {
                if (this.flock != null) {
                    this.flock.release();
                }
                this.fchan.close();
                this.fos.close();
                if (this.flock != null) {
                    AppConstants.LOCK_FILE.delete();
                }
            } catch (IOException e) {
                LOG.get().warn("ファイルロック解放でエラー", e);
            }
        }
    }

    /**
     * <p>
     * 終了処理を行います
     * </p>
     */
    private static final class ShutdownHookThread implements Runnable {

        /** ロガー */
        private static final LoggerHolder LOG = new LoggerHolder(ShutdownHookThread.class);

        @Override
        public void run() {
            try {

                // 設定を書き込みます
                AppConfig.store();
                ShipGroupConfig.store();
                UserDataConfig.store();
                MasterData.store();
                EnemyData.store();
                ShipParameterRecord.store();
                ScriptData.store();

                // スレッドを終了する
                endThread();

                // ロック解放
                applicationLock.release();

            } catch (Exception e) {
                LOG.get().fatal("シャットダウンスレッドで異常終了しました", e);
            }
        }
    }

    /** ベースクラスの持っているshellと同じ */
    private Shell shell;
    /** 表示しない親ウィンドウ */
    private Shell subwindowHost;
    private boolean showSubwindowHost;
    /** ディスプレイ */
    private Display display;

    /** トレイ */
    private TrayItem trayItem;

    /** タイトルテキスト */
    private String titleText;

    /** キャプチャ */
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
    /** 遠征一覧 */
    private MissionTable missionTableWindow;
    /** 任務一覧 */
    private QuestTable questTableWindow;
    /** 戦況 */
    private BattleWindowLarge battleWindowLarge;
    /** 戦況-横 */
    private BattleWindowSmall battleWindowSmall;
    /** 自軍敵軍パラメータ */
    private BattleShipWindow battleShipWindow;
    /** 経験値計算 */
    private CalcExpDialog calcExpWindow;
    /** 演習経験値計算 */
    private CalcPracticeExpDialog calcPracticeExpWindow;
    /** 出撃統計 */
    private BattleAggDialog battleCounterWindow;
    /** グループエディター */
    private ShipFilterGroupDialog shipFilterGroupWindow;
    /** 資材チャート */
    private ResourceChartDialog resourceChartWindow;
    /** ツールウィンドウ */
    private LauncherWindow launcherWindow;
    /** 艦隊1-4 */
    private final FleetWindow[] fleetWindows = new FleetWindow[4];

    /** コマンドボタン */
    private Composite commandComposite;
    /** 所有装備 */
    private Button itemList;
    /** 所有艦娘 */
    private Button shipList;
    /** タブ */
    private CTabFolder tabFolder;
    /** メインコンポジット */
    private Composite mainComposite;

    private Group notifySettingGroup;
    /** 遠征通知 */
    private Button deckNotice;
    /** 入渠通知 */
    private Button ndockNotice;
    /** 泊地修理通知 */
    private Button akashiNotice;
    /** 疲労通知 */
    private Button condNotice;

    /** 遠征グループ */
    private Group deckGroup;
    /** 艦隊.艦隊1の艦隊名 */
    private Label deck1name;
    /** 艦隊.艦隊1の帰投時間 */
    private Text deck1time;
    /** 艦隊.艦隊2の艦隊名 */
    private Label deck2name;
    /** 艦隊.艦隊2の帰投時間 */
    private Text deck2time;
    /** 艦隊.艦隊3の艦隊名 */
    private Label deck3name;
    /** 艦隊.艦隊3の帰投時間 */
    private Text deck3time;
    /** 艦隊.艦隊4の艦隊名 */
    private Label deck4name;
    /** 艦隊.艦隊4の帰投時間 */
    private Text deck4time;

    /** 入渠グループ **/
    private Group ndockGroup;
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

    /** 疲労タイマー **/
    private Composite condTimerGroup;
    private Label condTimerLabel;
    private Text condTimerTime;

    /** 泊地修理タイマー **/
    private Composite akashiTimerGroup;
    private Label akashiTimerLabel;
    private Text akashiTimerTime;

    /** 戦果 **/
    private Composite resultRecordGroup;
    private Label resultRecordLabel;
    private Label admiralExpLabel;

    /** 基地航空隊 **/
    private Composite airbaseGroup;
    private Combo airbaseCombo;

    /** エラー表示 **/
    private Label errorLabel;
    /** コンソール **/
    private org.eclipse.swt.widgets.List console;

    /**
     * Launch the application.
     * @param args
     */
    public static void main(String[] args) {
        try {
            // グループ化のためのアプリケーションID (Windows 7以降)
            Display.setAppName(AppConstants.NAME);
            sysPrint("起動");
            // 多重起動チェック
            if (!applicationLock.isError() && !applicationLock.isLocked()) {
                printErrorMessageBox();
                applicationLock.release();
                return;
            }
            // 設定読み込み
            AppConfig.load();
            /*　static initializer に移行
            ShipConfig.load();
            MasterDataConfig.load();
            ShipGroupConfig.load();
            ItemMasterConfig.load();
            ItemConfig.load();
            EnemyData.load();
            */
            sysPrint("基本設定ファイル読み込み完了");
            // シャットダウンフックを登録します
            Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookThread()));
            // アプリケーション開始
            main = new ApplicationMain();
            sysPrint("メインウィンドウ初期化開始");
            main.restore();
        } catch (Error e) {
            LOG.get().fatal("メインスレッドが異常終了しました", e);
        } catch (Exception e) {
            LOG.get().fatal("メインスレッドが異常終了しました", e);
        } finally {
            endThread();
        }
    }

    private static void printErrorMessageBox() {
        Shell shell = new Shell(Display.getDefault(), SWT.TOOL);
        MessageBox mes = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
        mes.setText(AppConstants.TITLEBAR_TEXT);
        mes.setMessage("多重起動を検出しました。起動中の航海日誌拡張版を終了してください。");
        mes.open();
        shell.dispose();
    }

    /**
     * Open the window.
     */
    @Override
    public void open() {
        try {
            Display display = Display.getDefault();
            this.createContents();
            this.registerEvents();
            sysPrint("ウィンドウ表示開始...");
            this.restoreWindows();
            sysPrint("メッセージループに入ります...");
            while (!this.shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();

                }
            }
            this.subwindowHost.dispose();
        } finally {
            Tray tray = Display.getDefault().getSystemTray();
            if (tray != null) {
                for (TrayItem item : tray.getItems()) {
                    item.dispose();
                }
            }
        }
    }

    @Override
    protected boolean moveWithDrag() {
        return true;
    }

    /**
     * 画面レイアウトを作成します
     */
    public void createContents() {
        this.display = Display.getDefault();
        super.createContents(this.display, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE, true);
        this.shell = this.getShell();
        this.shell.setText(AppConstants.TITLEBAR_TEXT);

        this.showSubwindowHost = AppConfig.get().isShowSubwindowHost();
        if (this.showSubwindowHost) {
            final Shell dummyHolder = this.subwindowHost = new Shell(this.display, SWT.NONE);
            dummyHolder.setText("サブウィンドウ - 航海日誌拡張版");
            dummyHolder.setSize(SwtUtils.DPIAwareSize(new Point(150, 50)));
            dummyHolder.setLayout(SwtUtils.makeGridLayout(1, 0, 0, 0, 0));
            dummyHolder.setImage(SWTResourceManager.getImage(WindowBase.class, AppConstants.LOGO));
            dummyHolder.addShellListener(new ShellAdapter() {
                @Override
                public void shellClosed(ShellEvent e) {
                    e.doit = false;
                }
            });
            Label dummyLabel = new Label(this.subwindowHost, SWT.CENTER);
            SwtUtils.initLabel(dummyLabel, "航海日誌拡張版\nサブウィンドウ", 3, 2.4,
                    new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

            // 基本的に画面外に表示させるが何かの拍子に画面内に移動してしまったら、
            // クリックで画面外に移動する
            MouseListener dummyHolderMouseListener = new MouseAdapter() {
                @Override
                public void mouseDown(MouseEvent e) {
                    Rectangle displayRect = dummyHolder.getDisplay().getClientArea();
                    Rectangle windowRect = dummyHolder.getBounds();
                    windowRect.x = displayRect.x + displayRect.width;
                    windowRect.y = displayRect.y + displayRect.height;
                    dummyHolder.setBounds(windowRect);
                }
            };
            dummyHolderMouseListener.mouseDown(null);
            dummyLabel.addMouseListener(dummyHolderMouseListener);
        }
        else {
            this.subwindowHost = new Shell(this.display, SWT.TOOL);
        }

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
                    e.doit = box.open() == SWT.YES;
                }

                if (e.doit) {
                    // 他のウィンドウを閉じる
                    for (WindowBase win : ApplicationMain.this.getWindowList()) {
                        win.dispose();
                    }
                }
            }

            @Override
            public void shellActivated(ShellEvent event) {
                // Mainウィンドウがアクティブになった時、子ウィンドウをMainウィンドウの直後に入れる
                if (nativeService.isTopMostAvailable()) {
                    Set<WindowBase> windows = getActivatedWindows();
                    WindowBase[] windowArray = windows.toArray(new WindowBase[windows.size()]);
                    for (int i = windowArray.length - 1; i >= 0; --i) {
                        WindowBase win = windowArray[i];
                        if (win.getVisible() && (win.getActualParent() == ApplicationMain.this.subwindowHost)) {
                            windowArray[i].setBehindTo(ApplicationMain.this);
                            break;
                        }
                    }
                }
            }

            @Override
            public void shellDeiconified(ShellEvent e) {
                // Main以外のウィンドウも連動させる
                ApplicationMain.this.childDeiconified();
            }

            @Override
            public void shellIconified(ShellEvent e) {
                // Main以外のウィンドウも連動させる
                ApplicationMain.this.childIconified();
            }
        });

        // メニューバー
        this.createMenubar();
        Menu menubar = this.getMenubar();
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
        this.captureWindow = new CaptureDialog(this.subwindowHost, capture);
        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);
        // コマンド-ドロップ報告書
        MenuItem cmddrop = new MenuItem(cmdmenu, SWT.CHECK);
        cmddrop.setText("ドロップ報告書(&D)\tCtrl+D");
        cmddrop.setAccelerator(SWT.CTRL + 'D');
        this.dropReportWindow = new DropReportTable(this.subwindowHost, cmddrop);
        // コマンド-建造報告書
        MenuItem cmdcreateship = new MenuItem(cmdmenu, SWT.CHECK);
        cmdcreateship.setText("建造報告書(&Y)\tCtrl+Y");
        cmdcreateship.setAccelerator(SWT.CTRL + 'Y');
        this.createShipReportWindow = new CreateShipReportTable(this.subwindowHost, cmdcreateship);
        // コマンド-開発報告書
        MenuItem cmdcreateitem = new MenuItem(cmdmenu, SWT.CHECK);
        cmdcreateitem.setText("開発報告書(&E)\tCtrl+E");
        cmdcreateitem.setAccelerator(SWT.CTRL + 'E');
        this.createItemReportWindow = new CreateItemReportTable(this.subwindowHost, cmdcreateitem);
        // コマンド-遠征報告書
        MenuItem cmdmissionresult = new MenuItem(cmdmenu, SWT.CHECK);
        cmdmissionresult.setText("遠征報告書(&T)\tCtrl+T");
        cmdmissionresult.setAccelerator(SWT.CTRL + 'T');
        this.missionResultWindow = new MissionResultTable(this.subwindowHost, cmdmissionresult);

        // コマンド-遠征一覧
        MenuItem missionlist = new MenuItem(cmdmenu, SWT.CHECK);
        missionlist.setText("遠征一覧");
        this.missionTableWindow = new MissionTable(this.subwindowHost, missionlist);

        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);
        // コマンド-所有装備一覧
        MenuItem cmditemlist = new MenuItem(cmdmenu, SWT.CHECK);
        cmditemlist.setText("所有装備一覧(&X)\tCtrl+X");
        cmditemlist.setAccelerator(SWT.CTRL + 'X');
        this.itemTableWindow = new ItemTable(this.subwindowHost, cmditemlist);
        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);
        // コマンド-所有艦娘一覧
        for (int i = 0; i < 4; ++i) {
            MenuItem cmdshiplist = new MenuItem(cmdmenu, SWT.CHECK);
            if (i == 0) {
                cmdshiplist.setAccelerator(SWT.CTRL + ('S'));
            }
            else {
                cmdshiplist.setAccelerator(SWT.CTRL + ('1' + i));
            }
            this.shipTableWindows[i] = new ShipTable(this.subwindowHost, cmdshiplist, i);
        }

        // コマンド-お風呂に入りたい艦娘
        MenuItem cmdbathwaterlist = new MenuItem(cmdmenu, SWT.CHECK);
        cmdbathwaterlist.setText("お風呂に入りたい艦娘(&N)\tCtrl+N");
        cmdbathwaterlist.setAccelerator(SWT.CTRL + 'N');
        this.bathwaterTablwWindow = new BathwaterTableDialog(this.subwindowHost, cmdbathwaterlist);
        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);

        // コマンド-任務一覧
        MenuItem questlist = new MenuItem(cmdmenu, SWT.CHECK);
        questlist.setText("任務一覧(&Q)\tCtrl+Q");
        questlist.setAccelerator(SWT.CTRL + 'Q');
        this.questTableWindow = new QuestTable(this.subwindowHost, questlist);
        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);

        // 表示-戦況ウィンドウ
        MenuItem battleWinMenu = new MenuItem(cmdmenu, SWT.CHECK);
        battleWinMenu.setText("戦況(&B)\tCtrl+B");
        battleWinMenu.setAccelerator(SWT.CTRL + 'B');
        this.battleWindowLarge = new BattleWindowLarge(this.subwindowHost, battleWinMenu);

        // 表示-戦況ウィンドウ （小）
        MenuItem battleWinSMenu = new MenuItem(cmdmenu, SWT.CHECK);
        battleWinSMenu.setText("戦況-横(&H)\tCtrl+H");
        battleWinSMenu.setAccelerator(SWT.CTRL + 'H');
        this.battleWindowSmall = new BattleWindowSmall(this.subwindowHost, battleWinSMenu);

        // 表示-敵味方パラメータ
        MenuItem battleShipWinMenu = new MenuItem(cmdmenu, SWT.CHECK);
        battleShipWinMenu.setText("自軍敵軍パラメータ(&P)\tCtrl+P");
        battleShipWinMenu.setAccelerator(SWT.CTRL + 'P');
        this.battleShipWindow = new BattleShipWindow(this.subwindowHost, battleShipWinMenu);

        // セパレータ
        new MenuItem(cmdmenu, SWT.SEPARATOR);
        // 終了
        final MenuItem dispose = new MenuItem(cmdmenu, SWT.NONE);
        dispose.setText("終了");
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
        this.calcExpWindow = new CalcExpDialog(this.subwindowHost, calcexp);

        // 計算機-演習経験値計算
        MenuItem calcpracticeexp = new MenuItem(calcmenu, SWT.CHECK);
        calcpracticeexp.setText("演習経験値計算機(&V)\tCtrl+V");
        calcpracticeexp.setAccelerator(SWT.CTRL + 'V');
        this.calcPracticeExpWindow = new CalcPracticeExpDialog(this.subwindowHost, calcpracticeexp);

        // その他-資材チャート
        MenuItem resourceChart = new MenuItem(etcmenu, SWT.CHECK);
        resourceChart.setText("資材チャート(&R)\tCtrl+R");
        resourceChart.setAccelerator(SWT.CTRL + 'R');
        this.resourceChartWindow = new ResourceChartDialog(this.subwindowHost, resourceChart);

        // コマンド-出撃統計
        MenuItem battleCounter = new MenuItem(etcmenu, SWT.CHECK);
        battleCounter.setText("出撃統計(&A)\tCtrl+A");
        battleCounter.setAccelerator(SWT.CTRL + 'A');
        this.battleCounterWindow = new BattleAggDialog(this.subwindowHost, battleCounter);
        // セパレータ
        new MenuItem(etcmenu, SWT.SEPARATOR);
        // その他-グループエディター
        MenuItem shipgroup = new MenuItem(etcmenu, SWT.CHECK);
        shipgroup.setText("グループエディター(&G)\tCtrl+G");
        shipgroup.setAccelerator(SWT.CTRL + 'G');
        this.shipFilterGroupWindow = new ShipFilterGroupDialog(this.subwindowHost, shipgroup);
        // その他-自動プロキシ構成スクリプトファイル生成
        MenuItem pack = new MenuItem(etcmenu, SWT.NONE);
        pack.setText("自動プロキシ構成スクリプト");
        pack.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new CreatePacFileDialog(ApplicationMain.this.subwindowHost).open();
            }
        });
        // セパレータ
        new MenuItem(etcmenu, SWT.SEPARATOR);
        // その他-ツール
        MenuItem toolwindows = new MenuItem(etcmenu, SWT.CHECK);
        toolwindows.setText("ツール");
        this.launcherWindow = new LauncherWindow(this.subwindowHost, toolwindows);
        // その他-艦隊タブ切り離し
        MenuItem floatFleetItem = new MenuItem(etcmenu, SWT.CASCADE);
        floatFleetItem.setText("艦隊タブ切り離し");
        Menu floatFleetMenu = new Menu(floatFleetItem);
        floatFleetItem.setMenu(floatFleetMenu);
        // その他-ウィンドウをディスプレイ内に移動
        MenuItem movewindows = new MenuItem(etcmenu, SWT.NONE);
        movewindows.setText("画面外のウィンドウを戻す(&W)\tCtrl+W");
        movewindows.setAccelerator(SWT.CTRL + 'W');
        movewindows.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ApplicationMain.this.moveWindowsIntoDisplay();
            }
        });
        // その他-設定
        MenuItem config = new MenuItem(etcmenu, SWT.NONE);
        config.setText("設定(&O)\tCtrl+O");
        config.setAccelerator(SWT.CTRL + 'O');
        config.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ConfigDialog(ApplicationMain.this).open();
            }
        });
        // その他-バージョン情報
        MenuItem version = new MenuItem(etcmenu, SWT.NONE);
        version.setText("バージョン情報(&V)");
        version.addSelectionListener(new HelpEventListener(this));

        // テスト用
        if (AppConfig.get().isEnableTestWindow()) {
            MenuItem testfeeder = new MenuItem(etcmenu, SWT.NONE);
            testfeeder.setText("テストする");
            testfeeder.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    new TestDataFeeder(ApplicationMain.this).open();
                }
            });

            MenuItem itemcsvout = new MenuItem(etcmenu, SWT.NONE);
            itemcsvout.setText("装備アイテムをCSVダンプ");
            itemcsvout.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    try {
                        File file = new File("itemInfo.csv");
                        OutputStreamWriter fw = new OutputStreamWriter(
                                new BufferedOutputStream(new FileOutputStream(file)), AppConstants.CHARSET);
                        Item.dumpCSV(fw);
                        fw.close();
                        SwtUtils.messageDialog("以下のファイルに書き込みました\n" + file.getAbsolutePath(),
                                ApplicationMain.this.shell);
                    } catch (IOException e1) {
                        logPrint("書き込み失敗: " + e1.getMessage());
                    }
                }
            });
            MenuItem shipcsvout = new MenuItem(etcmenu, SWT.NONE);
            shipcsvout.setText("艦娘をCSVダンプ");
            shipcsvout.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    try {
                        File file = new File("shipInfo.csv");
                        OutputStreamWriter fw = new OutputStreamWriter(
                                new BufferedOutputStream(new FileOutputStream(file)), AppConstants.CHARSET);
                        Ship.dumpCSV(fw);
                        fw.close();
                        SwtUtils.messageDialog("以下のファイルに書き込みました\n" + file.getAbsolutePath(),
                                ApplicationMain.this.shell);
                    } catch (IOException e1) {
                        logPrint("書き込み失敗: " + e1.getMessage());
                    }
                }
            });
        }

        // ショートカットキー
        this.display.addFilter(SWT.KeyDown, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if ((e.stateMask & (SWT.CTRL | SWT.SHIFT)) == (SWT.CTRL | SWT.SHIFT)) {
                    ApplicationMain.this.shortcutKeyPushed(e.keyCode);
                }
            }
        });

        // シェルイベント
        this.shell.addShellListener(new MainShellAdapter());
        // キーが押された時に呼ばれるリスナーを追加します
        this.shell.addHelpListener(new HelpEventListener(this));

        this.trayItem = this.addTrayItem(this.display);

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
        this.mainComposite.setLayout(SwtUtils.makeGridLayout(1, 1, 0, 0, 0));
        this.mainComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mainItem.setControl(this.mainComposite);

        // 通知設定
        this.notifySettingGroup = new Group(this.mainComposite, SWT.NONE);
        this.notifySettingGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        RowLayout notifySettingLayout = new RowLayout(SWT.HORIZONTAL);
        notifySettingLayout.marginBottom = notifySettingLayout.marginHeight = notifySettingLayout.marginTop = notifySettingLayout.marginWidth = notifySettingLayout.spacing = 0;
        this.notifySettingGroup.setLayout(notifySettingLayout);
        this.notifySettingGroup.setText("通知設定");

        this.deckNotice = new Button(this.notifySettingGroup, SWT.CHECK);
        this.deckNotice.setSelection(AppConfig.get().isNoticeDeckmission());
        this.deckNotice.setText("遠征");
        this.deckNotice.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setNoticeDeckmission(ApplicationMain.this.deckNotice.getSelection());
            }
        });

        this.ndockNotice = new Button(this.notifySettingGroup, SWT.CHECK);
        this.ndockNotice.setSelection(AppConfig.get().isNoticeNdock());
        this.ndockNotice.setText("入渠");
        this.ndockNotice.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setNoticeNdock(ApplicationMain.this.ndockNotice.getSelection());
            }
        });

        this.akashiNotice = new Button(this.notifySettingGroup, SWT.CHECK);
        this.akashiNotice.setSelection(AppConfig.get().isNoticeAkashi());
        this.akashiNotice.setText("泊地修理");
        this.akashiNotice.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setNoticeAkashi(ApplicationMain.this.akashiNotice.getSelection());
            }
        });

        this.condNotice = new Button(this.notifySettingGroup, SWT.CHECK);
        this.condNotice.setSelection(AppConfig.get().isNoticeCond());
        this.condNotice.setText("疲労");
        this.condNotice.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setNoticeCond(ApplicationMain.this.condNotice.getSelection());
            }
        });

        // 艦隊
        this.deckGroup = new Group(this.mainComposite, SWT.NONE);
        this.deckGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.deckGroup.setText("艦隊");
        this.deckGroup.setLayout(SwtUtils.makeGridLayout(2, 1, 1, 0, 0));

        this.deck1name = new Label(this.deckGroup, SWT.NONE);
        this.deck1name.setText("ここに艦隊1の艦隊名が入ります");
        this.deck1name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.deck1time = new Text(this.deckGroup, SWT.SINGLE | SWT.BORDER);
        this.deck1time.setText("艦隊1の帰投時間");
        GridData gddeck1time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gddeck1time.widthHint = SwtUtils.DPIAwareWidth(75);
        this.deck1time.setLayoutData(gddeck1time);

        this.deck2name = new Label(this.deckGroup, SWT.NONE);
        this.deck2name.setText("ここに艦隊2の艦隊名が入ります");
        this.deck2name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.deck2time = new Text(this.deckGroup, SWT.SINGLE | SWT.BORDER);
        this.deck2time.setText("艦隊2の帰投時間");
        GridData gddeck2time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gddeck2time.widthHint = SwtUtils.DPIAwareWidth(75);
        this.deck2time.setLayoutData(gddeck2time);

        this.deck3name = new Label(this.deckGroup, SWT.NONE);
        this.deck3name.setText("ここに艦隊3の艦隊名が入ります");
        this.deck3name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.deck3time = new Text(this.deckGroup, SWT.SINGLE | SWT.BORDER);
        this.deck3time.setText("艦隊3の帰投時間");
        GridData gddeck3time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gddeck3time.widthHint = SwtUtils.DPIAwareWidth(75);
        this.deck3time.setLayoutData(gddeck3time);

        this.deck4name = new Label(this.deckGroup, SWT.NONE);
        this.deck4name.setText("ここに艦隊4の艦隊名が入ります");
        this.deck4name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.deck4time = new Text(this.deckGroup, SWT.SINGLE | SWT.BORDER);
        this.deck4time.setText("艦隊4の帰投時間");
        GridData gddeck4time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gddeck4time.widthHint = SwtUtils.DPIAwareWidth(75);
        this.deck4time.setLayoutData(gddeck4time);

        // 入渠
        this.ndockGroup = new Group(this.mainComposite, SWT.NONE);
        this.ndockGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.ndockGroup.setText("入渠");
        this.ndockGroup.setLayout(SwtUtils.makeGridLayout(2, 1, 1, 0, 0));

        this.ndock1name = new Label(this.ndockGroup, SWT.NONE);
        this.ndock1name.setText("ドッグ1に浸かっている艦娘の名前");
        this.ndock1name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.ndock1time = new Text(this.ndockGroup, SWT.SINGLE | SWT.BORDER);
        this.ndock1time.setText("お風呂から上がる時間");
        GridData gdndock1time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdndock1time.widthHint = SwtUtils.DPIAwareWidth(75);
        this.ndock1time.setLayoutData(gdndock1time);

        this.ndock2name = new Label(this.ndockGroup, SWT.NONE);
        this.ndock2name.setText("ドッグ2に浸かっている艦娘の名前");
        this.ndock2name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.ndock2time = new Text(this.ndockGroup, SWT.SINGLE | SWT.BORDER);
        this.ndock2time.setText("お風呂から上がる時間");
        GridData gdndock2time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdndock2time.widthHint = SwtUtils.DPIAwareWidth(75);
        this.ndock2time.setLayoutData(gdndock2time);

        this.ndock3name = new Label(this.ndockGroup, SWT.NONE);
        this.ndock3name.setText("ドッグ3に浸かっている艦娘の名前");
        this.ndock3name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.ndock3time = new Text(this.ndockGroup, SWT.SINGLE | SWT.BORDER);
        this.ndock3time.setText("お風呂から上がる時間");
        GridData gdndock3time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdndock3time.widthHint = SwtUtils.DPIAwareWidth(75);
        this.ndock3time.setLayoutData(gdndock3time);

        this.ndock4name = new Label(this.ndockGroup, SWT.NONE);
        this.ndock4name.setText("ドッグ4に浸かっている艦娘の名前");
        this.ndock4name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.ndock4time = new Text(this.ndockGroup, SWT.SINGLE | SWT.BORDER);
        this.ndock4time.setText("お風呂から上がる時間");
        GridData gdndock4time = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdndock4time.widthHint = SwtUtils.DPIAwareWidth(75);
        this.ndock4time.setLayoutData(gdndock4time);

        // -------

        this.akashiTimerGroup = new Composite(this.mainComposite, SWT.NONE);
        this.akashiTimerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.akashiTimerGroup.setLayout(SwtUtils.makeGridLayout(2, 1, 1, 3, 3));

        this.akashiTimerLabel = new Label(this.akashiTimerGroup, SWT.NONE);
        this.akashiTimerLabel.setText("泊地修理タイマー");
        this.akashiTimerLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.akashiTimerTime = new Text(this.akashiTimerGroup, SWT.SINGLE | SWT.BORDER);
        this.akashiTimerTime.setText("泊地修理タイマーの経過時間");
        GridData gdakashiTimerTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdakashiTimerTime.widthHint = SwtUtils.DPIAwareWidth(75);
        this.akashiTimerTime.setLayoutData(gdakashiTimerTime);

        this.condTimerGroup = new Composite(this.mainComposite, SWT.NONE);
        this.condTimerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.condTimerGroup.setLayout(SwtUtils.makeGridLayout(2, 1, 1, 3, 3));

        this.condTimerLabel = new Label(this.condTimerGroup, SWT.NONE);
        this.condTimerLabel.setText("次の疲労回復まで");
        this.condTimerLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.condTimerTime = new Text(this.condTimerGroup, SWT.SINGLE | SWT.BORDER);
        this.condTimerTime.setText("次の疲労回復までの時間");
        GridData gdconTimeTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdconTimeTime.widthHint = SwtUtils.DPIAwareWidth(75);
        this.condTimerTime.setLayoutData(gdconTimeTime);

        this.resultRecordGroup = new Composite(this.mainComposite,SWT.NONE);
        this.resultRecordGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.resultRecordGroup.setLayout(SwtUtils.makeGridLayout(2, 1, 1, 3, 3));

        this.resultRecordLabel = new Label(this.resultRecordGroup, SWT.NONE);
        this.resultRecordLabel.setText(String.format("戦果　今回: %8.2f / 今日: %8.2f / 今月: %8.2f",0.0,0.0,0.0));
        this.resultRecordLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.admiralExpLabel = new Label(this.resultRecordGroup, SWT.RIGHT);
        this.admiralExpLabel.setText(String.format("%d exp.",0));
        GridData gdAdmiralExp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdAdmiralExp.widthHint = SwtUtils.DPIAwareWidth(75);
        this.admiralExpLabel.setLayoutData(gdAdmiralExp);

        this.airbaseGroup = new Composite(this.mainComposite,SWT.NONE);
        this.airbaseGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.airbaseGroup.setLayout(SwtUtils.makeGridLayout(1, 1, 1, 3, 3));

        this.airbaseCombo = new Combo(this.airbaseGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        this.airbaseCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.airbaseCombo.add("基地航空隊");
        this.airbaseCombo.select(0);
        this.airbaseCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateAirbase();
            }
        });

        // -------

        // エラー表示
        this.errorLabel = new Label(this.mainComposite, SWT.NONE);
        this.errorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.errorLabel.setAlignment(SWT.CENTER);
        this.errorLabel.setBackground(ColorManager.getColor(AppConstants.COND_RED_COLOR));
        this.errorLabel.setText("エラー表示");
        LayoutLogic.hide(this.errorLabel, true);
        this.errorLabel.setVisible(false);

        // コンソール
        this.console = new org.eclipse.swt.widgets.List(this.mainComposite, SWT.BORDER | SWT.V_SCROLL);
        this.console.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

        // 艦隊ウィンドウ
        for (int i = 0; i < this.fleetWindows.length; ++i) {
            MenuItem menuItem = new MenuItem(floatFleetMenu, SWT.CHECK);
            menuItem.setText("#" + (i + 1));
            this.fleetWindows[i] = new FleetWindow(this.subwindowHost, menuItem, this.tabFolder, i + 1);
        }

        // メニュー表示
        this.tabFolder.setData("disable-window-menu-this", new Object());
        this.tabFolder.addListener(SWT.MenuDetect, new Listener() {
            @Override
            public void handleEvent(Event event) {
                CTabFolder tabFolder = ApplicationMain.this.tabFolder;
                Point point = ApplicationMain.this.display.map(null,
                        tabFolder, new Point(event.x, event.y));
                CTabItem item = tabFolder.getItem(point);
                if (item != null) {
                    Object data = item.getData();
                    if (data instanceof FleetWindow) {
                        FleetWindow fw = (FleetWindow) data;
                        fw.showTabMenu();
                        return;
                    }
                }
                // ウィンドウメニューを表示
                //ApplicationMain.this.getPopupMenu().setVisible(true);
            }
        });

        //  ウィンドウの右クリックメニューに追加
        if (this.getPopupMenu().getItemCount() > 0) {
            new MenuItem(this.getPopupMenu(), SWT.SEPARATOR);
        }

        MenuItem showNotifySetting = new MenuItem(this.getPopupMenu(), SWT.CHECK);
        showNotifySetting.setText("通知設定を表示");
        this.bindControlToMenuItem(this.notifySettingGroup, showNotifySetting, "ShowNotifySetting");

        MenuItem showAkashiTimer = new MenuItem(this.getPopupMenu(), SWT.CHECK);
        showAkashiTimer.setText("泊地修理タイマーを表示");
        this.bindControlToMenuItem(this.akashiTimerGroup, showAkashiTimer, "ShowAkashiGlobalTimer");

        MenuItem showCondTimer = new MenuItem(this.getPopupMenu(), SWT.CHECK);
        showCondTimer.setText("疲労タイマーを表示");
        this.bindControlToMenuItem(this.condTimerGroup, showCondTimer, "ShowCondCycleTimer");

        MenuItem showResultRecord = new MenuItem(this.getPopupMenu(), SWT.CHECK);
        showResultRecord.setText("戦果を表示");
        this.bindControlToMenuItem(this.resultRecordGroup, showResultRecord, "ShowResultRecord");

        MenuItem showAirbase = new MenuItem(this.getPopupMenu(), SWT.CHECK);
        showAirbase.setText("基地航空隊を表示");
        this.bindControlToMenuItem(this.airbaseGroup, showAirbase, "ShowAirbase");

        // 縮小表示
        final MenuItem dispsize = new MenuItem(this.getPopupMenu(), SWT.CHECK);
        dispsize.setText("縮小表示(&M)\tCtrl+M");
        dispsize.setAccelerator(SWT.CTRL + 'M');

        // 初期設定 縮小表示が有効なら縮小表示にする
        if (AppConfig.get().isMinimumLayout()) {
            this.shell.setRedraw(false);
            ApplicationMain.this.hide(true, this.getSwitchControls());
            dispsize.setSelection(true);
            this.shell.pack();
            this.shell.setRedraw(true);
        }

        // 縮小表示チェック時の動作
        dispsize.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell shell = ApplicationMain.this.shell;
                shell.setRedraw(false);
                boolean minimum = dispsize.getSelection();
                // コントロールを隠す
                ApplicationMain.this.hide(minimum, ApplicationMain.this.getSwitchControls());

                // タブを整理する
                ApplicationMain.this.tabFolder.setSelection(0);

                if (AppConfig.get().isCloseWhenMinimized()) {
                    // 他のウィンドウを連動させる
                    if (minimum) {
                        ApplicationMain.this.childIconified();
                    }
                    else {
                        ApplicationMain.this.childDeiconified();
                    }
                }

                // 付随しないため
                updateResultRecord(minimum);
                updateAirbase(minimum);

                // ウインドウサイズを調節
                if (minimum) {
                    // ウィンドウのサイズを記憶
                    ApplicationMain.this.save();

                    ApplicationMain.this.tabFolder.setSingle(true);
                    CTabItem[] tabitems = ApplicationMain.this.tabFolder.getItems();
                    for (CTabItem tabitem : tabitems) {
                        Control control = tabitem.getControl();
                        if (control instanceof FleetComposite) {
                            LayoutLogic.hide(control, true);
                        }
                    }

                    shell.pack();

                    ApplicationMain.this.tabFolder.setSingle(false);
                    for (CTabItem tabitem : tabitems) {
                        Control control = tabitem.getControl();
                        if (control instanceof FleetComposite) {
                            LayoutLogic.hide(control, false);
                        }
                    }
                }
                else {
                    shell.setSize(ApplicationMain.this.getRestoreSize());
                }
                shell.setRedraw(true);

                // 設定を保存
                AppConfig.get().setMinimumLayout(minimum);
            }
        });

        final MenuItem rootCopyDeckBuilder = new MenuItem(this.getPopupMenu(), SWT.CASCADE);
        rootCopyDeckBuilder.setText("艦隊シミュレーター＆デッキビルダー");
        Menu copyDeckBuilderMenu = new Menu(rootCopyDeckBuilder);
        /*
        final MenuItem copyDeckBuilderFormat = new MenuItem(copyDeckBuilderMenu, SWT.PUSH);
        copyDeckBuilderFormat.setText("フォーマットをクリップボードにコピー");
        
        copyDeckBuilderFormat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean[] isUseCopyDeckBuilder = {
                        AppConfig.get().isUseCopyDeckBuilder1(),
                        AppConfig.get().isUseCopyDeckBuilder2(),
                        AppConfig.get().isUseCopyDeckBuilder3(),
                        AppConfig.get().isUseCopyDeckBuilder4() };
                if (GlobalContext.getState() == 1) {
                    Clipboard clipboard = new Clipboard(Display.getDefault());
                    clipboard.setContents(new Object[] { new DeckBuilder().getDeckBuilderFormat(isUseCopyDeckBuilder) },
                            new Transfer[] { TextTransfer.getInstance() });
                } else {
                    Shell shell = new Shell(Display.getDefault(), SWT.TOOL);
                    MessageBox mes = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                    mes.setText(AppConstants.TITLEBAR_TEXT);
                    mes.setMessage("母港情報が不足しています。母港画面に遷移してデータを読み込んでください。");
                    mes.open();
                    shell.dispose();
                }
            }
        });*/
        final MenuItem copyDeckBuilderURL = new MenuItem(copyDeckBuilderMenu, SWT.PUSH);
        copyDeckBuilderURL.setText("URLをクリップボードにコピー");

        copyDeckBuilderURL.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean[] isUseCopyDeckBuilder = {
                        AppConfig.get().isUseCopyDeckBuilder1(),
                        AppConfig.get().isUseCopyDeckBuilder2(),
                        AppConfig.get().isUseCopyDeckBuilder3(),
                        AppConfig.get().isUseCopyDeckBuilder4() };
                if (GlobalContext.getState() == 1) {
                    Clipboard clipboard = new Clipboard(Display.getDefault());
                    clipboard.setContents(new Object[] { new DeckBuilder().getDeckBuilderURL(isUseCopyDeckBuilder) },
                            new Transfer[] { TextTransfer.getInstance() });
                }
                else {
                    Shell shell = new Shell(Display.getDefault(), SWT.TOOL);
                    MessageBox mes = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                    mes.setText(AppConstants.TITLEBAR_TEXT);
                    mes.setMessage("情報が不足しています。艦これをリロードしてデータを読み込んでください。");
                    mes.open();
                    shell.dispose();
                }
            }
        });
        new MenuItem(copyDeckBuilderMenu, SWT.SEPARATOR);
        final MenuItem copyDeckBuilder1 = new MenuItem(copyDeckBuilderMenu, SWT.CHECK);
        copyDeckBuilder1.setText("第一艦隊");
        copyDeckBuilder1.setSelection(AppConfig.get().isUseCopyDeckBuilder1());
        copyDeckBuilder1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setUseCopyDeckBuilder1(copyDeckBuilder1.getSelection());
            }
        });
        final MenuItem copyDeckBuilder2 = new MenuItem(copyDeckBuilderMenu, SWT.CHECK);
        copyDeckBuilder2.setText("第二艦隊");
        copyDeckBuilder2.setSelection(AppConfig.get().isUseCopyDeckBuilder2());
        copyDeckBuilder2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setUseCopyDeckBuilder2(copyDeckBuilder2.getSelection());
            }
        });
        final MenuItem copyDeckBuilder3 = new MenuItem(copyDeckBuilderMenu, SWT.CHECK);
        copyDeckBuilder3.setText("第三艦隊");
        copyDeckBuilder3.setSelection(AppConfig.get().isUseCopyDeckBuilder3());
        copyDeckBuilder3.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setUseCopyDeckBuilder3(copyDeckBuilder3.getSelection());
            }
        });
        final MenuItem copyDeckBuilder4 = new MenuItem(copyDeckBuilderMenu, SWT.CHECK);
        copyDeckBuilder4.setText("第四艦隊");
        copyDeckBuilder4.setSelection(AppConfig.get().isUseCopyDeckBuilder4());
        copyDeckBuilder4.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setUseCopyDeckBuilder4(copyDeckBuilder4.getSelection());
            }
        });

        rootCopyDeckBuilder.setMenu(copyDeckBuilderMenu);

        final MenuItem rootFleetFormatter = new MenuItem(this.getPopupMenu(), SWT.CASCADE);
        rootFleetFormatter.setText("所持艦隊晒し用ページ");
        Menu copyFleetFormatterMenu = new Menu(rootFleetFormatter);

        final MenuItem copyFleetFormat = new MenuItem(copyFleetFormatterMenu, SWT.PUSH);
        copyFleetFormat.setText("フォーマットをクリップボードにコピー");

        copyFleetFormat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean isLockedOnlyFleetFormat = AppConfig.get().isUseLockedOnlyFleetFormat();
                if (GlobalContext.getState() == 1) {
                    Clipboard clipboard = new Clipboard(Display.getDefault());
                    clipboard.setContents(new Object[] { new FleetFormatter().get(isLockedOnlyFleetFormat) },
                            new Transfer[] { TextTransfer.getInstance() });
                }
                else {
                    Shell shell = new Shell(Display.getDefault(), SWT.TOOL);
                    MessageBox mes = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                    mes.setText(AppConstants.TITLEBAR_TEXT);
                    mes.setMessage("情報が不足しています。艦これをリロードしてデータを読み込んでください。");
                    mes.open();
                    shell.dispose();
                }
            }
        });

        final MenuItem copyFleetFormatURL = new MenuItem(copyFleetFormatterMenu, SWT.PUSH);
        copyFleetFormatURL.setText("サイトURLをクリップボードにコピー");

        copyFleetFormatURL.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Clipboard clipboard = new Clipboard(Display.getDefault());
                clipboard.setContents(new Object[] { "http://kancolle-calc.net/kanmusu_list.html" },
                        new Transfer[] { TextTransfer.getInstance() });
            }
        });

        new MenuItem(copyFleetFormatterMenu, SWT.SEPARATOR);
        final MenuItem isLockedOnlyFleetFormat = new MenuItem(copyFleetFormatterMenu, SWT.CHECK);
        isLockedOnlyFleetFormat.setText("ロックしている艦限定");
        isLockedOnlyFleetFormat.setSelection(AppConfig.get().isUseLockedOnlyFleetFormat());
        ;
        isLockedOnlyFleetFormat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setUseLockedOnlyFleetFormat(isLockedOnlyFleetFormat.getSelection());
            }
        });

        rootFleetFormatter.setMenu(copyFleetFormatterMenu);

        final MenuItem rootItemFormatter = new MenuItem(this.getPopupMenu(), SWT.CASCADE);
        rootItemFormatter.setText("艦隊分析ページ");
        Menu copyItemFormatterMenu = new Menu(rootItemFormatter);

        final MenuItem copyItemFormat = new MenuItem(copyItemFormatterMenu, SWT.PUSH);
        copyItemFormat.setText("装備フォーマットをクリップボードにコピー");

        copyItemFormat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                if (GlobalContext.getState() == 1) {
                    Clipboard clipboard = new Clipboard(Display.getDefault());
                                  clipboard.setContents(new Object[] { new ItemFormatter().get(true) },
                            new Transfer[] { TextTransfer.getInstance() });
                } else {
                    Shell shell = new Shell(Display.getDefault(), SWT.TOOL);
                    MessageBox mes = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                    mes.setText(AppConstants.TITLEBAR_TEXT);
                    mes.setMessage("情報が不足しています。艦これをリロードしてデータを読み込んでください。");
                    mes.open();
                    shell.dispose();
                }
            }
        });

        final MenuItem copyItemFormatURL = new MenuItem(copyItemFormatterMenu, SWT.PUSH);
        copyItemFormatURL.setText("サイトURLをクリップボードにコピー");

        copyItemFormatURL.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Clipboard clipboard = new Clipboard(Display.getDefault());
                clipboard.setContents(new Object[] { "https://kancolle-fleetanalysis.firebaseapp.com/#/equipInput" },
                        new Transfer[] { TextTransfer.getInstance() });
            }
        });

        rootItemFormatter.setMenu(copyItemFormatterMenu);
        
        // 選択する項目はドラックで移動できないようにする
        for (Control c : new Control[] { this.commandComposite,
                this.deckNotice, this.ndockNotice,
                this.deck1time, this.deck2time, this.deck3time, this.deck4time,
                this.akashiTimerTime,
                this.ndock1time, this.ndock2time, this.ndock3time, this.ndock4time,
                this.condTimerTime,
                this.console }) {
            c.setData("disable-drag-move", true);
        }
        this.tabFolder.setData("disable-drag-move-this", true);

        // 処理開始前に必要な値をセット
        BattleResultServer.setLogPath(AppConfig.get().getBattleLogPath());

        this.configUpdated();

        // ホットキー
        JIntellitypeWrapper.addListener(new HotkeyListener() {
            @Override
            public void onHotKey(int arg0) {
                ApplicationMain.this.display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (ApplicationMain.this.shell.isDisposed() == false) {
                            ApplicationMain.this.shell.forceActive();
                        }
                    }
                });
            }
        });

        sysPrint("ウィンドウ構築完了");

        this.startThread();
        this.updateCheck();
    }

    private void bindControlToMenuItem(final Control control, final MenuItem menu, String name) {
        try {
            Method isEnabled = AppConfigBean.class.getMethod("is" + name);
            final Method setEnabled = AppConfigBean.class.getMethod("set" + name, boolean.class);
            boolean enabled = (Boolean) isEnabled.invoke(AppConfig.get());
            menu.setSelection(enabled);
            // 初期設定
            if (!enabled) {
                ApplicationMain.this.hide(true, new Control[] { control });
            }
            menu.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    boolean show = menu.getSelection();

                    // コントロールを隠す
                    Shell shell = ApplicationMain.this.shell;
                    shell.setRedraw(false);
                    ApplicationMain.this.hide(!show, new Control[] { control });
                    ApplicationMain.this.mainComposite.layout();
                    shell.setRedraw(true);

                    // 設定を保存
                    try {
                        setEnabled.invoke(AppConfig.get(), show);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            });
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e2) {
            throw new RuntimeException(e2);
        }
    }

    private void shortcutKeyPushed(int keyCode) {
        switch (keyCode) {
        case 'd':
            this.activate(this.dropReportWindow);
            break;
        case 'y':
            this.activate(this.createShipReportWindow);
            break;
        case 'e':
            this.activate(this.createItemReportWindow);
            break;
        case 't':
            this.activate(this.missionResultWindow);
            break;
        case 'x':
            this.activate(this.itemTableWindow);
            break;
        case '1':
            this.activate(this.shipTableWindows[0]);
            break;
        case 's':
            this.activate(this.shipTableWindows[0]);
            break;
        case '2':
            this.activate(this.shipTableWindows[1]);
            break;
        case '3':
            this.activate(this.shipTableWindows[2]);
            break;
        case '4':
            this.activate(this.shipTableWindows[3]);
            break;
        case 'n':
            this.activate(this.bathwaterTablwWindow);
            break;
        case 'q':
            this.activate(this.questTableWindow);
            break;
        case 'b':
            this.activate(this.battleWindowLarge);
            break;
        case 'h':
            this.activate(this.battleWindowSmall);
            break;
        case 'p':
            this.activate(this.battleShipWindow);
            break;
        case 'c':
            this.activate(this.calcExpWindow);
            break;
        case 'v':
            this.activate(this.calcPracticeExpWindow);
            break;
        case 'g':
            this.activate(this.shipFilterGroupWindow);
            break;
        case 'a':
            this.activate(this.battleCounterWindow);
            break;
        case 'r':
            this.activate(this.resourceChartWindow);
            break;
        case 'z':
            this.shell.setActive();
            break;
        case 'w':
            this.moveWindowsIntoDisplay();
            break;
        }
    }

    private void activate(WindowBase win) {
        if ((win.getShell() == null) || (win.getShell().isVisible() == false)) {
            win.open();
        }
        win.getShell().setActive();
        MenuItem menu = win.getMenuItem();
        if (menu != null) {
            menu.setSelection(true);
        }
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
        if (this.showSubwindowHost) {
            this.subwindowHost.setVisible(true);
        }
        this.shell.forceActive();
        sysPrint("メインウィンドウ表示完了");
        for (WindowBase window : this.getWindowList()) {
            window.restore();
        }
    }

    public void saveWindows() {
        this.save();
        for (WindowBase window : this.getWindowList()) {
            window.save();
        }
    }

    private void moveWindowsIntoDisplay() {
        this.moveIntoDisplay();
        for (WindowBase window : this.getWindowList()) {
            window.moveIntoDisplay();
        }
    }

    // Main以外のウィンドウも連動させる
    private void childDeiconified() {
        for (Shell shell : ApplicationMain.this.subwindowHost.getShells()) {
            if (shell.getData() instanceof WindowBase) {
                WindowBase window = (WindowBase) shell.getData();
                window.shellDeiconified();
            }
        }
    }

    // Main以外のウィンドウも連動させる
    private void childIconified() {
        for (Shell shell : ApplicationMain.this.subwindowHost.getShells()) {
            if (shell.getData() instanceof WindowBase) {
                WindowBase window = (WindowBase) shell.getData();
                window.shellIconified();
            }
        }
    }

    private Control[] getSwitchControls() {
        Control[] controls = new Control[0];
        return ArrayUtils.addAll(controls,
                this.commandComposite,
                this.deck1name, this.deck2name,
                this.deck3name, this.deck4name,
                this.ndock1name, this.ndock2name,
                this.ndock3name, this.ndock4name,
                this.console);
    }

    public WindowBase[] getWindowList() {
        return new WindowBase[] {
                this.captureWindow,
                this.dropReportWindow,
                this.createShipReportWindow,
                this.createItemReportWindow,
                this.missionResultWindow,
                this.missionTableWindow,
                this.itemTableWindow,
                this.shipTableWindows[0],
                this.shipTableWindows[1],
                this.shipTableWindows[2],
                this.shipTableWindows[3],
                this.bathwaterTablwWindow,
                this.questTableWindow,
                this.battleWindowLarge,
                this.battleWindowSmall,
                this.battleShipWindow,
                this.calcExpWindow,
                this.calcPracticeExpWindow,
                this.shipFilterGroupWindow,
                this.resourceChartWindow,
                this.battleCounterWindow,
                this.fleetWindows[0],
                this.fleetWindows[1],
                this.fleetWindows[2],
                this.fleetWindows[3],
                this.launcherWindow
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
        item.setImage(SWTResourceManager.getImage(WindowBase.class, AppConstants.LOGO));
        item.setToolTipText(AppConstants.TITLEBAR_TEXT);
        item.addListener(SWT.Selection, new TraySelectionListener(this.shell));
        item.addMenuDetectListener(new TrayItemMenuListener());
        return item;
    }

    /**
     * 縮小表示と通常表示とを切り替えます
     *
     * @param minimum
     * @param controls 隠すコントロール
     */
    private void hide(boolean minimum, Control[] controls) {
        for (Control control : controls) {
            LayoutLogic.hide(control, minimum);
        }
        this.shell.layout();
    }

    /**
     * スレッドを開始します
     */
    private void startThread() {
        // 時間のかかる初期化を別スレッドで実行
        Thread backgroundInitializer = new BackgroundInitializer(this.shell);
        backgroundInitializer.setDaemon(true);
        backgroundInitializer.start();

        sysPrint("その他のスレッド起動...");
        // 非同期で画面を更新するスレッド
        ThreadManager.regist(new AsyncExecApplicationMain(this));
        // サウンドを出すスレッド
        ThreadManager.regist(new Sound.PlayerThread());
        // Push通知を行うスレッド
        ThreadManager.regist(new PushNotify.PushNotifyThread());
        // スレッドを監視するスレッド
        ThreadManager.regist(new ThreadStateObserver(this.shell));

        ThreadManager.start();
    }

    private static void endThread() {
        // リソースを開放する
        SWTResourceManager.dispose();
        // プロキシサーバーをシャットダウンする
        ProxyServer.end();
        DatabaseClient.end();
        // ホットキーを解除
        JIntellitypeWrapper.cleanup();
    }

    private void updateCheck() {
        // アップデートチェックする
        final Display display = this.shell.getDisplay();
        new AsyncExecUpdateCheck(new AsyncExecUpdateCheck.UpdateResult() {

            @Override
            public void onSuccess(final String[] okversions) {
                boolean ok = false;
                for (String okversion : okversions) {
                    if (AppConstants.VERSION.equals(okversion)) {
                        ok = true;
                        break;
                    }
                }

                if ((ok == false) && AppConfig.get().isUpdateCheck()) {
                    display.asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (ApplicationMain.this.shell.isDisposed()) {
                                    // ウインドウが閉じられていたらなにもしない
                                    return;
                                }

                                MessageBox box = new MessageBox(ApplicationMain.this.shell, SWT.YES | SWT.NO
                                        | SWT.ICON_QUESTION);
                                box.setText("新しいバージョン");
                                box.setMessage("新しいバージョンがあります。ホームページを開きますか？\r\n"
                                        + "現在のバージョン:" + AppConstants.VERSION + "\r\n"
                                        + "新しいバージョン:" + okversions[0] + "\r\n"
                                        + "※自動アップデートチェックは[その他]-[設定]からOFFに出来ます");

                                // OKを押されたらホームページへ移動する
                                if (box.open() == SWT.YES) {
                                    try {
                                        Desktop.getDesktop().browse(AppConstants.HOME_PAGE_URI);
                                    } catch (Exception e) {
                                        LOG.get().warn("ウェブサイトに移動が失敗しました", e);
                                    }
                                }
                            }
                            catch (Exception e) {
                                LOG.get().warn("更新確認でエラー", e);
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                // チェックしなくてもいい設定の場合はエラーを無視する
                if (AppConfig.get().isUpdateCheck()) {
                    // アップデートチェック失敗はクラス名のみ
                    LOG.get().info(e.getClass().getName() + "が原因でアップデートチェックに失敗しました");
                }
            }
        }).start();
    }

    @Override
    protected boolean shouldSaveWindowSize() {
        return !AppConfig.get().isMinimumLayout();
    }

    @Override
    protected Point getDefaultSize() {
        return SwtUtils.DPIAwareSize(new Point(280, 420));
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
            String menuTitle = (i == 0) ? (shipNames[i] + "(&S)\tCtrl+S")
                    : (shipNames[i] + "(&" + number + ")\tCtrl+" + number);
            this.shipTableWindows[i].getMenuItem().setText(menuTitle);
            this.shipTableWindows[i].windowTitleChanged();
        }
        // ツールウィンドウ
        this.launcherWindow.configUpdated();
        //
        JIntellitypeWrapper.changeSetting(AppConfig.get().getSystemWideHotKey());
        // プロキシサーバ再起動
        ProxyServer.restart();
    }

    public void setTitleText(String newText) {
        if ((this.titleText == null) || (this.titleText.equals(newText) == false)) {
            this.shell.setText(newText);
            this.trayItem.setText(newText);
            this.titleText = newText;
        }
    }

    /** キャプチャ */
    public CaptureDialog getCaptureDialog() {
        return this.captureWindow;
    }

    /** ドロップ報告書 */
    public DropReportTable getDropTable() {
        return this.dropReportWindow;
    }

    /** 建造報告書 */
    public CreateShipReportTable getCreateShipReportTable() {
        return this.createShipReportWindow;
    }

    /** 開発報告書 */
    public CreateItemReportTable getCreateItemReportTable() {
        return this.createItemReportWindow;
    }

    /** 遠征報告書 */
    public MissionResultTable getMissionResultTable() {
        return this.missionResultWindow;
    }

    /** 所有装備一覧 */
    public ItemTable getItemTable() {
        return this.itemTableWindow;
    }

    /** 艦娘一覧1-4 */
    public ShipTable[] getShipTables() {
        return this.shipTableWindows;
    }

    /** お風呂に入りたい艦娘 */
    public BathwaterTableDialog getBathwaterTableDialog() {
        return this.bathwaterTablwWindow;
    }

    /** 遠征一覧 */
    public MissionTable getMissionTable() {
        return this.missionTableWindow;
    }

    /** 任務一覧 */
    public QuestTable getQuestTable() {
        return this.questTableWindow;
    }

    /** 戦況 */
    public BattleWindowLarge getBattleWindowLarge() {
        return this.battleWindowLarge;
    }

    /** 戦況-横 */
    public BattleWindowSmall getBattleWindowSmall() {
        return this.battleWindowSmall;
    }

    /** 自軍敵軍パラメータ */
    public BattleShipWindow getBattleShipWindow() {
        return this.battleShipWindow;
    }

    /** 経験値計算 */
    public CalcExpDialog getCalcExpDialog() {
        return this.calcExpWindow;
    }

    /** 演習経験値計算 */
    public CalcPracticeExpDialog getCalcPracticeExpDialog() {
        return this.calcPracticeExpWindow;
    }

    /** 出撃統計 */
    public BattleAggDialog getBattleAggDialog() {
        return this.battleCounterWindow;
    }

    /** グループエディター */
    public ShipFilterGroupDialog getShipFilterGroupDialog() {
        return this.shipFilterGroupWindow;
    }

    /** 資材チャート */
    public ResourceChartDialog getResourceChartDialog() {
        return this.resourceChartWindow;
    }

    /** ツールウィンドウ */
    public LauncherWindow getLauncherWindow() {
        return this.launcherWindow;
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
     * @return 1分前に通知する(遠征)
     */
    public Button getDeckNotice() {
        return this.deckNotice;
    }

    /**
     * @return 1分前に通知する(入渠)
     */
    public Button getNdockNotice() {
        return this.ndockNotice;
    }

    /**
     * @return akashiNotice
     */
    public Button getAkashiNotice() {
        return this.akashiNotice;
    }

    /**
     * @return condNotice
     */
    public Button getCondNotice() {
        return this.condNotice;
    }

    /**
     * @return 遠征グループ
     */
    public Group getDeckGroup() {
        return this.deckGroup;
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
     * @return 遠征.艦隊4の艦隊名
     */
    public Label getDeck4name() {
        return this.deck4name;
    }

    /**
     * @return 遠征.艦隊4の帰投時間
     */
    public Text getDeck4time() {
        return this.deck4time;
    }

    /**
     * @return 入渠グループ
     */
    public Group getNdockGroup() {
        return this.ndockGroup;
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

    public Label getCondTimerLabel() {
        return this.condTimerLabel;
    }

    public Text getCondTimerTime() {
        return this.condTimerTime;
    }

    public Label getAkashiTimerLabel() {
        return this.akashiTimerLabel;
    }

    public Text getAkashiTimerTime() {
        return this.akashiTimerTime;
    }

    public Label getResultRecordLabel() { return this.resultRecordLabel; }

    public Label getAdmiralExpLabel(){ return this.admiralExpLabel; }

    public Combo getAirbaseCombo(){ return this.airbaseCombo; }

    /**
     * エラーラベルを取得
     * @return
     */
    public Label getErrorLabel() {
        return this.errorLabel;
    }

    /**
     * コンソールを更新します
     * @param message コンソールに表示するメッセージ
     */
    public void printMessage(final String message) {
        getUserlogger().get().info(message);
        if (disableUpdate || this.console.isDisposed())
            return;
        int size = this.console.getItemCount();
        if (size >= MAX_LOG_LINES) {
            this.console.remove(0);
        }
        this.console.add(LOG_DATE_FORMAT.format(new Date()) + "  " + message);
        this.console.setSelection(this.console.getItemCount() - 1);
    }

    // 出撃更新 //

    private List<DockDto> getSortieDocks() {
        boolean[] isSortie = GlobalContext.getIsSortie();
        if (GlobalContext.isCombined() && isSortie[0]) {
            // 連合艦隊
            return Arrays.asList(new DockDto[] {
                    GlobalContext.getDock("1"),
                    GlobalContext.getDock("2")
            });
        }
        for (int i = 0; i < 4; i++) {
            if (isSortie[i]) {
                DockDto dock = GlobalContext.getDock(Integer.toString(i + 1));
                return Arrays.asList(new DockDto[] { dock });
            }
        }
        return null;
    }

    private BattleWindowBase[] getBattleWindowList() {
        return new BattleWindowBase[] {
                this.battleWindowLarge,
                this.battleWindowSmall,
                this.battleShipWindow
        };
    }

    public void startSortie() {
        List<DockDto> sortieDocks = this.getSortieDocks();
        if (sortieDocks != null) {
            for (BattleWindowBase window : this.getBattleWindowList()) {
                window.updateSortieDock(sortieDocks);
            }
        }
    }

    public void endSortie() {
        for (BattleWindowBase window : this.getBattleWindowList()) {
            window.endSortie();
        }
    }

    public void updateSortieDock() {
        List<DockDto> sortieDocks = this.getSortieDocks();
        if (sortieDocks != null) {
            for (BattleWindowBase window : this.getBattleWindowList()) {
                window.updateSortieDock(sortieDocks);
            }
        }
    }

    public void updateMapCell(MapCellDto mapCellDto) {
        for (BattleWindowBase window : this.getBattleWindowList()) {
            window.updateMapCell(mapCellDto);
        }
    }

    public void updateBattle(BattleExDto battleDto) {
        for (BattleWindowBase window : this.getBattleWindowList()) {
            window.updateBattle(battleDto);
        }
    }

    public void updateCalcPracticeExp(PracticeUserDetailDto practiceUserExDto) {
        this.calcPracticeExpWindow.updatePracticeUser(practiceUserExDto);
    }

    public void updateResultRecord(){
        updateResultRecord(AppConfig.get().isMinimumLayout());
    }

    public void updateResultRecord(boolean isMinimumLayout){
        // 戦果
        ResultRecord r = GlobalContext.getResultRecord();
        String resultRecordTooltipText = String.format("今回: +%d exp. / 戦果 %.2f \r\n今日: +%d exp. / 戦果 %.2f \r\n今月: +%d exp. / 戦果 %.2f ",
                r.getAcquiredAdmiralExpOfHalfDay(), r.getAcquiredValueOfHalfDay(),
                r.getAcquiredAdmiralExpOfDay(), r.getAcquiredValueOfDay(),
                r.getAcquiredAdmiralExpOfMonth(), r.getAcquiredValueOfMonth());
        // 縮小表示にした際、大きくレイアウトが崩れるため表示変更
        if (isMinimumLayout) {
            resultRecordLabel.setText("戦果");
        } else {
            resultRecordLabel.setText(String.format("戦果　今回: %8.2f / 今日: %8.2f / 今月: %8.2f",
                    r.getAcquiredValueOfHalfDay(),
                    r.getAcquiredValueOfDay(),
                    r.getAcquiredValueOfMonth()));
        }
        resultRecordLabel.setToolTipText(resultRecordTooltipText);
        admiralExpLabel.setText(String.format("%d exp.", r.getNowAdmiralExp()));
        admiralExpLabel.setToolTipText(resultRecordTooltipText);
    }

    public void updateAirbase(){
        updateAirbase(AppConfig.get().isMinimumLayout());
    }

    public void updateAirbase(boolean isMinimumLayout){
        Optional.ofNullable(GlobalContext.getAirbase()).ifPresent(airbase -> {
            int select = this.airbaseCombo.getSelectionIndex();
            this.airbaseCombo.removeAll();
            airbase.get().forEach((key, value) -> {
                String result = "#" + key + " - " + String.join(" ", value.values().stream()
                        .map(v -> "[" + v.getActionKindString() + "/" + v.getDistance() + "]:" + v.getAirPower().toString())
                        .toArray(String[]::new));
                String miniResult = "#" + key + " - " + String.join(" ", value.values().stream()
                        .map(v -> "[" + v.getActionKindString().substring(0,1) + "/" + v.getDistance() + "]")
                        .toArray(String[]::new));
                this.airbaseCombo.add(isMinimumLayout ? miniResult : result);
            });
            if(airbase.get().size() > 0){
                this.airbaseCombo.select(select < 0 && select < airbase.get().size() ? 0 : select);
            }

            String result = "";
            int area = Integer.parseInt(this.airbaseCombo.getItem(this.airbaseCombo.getSelectionIndex()).replaceAll("#(\\d*).*","$1"));
            Map<Integer, AirbaseDto.AirCorpsDto> airCorps = airbase.get().get(area);
            for(final int airId : airCorps.keySet().stream().sorted().collect(Collectors.toList())){
                AirbaseDto.AirCorpsDto airCorp = airCorps.get(airId);
                result += "#" + area + "-" + airId + " " + "[" + airCorp.getActionKindString() + "]" + airCorp.getName() + "\r\n";
                result += "制空値:" + airCorp.getAirPower() + " 半径:" + airCorp.getDistance() + "\r\n";
                for(final int sqId : airCorp.getSquadrons().keySet().stream().sorted().collect(Collectors.toList())){
                    int now = airCorp.getSquadrons().get(sqId).getCount();
                    int max = airCorp.getSquadrons().get(sqId).getMaxCount();
                    int slotid = airCorp.getSquadrons().get(sqId).getSlotid();
                    if(slotid > 0){
                        ItemDto item = GlobalContext.getItem(slotid);
                        result += "[" + now + "/" + max + "]:" + item.getName()
                                + getLevelString(item.getLevel()) + " " + getAlvString(item.getAlv())
                                + " (半径:" + item.getParam().getDistance() + ")\r\n";
                    } else {
                        result += "(なし)\r\n";
                    }
                }
                result += "\r\n";
            }
            this.airbaseCombo.setToolTipText(result);
        });
    }

    private String getAlvString(int alv){
        switch (alv){
            case 1: return "|";
            case 2: return "||";
            case 3: return "|||";
            case 4: return "/";
            case 5: return "//";
            case 6: return "///";
            case 7: return ">>";
        }
        return "";
    }

    private String getLevelString(int lv){
        return lv > 0 ? "+" + lv : "";
    }

    /**
     * @return fleetWindows
     */
    public FleetWindow[] getFleetWindows() {
        return this.fleetWindows;
    }

    /**
     * @return userlogger
     */
    public static LoggerHolder getUserlogger() {
        return userLogger;
    }
}