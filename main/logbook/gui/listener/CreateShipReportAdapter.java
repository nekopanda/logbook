/**
 * 
 */
package logbook.gui.listener;

import logbook.gui.TableDialog;
import logbook.gui.logic.CreateReportLogic;

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
        new TableDialog(this.shell, "建造報告書", CreateReportLogic.getCreateShipHeader(),
                CreateReportLogic.getCreateShipBody()).open();
    }
}
