package logbook.gui.listener;

import logbook.gui.VersionDialog;
import logbook.gui.WindowBase;

import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * ヘルプキーを押した場合のリスナー
 *
 */
public final class HelpEventListener extends SelectionAdapter implements HelpListener {

    /** シェル */
    private final WindowBase parent;

    /**
     * コンストラクター
     * 
     * @param shell シェル
     */
    public HelpEventListener(WindowBase parent) {
        this.parent = parent;
    }

    @Override
    public void helpRequested(HelpEvent paramHelpEvent) {
        new VersionDialog(this.parent).open();
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        new VersionDialog(this.parent).open();
    }
}
