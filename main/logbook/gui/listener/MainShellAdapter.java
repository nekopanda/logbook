/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.listener;

import logbook.config.AppConfig;

import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * シェルイベント
 */
public final class MainShellAdapter extends ShellAdapter {

    @Override
    public void shellIconified(ShellEvent e) {
        if (AppConfig.get().isHideWindow()) {
            Display.getDefault().asyncExec(new AsyncHide((Shell) e.getSource()));
        }
    }

    /**
     * 非同期でウインドウを非表示に設定する
     */
    private static class AsyncHide implements Runnable {

        private final Shell shell;

        /**
         * コンストラクター
         */
        public AsyncHide(Shell shell) {
            this.shell = shell;
        }

        @Override
        public void run() {
            try {
                this.shell.setMinimized(true);
                // 最小化のアニメーションが終わるまでとりあえず100ms待つ
                Thread.sleep(100);
                // シェルを非表示にする
                this.shell.setVisible(false);
            } catch (InterruptedException e) {
            }
        }
    }
}
