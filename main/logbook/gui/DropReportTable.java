package logbook.gui;

import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * ドロップ報告書
 *
 */
public final class DropReportTable extends AbstractTableDialog {

    /**
     * @param parent
     */
    public DropReportTable(Shell parent) {
        super(parent);
    }

    @Override
    protected void createContents() {
        this.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                TableItem[] items = DropReportTable.this.table.getSelection();
                for (TableItem tableItem : items) {
                    new BattleDialog(DropReportTable.this.shell, tableItem.getText(0)).open();
                }
            }
        });
    }

    @Override
    protected String getTitle() {
        return "ドロップ報告書";
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getBattleResultHeader();
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getBattleResultBody();
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
                    DropReportTable.this.sortTableItems((TableColumn) e.getSource());
                }
            }
        };
    }
}
