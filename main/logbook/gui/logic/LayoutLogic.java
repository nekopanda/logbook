package logbook.gui.logic;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * レイアウトを操作します
 *
 */
public final class LayoutLogic {

    private static void recirsivelySetExclude(Control widget, boolean hide) {
        Object data = widget.getLayoutData();
        if (data instanceof GridData) {
            ((GridData) data).exclude = hide;
        }
        if (widget instanceof Composite) {
            Control[] controls = ((Composite) widget).getChildren();
            for (Control control : controls) {
                recirsivelySetExclude(control, hide);
            }
        }
    }

    /**
     * ウィジェットを非表示または表示します。
     * 
     * @param widget 
     * @param hide
     */
    public static void hide(Control widget, boolean hide) {
        widget.setVisible(!hide);
        recirsivelySetExclude(widget, hide);
    }

    /**
     * ウィジェットを無効または有効にします。
     * 
     * @param widget
     * @param enabled
     */
    public static void enable(Control widget, boolean enabled) {
        if (widget instanceof Composite) {
            Control[] controls = ((Composite) widget).getChildren();
            for (Control control : controls) {
                enable(control, enabled);
            }
        }
        widget.setEnabled(enabled);
    }
}
