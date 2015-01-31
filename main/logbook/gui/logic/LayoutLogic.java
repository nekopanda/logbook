package logbook.gui.logic;

import java.util.Map;

import logbook.config.AppConfig;
import logbook.config.bean.WindowLocationBean;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

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

    /**
     * Shellのウインドウ位置とサイズを読み込み適用します
     * 
     * @param clazz ウインドウクラス
     * @param shell Shell
     */
    public static void applyWindowLocation(Class<? extends Dialog> clazz, Shell shell) {
        Map<String, WindowLocationBean> map = AppConfig.get().getWindowLocationMap();
        WindowLocationBean location;
        synchronized (map) {
            location = map.get(clazz.getName());
        }
        if (location != null) {
            if ((location.getWidth() > 0) && (location.getHeight() > 0)) {
                shell.setLocation(location.getX(), location.getY());
                shell.setSize(location.getWidth(), location.getHeight());
            }
        }
    }

    /**
     * Shellのウインドウ位置とサイズを保存します
     * 
     * @param clazz ウインドウクラス
     * @param shell Shell
     */
    public static void saveWindowLocation(Class<? extends Dialog> clazz, Shell shell) {
        Map<String, WindowLocationBean> map = AppConfig.get().getWindowLocationMap();
        Point location = shell.getLocation();
        Point size = shell.getSize();
        WindowLocationBean wlocation = new WindowLocationBean();
        wlocation.setX(location.x);
        wlocation.setY(location.y);
        wlocation.setWidth(size.x);
        wlocation.setHeight(size.y);
        synchronized (map) {
            map.put(clazz.getName(), wlocation);
        }
    }
}
