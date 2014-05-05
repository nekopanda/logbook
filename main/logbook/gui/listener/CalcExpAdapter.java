package logbook.gui.listener;

import logbook.gui.CalcExpDialog;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * メニューから経験値計算機を押した場合のリスナー
 *
 */
public final class CalcExpAdapter extends SelectionAdapter {

    /** シェル */
    private final Shell shell;

    /**
     * コンストラクター
     * 
     * @param shell シェル
     */
    public CalcExpAdapter(Shell shell) {
        this.shell = shell;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        new CalcExpDialog(this.shell).open();
    }
}
