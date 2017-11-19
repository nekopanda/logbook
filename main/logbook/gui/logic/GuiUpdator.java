/**
 * 
 */
package logbook.gui.logic;

import logbook.internal.LoggerHolder;

import org.eclipse.swt.widgets.Display;

/**
 * 無駄に更新されるのを防ぐためこれを挟む
 * @author Nekopanda
 */
public class GuiUpdator implements Runnable {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(GuiUpdator.class);

    private boolean needUpdate = false;

    private final Runnable exec;

    public GuiUpdator(final Runnable listener) {
        this.exec = new Runnable() {
            @Override
            public void run() {
                if (GuiUpdator.this.needUpdate) {
                    try {
                        listener.run();
                    } catch (Exception e) {
                        LOG.get().warn("GuiUpdatorでエラー", e);
                    }
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
