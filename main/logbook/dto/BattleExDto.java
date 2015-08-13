/**
 * 
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.data.context.GlobalContext;
import logbook.internal.EnemyData;
import logbook.internal.UseItem;
import logbook.util.JsonUtils;

import org.apache.commons.lang3.StringUtils;

import com.dyuproject.protostuff.Tag;

/**
 * １回の会敵情報
 * @author Nekopanda
 */
public class BattleExDto extends AbstractDto {

    /** 日付 */
    @Tag(1)
    private final Date battleDate;

    /** 味方艦隊 */
    @Tag(2)
    private final List<DockDto> friends = new ArrayList<>();

    /** 敵艦隊 */
    @Tag(3)
    private final List<EnemyShipDto> enemy = new ArrayList<>();

    /** 味方MaxHP */
    @Tag(6)
    private int[] maxFriendHp;

    @Tag(7)
    private int[] maxFriendHpCombined;

    /** 敵MaxHP */
    @Tag(8)
    private int[] maxEnemyHp;

    /** 味方戦闘開始時HP */
    @Tag(9)
    private int[] startFriendHp;

    @Tag(10)
    private int[] startFriendHpCombined;

    /** 敵戦闘開始時HP */
    @Tag(11)
    private int[] startEnemyHp;

    /** 戦闘前の味方総HP */
    @Tag(12)
    private int friendGaugeMax = 0;

    /** 戦闘前の敵総HP */
    @Tag(13)
    private int enemyGaugeMax = 0;

    /** 陣形（味方・敵） */
    @Tag(14)
    private final String[] formation = new String[] { "陣形不明", "陣形不明" };

    /** 同航戦とか　*/
    @Tag(15)
    private String formationMatch = "不明";

    /** 索敵状態（味方・敵） */
    @Tag(16)
    private String sakuteki[];

    /** 攻撃フェーズ */
    @Tag(17)
    private final List<Phase> phaseList = new ArrayList<Phase>();

    /** 海域名 */
    @Tag(18)
    private String questName;

    /** ランク */
    @Tag(19)
    private ResultRank rank;

    /** マス */
    @Tag(20)
    private MapCellDto mapCellDto;

    /** 敵艦隊名 */
    @Tag(21)
    private String enemyName;

    /** ドロップフラグ */
    @Tag(22)
    private boolean dropShip;

    /** ドロップフラグ */
    @Tag(39)
    private boolean dropItem;

    /** 艦種 */
    @Tag(23)
    private String dropType;

    /** 艦名 */
    @Tag(24)
    private String dropName;

    /** ドロップ艦ID */
    @Tag(46)
    private int dropShipId;

    /** MVP艦（ゼロ始まりのインデックス） */
    @Tag(25)
    private int mvp;

    @Tag(26)
    private int mvpCombined;

    /** 提督Lv */
    @Tag(27)
    private int hqLv;

    /** 
     * BattleExDtoのバージョン
     * exVersion == 0 : Tag 34以降がない
     * exVersion == 1 : Tag 36まである
     * exVersion == 2 : Jsonがある
     *  */
    @Tag(34)
    private int exVersion = 2;

    /** 母港空き（ドロップ分を含まない） */
    @Tag(35)
    private int shipSpace;

    /** 装備空き（ドロップ分を含まない） */
    @Tag(36)
    private int itemSpace;

    /** 連合艦隊における退避意見 [退避する艦(0-11), 護衛艦(0-11)] */
    @Tag(37)
    private int[] escapeInfo;

    @Tag(38)
    private boolean[] escaped;

    /** 轟沈フラグ */
    @Tag(44)
    private boolean[] lostflag;

    /** 次の連合艦隊ではこうなりそう(ならなかったら轟沈艦を取り除くことができないので処理を再考する必要あり) */
    //@Tag(45)
    //private boolean[] lostflagCombined;

    @Tag(51)
    private String resultJson;

    /////////////////////////////////////////////////

    /**
     * 戦闘1フェーズの情報
     * @author Nekopanda
     */
    public static class Phase {

        @Tag(1)
        private final BattlePhaseKind kind;
        /** 味方HP */
        @Tag(2)
        private final int[] nowFriendHp;

        @Tag(3)
        private final int[] nowFriendHpCombined;

        /** 敵HP */
        @Tag(4)
        private final int[] nowEnemyHp;

        /** ランク */
        @Tag(5)
        private ResultRank estimatedRank;

        /** 夜戦 */
        @Tag(6)
        private final boolean isNight;

        /** 支援攻撃のタイプ */
        @Tag(7)
        private String supportType;

        /** 触接機（味方・敵） -1の場合は「触接なし」 */
        @Tag(8)
        private int[] touchPlane;

        /** 照明弾発射艦 */
        @Tag(32)
        private int[] flarePos;

        @Tag(9)
        private String seiku;

        /** 損害率（味方・敵） */
        @Tag(10)
        private double[] damageRate;

        /** 攻撃シーケンス */
        @Tag(11)
        private AirBattleDto air = null;
        @Tag(12)
        private AirBattleDto air2 = null;
        @Tag(13)
        private List<BattleAtackDto> support = null;
        @Tag(14)
        private List<BattleAtackDto> opening = null;
        @Tag(15)
        private List<BattleAtackDto> raigeki = null;
        @Tag(16)
        private List<BattleAtackDto> hougeki = null;
        @Tag(17)
        private List<BattleAtackDto> hougeki1 = null;
        @Tag(18)
        private List<BattleAtackDto> hougeki2 = null;
        @Tag(19)
        private List<BattleAtackDto> hougeki3 = null;

        @Tag(30)
        private final String json;

