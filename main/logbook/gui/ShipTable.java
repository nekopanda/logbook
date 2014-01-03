/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui;

import logbook.dto.ShipFilterDto;
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
 * 所有艦娘一覧テーブル
 *
 */
public final class ShipTable extends AbstractTableDialog {

    /** 成長余地 */
    private static boolean specdiff = false;

    /** フィルター */
    private ShipFilterDto filter;

    /**
     * @param parent
     */
    public ShipTable(Shell parent) {
        super(parent);
    }

    /**
     * フィルターを設定する
     * @param filter フィルター
     */
    public void updateFilter(ShipFilterDto filter) {
        this.filter = filter;
        this.reloadTable();
    }

    @Override
    protected void createContents() {
        // メニューバーに追加する
        // フィルターメニュー
        final MenuItem filter = new MenuItem(this.opemenu, SWT.PUSH);
        filter.setText("フィルター(&F)\tCtrl+F");
        filter.setAccelerator(SWT.CTRL + 'F');
        filter.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ShipFilterDialog(ShipTable.this.shell, ShipTable.this, ShipTable.this.filter).open();
            }
        });
        // セパレータ
        new MenuItem(this.opemenu, SWT.SEPARATOR);
        // 成長の余地を表示メニュー
        final MenuItem switchdiff = new MenuItem(this.opemenu, SWT.CHECK);
        switchdiff.setText("成長の余地を表示");
        switchdiff.setSelection(specdiff);
        switchdiff.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                specdiff = switchdiff.getSelection();
                ShipTable.this.reloadTable();
            }
        });
        // 右クリックメニューに追加する
        final MenuItem filtertable = new MenuItem(this.tablemenu, SWT.NONE);
        filtertable.setText("フィルター(&F)");
        filtertable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new ShipFilterDialog(ShipTable.this.shell, ShipTable.this, ShipTable.this.filter).open();
            }
        });

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
        this.body = CreateReportLogic.getShipListBody(ShipTable.specdiff, this.filter);
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
