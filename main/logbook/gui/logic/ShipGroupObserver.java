/**
 * 
 */
package logbook.gui.logic;

import java.util.ArrayList;
import java.util.List;

import logbook.config.bean.ShipGroupBean;

/**
 * @author Nekopanda
 *
 */
public class ShipGroupObserver {

    private static List<ShipGroupListener> listeners = new ArrayList<ShipGroupListener>();

    public static void addListener(ShipGroupListener listener) {
        listeners.add(listener);
    }

    public static boolean removeListener(ShipGroupListener listener) {
        return listeners.remove(listener);
    }

    public static void listChanged() {
        for (ShipGroupListener listener : listeners) {
            listener.listChanged();
        }
    }

    public static void groupChanged(ShipGroupBean group) {
        for (ShipGroupListener listener : listeners) {
            listener.groupChanged(group);
        }
    }
}
