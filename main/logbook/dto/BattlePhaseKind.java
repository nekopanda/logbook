/**
 * 
 */
package logbook.dto;

/**
 * @author Nekopanda
 *
 */
public enum BattlePhaseKind {

    BATTLE(false),
    MIDNIGHT(true),
    SP_MIDNIGHT(true),
    NIGHT_TO_DAY(false),
    COMBINED_BATTLE(false),
    COMBINED_AIR(false),
    COMBINED_MIDNIGHT(true),
    COMBINED_SP_MIDNIGHT(true);

    private final boolean night;

    private BattlePhaseKind(boolean night) {
        this.night = night;
    }

    /**
     * @return night
     */
    public boolean isNight() {
        return this.night;
    }
}
