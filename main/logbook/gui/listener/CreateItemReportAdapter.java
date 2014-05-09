package logbook.gui.listener;

import logbook.gui.CreateItemReportTable;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * 開発報告書ボタンのリスナー
 * 
 */
public final class CreateItemReportAdapter extends AbstractReportAdapter {

    /**
     * @param shell シェル
     */
    public CreateItemReportAdapter(Shell shell) {
        super(shell);
    }

    @Override
    public void widgetSelected(SelectionEvent paramSelectionEvent) {
        new CreateItemReportTable(this.shell).open();
    }
}
