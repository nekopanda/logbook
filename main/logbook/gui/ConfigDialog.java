package logbook.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.gui.logic.ColorManager;
import logbook.gui.logic.LayoutLogic;
import logbook.gui.logic.PushNotify;
import logbook.internal.EvaluateExp;
import logbook.internal.SeaExp;
import logbook.util.JIntellitypeWrapper;
import logbook.util.SwtUtils;
import logbook.util.SwtUtils.TableDragAndDropListener;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * 設定画面
 *
 */
public final class ConfigDialog extends Dialog {

    private final ApplicationMain main;
    private Shell shell;
    private Tree tree;
    private Composite composite;
    private ScrolledComposite scrolledComposite;

    /**
     * Create the dialog.
     * @param main
     */
    public ConfigDialog(ApplicationMain main) {
        super(main.getShell(), SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE);
        this.main = main;
        this.setText("設定");
    }

    /**
     * Open the dialog.
     */
    public void open() {
        this.createContents();
        this.shell.open();
        this.shell.layout();
    }

    /**
     * ペインを選択
     * @param paneName
     */
    public void selectPane(String paneName) {
        for (TreeItem item : this.tree.getItems()) {
            if (item.getText().equals(paneName)) {
                this.tree.setSelection(item);
                this.selectPane((Composite) item.getData());
                break;
            }
        }
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setSize(SwtUtils.DPIAwareSize(new Point(600, 400)));
        this.shell.setText(this.getText());
        this.shell.setLayout(new GridLayout(1, false));

        SashForm sashForm = new SashForm(this.shell, SWT.SMOOTH);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        this.tree = new Tree(sashForm, SWT.BORDER);
        this.tree.addSelectionListener(new TreeSelectionAdapter(this));

        this.scrolledComposite = new ScrolledComposite(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        this.scrolledComposite.setExpandHorizontal(true);
        this.scrolledComposite.setExpandVertical(true);

        this.composite = new Composite(this.scrolledComposite, SWT.NONE);
        this.composite.setLayout(new GridLayout(1, false));

        // ツリーメニュー
        TreeItem connection = new TreeItem(this.tree, SWT.NONE);
        connection.setText("通信");
        Composite compositeConnection = new Composite(this.composite, SWT.NONE);
        connection.setData(compositeConnection);
        TreeItem systemroot = new TreeItem(this.tree, SWT.NONE);
        systemroot.setText("一般");
        Composite compositeSystem = new Composite(this.composite, SWT.NONE);
        systemroot.setData(compositeSystem);
        TreeItem reportTab = new TreeItem(this.tree, SWT.NONE);
        reportTab.setText("報告書");
        Composite compositeReport = new Composite(this.composite, SWT.NONE);
        reportTab.setData(compositeReport);
        TreeItem fleettab = new TreeItem(this.tree, SWT.NONE);
        fleettab.setText("艦隊タブ");
        Composite compositeFleetTab = new Composite(this.composite, SWT.NONE);
        fleettab.setData(compositeFleetTab);
        TreeItem fleetdetail = new TreeItem(fleettab, SWT.NONE);
        fleetdetail.setText("詳細");
        Composite compositeFleetDetail = new Composite(this.composite, SWT.NONE);
        fleetdetail.setData(compositeFleetDetail);
        TreeItem notify = new TreeItem(this.tree, SWT.NONE);
        notify.setText("通知");
        Composite compositeNotify = new Composite(this.composite, SWT.NONE);
        notify.setData(compositeNotify);
        TreeItem pushnotify = new TreeItem(this.tree, SWT.NONE);
        pushnotify.setText("Push通知");
        Composite compositePushNotify = new Composite(this.composite, SWT.NONE);
        pushnotify.setData(compositePushNotify);
        TreeItem capture = new TreeItem(this.tree, SWT.NONE);
        capture.setText("キャプチャ");
        Composite compositeCapture = new Composite(this.composite, SWT.NONE);
        capture.setData(compositeCapture);
        TreeItem chart = new TreeItem(this.tree, SWT.NONE);
        chart.setText("資材チャート");
        Composite compositeChart = new Composite(this.composite, SWT.NONE);
        chart.setData(compositeChart);
        TreeItem window = new TreeItem(this.tree, SWT.NONE);
        window.setText("ウィンドウ");
        Composite compositeWindow = new Composite(this.composite, SWT.NONE);
        window.setData(compositeWindow);
        TreeItem toolWin = new TreeItem(this.tree, SWT.NONE);
        toolWin.setText("ツール");
        Composite compositeToolWin = new Composite(this.composite, SWT.NONE);
        toolWin.setData(compositeToolWin);
        TreeItem development = new TreeItem(this.tree, SWT.NONE);
        development.setText("Development");
        Composite compositeDevelopment = new Composite(this.composite, SWT.NONE);
        development.setData(compositeDevelopment);

        fleettab.setExpanded(true);

        // 通信
        compositeConnection.setLayout(new GridLayout(4, false));

        Label label = new Label(compositeConnection, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("受信ポート");

        final Text listenport = new Text(compositeConnection, SWT.BORDER);
        GridData gdListenport = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdListenport.widthHint = SwtUtils.DPIAwareWidth(90);
        listenport.setLayoutData(gdListenport);
        listenport.setText(Integer.toString(AppConfig.get().getListenPort()));
        new Label(compositeConnection, SWT.NONE);
        new Label(compositeConnection, SWT.NONE);

        new Label(compositeConnection, SWT.NONE);
        final Button onlyFromLocalhost = new Button(compositeConnection, SWT.CHECK);
        onlyFromLocalhost.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        onlyFromLocalhost.setText("他のマシンからの要求は拒否する");
        onlyFromLocalhost.setSelection(AppConfig.get().isAllowOnlyFromLocalhost());
        onlyFromLocalhost.setToolTipText("セキュリティの観点から通常は拒否設定にしてください。\n" +
                "他のマシンから接続するなど特別な事情がある場合のみOFFにします");

        new Label(compositeConnection, SWT.NONE);
        final Button closeOutsidePort = new Button(compositeConnection, SWT.CHECK);
        closeOutsidePort.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        closeOutsidePort.setText("外向けポートは閉じておく");
        closeOutsidePort.setSelection(AppConfig.get().isCloseOutsidePort());
        closeOutsidePort
                .setToolTipText("1.6.0からデフォルト動作になりました。ただし、専ブラなどでポートを開けておく必要がある場合があります。\n" +
                        "マシンが直接インターネットに接続されている場合、ポートを開けると若干セキュリティが低下します");
        closeOutsidePort.setEnabled(onlyFromLocalhost.getSelection());

        onlyFromLocalhost.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                closeOutsidePort.setEnabled(onlyFromLocalhost.getSelection());
            }
        });

        final Button useProxyButton = new Button(compositeConnection, SWT.CHECK);
        useProxyButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
        useProxyButton.setText("接続にプロキシを使用する");
        useProxyButton.setSelection(AppConfig.get().isUseProxy());

        Label proxyHostLabel = new Label(compositeConnection, SWT.NONE);
        proxyHostLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        proxyHostLabel.setText("サーバ:");

        final Text proxyHostText = new Text(compositeConnection, SWT.BORDER);
        GridData gdProxyHostText = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdProxyHostText.widthHint = SwtUtils.DPIAwareWidth(100);
        proxyHostText.setLayoutData(gdProxyHostText);
        proxyHostText.setText(AppConfig.get().getProxyHost());

        Label proxyPortLabel = new Label(compositeConnection, SWT.NONE);
        proxyPortLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        proxyPortLabel.setText("ポート:");

        final Spinner proxyPortSpinner = new Spinner(compositeConnection, SWT.BORDER);
        proxyPortSpinner.setMaximum(65535);
        proxyPortSpinner.setMinimum(1);
        proxyPortSpinner.setSelection(AppConfig.get().getProxyPort());
        GridData gdProxyPortSpinner = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdProxyPortSpinner.widthHint = SwtUtils.DPIAwareWidth(55);
        proxyPortSpinner.setLayoutData(gdProxyPortSpinner);

