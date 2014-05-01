/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.dto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.internal.ExpTable;
import logbook.internal.Ship;

/**
 * 艦娘を表します
 *
 */
public final class ShipDto extends AbstractDto {

    /** 日時 */
    private final Calendar time = Calendar.getInstance();

    /** 艦娘個人を識別するID */
    private final long id;

    /** 鍵付き */
    private final boolean locked;

    /** 艦隊ID */
    private String fleetid = "";

    /** 名前 */
    private final String name;

    /** 艦種 */
    private final String type;

    /** Lv */
    private final long lv;

    /** 疲労 */
    private final long cond;

    /** 入渠時間 */
    private final long docktime;

    /** 修復資材 燃料 */
    private final long dockfuel;

    /** 修復資材 鋼材 */
    private final long dockmetal;

    /** 残弾 */
    private int bull;

    /** 弾Max */
    private final int bullmax;

    /** 残燃料 */
    private int fuel;

    /** 燃料Max */
    private final int fuelmax;

    /** 経験値 */
    private final long exp;

    /** HP */
    private final long nowhp;

    /** MaxHP */
    private final long maxhp;

    /** 装備 */
    private final List<Long> slot;

    /** 装備の搭載数 */
    private final List<Integer> onslot;

    /** 火力 */
    private final long karyoku;

    /** 火力(最大) */
    private final long karyokuMax;

    /** 雷装 */
    private final long raisou;

    /** 雷装(最大) */
    private final long raisouMax;

    /** 対空 */
    private final long taiku;

    /** 対空(最大) */
    private final long taikuMax;

    /** 装甲 */
    private final long soukou;

    /** 装甲(最大) */
    private final long soukouMax;

    /** 回避 */
    private final long kaihi;

    /** 回避(最大) */
    private final long kaihiMax;

    /** 対潜 */
    private final long taisen;

    /** 対潜(最大) */
    private final long taisenMax;

    /** 索敵 */
    private final long sakuteki;

    /** 索敵(最大) */
    private final long sakutekiMax;

    /** 運 */
    private final long lucky;

    /** 運(最大) */
    private final long luckyMax;

