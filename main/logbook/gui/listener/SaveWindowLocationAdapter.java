package logbook.gui.listener;

import logbook.gui.logic.LayoutLogic;

import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

/**
 * ウインドウが閉じた時にウインドウ位置とサイズを保存するアダプターです
 *
 */
public final class SaveWindowLocationAdapter extends ShellAdapter {

    private final Class<? extends Dialog> dialogClass;

    /**
     * コンストラクター
     * 
     * @param dialog　ウインドウ
     */
    public SaveWindowLocationAdapter(Class<? extends Dialog> dialogClass) {
        this.dialogClass = dialogClass;
    }

    @Override
    public void shellClosed(ShellEvent e) {
        if (e.widget instanceof Shell) {
            LayoutLogic.saveWindowLocation(this.dialogClass, (Shell) e.widget);
        }
    }
}
