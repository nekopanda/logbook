package logbook.gui;

import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * 所有装備一覧
 *
 */
public final class ItemTable extends AbstractTableDialog {

    /**
     * @param parent
     */
    public ItemTable(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
    }

    @Override
    protected void createContents() {
        MenuItem itemCopy = new MenuItem(this.tablemenu, SWT.NONE);
        itemCopy.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                StringBuilder sb = new StringBuilder();
                for (TableItem item : ItemTable.this.table.getSelection()) {
                    sb.append("\"").append(item.getText(1)).append("\", ");
                }
                Clipboard clipboard = new Clipboard(Display.getDefault());
                clipboard.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
            }
        });
        itemCopy.setText("装備名をコピー(&1)");
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
