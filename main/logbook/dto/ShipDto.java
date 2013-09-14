/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.data.context.GlobalContext;
import logbook.internal.Ship;

/**
 * 艦娘を表します
 *
 */
public final class ShipDto extends AbstractDto {

    /** 艦娘個人を識別するID */
    private final long id;

    /** 艦隊ID */
    private String fleetid = "";

    /** 名前 */
    private final String name;

    /** Lv */
    private final long lv;

    /** 疲労 */
    private final long cond;

    /** 経験値 */
    private final long exp;

    /** HP */
    private final long nowhp;

    /** MaxHP */
    private final long maxhp;

    /** 装備 */
    private final List<String> slot;

    /** 火力 */
    private final long karyoku;

    /** 雷装 */
    private final long raisou;

    /** 対空 */
    private final long taiku;

    /** 装甲 */
    private final long soukou;

    /** 回避 */
    private final long kaihi;

    /** 対潜 */
    private final long taisen;

    /** 索敵 */
    private final long sakuteki;

    /** 運 */
    private final long lucky;

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     */
    public ShipDto(JsonObject object) {

        this.id = object.getJsonNumber("api_id").longValue();
        this.name = Ship.get(object.getJsonNumber("api_ship_id").toString());
        this.lv = object.getJsonNumber("api_lv").longValue();
        this.cond = object.getJsonNumber("api_cond").longValue();
        this.exp = object.getJsonNumber("api_exp").longValue();
        this.nowhp = object.getJsonNumber("api_nowhp").longValue();
        this.maxhp = object.getJsonNumber("api_maxhp").longValue();
        this.slot = new ArrayList<String>();
        JsonArray array = object.getJsonArray("api_slot");
        for (JsonValue jsonValue : array) {
            JsonNumber itemid = (JsonNumber) jsonValue;
            this.slot.add(Long.toString(itemid.longValue()));
        }
        this.karyoku = ((JsonNumber) object.getJsonArray("api_karyoku").get(0)).longValue();
        this.raisou = ((JsonNumber) object.getJsonArray("api_raisou").get(0)).longValue();
        this.taiku = ((JsonNumber) object.getJsonArray("api_taiku").get(0)).longValue();
        this.soukou = ((JsonNumber) object.getJsonArray("api_soukou").get(0)).longValue();
        this.kaihi = ((JsonNumber) object.getJsonArray("api_kaihi").get(0)).longValue();
        this.taisen = ((JsonNumber) object.getJsonArray("api_taisen").get(0)).longValue();
        this.sakuteki = ((JsonNumber) object.getJsonArray("api_sakuteki").get(0)).longValue();
        this.lucky = ((JsonNumber) object.getJsonArray("api_lucky").get(0)).longValue();
    }

    /**
     * @return 艦娘個人を識別するID
     */
    public long getId() {
        return this.id;
    }

    /**
     * @return 艦隊ID
     */
    public String getFleetid() {
        return this.fleetid;
    }

    /**
     * 艦隊IDを設定する
     * 
     * @param fleetid 艦隊ID
     */
    public void setFleetid(String fleetid) {
        this.fleetid = fleetid;
    }

    /**
     * @return 名前
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return Lv
     */
    public long getLv() {
        return this.lv;
    }

    /**
     * @return 疲労
     */
    public long getCond() {
        return this.cond;
    }

    /**
     * @return 経験値
     */
    public long getExp() {
        return this.exp;
    }

    /**
     * @return HP
     */
    public long getNowhp() {
        return this.nowhp;
    }

    /**
     * @return MaxHP
     */
    public long getMaxhp() {
        return this.maxhp;
    }

    /**
     * @return 装備
     */
    public List<String> getSlot() {
        List<String> itemNames = new ArrayList<String>();
        Map<String, ItemDto> itemMap = GlobalContext.getItemMap();
        for (String itemid : this.slot) {
            if (!"-1".equals(itemid)) {
                ItemDto name = itemMap.get(itemid);
                if (name != null) {
                    itemNames.add(name.getName());
                } else {
                    itemNames.add("<UNKNOWN>");
                }
            } else {
                itemNames.add("");
            }
        }
        return itemNames;
    }

    /**
     * @return 火力
     */
    public long getKaryoku() {
        return this.karyoku;
    }

    /**
     * @return 雷装
     */
    public long getRaisou() {
        return this.raisou;
    }

    /**
     * @return 対空
     */
    public long getTaiku() {
        return this.taiku;
    }

    /**
     * @return 装甲
     */
    public long getSoukou() {
        return this.soukou;
    }

    /**
     * @return 回避
     */
    public long getKaihi() {
        return this.kaihi;
    }

    /**
     * @return 対潜
     */
    public long getTaisen() {
        return this.taisen;
    }

    /**
     * @return 索敵
     */
    public long getSakuteki() {
        return this.sakuteki;
    }

    /**
     * @return 運
     */
    public long getLucky() {
        return this.lucky;
    }

}
