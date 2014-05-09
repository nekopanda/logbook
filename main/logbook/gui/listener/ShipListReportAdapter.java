package logbook.gui.listener;

import logbook.gui.ShipTable;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * 所有艦娘一覧ボタンのリスナー
 * 
 */
public final class ShipListReportAdapter extends AbstractReportAdapter {

    /**
     * @param shell シェル
     */
    public ShipListReportAdapter(Shell shell) {
        super(shell);
    }

    @Override
    public void widgetSelected(SelectionEvent paramSelectionEvent) {
        new ShipTable(this.shell).open();
    }
}
