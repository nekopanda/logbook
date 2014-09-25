/**
 * 
 */
package logbook.gui.logic;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import logbook.gui.ApplicationMain;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 * SWTに実装されていないShell作成後のTopMost変更をするための実装
 * OS固有の操作が必要なため現在はWindowsのみをサポート
 */
public class WindowNativeSupport {

    private static int HWND_TOP = 0;
    private static int HWND_TOPMOST = -1;
    private static int HWND_NOTOPMOST = -2;
    private static int SWP_NOACTIVATE = 0x0010;

    private Method setWindowPosMethod;
    private Field handleField;

    public WindowNativeSupport() {
        // Windowsのみ対応
        try {
            Class<?> cls = Class.forName("org.eclipse.swt.internal.win32.OS");
            this.setWindowPosMethod = cls.getMethod("SetWindowPos",
                    int.class, int.class, int.class, int.class,
                    int.class, int.class, int.class);
            this.handleField = Control.class.getField("handle");
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | NoSuchMethodException e) {
        }
    }

    public boolean isTopMostAvailable() {
        return this.handleField != null;
    }

    /** ウィンドウを最前面に表示するかを設定 */
    public void setTopMost(Shell shell, boolean topMost) {
        try {
            int insertAfter = topMost ? HWND_TOPMOST : HWND_NOTOPMOST;
            Rectangle rect = shell.getBounds();
            Object[] args = new Object[] {
                    this.handleField.get(shell), insertAfter,
                    rect.x, rect.y, rect.width, rect.height, 0 };
            this.setWindowPosMethod.invoke(null, args);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            ApplicationMain.main.printMessage("ウィンドウ操作に失敗しました");
        }
    }

    /**
     *  shellをbehindToの直後に持ってくる
     *  behindToが親ウィンドウ、shellが子ウィンドウとして、親ウィンドウがアクティブになった時の子ウィンドウの動作を再現
     */
    public void setBehindTo(Shell shell, Shell behindTo) {
        try {
            Rectangle rect = shell.getBounds();
            int insertAfter = (behindTo != null) ? (int) this.handleField.get(behindTo) : HWND_TOP;
            Object[] args = new Object[] {
                    this.handleField.get(shell), insertAfter,
                    rect.x, rect.y, rect.width, rect.height, SWP_NOACTIVATE };
            this.setWindowPosMethod.invoke(null, args);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            ApplicationMain.main.printMessage("ウィンドウ操作に失敗しました");
        }
    }
}
