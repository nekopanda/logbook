package logbook.gui;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import logbook.config.AppConfig;
import logbook.server.proxy.Filter;
import logbook.util.SwtUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 自動プロキシ構成スクリプトファイル生成
 */
public final class CreatePacFileDialog extends Dialog {

    protected static final String SCRIPT = "function FindProxyForURL(url, host) '{'\r\n"
            + "  if (/^{0}/.test(host)) '{'\r\n"
            + "     return \"PROXY 127.0.0.1:{1}; DIRECT\";\r\n"
            + "  '}'\r\n"
            + "  return \"DIRECT\";\r\n"
            + "'}'\r\n";

    protected Shell shell;

    protected String server;
    protected Text iePath;
    protected Text firefoxPath;

    /**
     * Create the dialog.
     * @param parent
     */
    public CreatePacFileDialog(Shell parent) {
        super(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE);
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
        this.shell.setText("自動プロキシ構成スクリプトファイル生成");
        this.shell.setLayout(new GridLayout(1, false));

        Composite composite = new Composite(this.shell, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(1, false));

        Label labelTitle = new Label(composite, SWT.NONE);
        labelTitle.setText("自動プロキシ構成スクリプトファイルを生成します");

        String server = Filter.getServerName();
        if (server == null) {
            Group manualgroup = new Group(composite, SWT.NONE);
            manualgroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            manualgroup.setLayout(new GridLayout(2, false));
            manualgroup.setText("鎮守府サーバーが未検出です。IPアドレスを入力して下さい。");

            Label iplabel = new Label(manualgroup, SWT.NONE);
            iplabel.setText("IPアドレス:");

            final Text text = new Text(manualgroup, SWT.BORDER);
            GridData gdip = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gdip.widthHint = SwtUtils.DPIAwareWidth(150);
            text.setLayoutData(gdip);
            text.setText("0.0.0.0");
            text.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    CreatePacFileDialog.this.server = text.getText();
                }
            });

            this.server = "0.0.0.0";
        } else {
            this.server = server;
        }

        Button storeButton = new Button(composite, SWT.NONE);
        storeButton.setText("保存先を選択...");
        storeButton.addSelectionListener(new FileSelectionAdapter(this));

        Group addrgroup = new Group(composite, SWT.NONE);
        addrgroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addrgroup.setLayout(new GridLayout(2, false));
        addrgroup.setText("アドレス(保存先のアドレスより生成されます)");

        Label ieAddrLabel = new Label(addrgroup, SWT.NONE);
        ieAddrLabel.setText("IE用:");

        this.iePath = new Text(addrgroup, SWT.BORDER);
        GridData gdIePath = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdIePath.widthHint = SwtUtils.DPIAwareWidth(380);
        this.iePath.setLayoutData(gdIePath);

        Label fxAddrLabel = new Label(addrgroup, SWT.NONE);
        fxAddrLabel.setText("Firefox用:");

        this.firefoxPath = new Text(addrgroup, SWT.BORDER);
        GridData gdFxPath = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdFxPath.widthHint = SwtUtils.DPIAwareWidth(380);
        this.firefoxPath.setLayoutData(gdFxPath);

        this.shell.pack();
    }

    /**
     * PACファイルを書き込みます
     */
    private static final class FileSelectionAdapter extends SelectionAdapter {
        private final Shell shell;
        private final CreatePacFileDialog parent;

        public FileSelectionAdapter(CreatePacFileDialog dialog) {
            this.shell = dialog.shell;
            this.parent = dialog;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            FileDialog dialog = new FileDialog(this.shell, SWT.SAVE);
            dialog.setFileName("proxy.pac");
            dialog.setFilterExtensions(new String[] { "*.pac" });
            String filename = dialog.open();

            if (filename != null) {
                if (!StringUtils.isAsciiPrintable(filename)) {
                    MessageBox messageBox = new MessageBox(this.shell, SWT.ICON_WARNING);
                    messageBox.setText("注意");
                    messageBox.setMessage("保存先にASCII範囲外の文字(全角文字等)が含まれています。\nこのアドレスはInternetExplorerで読み込めません。");
                    messageBox.open();
                }

                MessageBox messageBox = new MessageBox(this.shell, SWT.ICON_INFORMATION);
                messageBox.setText("成功");
                messageBox.setMessage("自動プロキシ構成スクリプトファイルを生成しました。\r\n"
                        + "次にブラウザの設定を行って下さい。");
                messageBox.open();

                String script = MessageFormat.format(CreatePacFileDialog.SCRIPT,
                        this.parent.server.replace(".", "\\."),
                        Integer.toString(AppConfig.get().getListenPort()));

                File file = new File(filename);
                if (file.getAbsolutePath().startsWith("\\\\")) {
                    this.parent.iePath.setText("file://\\\\" + file.getAbsolutePath().substring(2).replace("\\", "/"));
                } else {
                    this.parent.iePath.setText("file://" + file.getAbsolutePath().replace("\\", "/"));
                }
                this.parent.firefoxPath.setText("file:///" + file.toURI().toString().replaceFirst("file:/", ""));

                try {
                    FileUtils.write(file, script);
                } catch (IOException e1) {
                    MessageBox errMessageBox = new MessageBox(this.shell, SWT.ICON_ERROR);
                    errMessageBox.setText("書き込めませんでした");
                    errMessageBox.setMessage(e1.toString());
                    errMessageBox.open();
                }
            }
        }
    }
}
