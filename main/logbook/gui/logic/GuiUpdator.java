/**
 * 
 */
package logbook.gui.logic;

import org.eclipse.swt.widgets.Display;

/**
 * 無駄に更新されるのを防ぐためこれを挟む
 * @author Nekopanda
 */
public class GuiUpdator implements Runnable {
    private boolean needUpdate = false;

    private final Runnable exec;

    public GuiUpdator(final Runnable listener) {
        this.exec = new Runnable() {
            @Override
            public void run() {
                if (GuiUpdator.this.needUpdate) {
                    listener.run();
                    GuiUpdator.this.needUpdate = false;
                }
            }
        };
    }

    @Override
    public void run() {
        this.needUpdate = true;
        Display.getDefault().asyncExec(this.exec);
    }
}
