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
    /** 遠征 */
    DECK_PORT("/kcsapi/api_get_member/deck_port"),
    /** 基本 */
    BASIC("/kcsapi/api_get_member/basic"),
    /** 入渠 */
    NDOCK("/kcsapi/api_get_member/ndock"),
    /** アイテム一覧 */
    SLOTITEM_MEMBER("/kcsapi/api_get_member/slotitem"),
    /** アイテム一覧 */
    SLOTITEM_MASTER("/kcsapi/api_get_master/slotitem"),
    /** 戦闘 */
    BATTLE("/kcsapi/api_req_sortie/battle"),
    /** 戦闘結果 */
    BATTLERESULT("/kcsapi/api_req_sortie/battleresult"),
    /** 開発 */
    CREATEITEM("/kcsapi/api_req_kousyou/createitem"),
    /** 建造 */
    CREATESHIP("/kcsapi/api_req_kousyou/createship"),
    /** 建造(入手) */
    GETSHIP("/kcsapi/api_req_kousyou/getship"),
    /** 艦隊 */
    DECK("/kcsapi/api_get_member/deck"),
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