        public Phase(BattleExDto battle, JsonObject object, BattlePhaseKind kind,
                int[] beforeFriendHp, int[] beforeFriendHpCombined, int[] beforeEnemyHp)
        {
            boolean isCombined = (beforeFriendHpCombined != null);

            this.kind = kind;
            this.isNight = kind.isNight();

            this.nowFriendHp = beforeFriendHp.clone();
            this.nowEnemyHp = beforeEnemyHp.clone();
            if (isCombined) {
                this.nowFriendHpCombined = beforeFriendHpCombined.clone();
            }
            else {
                this.nowFriendHpCombined = null;
            }

            // 夜間触接
            JsonValue jsonTouchPlane = object.get("api_touch_plane");
            if ((jsonTouchPlane != null) && (jsonTouchPlane != JsonValue.NULL)) {
                this.touchPlane = JsonUtils.getIntArray(object, "api_touch_plane");
            }

            // 照明弾発射艦
            JsonValue jsonFlarePos = object.get("api_flare_pos");
            if ((jsonFlarePos != null) && (jsonFlarePos != JsonValue.NULL)) {
                this.flarePos = JsonUtils.getIntArray(object, "api_flare_pos");
            }

            // 攻撃シーケンスを読み取る //

            // 航空戦（通常）
            JsonObject kouku = object.getJsonObject("api_kouku");
            if (kouku != null) {
                this.air = new AirBattleDto(kouku, isCombined);
                // 昼戦の触接はここ
                this.touchPlane = this.air.touchPlane;
                // 制空はここから取る
                this.seiku = this.air.seiku;
            }

            // 支援艦隊
            JsonNumber support_flag = object.getJsonNumber("api_support_flag");
            if ((support_flag != null) && (support_flag.intValue() != 0)) {
                JsonObject support = object.getJsonObject("api_support_info");
                JsonValue support_hourai = support.get("api_support_hourai");
                JsonValue support_air = support.get("api_support_airatack");
                if ((support_hourai != null) && (support_hourai != JsonValue.NULL)) {
                    JsonArray edam = ((JsonObject) support_hourai).getJsonArray("api_damage");
                    if (edam != null) {
                        this.support = BattleAtackDto.makeSupport(edam);
                    }
                }
                else if ((support_air != null) && (support_air != JsonValue.NULL)) {
                    JsonValue stage3 = ((JsonObject) support_air).get("api_stage3");
                    if ((stage3 != null) && (stage3 != JsonValue.NULL)) {
                        this.support = BattleAtackDto.makeSupport(((JsonObject) stage3).getJsonArray("api_edam"));
                    }
                }
                this.supportType = toSupport(support_flag.intValue());
            }
            else {
                this.supportType = "";
            }

            // 航空戦（連合艦隊のみ？）
            JsonObject kouku2 = object.getJsonObject("api_kouku2");
            if (kouku2 != null)
                this.air2 = new AirBattleDto(kouku2, isCombined);

            // 開幕
            this.opening = BattleAtackDto.makeRaigeki(object.get("api_opening_atack"), kind.isOpeningSecond());

            // 砲撃
            this.hougeki = BattleAtackDto.makeHougeki(object.get("api_hougeki"), kind.isHougekiSecond()); // 夜戦
            this.hougeki1 = BattleAtackDto.makeHougeki(object.get("api_hougeki1"), kind.isHougeki1Second());
            this.hougeki2 = BattleAtackDto.makeHougeki(object.get("api_hougeki2"), kind.isHougeki2Second());
            this.hougeki3 = BattleAtackDto.makeHougeki(object.get("api_hougeki3"), kind.isHougeki3Second());

            // 雷撃
            this.raigeki = BattleAtackDto.makeRaigeki(object.get("api_raigeki"), kind.isRaigekiSecond());

            // ダメージを反映 //

            if (this.air != null)
                this.doAtack(this.air.atacks);
            this.doAtack(this.support);
            if (this.air2 != null)
                this.doAtack(this.air2.atacks);
            this.doAtack(this.opening);
            this.doAtack(this.hougeki);
            this.doAtack(this.hougeki1);
            this.doAtack(this.raigeki);
            this.doAtack(this.hougeki2);
            this.doAtack(this.hougeki3);

            this.json = object.toString();
        }

        public void battleDamage(BattleExDto battle) {
            int numFships = this.nowFriendHp.length;
            int numEships = this.nowEnemyHp.length;
            boolean isCombined = (this.nowFriendHpCombined != null);
            int numFshipsCombined = isCombined ? this.nowFriendHpCombined.length : 0;

            // HP0以下を0にする
            for (int i = 0; i < numFships; i++) {
                this.nextHp(i, this.nowFriendHp, battle.getDock().getShips());
            }
            for (int i = 0; i < numEships; i++) {
                if (this.nowEnemyHp[i] <= 0)
                    this.nowEnemyHp[i] = 0;
            }
            if (isCombined) {
                for (int i = 0; i < numFshipsCombined; i++) {
                    this.nextHp(i, this.nowFriendHpCombined, battle.getDockCombined().getShips());
                }
            }

            // 判定を計算
            this.estimatedRank = this.calcResultRank(battle);
        }

        public void practiceDamage(BattleExDto battle) {
            int numFships = this.nowFriendHp.length;
            int numEships = this.nowEnemyHp.length;
            boolean isCombined = (this.nowFriendHpCombined != null);
            int numFshipsCombined = isCombined ? this.nowFriendHpCombined.length : 0;

            // HP0以下を0にする
            for (int i = 0; i < numFships; i++) {
                if (this.nowFriendHp[i] <= 0)
                    this.nowFriendHp[i] = 0;
            }
            for (int i = 0; i < numEships; i++) {
                if (this.nowEnemyHp[i] <= 0)
                    this.nowEnemyHp[i] = 0;
            }
            if (isCombined) {
                for (int i = 0; i < numFshipsCombined; i++) {
                    if (this.nowFriendHpCombined[i] <= 0)
                        this.nowFriendHpCombined[i] = 0;
                }
            }

            // 判定を計算
            this.estimatedRank = this.calcResultRank(battle);
        }

