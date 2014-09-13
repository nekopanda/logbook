/**
 * 
 */
package logbook.dto;

import logbook.proto.LogbookEx.BattlePhaseKindPb;

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

    public BattlePhaseKindPb toProto() {
        switch (this) {
        case BATTLE:
            return BattlePhaseKindPb.BATTLE;
        case MIDNIGHT:
            return BattlePhaseKindPb.MIDNIGHT;
        case SP_MIDNIGHT:
            return BattlePhaseKindPb.SP_MIDNIGHT;
        case NIGHT_TO_DAY:
            return BattlePhaseKindPb.NIGHT_TO_DAY;
        case COMBINED_BATTLE:
            return BattlePhaseKindPb.COMBINED_BATTLE;
        case COMBINED_AIR:
            return BattlePhaseKindPb.COMBINED_AIR;
        case COMBINED_MIDNIGHT:
            return BattlePhaseKindPb.COMBINED_MIDNIGHT;
        case COMBINED_SP_MIDNIGHT:
            return BattlePhaseKindPb.COMBINED_SP_MIDNIGHT;
        }
        return null;
    }

    /**
     * @return night
     */
    public boolean isNight() {
        return this.night;
    }
}
