package logbook.gui.listener;

import logbook.gui.CreateShipReportTable;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * 建造報告書ボタンのリスナー
 * 
 */
public final class CreateShipReportAdapter extends AbstractReportAdapter {

    /**
     * @param shell シェル
     */
    public CreateShipReportAdapter(Shell shell) {
        super(shell);
    }

    @Override
    public void widgetSelected(SelectionEvent paramSelectionEvent) {
        new CreateShipReportTable(this.shell).open();
    }
}