        private void nextHp(int index, int[] hps, List<ShipDto> ships) {
            int hp = hps[index];
            ShipDto ship = ships.get(index);
            if (hp <= 0) {
                for (ItemInfoDto item : ship.getItem()) {
                    if (item == null)
                        continue;
                    if (item.getId() == 42) { //応急修理要員
                        hps[index] = (int) (ship.getMaxhp() * 0.2);
                        return;
                    } else if (item.getId() == 43) { //応急修理女神
                        hps[index] = ship.getMaxhp();
                        return;
                    }
                }
                hps[index] = 0;
                return;
            }
            return;
        }

        // 勝利判定 //
        private ResultRank calcResultRank(BattleExDto battle) {
            boolean isCombined = (this.nowFriendHpCombined != null);
            int numFships = this.nowFriendHp.length;
            int numFshipsCombined = isCombined ? this.nowFriendHpCombined.length : 0;
            int numEships = this.nowEnemyHp.length;
            // 戦闘後に残っている艦数
            int friendEscaped = 0;
            int friendNowShips = 0;
            int enemyNowShips = 0;
            // 総ダメージ
            int friendGauge = 0;
            int enemyGauge = 0;

            for (int i = 0; i < numFships; i++) {
                if ((battle.escaped != null) && battle.escaped[i]) {
                    ++friendEscaped; // 退避
                    continue;
                }
                if (this.nowFriendHp[i] > 0) {
                    ++friendNowShips;
                }
                friendGauge += battle.getStartFriendHp()[i] - this.nowFriendHp[i];
            }
            if (isCombined) {
                for (int i = 0; i < numFshipsCombined; i++) {
                    if ((battle.escaped != null) && battle.escaped[i + 6]) {
                        ++friendEscaped; // 退避
                        continue;
                    }
                    if (this.nowFriendHpCombined[i] > 0) {
                        ++friendNowShips;
                    }
                    friendGauge += battle.getStartFriendHpCombined()[i] - this.nowFriendHpCombined[i];
                }
            }
            for (int i = 0; i < numEships; i++) {
                if (this.nowEnemyHp[i] > 0) {
                    ++enemyNowShips;
                }
                enemyGauge += battle.getStartEnemyHp()[i] - this.nowEnemyHp[i];
            }

            // 轟沈・撃沈数
            int friendSunk = (numFships + numFshipsCombined) - (friendEscaped + friendNowShips);
            int enemySunk = numEships - enemyNowShips;

            this.damageRate = new double[] {
                    (double) friendGauge / battle.getFriendGaugeMax(),
                    (double) enemyGauge / battle.getEnemyGaugeMax()
            };

            double friendGaugeRate = Math.floor(this.damageRate[0] * 100);
            double enemyGaugeRate = Math.floor(this.damageRate[1] * 100);
            boolean equalOrMore = (enemyGaugeRate > (0.9 * friendGaugeRate));
            boolean superior = (enemyGaugeRate > 0) && (enemyGaugeRate > (2.5 * friendGaugeRate));

            if (friendSunk == 0) { // 味方轟沈数ゼロ
                if (enemyNowShips == 0) { // 敵を殲滅した
                    if (friendGauge == 0) { // 味方ダメージゼロ
                        return ResultRank.PERFECT;
                    }
                    return ResultRank.S;
                }
                else {
                    // 6隻の場合のみ4隻以上撃沈？
                    if (numEships == 6) {
                        if (enemySunk >= 4) {
                            return ResultRank.A;
                        }
                    }
                    // 半数以上撃沈？
                    else if ((enemySunk * 2) >= numEships) {
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
                // 敵旗艦を撃沈
                // TODO: 味方の轟沈艦が２隻以上ある場合、敵旗艦を撃沈してもDになる場合がある
                if (this.nowEnemyHp[0] == 0) {
                    return ResultRank.C;
                }
            }
            // 敵に与えたダメージが一定以上 and 戦果ゲージが1.0倍以上
            if (enemyGauge > 0) {
                if (equalOrMore) {
                    return ResultRank.C;
                }
            }
            // 轟沈艦があり かつ 残った艦が１隻のみ
            if ((friendSunk > 0) && ((numFships - friendSunk) == 1)) {
                return ResultRank.E;
            }
            // 残りはD
            return ResultRank.D;
        }

        // ダメージを反映
        private void doAtack(List<BattleAtackDto> seq) {
            if (seq == null)
                return;

            for (BattleAtackDto dto : seq) {
                for (int i = 0; i < dto.target.length; ++i) {
                    int target = dto.target[i];
                    int damage = dto.damage[i];
                    if (dto.friendAtack) {
                        this.nowEnemyHp[target] -= damage;
                    }
                    else {
                        if (target < 6) {
                            this.nowFriendHp[target] -= damage;
                        }
                        else {
                            this.nowFriendHpCombined[target - 6] -= damage;
                        }
                    }
                }
            }
        }

        /**
         * 連合艦隊か？
         * @return
         */
        public boolean isCombined() {
            return (this.nowFriendHpCombined != null);
        }

        /**
         * 航空戦情報 [１回目, 2回目]
         * 2回目は連合艦隊航空戦マスでの戦闘のみ
         * @return
         */
        public AirBattleDto[] getAirBattleDto() {
            return new AirBattleDto[] {
                    this.air, this.air2
            };
        }

        private BattleAtackDto[] toArray(List<BattleAtackDto> list) {
            return list.toArray(new BattleAtackDto[list.size()]);
        }

        /**
         * 攻撃の全シーケンスを取得
         * [ 航空戦1, 支援艦隊の攻撃, 航空戦2, 開幕, 夜戦, 砲撃戦1, 雷撃, 砲撃戦2, 砲撃戦3 ]
         * 各戦闘がない場合はnullになる
         * @return
         */
        public BattleAtackDto[][] getAtackSequence() {
            return new BattleAtackDto[][] {
                    ((this.air == null) || (this.air.atacks == null)) ? null :
                            this.toArray(this.air.atacks),
                    this.support == null ? null : this.toArray(this.support),
                    ((this.air2 == null) || (this.air2.atacks == null)) ? null :
                            this.toArray(this.air2.atacks),
                    this.opening == null ? null : this.toArray(this.opening),
                    this.hougeki == null ? null : this.toArray(this.hougeki),
                    this.hougeki1 == null ? null : this.toArray(this.hougeki1),
                    this.raigeki == null ? null : this.toArray(this.raigeki),
                    this.hougeki2 == null ? null : this.toArray(this.hougeki2),
                    this.hougeki3 == null ? null : this.toArray(this.hougeki3),
            };
        }

        /**
         * 戦闘ランクの計算に使われた情報の概要を取得
         * @param battle
         * @return
         */
        public String getRankCalcInfo(BattleExDto battle) {
            boolean isCombined = (this.nowFriendHpCombined != null);
            int numFships = this.nowFriendHp.length;
            int numFshipsCombined = isCombined ? this.nowFriendHpCombined.length : 0;
            int numEships = this.nowEnemyHp.length;
            // 戦闘後に残っている艦数
            int friendNowShips = 0;
            int enemyNowShips = 0;
            // 総ダメージ
            int friendGauge = 0;
            int enemyGauge = 0;

            for (int i = 0; i < numFships; i++) {
                if (this.nowFriendHp[i] > 0) {
                    ++friendNowShips;
                }
                friendGauge += battle.getStartFriendHp()[i] - this.nowFriendHp[i];

            }
            if (isCombined) {
                for (int i = 0; i < numFshipsCombined; i++) {
                    if (this.nowFriendHpCombined[i] > 0) {
                        ++friendNowShips;
                    }
                    friendGauge += battle.getStartFriendHpCombined()[i] - this.nowFriendHpCombined[i];
                }
            }
            for (int i = 0; i < numEships; i++) {
                if (this.nowEnemyHp[i] > 0)
                    ++enemyNowShips;

                enemyGauge += battle.getStartEnemyHp()[i] - this.nowEnemyHp[i];
            }

            //double enemyGaugeRate = (double) enemyGauge / this.enemyGaugeMax;
            //double friendGaugeRate = (double) friendGauge / this.friendGaugeMax;

            return "味方[艦:" + (numFships + numFshipsCombined) + "→" + friendNowShips + " ゲージ:" +
                    friendGauge + "/" + battle.getFriendGaugeMax() +
                    "] 敵[艦:" + this.nowEnemyHp.length + "→" + enemyNowShips + " ゲージ:" + enemyGauge + "/"
                    + battle.getEnemyGaugeMax() +
                    "]" +
                    //"(" + (enemyGaugeRate / friendGaugeRate) + "/" + (friendGaugeRate / enemyGaugeRate) + ") " +
                    "判定:" + this.estimatedRank.rank();
        }

        /**
         * 受け取ったJSON
         * @return
         */
        public JsonObject getJson() {
            if (this.json == null) {
                return null;
            }
            return JsonUtils.fromString(this.json);
        }

        /**
         * この戦闘フェーズのAPIリクエスト先
         * @return
         */
        public String getApi() {
            return this.kind.getApi().getApiName();
        }

        /**
         * この戦闘フェーズの種別
         * @return kind
         */
        public BattlePhaseKind getKind() {
            return this.kind;
        }

        /**
         * この戦闘フェーズ後の味方艦HP（連合艦隊の時は第一艦隊）
         * @return nowFriendHp
         */
        public int[] getNowFriendHp() {
            return this.nowFriendHp;
        }

        /**
         * この戦闘フェーズ後の味方艦HP（連合艦隊でないときはnull）
         * @return nowFriendHpCombined
         */
        public int[] getNowFriendHpCombined() {
            return this.nowFriendHpCombined;
        }

        /**
         * この戦闘フェーズ後の敵艦HP
         * @return nowEnemyHp
         */
        public int[] getNowEnemyHp() {
            return this.nowEnemyHp;
        }

        /**
         * この戦闘フェーズ後のランク（予測値）
         * @return estimatedRank
         */
        public ResultRank getEstimatedRank() {
            return this.estimatedRank;
        }

        /**
         * この戦闘フェーズが夜戦か？
         * @return isNight
         */
        public boolean isNight() {
            return this.isNight;
        }

        /**
         * 支援攻撃のタイプ
         * @return supportType
         */
        public String getSupportType() {
            return this.supportType;
        }

        /**
         * 触接機 [味方・敵] -1の場合は「触接なし」
         * @return touchPlane
         */
        public int[] getTouchPlane() {
            return this.touchPlane;
        }

        /**
         * 制空状態
         * @return seiku
         */
        public String getSeiku() {
            return this.seiku;
        }

        /**
         * 損害率 [味方, 敵]
         * @return damageRate
         */
        public double[] getDamageRate() {
            return this.damageRate;
        }

        /**
         * 航空戦1
         * @return air
         */
        public AirBattleDto getAir() {
            return this.air;
        }

        /**
         * 航空戦2
         * @return air2
         */
        public AirBattleDto getAir2() {
            return this.air2;
        }

        /**
         * 支援艦隊の攻撃
         * @return support
         */
        public List<BattleAtackDto> getSupport() {
            return this.support;
        }

        /**
         * 開幕
         * @return opening
         */
        public List<BattleAtackDto> getOpening() {
            return this.opening;
        }

        /**
         * 雷撃戦
         * @return raigeki
         */
        public List<BattleAtackDto> getRaigeki() {
            return this.raigeki;
        }

        /**
         * 夜戦
         * @return hougeki
         */
        public List<BattleAtackDto> getHougeki() {
            return this.hougeki;
        }

        /**
         * 砲撃戦1
         * @return hougeki1
         */
        public List<BattleAtackDto> getHougeki1() {
            return this.hougeki1;
        }

        /**
         * 砲撃戦2
         * @return hougeki2
         */
        public List<BattleAtackDto> getHougeki2() {
            return this.hougeki2;
        }

        /**
         * 砲撃戦3
         * @return hougeki3
         */
        public List<BattleAtackDto> getHougeki3() {
            return this.hougeki3;
        }

        /**
         * @param touchPlane セットする touchPlane
         */
        public void setTouchPlane(int[] touchPlane) {
            this.touchPlane = touchPlane;
        }

        /**
         * @return flarePos
         */
        public int[] getFlarePos() {
            return this.flarePos;
        }
    }

    /**
     * 戦闘データオブジェクト作成
     * @param date 戦闘のあった日時
     */
    public BattleExDto(Date date) {
        this.battleDate = date;
    }

    /**
     * 母港情報を設定
     * @param shipSpace
     * @param itemSpace
     */
    public void setBasicInfo(int shipSpace, int itemSpace) {
        this.shipSpace = shipSpace;
        this.itemSpace = itemSpace;
    }

    /**
     * 中に保存してあるJSONを使ってフィールドを更新する
     */
    public void readFromJson() {
        if (this.exVersion >= 2) {
            Phase[] phaseCopy = this.phaseList.toArray(new Phase[0]);
            this.enemy.clear();
            this.phaseList.clear();
            for (Phase phase : phaseCopy) {
                this.addPhase(phase.getJson(), phase.getKind());
            }
            this.readResultJson(JsonUtils.fromString(this.resultJson));
        }
    }

    /**
     * 戦闘フェーズ結果を読み込む
     * @param object 受け取ったJSON
     * @param kind 戦闘の種別
     * @return 作成されたPhaseオブジェクト
     */
    public Phase addPhase(JsonObject object, BattlePhaseKind kind) {
        if (this.phaseList.size() == 0) {
            // 最初のフェーズ
            String dockId;

            if (object.containsKey("api_dock_id")) {
                dockId = object.get("api_dock_id").toString();
            } else {
                dockId = object.get("api_deck_id").toString();
            }

            JsonArray nowhps = object.getJsonArray("api_nowhps");
            JsonArray maxhps = object.getJsonArray("api_maxhps");
            JsonArray nowhpsCombined = object.getJsonArray("api_nowhps_combined");
            JsonArray maxhpsCombined = object.getJsonArray("api_maxhps_combined");
            boolean isCombined = (nowhpsCombined != null);

            int numFships = 6;
            int numFshipsCombined = 0;

            for (int i = 1; i <= 6; ++i) {
                if (maxhps.getInt(i) == -1) {
                    numFships = i - 1;
                    break;
                }
            }
            if (object.containsKey("api_fParam_combined")) {
                numFshipsCombined = 6;
                for (int i = 1; i <= 6; ++i) {
                    if (maxhpsCombined.getInt(i) == -1) {
                        numFshipsCombined = i - 1;
                        break;
                    }
                }
            }

            if (this.friends.size() == 0) { // 再読み込みの場合はスキップ
                this.friends.add(GlobalContext.getDock(dockId));
                if (numFshipsCombined > 0) {
                    this.friends.add(GlobalContext.getDock("2"));
                }
            }

            JsonArray shipKe = object.getJsonArray("api_ship_ke");
            JsonArray eSlots = object.getJsonArray("api_eSlot");
            JsonArray eParams = object.getJsonArray("api_eParam");
            JsonArray eLevel = object.getJsonArray("api_ship_lv");
            for (int i = 1; i < shipKe.size(); i++) {
                int id = shipKe.getInt(i);
                if (id != -1) {
                    int[] slot = JsonUtils.toIntArray(eSlots.getJsonArray(i - 1));
                    int[] param = JsonUtils.toIntArray(eParams.getJsonArray(i - 1));
                    this.enemy.add(new EnemyShipDto(id, slot, param, eLevel.getInt(i)));
                }
            }
            int numEships = this.enemy.size();

            this.startFriendHp = new int[numFships];
            this.startEnemyHp = new int[numEships];
            this.maxFriendHp = new int[numFships];
            this.maxEnemyHp = new int[numEships];
            if (isCombined) {
                this.startFriendHpCombined = new int[numFshipsCombined];
                this.maxFriendHpCombined = new int[numFshipsCombined];
            }
            else {
                this.maxFriendHpCombined = null;
            }

            // 陣形
            if (object.containsKey("api_formation")) {
                JsonArray formation = object.getJsonArray("api_formation");
                for (int i = 0; i < 2; ++i) {
                    switch (formation.get(i).getValueType()) {
                    case NUMBER:
                        this.formation[i] = toFormation(formation.getInt(i));
                        break;
                    default:
                        this.formation[i] = toFormation(Integer.parseInt(formation.getString(i)));
                    }
                }
                this.formationMatch = toMatch(formation.getInt(2));
            }

            // 索敵
            JsonArray jsonSearch = object.getJsonArray("api_search");
            if (jsonSearch != null) {
                this.sakuteki = new String[] {
                        toSearch(jsonSearch.getInt(0)),
                        toSearch(jsonSearch.getInt(1))
                };
            }

            // この戦闘の開始前HPを取得
            for (int i = 1; i < nowhps.size(); i++) {
                int hp = nowhps.getInt(i);
                int maxHp = maxhps.getInt(i);
                if (i <= 6) {
                    if (i <= numFships) {
                        this.maxFriendHp[i - 1] = maxHp;
                        this.friendGaugeMax += this.startFriendHp[i - 1] = hp;
                    }
                } else {
                    if ((i - 6) <= numEships) {
                        this.maxEnemyHp[i - 1 - 6] = maxHp;
                        this.enemyGaugeMax += this.startEnemyHp[i - 1 - 6] = hp;
                    }
                }
            }
            if (isCombined) {
                for (int i = 1; i < nowhpsCombined.size(); i++) {
                    int hp = nowhpsCombined.getInt(i);
                    int maxHp = maxhpsCombined.getInt(i);
                    if (i <= numFshipsCombined) {
                        this.maxFriendHpCombined[i - 1] = maxHp;
                        this.friendGaugeMax += this.startFriendHpCombined[i - 1] = hp;
                    }
                }

                // 退避
                this.escaped = new boolean[12];
                if (JsonUtils.hasKey(object, "api_escape_idx")) {
                    for (JsonValue jsonShip : object.getJsonArray("api_escape_idx")) {
                        this.escaped[((JsonNumber) jsonShip).intValue() - 1] = true;
                    }
                }
                if (JsonUtils.hasKey(object, "api_escape_idx_combined")) {
                    for (JsonValue jsonShip : object.getJsonArray("api_escape_idx_combined")) {
                        this.escaped[(((JsonNumber) jsonShip).intValue() - 1) + 6] = true;
                    }
                }
                for (int i = 0; i < 2; ++i) {
                    this.friends.get(i).setEscaped(Arrays.copyOfRange(this.escaped, i * 6, (i + 1) * 6));
                }
            }
        }

        if (this.phaseList.size() > 0) {
            Phase phase = this.phaseList.get(0);
            this.completeDamageAndAddPhase(new Phase(this, object, kind,
                    phase.getNowFriendHp(), phase.getNowFriendHpCombined(), phase.getNowEnemyHp()), kind);
        }
        else {
            this.completeDamageAndAddPhase(new Phase(this, object, kind,
                    this.startFriendHp, this.startFriendHpCombined, this.startEnemyHp), kind);
        }
        return this.phaseList.get(this.phaseList.size() - 1);
    }

    private void completeDamageAndAddPhase(Phase phase, BattlePhaseKind kind) {
        if (kind.isPractice()) {
            phase.practiceDamage(this);
        }
        else {
            phase.battleDamage(this);
        }
        this.phaseList.add(phase);
    }

    private void readResultJson(JsonObject object) {
        if (object.get("api_quest_name") != null) {
            this.questName = object.getString("api_quest_name");
        }
        else {
            // 演習の場合はない
            this.questName = null;
        }
        this.rank = ResultRank.fromRank(object.getString("api_win_rank"));
        // 完全勝利Sは分からないので戦闘結果を見る
        Phase lastPhase = this.getLastPhase();
        if ((lastPhase != null) && (lastPhase.getEstimatedRank() == ResultRank.PERFECT)) {
            this.rank = ResultRank.PERFECT;
        }
        this.enemyName = object.getJsonObject("api_enemy_info").getString("api_deck_name");
        this.dropShip = object.containsKey("api_get_ship");
        this.dropItem = object.containsKey("api_get_useitem");
        if (this.dropShip || this.dropItem) {
            if (this.dropShip) {
                JsonObject getShip = object.getJsonObject("api_get_ship");
                this.dropShipId = getShip.getInt("api_ship_id");
                this.dropType = getShip.getString("api_ship_type");
                this.dropName = getShip.getString("api_ship_name");
            } else {
                String name = UseItem.get(object.getJsonObject("api_get_useitem").getInt("api_useitem_id"));
                this.dropType = "アイテム";
                this.dropName = StringUtils.defaultString(name);
            }
        } else {
            this.dropType = "";
            this.dropName = "";
        }
        this.mvp = object.getInt("api_mvp");
        if (JsonUtils.hasKey(object, "api_mvp_combined")) {
            this.mvpCombined = object.getInt("api_mvp_combined");
        }
        this.hqLv = object.getInt("api_member_lv");
        if (JsonUtils.hasKey(object, "api_escape")) {
            JsonObject jsonEscape = object.getJsonObject("api_escape");
            this.escapeInfo = new int[] {
                    jsonEscape.getJsonArray("api_escape_idx").getInt(0) - 1,
                    jsonEscape.getJsonArray("api_tow_idx").getInt(0) - 1
            };
        }
        if (JsonUtils.hasKey(object, "api_lost_flag")) {
            this.lostflag = new boolean[6];
            JsonArray jsonLostflag = object.getJsonArray("api_lost_flag");
            for (int i = 1; i < jsonLostflag.size(); i++) {
                this.lostflag[i - 1] = (jsonLostflag.getInt(i) != 0);
            }
        }
    }

    /**
     * 戦闘結果を読み込む
     * @param object 受け取ったJSON
     * @param mapInfo マス情報
     */
    public void setResult(JsonObject object, MapCellDto mapInfo) {
        this.resultJson = object.toString();
        this.mapCellDto = mapInfo;
        this.readResultJson(object);
    }

    public static String toFormation(int f) {
        String formation;
        switch (f) {
        case 1:
            formation = "単縦陣";
            break;
        case 2:
            formation = "複縦陣";
            break;
        case 3:
            formation = "輪形陣";
            break;
        case 4:
            formation = "梯形陣";
            break;
        case 5:
            formation = "単横陣";
            break;
        case 11:
            formation = "第一警戒航行序列";
            break;
        case 12:
            formation = "第二警戒航行序列";
            break;
        case 13:
            formation = "第三警戒航行序列";
            break;
        case 14:
            formation = "第四警戒航行序列";
            break;
        default:
            formation = "単縦陣";
            break;
        }
        return formation;
    }

    public static int fromFormation(String f) {
        if (f.startsWith("単縦"))
            return 1;
        else if (f.startsWith("複縦"))
            return 2;
        else if (f.startsWith("輪形"))
            return 3;
        else if (f.startsWith("梯形"))
            return 4;
        else if (f.startsWith("単横"))
            return 5;
        else if (f.startsWith("第一警戒"))
            return 11;
        else if (f.startsWith("第二警戒"))
            return 12;
        else if (f.startsWith("第三警戒"))
            return 13;
        else if (f.startsWith("第四警戒"))
            return 14;
        else
            return 1;
    }

    private static String toMatch(int id) {
        switch (id) {
        case 1:
            return "同航戦";
        case 2:
            return "反航戦";
        case 3:
            return "Ｔ字有利";
        case 4:
            return "Ｔ字不利";
        default:
            return "不明(" + id + ")";
        }
    }

    private static String toSupport(int id) {
        switch (id) {
        case 1:
            return "航空支援";
        case 2:
            return "支援射撃";
        case 3:
            return "支援長距離雷撃";
        default:
            return "不明(" + id + ")";
        }
    }

    private static String toSearch(int id) {
        switch (id) {
        case 1:
            return "発見!";
        case 2:
            return "発見!索敵機未帰還機あり";
        case 3:
            return "発見できず…索敵機未帰還機あり";
        case 4:
            return "発見できず…";
        case 5:
            return "発見!(索敵機なし)";
        case 6:
            return "なし";
        default:
            return "不明(" + id + ")";
        }
    }

    /**
     * 保存用エネミーデータ作成
     * @param enemyId
     * @param enemyName
     * @return
     */
    public EnemyData getEnemyData(int enemyId, String enemyName) {
        int[] enemyShips = new int[] { -1, -1, -1, -1, -1, -1 };
        for (int i = 0; i < this.enemy.size(); ++i) {
            enemyShips[i] = this.enemy.get(i).getShipId();
        }
        return new EnemyData(enemyId, enemyName, enemyShips, this.formation[1]);
    }

    /**
     * 連合艦隊か？
     * @return
     */
    public boolean isCombined() {
        return (this.startFriendHpCombined != null);
    }

    /**
     * 最後に行ったフェーズを取得
     * @return
     */
    public Phase getLastPhase() {
        if (this.phaseList.size() == 0)
            return null;
        return this.phaseList.get(this.phaseList.size() - 1);
    }

    /**
     * 最初のフェーズを取得
     * @return
     */
    public Phase getPhase1() {
        if (this.phaseList.size() < 1)
            return null;
        return this.phaseList.get(0);
    }

    /**
     * ２番目のフェーズ（ない時はnull）
     * @return
     */
    public Phase getPhase2() {
        if (this.phaseList.size() < 2)
            return null;
        return this.phaseList.get(1);
    }

    /**
     * 戦闘結果も含んでいるか
     * これがfalseに場合は正常に記録されない
     * @return
     */
    public boolean isCompleteResult() {
        if (this.questName != null) {
            // 出撃の場合
            if (this.mapCellDto == null) {
                return false;
            }
        }
        else {
            // 演習の場合
        }
        return (this.friends != null) && (this.getDock() != null) &&
                (this.rank != null) && (this.phaseList != null) &&
                (this.phaseList.size() > 0);
    }

    /**
     * 演習か？
     * @return
     */
    public boolean isPractice() {
        return (this.questName == null);
    }

    /**
     * 交戦後の味方艦HP（連合艦隊の時は第一艦隊）
     * @return
     */
    public int[] getNowFriendHp() {
        return this.getLastPhase().getNowFriendHp();
    }

    /**
     * 交戦後の味方艦HP（連合艦隊でないときはnull）
     * @return
     */
    public int[] getNowFriendHpCombined() {
        return this.getLastPhase().getNowFriendHpCombined();
    }

    /**
     * 交戦後の敵艦HP
     * @return
     */
    public int[] getNowEnemyHp() {
        return this.getLastPhase().getNowEnemyHp();
    }

    /**
     * 味方艦隊（連合艦隊の時は第一艦隊）
     * @return
     */
    public DockDto getDock() {
        return this.friends.get(0);
    }

    /**
     * 連合艦隊第二艦隊（連合艦隊でないときはnull）
     * @return
     */
    public DockDto getDockCombined() {
        if (this.friends.size() < 2)
            return null;
        return this.friends.get(1);
    }

    /**
     * 戦闘のあった日時
     * @return battleDate
     */
    public Date getBattleDate() {
        return this.battleDate;
    }

    /**
     * 味方艦隊
     * @return friends
     */
    public List<DockDto> getFriends() {
        return this.friends;
    }

    /**
     * 敵艦
     * @return enemy
     */
    public List<EnemyShipDto> getEnemy() {
        return this.enemy;
    }

    /**
     * 味方艦のMaxHP
     * 連合艦隊の時は第一艦隊のみ
     * @return maxFriendHp
     */
    public int[] getMaxFriendHp() {
        return this.maxFriendHp;
    }

    /**
     * 味方連合艦隊第二艦隊のMaxHP
     * @return maxFriendHpCombined
     */
    public int[] getMaxFriendHpCombined() {
        return this.maxFriendHpCombined;
    }

    /**
     * 敵艦のMaxHP
     * @return maxEnemyHp
     */
    public int[] getMaxEnemyHp() {
        return this.maxEnemyHp;
    }

    /**
     * 戦闘開始時の味方艦のHP
     * 連合艦隊の時は第一艦隊のみ
     * @return startFriendHp
     */
    public int[] getStartFriendHp() {
        return this.startFriendHp;
    }

    /**
     * 味方連合艦隊第二艦隊の戦闘開始時HP
     * @return startFriendHpCombined
     */
    public int[] getStartFriendHpCombined() {
        return this.startFriendHpCombined;
    }

    /**
     * 敵艦の戦闘開始時HP
     * @return startEnemyHp
     */
    public int[] getStartEnemyHp() {
        return this.startEnemyHp;
    }

    /**
     * 味方戦果ゲージの最大（味方艦MaxHPの合計）
     * @return friendGaugeMax
     */
    public int getFriendGaugeMax() {
        return this.friendGaugeMax;
    }

    /**
     * 敵戦果ゲージの最大（敵艦MaxHPの合計）
     * @return enemyGaugeMax
     */
    public int getEnemyGaugeMax() {
        return this.enemyGaugeMax;
    }

    /**
     * 陣形 [味方, 敵] 
     * @return formation
     */
    public String[] getFormation() {
        return this.formation;
    }

    /**
     * 同航戦、反航戦など
     * @return formationMatch
     */
    public String getFormationMatch() {
        return this.formationMatch;
    }

    /**
     * 索敵状況 [味方, 敵]
     * @return sakuteki
     */
    public String[] getSakuteki() {
        return this.sakuteki;
    }

    /**
     * 出撃海域情報
     * @return questName
     */
    public String getQuestName() {
        return this.questName;
    }

    /**
     * 戦闘結果のランク
     * @return rank
     */
    public ResultRank getRank() {
        return this.rank;
    }

    /**
     * 戦闘のあったマスの情報
     * @return mapCelldto
     */
    public MapCellDto getMapCellDto() {
        return this.mapCellDto;
    }

    /**
     * 敵艦隊の名前
     * @return enemyName
     */
    public String getEnemyName() {
        return this.enemyName;
    }

    /**
     * ドロップ艦があったか？
     * @return dropShip
     */
    public boolean isDropShip() {
        return this.dropShip;
    }

    /**
     * ドロップアイテムがあったか？
     * @return dropItem
     */
    public boolean isDropItem() {
        return this.dropItem;
    }

    /**
     * ドロップ艦の艦種（アイテムの場合は「アイテム」）
     * @return dropType
     */
    public String getDropType() {
        return this.dropType;
    }

    /**
     * ドロップ艦・アイテムの名前
     * @return dropName
     */
    public String getDropName() {
        return this.dropName;
    }

    /**
     * 戦闘フェーズ（昼戦・夜戦）リスト
     * @return phaseList
     */
    public List<Phase> getPhaseList() {
        return this.phaseList;
    }

    /**
     * MVP艦が何番目の艦か (0～)
     * MVPがいない時は-1
     * @return mvp
     */
    public int getMvp() {
        return this.mvp;
    }

    /**
     * 連合艦隊第二艦隊のMVP艦が何番目の艦か
     * 連合艦隊でない時またはMVPがいない時は-1
     * @return mvpCombined
     */
    public int getMvpCombined() {
        return this.mvpCombined;
    }

    /**
     * 司令部Lv
     * @return hqLv
     */
    public int getHqLv() {
        return this.hqLv;
    }

    /*** 
     * BattleExDtoのバージョン
     * exVersion == 0 : Tag 34以降がない
     * exVersion == 1 : Tag 36まである
     * exVersion == 2 : Jsonがある
     * @return exVersion
     */
    public int getExVersion() {
        return this.exVersion;
    }

    void setExVersion(int exVersion) {
        this.exVersion = exVersion;
    }

    /**
     * 母港の艦娘空き枠
     * @return shipSpace
     */
    public int getShipSpace() {
        return this.shipSpace;
    }

    /**
     * 母港の装備アイテム空き枠
     * @return itemSpace
     */
    public int getItemSpace() {
        return this.itemSpace;
    }

    /**
     * 連合艦隊における退避意見 [退避する艦(0-11), 護衛艦(0-11)]
     * @return escapeInfo
     */
    public int[] getEscapeInfo() {
        return this.escapeInfo;
    }

    /**
     * 護衛退避で戦線離脱したか [第1艦隊1番艦～第2艦隊6番艦]
     * 艦隊の艦数に関係なく常に長さは12
     * @return escaped
     */
    public boolean[] getEscaped() {
        return this.escaped;
    }

    /**
     * 戦闘結果のレスポンスJSON
     * @return resultJson
     */
    public JsonObject getResultJson() {
        if (this.resultJson == null) {
            return null;
        }
        return JsonUtils.fromString(this.resultJson);
    }

    /**
     * @return lostflag
     */
    public boolean[] getLostflag() {
        return this.lostflag;
    }

    /**
     * @return shipId
     */
    public int getDropShipId() {
        return this.dropShipId;
    }
}