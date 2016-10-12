package logbook.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * データが何を示すのかを列挙する
 *
 */
public enum DataType {

    /** 補給 */
    CHARGE("/kcsapi/api_req_hokyu/charge"),
    /** 編成 */
    CHANGE("/kcsapi/api_req_hensei/change"),
    /** プリセット展開 */
    PRESET_SELECT("/kcsapi/api_req_hensei/preset_select"),
    /** 母港 */
    PORT("/kcsapi/api_port/port"),
    /** 保有艦 */
    SHIP2("/kcsapi/api_get_member/ship2"),
    /** 保有艦 */
    SHIP3("/kcsapi/api_get_member/ship3"),
    /** 出撃中の更新 */
    SHIP_DECK("/kcsapi/api_get_member/ship_deck"),
    /** 遠征(帰還) */
    MISSION_RESULT("/kcsapi/api_req_mission/result"),
    /** 基本 */
    BASIC("/kcsapi/api_get_member/basic"),
    /** 資材 */
    MATERIAL("/kcsapi/api_get_member/material"),
    /** 入渠ドック */
    NDOCK("/kcsapi/api_get_member/ndock"),
    /** アイテム一覧 */
    SLOTITEM_MEMBER("/kcsapi/api_get_member/slot_item"),
    /** 艦隊 */
    DECK("/kcsapi/api_get_member/deck"),
    /** 戦闘 */
    BATTLE("/kcsapi/api_req_sortie/battle"),
    /** 戦闘(夜戦) */
    BATTLE_MIDNIGHT("/kcsapi/api_req_battle_midnight/battle"),
    /** 戦闘(夜戦) */
    BATTLE_SP_MIDNIGHT("/kcsapi/api_req_battle_midnight/sp_midnight"),
    /** 戦闘(夜戦→昼戦) */
    BATTLE_NIGHT_TO_DAY("/kcsapi/api_req_sortie/night_to_day"),
    /** 戦闘(航空戦) */
    AIR_BATTLE("/kcsapi/api_req_sortie/airbattle"),
    /** 戦闘(長距離空襲戦) */
    LD_AIRBATTLE("/kcsapi/api_req_sortie/ld_airbattle"),
    /** 戦闘(航空戦) */
    COMBINED_AIR_BATTLE("/kcsapi/api_req_combined_battle/airbattle"),
    /** 戦闘(長距離空襲戦) */
    COMBINED_LD_AIRBATTLE("/kcsapi/api_req_combined_battle/ld_airbattle"),
    /** 戦闘 */
    COMBINED_BATTLE("/kcsapi/api_req_combined_battle/battle"),
    /** 戦闘 */
    COMBINED_BATTLE_MIDNIGHT("/kcsapi/api_req_combined_battle/midnight_battle"),
    /** 戦闘 */
    COMBINED_BATTLE_SP_MIDNIGHT("/kcsapi/api_req_combined_battle/sp_midnight"),
    /** 戦闘 */
    COMBINED_BATTLE_WATER("/kcsapi/api_req_combined_battle/battle_water"),
    /** 戦闘(敵連合艦隊) */
    COMBINED_EC_BATTLE("/kcsapi/api_req_combined_battle/ec_battle"),
    /** 戦闘(夜戦 敵連合艦隊) */
    COMBINED_EC_BATTLE_MIDNIGHT("/kcsapi/api_req_combined_battle/ec_midnight_battle"),
    /** 戦闘結果 */
    BATTLE_RESULT("/kcsapi/api_req_sortie/battleresult"),
    /** 戦闘結果(連合艦隊) */
    COMBINED_BATTLE_RESULT("/kcsapi/api_req_combined_battle/battleresult"),
    /** 退避 */
    COMBINED_BATTLE_GOBACK_PORT("/kcsapi/api_req_combined_battle/goback_port"),
    /** 開発 */
    CREATE_ITEM("/kcsapi/api_req_kousyou/createitem"),
    /** 建造 */
    CREATE_SHIP("/kcsapi/api_req_kousyou/createship"),
    /** 建造ドック */
    KDOCK("/kcsapi/api_get_member/kdock"),
    /** 建造(入手) */
    GET_SHIP("/kcsapi/api_req_kousyou/getship"),
    /** 解体 */
    DESTROY_SHIP("/kcsapi/api_req_kousyou/destroyship"),
    /** 廃棄 */
    DESTROY_ITEM2("/kcsapi/api_req_kousyou/destroyitem2"),
    /** 近代化改修 */
    POWERUP("/kcsapi/api_req_kaisou/powerup"),
    /** 装備位置交換 */
    SLOT_EXCHANGE_INDEX("/kcsapi/api_req_kaisou/slot_exchange_index"),
    /** 艦娘のロック操作 */
    LOCK_SHIP("/kcsapi/api_req_hensei/lock"),
    /** 装備ロック操作 */
    LOCK_SLOTITEM("/kcsapi/api_req_kaisou/lock"),
    /** 装備改修 */
    REMODEL_SLOT("/kcsapi/api_req_kousyou/remodel_slot"),
    /** 出撃 */
    START("/kcsapi/api_req_map/start"),
    /** 進撃 */
    NEXT("/kcsapi/api_req_map/next"),
    /** 出撃 */
    START_AIR_BASE("/kcsapi/api_req_map/start_air_base"),
    /** 任務一覧 */
    QUEST_LIST("/kcsapi/api_get_member/questlist"),
    /** 任務消化 */
    QUEST_CLEAR("/kcsapi/api_req_quest/clearitemget"),
    /** 設定 */
    START2("/kcsapi/api_start2"),
    /** マップ情報 */
    MAPINFO("/kcsapi/api_get_member/mapinfo"),
    /** 遠征情報 */
    MISSION("/kcsapi/api_get_member/mission"),
    /** 演習情報 */
    PRACTICE("/kcsapi/api_get_member/practice"),
    /** 演習情報 */
    PRACTICE_ENEMYINFO("/kcsapi/api_req_member/get_practice_enemyinfo"),
    /** 戦闘 */
    PRACTICE_BATTLE("/kcsapi/api_req_practice/battle"),
    /** 戦闘(夜戦) */
    PRACTICE_BATTLE_MIDNIGHT("/kcsapi/api_req_practice/midnight_battle"),
    /** 戦闘結果 */
    PRACTICE_BATTLE_RESULT("/kcsapi/api_req_practice/battle_result"),
    /** 連合艦隊操作 */
    COMBINED("/kcsapi/api_req_hensei/combined"),
    /** 入渠開始 */
    NYUKYO_START("/kcsapi/api_req_nyukyo/start"),
    /** 高速修復 */
    NYUKYO_SPEEDCHANGE("/kcsapi/api_req_nyukyo/speedchange"),
    /** 改造 */
    REMODELING("/kcsapi/api_req_kaisou/remodeling"),
    /** 疲労度回復アイテム使用 */
    ITEMUSE_COND("/kcsapi/api_req_member/itemuse_cond"),
    /** ログイン時の情報 */
    REQUIRE_INFO("/kcsapi/api_get_member/require_info"),
    /** フィルタ前のデータ */
    UNDEFINED(null);

    public static final Map<String, DataType> TYPEMAP;

    static {
        TYPEMAP = new ConcurrentHashMap<>();
        for (DataType type : values()) {
            if (type.getUrl() != null) {
                TYPEMAP.put(type.getUrl(), type);
            }
        }
    }

    private final String url;

    private DataType(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public String getApiName() {
        return this.url.substring("/kcsapi/".length());
    }
}
