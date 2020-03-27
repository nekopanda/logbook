/**
 *
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.IntStream;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.constants.AppConstants;
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

    private static final int ENEMY_SECOND_BASE = 6;

    /** 日付 */
    @Tag(1)
    private final Date battleDate;

    /** 味方艦隊 */
    @Tag(2)
    private final List<DockDto> friends = new ArrayList<>();

    /** 敵艦隊 */
    @Tag(3)
    private final List<EnemyShipDto> enemy = new ArrayList<>();

    /** 敵随伴艦隊 */
    @Tag(31)
    private List<EnemyShipDto> enemyCombined = new ArrayList<>();

    /** 味方MaxHP */
    @Tag(6)
    private int[] maxFriendHp;

    @Tag(7)
    private int[] maxFriendHpCombined;

    @Tag(116)
    private int[] maxFriendlyFleetHp;

    /** 敵MaxHP */
    @Tag(8)
    private int[] maxEnemyHp;

    @Tag(32)
    private int[] maxEnemyHpCombined;

    /** 味方戦闘開始時HP */
    @Tag(9)
    private int[] startFriendHp;

    @Tag(10)
    private int[] startFriendHpCombined;

    @Tag(117)
    private int[] startFriendlyFleetHp;

    /** 敵戦闘開始時HP */
    @Tag(11)
    private int[] startEnemyHp;

    @Tag(33)
    private int[] startEnemyHpCombined;

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

    /** アイテム名 */
    @Tag(47)
    private String dropItemName;

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

    /** 連合艦隊の種類 */
    @Tag(52)
    private int combinedKind = 0;

    /** 新旧API */
    @Tag(91)
    private int baseidx = 0;

    //@Tag(92)
    //private int secondBase = 0;

    @Tag(93)
    private int friendSecondBase = 0;

    @Tag(94)
    private final int enemySecondBase = 0;

    private static Date date20170405;

    static {
        // 敵艦IDが+1000された日時
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
        calendar.set(2017, 3, 5, 15, 0);
        date20170405 = calendar.getTime();
    }

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

        @Tag(108)
        private final int[] nowFriendlyFleetHp;
        
        @Tag(120)
        private final int[] maxFriendlyFleetHp;

        @Tag(121)
        private final int[] beforeFriendlyFleetHp;

        /** 友軍艦隊 */
        @Tag(115)
        private List<EnemyShipDto> ff = new ArrayList<>();

        /** 友軍艦隊 触接機（味方・敵） -1の場合は「触接なし」 */
        @Tag(106)
        private int[] friendlytouchPlane;

        /** 友軍艦隊 照明弾発射艦 */
        @Tag(107)
        private int[] friendlyflarePos;

        /** 敵HP */
        @Tag(4)
        private final int[] nowEnemyHp;

        @Tag(21)
        private final int[] nowEnemyHpCombined;

        /** ランク */
        @Tag(5)
        private ResultRank estimatedRank;

        /** 夜戦 */
        @Tag(6)
        private final boolean isNight;

        /** 敵は連合艦隊第二艦隊か？ */
        @Tag(22)
        private final boolean isEnemySecond;

        /** 自分は連合艦隊の第二艦隊か？*/
        @Tag(100)
        private final boolean isFriendSecond;

        /** 払暁戦（夜戦から始まり昼戦へ移行）か？*/
        @Tag(101)
        private final boolean isNightToDay;

        /** 友軍艦隊支援か？*/
        @Tag(104)
        private final boolean isFriendFleet;

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
        @Tag(36)
        private AirBattleDto airBaseInjection = null;
        @Tag(35)
        private List<AirBattleDto> airBase = null;
        @Tag(37)
        private AirBattleDto airInjection = null;
        @Tag(11)
        private AirBattleDto air = null;
        @Tag(12)
        private AirBattleDto air2 = null;
        @Tag(13)
        private List<BattleAtackDto> support = null;
        @Tag(50)
        private List<BattleAtackDto> openingTaisen = null;
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
        @Tag(102)
        private List<BattleAtackDto> hougeki_n_1 = null;
        @Tag(103)
        private List<BattleAtackDto> hougeki_n_2 = null;
        @Tag(105)
        private List<BattleAtackDto> hougeki_f = null;

        @Tag(30)
        private final String json;

        public Phase(BattleExDto battle, JsonObject object, BattlePhaseKind kind,
                int[] beforeFriendHp, int[] beforeFriendHpCombined, int[] beforeEnemyHp, int[] beforeEnemyHpCombined) {
            int baseidx = battle.baseidx;
            boolean isFriendCombined = (beforeFriendHpCombined != null);
            boolean isEnemyCombined = (beforeEnemyHpCombined != null);

            // 敵は連合艦隊の第二艦隊か？（敵連合艦隊夜戦で第二艦隊が相手の場合のみ）
            this.isEnemySecond = (object.containsKey("api_active_deck")
                    ? (object.getJsonArray("api_active_deck").getInt(1) == 2) : false);

            // 自分は連合艦隊の第二艦隊か？（自連合艦隊 vs 敵連合艦隊の夜戦で
            // 自分が第二艦隊であることの確認）
            this.isFriendSecond = (object.containsKey("api_active_deck")
                    ? (object.getJsonArray("api_active_deck").getInt(0) == 2) : false);

            // 払暁戦か？
            this.isNightToDay = (object.containsKey("api_day_flag")
                    ? (object.getInt("api_day_flag") == 1) : false);

            this.kind = kind;
            this.isNight = kind.isNight();

            // 友軍艦隊支援か？
            this.isFriendFleet = (object.containsKey("api_friendly_battle")
                    ? true : false);

            this.nowFriendHp = beforeFriendHp.clone();
            this.nowEnemyHp = beforeEnemyHp.clone();
            this.nowFriendHpCombined = isFriendCombined ? beforeFriendHpCombined.clone() : null;
            this.nowEnemyHpCombined = isEnemyCombined ? beforeEnemyHpCombined.clone() : null;

            int n[] = {0,0,0,0,0,0,0}; 

            this.nowFriendlyFleetHp = n.clone();
            this.maxFriendlyFleetHp = n.clone();
            this.beforeFriendlyFleetHp = n.clone();

            if (this.isFriendFleet) {
                this.ff = new ArrayList<>();
                JsonObject friendlyInfoObj = JsonUtils.getJsonObject(object, "api_friendly_info");
                JsonArray ffshipid = JsonUtils.getJsonArray(friendlyInfoObj, "api_ship_id");
                JsonArray ffSlots = JsonUtils.getJsonArray(friendlyInfoObj, "api_Slot");
                JsonArray ffParams = JsonUtils.getJsonArray(friendlyInfoObj, "api_Param");
                JsonArray ffLevel = JsonUtils.getJsonArray(friendlyInfoObj, "api_ship_lv");

                for (int i = baseidx; i < ffshipid.size(); i++) {
                    int ffid = ffshipid.getInt(i);
                    int[] ffslot = JsonUtils.toIntArray(ffSlots.getJsonArray(i - baseidx));
                    int[] ffparam = JsonUtils.toIntArray(ffParams.getJsonArray(i - baseidx));
                    this.ff.add(new EnemyShipDto(ffid, ffslot, ffparam, ffLevel.getInt(i)));
                }


                JsonArray ff_maxhps = JsonUtils.getJsonArray(friendlyInfoObj, "api_maxhps");
                JsonArray ff_nowhps = JsonUtils.getJsonArray(friendlyInfoObj, "api_nowhps");
                for (int i= baseidx; i< ff_maxhps.size(); i++) {
                   this.maxFriendlyFleetHp[i] = ff_maxhps.getInt(i);
                   this.nowFriendlyFleetHp[i] = ff_nowhps.getInt(i);
                   this.beforeFriendlyFleetHp[i] = ff_nowhps.getInt(i);
                }
            } 

            // 友軍艦隊 夜間触接および照明弾発射艦
            if (JsonUtils.hasKey(object, "api_friendly_battle")) {
                JsonObject friendlyBattleObj = JsonUtils.getJsonObject(object, "api_friendly_battle");
                
                JsonArray jsonfriendlyTouchPlane = JsonUtils.getJsonArray(friendlyBattleObj, "api_touch_plane");
                if (jsonfriendlyTouchPlane != null) {
                    this.friendlytouchPlane = new int[] {
                        Integer.parseInt(jsonfriendlyTouchPlane.get(0).toString()),
                        Integer.parseInt(jsonfriendlyTouchPlane.get(1).toString())
                        };
                }
                
                JsonArray jsonfriendlyFlarePos = JsonUtils.getJsonArray(friendlyBattleObj, "api_flare_pos");
                
                if ((jsonfriendlyFlarePos != null) && (jsonfriendlyFlarePos != JsonValue.NULL)) {
                    this.friendlyflarePos = JsonUtils.getIntArray(friendlyBattleObj, "api_flare_pos");
                }
            }

            // 夜間触接
            JsonArray jsonTouchPlane = JsonUtils.getJsonArray(object, "api_touch_plane");
            if (jsonTouchPlane != null) {
                this.touchPlane = new int[] {
                        Integer.parseInt(jsonTouchPlane.get(0).toString()),
                        Integer.parseInt(jsonTouchPlane.get(1).toString()),
                };
            }

            // 照明弾発射艦
            JsonValue jsonFlarePos = object.get("api_flare_pos");
            if ((jsonFlarePos != null) && (jsonFlarePos != JsonValue.NULL)) {
                this.flarePos = JsonUtils.getIntArray(object, "api_flare_pos");
            }

            // 攻撃シーケンスを読み取る //

            // 基地航空隊
            JsonValue air_base_attack = object.get("api_air_base_attack");
            if (air_base_attack instanceof JsonArray) {
                this.airBase = new ArrayList<>();
                for (JsonValue elem : (JsonArray) air_base_attack) {
                    JsonObject obj = (JsonObject) elem;
                    this.airBase.add(new AirBattleDto(baseidx, battle.friendSecondBase,
                            obj, isFriendCombined || isEnemyCombined, true));
                }
            }

            // 基地航空隊（墳式強襲）
            JsonObject air_base_injection = object.getJsonObject("api_air_base_injection");
            if (air_base_injection != null) {
                this.airBaseInjection = new AirBattleDto(baseidx, battle.friendSecondBase,
                        air_base_injection, isFriendCombined || isEnemyCombined, true);
            }

            // 航空戦（墳式強襲）
            JsonObject injection_kouku = object.getJsonObject("api_injection_kouku");
            if (injection_kouku != null) {
                this.airInjection = new AirBattleDto(baseidx, battle.friendSecondBase,
                        injection_kouku, isFriendCombined || isEnemyCombined, false);
            }

            // 航空戦（通常）
            JsonObject kouku = object.getJsonObject("api_kouku");
            if (kouku != null) {
                this.air = new AirBattleDto(baseidx, battle.friendSecondBase,
                        kouku, isFriendCombined || isEnemyCombined, false);
                // 昼戦の触接はここ
                this.touchPlane = this.air.touchPlane;
                // 制空はここから取る
                this.seiku = this.air.seiku;
            }

            // 支援艦隊
            JsonNumber support_flag = object.getJsonNumber("api_support_flag");
            JsonNumber support_n_flag = object.getJsonNumber("api_n_support_flag");

            if ((support_flag != null) && (support_flag.intValue() != 0)) {
                JsonObject support = JsonUtils.getJsonObject(object, "api_support_info");
                JsonObject support_hourai = JsonUtils.getJsonObject(support, "api_support_hourai");
                JsonObject support_air = JsonUtils.getJsonObject(support, "api_support_airatack");
                if (support_hourai != null) {
                    JsonArray edam = JsonUtils.getJsonArray(support_hourai, "api_damage");
                    if (edam != null) {
                        this.support = BattleAtackDto.makeSupport(baseidx, edam);
                    }
                }
                else if (support_air != null) {
                    JsonObject stage3 = JsonUtils.getJsonObject(support_air, "api_stage3");
                    if (stage3 != null) {
                        this.support = BattleAtackDto.makeSupport(baseidx,
                                JsonUtils.getJsonArray(stage3, "api_edam"));
                    }
                }
                this.supportType = toSupport(support_flag.intValue());
            }
            else if ((support_n_flag != null) && (support_n_flag.intValue() != 0)) {
                JsonObject support = JsonUtils.getJsonObject(object, "api_n_support_info");
                JsonObject support_hourai = JsonUtils.getJsonObject(support, "api_support_hourai");
                JsonObject support_air = JsonUtils.getJsonObject(support, "api_support_airatack");
                if (support_hourai != null) {
                    JsonArray edam = JsonUtils.getJsonArray(support_hourai, "api_damage");
                    if (edam != null) {
                        this.support = BattleAtackDto.makeSupport(baseidx, edam);
                    }
                }
                else if (support_air != null) {
                    JsonObject stage3 = JsonUtils.getJsonObject(support_air, "api_stage3");
                    if (stage3 != null) {
                        this.support = BattleAtackDto.makeSupport(baseidx,
                                JsonUtils.getJsonArray(stage3, "api_edam"));
                    }
                }
                this.supportType = toSupport(support_n_flag.intValue());
            }
            else {
                this.supportType = "";
            }

            // 航空戦（連合艦隊のみ？）
            JsonObject kouku2 = object.getJsonObject("api_kouku2");
            if (kouku2 != null)
                this.air2 = new AirBattleDto(baseidx, battle.friendSecondBase,
                        kouku2, isFriendCombined || isEnemyCombined, false);

            // 開幕対潜
            this.openingTaisen = BattleAtackDto.makeHougeki(baseidx, battle.friendSecondBase,
                    JsonUtils.getJsonObject(object, "api_opening_taisen"),
                    kind.isOpeningSecond(),
                    this.isEnemySecond);

            // 開幕
            this.opening = BattleAtackDto.makeRaigeki(baseidx, battle.friendSecondBase,
                    JsonUtils.getJsonObject(object, "api_opening_atack"),
                    kind.isOpeningSecond());

            // 砲撃
            if (this.isFriendSecond) {
                this.hougeki = BattleAtackDto.makeHougeki(baseidx, battle.friendSecondBase,
                        JsonUtils.getJsonObject(object, "api_hougeki"),
                        true, this.isEnemySecond); // 自分が連合艦隊の場合の夜戦
            }
            else {
                this.hougeki = BattleAtackDto.makeHougeki(baseidx, battle.friendSecondBase,
                        JsonUtils.getJsonObject(object, "api_hougeki"),
                        kind.isHougekiSecond(), this.isEnemySecond); // 夜戦
            }
            if (this.isNightToDay) {
                this.hougeki_n_1 = BattleAtackDto.makeHougeki(baseidx, battle.friendSecondBase,
                        JsonUtils.getJsonObject(object, "api_n_hougeki1"),
                        false, true);
                this.hougeki_n_2 = BattleAtackDto.makeHougeki(baseidx, battle.friendSecondBase,
                        JsonUtils.getJsonObject(object, "api_n_hougeki2"),
                        false, false);
            }
            this.hougeki1 = BattleAtackDto.makeHougeki(baseidx, battle.friendSecondBase,
                    JsonUtils.getJsonObject(object, "api_hougeki1"),
                    kind.isHougeki1Second(), this.isEnemySecond);
            this.hougeki2 = BattleAtackDto.makeHougeki(baseidx, battle.friendSecondBase,
                    JsonUtils.getJsonObject(object, "api_hougeki2"),
                    kind.isHougeki2Second(), this.isEnemySecond);
            this.hougeki3 = BattleAtackDto.makeHougeki(baseidx, battle.friendSecondBase,
                    JsonUtils.getJsonObject(object, "api_hougeki3"),
                    kind.isHougeki3Second(), this.isEnemySecond);

            if (this.isFriendFleet) {
		JsonObject FriendObj = JsonUtils.getJsonObject(object, "api_friendly_battle");
                this.hougeki_f = BattleAtackDto.makeHougeki(baseidx, battle.friendSecondBase,
                        JsonUtils.getJsonObject(FriendObj, "api_hougeki"),
                        false, false);
            }



            // 雷撃
            this.raigeki = BattleAtackDto.makeRaigeki(baseidx, battle.friendSecondBase,
                    JsonUtils.getJsonObject(object, "api_raigeki"),
                    kind.isRaigekiSecond());

            // ダメージを反映 //

            if (this.airBaseInjection != null)
                this.doAtack(this.airBaseInjection.atacks, battle.friendSecondBase, this.isFriendFleet, battle);
            if (this.airBase != null)
                for (AirBattleDto attack : this.airBase)
                    this.doAtack(attack.atacks, battle.friendSecondBase, this.isFriendFleet, battle);
            if (this.airInjection != null)
                this.doAtack(this.airInjection.atacks, battle.friendSecondBase, this.isFriendFleet, battle);
            if (this.air != null)
                this.doAtack(this.air.atacks, battle.friendSecondBase, this.isFriendFleet, battle);
            this.doAtack(this.support, battle.friendSecondBase, this.isFriendFleet, battle);
            if (this.air2 != null)
                this.doAtack(this.air2.atacks, battle.friendSecondBase, this.isFriendFleet, battle);
            this.doAtack(this.openingTaisen, battle.friendSecondBase, this.isFriendFleet, battle);
            this.doAtack(this.opening, battle.friendSecondBase, this.isFriendFleet, battle);
            this.doAtack(this.hougeki_n_1, battle.friendSecondBase, this.isFriendFleet, battle);
            this.doAtack(this.hougeki_n_2, battle.friendSecondBase, this.isFriendFleet, battle);
            this.doAtack(this.hougeki1, battle.friendSecondBase, this.isFriendFleet, battle);
            this.doAtack(this.raigeki, battle.friendSecondBase, this.isFriendFleet, battle);
            this.doAtack(this.hougeki2, battle.friendSecondBase, this.isFriendFleet, battle);
            this.doAtack(this.hougeki3, battle.friendSecondBase, this.isFriendFleet, battle);
            if (isFriendFleet) {
                // １つのjsonファイルの中に友軍艦隊の砲撃と自艦隊の砲撃の２つの処理が存在する場合
                this.doAtack(this.hougeki_f, battle.friendSecondBase, this.isFriendFleet, battle);
                this.doAtack(this.hougeki, battle.friendSecondBase, false, battle);
            } else {
                this.doAtack(this.hougeki, battle.friendSecondBase, this.isFriendFleet, battle);
            }

            this.json = object.toString();
        }

        public void battleDamage(BattleExDto battle) {
            int numFships = this.nowFriendHp.length;
            int numEships = this.nowEnemyHp.length;
            DockDto dock = battle.getDock();
            DockDto dockCombined = battle.getDockCombined();

            // HP0以下を0にする
            for (int i = 0; i < numFships; i++) {
                this.nextHp(i, this.nowFriendHp, (dock != null) ? dock.getShips() : null);
            }
            for (int i = 0; i < numEships; i++) {
                if (this.nowEnemyHp[i] <= 0)
                    this.nowEnemyHp[i] = 0;
            }
            if (this.nowFriendHpCombined != null) {
                for (int i = 0; i < this.nowFriendHpCombined.length; i++) {
                    this.nextHp(i, this.nowFriendHpCombined, (dockCombined != null) ? dockCombined.getShips() : null);
                }
            }
            if (this.nowEnemyHpCombined != null) {
                for (int i = 0; i < this.nowEnemyHpCombined.length; i++) {
                    if (this.nowEnemyHpCombined[i] <= 0)
                        this.nowEnemyHpCombined[i] = 0;
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
            if (ships == null) {
                if (hp <= 0) {
                    hps[index] = 0;
                }
                return;
            }
            ShipDto ship = ships.get(index);
            if (hp <= 0) {
                List<ItemDto> items = new ArrayList<>(ship.getItem2());
                items.add(ship.getSlotExItem());
                for (ItemDto item : items) {
                    if (item == null)
                        continue;
                    if (item.getSlotitemId() == 42) { //応急修理要員
                        hps[index] = (int) (ship.getMaxhp() * 0.2);
                        return;
                    }
                    else if (item.getSlotitemId() == 43) { //応急修理女神
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
            boolean isFriendCombined = (this.nowFriendHpCombined != null);
            boolean isEnemyCombined = (this.nowEnemyHpCombined != null);
            int numFships = this.nowFriendHp.length;
            int numFshipsCombined = isFriendCombined ? this.nowFriendHpCombined.length : 0;
            int numEships = this.nowEnemyHp.length;
            int numEshipsCombined = isEnemyCombined ? this.nowEnemyHpCombined.length : 0;
            int[] nowFriendHp = this.nowFriendHp;
            int[] nowFriendHpCombined = this.nowFriendHpCombined;
            int[] nowEnemyHp = this.nowEnemyHp;
            int[] nowEnemyHpCombined = this.nowEnemyHpCombined;
            int friendEscaped = 0;

            // 自艦隊の戦闘終了時のHPの合計(A)
            int friendGauge = IntStream.range(0, numFships)
                    .filter(i -> !((battle.escaped != null) && battle.escaped[i]))
                    .map(i -> nowFriendHp[i]).sum();

            // 自艦隊の戦闘開始時のHPの合計(B)
            int friendGaugeMax = battle.friendGaugeMax;

            // 自艦隊の戦闘終了後の生存数
            int friendNowShips = (int) Arrays.stream(nowFriendHp).filter(hp -> hp > 0).count();

            // 連合艦隊(自艦隊)
            if (isFriendCombined) {
                friendGauge += IntStream.range(0, numFshipsCombined)
                        .filter(i -> !((battle.escaped != null) && battle.escaped[i + 6]))
                        .map(i -> nowFriendHpCombined[i]).sum();
                friendNowShips += (int) Arrays.stream(nowFriendHpCombined).filter(hp -> hp > 0).count();
                friendEscaped = (int) (battle.escaped != null ? IntStream.range(0, battle.escaped.length)
                        .mapToObj(i -> battle.escaped[i]).filter(escaped -> escaped).count() : 0);
            }

            // 自艦隊の轟沈数(C) (生存艦には退避した艦も含まれていることに注意)
            int friendSunk = (numFships + numFshipsCombined) - friendNowShips;

            // 敵艦隊の戦闘終了時のHPの合計(D)
            int enemyGauge = Arrays.stream(nowEnemyHp).sum();

            // 敵艦隊の戦闘開始時のHPの合計(E)
            int enamyGaugeMax = battle.enemyGaugeMax;

            // 敵艦隊の戦闘終了後の生存数
            int enemyNowShips = (int) Arrays.stream(nowEnemyHp).filter(hp -> hp > 0).count();

            // 連合艦隊(敵艦隊)
            if (isEnemyCombined) {
                enemyGauge += Arrays.stream(nowEnemyHpCombined).sum();
                enemyNowShips += (int) Arrays.stream(nowEnemyHpCombined).filter(hp -> hp > 0).count();
            }

            // 敵艦隊の轟沈数(F)
            int enemySunk = (numEships + numEshipsCombined) - enemyNowShips;

            // 自艦隊の戦闘開始時の艦船数(G)
            int numStartFships = (numFships + numFshipsCombined) - friendEscaped;

            // 敵艦隊の戦闘開始時の艦船数(H)
            int numStartEships = numEships + numEshipsCombined;

            this.damageRate = new double[] {
                    (double) (friendGaugeMax - friendGauge) / friendGaugeMax,
                    (double) (enamyGaugeMax - enemyGauge) / enamyGaugeMax,
            };

            double friendGaugeRate = Math.floor(this.damageRate[0] * 100);
            double enemyGaugeRate = Math.floor(this.damageRate[1] * 100);

            if ((this.kind == BattlePhaseKind.LD_AIRBATTLE) ||
                    (this.kind == BattlePhaseKind.COMBINED_LD_AIR)) {
                // 空襲戦
                // S勝利は発生しないと思われる(完全勝利Sのみ)
                if (friendGaugeMax <= friendGauge) {
                    return ResultRank.PERFECT;
                }
                if (friendGaugeRate < 10) {
                    return ResultRank.A;
                }
                if (friendGaugeRate < 20) {
                    return ResultRank.B;
                }
                if (friendGaugeRate < 50) {
                    return ResultRank.C;
                }
                if (friendGaugeRate < 80) {
                    return ResultRank.D;
                }
                return ResultRank.E;
            }
            else {
                // PHASE1:轟沈艦なし かつ 敵艦全滅
                if ((friendSunk == 0) && (enemySunk == numStartEships)) {
                    // 戦闘終了時のHPが戦闘開始時のHP以上の場合、完全勝利S判定にする
                    // ソースはscenes/BattleResultMain.swfのgetTweenShowRank()参照
                    if (friendGaugeMax <= friendGauge) {
                        return ResultRank.PERFECT;
                    }
                    else {
                        return ResultRank.S;
                    }
                }
                // PHASE2:轟沈艦なし かつ 敵艦隊の戦闘開始時の数が1隻より上 かつ 敵艦の撃沈数が7割以上
                else if ((friendSunk == 0) && (numStartEships > 1)
                        && (enemySunk >= Math.floor(0.7 * numStartEships))) {
                    return ResultRank.A;
                }
                // PHASE3:自艦隊の轟沈数より敵艦隊の撃沈数の方が多い かつ 敵旗艦撃沈
                else if ((friendSunk < enemySunk) && (nowEnemyHp[0] == 0)) {
                    return ResultRank.B;
                }
                // PHASE4:自艦隊が1隻のみ かつ 自旗艦大破
                else if ((numStartFships == 1) && (((double) nowFriendHp[0] / battle.getMaxFriendHp()[0]) <= 0.25)) {
                    return ResultRank.D;
                }
                // PHASE5:敵艦隊の損害率が自艦隊の損害率を2.5倍しても上なら
                else if (enemyGaugeRate > (2.5 * friendGaugeRate)) {
                    return ResultRank.B;
                }
                // PHASE6:敵艦隊の損害率が自艦隊の損害率を0.9倍しても上なら
                else if (enemyGaugeRate > (0.9 * friendGaugeRate)) {
                    return ResultRank.C;
                }
                // PHASE7:開始時2隻以上 かつ 旗艦以外全滅
                else if ((numStartFships > 1) && ((numStartFships - 1) == friendSunk)) {
                    return ResultRank.E;
                }
                // 残りはD
                else {
                    return ResultRank.D;
                }
            }
        }

        // ダメージを反映
        private void doAtack(List<BattleAtackDto> seq, int friendSecondBase, boolean isFF, BattleExDto battle) {
            if (seq == null)
                return;

            for (BattleAtackDto dto : seq) {
                for (int i = 0; i < dto.target.length; ++i) {
                    int target = dto.target[i];
                    int damage = dto.damage[i];
                    // 自艦隊から敵艦隊への攻撃
                    if ((dto.friendAtack == true) && (isFF == false)) {
                        if (target < ENEMY_SECOND_BASE) {
                            this.nowEnemyHp[target] -= damage;
                        }
                        else {
                            this.nowEnemyHpCombined[target - ENEMY_SECOND_BASE] -= damage;
                        }
                    }
                    // 友軍艦隊から敵艦隊への攻撃
                    else if ((dto.friendAtack == true) && (isFF == true)) {
                        if (target < ENEMY_SECOND_BASE) {
                            this.nowEnemyHp[target] -= damage;
                        }
                        else {
                            this.nowEnemyHpCombined[target - ENEMY_SECOND_BASE] -= damage;
                        }
                    }
                    // 敵艦隊から自艦隊への攻撃
                    else if ((dto.friendAtack == false) && (isFF == false)) {
                        if (target < friendSecondBase) {
                            this.nowFriendHp[target] -= damage;
                            // issue#98対応
                            // Hp変更後にその値を確認し、それが0以下の場合、女神復活・ダメコン発動が
                            // 行われる可能性がある(該当アイテムを保持していた場合)ので、その処理(ア
                            // イテム発動による残Hpの変更処理)を実施する。
                            if (this.nowFriendHp[target] <= 0) {
                                DockDto dock = battle.getDock();
                                if (dock != null) {
                                    dock.getShips();
                                }
                                ShipDto ship = dock.getShips().get(target);
                                List<ItemDto> items = new ArrayList<>(ship.getItem2());
                                items.add(ship.getSlotExItem());
                                for (ItemDto item : items) {
                                    if (item == null)
                                        continue;
                                    if (item.getSlotitemId() == 42) { //応急修理要員
                                        this.nowFriendHp[target] = (int) (ship.getMaxhp() * 0.2);
                                        break;
                                    }
                                    else if (item.getSlotitemId() == 43) { //応急修理女神
                                        this.nowFriendHp[target] = ship.getMaxhp();
                                        break;
                                    }
                                }
                            }
                        }
                        else {
                            this.nowFriendHpCombined[target - friendSecondBase] -= damage;
                            if (this.nowFriendHpCombined[target - friendSecondBase] <= 0) {
                                DockDto dockCombined = battle.getDockCombined();
                                if (dockCombined != null) {
                                    dockCombined.getShips();
                                }
                                ShipDto shipCombined = dockCombined.getShips().get(target - friendSecondBase);
                                List<ItemDto> itemsCombined = new ArrayList<>(shipCombined.getItem2());
                                itemsCombined.add(shipCombined.getSlotExItem());
                                for (ItemDto itemCombined : itemsCombined) {
                                    if (itemCombined == null)
                                        continue;
                                    if (itemCombined.getSlotitemId() == 42) { //応急修理要員
                                        this.nowFriendHpCombined[target  - friendSecondBase] = (int) (shipCombined.getMaxhp() * 0.2);
                                        break;
                                    }
                                    else if (itemCombined.getSlotitemId() == 43) { //応急修理女神
                                        this.nowFriendHpCombined[target  - friendSecondBase] = shipCombined.getMaxhp();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    // 敵艦隊から友軍艦隊への攻撃
                    else if ((dto.friendAtack == false) && (isFF == true)) {
                        if (target < friendSecondBase) {
                            this.nowFriendlyFleetHp[target] -= damage;
                        }
                        else {
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

        private BattleAtackDto[] getAirBaseBattlesArray() {
            if (this.airBase == null) {
                return null;
            }
            List<BattleAtackDto> arr = new ArrayList<>();
            for (AirBattleDto dto : this.airBase) {
                if (dto.atacks != null) {
                    arr.addAll(dto.atacks);
                }
            }
            return arr.toArray(new BattleAtackDto[0]);
        }

        /**
         * 攻撃の全シーケンスを取得
         * [ 噴式基地航空隊航空戦, 基地航空隊航空戦, 噴式航空戦, 航空戦1, 支援艦隊の攻撃, 航空戦2, 開幕対潜, 開幕, 夜戦の砲撃戦1, 夜戦の砲撃戦2, 夜戦, 砲撃戦1, 雷撃, 砲撃戦2, 砲撃戦3, 友軍艦隊 ]
         * 各戦闘がない場合はnullになる
         * @return
         */
        public BattleAtackDto[][] getAtackSequence() {
            return new BattleAtackDto[][] {
                    ((this.airBaseInjection == null) || (this.airBaseInjection.atacks == null)) ? null : this
                            .toArray(this.airBaseInjection.atacks),
                    this.getAirBaseBattlesArray(),
                    ((this.airInjection == null) || (this.airInjection.atacks == null)) ? null : this
                            .toArray(this.airInjection.atacks),
                    ((this.air == null) || (this.air.atacks == null)) ? null : this.toArray(this.air.atacks),
                    this.support == null ? null : this.toArray(this.support),
                    ((this.air2 == null) || (this.air2.atacks == null)) ? null : this.toArray(this.air2.atacks),
                    this.openingTaisen == null ? null : this.toArray(this.openingTaisen),
                    this.opening == null ? null : this.toArray(this.opening),
                    this.hougeki_n_1 == null ? null : this.toArray(this.hougeki_n_1),
                    this.hougeki_n_2 == null ? null : this.toArray(this.hougeki_n_2),                   
                    this.hougeki == null ? null : this.toArray(this.hougeki),
                    this.hougeki1 == null ? null : this.toArray(this.hougeki1),
                    this.raigeki == null ? null : this.toArray(this.raigeki),
                    this.hougeki2 == null ? null : this.toArray(this.hougeki2),
                    this.hougeki3 == null ? null : this.toArray(this.hougeki3),
                    this.hougeki_f == null ? null : this.hougeki_f.stream().filter(attack -> attack.friendAtack).toArray(BattleAtackDto[]::new), 
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
         * 処理対象の艦隊は連合艦隊の第二艦隊か？
         * @return isFriendSecond
         */
        public boolean getisFriendSecond() {
            return this.isFriendSecond;
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
         * この戦闘フェーズ後の敵艦HP（敵連合艦隊の時は第一艦隊）
         * @return nowEnemyHp
         */
        public int[] getNowEnemyHp() {
            return this.nowEnemyHp;
        }

        /**
         * この戦闘フェーズ後の敵艦HP（敵連合艦隊でないときはnull）
         * @return nowEnemyHp
         */
        public int[] getNowEnemyHpCombined() {
            return this.nowEnemyHpCombined;
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
         * 敵は連合艦隊第二艦隊か？
         * @return isEnemySecond
         */
        public boolean isEnemySecond() {
            return this.isEnemySecond;
        }

        /**
         * 支援攻撃のタイプ
         * @return supportType
         */
        public String getSupportType() {
            return this.supportType;
        }

        /**
         * 友軍艦隊支援か？
         * @return isFriendFeet
         */
        public boolean isFriendFleet() {
            return this.isFriendFleet;
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
         * @return airInjection
         */
        public AirBattleDto getAirInjection() {
            return this.airInjection;
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
         * 開幕対潜
         * @return openingTaisen
         */
        public List<BattleAtackDto> getOpeningTaisen() {
            return this.openingTaisen;
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

        /**
         * @return airBaseInjection
         */
        public AirBattleDto getAirBaseInjection() {
            return this.airBaseInjection;
        }

        /**
         * @return airBase
         */
        public List<AirBattleDto> getAirBase() {
            return this.airBase;
        }

        /**
         * 友軍艦隊関連
         */

        /**
         * @return friendlyHougeki
         */
        public List<BattleAtackDto> getfriendlyHougeki() {
            return this.hougeki_f;
        }

        /**
         * @return friendlyflarePos
         */
        public int[] getfriendlyFlarePos() {
            return this.friendlyflarePos;
        }

        /**
         * 友軍艦隊 触接機 [味方・敵] -1の場合は「触接なし」
         * @return friendlytouchPlane
         */
        public int[] getfriendlyTouchPlane() {
            return this.friendlytouchPlane;
        }

        /**
         * @return nowFriendlyFleetHp
         */
        public int[] getnowFriendlyFleetHp() {
            return this.nowFriendlyFleetHp;
        }

        /**
         * @return maxFriendlyFleetHp
         */
        public int[] getmaxFriendlyFleetHp() {
            return this.maxFriendlyFleetHp;
        }

        /**
         * @return beforeFriendlyFleetHp
         */
        public int[] getbeforeFriendlyFleetHp() {
            return this.beforeFriendlyFleetHp;
        }

        /**
         * @return FriendlyFleet
         */
        public List<EnemyShipDto> getFriendlyFleet() {
            return this.ff;
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
        // 後から追加したフィールドはnullになっているので最低限のオブジェクトを作成する
        if (this.enemyCombined == null)
            this.enemyCombined = new ArrayList<>();

        if (this.exVersion >= 2) {
            // Jsonが保存してあるのはバージョン2以降
            Phase[] phaseCopy = this.phaseList.toArray(new Phase[0]);
            this.enemy.clear();
            this.enemyCombined.clear();
            this.phaseList.clear();
            boolean emulateOld = this.battleDate.before(date20170405);
            for (Phase phase : phaseCopy) {
                this.addPhase(phase.getJson(), phase.getKind(), emulateOld);
            }
            this.readResultJson(JsonUtils.fromString(this.resultJson));
        }
        else {
            // 旧バージョンのログに対応
            // ドロップの"アイテム"をdropItemNameに移動させる
            if (this.dropItem && !this.dropShip && StringUtils.isEmpty(this.dropItemName)) {
                this.dropItemName = this.dropName;
                this.dropName = "";
                this.dropType = "";
            }

            // 敵艦ID+1000前のエミュレーションをON
            for (ShipBaseDto eship : this.enemy) {
                eship.getShipInfo().setBefore20170405(true);
            }
            for (ShipBaseDto eship : this.enemyCombined) {
                eship.getShipInfo().setBefore20170405(true);
            }
        }
    }

    /**
     * 戦闘フェーズ結果を読み込む
     * @param object 受け取ったJSON
     * @param kind 戦闘の種別
     * @return 作成されたPhaseオブジェクト
     */
    public Phase addPhase(JsonObject object, BattlePhaseKind kind, boolean before20170405) {
        if (this.phaseList.size() == 0) {
            // 最初のフェーズ
            String dockId;

            if (object.containsKey("api_dock_id")) {
                dockId = object.get("api_dock_id").toString();
            }
            else {
                dockId = object.get("api_deck_id").toString();
            }

            // 新API
            JsonArray fnowhps = JsonUtils.getJsonArray(object, "api_f_nowhps");
            JsonArray enowhps = JsonUtils.getJsonArray(object, "api_e_nowhps");
            JsonArray fmaxhps = JsonUtils.getJsonArray(object, "api_f_maxhps");
            JsonArray emaxhps = JsonUtils.getJsonArray(object, "api_e_maxhps");

            // 新API予想・・・
            JsonArray fnowhpsCombined = JsonUtils.getJsonArray(object, "api_f_nowhps_combined");
            JsonArray enowhpsCombined = JsonUtils.getJsonArray(object, "api_e_nowhps_combined");
            JsonArray fmaxhpsCombined = JsonUtils.getJsonArray(object, "api_f_maxhps_combined");
            JsonArray emaxhpsCombined = JsonUtils.getJsonArray(object, "api_e_maxhps_combined");

            // 旧API
            JsonArray nowhps = JsonUtils.getJsonArray(object, "api_nowhps");
            JsonArray maxhps = JsonUtils.getJsonArray(object, "api_maxhps");
            JsonArray nowhpsCombined = JsonUtils.getJsonArray(object, "api_nowhps_combined");
            JsonArray maxhpsCombined = JsonUtils.getJsonArray(object, "api_maxhps_combined");

            boolean isFriendCombined = object.containsKey("api_fParam_combined");
            boolean isEnemyCombined = object.containsKey("api_eParam_combined");

            int numFships = 6; // 旧API用初期値
            int numFshipsCombined = 0;

            this.friendSecondBase = 6;

            int baseidx = this.baseidx = (fnowhps != null) ? 0 : 1;
            if (fnowhps != null) {
                // 新API
                numFships = fnowhps.size();
                if (isFriendCombined) {
                    numFshipsCombined = fnowhpsCombined.size();
                }
            }
            else {
                // 旧API
                for (int i = baseidx; i <= 6; ++i) {
                    if (maxhps.getInt(i) == -1) {
                        numFships = i - baseidx;
                        break;
                    }
                }
                if (isFriendCombined) {
                    numFshipsCombined = 6;
                    for (int i = this.baseidx; i <= 6; ++i) {
                        if (maxhpsCombined.getInt(i) == -1) {
                            numFshipsCombined = i - baseidx;
                            break;
                        }
                    }
                }
            }

            // 第一艦隊が6隻より大きかったらsecondBaseも大きくする
            if (this.friendSecondBase < numFships) {
                this.friendSecondBase = numFships;
            }

            if (this.friends.size() == 0) { // 再読み込みの場合はスキップ
                this.friends.add(GlobalContext.getDock(dockId));
                if (numFshipsCombined > 0) {
                    this.friends.add(GlobalContext.getDock("2"));
                }
            }

            JsonArray shipKe = JsonUtils.getJsonArray(object, "api_ship_ke");
            JsonArray eSlots = JsonUtils.getJsonArray(object, "api_eSlot");
            JsonArray eParams = JsonUtils.getJsonArray(object, "api_eParam");
            JsonArray eLevel = JsonUtils.getJsonArray(object, "api_ship_lv");
            for (int i = baseidx; i < shipKe.size(); i++) {
                int id = shipKe.getInt(i);
                if (id != -1) {
                    if (before20170405) {
                        id += 1000;
                    }
                    int[] slot = JsonUtils.toIntArray(eSlots.getJsonArray(i - baseidx));
                    int[] param = JsonUtils.toIntArray(eParams.getJsonArray(i - baseidx));
                    this.enemy.add(new EnemyShipDto(id, slot, param, eLevel.getInt(i)));
                }
            }
            if (isEnemyCombined) {
                JsonArray shipKeCombined = JsonUtils.getJsonArray(object, "api_ship_ke_combined");
                JsonArray eSlotsCombined = JsonUtils.getJsonArray(object, "api_eSlot_combined");
                JsonArray eParamsCombined = JsonUtils.getJsonArray(object, "api_eParam_combined");
                JsonArray eLevelCombined = JsonUtils.getJsonArray(object, "api_ship_lv_combined");
                for (int i = baseidx; i < shipKeCombined.size(); i++) {
                    int id = shipKeCombined.getInt(i);
                    if (id != -1) {
                        if (before20170405) {
                            id += 1000;
                        }
                        int[] slot = JsonUtils.toIntArray(JsonUtils.getJsonArray(eSlotsCombined, i - baseidx));
                        int[] param = JsonUtils.toIntArray(JsonUtils.getJsonArray(eParamsCombined, i - baseidx));
                        this.enemyCombined.add(new EnemyShipDto(id, slot, param, eLevelCombined.getInt(i)));
                    }
                }
            }
            int numEships = this.enemy.size();
            int numEshipsCombined = this.enemyCombined.size();

            this.startFriendHp = new int[numFships];
            this.startEnemyHp = new int[numEships];
            this.maxFriendHp = new int[numFships];
            this.maxEnemyHp = new int[numEships];
            if (isFriendCombined) {
                this.startFriendHpCombined = new int[numFshipsCombined];
                this.maxFriendHpCombined = new int[numFshipsCombined];
            }
            else {
                this.maxFriendHpCombined = null;
            }
            if (isEnemyCombined) {
                this.startEnemyHpCombined = new int[numEshipsCombined];
                this.maxEnemyHpCombined = new int[numEshipsCombined];
            }
            else {
                this.maxEnemyHpCombined = null;
            }

            // 陣形
            if (object.containsKey("api_formation")) {
                JsonArray formation = JsonUtils.getJsonArray(object, "api_formation");
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
            JsonArray jsonSearch = JsonUtils.getJsonArray(object, "api_search");
            if (jsonSearch != null) {
                this.sakuteki = new String[] {
                        toSearch(jsonSearch.getInt(0)),
                        toSearch(jsonSearch.getInt(1))
                };
            }

            // この戦闘の開始前HPを取得
            if (fnowhps != null) {
                // 新API
                for (int i = 0; i < numFships; i++) {
                    this.maxFriendHp[i] = fmaxhps.getInt(i);
                    this.friendGaugeMax += this.startFriendHp[i] = fnowhps.getInt(i);
                }
                for (int i = 0; i < numEships; i++) {
                    this.maxEnemyHp[i] = emaxhps.getInt(i);
                    this.enemyGaugeMax += this.startEnemyHp[i] = enowhps.getInt(i);
                }
                for (int i = 0; i < numFshipsCombined; i++) {
                    this.maxFriendHpCombined[i] = fmaxhpsCombined.getInt(i);
                    this.friendGaugeMax += this.startFriendHpCombined[i] = fnowhpsCombined.getInt(i);
                }
                for (int i = 0; i < numEshipsCombined; i++) {
                    this.maxEnemyHpCombined[i] = emaxhpsCombined.getInt(i);
                    this.enemyGaugeMax += this.startEnemyHpCombined[i] = enowhpsCombined.getInt(i);
                }
            }
            else {
                // 旧API
                for (int i = baseidx; i < nowhps.size(); i++) {
                    int hp = nowhps.getInt(i);
                    int maxHp = maxhps.getInt(i);
                    if (i <= 6) {
                        if (i <= numFships) {
                            this.maxFriendHp[i - baseidx] = maxHp;
                            this.friendGaugeMax += this.startFriendHp[i - baseidx] = hp;
                        }
                    }
                    else {
                        if ((i - 6) <= numEships) {
                            this.maxEnemyHp[i - baseidx - 6] = maxHp;
                            this.enemyGaugeMax += this.startEnemyHp[i - baseidx - 6] = hp;
                        }
                    }
                }
                if (isFriendCombined || isEnemyCombined) {
                    for (int i = baseidx; i < nowhpsCombined.size(); i++) {
                        int hp = nowhpsCombined.getInt(i);
                        int maxHp = maxhpsCombined.getInt(i);
                        if (i <= 6) {
                            if (i <= numFshipsCombined) {
                                this.maxFriendHpCombined[i - baseidx] = maxHp;
                                this.friendGaugeMax += this.startFriendHpCombined[i - baseidx] = hp;
                            }
                        }
                        else {
                            if ((i - 6) <= numEshipsCombined) {
                                this.maxEnemyHpCombined[i - baseidx - 6] = maxHp;
                                this.enemyGaugeMax += this.startEnemyHpCombined[i - baseidx - 6] = hp;
                            }
                        }
                    }
                }
            }

            if (isFriendCombined) {
                // 退避（連合艦隊は今の所最大が6+6=12隻なのでそれで固定）
                this.escaped = new boolean[12];
                // 退避艦は1始まりの模様
                if (JsonUtils.hasKey(object, "api_escape_idx")) {
                    for (JsonValue jsonShip : JsonUtils.getJsonArray(object, "api_escape_idx")) {
                        this.escaped[((JsonNumber) jsonShip).intValue() - 1] = true;
                    }
                }
                if (JsonUtils.hasKey(object, "api_escape_idx_combined")) {
                    for (JsonValue jsonShip : JsonUtils.getJsonArray(object, "api_escape_idx_combined")) {
                        this.escaped[(((JsonNumber) jsonShip).intValue() - 1) + 6] = true;
                    }
                }
                for (int i = 0; i < 2; ++i) {
                    DockDto dock = this.friends.get(i);
                    if (dock != null) {
                        dock.setEscaped(Arrays.copyOfRange(this.escaped, i * 6, (i + 1) * 6));
                    }
                }
            }
            else {
                // 退避（遊撃部隊は今の所最大が7隻なのでそれで固定）
                this.escaped = new boolean[7];
                // 退避艦は1始まりの模様
                if (JsonUtils.hasKey(object, "api_escape_idx")) {
                    for (JsonValue jsonShip : JsonUtils.getJsonArray(object, "api_escape_idx")) {
                        this.escaped[((JsonNumber) jsonShip).intValue() - 1] = true;
                    }
                }
                for (int i = 0; i < 1; ++i) {
                    DockDto dock = this.friends.get(i);
                    if (dock != null) {
                        dock.setEscaped(Arrays.copyOfRange(this.escaped, i * 7, (i + 1) * 7));
                    }
                }
            }
        }

        if (this.phaseList.size() > 0) {
            Phase phase = this.phaseList.get(0);
            this.completeDamageAndAddPhase(
                    new Phase(this, object, kind,
                            phase.getNowFriendHp(), phase.getNowFriendHpCombined(),
                            phase.getNowEnemyHp(), phase.getNowEnemyHpCombined()),
                    kind);
        }
        else {
            this.completeDamageAndAddPhase(new Phase(this, object, kind,
                    this.startFriendHp, this.startFriendHpCombined,
                    this.startEnemyHp, this.startEnemyHpCombined), kind);
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
        if (this.dropShip) {
            JsonObject getShip = object.getJsonObject("api_get_ship");
            this.dropShipId = getShip.getInt("api_ship_id");
            this.dropType = getShip.getString("api_ship_type");
            this.dropName = getShip.getString("api_ship_name");
        }
        else {
            this.dropType = "";
            this.dropName = "";
        }
        if (this.dropItem) {
            String name = UseItem.get(object.getJsonObject("api_get_useitem").getInt("api_useitem_id"));
            this.dropItemName = StringUtils.defaultString(name);
        }
        else {
            this.dropItemName = "";
        }
        this.mvp = object.getInt("api_mvp");
        if (JsonUtils.hasKey(object, "api_mvp_combined")) {
            this.mvpCombined = object.getInt("api_mvp_combined");
        }
        this.hqLv = object.getInt("api_member_lv");
        if (JsonUtils.hasKey(object, "api_escape")) {
            JsonObject jsonEscape = object.getJsonObject("api_escape");
            if (JsonUtils.hasKey(jsonEscape, "api_tow_idx")) {
                // 退避艦は1始まりの模様
                // 連合艦隊 護衛退避では退避艦と随行艦の情報
                this.escapeInfo = new int[] {
                    JsonUtils.getJsonArray(jsonEscape, "api_escape_idx").getInt(0) - 1,
                    JsonUtils.getJsonArray(jsonEscape, "api_tow_idx").getInt(0) - 1
                };
            }
            else {
                // 遊撃部隊 司令部施設の場合、退避艦１隻のみの情報となる
                this.escapeInfo = new int[] {
                    JsonUtils.getJsonArray(jsonEscape, "api_escape_idx").getInt(0) - 1
                };
            }
        }
        if (JsonUtils.hasKey(object, "api_lost_flag")) {
            this.lostflag = new boolean[AppConstants.MAXCHARA];
            JsonArray jsonLostflag = JsonUtils.getJsonArray(object, "api_lost_flag");
            for (int i = this.baseidx; i < jsonLostflag.size(); i++) {
                this.lostflag[i - this.baseidx] = (jsonLostflag.getInt(i) != 0);
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
        case 6:
            formation = "警戒陣";
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
            formation = "不明(" + f + ")";
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
     * 味方は連合艦隊か？
     * @return
     */
    public boolean isCombined() {
        return (this.startFriendHpCombined != null);
    }

    /**
     * 敵は連合艦隊か？
     * @return
     */
    public boolean isEnemyCombined() {
        return (this.startEnemyHpCombined != null);
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
     * 交戦後の敵艦HP 連合艦隊第二艦隊
     * @return
     */
    public int[] getNowEnemyHpCombined() {
        return this.getLastPhase().getNowEnemyHpCombined();
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
     * 敵艦 連合艦隊第二艦隊
     * @return enemy
     */
    public List<EnemyShipDto> getEnemyCombined() {
        return this.enemyCombined;
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
     * 敵連合艦隊第二艦隊のMaxHP
     * @return maxEnemyHpCombined
     */
    public int[] getMaxEnemyHpCombined() {
        return this.maxEnemyHpCombined;
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
     * 敵艦の戦闘開始時HP 連合艦隊第二艦隊
     * @return startEnemyHpCombined
     */
    public int[] getStartEnemyHpCombined() {
        return this.startEnemyHpCombined;
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
     * ドロップ艦の名前
     * @return dropName
     */
    public String getDropName() {
        return this.dropName;
    }

    /**
     * ドロップアイテムの名前
     * @return dropItemName
     */
    public String getDropItemName() {
        return this.dropItemName;
    }

    /**
     * 戦闘フェーズ（昼戦・夜戦）リスト
     * @return phaseList
     */
    public List<Phase> getPhaseList() {
        return this.phaseList;
    }

    /**
     * MVP艦が何番目の艦か (1～)
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

    /**
     * 連合艦隊の種類を取得します
     * @return 連合艦隊の種類(0:未結成、1:機動部隊、2:水上部隊、3:輸送部隊、-x:強制解隊)
     */
    public int getCombinedKind() {
        return this.combinedKind;
    }

    public void setCombinedKind(int combinedKind) {
        this.combinedKind = combinedKind;
    }

    /**
     * 味方第一艦隊の最大隻数＝第2艦隊のベースインデックス
     * @return numFirstMax
     */
    public int getFriendSecondBase() {
        if (this.friendSecondBase == 0) {
            // 旧データ対応
            return 6;
        }
        return this.friendSecondBase;
    }

    /**
     * 敵第一艦隊の最大隻数＝第2艦隊のベースインデックス
     * @return numFirstMax
     */
    public int getEnemySecondBase() {
        return ENEMY_SECOND_BASE;
    }
}