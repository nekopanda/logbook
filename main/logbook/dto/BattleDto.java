package logbook.dto;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.data.context.GlobalContext;
import logbook.internal.Ship;

/**
 * 会敵を表します
 *
 */
public final class BattleDto extends AbstractDto {

    /** 味方艦隊 */
    private final DockDto dock;

    private final DockDto dockCombined;

    /** 味方艦 */
    private final List<ShipDto> fships = new ArrayList<ShipDto>();

    private final List<ShipDto> fshipsCombined = new ArrayList<ShipDto>();

    /** 敵艦隊 */
    private final List<ShipInfoDto> enemy = new ArrayList<ShipInfoDto>();

    /** 味方HP */
    private final int[] nowFriendHp;

    private final int[] nowFriendHpCombined;

    /** 敵HP */
    private final int[] nowEnemyHp;

    /** 味方MaxHP */
    private final int[] maxFriendHp;

    private final int[] maxFriendHpCombined;

    /** 敵MaxHP */
    private final int[] maxEnemyHp;

    /** 味方戦闘開始時HP */
    public int[] startFriendHp;

    public int[] startFriendHpCombined;

    /** 敵戦闘開始時HP */
    public int[] startEnemyHp;

    /** 戦闘前の味方総HP */
    private int friendGaugeMax = 0;

    /** 戦闘前の敵総HP） */
    private int enemyGaugeMax = 0;

    /** ランク */
    private final ResultRank rank;

    private final boolean isYasen;

