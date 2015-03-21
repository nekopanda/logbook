package logbook.gui;

import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;
import logbook.scripting.TableItemCreatorProxy;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 遠征報告書
 *
 */
public final class MissionResultTable extends AbstractTableDialog {

    /**
     * @param parent
     */
    public MissionResultTable(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
    }

    @Override
    protected void createContents() {
    }

    @Override
    protected String getTitleMain() {
        return "遠征報告書";
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getMissionResultHeader();
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getMissionResultBody(GlobalContext.getMissionResultList());
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        return TableItemCreatorProxy.get(AppConstants.MISSIONRESULTTABLE_PREFIX);
    }

    @Override
    protected SelectionListener getHeaderSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.getSource() instanceof TableColumn) {
                    MissionResultTable.this.sortTableItems((TableColumn) e.getSource());
                }
            }
        };
    }
}
