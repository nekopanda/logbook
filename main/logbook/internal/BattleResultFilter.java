/**
 * 
 */
package logbook.internal;

import java.util.Date;

import logbook.dto.ResultRank;
import logbook.gui.logic.IntegerPair;

/**
 * @author Nekopanda
 *
 */
public class BattleResultFilter {

    public Date fromTime = null;
    public Date toTime = null;
    public TimeSpanKind timeSpan = null;
    public String dropShip = null;
    public IntegerPair map = null;
    public Integer cell = null;
    public ResultRank rank = null;
    public Boolean printPractice = null;

}
