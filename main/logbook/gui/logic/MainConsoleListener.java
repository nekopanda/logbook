/**
 * 
 */
package logbook.gui.logic;

import org.eclipse.swt.widgets.List;

/**
 * コンソールを更新するイベントハンドラ
 *
 */
public class MainConsoleListener {
    private static final int MAX_LOG_LINES = 200;

    private final List console;

    /**
     * コンソールを更新するイベントハンドラ
     * 
     * @param console
     */
    public MainConsoleListener(List console) {
        this.console = console;
    }

    /**
     * コンソールを更新します
     * @param message コンソールに表示するメッセージ
     */
    public void printMessage(final String message) {
        int size = this.console.getItemCount();
        if (size >= MAX_LOG_LINES) {
            this.console.remove(0);
        }
        this.console.add(message);
        this.console.setSelection(MainConsoleListener.this.console.getItemCount() - 1);
    }
}
