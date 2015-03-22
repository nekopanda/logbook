package logbook.gui;

import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;
import logbook.scripting.TableItemCreatorProxy;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * 建造報告書
 *
 */
public final class CreateShipReportTable extends AbstractTableDialog {

    /**
     * @param parent
     */
    public CreateShipReportTable(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
    }

    @Override
    protected void createContents() {
    }

    @Override
    protected String getTitleMain() {
        return "建造報告書";
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getCreateShipHeader();
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getCreateShipBody(GlobalContext.getGetshipList());
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        return TableItemCreatorProxy.get(AppConstants.CREATESHIPTABLE_PREFIX);
    }
}
