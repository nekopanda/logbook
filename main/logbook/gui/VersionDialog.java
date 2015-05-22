package logbook.gui;

import java.awt.Desktop;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.gui.background.AsyncExecUpdateCheck;
import logbook.internal.LoggerHolder;
import logbook.internal.MasterData;
import logbook.server.proxy.Filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * バージョン情報
 *
 */
public final class VersionDialog extends WindowBase {

    private static final LoggerHolder LOG = new LoggerHolder(VersionDialog.class);

    private static final String ADDRESS = "http://nekopandanet.sakura.ne.jp/logbook/proxy.php?ip={0}&port={1}";

    private Shell shell;

    /**
     * Create the dialog.
     * @param parent
     */
    public VersionDialog(WindowBase parent) {
        this.createContents(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE, false);
        this.getShell().setText("バージョン情報");
    }

    /**
     * Open the dialog.
     */
    @Override
    public void open() {
        // 初期化済みの場合
        if (this.isWindowInitialized()) {
            // リロードして表示
            this.setVisible(true);
            return;
        }

        this.createContents();
        this.registerEvents();
        this.setWindowInitialized(true);
        this.setVisible(true);
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        this.shell = this.getShell();
        this.shell.setLayout(new GridLayout(1, false));

        // バージョン
        Group versionGroup = new Group(this.shell, SWT.NONE);
        versionGroup.setText("バージョン");
        versionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        versionGroup.setLayout(new GridLayout(2, true));

        label(AppConstants.NAME + " " + AppConstants.SUFFIX, versionGroup);
        label(AppConstants.VERSION, versionGroup);

        final String updateText = "アップデートを確認中...";
        final Label update = new Label(versionGroup, SWT.NONE);
        update.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        update.setText(updateText);
        final Display display = this.shell.getDisplay();
        new AsyncExecUpdateCheck(new AsyncExecUpdateCheck.UpdateResult() {

            @Override
            public void onSuccess(final String[] okversions) {
                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (VersionDialog.this.shell.isDisposed() == false) {
                            String text = updateText + "最新版です";
                            if (okversions[0].equals(AppConstants.VERSION) == false) {
                                text = "最新版 " + okversions[0] + " が公開されています";
                            }
                            update.setText(text);
                            VersionDialog.this.shell.layout();
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                LOG.get().info(e.getClass().getName() + "が原因でアップデートチェックに失敗しました");
                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (VersionDialog.this.shell.isDisposed() == false) {
                            update.setText("アップデートの確認に失敗");
                            VersionDialog.this.shell.layout();
                        }
                    }
                });
            }
        }).start();

        Link gowebsite = new Link(versionGroup, SWT.NONE);
        gowebsite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        gowebsite.setText("<a>クリックするとウェブサイトに移動します</a>");
        gowebsite.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    Desktop.getDesktop().browse(AppConstants.HOME_PAGE_URI);
                } catch (Exception e) {
                    LOG.get().warn("ウェブサイトに移動が失敗しました", e);
                }
            }
        });

        // 設定
        Group appGroup = new Group(this.shell, SWT.NONE);
        appGroup.setText("設定");
        appGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        appGroup.setLayout(new GridLayout(2, true));

        label("鎮守府サーバー", appGroup);
        label(StringUtils.defaultString(Filter.getServerName(), "未設定"), appGroup);

        Link copyScriptAddress = new Link(appGroup, SWT.NONE);
        copyScriptAddress.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        copyScriptAddress.setText("<a>自動構成スクリプトのアドレスをコピー</a>");
        copyScriptAddress.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String serverName = Filter.getServerName();
                if (StringUtils.isEmpty(serverName)) {
                    MessageBox msg = new MessageBox(VersionDialog.this.shell, SWT.NONE);
                    msg.setText(AppConstants.NAME);
                    msg.setMessage("鎮守府サーバーが確定していません。\r\nプロキシ設定を行って一度艦これにログインする必要があります。");
                    msg.open();
                }
                else {
                    String address = MessageFormat.format(ADDRESS, serverName,
                            Integer.toString(AppConfig.get().getListenPort()));
                    Clipboard clipboard = new Clipboard(Display.getDefault());
                    clipboard.setContents(new Object[] { address }, new Transfer[] { TextTransfer.getInstance() });
                }
            }
        });

        // データ
        Group dataGroup = new Group(this.shell, SWT.NONE);
        dataGroup.setText("データ更新日時");
        dataGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dataGroup.setLayout(new GridLayout(2, true));

        label("マスターデータ", dataGroup);
        String updateTimeStr = "不明";
        Date masterUpdateTime = MasterData.getMaster().getTime();
        if (masterUpdateTime.getTime() > 0) {
            updateTimeStr = (new SimpleDateFormat("yyyy/MM/dd HH:mm").format(masterUpdateTime));
        }
        label(updateTimeStr, dataGroup);

        // 設定
        Group javaGroup = new Group(this.shell, SWT.NONE);
        javaGroup.setText("環境");
        javaGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        javaGroup.setLayout(new GridLayout(2, true));

        double totalMemory = ((double) Runtime.getRuntime().totalMemory()) / 1024 / 1024;
        double freeMemory = ((double) Runtime.getRuntime().freeMemory()) / 1024 / 1024;

        label("利用可能メモリサイズ", javaGroup);
        label(Long.toString(Math.round(totalMemory)) + " MB", javaGroup);

        label("利用中メモリサイズ", javaGroup);
        label(Long.toString(Math.round(totalMemory - freeMemory)) + " MB", javaGroup);

        label("os.name", javaGroup);
        label(SystemUtils.OS_NAME, javaGroup);

        label("os.version", javaGroup);
        label(SystemUtils.OS_VERSION, javaGroup);

        label("java.vendor", javaGroup);
        label(SystemUtils.JAVA_VENDOR, javaGroup);

        label("java.version", javaGroup);
        label(SystemUtils.JAVA_VERSION, javaGroup);

        this.shell.pack();
    }

    private static Label label(String text, Composite composite) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(text);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return label;
    }
}