        final Button sendDatabaseButton = new Button(compositeConnection, SWT.CHECK);
        sendDatabaseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
        sendDatabaseButton.setText("艦これ統計データベースへデータを送信する");
        sendDatabaseButton.setSelection(AppConfig.get().isSendDatabase());

        Label accessKeyLabel = new Label(compositeConnection, SWT.NONE);
        accessKeyLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        accessKeyLabel.setText("アクセスキー:");

        final Text accessKeyText = new Text(compositeConnection, SWT.BORDER);
        GridData gdAccessKeyText = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
        // gdAccessKeyText.widthHint = SwtUtils.DPIAwareWidth(300);
        accessKeyText.setLayoutData(gdAccessKeyText);
        accessKeyText.setText(AppConfig.get().getAccessKey());

        final Button databaseLogButton = new Button(compositeConnection, SWT.CHECK);
        databaseLogButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
        databaseLogButton.setText("データベースへの送信をログ出力する");
        databaseLogButton.setSelection(AppConfig.get().isDatabaseSendLog());

        // システム タブ
        compositeSystem.setLayout(new GridLayout(3, false));

        final Button hidewindow = new Button(compositeSystem, SWT.CHECK);
        hidewindow.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        hidewindow.setText("最小化時にタスクトレイに格納");
        hidewindow.setSelection(AppConfig.get().isHideWindow());

        final Button closewindow = new Button(compositeSystem, SWT.CHECK);
        closewindow.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        closewindow.setText("縮小表示の時は開いているウィンドウを閉じる");
        closewindow.setSelection(AppConfig.get().isCloseWhenMinimized());

        final Button ontop;
        if (WindowBase.isCommonTopMostEnabled()) { // 右クリックから設定できる場合は表示しない
            ontop = new Button(compositeSystem, SWT.CHECK);
            ontop.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
            ontop.setText("最前面に表示する*");
            ontop.setSelection(AppConfig.get().isOnTop());
        }
        else {
            ontop = null;
        }

        final Button checkUpdate = new Button(compositeSystem, SWT.CHECK);
        checkUpdate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        checkUpdate.setText("起動時にアップデートチェック*");
        checkUpdate.setSelection(AppConfig.get().isUpdateCheck());

        final Button checkDoit = new Button(compositeSystem, SWT.CHECK);
        checkDoit.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        checkDoit.setText("終了時に確認する");
        checkDoit.setSelection(AppConfig.get().isCheckDoit());

        final Button nameOnTitlebar = new Button(compositeSystem, SWT.CHECK);
        nameOnTitlebar.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        nameOnTitlebar.setText("タイトルバーに提督名を表示する");
        nameOnTitlebar.setSelection(AppConfig.get().isNameOnTitlebar());

        final Button colorSupport = new Button(compositeSystem, SWT.CHECK);
        colorSupport.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        colorSupport.setText("色覚サポート*");
        colorSupport.setSelection(AppConfig.get().isColorSupport());

        final Combo systemWideShortcutKey;
        if (JIntellitypeWrapper.getInstance() != null) {
            final Label systemWideShortcutKeyLabel = new Label(compositeSystem, SWT.NONE);
            systemWideShortcutKeyLabel.setText("航海日誌をアクティブにするホットキー");
            systemWideShortcutKey = new Combo(compositeSystem, SWT.READ_ONLY);
            systemWideShortcutKey.add("なし");
            systemWideShortcutKey.add("Ctrl+Shift+Z");
            systemWideShortcutKey.add("Ctrl+Alt+Z");
            systemWideShortcutKey.select(AppConfig.get().getSystemWideHotKey());
        }
        else {
            systemWideShortcutKey = null;
        }

        // 報告書 タブ
        compositeReport.setLayout(new GridLayout(3, false));

        Label label8 = new Label(compositeReport, SWT.NONE);
        label8.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label8.setText("報告書の保存先");

        final Text reportDir = new Text(compositeReport, SWT.BORDER);
        GridData gdReportDir = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdReportDir.widthHint = SwtUtils.DPIAwareWidth(120);
        reportDir.setLayoutData(gdReportDir);
        reportDir.setText(AppConfig.get().getReportPath());

