package logbook.gui.listener;

import logbook.gui.MissionResultTable;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * 遠征報告書ボタンのリスナー
 * 
 */
public final class MissionResultReportAdapter extends AbstractReportAdapter {

    /**
     * @param shell シェル
     */
    public MissionResultReportAdapter(Shell shell) {
        super(shell);
    }

    @Override
    public void widgetSelected(SelectionEvent paramSelectionEvent) {
        new MissionResultTable(this.shell).open();
    }
}
