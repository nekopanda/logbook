package logbook.gui.background;

import logbook.data.context.GlobalContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;

/**
 * 非同期にコンソールを更新します
 *
 */
public final class AsyncExecApplicationMainConsole extends Thread {
    private static final Logger LOG = LogManager.getLogger(AsyncExecApplicationMainConsole.class);
    private static final int UPDATE_FORMILIS = 500;
    private static final int MAX_LOG_LINES = 200;

    private final Display display;
    private final List console;

    /**
     * 非同期にコンソールを更新します
     * 
     * @param display
     * @param console
     */
    public AsyncExecApplicationMainConsole(Display display, List console) {
        this.display = display;
        this.console = console;
    }

    /**
     * 現在のメイン画面を更新します
     */
    @Override
    public void run() {
        try {
            while (true) {
                this.display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        // ログメッセージを取り出す
                        String message;
                        while ((message = GlobalContext.getConsoleMessage()) != null) {
                            List console = AsyncExecApplicationMainConsole.this.console;
                            int size = console.getItemCount();
                            if (size >= MAX_LOG_LINES) {
                                console.remove(0);
                            }
                            console.add(message);
                            console.setSelection(console.getItemCount() - 1);
                        }
                    }
                });
                Thread.sleep(UPDATE_FORMILIS);
            }
        } catch (Exception e) {
            LOG.fatal("スレッドが異常終了しました", e);
        }
    }
}
