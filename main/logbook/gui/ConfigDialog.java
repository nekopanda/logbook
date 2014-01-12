/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui;

import java.io.IOException;

import logbook.config.AppConfig;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * 設定画面
 *
 */
public final class ConfigDialog extends Dialog {

    private Shell shell;

    /**
     * Create the dialog.
     * @param parent
     */
    public ConfigDialog(Shell parent) {
        super(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE);
        this.setText("設定");
    }

    /**
     * Open the dialog.
     */
    public void open() {
        this.createContents();
        this.shell.open();
        this.shell.layout();
        Display display = this.getParent().getDisplay();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setText(this.getText());
        this.shell.setLayout(new GridLayout(1, false));

        TabFolder tabFolder = new TabFolder(this.shell, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

        // システム タブ
        TabItem tabSystem = new TabItem(tabFolder, SWT.NONE);
        tabSystem.setText("システム");

        Composite compositeSystem = new Composite(tabFolder, SWT.NONE);
        compositeSystem.setLayout(new GridLayout(3, false));
        tabSystem.setControl(compositeSystem);

        Label label = new Label(compositeSystem, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("ポート番号*");

        final Text listenport = new Text(compositeSystem, SWT.BORDER);
        GridData gdListenport = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdListenport.widthHint = 90;
        listenport.setLayoutData(gdListenport);
        listenport.setText(Integer.toString(AppConfig.get().getListenPort()));
        new Label(compositeSystem, SWT.NONE);

        Label label3 = new Label(compositeSystem, SWT.NONE);
        label3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label3.setText("音量(%)");

        final Text soundlevel = new Text(compositeSystem, SWT.BORDER);
        GridData gdSoundlevel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdSoundlevel.widthHint = 90;
        soundlevel.setLayoutData(gdSoundlevel);
        soundlevel.setText(Integer.toString((int) (AppConfig.get().getSoundLevel() * 100)));
        new Label(compositeSystem, SWT.NONE);

        Label label7 = new Label(compositeSystem, SWT.NONE);
        label7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label7.setText("透明度*");

        final Text alpha = new Text(compositeSystem, SWT.BORDER);
        GridData gdAlpha = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdAlpha.widthHint = 90;
        alpha.setLayoutData(gdAlpha);
        alpha.setText(Integer.toString(AppConfig.get().getAlpha()));
        new Label(compositeSystem, SWT.NONE);

        Label label8 = new Label(compositeSystem, SWT.NONE);
        label8.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label8.setText("報告書の保存先");

        final Text reportDir = new Text(compositeSystem, SWT.BORDER);
        GridData gdReportDir = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdReportDir.widthHint = 120;
        reportDir.setLayoutData(gdReportDir);
        reportDir.setText(AppConfig.get().getReportPath());

        Button reportSavedirBtn = new Button(compositeSystem, SWT.NONE);
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

        final Button hidewindow = new Button(compositeSystem, SWT.CHECK);
        hidewindow.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        hidewindow.setText("最小化時にタスクトレイに格納");
        hidewindow.setSelection(AppConfig.get().isHideWindow());
        new Label(compositeSystem, SWT.NONE);

        final Button ontop = new Button(compositeSystem, SWT.CHECK);
        ontop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        ontop.setText("最前面に表示する*");
        ontop.setSelection(AppConfig.get().isOnTop());
        new Label(compositeSystem, SWT.NONE);

        final Button checkUpdate = new Button(compositeSystem, SWT.CHECK);
        checkUpdate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        checkUpdate.setText("起動時にアップデートチェック*");
        checkUpdate.setSelection(AppConfig.get().isCheckUpdate());
        new Label(compositeSystem, SWT.NONE);

        // キャプチャ タブ
        TabItem tabCapture = new TabItem(tabFolder, SWT.NONE);
        tabCapture.setText("画面キャプチャ");

        Composite compositeCapture = new Composite(tabFolder, SWT.NONE);
        compositeCapture.setLayout(new GridLayout(3, false));
        tabCapture.setControl(compositeCapture);

        Label label4 = new Label(compositeCapture, SWT.NONE);
        label4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label4.setText("保存先");

        final Text captureDir = new Text(compositeCapture, SWT.BORDER);
        GridData gdCaptureDir = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdCaptureDir.widthHint = 120;
        captureDir.setLayoutData(gdCaptureDir);
        captureDir.setText(AppConfig.get().getCapturePath());

        Button savedirBtn = new Button(compositeCapture, SWT.NONE);
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

        // Development タブ
        TabItem tabDevelopment = new TabItem(tabFolder, SWT.NONE);
        tabDevelopment.setText("Development");

        Composite compositeDevelopment = new Composite(tabFolder, SWT.NONE);
        compositeDevelopment.setLayout(new GridLayout(2, false));
        tabDevelopment.setControl(compositeDevelopment);
        new Label(compositeDevelopment, SWT.NONE);

        final Button btnJson = new Button(compositeDevelopment, SWT.CHECK);
        btnJson.setText("JSONを保存する");
        btnJson.setSelection(AppConfig.get().isStoreJson());

        Label lblJson = new Label(compositeDevelopment, SWT.NONE);
        lblJson.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblJson.setText("JSON保存先");

        final Text jsonpath = new Text(compositeDevelopment, SWT.BORDER);
        jsonpath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        jsonpath.setText(AppConfig.get().getStoreJsonPath());

        Composite command = new Composite(this.shell, SWT.NONE);
        RowLayout rlCommand = new RowLayout();
        command.setLayout(rlCommand);
        command.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        // OK ボタン
        Button applyBtn = new Button(command, SWT.NONE);
        applyBtn.setLayoutData(new RowData(100, SWT.DEFAULT));
        applyBtn.setText("OK");
        applyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // 設定の保存アクション

                // system
                if (StringUtils.isNumeric(listenport.getText())) {
                    AppConfig.get().setListenPort(Integer.parseInt(listenport.getText()));
                }
                AppConfig.get().setHideWindow(hidewindow.getSelection());
                AppConfig.get().setOnTop(ontop.getSelection());
                if (StringUtils.isNumeric(soundlevel.getText())) {
                    float level = (float) Integer.parseInt(soundlevel.getText()) / 100;
                    AppConfig.get().setSoundLevel(level);
                }
                if (StringUtils.isNumeric(alpha.getText())) {
                    AppConfig.get().setAlpha(Integer.parseInt(alpha.getText()));
                }
                AppConfig.get().setReportPath(reportDir.getText());
                AppConfig.get().setCheckUpdate(checkUpdate.getSelection());
                // capture
                AppConfig.get().setCapturePath(captureDir.getText());
                AppConfig.get().setImageFormat(imageformatCombo.getItem(imageformatCombo.getSelectionIndex()));

                // development
                AppConfig.get().setStoreJson(btnJson.getSelection());
                AppConfig.get().setStoreJsonPath(jsonpath.getText());
                try {
                    AppConfig.store();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                ConfigDialog.this.shell.close();
            }
        });

        Button cancelBtn = new Button(command, SWT.NONE);
        cancelBtn.setLayoutData(new RowData(100, SWT.DEFAULT));
        cancelBtn.setText("キャンセル");
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ConfigDialog.this.shell.close();
            }
        });

        this.shell.pack();
    }
}
