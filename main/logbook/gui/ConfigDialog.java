/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui;

import logbook.config.GlobalConfig;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
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
        super(parent, SWT.SHELL_TRIM | SWT.MODELESS);
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

        TabItem tabSystem = new TabItem(tabFolder, SWT.NONE);
        tabSystem.setText("システム");

        Composite compositeSystem = new Composite(tabFolder, SWT.NONE);
        compositeSystem.setLayout(new GridLayout(2, false));
        tabSystem.setControl(compositeSystem);

        Label label = new Label(compositeSystem, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("ポート番号*");

        final Text listenport = new Text(compositeSystem, SWT.BORDER);
        listenport.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        listenport.setText(Integer.toString(GlobalConfig.getListenPort()));

        Label label1 = new Label(compositeSystem, SWT.NONE);
        label1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label1.setText("ウインドウサイズ(横)");

        final Text width = new Text(compositeSystem, SWT.BORDER);
        width.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        width.setText(Integer.toString(GlobalConfig.getWidth()));

        Label label2 = new Label(compositeSystem, SWT.NONE);
        label2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label2.setText("ウインドウサイズ(縦)");

        final Text height = new Text(compositeSystem, SWT.BORDER);
        height.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        height.setText(Integer.toString(GlobalConfig.getHeight()));

        Label label3 = new Label(compositeSystem, SWT.NONE);
        label3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label3.setText("音量(%)");

        final Text soundlevel = new Text(compositeSystem, SWT.BORDER);
        soundlevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        soundlevel.setText(Integer.toString((int) (GlobalConfig.getSoundLevel() * 100)));

        final Button hidewindow = new Button(compositeSystem, SWT.CHECK);
        hidewindow.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        hidewindow.setText("最小化時にタスクトレイに格納");
        hidewindow.setSelection(GlobalConfig.getHideWindow());

        final Button ontop = new Button(compositeSystem, SWT.CHECK);
        ontop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        ontop.setText("最前面に表示する*");
        ontop.setSelection(GlobalConfig.getOnTop() != SWT.NONE);

        final Button checkUpdate = new Button(compositeSystem, SWT.CHECK);
        checkUpdate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        checkUpdate.setText("起動時にアップデートチェック*");
        checkUpdate.setSelection(GlobalConfig.getCheckUpdate());

        TabItem tbtmDevelopment = new TabItem(tabFolder, SWT.NONE);
        tbtmDevelopment.setText("Development");

        Composite compositeDevelopment = new Composite(tabFolder, SWT.NONE);
        compositeDevelopment.setLayout(new GridLayout(2, false));
        tbtmDevelopment.setControl(compositeDevelopment);
        new Label(compositeDevelopment, SWT.NONE);

        final Button btnJson = new Button(compositeDevelopment, SWT.CHECK);
        btnJson.setText("JSONを保存する");
        btnJson.setSelection(GlobalConfig.getStoreJson());

        Label lblJson = new Label(compositeDevelopment, SWT.NONE);
        lblJson.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblJson.setText("JSON保存先");

        final Text jsonpath = new Text(compositeDevelopment, SWT.BORDER);
        jsonpath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        jsonpath.setText(GlobalConfig.getStoreJsonPath());

        Composite command = new Composite(this.shell, SWT.NONE);
        RowLayout rlCommand = new RowLayout();
        command.setLayout(rlCommand);
        command.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        Button applyBtn = new Button(command, SWT.NONE);
        applyBtn.setLayoutData(new RowData(100, SWT.DEFAULT));
        applyBtn.setText("OK");
        applyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // system
                GlobalConfig.setListenPort(listenport.getText());
                GlobalConfig.setWidth(width.getText());
                GlobalConfig.setHeight(height.getText());
                GlobalConfig.setHideWindow(hidewindow.getSelection());
                GlobalConfig.setOnTop(ontop.getSelection());
                GlobalConfig.setSoundLevel(soundlevel.getText());
                GlobalConfig.setCheckUpdate(checkUpdate.getSelection());

                // development
                GlobalConfig.setStoreJson(btnJson.getSelection());
                GlobalConfig.setStoreJsonPath(jsonpath.getText());

                GlobalConfig.store();
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