    /**
     * コンストラクター
     */
    public BattleDto(JsonObject object, BattleDto firstBattle, boolean isYasen) {

        String dockId;

        if (object.containsKey("api_dock_id")) {
            dockId = object.get("api_dock_id").toString();
        } else {
            dockId = object.get("api_deck_id").toString();
        }

        this.isYasen = isYasen;

        JsonArray nowhps = object.getJsonArray("api_nowhps");
        JsonArray maxhps = object.getJsonArray("api_maxhps");
        JsonArray nowhpsCombined = object.getJsonArray("api_nowhps_combined");
        JsonArray maxhpsCombined = object.getJsonArray("api_maxhps_combined");
        boolean isCombined = (nowhpsCombined != null);

        this.dock = GlobalContext.getDock(dockId);
        this.dockCombined = isCombined ? GlobalContext.getDock(Integer.toString(2)) : null;

        if (this.dock != null) {
            dockToList(this.dock, this.fships);
            if (this.dockCombined != null) {
                dockToList(this.dockCombined, this.fshipsCombined);
            }
        }
        else {
            // ドッグ情報がない時（主にデバッグ用）
            for (int i = 1; i <= 6; i++) {
                int hp = nowhps.getJsonNumber(i).intValue();
                if (hp != -1) {
                    this.fships.add(null);
                }
            }
        }

        JsonArray shipKe = object.getJsonArray("api_ship_ke");
        for (int i = 1; i < shipKe.size(); i++) {
            long id = shipKe.getJsonNumber(i).longValue();
            ShipInfoDto dto = Ship.get(Long.toString(id));
            if (dto != null) {
                this.enemy.add(dto);
            }
        }

        // このマスでの最初の戦闘がこれでない場合は、その時の値を取得
        if (firstBattle != null) {
            this.startFriendHp = firstBattle.startFriendHp;
            this.startEnemyHp = firstBattle.startEnemyHp;
            this.startFriendHpCombined = firstBattle.startFriendHpCombined;
            this.friendGaugeMax = firstBattle.friendGaugeMax;
            this.enemyGaugeMax = firstBattle.enemyGaugeMax;
        }
        else {
            this.startFriendHp = new int[this.fships.size()];
            this.startEnemyHp = new int[this.enemy.size()];
            if (isCombined) {
                this.startFriendHpCombined = new int[this.fshipsCombined.size()];
            }
        }
        this.nowFriendHp = new int[this.fships.size()];
        this.nowEnemyHp = new int[this.enemy.size()];
        this.maxFriendHp = new int[this.fships.size()];
        this.maxEnemyHp = new int[this.enemy.size()];
        if (isCombined) {
            this.nowFriendHpCombined = new int[this.fshipsCombined.size()];
            this.maxFriendHpCombined = new int[this.fshipsCombined.size()];
        }
        else {
            this.nowFriendHpCombined = null;
            this.maxFriendHpCombined = null;
        }

        // この戦闘の開始前HPを取得
        for (int i = 1; i < nowhps.size(); i++) {
            int hp = nowhps.getInt(i);
            int maxHp = maxhps.getInt(i);
            if (i <= 6) {
                if (i <= this.fships.size()) {
                    this.nowFriendHp[i - 1] = hp;
                    this.maxFriendHp[i - 1] = maxHp;
                    if (firstBattle == null)
                        this.friendGaugeMax += this.startFriendHp[i - 1] = hp;
                }
            } else {
                if ((i - 6) <= this.enemy.size()) {
                    this.nowEnemyHp[i - 1 - 6] = hp;
                    this.maxEnemyHp[i - 1 - 6] = maxHp;
                    if (firstBattle == null)
                        this.enemyGaugeMax += this.startEnemyHp[i - 1 - 6] = hp;
                }
            }
        }
        if (isCombined) {
            for (int i = 1; i < nowhpsCombined.size(); i++) {
                int hp = nowhpsCombined.getInt(i);
                int maxHp = maxhpsCombined.getInt(i);
                this.nowFriendHpCombined[i - 1] = hp;
                this.maxFriendHpCombined[i - 1] = maxHp;
            }
        }

        // ダメージ計算 //

        // 航空戦（通常）
        JsonObject kouku = object.getJsonObject("api_kouku");
        if (kouku != null)
            this.doKouku(kouku.get("api_stage3"), isCombined ? kouku.get("api_stage3_combined") : null);

        // 航空戦（連合艦隊のみ？）
        JsonObject kouku2 = object.getJsonObject("api_kouku2");
        if (kouku2 != null)
            this.doKouku(kouku2.get("api_stage3"), isCombined ? kouku2.get("api_stage3_combined") : null);

        // 支援艦隊
        JsonNumber support_flag = object.getJsonNumber("api_support_flag");
        if ((support_flag != null) && (support_flag.intValue() != 0)) {
            JsonObject support = object.getJsonObject("api_support_info");
            if (support != null) {
                JsonObject support_hourai = support.getJsonObject("api_support_hourai");
                if (support_hourai != null) {
                    JsonArray edam = support_hourai.getJsonArray("api_damage");
                    if (edam != null) {
                        for (int i = 1; i <= this.enemy.size(); i++) {
                            this.nowEnemyHp[i - 1] -= edam.getJsonNumber(i).intValue();
                        }
                    }
                }
            }
        }

        // 開幕
        this.doRaigeki(object.get("api_opening_atack"), this.isCombined());

        // 砲撃
        this.doHougeki(object.get("api_hougeki"), (this.isCombined() && isYasen)); // 夜戦
        this.doHougeki(object.get("api_hougeki1"), this.isCombined());
        this.doHougeki(object.get("api_hougeki2"), false);
        this.doHougeki(object.get("api_hougeki3"), false);

        // 雷撃
        this.doRaigeki(object.get("api_raigeki"), this.isCombined());

        // HP0以下を0にする
        for (int i = 0; i < this.fships.size(); i++) {
            if (this.nowFriendHp[i] <= 0)
                this.nowFriendHp[i] = 0;
        }
        for (int i = 0; i < this.enemy.size(); i++) {
            if (this.nowEnemyHp[i] <= 0)
                this.nowEnemyHp[i] = 0;
        }
        if (isCombined) {
            for (int i = 0; i < this.fshipsCombined.size(); i++) {
                if (this.nowFriendHpCombined[i] <= 0)
                    this.nowFriendHpCombined[i] = 0;
            }
        }

        // 判定を計算
        this.rank = this.calcResultRank();
    }

    private static void dockToList(DockDto dock, List<ShipDto> array) {
        List<ShipDto> dock_ships = dock.getShips();
        for (int i = 0; i < dock_ships.size(); i++) {
            ShipDto ship = dock_ships.get(i);
            array.add(ship);
        }
    }

