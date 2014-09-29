package logbook.gui;

import java.awt.Desktop;
import java.text.SimpleDateFormat;
import java.util.Date;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.internal.MasterData;
import logbook.server.proxy.Filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

/**
 * バージョン情報
 *
 */
public final class VersionDialog extends WindowBase {

    private static final Logger LOG = LogManager.getLogger(VersionDialog.class);

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

        label("航海日誌 拡張版", versionGroup);
        label(AppConstants.VERSION, versionGroup);

        String latestVersion = AppConfig.get().getLatestVersion();
        if (latestVersion != null) {
            String text = "最新版です";
            if (latestVersion.equals(AppConstants.VERSION) == false) {
                text = "最新版 " + latestVersion + " が公開されています";
            }
            Label update = new Label(versionGroup, SWT.NONE);
            update.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
            update.setText(text);
        }

        Link gowebsite = new Link(versionGroup, SWT.NONE);
        gowebsite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        gowebsite.setText("<a>クリックするとウェブサイトに移動します</a>");
        gowebsite.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    Desktop.getDesktop().browse(AppConstants.HOME_PAGE_URI);
                } catch (Exception e) {
                    LOG.warn("ウェブサイトに移動が失敗しました", e);
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

        // データ
        Group dataGroup = new Group(this.shell, SWT.NONE);
        dataGroup.setText("データ更新日時");
        dataGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dataGroup.setLayout(new GridLayout(2, true));

        label("マスターデータ", dataGroup);
        String updateTimeStr = "不明";
        Date masterUpdateTime = MasterData.getInstance().getMasterUpdateTime();
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
