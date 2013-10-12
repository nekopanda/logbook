/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
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
 * 所有艦娘一覧テーブル
 *
 */
public final class ShipTable extends AbstractTableDialog {

    /**
     * @param parent
     */
    public ShipTable(Shell parent) {
        super(parent);
    }

    @Override
    protected void createContents() {
    }

    @Override
    protected String getTitle() {
        return "所有艦娘一覧";
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getShipListHeader();
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getShipListBody();
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        return CreateReportLogic.SHIP_LIST_TABLE_ITEM_CREATOR;
    }

    @Override
    protected SelectionListener getHeaderSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.getSource() instanceof TableColumn) {
                    ShipTable.this.sortTableItems((TableColumn) e.getSource());
                }
            }
        };
    }
}
