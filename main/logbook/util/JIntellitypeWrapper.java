/**
 * 
 */
package logbook.util;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

/**
 * @author Nekopanda
 *
 */
public class JIntellitypeWrapper {
    private static boolean initialized = false;
    private static JIntellitype instance = null;
    private static int currentSetting = 0;

    public static JIntellitype getInstance() {
        if (initialized == false) {
            try {
                instance = JIntellitype.getInstance();
            } catch (Exception e) {
                instance = null;
            }
            initialized = true;
        }
        return instance;
    }

    public static void addListener(HotkeyListener listener) {
        if (getInstance() == null) {
            return;
        }
        instance.addHotKeyListener(listener);
    }

    public static void changeSetting(int newSetting) {
        if (getInstance() == null) {
            return;
        }
        if (currentSetting != newSetting) {
            if (currentSetting != 0) {
                instance.unregisterHotKey(currentSetting);
            }
            switch (newSetting) {
            case 0:
                break;
            case 1:
                instance.registerHotKey(newSetting, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, 'Z');
                break;
            case 2:
                instance.registerHotKey(newSetting, JIntellitype.MOD_CONTROL + JIntellitype.MOD_ALT, 'Z');
                break;
            }
            currentSetting = newSetting;
        }
    }

    public static void cleanup() {
        if (getInstance() == null) {
            return;
        }
        instance.cleanUp();
        currentSetting = 0;
        instance = null;
    }
}
