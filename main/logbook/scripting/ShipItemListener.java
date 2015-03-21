/**
 * 
 */
package logbook.scripting;

import logbook.dto.ShipDto;

/**
 * @author Nekopanda
 *
 */
public interface ShipItemListener extends TableScriptListener {
    public void begin(boolean specdiff);

    public Comparable[] body(ShipDto ship);

    public void end();
}
