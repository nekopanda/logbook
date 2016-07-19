/**
 * 
 */
package logbook.gui.twitter;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import logbook.gui.WindowBase;
import logbook.util.SwtUtils;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * @author Nekopanda
 *
 */
public class OauthDialog extends WindowBase {

    private final WindowBase parent;
    private Shell shell;
    private final Twitter twitter;
    private RequestToken requestToken;
    private AccessToken accessToken;
    private User user;

    /**
     * Create the dialog.
     * @param parent
     */
    public OauthDialog(WindowBase parent, Twitter twitter) {
        this.parent = parent;
        this.twitter = twitter;
    }

    /**
     * Open the dialog.
     * @return the result
     */
    @Override
    public void open() {
        this.createContents();
        this.registerEvents();
        this.setWindowInitialized(true);
        this.setVisible(true);
        Display display = this.getShell().getDisplay();
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
        // シェル
        super.createContents(this.parent, SWT.CLOSE | SWT.TITLE | SWT.TOOL | SWT.APPLICATION_MODAL, false);
        this.getShell().setText("航海日誌 ツイッター認証");
        this.shell = this.getShell();

        // レイアウト
        GridLayout glShell = new GridLayout(3, false);
        this.shell.setLayout(glShell);

        Label label = new Label(this.shell, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        label.setText("Webブラウザ上でアプリを承認し、\nベリファイコードを入力する必要があります");

        Button launchWeb = new Button(this.shell, SWT.NONE);
        GridData gdLaunchWeb = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdLaunchWeb.widthHint = SwtUtils.DPIAwareWidth(80);
        launchWeb.setLayoutData(gdLaunchWeb);
        launchWeb.setText("起動");
        launchWeb.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    if (OauthDialog.this.requestToken == null) {
                        OauthDialog.this.requestToken = OauthDialog.this.twitter.getOAuthRequestToken();
                    }
                    Desktop.getDesktop().browse(URI.create(OauthDialog.this.requestToken.getAuthorizationURL()));
                } catch (TwitterException | IOException e1) {
                    SwtUtils.errorDialog(e1, OauthDialog.this.shell);
                }
            }
        });

        Label hBar = new Label(this.shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        hBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

        Label label2 = new Label(this.shell, SWT.NONE);
        label2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        label2.setText("ベリファイコード: ");

        final Text text = new Text(this.shell, SWT.BORDER);
        GridData gdText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdText.widthHint = SwtUtils.DPIAwareWidth(120);
        text.setLayoutData(gdText);
        text.setText("");

        Button complete = new Button(this.shell, SWT.NONE);
        complete.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        complete.setText("完了");
        complete.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String pin = text.getText();
                if ((OauthDialog.this.requestToken != null) && StringUtils.isNotEmpty(pin)) {
                    try {
                        OauthDialog.this.accessToken = OauthDialog.this.twitter.getOAuthAccessToken(
                                OauthDialog.this.requestToken, pin);
                        OauthDialog.this.user = OauthDialog.this.twitter.verifyCredentials();
                        OauthDialog.this.shell.close();
                    } catch (TwitterException e1) {
                        SwtUtils.errorDialog(e1, OauthDialog.this.shell);
                        OauthDialog.this.accessToken = null;
                    }
                }
            }
        });

        this.shell.pack();
    }

    /**
     * @return accessToken
     */
    public AccessToken getAccessToken() {
        return this.accessToken;
    }

    /**
     * @return user
     */
    public User getUser() {
        return this.user;
    }
}
