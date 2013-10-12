/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.listener;

import logbook.gui.ItemTable;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * 所有装備一覧ボタンのリスナー
 * 
 */
public final class ItemListReportAdapter extends AbstractReportAdapter {

    /**
     * @param shell シェル
     */
    public ItemListReportAdapter(Shell shell) {
        super(shell);
    }

    @Override
    public void widgetSelected(SelectionEvent paramSelectionEvent) {
        new ItemTable(this.shell).open();
    }
}
