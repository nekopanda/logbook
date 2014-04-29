/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.dto;

import java.util.Calendar;
import java.util.Date;

import javax.json.JsonObject;

/**
 * 海戦とドロップした艦娘を表します
 */
public final class BattleResultDto extends AbstractDto {

    /** 日付 */
    private final Date battleDate;

    /** 海域名 */
    private final String questName;

    /** ランク */
    private final String rank;

    /** 敵艦隊名 */
    private final String enemyName;

    /** ドロップフラグ */
    private final boolean dropFlag;

    /** 艦種 */
    private final String dropType;

    /** 艦名 */
    private final String dropName;

    /** 戦闘詳細 */
    private final BattleDto battle;

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     * @param battle 戦闘詳細
     */
    public BattleResultDto(JsonObject object, BattleDto battle, int[] mapInfo) {

        this.battleDate = Calendar.getInstance().getTime();
        this.questName = "(" + mapInfo[0] + "-" + mapInfo[1] + "-" + mapInfo[2] + ") "
                + object.getString("api_quest_name");
        this.rank = object.getString("api_win_rank");
        this.enemyName = object.getJsonObject("api_enemy_info").getString("api_deck_name") + " (e_id: " + mapInfo[3]
                + ")";
        this.dropFlag = object.containsKey("api_get_ship");
        if (this.dropFlag) {
            this.dropType = object.getJsonObject("api_get_ship").getString("api_ship_type");
            this.dropName = object.getJsonObject("api_get_ship").getString("api_ship_name");
        } else {
            this.dropType = "";
            this.dropName = "";
        }

        this.battle = battle;
    }

    /**
     * @return 日付
     */
    public Date getBattleDate() {
        return this.battleDate;
    }

    /**
     * @return 海域名
     */
    public String getQuestName() {
        return this.questName;
    }

    /**
     * @return ランク
     */
    public String getRank() {
        return this.rank;
    }

    /**
     * @return 敵艦隊名
     */
    public String getEnemyName() {
        return this.enemyName;
    }

    /**
     * @return ドロップフラグ
     */
    public boolean isDropFlag() {
        return this.dropFlag;
    }

    /**
     * @return 艦種
     */
    public String getDropType() {
        return this.dropType;
    }

    /**
     * @return 艦名
     */
    public String getDropName() {
        return this.dropName;
    }

    /**
     * @return 戦闘詳細
     */
    public BattleDto getBattleDto() {
        return this.battle;
    }
}
