/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.listener;

import logbook.gui.Help;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Shell;

/**
 * ヘルプキーを押した場合のリスナー
 *
 */
public final class HelpEventListener implements HelpListener {

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
        new Help(this.shell, SWT.SHELL_TRIM | SWT.MODELESS).open();
    }

}
