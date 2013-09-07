/**
 * 
 */
package logbook.dto;

import java.util.Calendar;
import java.util.Date;

import javax.json.JsonObject;

/**
 * 海戦とドロップした艦娘を表します
 */
public final class BattleResultDto extends AbstractDto {

    private final Date battleDate;
    private final String questName;
    private final String rank;
    private final String enemyName;
    private final boolean dropFlag;
    private final String dropType;
    private final String dropName;

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     */
    public BattleResultDto(JsonObject object) {

        this.battleDate = Calendar.getInstance().getTime();
        this.questName = object.getString("api_quest_name");
        this.rank = object.getString("api_win_rank");
        this.enemyName = object.getJsonObject("api_enemy_info").getString("api_deck_name");
        this.dropFlag = object.containsKey("api_get_ship");
        if (this.dropFlag) {
            this.dropType = object.getJsonObject("api_get_ship").getString("api_ship_type");
            this.dropName = object.getJsonObject("api_get_ship").getString("api_ship_name");
        } else {
            this.dropType = "";
            this.dropName = "";
        }
    }

    /**
     * @return battleDate
     */
    public Date getBattleDate() {
        return this.battleDate;
    }

    /**
     * @return questName
     */
    public String getQuestName() {
        return this.questName;
    }

    /**
     * @return rank
     */
    public String getRank() {
        return this.rank;
    }

    /**
     * @return enemyName
     */
    public String getEnemyName() {
        return this.enemyName;
    }

    /**
     * @return dropFlag
     */
    public boolean isDropFlag() {
        return this.dropFlag;
    }

    /**
     * @return dropType
     */
    public String getDropType() {
        return this.dropType;
    }

    /**
     * @return dropName
     */
    public String getDropName() {
        return this.dropName;
    }
}
