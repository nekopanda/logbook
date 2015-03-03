/**
 * 
 */
package logbook.gui.logic;

import logbook.constants.AppConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author Nekopanda
 *
 */
public enum DamageRate {

    MUKIZU("無傷", 0, null, null),
    KENZAI("健在", 1, null, null),
    SYOHA("小破", 2, SWTResourceManager.getColor(AppConstants.SYOHA_SHIP_COLOR), null),
    TYUHA("中破", 3,
            SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR),
            SWTResourceManager.getColor(SWT.COLOR_WHITE)),
    TAIHA("大破", 4,
            SWTResourceManager.getColor(AppConstants.COND_RED_COLOR),
            SWTResourceManager.getColor(SWT.COLOR_WHITE)),
    GOTIN("轟沈", 5,
            SWTResourceManager.getColor(AppConstants.SUNK_SHIP_COLOR),
            SWTResourceManager.getColor(SWT.COLOR_WHITE)),
    ESCAPED("退避", -1,
            SWTResourceManager.getColor(AppConstants.ESCAPED_SHIP_COLOR), null);

    private final String str;
    private final int level;
    private final Color background;
    private final Color foreground;

    private DamageRate(String str, int level, Color fb, Color ff) {
        this.str = str;
        this.level = level;
        this.background = fb;
        this.foreground = ff;
    }

    public static DamageRate fromHP(int nowhp, int maxhp) {
        double rate = (double) nowhp / (double) maxhp;
        if (rate == 0.0) {
            return GOTIN;
        }
        else if (rate <= AppConstants.BADLY_DAMAGE) {
            return TAIHA;
        }
        else if (rate <= AppConstants.HALF_DAMAGE) {
            return TYUHA;
        }
        else if (rate <= AppConstants.SLIGHT_DAMAGE) {
            return SYOHA;
        }
        else if (rate < 1.0) {
            return KENZAI;
        }
        return MUKIZU;
    }

    @Override
    public String toString() {
        return this.str;
    }

    public String toString(boolean friend) {
        if ((friend == false) && (this.level == 5)) {
            return "撃沈";
        }
        return this.str;
    }

    public int getLevel() {
        return this.level;
    }

    /**
     * @return friendBackground
     */
    public Color getBackground() {
        return this.background;
    }

    /**
     * @return friendForeground
     */
    public Color getForeground() {
        return this.foreground;
    }
}
