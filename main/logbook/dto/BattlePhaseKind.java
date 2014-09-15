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

    public static BattlePhaseKind fromProto(BattlePhaseKindPb pb) {
        switch (pb.getNumber()) {
        case 0:
            return BATTLE;
        case 1:
            return MIDNIGHT;
        case 2:
            return SP_MIDNIGHT;
        case 3:
            return NIGHT_TO_DAY;
        case 4:
            return COMBINED_BATTLE;
        case 5:
            return COMBINED_AIR;
        case 6:
            return COMBINED_MIDNIGHT;
        case 7:
            return COMBINED_SP_MIDNIGHT;
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
