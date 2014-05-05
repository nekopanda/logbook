package logbook.gui;

import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 所有装備一覧
 *
 */
public final class ItemTable extends AbstractTableDialog {

    /**
     * @param parent
     */
    public ItemTable(Shell parent) {
        super(parent);
    }

    @Override
    protected void createContents() {
    }

    @Override
    protected String getTitle() {
        return "所有装備一覧";
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getItemListHeader();
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getItemListBody();
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        return CreateReportLogic.DEFAULT_TABLE_ITEM_CREATOR;
    }

    @Override
    protected SelectionListener getHeaderSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.getSource() instanceof TableColumn) {
                    ItemTable.this.sortTableItems((TableColumn) e.getSource());
                }
            }
        };
    }
}
