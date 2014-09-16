/**
 * 
 */
package logbook.gui.logic;

import logbook.config.bean.ShipGroupBean;

/**
 * @author Nekopanda
 *
 */
public interface ShipGroupListener {

    void listChanged();

    void groupChanged(ShipGroupBean group);
}
