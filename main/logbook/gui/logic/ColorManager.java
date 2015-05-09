/**
 * 
 */
package logbook.gui.logic;

import logbook.config.AppConfig;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author Nekopanda
 *
 */
public class ColorManager {

    public static Color getColor(RGB[] rgbs) {
        int index = AppConfig.get().isColorSupport() ? 1 : 0;
        return SWTResourceManager.getColor(rgbs[index]);
    }

    public static Color getColor(RGB rgb) {
        return SWTResourceManager.getColor(rgb);
    }

    public static Color getColor(int[] ids) {
        int index = AppConfig.get().isColorSupport() ? 1 : 0;
        return SWTResourceManager.getColor(ids[index]);
    }

    public static Color getColor(int id) {
        return SWTResourceManager.getColor(id);
    }

}
