/**
 * 
 */
package logbook.gui;

import logbook.config.GlobalConfig;
import logbook.server.proxy.Filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author noname
 *
 */
public class Help extends Dialog {

    protected Object result;
    protected Shell shell;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public Help(Shell parent, int style) {
        super(parent, style);
        this.setText("ヘルプ");
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public Object open() {
        this.createContents();
        this.shell.open();
        this.shell.layout();
        Display display = this.getParent().getDisplay();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return this.result;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setSize(300, 350);
        this.shell.setText(this.getText());
        this.shell.setLayout(new GridLayout(1, false));

        // バージョン
        Group versionGroup = new Group(this.shell, SWT.NONE);
        versionGroup.setText("バージョン");
        versionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        versionGroup.setLayout(new GridLayout(2, true));

        this.label("航海日誌", versionGroup);
        this.label(GlobalConfig.VERSION, versionGroup);

        // 設定
        Group appGroup = new Group(this.shell, SWT.NONE);
        appGroup.setText("設定");
        appGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        appGroup.setLayout(new GridLayout(2, true));

        this.label("鎮守府サーバー", appGroup);
        this.label(StringUtils.defaultString(Filter.getServerName(), "未設定"), appGroup);

        // 設定
        Group javaGroup = new Group(this.shell, SWT.NONE);
        javaGroup.setText("環境");
        javaGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        javaGroup.setLayout(new GridLayout(2, true));

        double totalMemory = ((double) Runtime.getRuntime().totalMemory()) / 1024 / 1024;
        double freeMemory = ((double) Runtime.getRuntime().freeMemory()) / 1024 / 1024;

        this.label("利用可能メモリサイズ", javaGroup);
        this.label(Long.toString(Math.round(totalMemory)) + " MB", javaGroup);

        this.label("利用中メモリサイズ", javaGroup);
        this.label(Long.toString(Math.round(totalMemory - freeMemory)) + " MB", javaGroup);

        this.label("os.name", javaGroup);
        this.label(SystemUtils.OS_NAME, javaGroup);

        this.label("os.version", javaGroup);
        this.label(SystemUtils.OS_VERSION, javaGroup);

        this.label("java.vendor", javaGroup);
        this.label(SystemUtils.JAVA_VENDOR, javaGroup);

        this.label("java.version", javaGroup);
        this.label(SystemUtils.JAVA_VERSION, javaGroup);
    }

    private Label label(String text, Composite composite) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(text);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return label;
    }
}
