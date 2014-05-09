package logbook.gui.listener;

import logbook.gui.BathwaterTableDialog;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * お風呂に入りたい艦娘を押した場合のリスナー
 *
 */
public final class BathwaterTableAdapter extends SelectionAdapter {

    /**
     * シェル
     */
    private final Shell shell;

    /**
     * コンストラクター
     */
    public BathwaterTableAdapter(Shell shell) {
        this.shell = shell;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        new BathwaterTableDialog(this.shell).open();
    }
}
