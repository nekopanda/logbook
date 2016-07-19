package logbook.gui;

import logbook.constants.AppConstants;
import logbook.data.Data;
import logbook.data.DataType;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;
import logbook.scripting.TableItemCreatorProxy;
import logbook.util.SwtUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
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
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(item.getText(1));
                }
                Clipboard clipboard = new Clipboard(Display.getDefault());
                clipboard.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
            }
        });
        itemCopy.setText("装備名をコピー(&1)");
    }

    @Override
    protected String getTitleMain() {
        return "所有装備一覧";
    }

    @Override
    protected Point getSize() {
        return SwtUtils.DPIAwareSize(new Point(600, 350));
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
        return TableItemCreatorProxy.get(AppConstants.ITEMTABLE_PREFIX);
    }

    /**
     * 更新する必要のあるデータ
     */
    @SuppressWarnings("incomplete-switch")
    @Override
    public void update(DataType type, Data data) {
        switch (type) {
        case CHANGE:
        case PORT:
        case SHIP2:
        case SHIP3:
        case SLOTITEM_MEMBER:
        case GET_SHIP:
        case DESTROY_SHIP:
        case DESTROY_ITEM2:
        case POWERUP:
        case LOCK_SLOTITEM:
        case REMODEL_SLOT:
            this.needsUpdate = true;
        }
    }
}
