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
     * @param style
     */
    public ConfigDialog(Shell parent) {
        super(parent, SWT.SHELL_TRIM | SWT.MODELESS);
        this.setText("設定");
    }

    /**
     * Open the dialog.
     * @return the result
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

        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText("システム");

        Composite composite = new Composite(tabFolder, SWT.NONE);
        tabItem.setControl(composite);
        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("ポート番号");

        final Text listenport = new Text(composite, SWT.BORDER);
        listenport.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        listenport.setText(Integer.toString(GlobalConfig.getListenPort()));

        Label label1 = new Label(composite, SWT.NONE);
        label1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label1.setText("ウインドウサイズ(横)");

        final Text width = new Text(composite, SWT.BORDER);
        width.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        width.setText(Integer.toString(GlobalConfig.getWidth()));

        Label label2 = new Label(composite, SWT.NONE);
        label2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label2.setText("ウインドウサイズ(縦)");

        final Text height = new Text(composite, SWT.BORDER);
        height.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        height.setText(Integer.toString(GlobalConfig.getHeight()));

        new Label(composite, SWT.NONE);

        final Button ontop = new Button(composite, SWT.CHECK);
        ontop.setText("最前面に表示する");
        ontop.setSelection(GlobalConfig.getOnTop() != SWT.NONE);

        Label label3 = new Label(composite, SWT.NONE);
        label3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label3.setText("音量(%)");

        final Text soundlevel = new Text(composite, SWT.BORDER);
        soundlevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        soundlevel.setText(Integer.toString((int) (GlobalConfig.getSoundLevel() * 100)));

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
                GlobalConfig.setListenPort(listenport.getText());
                GlobalConfig.setWidth(width.getText());
                GlobalConfig.setHeight(height.getText());
                GlobalConfig.setOnTop(ontop.getSelection());
                GlobalConfig.setSoundLevel(soundlevel.getText());
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