    /** 艦娘 */
    private final ShipInfoDto shipInfo;

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     */
    public ShipDto(JsonObject object) {

        this.id = object.getJsonNumber("api_id").longValue();
        this.locked = object.getJsonNumber("api_locked").longValue() == 1;

        ShipInfoDto shipinfo = Ship.get(object.getJsonNumber("api_ship_id").toString());
        this.shipInfo = shipinfo;
        this.name = shipinfo.getName();
        this.type = shipinfo.getType();

        this.lv = object.getJsonNumber("api_lv").longValue();
        this.cond = object.getJsonNumber("api_cond").longValue();

        this.docktime = object.getJsonNumber("api_ndock_time").longValue();
        this.dockfuel = object.getJsonArray("api_ndock_item").getJsonNumber(0).longValue();
        this.dockmetal = object.getJsonArray("api_ndock_item").getJsonNumber(1).longValue();

        this.bull = object.getJsonNumber("api_bull").intValue();
        this.fuel = object.getJsonNumber("api_fuel").intValue();
        this.bullmax = shipinfo.getMaxBull();
        this.fuelmax = shipinfo.getMaxFuel();

        this.exp = object.getJsonArray("api_exp").getJsonNumber(0).longValue();
        this.nowhp = object.getJsonNumber("api_nowhp").longValue();
        this.maxhp = object.getJsonNumber("api_maxhp").longValue();
        this.slot = new ArrayList<Long>();
        JsonArray slot = object.getJsonArray("api_slot");
        for (JsonValue jsonValue : slot) {
            JsonNumber itemid = (JsonNumber) jsonValue;
            this.slot.add(Long.valueOf(itemid.longValue()));
        }
        this.onslot = new ArrayList<Integer>();
        JsonArray onslot = object.getJsonArray("api_onslot");
        for (JsonValue jsonValue : onslot) {
            JsonNumber itemid = (JsonNumber) jsonValue;
            this.onslot.add(Integer.valueOf(itemid.intValue()));
        }
        this.karyoku = ((JsonNumber) object.getJsonArray("api_karyoku").get(0)).longValue();
        this.karyokuMax = ((JsonNumber) object.getJsonArray("api_karyoku").get(1)).longValue();
        this.raisou = ((JsonNumber) object.getJsonArray("api_raisou").get(0)).longValue();
        this.raisouMax = ((JsonNumber) object.getJsonArray("api_raisou").get(1)).longValue();
        this.taiku = ((JsonNumber) object.getJsonArray("api_taiku").get(0)).longValue();
        this.taikuMax = ((JsonNumber) object.getJsonArray("api_taiku").get(1)).longValue();
        this.soukou = ((JsonNumber) object.getJsonArray("api_soukou").get(0)).longValue();
        this.soukouMax = ((JsonNumber) object.getJsonArray("api_soukou").get(1)).longValue();
        this.kaihi = ((JsonNumber) object.getJsonArray("api_kaihi").get(0)).longValue();
        this.kaihiMax = ((JsonNumber) object.getJsonArray("api_kaihi").get(1)).longValue();
        this.taisen = ((JsonNumber) object.getJsonArray("api_taisen").get(0)).longValue();
        this.taisenMax = ((JsonNumber) object.getJsonArray("api_taisen").get(1)).longValue();
        this.sakuteki = ((JsonNumber) object.getJsonArray("api_sakuteki").get(0)).longValue();
        this.sakutekiMax = ((JsonNumber) object.getJsonArray("api_sakuteki").get(1)).longValue();
        this.lucky = ((JsonNumber) object.getJsonArray("api_lucky").get(0)).longValue();
        this.luckyMax = ((JsonNumber) object.getJsonArray("api_lucky").get(1)).longValue();
        // 疲労が抜ける時間を計算する
        if (this.cond < 49) {
            this.time.add(Calendar.MINUTE, Math.max(49 - (int) this.cond, 3));
        }
    }

    /**
     * @return 艦娘個人を識別するID
     */
    public long getId() {
        return this.id;
    }