    // 勝利判定 //
    private ResultRank calcResultRank() {
        boolean isCombined = (this.nowFriendHpCombined != null);
        // 戦闘後に残っている艦数
        int friendNowShips = 0;
        int enemyNowShips = 0;
        // 総ダメージ
        int friendGauge = 0;
        int enemyGauge = 0;

        for (int i = 0; i < this.fships.size(); i++) {
            if (this.nowFriendHp[i] > 0) {
                ++friendNowShips;
            }
            friendGauge += this.startFriendHp[i] - this.nowFriendHp[i];

            if (isCombined) {
                if (this.nowFriendHpCombined[i] > 0) {
                    ++friendNowShips;
                }
                friendGauge += this.startFriendHpCombined[i] - this.nowFriendHpCombined[i];
            }
        }
        for (int i = 0; i < this.enemy.size(); i++) {
            if (this.nowEnemyHp[i] > 0)
                ++enemyNowShips;

            enemyGauge += this.startEnemyHp[i] - this.nowEnemyHp[i];
        }

        // 轟沈・撃沈数
        int friendSunk = (this.fships.size() + this.fshipsCombined.size()) - friendNowShips;
        int enemySunk = this.enemy.size() - enemyNowShips;

        double enemyGaugeRate = (double) enemyGauge / this.enemyGaugeMax;
        double friendGaugeRate = (double) friendGauge / this.friendGaugeMax;
        // 0.91倍以上（0.902~0.92のどれか、逆に敵の1.1倍かも）
        boolean equalOrMore = (1.1 * enemyGaugeRate) >= friendGaugeRate;
        // 2.5倍以上
        boolean superior1 = (enemyGaugeRate != 0) && (enemyGaugeRate >= (2.5 * friendGaugeRate));
        boolean superior2 = (enemyGaugeRate != 0) && (enemyGaugeRate >= (2.3 * friendGaugeRate));
        boolean superior = superior1;

        if (friendSunk == 0) { // 味方轟沈数ゼロ
            if (enemyNowShips == 0) { // 敵を殲滅した
                if (friendGauge == 0) { // 味方ダメージゼロ
                    return ResultRank.PERFECT;
                }
                return ResultRank.S;
            }
            else {
                // 6隻の場合のみ4隻以上撃沈？
                if (this.enemy.size() == 6) {
                    if (enemySunk >= 4) {
                        return ResultRank.A;
                    }
                }
                // 半数以上撃沈？
                else if ((enemySunk * 2) >= this.enemy.size()) {
                    return ResultRank.A;
                }
                // 敵旗艦を撃沈
                if (this.nowEnemyHp[0] == 0) {
                    return ResultRank.B;
                }
                // 戦果ゲージが2.5倍以上
                if (superior) {
                    return ResultRank.B;
                }
                else if (superior2) {
                    return ResultRank.B_OR_C;
                }
            }
        }
        else {
            // 敵を殲滅した
            if (enemyNowShips == 0) {
                return ResultRank.B;
            }
            // 敵旗艦を撃沈 and 味方轟沈数 < 敵撃沈数
            if ((this.nowEnemyHp[0] == 0) && (friendSunk < enemySunk)) {
                return ResultRank.B;
            }
            // 戦果ゲージが2.5倍以上
            if (superior) {
                return ResultRank.B;
            }
            else if (superior2) {
                return ResultRank.B_OR_C;
            }
            // 敵旗艦を撃沈
            if (this.nowEnemyHp[0] == 0) {
                return ResultRank.C;
            }
        }
        // 敵に与えたダメージが一定以上 and 戦果ゲージが1.0倍以上
        if ((enemyGauge > 0) && equalOrMore) {
            return ResultRank.C;
        }
        // 味方轟沈艦ゼロで敵に１以上のダメージ
        // 轟沈艦なしで味方が最大ダメージを受けて（残りHP1）、かつ敵へのダメージがゼロなのにDなったという報告があったので「敵に１以上のダメージ」という条件は怪しい
        if (friendSunk == 0) {
            return ResultRank.D;
        }
        // 敵へのダメージゼロで、味方ダメージが一定以下（データが少なすぎてここのボーダーは不明。0.2~0.7のどれかだと思う。
        if ((friendGaugeRate < 0.3) && (enemyGauge == 0)) {
            return ResultRank.D;
        }
        // DとEは情報が不足してて区別できない
        return ResultRank.D_OR_E;
    }

    private void doKouku(JsonValue raigeki, JsonValue combined) {
        if ((raigeki == null) || (raigeki == JsonValue.NULL))
            return;

        JsonObject raigeki_obj = (JsonObject) raigeki;
        JsonArray fdam = raigeki_obj.getJsonArray("api_fdam");
        JsonArray edam = raigeki_obj.getJsonArray("api_edam");
        JsonArray fdamCombined = null;
        if ((combined != null) && (combined != JsonValue.NULL)) {
            fdamCombined = ((JsonObject) combined).getJsonArray("api_fdam");
        }
        for (int i = 1; i <= 6; i++) {
            if (i <= this.fships.size())
                this.nowFriendHp[i - 1] -= fdam.getInt(i);
            if (i <= this.enemy.size())
                this.nowEnemyHp[i - 1] -= edam.getInt(i);
            if ((fdamCombined != null) && (i <= this.fshipsCombined.size()))
                this.nowFriendHpCombined[i - 1] -= fdamCombined.getInt(i);
        }
    }

    private void doRaigeki(JsonValue raigeki, boolean second) {
        if ((raigeki == null) || (raigeki == JsonValue.NULL))
            return;

        JsonObject raigeki_obj = (JsonObject) raigeki;
        JsonArray fdam = raigeki_obj.getJsonArray("api_fdam");
        JsonArray edam = raigeki_obj.getJsonArray("api_edam");
        int[] targetFriendHp = second ? this.nowFriendHpCombined : this.nowFriendHp;
        for (int i = 1; i <= 6; i++) {
            if (i <= this.fships.size())
                targetFriendHp[i - 1] -= fdam.getInt(i);
            if (i <= this.enemy.size())
                this.nowEnemyHp[i - 1] -= edam.getInt(i);
        }
    }

