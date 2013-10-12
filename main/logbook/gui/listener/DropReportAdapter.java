/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.listener;

import logbook.gui.DropReportTable;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * ドロップ報告書ボタンのリスナー
 * 
 */
public final class DropReportAdapter extends AbstractReportAdapter {

    /**
     * @param shell シェル
     */
    public DropReportAdapter(Shell shell) {
        super(shell);
    }

    @Override
    public void widgetSelected(SelectionEvent paramSelectionEvent) {
        new DropReportTable(this.shell).open();
    }
}
