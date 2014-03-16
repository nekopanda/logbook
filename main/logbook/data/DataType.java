/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.data;

/**
 * データが何を示すのかを列挙する
 *
 */
public enum DataType {

    /** 保有艦 */
    SHIP2("/kcsapi/api_get_member/ship2"),
    /** 保有艦 */
    SHIP3("/kcsapi/api_get_member/ship3"),
    /** 遠征 */
    DECK_PORT("/kcsapi/api_get_member/deck_port"),
    /** 遠征(帰還) */
    MISSION_RESULT("/kcsapi/api_req_mission/result"),
    /** 基本 */
    BASIC("/kcsapi/api_get_member/basic"),
    /** 資材 */
    MATERIAL("/kcsapi/api_get_member/material"),
    /** 入渠ドック */
    NDOCK("/kcsapi/api_get_member/ndock"),
    /** アイテム一覧 */
    SLOTITEM_MEMBER("/kcsapi/api_get_member/slotitem"),
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
    /** 戦闘結果 */
    BATTLERESULT("/kcsapi/api_req_sortie/battleresult"),
    /** 開発 */
    CREATEITEM("/kcsapi/api_req_kousyou/createitem"),
    /** 建造 */
    CREATESHIP("/kcsapi/api_req_kousyou/createship"),
    /** 建造ドック */
    KDOCK("/kcsapi/api_get_member/kdock"),
    /** 建造(入手) */
    GETSHIP("/kcsapi/api_req_kousyou/getship"),
    /** 出撃 */
    START("kcsapi/api_req_map/start"),
    /** 任務一覧 */
    QUEST_LIST("/kcsapi/api_get_member/questlist"),
    /** 任務消化 */
    QUEST_CLEAR("/kcsapi/api_req_quest/clearitemget"),
    /** アイテム一覧 */
    SLOTITEM_MASTER("/kcsapi/api_get_master/slotitem"),
    /** 艦娘一覧 */
    SHIP_MASTER("/kcsapi/api_get_master/ship"),
    /** フィルタ前のデータ */
    UNDEFINED(null);

    private final String url;

    private DataType(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }
}
