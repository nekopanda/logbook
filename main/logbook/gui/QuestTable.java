package logbook.gui;

import logbook.data.context.GlobalContext;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 任務一覧テーブル
 *
 */
public final class QuestTable extends AbstractTableDialog {

    /**
     * @param parent
     */
    public QuestTable(Shell parent) {
        super(parent);
    }

    @Override
    protected void createContents() {
        // 任務をリセット
        final MenuItem switchdiff = new MenuItem(this.opemenu, SWT.NONE);
        switchdiff.setText("任務をリセット");
        switchdiff.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                GlobalContext.getQuest().clear();
                QuestTable.this.reloadTable();
            }
        });
    }

    @Override
    protected String getTitle() {
        return "任務一覧";
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getCreateQuestHeader();
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getQuestBody();
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
                    QuestTable.this.sortTableItems((TableColumn) e.getSource());
                }
            }
        };
    }
}
