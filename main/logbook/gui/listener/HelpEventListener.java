/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.listener;

import logbook.gui.VersionDialog;

import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

/**
 * ヘルプキーを押した場合のリスナー
 *
 */
public final class HelpEventListener extends SelectionAdapter implements HelpListener {

    /** シェル */
    private final Shell shell;

    /**
     * コンストラクター
     * 
     * @param shell シェル
     */
    public HelpEventListener(Shell shell) {
        this.shell = shell;
    }

    @Override
    public void helpRequested(HelpEvent paramHelpEvent) {
        new VersionDialog(this.shell).open();
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        new VersionDialog(this.shell).open();
    }
}
