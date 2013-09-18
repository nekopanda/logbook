/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.listener;

import logbook.gui.TableDialog;
import logbook.gui.logic.CreateReportLogic;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * 開発報告書ボタンのリスナー
 * 
 */
public final class CreateItemReportAdapter extends AbstractReportAdapter {

    /**
     * @param shell シェル
     */
    public CreateItemReportAdapter(Shell shell) {
        super(shell);
    }

    @Override
    public void widgetSelected(SelectionEvent paramSelectionEvent) {
        new TableDialog(this.shell, "開発報告書", CreateReportLogic.getCreateItemHeader(),
                CreateReportLogic.getCreateItemBody(), CreateReportLogic.DEFAULT_TABLE_ITEM_CREATOR).open();
    }
}
