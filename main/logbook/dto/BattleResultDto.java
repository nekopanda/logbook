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

    /** マス */
    private final int mapCellNo;

    /** ボスマス */
    private final boolean boss;

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
     * @param cell マップ上のマス
     * @param battle 戦闘詳細
     */
    public BattleResultDto(JsonObject object, int mapCellNo, int mapBossCellNo, int eventId, BattleDto battle) {

        this.battleDate = Calendar.getInstance().getTime();
        this.questName = object.getString("api_quest_name");
        this.rank = object.getString("api_win_rank");
        this.mapCellNo = mapCellNo;
        this.boss = (mapCellNo == mapBossCellNo) || (eventId == 5);
        this.enemyName = object.getJsonObject("api_enemy_info").getString("api_deck_name");
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
     * 日付を取得します。
     * @return 日付
     */
    public Date getBattleDate() {
        return this.battleDate;
    }

    /**
     * 海域名を取得します。
     * @return 海域名
     */
    public String getQuestName() {
        return this.questName;
    }

    /**
     * ランクを取得します。
     * @return ランク
     */
    public String getRank() {
        return this.rank;
    }

    /**
     * マスを取得します。
     * @return マス
     */
    public int getMapCellNo() {
        return this.mapCellNo;
    }

    /**
     * ボスマスを取得します。
     * @return ボスマス
     */
    public boolean isBoss() {
        return this.boss;
    }

    /**
     * 敵艦隊名を取得します。
     * @return 敵艦隊名
     */
    public String getEnemyName() {
        return this.enemyName;
    }

    /**
     * ドロップフラグを取得します。
     * @return ドロップフラグ
     */
    public boolean isDropFlag() {
        return this.dropFlag;
    }

    /**
     * 艦種を取得します。
     * @return 艦種
     */
    public String getDropType() {
        return this.dropType;
    }

    /**
     * @return 戦闘詳細
     */
    public BattleDto getBattleDto() {
        return this.battle;
    }

    /**
     * 艦名を取得します。
     * @return 艦名
     */
    public String getDropName() {
        return this.dropName;
    }

    /**
     * 戦闘詳細を取得します。
     * @return 戦闘詳細
     */
    public BattleDto getBattle() {
        return this.battle;
    }
}
