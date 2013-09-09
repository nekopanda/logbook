package logbook.gui.listener;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * トレイアイコンをクリックした場合の動作
 */
public final class TraySelectionListener implements Listener {

    private final Shell shell;

    /**
     * コンストラクター
     */
    public TraySelectionListener(Shell shell) {
        this.shell = shell;
    }

    @Override
    public void handleEvent(Event paramEvent) {
        this.shell.forceActive();
    }

}