        Button reportSavedirBtn = new Button(compositeReport, SWT.NONE);
        reportSavedirBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        reportSavedirBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(ConfigDialog.this.shell);
                dialog.setMessage("保存先を指定して下さい");
                String path = dialog.open();
                if (path != null) {
                    reportDir.setText(path);
                }
            }
        });
        reportSavedirBtn.setText("選択...");

        Label label82 = new Label(compositeReport, SWT.NONE);
        label82.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label82.setText("戦闘履歴の保存先*");

        final Text battlelogDir = new Text(compositeReport, SWT.BORDER);
        GridData gdBattlelogDir = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdBattlelogDir.widthHint = SwtUtils.DPIAwareWidth(120);
        battlelogDir.setLayoutData(gdBattlelogDir);
        battlelogDir.setText(AppConfig.get().getBattleLogPath());

        Button battleLogdirBtn = new Button(compositeReport, SWT.NONE);
        battleLogdirBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        battleLogdirBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(ConfigDialog.this.shell);
                dialog.setMessage("保存先を指定して下さい");
                String path = dialog.open();
                if (path != null) {
                    battlelogDir.setText(path);
                }
            }
        });
        battleLogdirBtn.setText("選択...");

        Label materialintervallabel = new Label(compositeReport, SWT.NONE);
        materialintervallabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        materialintervallabel.setText("資材ログ保存間隔(秒)");

        final Spinner materialintervalSpinner = new Spinner(compositeReport, SWT.BORDER);
        materialintervalSpinner.setMaximum(60 * 60 * 24);
        materialintervalSpinner.setMinimum(10);
        materialintervalSpinner.setSelection(AppConfig.get().getMaterialLogInterval());
        GridData gdMaterialIntervalSpinner = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdMaterialIntervalSpinner.widthHint = SwtUtils.DPIAwareWidth(55);
        materialintervalSpinner.setLayoutData(gdMaterialIntervalSpinner);
        new Label(compositeReport, SWT.NONE);

        final Button detailMaterial = new Button(compositeReport, SWT.CHECK);
        detailMaterial.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        detailMaterial.setText("遠征帰還時や開発・建造時に資材ログ出力する");
        detailMaterial.setSelection(AppConfig.get().isMaterialLogDetail());

        Label loadReport = new Label(compositeReport, SWT.NONE);
        loadReport.setText("報告書読み込み（起動時にCSVファイルを全て読み込みます）");
        loadReport.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

        final Button loadCreateShipLog = new Button(compositeReport, SWT.CHECK);
        loadCreateShipLog.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        loadCreateShipLog.setText("建造報告書を読み込む*");
        loadCreateShipLog.setSelection(AppConfig.get().isLoadCreateShipLog());

        final Button loadCreateItemLog = new Button(compositeReport, SWT.CHECK);
        loadCreateItemLog.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        loadCreateItemLog.setText("開発報告書を読み込む*");
        loadCreateItemLog.setSelection(AppConfig.get().isLoadCreateItemLog());

        final Button loadMissionLog = new Button(compositeReport, SWT.CHECK);
        loadMissionLog.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        loadMissionLog.setText("遠征報告書を読み込む*");
        loadMissionLog.setSelection(AppConfig.get().isLoadMissionLog());

        // 艦隊タブ タブ
        compositeFleetTab.setLayout(new GridLayout(2, false));

        final Button balloonBybadlyDamage = new Button(compositeFleetTab, SWT.CHECK);
        balloonBybadlyDamage.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        balloonBybadlyDamage.setText("大破でバルーンツールチップを表示");
        balloonBybadlyDamage.setSelection(AppConfig.get().isBalloonBybadlyDamage());

        final Button visibleOnReturnMission = new Button(compositeFleetTab, SWT.CHECK);
        visibleOnReturnMission.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        visibleOnReturnMission.setText("遠征からの帰還時に母港タブを表示");
        visibleOnReturnMission.setSelection(AppConfig.get().isVisibleOnReturnMission());

        final Button visibleOnReturnBathwater = new Button(compositeFleetTab, SWT.CHECK);
        visibleOnReturnBathwater.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        visibleOnReturnBathwater.setText("お風呂から上がる時に母港タブを表示");
        visibleOnReturnBathwater.setSelection(AppConfig.get().isVisibleOnReturnBathwater());

        Label seikuLabel = new Label(compositeFleetTab, SWT.NONE);
        seikuLabel.setText("制空計算式");
        seikuLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

        final Combo seikuCombo = new Combo(compositeFleetTab, SWT.READ_ONLY);
        seikuCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        seikuCombo.add("A.艦載機素の制空値");
        seikuCombo.add("B.制空推定値範囲");
        seikuCombo.add("C.制空推定値範囲(艦載機素の制空値 + 熟練度ボーナス推定値)");
        seikuCombo.add("D.制空推定値中央");
        seikuCombo.add("E.制空推定値中央(艦載機素の制空値 + 熟練度ボーナス推定値)");
        seikuCombo.select(AppConfig.get().getSeikuMethod());

        Label sakutekiLabel = new Label(compositeFleetTab, SWT.NONE);
        sakutekiLabel.setText("索敵計算式");
        sakutekiLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

        final Combo sakutekiCombo = new Combo(compositeFleetTab, SWT.READ_ONLY);
        sakutekiCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        sakutekiCombo.add("A.艦隊素の索敵値 + 装備の索敵値");
        sakutekiCombo.add("B.判定式(33)(艦隊素の索敵分 + 装備分 - 提督Lv分 + 艦隊空き数分)");
        sakutekiCombo.add("C.判定式(33)(2-5式(秋))");
        sakutekiCombo.add("D.ほっぽアルファVer2.0.1(艦隊素の索敵分 + 装備分 - 提督Lv分)");
        sakutekiCombo.add("F.ほっぽアルファVer2.0.1(2-5式(秋))");
        sakutekiCombo.add("F.2-5式(秋)(艦隊素の索敵分 + 装備分 - 提督Lv分)");
        sakutekiCombo.add("G.装備込みの艦隊索敵値合計(2-5式(秋))");
        sakutekiCombo.add("H.2-5式(旧)(偵察機×2 + 電探 + √(装備込みの艦隊索敵値-偵察機-電探))");
        sakutekiCombo.add("I.装備込みの艦隊索敵値(2-5式(旧))");
        sakutekiCombo.select(AppConfig.get().getSakutekiMethodV4());

        Label mainLog = new Label(compositeFleetTab, SWT.NONE);
        mainLog.setText("母港タブのログ");
        mainLog.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

        final Button printSortieLog = new Button(compositeFleetTab, SWT.CHECK);
        printSortieLog.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        printSortieLog.setText("マップや勝利判定をログに表示");
        printSortieLog.setSelection(AppConfig.get().isPrintSortieLog());

        final Button printSunkLog = new Button(compositeFleetTab, SWT.CHECK);
        printSunkLog.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        printSunkLog.setText("艦娘の轟沈をログに表示");
        printSunkLog.setSelection(AppConfig.get().isPrintSunkLog());

        final Button printUpdateLog = new Button(compositeFleetTab, SWT.CHECK);
        printUpdateLog.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        printUpdateLog.setText("更新系ログを表示");
        printUpdateLog.setSelection(AppConfig.get().isPrintUpdateLog());

        final Button printDropLog = new Button(compositeFleetTab, SWT.CHECK);
        printDropLog.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        printDropLog.setText("ドロップをログに表示");
        printDropLog.setSelection(AppConfig.get().isPrintDropLog());

        // 艦隊タブ タブ
        compositeFleetDetail.setLayout(new GridLayout(1, false));

        Group leveling = new Group(compositeFleetDetail, SWT.NONE);
        leveling.setText("レベリング");
        leveling.setLayout(new RowLayout());
        leveling.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Button displaycount = new Button(leveling, SWT.CHECK);
        displaycount.setText("回数を表示");
        displaycount.setSelection(AppConfig.get().isDisplayCount());

        Label label9 = new Label(leveling, SWT.NONE);
        label9.setText("海域");
        final Combo seacombo = new Combo(leveling, SWT.READ_ONLY);
        int count = 0;
        for (Entry<String, Integer> entry : SeaExp.get().entrySet()) {
            seacombo.add(entry.getKey());
            if (entry.getKey().equals(AppConfig.get().getDefaultSea())) {
                seacombo.select(count);
            }
            count++;
        }
        Label label10 = new Label(leveling, SWT.NONE);
        label10.setText("評価");
        final Combo evalcombo = new Combo(leveling, SWT.READ_ONLY);
        count = 0;
        for (Entry<String, Double> entry : EvaluateExp.get().entrySet()) {
            evalcombo.add(entry.getKey());
            if (entry.getKey().equals(AppConfig.get().getDefaultEvaluate())) {
                evalcombo.select(count);
            }
            count++;
        }

        final Button warnByNeedSupply = new Button(compositeFleetDetail, SWT.CHECK);
        warnByNeedSupply.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        warnByNeedSupply.setText("補給不足で警告アイコン表示");
        warnByNeedSupply.setSelection(AppConfig.get().isWarnByNeedSupply());

        final Button warnByCondState = new Button(compositeFleetDetail, SWT.CHECK);
        warnByCondState.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        warnByCondState.setText("疲労状態で警告アイコン表示");
        warnByCondState.setSelection(AppConfig.get().isWarnByCondState());

        final Button warnByHalfDamage = new Button(compositeFleetDetail, SWT.CHECK);
        warnByHalfDamage.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        warnByHalfDamage.setText("中破で警告アイコン表示");
        warnByHalfDamage.setSelection(AppConfig.get().isWarnByHalfDamage());

        final Button fatalBybadlyDamage = new Button(compositeFleetDetail, SWT.CHECK);
        fatalBybadlyDamage.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        fatalBybadlyDamage.setText("大破で致命的アイコン表示");
        fatalBybadlyDamage.setSelection(AppConfig.get().isFatalBybadlyDamage());

        final Button useMonoIcon = new Button(compositeFleetDetail, SWT.CHECK);
        useMonoIcon.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        useMonoIcon.setText("モノクロアイコンを使用");
        useMonoIcon.setSelection(AppConfig.get().isMonoIcon());

        final Button showGunAndBulge = new Button(compositeFleetDetail, SWT.CHECK);
        showGunAndBulge.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        showGunAndBulge.setText("機銃やバルジの装備数を表示");
        showGunAndBulge.setSelection(AppConfig.get().isShowGunAndBulge());

        final Button showCondTimer = new Button(compositeFleetDetail, SWT.CHECK);
        showCondTimer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        showCondTimer.setText("疲労回復タイマーを表示");
        showCondTimer.setSelection(AppConfig.get().isShowCondTimer());

        final Button showAkashiTimer = new Button(compositeFleetDetail, SWT.CHECK);
        showAkashiTimer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        showAkashiTimer.setText("泊地修理タイマーを表示");
        showAkashiTimer.setSelection(AppConfig.get().isShowAkashiTimer());

        Composite akashiFormatBase = new Composite(compositeFleetDetail, SWT.NONE);
        akashiFormatBase.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        akashiFormatBase.setLayout(new RowLayout(SWT.HORIZONTAL));

        Label akashiFormatLabel = new Label(akashiFormatBase, SWT.NONE);
        akashiFormatLabel.setText("表示");

        final Combo akashiFormatCombo = new Combo(akashiFormatBase, SWT.READ_ONLY);
        akashiFormatCombo.add("A.全回復までの時間");
        akashiFormatCombo.add("B.次の回復までの時間");
        akashiFormatCombo.add("C.AとBを交互に表示");
        akashiFormatCombo.select(AppConfig.get().getAkashiTimerFormat());

        // 通知
        compositeNotify.setLayout(new GridLayout(3, false));

        Label label3 = new Label(compositeNotify, SWT.NONE);
        label3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label3.setText("音量(%)");

        final Text soundlevel = new Text(compositeNotify, SWT.BORDER);
        GridData gdSoundlevel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdSoundlevel.widthHint = SwtUtils.DPIAwareWidth(90);
        soundlevel.setLayoutData(gdSoundlevel);
        soundlevel.setText(Integer.toString((int) (AppConfig.get().getSoundLevel() * 100)));
        new Label(compositeNotify, SWT.NONE);

        final Button balloon = new Button(compositeNotify, SWT.CHECK);
        balloon.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        balloon.setText("タイマー類をバルーンで通知する");
        balloon.setSelection(AppConfig.get().isUseBalloon());

        Label condLabel1 = new Label(compositeNotify, SWT.NONE);
        condLabel1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        condLabel1.setText("回復判定疲労度");

        final Spinner condSpinner = new Spinner(compositeNotify, SWT.BORDER);
        condSpinner.setMaximum(49);
        condSpinner.setMinimum(0);
        condSpinner.setSelection(AppConfig.get().getOkCond());
        GridData gdCondSpinner = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdCondSpinner.widthHint = SwtUtils.DPIAwareWidth(55);
        condSpinner.setLayoutData(gdCondSpinner);
        new Label(compositeNotify, SWT.NONE);

        final Button condOnlyMain = new Button(compositeNotify, SWT.CHECK);
        condOnlyMain.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        condOnlyMain.setText("第一艦隊以外の疲労回復通知を行わない");
        condOnlyMain.setSelection(AppConfig.get().isNoticeCondOnlyMainFleet());

        final Button akashiFirstStep = new Button(compositeNotify, SWT.CHECK);
        akashiFirstStep.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        akashiFirstStep.setText("泊地修理開始から20分経過を通知する");
        akashiFirstStep.setSelection(AppConfig.get().isAkashiNotifyFirstStep());

        final Button akashiEveryStep = new Button(compositeNotify, SWT.CHECK);
        akashiEveryStep.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        akashiEveryStep.setText("泊地修理中艦娘のHPが1回復する度に通知する");
        akashiEveryStep.setSelection(AppConfig.get().isAkashiNotifyEveryStep());

        final Button remind = new Button(compositeNotify, SWT.CHECK);
        remind.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        remind.setText("遠征の通知をリマインドする");
        remind.setSelection(AppConfig.get().isMissionRemind());

        Label intervallabel = new Label(compositeNotify, SWT.NONE);
        intervallabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        intervallabel.setText("間隔(秒)");

        final Spinner intervalSpinner = new Spinner(compositeNotify, SWT.BORDER);
        intervalSpinner.setMaximum(60 * 60);
        intervalSpinner.setMinimum(10);
        intervalSpinner.setSelection(AppConfig.get().getRemindInterbal());
        GridData gdIntervalSpinner = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdIntervalSpinner.widthHint = SwtUtils.DPIAwareWidth(55);
        intervalSpinner.setLayoutData(gdIntervalSpinner);

        new Label(compositeNotify, SWT.NONE);

        final Button taskbar = new Button(compositeNotify, SWT.CHECK);
        taskbar.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        taskbar.setText("母港の空きをタスクバーで通知する");
        taskbar.setSelection(AppConfig.get().isUseTaskbarNotify());

        Label fullyLabel1 = new Label(compositeNotify, SWT.NONE);
        fullyLabel1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        fullyLabel1.setText("母港の空きが");

        final Spinner fullySpinner = new Spinner(compositeNotify, SWT.BORDER);
        fullySpinner.setMaximum(Math.max(100, GlobalContext.maxChara()));
        fullySpinner.setMinimum(0);
        fullySpinner.setSelection(AppConfig.get().getNotifyFully());
        GridData gdFullySpinner = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdFullySpinner.widthHint = SwtUtils.DPIAwareWidth(55);
        fullySpinner.setLayoutData(gdFullySpinner);

        Label fullyLabel2 = new Label(compositeNotify, SWT.NONE);
        fullyLabel2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        fullyLabel2.setText("以下で警告表示");

        final Button shipFullBalloon = new Button(compositeNotify, SWT.CHECK);
        shipFullBalloon.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        shipFullBalloon.setSelection(AppConfig.get().isEnableShipFullBalloonNotify());
        shipFullBalloon.setText("母港の空きが");

        final Spinner shipFullSpinner = new Spinner(compositeNotify, SWT.BORDER);
        shipFullSpinner.setMaximum(Math.max(100, GlobalContext.maxChara()));
        shipFullSpinner.setMinimum(0);
        shipFullSpinner.setSelection(AppConfig.get().getShipFullBalloonNotify());
        shipFullSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label fullyLabel3 = new Label(compositeNotify, SWT.NONE);
        fullyLabel3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        fullyLabel3.setText("以下でバルーン通知");

        final Button itemFullBalloon = new Button(compositeNotify, SWT.CHECK);
        itemFullBalloon.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        itemFullBalloon.setSelection(AppConfig.get().isEnableItemFullBalloonNotify());
        itemFullBalloon.setText("装備の空きが");

        final Spinner itemFullSpinner = new Spinner(compositeNotify, SWT.BORDER);
        itemFullSpinner.setMaximum(Math.max(100, GlobalContext.maxChara()));
        itemFullSpinner.setMinimum(0);
        itemFullSpinner.setSelection(AppConfig.get().getItemFullBalloonNotify());
        itemFullSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label fullyLabel4 = new Label(compositeNotify, SWT.NONE);
        fullyLabel4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        fullyLabel4.setText("以下でバルーン通知");

        // キャプチャ タブ
        compositeCapture.setLayout(new GridLayout(3, false));

        Label label4 = new Label(compositeCapture, SWT.NONE);
        label4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label4.setText("保存先");

        final Text captureDir = new Text(compositeCapture, SWT.BORDER);
        GridData gdCaptureDir = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdCaptureDir.widthHint = SwtUtils.DPIAwareWidth(120);
        captureDir.setLayoutData(gdCaptureDir);
        captureDir.setText(AppConfig.get().getCapturePath());

        Button savedirBtn = new Button(compositeCapture, SWT.NONE);
        savedirBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        savedirBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(ConfigDialog.this.shell);
                dialog.setMessage("保存先を指定して下さい");
                String path = dialog.open();
                if (path != null) {
                    captureDir.setText(path);
                }
            }
        });
        savedirBtn.setText("選択...");

        Label label5 = new Label(compositeCapture, SWT.NONE);
        label5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label5.setText("フォーマット");

        final Combo imageformatCombo = new Combo(compositeCapture, SWT.READ_ONLY);
        imageformatCombo.setItems(new String[] { "jpg", "png" });
        imageformatCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        imageformatCombo.select(0);
        for (int i = 0; i < imageformatCombo.getItems().length; i++) {
            if (AppConfig.get().getImageFormat().equals(imageformatCombo.getItem(i))) {
                imageformatCombo.select(i);
                break;
            }
        }
        new Label(compositeCapture, SWT.NONE);

        final Button createDateFolder = new Button(compositeCapture, SWT.CHECK);
        createDateFolder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        createDateFolder.setText("日付のフォルダを作成する");
        createDateFolder.setSelection(AppConfig.get().isCreateDateFolder());

        // 資材チャート タブ
        compositeChart.setLayout(new GridLayout(3, false));

        final Label fuel = this.createColorSetting(compositeChart, "燃料の色■",
                AppConfig.get().getFuelColor(), AppConstants.CHART_COLOR_TABLE[0]);
        final Label ammo = this.createColorSetting(compositeChart, "弾薬の色■",
                AppConfig.get().getAmmoColor(), AppConstants.CHART_COLOR_TABLE[1]);
        final Label metal = this.createColorSetting(compositeChart, "鋼材の色■",
                AppConfig.get().getMetalColor(), AppConstants.CHART_COLOR_TABLE[2]);
        final Label bauxite = this.createColorSetting(compositeChart, "ボーキの色■",
                AppConfig.get().getBauxiteColor(), AppConstants.CHART_COLOR_TABLE[3]);
        final Label burner = this.createColorSetting(compositeChart, "バーナーの色■",
                AppConfig.get().getBurnerColor(), AppConstants.CHART_COLOR_TABLE[4]);
        final Label bucket = this.createColorSetting(compositeChart, "バケツの色■",
                AppConfig.get().getBucketColor(), AppConstants.CHART_COLOR_TABLE[5]);
        final Label research = this.createColorSetting(compositeChart, "開発資材の色■",
                AppConfig.get().getResearchColor(), AppConstants.CHART_COLOR_TABLE[6]);
        final Label screw = this.createColorSetting(compositeChart, "ネジの色■",
                AppConfig.get().getScrewColor(), AppConstants.CHART_COLOR_TABLE[6]);

        // ウィンドウ
        compositeWindow.setLayout(new GridLayout(3, false));

        final Button enableMoveWithDD = new Button(compositeWindow, SWT.CHECK);
        enableMoveWithDD.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        enableMoveWithDD.setSelection(AppConfig.get().isEnableMoveWithDD());
        enableMoveWithDD.setText("タイトルバー以外でもドラッグ&&ドロップで移動できるようにする*");

        final Button noMenubar = new Button(compositeWindow, SWT.CHECK);
        noMenubar.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        noMenubar.setSelection(AppConfig.get().isNoMenubar());
        noMenubar.setText("メニューバーを消してポップアップメニュー化する*");

        final Button diableWindowMenu = new Button(compositeWindow, SWT.CHECK);
        diableWindowMenu.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        diableWindowMenu.setSelection(AppConfig.get().isDisableWindowMenu());
        diableWindowMenu.setText("ウィンドウメニューを無効化*");

        final Button toggleToolButton = new Button(compositeWindow, SWT.CHECK);
        toggleToolButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        toggleToolButton.setSelection(AppConfig.get().isToggleToolButton());
        toggleToolButton.setText("ツールウィンドウのボタンをトグル方式にする");

        final Button showSubwindowHost = new Button(compositeWindow, SWT.CHECK);
        showSubwindowHost.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        showSubwindowHost.setSelection(AppConfig.get().isShowSubwindowHost());
        showSubwindowHost.setText("サブウィンドウホストを表示する(Windows10デスクトップ切り替え対応)*");

        Group opaqueIntervalGroup = new Group(compositeWindow, SWT.NONE);
        opaqueIntervalGroup.setText("マウスが離れてから元の透明度に戻るまでの時間");
        opaqueIntervalGroup.setLayout(new GridLayout(2, false));
        opaqueIntervalGroup.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

        final Spinner opaqueIntervalSpinner = new Spinner(opaqueIntervalGroup, SWT.BORDER);
        opaqueIntervalSpinner.setMaximum(60 * 60 * 10);
        opaqueIntervalSpinner.setMinimum(0);
        opaqueIntervalSpinner.setSelection(AppConfig.get().getOpaqueInterval());
        GridData gdopaqueIntervalSpinner = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdopaqueIntervalSpinner.widthHint = SwtUtils.DPIAwareWidth(65);
        opaqueIntervalSpinner.setLayoutData(gdopaqueIntervalSpinner);

        Label opaqueIntervalSuffix = new Label(opaqueIntervalGroup, SWT.NONE);
        opaqueIntervalSuffix.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        opaqueIntervalSuffix.setText("x0.1秒");

        Group shipTableNameGroup = new Group(compositeWindow, SWT.NONE);
        shipTableNameGroup.setText("艦娘一覧ウィンドウの名前設定");
        shipTableNameGroup.setLayout(new GridLayout(3, false));
        shipTableNameGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

        //Label shipTableName = new Label(compositeWindow, SWT.NONE);
        //shipTableName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        //shipTableName.setText("艦娘一覧ウィンドウの名前設定");

        final Text[] shipTableNameText = new Text[4];
        for (int i = 0; i < 4; ++i) {
            Label shipTableLabel = new Label(shipTableNameGroup, SWT.NONE);
            shipTableLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
            shipTableLabel.setText(String.valueOf(i + 1) + ": ");
            shipTableNameText[i] = new Text(shipTableNameGroup, SWT.BORDER);
            shipTableNameText[i].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
            shipTableNameText[i].setText(AppConfig.get().getShipTableNames()[i]);
        }

        // Push通知タブ
        compositePushNotify.setLayout(new GridLayout(3, false));

        final Button prowl = new Button(compositePushNotify, SWT.CHECK);
        prowl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        prowl.setText("ProwlによるPush通知");
        prowl.setSelection(AppConfig.get().getNotifyProwl());

        Label label11 = new Label(compositePushNotify, SWT.NONE);
        label11.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label11.setText("Prowl APIKey");

        final Text prowlAPIKey = new Text(compositePushNotify, SWT.BORDER);
        GridData gdprowlAPIKey = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        gdprowlAPIKey.widthHint = SwtUtils.DPIAwareWidth(200);
        prowlAPIKey.setLayoutData(gdprowlAPIKey);
        prowlAPIKey.setText(AppConfig.get().getProwlAPIKey());

        final Button nma = new Button(compositePushNotify, SWT.CHECK);
        nma.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        nma.setText("Notify My Android (NMA)によるPush通知");
        nma.setSelection(AppConfig.get().getNotifyNMA());

        Label label12 = new Label(compositePushNotify, SWT.NONE);
        label12.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label12.setText("NMA APIKey");

        final Text nmaAPIKey = new Text(compositePushNotify, SWT.BORDER);
        GridData gdnmaAPIKey = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        gdnmaAPIKey.widthHint = SwtUtils.DPIAwareWidth(200);
        nmaAPIKey.setLayoutData(gdnmaAPIKey);
        nmaAPIKey.setText(AppConfig.get().getNMAAPIKey());

        final Button imkayac = new Button(compositePushNotify, SWT.CHECK);
        imkayac.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        imkayac.setText("ImKayac によるPush通知(秘密鍵優先)");
        imkayac.setSelection(AppConfig.get().getNotifyImKayac());

        Label label14 = new Label(compositePushNotify, SWT.NONE);
        label14.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label14.setText("ImKayac UserName");

        final Text imkayacUserName = new Text(compositePushNotify, SWT.BORDER);
        GridData gdimkayacUserName = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        gdimkayacUserName.widthHint = SwtUtils.DPIAwareWidth(200);
        imkayacUserName.setLayoutData(gdimkayacUserName);
        imkayacUserName.setText(AppConfig.get().getImKayacUserName());

        Label label15 = new Label(compositePushNotify, SWT.NONE);
        label15.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label15.setText("ImKayac パスワード");

        final Text imkayacPasswd = new Text(compositePushNotify, SWT.PASSWORD | SWT.BORDER);
        GridData gdimkayacPasswd = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        gdimkayacPasswd.widthHint = SwtUtils.DPIAwareWidth(200);
        imkayacPasswd.setLayoutData(gdimkayacPasswd);
        imkayacPasswd.setText(AppConfig.get().getImKayacPasswd());

        Label label16 = new Label(compositePushNotify, SWT.NONE);
        label16.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label16.setText("ImKayac 秘密鍵");

        final Text imkayacPrivateKey = new Text(compositePushNotify, SWT.BORDER);
        GridData gdimkayacPrivateKey = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        gdimkayacPrivateKey.widthHint = SwtUtils.DPIAwareWidth(200);
        imkayacPrivateKey.setLayoutData(gdimkayacUserName);
        imkayacPrivateKey.setText(AppConfig.get().getImKayacPrivateKey());

        String pushName[] = new String[] {
                "遠征帰投を通知",
                "入渠完了を通知",
                "泊地修理完了を通知",
                "疲労回復を通知"
        };

        final Button pushNotifyMission = new Button(compositePushNotify, SWT.CHECK);
        final Combo pushPriorityMissionCombo = new Combo(compositePushNotify, SWT.READ_ONLY);
        final Button pushNotifyNdock = new Button(compositePushNotify, SWT.CHECK);
        final Combo pushPriorityNdockCombo = new Combo(compositePushNotify, SWT.READ_ONLY);
        final Button pushNotifyAkashi = new Button(compositePushNotify, SWT.CHECK);
        final Combo pushPriorityAkashiCombo = new Combo(compositePushNotify, SWT.READ_ONLY);
        final Button pushNotifyCond = new Button(compositePushNotify, SWT.CHECK);
        final Combo pushPriorityCondCombo = new Combo(compositePushNotify, SWT.READ_ONLY);

        Button[] pushNotifyButtons = new Button[] {
                pushNotifyMission, pushNotifyNdock, pushNotifyAkashi, pushNotifyCond };
        Combo[] pushNotifyCombos = new Combo[] {
                pushPriorityMissionCombo, pushPriorityNdockCombo, pushPriorityAkashiCombo, pushPriorityCondCombo };
        boolean[] pushEnabled = new boolean[] {
                AppConfig.get().getPushMission(),
                AppConfig.get().getPushNdock(),
                AppConfig.get().isPushAkashi(),
                AppConfig.get().isPushCond()
        };
        int[] pushPriorities = new int[] {
                AppConfig.get().getPushPriorityMission(),
                AppConfig.get().getPushPriorityNdock(),
                AppConfig.get().getPushPriorityAkashi(),
                AppConfig.get().getPushPriorityCond()
        };

        for (int i = 0; i < pushNotifyButtons.length; ++i) {
            Button button = pushNotifyButtons[i];
            Combo compo = pushNotifyCombos[i];

            button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
            button.setText(pushName[i]);
            button.setSelection(pushEnabled[i]);

            GridData gdpushPriorityMissionCombo = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
            compo.setLayoutData(gdpushPriorityMissionCombo);
            compo.add("Priority: Very Low");
            compo.add("Priority: Moderate");
            compo.add("Priority: Normal");
            compo.add("Priority: High");
            compo.add("Priority: Emergency");
            compo.select(pushPriorities[i] + 2);
        }

        Button TestNotifyMissionBtn = new Button(compositePushNotify, SWT.NONE);
        TestNotifyMissionBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
        TestNotifyMissionBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setNotifyProwl(prowl.getSelection());
                AppConfig.get().setProwlAPIKey(prowlAPIKey.getText());
                AppConfig.get().setNotifyNMA(nma.getSelection());
                AppConfig.get().setNMAAPIKey(nmaAPIKey.getText());
                AppConfig.get().setNotifyImKayac(imkayac.getSelection());
                AppConfig.get().setImKayacUserName(imkayacUserName.getText());
                AppConfig.get().setImKayacPasswd(imkayacPasswd.getText());
                AppConfig.get().setImKayacPrivateKey(imkayacPrivateKey.getText());
                AppConfig.get().setPushMission(pushNotifyMission.getSelection());
                AppConfig.get().setPushNdock(pushNotifyNdock.getSelection());
                AppConfig.get().setPushPriorityMission(pushPriorityMissionCombo.getSelectionIndex() - 2);
                AppConfig.get().setPushPriorityNdock(pushPriorityNdockCombo.getSelectionIndex() - 2);
                if (AppConfig.get().getPushMission()) {
                    PushNotify.add("通知テスト艦隊(テスト遠征)がまもなく帰投します", "遠征", AppConfig.get().getPushPriorityMission());
                }
            }
        });
        TestNotifyMissionBtn.setText("テスト通知(遠征)");

        Button TestNotifyNdockBtn = new Button(compositePushNotify, SWT.NONE);
        TestNotifyNdockBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        TestNotifyNdockBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AppConfig.get().setNotifyProwl(prowl.getSelection());
                AppConfig.get().setProwlAPIKey(prowlAPIKey.getText());
                AppConfig.get().setNotifyNMA(nma.getSelection());
                AppConfig.get().setNMAAPIKey(nmaAPIKey.getText());
                AppConfig.get().setNotifyImKayac(imkayac.getSelection());
                AppConfig.get().setImKayacUserName(imkayacUserName.getText());
                AppConfig.get().setImKayacPasswd(imkayacPasswd.getText());
                AppConfig.get().setImKayacPrivateKey(imkayacPrivateKey.getText());
                AppConfig.get().setPushMission(pushNotifyMission.getSelection());
                AppConfig.get().setPushNdock(pushNotifyNdock.getSelection());
                AppConfig.get().setPushPriorityMission(pushPriorityMissionCombo.getSelectionIndex() - 2);
                AppConfig.get().setPushPriorityNdock(pushPriorityNdockCombo.getSelectionIndex() - 2);
                if (AppConfig.get().getPushNdock()) {
                    PushNotify.add("通知テスト改(Lv99)がまもなくお風呂からあがります", "入渠", AppConfig.get().getPushPriorityNdock());
                }
            }
        });
        TestNotifyNdockBtn.setText("テスト通知(入渠)");

        // ツールウィンドウ
        compositeToolWin.setLayout(new GridLayout(1, false));

        Group toolWinGroup = new Group(compositeToolWin, SWT.NONE);
        toolWinGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        toolWinGroup.setLayout(new GridLayout(3, false));
        toolWinGroup.setText("表示ボタン設定（ドラッグ&&ドロップで移動できます）");

        Label enabledButtonsLabel = new Label(toolWinGroup, SWT.NONE);
        enabledButtonsLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        enabledButtonsLabel.setText("表示するボタン");

        Label toolWinSeparator = new Label(toolWinGroup, SWT.SEPARATOR);
        toolWinSeparator.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 2));

        Label disabledButtonsLabel = new Label(toolWinGroup, SWT.NONE);
        disabledButtonsLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        disabledButtonsLabel.setText("表示しないボタン");

        final Table enabledButtonTable = this.createToolButtonTable(toolWinGroup);
        final Table disabledButtonTable = this.createToolButtonTable(toolWinGroup);

        this.createToolButtonTableContents(enabledButtonTable, disabledButtonTable);

        // Development タブ
        compositeDevelopment.setLayout(new GridLayout(2, false));

        new Label(compositeDevelopment, SWT.NONE);
        final Button btnJson = new Button(compositeDevelopment, SWT.CHECK);
        btnJson.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        btnJson.setText("JSONを保存する");
        btnJson.setSelection(AppConfig.get().isStoreJson());

        Label lblJson = new Label(compositeDevelopment, SWT.NONE);
        lblJson.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblJson.setText("JSON保存先");

        final Text jsonpath = new Text(compositeDevelopment, SWT.BORDER);
        GridData gdJsonpath = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdJsonpath.widthHint = SwtUtils.DPIAwareWidth(120);
        jsonpath.setLayoutData(gdJsonpath);
        jsonpath.setText(AppConfig.get().getStoreJsonPath());

        final Button btnTest = new Button(compositeDevelopment, SWT.CHECK);
        btnTest.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        btnTest.setText("航海日誌開発者向けメニューを追加する*");
        btnTest.setSelection(AppConfig.get().isEnableTestWindow());

        Composite commandComposite = new Composite(this.shell, SWT.NONE);
        commandComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        GridLayout glCommandComposite = new GridLayout(2, false);
        glCommandComposite.verticalSpacing = 0;
        glCommandComposite.marginWidth = 0;
        glCommandComposite.marginHeight = 0;
        glCommandComposite.horizontalSpacing = 0;
        commandComposite.setLayout(glCommandComposite);

        // '*'の説明
        Composite commandLeft = new Composite(commandComposite, SWT.NONE);
        GridLayout glCommandLeft = new GridLayout(1, false);
        glCommandLeft.horizontalSpacing = 0;
        glCommandLeft.marginHeight = 0;
        glCommandLeft.verticalSpacing = 0;
        glCommandLeft.marginWidth = 0;
        commandLeft.setLayout(glCommandLeft);

        Label attentionLabel = new Label(commandLeft, SWT.NONE);
        attentionLabel.setText("*再起動後に有効になります");

        Composite commandRight = new Composite(commandComposite, SWT.NONE);
        RowLayout rlCommandRight = new RowLayout(SWT.HORIZONTAL);
        rlCommandRight.marginTop = 0;
        rlCommandRight.marginLeft = 0;
        rlCommandRight.marginRight = 0;
        rlCommandRight.marginBottom = 0;
        commandRight.setLayout(rlCommandRight);
        commandRight.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));

        // OK ボタン
        Button applyBtn = new Button(commandRight, SWT.NONE);
        applyBtn.setLayoutData(new RowData(100, SWT.DEFAULT));
        applyBtn.setText("OK");
        applyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // 設定の保存アクション

                // 値チェック
                if (!StringUtils.isNumeric(listenport.getText())) {
                    ConfigDialog.this.printErrorMessage("受信ポートには数値(0-65535)を入力してください");
                    return;
                }
                int listenPort = Integer.parseInt(listenport.getText());
                if ((listenPort < 0) || (listenPort > 65535)) {
                    ConfigDialog.this.printErrorMessage("受信ポートには0～65535の数値を入力してください");
                    return;
                }

                // system
                AppConfig.get().setListenPort(listenPort);
                AppConfig.get().setHideWindow(hidewindow.getSelection());
                AppConfig.get().setCloseWhenMinimized(closewindow.getSelection());
                if (ontop != null) {
                    AppConfig.get().setOnTop(ontop.getSelection());
                }
                AppConfig.get().setCheckDoit(checkDoit.getSelection());
                AppConfig.get().setNameOnTitlebar(nameOnTitlebar.getSelection());
                AppConfig.get().setColorSupport(colorSupport.getSelection());
                if (StringUtils.isNumeric(soundlevel.getText())) {
                    float level = (float) Integer.parseInt(soundlevel.getText()) / 100;
                    AppConfig.get().setSoundLevel(level);
                }
                /*
                if (StringUtils.isNumeric(alpha.getText())) {
                    AppConfig.get().setAlpha(Integer.parseInt(alpha.getText()));
                }
                */
                AppConfig.get().setReportPath(reportDir.getText());
                AppConfig.get().setBattleLogPath(battlelogDir.getText());
                AppConfig.get().setUpdateCheck(checkUpdate.getSelection());
                AppConfig.get().setAllowOnlyFromLocalhost(onlyFromLocalhost.getSelection());
                AppConfig.get().setCloseOutsidePort(closeOutsidePort.getSelection());
                // maintab
                AppConfig.get().setPrintSortieLog(printSortieLog.getSelection());
                AppConfig.get().setPrintSunkLog(printSunkLog.getSelection());
                AppConfig.get().setPrintUpdateLog(printUpdateLog.getSelection());
                AppConfig.get().setPrintDropLog(printDropLog.getSelection());
                AppConfig.get().setLoadCreateItemLog(loadCreateItemLog.getSelection());
                AppConfig.get().setLoadCreateShipLog(loadCreateShipLog.getSelection());
                AppConfig.get().setLoadMissionLog(loadMissionLog.getSelection());
                if (JIntellitypeWrapper.getInstance() != null) {
                    AppConfig.get().setSystemWideHotKey(systemWideShortcutKey.getSelectionIndex());
                }
                // fleettab
                AppConfig.get().setDisplayCount(displaycount.getSelection());
                AppConfig.get().setDefaultSea(seacombo.getItem(seacombo.getSelectionIndex()));
                AppConfig.get().setDefaultEvaluate(evalcombo.getItem(evalcombo.getSelectionIndex()));
                AppConfig.get().setWarnByNeedSupply(warnByNeedSupply.getSelection());
                AppConfig.get().setWarnByCondState(warnByCondState.getSelection());
                AppConfig.get().setWarnByHalfDamage(warnByHalfDamage.getSelection());
                AppConfig.get().setFatalBybadlyDamage(fatalBybadlyDamage.getSelection());
                AppConfig.get().setBalloonBybadlyDamage(balloonBybadlyDamage.getSelection());
                AppConfig.get().setVisibleOnReturnMission(visibleOnReturnMission.getSelection());
                AppConfig.get().setVisibleOnReturnBathwater(visibleOnReturnBathwater.getSelection());
                AppConfig.get().setMonoIcon(useMonoIcon.getSelection());
                AppConfig.get().setShowGunAndBulge(showGunAndBulge.getSelection());
                AppConfig.get().setShowCondTimer(showCondTimer.getSelection());
                AppConfig.get().setShowAkashiTimer(showAkashiTimer.getSelection());
                AppConfig.get().setAkashiTimerFormat(akashiFormatCombo.getSelectionIndex());
                AppConfig.get().setSeikuMethod(seikuCombo.getSelectionIndex());
                AppConfig.get().setSakutekiMethodV4(sakutekiCombo.getSelectionIndex());
                // notify
                AppConfig.get().setOkCond(condSpinner.getSelection());
                AppConfig.get().setNoticeCondOnlyMainFleet(condOnlyMain.getSelection());
                AppConfig.get().setAkashiNotifyFirstStep(akashiFirstStep.getSelection());
                AppConfig.get().setAkashiNotifyEveryStep(akashiEveryStep.getSelection());
                AppConfig.get().setMissionRemind(remind.getSelection());
                AppConfig.get().setRemindInterbal(intervalSpinner.getSelection());
                AppConfig.get().setUseBalloon(balloon.getSelection());
                AppConfig.get().setUseTaskbarNotify(taskbar.getSelection());
                AppConfig.get().setNotifyFully(fullySpinner.getSelection());
                AppConfig.get().setEnableShipFullBalloonNotify(shipFullBalloon.getSelection());
                AppConfig.get().setShipFullBalloonNotify(shipFullSpinner.getSelection());
                AppConfig.get().setEnableItemFullBalloonNotify(itemFullBalloon.getSelection());
                AppConfig.get().setItemFullBalloonNotify(itemFullSpinner.getSelection());
                // capture
                AppConfig.get().setCapturePath(captureDir.getText());
                AppConfig.get().setImageFormat(imageformatCombo.getItem(imageformatCombo.getSelectionIndex()));
                AppConfig.get().setCreateDateFolder(createDateFolder.getSelection());
                // チャート
                AppConfig.get().setMaterialLogInterval(materialintervalSpinner.getSelection());
                AppConfig.get().setMaterialLogDetail(detailMaterial.getSelection());
                AppConfig.get().setFuelColor(fuel.getForeground().getRGB());
                AppConfig.get().setAmmoColor(ammo.getForeground().getRGB());
                AppConfig.get().setMetalColor(metal.getForeground().getRGB());
                AppConfig.get().setBauxiteColor(bauxite.getForeground().getRGB());
                AppConfig.get().setBurnerColor(burner.getForeground().getRGB());
                AppConfig.get().setBucketColor(bucket.getForeground().getRGB());
                AppConfig.get().setResearchColor(research.getForeground().getRGB());
                AppConfig.get().setScrewColor(screw.getForeground().getRGB());
                // ウィンドウ
                AppConfig.get().setOpaqueInterval(opaqueIntervalSpinner.getSelection());
                AppConfig.get().setEnableMoveWithDD(enableMoveWithDD.getSelection());
                AppConfig.get().setNoMenubar(noMenubar.getSelection());
                AppConfig.get().setDisableWindowMenu(diableWindowMenu.getSelection());
                AppConfig.get().setToggleToolButton(toggleToolButton.getSelection());
                AppConfig.get().setShowSubwindowHost(showSubwindowHost.getSelection());
                for (int i = 0; i < 4; ++i) {
                    AppConfig.get().getShipTableNames()[i] = shipTableNameText[i].getText();
                }
                // connection
                AppConfig.get().setUseProxy(useProxyButton.getSelection());
                AppConfig.get().setProxyHost(proxyHostText.getText());
                AppConfig.get().setProxyPort(proxyPortSpinner.getSelection());
                AppConfig.get().setSendDatabase(sendDatabaseButton.getSelection());
                AppConfig.get().setDatabaseSendLog(databaseLogButton.getSelection());
                AppConfig.get().setAccessKey(accessKeyText.getText());
                // push notify
                AppConfig.get().setNotifyProwl(prowl.getSelection());
                AppConfig.get().setProwlAPIKey(prowlAPIKey.getText());
                AppConfig.get().setNotifyNMA(nma.getSelection());
                AppConfig.get().setNMAAPIKey(nmaAPIKey.getText());
                AppConfig.get().setNotifyImKayac(imkayac.getSelection());
                AppConfig.get().setImKayacUserName(imkayacUserName.getText());
                AppConfig.get().setImKayacPasswd(imkayacPasswd.getText());
                AppConfig.get().setImKayacPrivateKey(imkayacPrivateKey.getText());
                AppConfig.get().setPushMission(pushNotifyMission.getSelection());
                AppConfig.get().setPushNdock(pushNotifyNdock.getSelection());
                AppConfig.get().setPushAkashi(pushNotifyAkashi.getSelection());
                AppConfig.get().setPushCond(pushNotifyCond.getSelection());
                AppConfig.get().setPushPriorityMission(pushPriorityMissionCombo.getSelectionIndex() - 2);
                AppConfig.get().setPushPriorityNdock(pushPriorityNdockCombo.getSelectionIndex() - 2);
                AppConfig.get().setPushPriorityAkashi(pushPriorityAkashiCombo.getSelectionIndex() - 2);
                AppConfig.get().setPushPriorityCond(pushPriorityCondCombo.getSelectionIndex() - 2);

                // ツールウィンドウ
                List<String> toolButtons = AppConfig.get().getToolButtons();
                toolButtons.clear();
                for (TableItem item : enabledButtonTable.getItems()) {
                    String key = (String) item.getData();
                    toolButtons.add(key);
                }

                // development
                AppConfig.get().setStoreJson(btnJson.getSelection());
                AppConfig.get().setStoreJsonPath(new File(jsonpath.getText()).getAbsolutePath());
                AppConfig.get().setEnableTestWindow(btnTest.getSelection());
                try {
                    AppConfig.store();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                ConfigDialog.this.shell.close();
                ConfigDialog.this.main.configUpdated();
            }
        });

        Button cancelBtn = new Button(commandRight, SWT.NONE);
        cancelBtn.setLayoutData(new RowData(100, SWT.DEFAULT));
        cancelBtn.setText("キャンセル");
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ConfigDialog.this.shell.close();
            }
        });

        for (Control control : this.composite.getChildren()) {
            LayoutLogic.hide(control, true);
        }

        sashForm.setWeights(new int[] { 2, 5 });
        this.scrolledComposite.setContent(this.composite);
        this.scrolledComposite.setMinSize(this.composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    private void printErrorMessage(String message) {
        MessageBox mes = new MessageBox(ConfigDialog.this.shell, SWT.ICON_WARNING | SWT.OK);
        mes.setMessage(message);
        mes.open();
    }

    private Label createColorSetting(Composite chartGroup, String title, RGB currentColor, final RGB[] defaultColor) {
        final Label label = new Label(chartGroup, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText(title);
        label.setForeground(ColorManager.getColor(currentColor));

        Button changeColor = new Button(chartGroup, SWT.NONE);
        changeColor.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        changeColor.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ColorDialog dialog = new ColorDialog(ConfigDialog.this.shell);
                RGB rgb = dialog.open();
                if (rgb != null) {
                    label.setForeground(ColorManager.getColor(rgb));
                }
            }
        });
        changeColor.setText("色の設定");

        Button resetColor = new Button(chartGroup, SWT.NONE);
        resetColor.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        resetColor.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                label.setForeground(ColorManager.getColor(defaultColor));
            }
        });
        resetColor.setText("リセット");
        return label;
    }

    private Table createToolButtonTable(Composite parent) {
        final Table table = new Table(parent, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd.heightHint = SwtUtils.DPIAwareWidth(230);
        gd.widthHint = SwtUtils.DPIAwareHeight(150);
        table.setLayoutData(gd);
        table.setHeaderVisible(false);
        final TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText("ボタン");
        table.addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                column.setWidth(table.getClientArea().width);
            }
        });
        return table;
    }

    private void createToolButtonTableItems(Table table, List<String> keys, Map<String, Integer> keyMap) {
        for (String key : keys) {
            int i = keyMap.get(key);
            String shortName = AppConstants.SHORT_WINDOW_NAME_LIST[i];
            String name = AppConstants.WINDOW_NAME_LIST[i];

            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(shortName + " (" + name + ")");
            item.setData(key);
        }
    }

    private void createToolButtonTableContents(final Table enabledTable, Table disabledTable) {

        final WindowBase[] winList = LauncherWindow.getWindowList();
        Map<String, Integer> winKeyMap = LauncherWindow.getWindowKeyMap();
        List<String> toolButtons = AppConfig.get().getToolButtons();
        if ((toolButtons == null) || (toolButtons.size() == 0)) {
            // 初期化
            toolButtons = new ArrayList<String>();
            for (WindowBase win : winList) {
                toolButtons.add(win.getWindowId());
            }
            AppConfig.get().setToolButtons(toolButtons);
        }

        Set<String> enabledSet = new HashSet<>();
        for (String key : toolButtons) {
            enabledSet.add(key);
        }
        List<String> disabledButtons = new ArrayList<>();
        for (WindowBase win : winList) {
            String key = win.getWindowId();
            if (enabledSet.contains(key) == false) {
                disabledButtons.add(key);
            }
        }
        this.createToolButtonTableItems(enabledTable, toolButtons, winKeyMap);
        this.createToolButtonTableItems(disabledTable, disabledButtons, winKeyMap);

        SwtUtils.addItemDragAndDropMoveSupport(new Table[] { enabledTable, disabledTable },
                new TableDragAndDropListener() {
                    @Override
                    public String tableItemToString(TableItem item) {
                        return item.getText();
                    }

                    @Override
                    public boolean canDragSource(TableItem source) {
                        if ((source.getParent() == enabledTable)
                                && (enabledTable.getItemCount() == 1)) {
                            // 表示ボタンゼロにはできない
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public TableItem create(Table table, TableItem source, int index) {
                        TableItem item = new TableItem(table, SWT.NONE, index);
                        item.setText(source.getText());
                        item.setData(source.getData());
                        return item;
                    }

                    @Override
                    public void finished(TableItem newItem) {
                        // do nothing
                    }
                });
    }

    private void selectPane(Composite selected) {
        for (Control control : this.composite.getChildren()) {
            LayoutLogic.hide(control, selected != control);
        }
        this.composite.layout();
        this.scrolledComposite.setMinSize(this.composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * ツリーをクリックした時に呼び出されるアダプター
     *
     */
    private static final class TreeSelectionAdapter extends SelectionAdapter {

        /** ダイアログ */
        private final ConfigDialog dialog;

        /**
         * コンストラクター
         */
        public TreeSelectionAdapter(ConfigDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            Object data = e.item.getData();
            if (data instanceof Composite) {
                this.dialog.selectPane((Composite) data);
            }
        }
    }
}