    /**
     * @return 鍵付き
     */
    public boolean getLocked() {
        return this.locked;
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
     * @return 艦種
     */
    public String getType() {
        return this.type;
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
     * @return 入渠時間
     */
    public long getDocktime() {
        return this.docktime;
    }

    /**
     * @return 修復資材 燃料
     */
    public long getDockfuel() {
        return this.dockfuel;
    }

    /**
     * @return 修復資材 鋼材
     */
    public long getDockmetal() {
        return this.dockmetal;
    }

    /**
     * @return 弾
     */
    public int getBull() {
        return this.bull;
    }

    /**
     * @return 弾Max
     */
    public int getBullMax() {
        return this.bullmax;
    }

    /**
     * @param bull 残弾
     */
    public void setBull(int bull) {
        this.bull = bull;
    }

    /**
     * @return 燃料
     */
    public int getFuel() {
        return this.fuel;
    }

    /**
     * @return 燃料Max
     */
    public int getFuelMax() {
        return this.fuelmax;
    }

    /**
     * @param fuel 残燃料
     */
    public void setFuel(int fuel) {
        this.fuel = fuel;
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
        Map<Long, ItemDto> itemMap = GlobalContext.getItemMap();
        for (Long itemid : this.slot) {
            if (-1 != itemid) {
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
     * @return 装備
     */
    public List<ItemDto> getItem() {
        List<ItemDto> items = new ArrayList<ItemDto>();
        Map<Long, ItemDto> itemMap = GlobalContext.getItemMap();
        for (Long itemid : this.slot) {
            if (-1 != itemid) {
                ItemDto item = itemMap.get(itemid);
                if (item != null) {
                    items.add(item);
                } else {
                    items.add(null);
                }
            } else {
                items.add(null);
            }
        }
        return items;
    }

    /**
     * @return 装備ID
     */
    public List<Long> getItemId() {
        return Collections.unmodifiableList(this.slot);
    }

    /**
     * @return 制空値
     */
    public int getSeiku() {
        List<ItemDto> items = this.getItem();
        int seiku = 0;
        for (int i = 0; i < 4; i++) {
            ItemDto item = items.get(i);
            if (item != null) {
                if ("6".equals(item.getTypeId3())
                        || "7".equals(item.getTypeId3())
                        || "8".equals(item.getTypeId3())
                        || ("10".equals(item.getTypeId3()) && "11".equals(item.getTypeId2()))) {
                    //6:艦上戦闘機,7:艦上爆撃機,8:艦上攻撃機,10:水上偵察機(ただし瑞雲のみ)の場合は制空値を計算する
                    seiku += (int) Math.floor(item.getTyku() * Math.sqrt(this.onslot.get(i)));
                }
            }
        }
        return seiku;
    }

    /**
     * @return 火力
     */
    public long getKaryoku() {
        return this.karyoku;
    }

    /**
     * @return 火力(最大)
     */
    public long getKaryokuMax() {
        return this.karyokuMax;
    }

    /**
     * @return 雷装
     */
    public long getRaisou() {
        return this.raisou;
    }

    /**
     * @return 雷装(最大)
     */
    public long getRaisouMax() {
        return this.raisouMax;
    }

    /**
     * @return 対空
     */
    public long getTaiku() {
        return this.taiku;
    }

    /**
     * @return 対空(最大)
     */
    public long getTaikuMax() {
        return this.taikuMax;
    }

    /**
     * @return 装甲
     */
    public long getSoukou() {
        return this.soukou;
    }

    /**
     * @return 装甲(最大)
     */
    public long getSoukouMax() {
        return this.soukouMax;
    }

    /**
     * @return 回避
     */
    public long getKaihi() {
        return this.kaihi;
    }

    /**
     * @return 回避(最大)
     */
    public long getKaihiMax() {
        return this.kaihiMax;
    }

    /**
     * @return 対潜
     */
    public long getTaisen() {
        return this.taisen;
    }

    /**
     * @return 対潜(最大)
     */
    public long getTaisenMax() {
        return this.taisenMax;
    }

    /**
     * @return 索敵
     */
    public long getSakuteki() {
        return this.sakuteki;
    }

    /**
     * @return 索敵(最大)
     */
    public long getSakutekiMax() {
        return this.sakutekiMax;
    }

    /**
     * @return 運
     */
    public long getLucky() {
        return this.lucky;
    }

    /**
     * @return 運(最大)
     */
    public long getLuckyMax() {
        return this.luckyMax;
    }

    /**
     * @return 艦娘
     */
    public ShipInfoDto getShipInfo() {
        return this.shipInfo;
    }

    /**
     * @return 次のレベルまでの経験値
     */
    public String getNext() {
        String next = "";
        Long nextLvExp = ExpTable.get().get((int) this.lv + 1);
        if (nextLvExp != null) {
            next = Long.toString(nextLvExp - this.exp);
        }
        return next;
    }

    /**
     * @return 疲労が抜けるまでの時間
     */
    public String getCondClearDate() {
        if (this.cond < 49) {
            return new SimpleDateFormat("HH:mm").format(this.time.getTime());
        }
        return "";
    }

    /**
     * 艦娘が大破しているかを調べます
     * @return 大破以上の場合
     */
    public boolean isBadlyDamage() {
        return ((float) this.nowhp / (float) this.maxhp) <= AppConstants.BADLY_DAMAGE;
    }

    /**
     * 艦娘が中破しているかを調べます
     * @return 中破以上の場合
     */
    public boolean isHalfDamage() {
        return ((float) this.nowhp / (float) this.maxhp) <= AppConstants.HALF_DAMAGE;
    }

    /**
     * 艦娘が小破しているかを調べます
     * @return 小破以上の場合
     */
    public boolean isSlightDamage() {
        return ((float) this.nowhp / (float) this.maxhp) <= AppConstants.SLIGHT_DAMAGE;
    }
}
