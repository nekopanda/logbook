/**
 * 
 */
package logbook.scripting;

import logbook.dto.BattleExDto;

/**
 * @author Nekopanda
 *
 */
public interface BattleLogListener extends TableScriptListener {
    public void begin();

    public Comparable[] body(BattleExDto battle);

    public void end();
}
