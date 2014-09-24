/**
 * 
 */
package logbook.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 *
 */
public class BattleDetailDialog extends WindowBase {

    private final WindowBase parent;
    private Browser browser;

    public BattleDetailDialog(WindowBase parent) {
        this.parent = parent;
    }

    /**
     * Open the dialog.
     * @return the result
     */
    @Override
    public void open() {
        if (!this.isWindowInitialized()) {
            this.createContents();
            super.registerEvents();
            // 閉じたときに dispose しない
            this.getShell().addShellListener(new ShellAdapter() {
                @Override
                public void shellClosed(ShellEvent e) {
                    e.doit = false;
                    BattleDetailDialog.this.setVisible(false);
                }
            });
            this.setWindowInitialized(true);
        }
        this.setVisible(true);
        this.getShell().setActive();
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        super.createContents(this.parent, SWT.CLOSE | SWT.TITLE | SWT.MAX | SWT.MIN | SWT.RESIZE, false);
        Shell shell = this.getShell();
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        this.browser = new Browser(shell, SWT.NONE);
    }

    public void setBattle(String html, String title) {
        if ((html == null) || (title == null)) {
            return;
        }
        Shell shell = this.getShell();
        shell.setText(title);
        this.browser.setText(html);
    }
}
