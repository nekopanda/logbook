/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.listener;

import logbook.gui.ConfigDialog;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * メニューから経験値計算機を押した場合のリスナー
 *
 */
public final class ConfigDialogAdapter extends SelectionAdapter {

    /** シェル */
    private final Shell shell;

    /**
     * コンストラクター
     * 
     * @param shell シェル
     */
    public ConfigDialogAdapter(Shell shell) {
        this.shell = shell;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        new ConfigDialog(this.shell).open();
    }
}
