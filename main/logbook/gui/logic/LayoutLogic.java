/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.logic;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * レイアウトを操作します
 *
 */
public final class LayoutLogic {

    /**
     * ウィジェットを非表示または表示します。
     * 
     * @param widget 
     * @param hide
     */
    public static void hide(Control widget, boolean hide) {
        if (widget instanceof Composite) {
            Control[] controls = ((Composite) widget).getChildren();
            for (Control control : controls) {
                hide(control, hide);
            }
        }
        Object data = widget.getLayoutData();
        if (data instanceof GridData) {
            ((GridData) data).exclude = hide;
            widget.setVisible(!hide);
        }
    }
}
