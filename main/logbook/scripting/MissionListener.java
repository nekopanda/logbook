/**
 * 
 */
package logbook.scripting;

import logbook.internal.MasterData.MissionDto;

/**
 * @author Nekopanda
 *
 */
public interface MissionListener extends TableScriptListener {
    void begin(int fleetid);

    Comparable[] body(MissionDto data);

    void end();
}
