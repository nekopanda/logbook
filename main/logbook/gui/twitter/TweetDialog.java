/**
 * 
 */
package logbook.gui.twitter;

import java.io.File;
import java.io.IOException;

import logbook.gui.ApplicationMain;
import logbook.gui.WindowBase;
import logbook.internal.LoggerHolder;
import logbook.util.SwtUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import twitter4j.TwitterException;

/**
 * @author Nekopanda
 *
 */
public class TweetDialog extends WindowBase {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(TweetDialog.class);

    private final WindowBase parent;
    private Shell shell;
    private final File imageFile;

    /**
     * Create the dialog.
     * @param parent
     */
    public TweetDialog(WindowBase parent, File imageFile) {
        this.parent = parent;
        this.imageFile = imageFile;
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
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        // シェル
        super.createContents(this.parent, SWT.CLOSE | SWT.TITLE, false);
        this.getShell().setText("つぶやく");
        this.shell = this.getShell();

        // レイアウト
        GridLayout glShell = new GridLayout(3, false);
        //        glShell.horizontalSpacing = 1;
        glShell.marginHeight = 10;
        glShell.marginWidth = 10;
        //        glShell.verticalSpacing = 1;
        this.shell.setLayout(glShell);

        Label thumnail = new Label(this.shell, SWT.NONE);
        GridData gdThumnail = new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1);
        thumnail.setLayoutData(gdThumnail);
        try {
            Image orig = SwtUtils.makeImage(this.imageFile);
            thumnail.setImage(SwtUtils.scaleToFit(orig, 400, 300));
            orig.dispose();
        } catch (IOException e2) {
            SwtUtils.errorDialog(e2, TweetDialog.this.shell);
        }

        final Text text = new Text(this.shell, SWT.MULTI | SWT.BORDER | SWT.WRAP);
        GridData gdText = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        gdText.widthHint = SwtUtils.DPIAwareWidth(300);
        gdText.heightHint = SwtUtils.DPIAwareHeight(80);
        text.setLayoutData(gdText);

        Label userName = new Label(this.shell, SWT.NONE);
        userName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        try {
            userName.setText(TwitterClient.getInstance().getUser().getScreenName());
        } catch (TwitterException e1) {
            SwtUtils.errorDialog(e1, TweetDialog.this.shell);
        }

        final Label remainChars = new Label(this.shell, SWT.NONE);
        remainChars.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        text.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                int remain = 117 - text.getText().length();
                remainChars.setText(String.valueOf(remain));
            }
        });
        text.setText("");

        Button tweet = new Button(this.shell, SWT.NONE);
        GridData gdTweet = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        gdTweet.widthHint = SwtUtils.DPIAwareWidth(100);
        tweet.setLayoutData(gdTweet);
        tweet.setText("つぶやく");
        tweet.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    TwitterClient.getInstance().tweet(
                            TweetDialog.this, text.getText(), TweetDialog.this.imageFile);
                    TweetDialog.this.shell.close();
                    ApplicationMain.logPrint("つぶやきました");
                } catch (TwitterException e1) {
                    SwtUtils.errorDialog(e1, TweetDialog.this.shell);
                }
            }
        });

        this.shell.pack();
    }
}