    private ArrayList<Integer> listupDamage(JsonArray damage_list) {
        ArrayList<Integer> list = new ArrayList<Integer>();

        for (JsonValue atack : damage_list) {
            switch (atack.getValueType()) {
            case NUMBER:
                list.add(((JsonNumber) atack).intValue());
                break;
            case ARRAY:
                for (JsonValue ship : (JsonArray) atack) {
                    list.add(((JsonNumber) ship).intValue());
                }
                break;
            default: // あり得ない
                break;
            }
        }

        return list;
    }

    /**
     * api_hougeki* を処理する
     * @param hougeki
     */
    private void doHougeki(JsonValue hougeki, boolean second) {
        if ((hougeki == null) || (hougeki == JsonValue.NULL))
            return;

        JsonObject hougeki_obj = (JsonObject) hougeki;
        ArrayList<Integer> df_list = this.listupDamage(hougeki_obj.getJsonArray("api_df_list"));
        ArrayList<Integer> damage = this.listupDamage(hougeki_obj.getJsonArray("api_damage"));

        if (df_list.size() != damage.size()) {
            throw new IndexOutOfBoundsException("df_list と damage の長さが合いません");
        }

        int[] targetFriendHp = second ? this.nowFriendHpCombined : this.nowFriendHp;
        for (int i = 0; i < df_list.size(); ++i) {
            int shipIdx = df_list.get(i);
            if (shipIdx == -1)
                continue;
            if (shipIdx <= 6) {
                targetFriendHp[shipIdx - 1] -= damage.get(i);
            }
            else {
                this.nowEnemyHp[shipIdx - 1 - 6] -= damage.get(i);
            }
        }
    }

    /**
     * @return dock
     */
    public DockDto getDock() {
        return this.dock;
    }

    public DockDto getDockCombined() {
        return this.dockCombined;
    }

    public List<ShipDto> getFriendShips() {
        return this.fships;
    }

    public List<ShipDto> getFriendShipsCombined() {
        return this.fshipsCombined;
    }

    /**
     * @return enemy
     */
    public List<ShipInfoDto> getEnemy() {
        return this.enemy;
    }

    /**
     * @return nowFriendHp
     */
    public int[] getNowFriendHp() {
        return this.nowFriendHp;
    }

    public int[] getNowFriendHpCombined() {
        return this.nowFriendHpCombined;
    }

    /**
     * @return nowEnemyHp
     */
    public int[] getNowEnemyHp() {
        return this.nowEnemyHp;
    }

    /**
     * @return maxFriendHp
     */
    public int[] getMaxFriendHp() {
        return this.maxFriendHp;
    }

    public int[] getMaxFriendHpCombined() {
        return this.maxFriendHpCombined;
    }

    /**
     * @return maxEnemyHp
     */
    public int[] getMaxEnemyHp() {
        return this.maxEnemyHp;
    }

    /**
     * @return rank
     */
    public ResultRank getRank() {
        return this.rank;
    }

    public boolean isCombined() {
        return (this.nowFriendHpCombined != null);
    }

    public String getRankCalcInfo() {
        boolean isCombined = (this.nowFriendHpCombined != null);
        // 戦闘後に残っている艦数
        int friendNowShips = 0;
        int enemyNowShips = 0;
        // 総ダメージ
        int friendGauge = 0;
        int enemyGauge = 0;

        for (int i = 0; i < this.fships.size(); i++) {
            if (this.nowFriendHp[i] > 0) {
                ++friendNowShips;
            }
            friendGauge += this.startFriendHp[i] - this.nowFriendHp[i];

            if (isCombined) {
                if (this.nowFriendHpCombined[i] > 0) {
                    ++friendNowShips;
                }
                friendGauge += this.startFriendHpCombined[i] - this.nowFriendHpCombined[i];
            }
        }
        for (int i = 0; i < this.enemy.size(); i++) {
            if (this.nowEnemyHp[i] > 0)
                ++enemyNowShips;

            enemyGauge += this.startEnemyHp[i] - this.nowEnemyHp[i];
        }

        return "味方[艦:" + (this.fships.size() + this.fshipsCombined.size()) + "→" + friendNowShips + " ゲージ:"
                + friendGauge + "/" + this.friendGaugeMax +
                "] 敵[艦:" + this.enemy.size() + "→" + enemyNowShips + " ゲージ:" + enemyGauge + "/" + this.enemyGaugeMax
                + "] 判定:" + this.rank.rank();
    }
}
