/**
 * 
 */
package logbook.dto;

import logbook.data.DataType;

/**
 * @author Nekopanda
 *
 */
public enum BattlePhaseKind {

    BATTLE(false, BattlePatternConstants.NON_COMBINED_PTTERN, DataType.BATTLE),
    MIDNIGHT(true, BattlePatternConstants.NON_COMBINED_PTTERN, DataType.BATTLE_MIDNIGHT),
    PRACTICE_BATTLE(false, BattlePatternConstants.NON_COMBINED_PTTERN, DataType.PRACTICE_BATTLE),
    PRACTICE_MIDNIGHT(true, BattlePatternConstants.NON_COMBINED_PTTERN, DataType.PRACTICE_BATTLE_MIDNIGHT),
    SP_MIDNIGHT(true, BattlePatternConstants.NON_COMBINED_PTTERN, DataType.BATTLE_SP_MIDNIGHT),
    NIGHT_TO_DAY(false, BattlePatternConstants.NON_COMBINED_PTTERN, DataType.BATTLE_NIGHT_TO_DAY),
    COMBINED_BATTLE(false, BattlePatternConstants.BATTLE_PATTERN, DataType.COMBINED_BATTLE),
    COMBINED_AIR(false, BattlePatternConstants.BATTLE_PATTERN, DataType.COMBINED_AIR_BATTLE),
    COMBINED_MIDNIGHT(true, BattlePatternConstants.BATTLE_PATTERN, DataType.COMBINED_BATTLE_MIDNIGHT),
    COMBINED_SP_MIDNIGHT(true, BattlePatternConstants.BATTLE_PATTERN, DataType.COMBINED_BATTLE_SP_MIDNIGHT),
    COMBINED_BATTLE_WATER(false, BattlePatternConstants.WATER_PATTERN, DataType.COMBINED_BATTLE_WATER);

    private final boolean night;
    private final boolean[] pattern;
    private final DataType api;

    private BattlePhaseKind(boolean night, boolean[] pattern, DataType api) {
        this.night = night;
        this.pattern = pattern;
        this.api = api;
    }

    /**
     * @return night
     */
    public boolean isNight() {
        return this.night;
    }

    public boolean isOpeningSecond() {
        return this.pattern[0];
    }

    public boolean isHougekiSecond() {
        return this.pattern[1];
    }

    public boolean isHougeki1Second() {
        return this.pattern[2];
    }

    public boolean isHougeki2Second() {
        return this.pattern[3];
    }

    public boolean isHougeki3Second() {
        return this.pattern[4];
    }

    public boolean isRaigekiSecond() {
        return this.pattern[5];
    }

    /**
     * @return api
     */
    public DataType getApi() {
        return api;
    }
}

class BattlePatternConstants {
    // 第一->false, 第二->true
    // opening, hougeki, hougeki1, hougeki2, hougeki3, raigeki

    // 通常戦闘
    public static boolean[] NON_COMBINED_PTTERN = new boolean[] {
            false, false, false, false, false, false
    };

    // 連合艦隊の通常
    public static boolean[] BATTLE_PATTERN = new boolean[] {
            true, true, true, false, false, true
    };

    // 連合艦隊の水雷戦
    public static boolean[] WATER_PATTERN = new boolean[] {
            true, true, false, false, true, true
    };
}
