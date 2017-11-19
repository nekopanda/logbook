package logbook.gui.listener;

import logbook.internal.LoggerHolder;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * トレイアイコンをクリックした場合の動作
 */
public final class TraySelectionListener implements Listener {

    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(TraySelectionListener.class);

    private final Shell shell;

    /**
     * コンストラクター
     */
    public TraySelectionListener(Shell shell) {
        this.shell = shell;
    }

    @Override
    public void handleEvent(Event paramEvent) {
        Display.getDefault().asyncExec(new AsyncActive(this.shell));
    }

    /**
     * 非同期でウインドウをアクティブに設定する
     */
    private static class AsyncActive implements Runnable {

        private final Shell shell;

        /**
         * コンストラクター
         */
        public AsyncActive(Shell shell) {
            this.shell = shell;
        }

        @Override
        public void run() {
            try {
                if (!this.shell.getVisible()) {
                    this.shell.setVisible(true);
                    // タスクバーに表示されるまでとりあえず100ms待ってみる
                    Thread.sleep(100);
                }
                this.shell.setMinimized(false);
                this.shell.setActive();
                this.shell.forceActive();
            } catch (InterruptedException e) {
            } catch (Exception e) {
                LOG.get().warn("不明なエラー", e);
            }
        }
    }
}
