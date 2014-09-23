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

    MUKIZU("無傷", 0,
            null, null,
            SWTResourceManager.getColor(AppConstants.COND_GREEN_COLOR),
            SWTResourceManager.getColor(SWT.COLOR_WHITE)),
    KENZAI("健在", 1,
            null, null,
            SWTResourceManager.getColor(AppConstants.COND_GREEN_COLOR),
            SWTResourceManager.getColor(SWT.COLOR_WHITE)),
    SYOHA("小破", 2,
            null, null,
            SWTResourceManager.getColor(AppConstants.COND_GREEN_COLOR),
            SWTResourceManager.getColor(SWT.COLOR_WHITE)),
    TYUHA("中破", 3,
            SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR),
            SWTResourceManager.getColor(SWT.COLOR_WHITE),
            SWTResourceManager.getColor(AppConstants.COND_GREEN_COLOR),
            SWTResourceManager.getColor(SWT.COLOR_WHITE)),
    TAIHA("大破", 4,
            SWTResourceManager.getColor(AppConstants.COND_RED_COLOR),
            SWTResourceManager.getColor(SWT.COLOR_WHITE),
            SWTResourceManager.getColor(AppConstants.COND_GREEN_COLOR),
            SWTResourceManager.getColor(SWT.COLOR_WHITE)),
    GOTIN("轟沈", 5,
            SWTResourceManager.getColor(AppConstants.COND_RED_COLOR),
            SWTResourceManager.getColor(SWT.COLOR_WHITE),
            null, null);

    private final String str;
    private final int level;
    private final Color friendBackground;
    private final Color friendForeground;
    private final Color enemyBackground;
    private final Color enemyForeground;

    private DamageRate(String str, int level, Color fb, Color ff, Color eb, Color ef) {
        this.str = str;
        this.level = level;
        this.friendBackground = fb;
        this.friendForeground = ff;
        this.enemyBackground = eb;
        this.enemyForeground = ef;
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

    public int getLevel() {
        return this.level;
    }

    /**
     * @return friendBackground
     */
    public Color getFriendBackground() {
        return this.friendBackground;
    }

    /**
     * @return friendForeground
     */
    public Color getFriendForeground() {
        return this.friendForeground;
    }

    /**
     * @return enemyBackground
     */
    public Color getEnemyBackground() {
        return this.enemyBackground;
    }

    /**
     * @return enemyForeground
     */
    public Color getEnemyForeground() {
        return this.enemyForeground;
    }
}
