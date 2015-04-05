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
    private static int SWP_NOSIZE = 0x0001;
    private static int SWP_NOMOVE = 0x0002;
    private static int SWP_NOACTIVATE = 0x0010;
    private static int SWP_NOOWNERZORDER = 0x0200;
    private static int GWL_STYLE = -16;
    private static int WS_CAPTION = 0x00C00000;

    private WindowsHandler handler;

    private static interface HandleConverter {
        Number fromInt(int value);
    }

    /** x86とx64の違いはHWNDがintかlongかだけなので共通化 */
    private static class WindowsHandler {
        private final Method setWindowPosMethod;
        private final Method getWindowLongMethod;
        private final Method setWindowLongMethod;
        private final Field handleField;
        private final HandleConverter handleConverter;

        public WindowsHandler(Class<?> handleType, HandleConverter handleConverter)
                throws ReflectiveOperationException {
            this.handleConverter = handleConverter;
            Class<?> cls = Class.forName("org.eclipse.swt.internal.win32.OS");
            this.setWindowPosMethod = cls.getMethod("SetWindowPos",
                    handleType, handleType, int.class, int.class,
                    int.class, int.class, int.class);
            this.getWindowLongMethod = cls.getMethod("GetWindowLong",
                    handleType, int.class);
            this.setWindowLongMethod = cls.getMethod("SetWindowLong",
                    handleType, int.class, int.class);
            this.handleField = Control.class.getField("handle");
        }

        private Number fromInt(int value) {
            return this.handleConverter.fromInt(value);
        }

        public void setTopMost(Shell shell, boolean topMost) {
            try {
                Object insertAfter = this.fromInt(topMost ? HWND_TOPMOST : HWND_NOTOPMOST);
                Object[] args = new Object[] {
                        this.handleField.get(shell), insertAfter, 0, 0, 0, 0,
                        SWP_NOMOVE | SWP_NOSIZE };
                this.setWindowPosMethod.invoke(null, args);
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                ApplicationMain.main.printMessage("ウィンドウ操作に失敗しました");
            }
        }

        public void setBehindTo(Shell shell, Shell behindTo) {
            try {
                Rectangle rect = shell.getBounds();
                Object insertAfter = (behindTo != null) ? this.handleField.get(behindTo) : this.fromInt(HWND_TOP);
                Object[] args = new Object[] {
                        this.handleField.get(shell), insertAfter, 0, 0, 0, 0,
                        SWP_NOACTIVATE | SWP_NOMOVE | SWP_NOSIZE };
                this.setWindowPosMethod.invoke(null, args);
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                ApplicationMain.main.printMessage("ウィンドウ操作に失敗しました");
            }
        }

        public void toggleTitlebar(Shell shell, boolean show) {
            try {
                Object[] args = new Object[] { this.handleField.get(shell), GWL_STYLE };
                int style = (Integer) this.getWindowLongMethod.invoke(null, args);
                if (show) {
                    style |= WS_CAPTION;
                }
                else {
                    style &= ~WS_CAPTION;
                }
                args = new Object[] { this.handleField.get(shell), GWL_STYLE, style };
                this.setWindowLongMethod.invoke(null, args);
                // 反映させる 39 = SWP_DRAWFRAME |SWP_NOMOVE |SWP_NOSIZE |SWP_NOZORDER
                args = new Object[] {
                        this.handleField.get(shell), this.fromInt(0), 0, 0, 0, 0, 39 };
                this.setWindowPosMethod.invoke(null, args);
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                ApplicationMain.main.printMessage("ウィンドウ操作に失敗しました");
            }
        }
    }

    public WindowNativeSupport() {
        try {
            // x86を試す
            this.handler = new WindowsHandler(int.class, new HandleConverter() {
                @Override
                public Number fromInt(int value) {
                    return value;
                }
            });
        } catch (ReflectiveOperationException e) {
            try {
                // x64を試す
                this.handler = new WindowsHandler(long.class, new HandleConverter() {
                    @Override
                    public Number fromInt(int value) {
                        return (long) value;
                    }
                });
            } catch (ReflectiveOperationException e2) {
                // 今のところWindowsのみ対応
                this.handler = null;
            }
        }
    }

    public boolean isTopMostAvailable() {
        return this.handler != null;
    }

    /** ウィンドウを最前面に表示するかを設定 */
    public void setTopMost(Shell shell, boolean topMost) {
        this.handler.setTopMost(shell, topMost);
    }

    /**
     *  shellをbehindToの直後に持ってくる
     *  behindToが親ウィンドウ、shellが子ウィンドウとして、親ウィンドウがアクティブになった時の子ウィンドウの動作を再現
     */
    public void setBehindTo(Shell shell, Shell behindTo) {
        this.handler.setBehindTo(shell, behindTo);
    }

    /**
     *  タイトルバーの表示非表示切り替え
     */
    public void toggleTitlebar(Shell shell, boolean show) {
        this.handler.toggleTitlebar(shell, show);
    }

}
