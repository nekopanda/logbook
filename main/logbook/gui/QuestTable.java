package logbook.gui;

import logbook.constants.AppConstants;
import logbook.data.Data;
import logbook.data.DataType;
import logbook.data.context.GlobalContext;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;
import logbook.scripting.TableItemCreatorProxy;
import logbook.util.SwtUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * 任務一覧テーブル
 *
 */
public final class QuestTable extends AbstractTableDialog {

    /**
     * @param parent
     */
    public QuestTable(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
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
    protected String getTitleMain() {
        return "任務一覧";
    }

    @Override
    protected Point getSize() {
        return SwtUtils.DPIAwareSize(new Point(600, 350));
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
        return TableItemCreatorProxy.get(AppConstants.QUESTTABLE_PREFIX);
    }

    /**
     * 更新する必要のあるデータ
     */
    @SuppressWarnings("incomplete-switch")
    @Override
    public void update(DataType type, Data data) {
        switch (type) {
        case QUEST_LIST:
        case QUEST_CLEAR:
            this.needsUpdate = true;
        }
    }
}
