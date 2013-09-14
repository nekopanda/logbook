/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Shell;

/**
 * 報告書ボタン
 *
 */
public abstract class AbstractReportAdapter extends SelectionAdapter {

    /** シェル */
    protected final Shell shell;

    /**
     * コンストラクター
     * 
     * @param shell シェル
     */
    public AbstractReportAdapter(Shell shell) {
        this.shell = shell;
    }
}
