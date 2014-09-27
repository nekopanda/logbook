package logbook.dto;

import java.util.Calendar;
import java.util.Date;

import javax.json.JsonObject;

import logbook.constants.AppConstants;

/**
 * 海戦とドロップした艦娘を表します
 */
public class BattleResultDto extends AbstractDto {

    /** 日付 */
    private final Date battleDate;

    /** 海域名 */
    private final String questName;

    /** ランク */
    private final ResultRank rank;

    /** マス */
    private final MapCellDto mapCell;

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

    /** この戦闘で大破艦が出たか　*/
    private final boolean hasTaiha;

    private final boolean isCombined;

    /** メモリ節約のためShipDtoは持たない */
    private final String mvp;

    private final String mvpCombined;

    private final String flagShip;

    private final String flagShipCombined;

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     * @param cell マップ上のマス
     * @param battle 戦闘詳細
     */
    public BattleResultDto(JsonObject object, MapCellDto mapCell, BattleDto battle) {

        this.battleDate = Calendar.getInstance().getTime();
        this.questName = object.getString("api_quest_name");
        this.rank = ResultRank.fromRank(object.getString("api_win_rank"));
        this.mapCell = mapCell;
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
        this.hasTaiha = false;
        this.isCombined = false;
        this.mvp = null;
        this.mvpCombined = null;
        this.flagShip = null;
        this.flagShipCombined = null;
    }

    public BattleResultDto(BattleExDto dto) {
        this.battleDate = dto.getBattleDate();
        this.questName = dto.getQuestName();
        this.rank = dto.getRank();
        this.mapCell = dto.getMapCellDto();
        this.enemyName = dto.getEnemyName();
        this.dropFlag = dto.getDropName().length() > 0;
        this.dropType = dto.getDropType();
        this.dropName = dto.getDropName();
        this.battle = null;

        // 大破艦があるか
        BattleExDto.Phase lastPhase = dto.getLastPhase();
        this.hasTaiha = (this.hasTaihaInFleet(lastPhase.getNowFriendHp(), dto.getMaxFriendHp()) ||
                this.hasTaihaInFleet(lastPhase.getNowFriendHpCombined(), dto.getMaxFriendHpCombined()));

        this.isCombined = dto.isCombined();

        // MVP
        if (dto.getMvp() == -1) { // 敗北Eの時はMVPなし
            this.mvp = null;
        }
        else {
            this.mvp = dto.getDock().getShips().get(dto.getMvp() - 1).getFriendlyName();
        }
        if (dto.isCombined()) {
            if (dto.getMvpCombined() == -1) { // 敗北Eの時はMVPなし
                this.mvpCombined = null;
            }
            else {
                this.mvpCombined = dto.getDockCombined().getShips()
                        .get(dto.getMvpCombined() - 1).getFriendlyName();
            }
        }
        else {
            this.mvpCombined = null;
        }

        // 旗艦
        this.flagShip = dto.getDock().getShips().get(0).getFriendlyName();
        ;
        if (dto.isCombined()) {
            this.flagShipCombined = dto.getDockCombined()
                    .getShips().get(0).getFriendlyName();
        }
        else {
            this.flagShipCombined = null;
        }
    }

    private boolean hasTaihaInFleet(int[] nowhp, int[] maxhp) {
        if ((nowhp == null) || (maxhp == null)) {
            return false;
        }
        for (int i = 0; i < nowhp.length; ++i) {
            double rate = (double) nowhp[i] / (double) maxhp[i];
            if (rate <= AppConstants.BADLY_DAMAGE) {
                return true;
            }
        }
        return false;
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

    public boolean isPractice() {
        return (this.questName == null);
    }

    /**
     * ランクを取得します。
     * @return ランク
     */
    public ResultRank getRank() {
        return this.rank;
    }

    /**
     * マスを取得します。
     * @return マス
     */
    public MapCellDto getMapCell() {
        return this.mapCell;
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

    /**
     * @return hasTaiha
     */
    public boolean isHasTaiha() {
        return this.hasTaiha;
    }

    /**
     * @return mvp
     */
    public String getMvp() {
        return this.mvp;
    }

    /**
     * @return mvpCombined
     */
    public String getMvpCombined() {
        return this.mvpCombined;
    }

    /**
     * @return flagShip
     */
    public String getFlagShip() {
        return this.flagShip;
    }

    /**
     * @return flagShipCombined
     */
    public String getFlagShipCombined() {
        return this.flagShipCombined;
    }

    /**
     * @return isCombined
     */
    public boolean isCombined() {
        return this.isCombined;
    }
}
