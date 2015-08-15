package logbook.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.internal.CondTiming;
import logbook.internal.ExpTable;
import logbook.internal.Ship;
import logbook.util.JsonUtils;

import com.dyuproject.protostuff.Tag;

/**
 * 艦娘を表します
 *
 */
public final class ShipDto extends ShipBaseDto implements Comparable<ShipDto> {

    /** 艦娘個人を識別するID */
    @Tag(10)
    private final int id;

    /** キャラクタ識別ID（その艦の最終形の艦ID） */
    @Tag(11)
    private final int charId;

    @Tag(12)
    private final int sortno;

    /** 鍵付き */
    @Tag(13)
    private boolean locked;

    /** 艦隊ID */
    @Tag(14)
    private String fleetid = "";

    @Tag(15)
    private int fleetpos;

    /** Lv */
    @Tag(16)
    private final int lv;

    /** 疲労 */
    @Tag(17)
    private final int cond;

    /** 入渠時間 */
    @Tag(18)
    private long docktime;

    /** 修復資材 燃料 */
    @Tag(19)
    private final int dockfuel;

    /** 修復資材 鋼材 */
    @Tag(20)
    private final int dockmetal;

    /** 残弾 */
    @Tag(21)
    private int bull;

    /** 残燃料 */
    @Tag(22)
    private int fuel;

    /** 経験値 */
    @Tag(23)
    private final int exp;

    /** 経験値ゲージの割合 */
    @Tag(30)
    private final float expraito;

    /** HP */
    @Tag(24)
    private int nowhp;

    /** MaxHP */
    @Tag(25)
    private final int maxhp;

    /** 搭載可能装備数 */
    @Tag(26)
    private final int slotnum;

    /** 艦載機の搭載数 */
    @Tag(27)
    private int[] onslot;

    /** 補助装備 */
    @Tag(34)
    private final int slotEx;

    @Tag(35)
    private final ItemDto slotExItem;

    @Tag(40)
    private final String json;

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     */
    public ShipDto(JsonObject object) {
        super(object);

        this.id = object.getJsonNumber("api_id").intValue();
        this.locked = object.getJsonNumber("api_locked").intValue() == 1;

        this.charId = Ship.getCharId(this.getShipInfo());
        this.sortno = object.getInt("api_sortno");

        this.lv = object.getJsonNumber("api_lv").intValue();
        this.cond = object.getJsonNumber("api_cond").intValue();

        this.docktime = object.getJsonNumber("api_ndock_time").longValue();
        this.dockfuel = object.getJsonArray("api_ndock_item").getJsonNumber(0).intValue();
        this.dockmetal = object.getJsonArray("api_ndock_item").getJsonNumber(1).intValue();

        this.bull = object.getJsonNumber("api_bull").intValue();
        this.fuel = object.getJsonNumber("api_fuel").intValue();

        this.exp = object.getJsonArray("api_exp").getJsonNumber(0).intValue();
        this.expraito = object.getJsonArray("api_exp").getJsonNumber(2).longValue() / 100f;
        this.nowhp = this.getParam().getHP();
        this.maxhp = this.getMax().getHP();
        this.slotnum = object.getJsonNumber("api_slotnum").intValue();
        this.onslot = JsonUtils.getIntArray(object, "api_onslot");

        int _slotEx = -1;
        if (object.containsKey("api_slot_ex")) {
            _slotEx = object.getInt("api_slot_ex");
        }
        this.slotEx = (_slotEx > 0) ? _slotEx : -1;
        this.slotExItem = GlobalContext.getItem(this.slotEx);

        this.json = object.toString();
    }

    /** 新規入手艦 */
    public ShipDto(int id, ShipInfoDto shipinfo, int[] slot) {
        super(shipinfo, slot, null, true);

        this.id = id;
        this.locked = false;
        this.charId = Ship.getCharId(shipinfo);
        this.sortno = this.getShipInfo().getSortNo();

        this.lv = 1;
        this.cond = 40;

        this.docktime = 0;
        this.dockfuel = 0;
        this.dockmetal = 0;

        this.bull = shipinfo.getMaxBull();
        this.fuel = shipinfo.getMaxFuel();

        this.exp = 0;
        this.expraito = 0;
        this.nowhp = this.maxhp = this.getParam().getHP();

        this.slotnum = shipinfo.getSlotNum();
        this.onslot = new int[] { 0, 0, 0, 0, 0 };

        List<ItemDto> items = this.getItem2();
        int[] maxeq = shipinfo.getMaxeq2();
        for (int i = 0; i < items.size(); ++i) {
            ItemDto item = items.get(i);
            if ((item != null) && item.isPlane()) {
                this.onslot[i] = maxeq[i];
            }
        }

        this.slotEx = -1;
        this.slotExItem = null;

        this.json = null;
    }

    /**
     * 艦娘個人を識別するID
     * @return 艦娘個人を識別するID
     */
    public int getId() {
        return this.id;
    }

    /**
     * 艦娘キャラを識別するID
     * @return 艦娘キャラを識別するID
     */
    public int getCharId() {
        return this.charId;
    }

    /**
     * sortno
     * @return sortno
     */
    public int getSortno() {
        return this.sortno;
    }

    /**
     * 鍵付き
     * @return 鍵付き
     */
    public boolean getLocked() {
        return this.locked;
    }

    /**
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
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
     * 艦隊に所属しているか？
     * @return
     */
    public boolean isFleetMember() {
        return (this.fleetid != null) && (this.fleetid.length() > 0);
    }

    /**
     * 艦隊での位置
     * @return fleetpos
     */
    public int getFleetpos() {
        return this.fleetpos;
    }

    /**
     * @param fleetpos セットする fleetpos
     */
    public void setFleetpos(int fleetpos) {
        this.fleetpos = fleetpos;
    }

    /**
     * Lv
     * @return Lv
     */
    @Override
    public int getLv() {
        return this.lv;
    }

    /**
     * 疲労
     * @return 疲労
     */
    public int getCond() {
        return this.cond;
    }

    /**
     * 現在の疲労推定値（下限値）
     * @return 現在の疲労推定値（下限値）
     */
    public int getEstimatedCond(CondTiming timer) {
        if (this.cond >= 49)
            return this.cond;
        int estimatedCond = this.cond + (timer.calcPastCycles() * 3);
        if (estimatedCond > 49)
            return 49;
        return estimatedCond;
    }

    /**
     * 入渠時間
     * @return 入渠時間
     */
    public long getDocktime() {
        return this.docktime;
    }

    /**
     * 泊地修理による修理時間
     * @return
     */
    public long getAkashiTime() {
        return this.docktime + (30 * 1000); // 最大30秒の遅延
    }

    /**
     * 入渠時間をセット
     */
    public void setDockTime(long docktime) {
        this.docktime = docktime;
    }

    /**
     * 修復資材 燃料
     * @return 修復資材 燃料
     */
    public int getDockfuel() {
        return this.dockfuel;
    }

    /**
     * 修復資材 鋼材
     * @return 修復資材 鋼材
     */
    public int getDockmetal() {
        return this.dockmetal;
    }

    /**
     * 残弾
     * @return 残弾
     */
    public int getBull() {
        return this.bull;
    }

    /**
     * @param bull 残弾
     */
    public void setBull(int bull) {
        this.bull = bull;
    }

    /**
     * 燃料
     * @return 燃料
     */
    public int getFuel() {
        return this.fuel;
    }

    /**
     * @param fuel 残燃料
     */
    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    /**
     * 経験値
     * @return 経験値
     */
    public int getExp() {
        return this.exp;
    }

    public void setNowhp(int v) {
        this.nowhp = v;
    }

    /**
     * 経験値ゲージの割合
     * @return 経験値ゲージの割合
     */
    public float getExpraito() {
        return this.expraito;
    }

    /**
     * 現在のHP
     * @return HP
     */
    public int getNowhp() {
        return this.nowhp;
    }

    /**
     * 最大HP
     * @return MaxHP
     */
    public int getMaxhp() {
        return this.maxhp;
    }

    /**
     * 装備可能スロット数
     * @return
     */
    public int getSlotNum() {
        return this.slotnum;
    }

    /**
     * 現在の艦載機搭載数
     * @return 現在の艦載機搭載数
     */
    @Override
    public int[] getOnSlot() {
        return this.onslot;
    }

    /**
     * 次のレベルまでの経験値
     * @return 次のレベルまでの経験値
     */
    public Integer getNext() {
        Integer nextLvExp = ExpTable.get().get(
                Math.min(ExpTable.MAX_LEVEL, this.lv + 1));
        if (nextLvExp != null) {
            return nextLvExp - this.exp;
        }
        return null;
    }

    /**
     * 疲労が抜けるまでの時間
     * @return 疲労が抜けるまでの時間
     */
    public Date getCondClearTime(CondTiming timer, int okCond) {
        if (this.cond >= okCond) {
            return null;
        }
        return timer.calcCondClearTime(this.cond, okCond);
    }

    /**
     * 疲労が抜けるまでの時間
     * @return 疲労が抜けるまでの時間
     */
    public Date getCondClearTime(CondTiming timer) {
        return this.getCondClearTime(timer, AppConfig.get().getOkCond());
    }

    /**
     * 艦娘が轟沈しているかを調べます
     * @return 轟沈したの場合
     */
    public boolean isSunk() {
        return (this.nowhp == 0);
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

    @Override
    public int compareTo(ShipDto o) {
        return Integer.compare(this.id, o.id);
    }

    /**
     * 装備で加算された命中
     * @return 装備の命中
     */
    public int getAccuracy() {
        int accuracy = 0;
        for (int itemid : this.slot) {
            if (-1 != itemid) {
                Map<Integer, ItemDto> itemMap = GlobalContext.getItemMap();
                ItemDto item = itemMap.get(itemid);
                if (item != null) {
                    accuracy += item.getParam().getHoum();
                }
            }
        }
        return accuracy;
    }

    /**
     * 砲撃戦火力
     * @return 砲撃戦火力
     */
    public int getHougekiPower() {
        switch (this.getStype()) {
        case 7: // 軽空母
        case 11: // 正規空母
        case 16: // 水上機母艦
        case 18: // 装甲空母
            // (火力 + 雷装) × 1.5 + 爆装 × 2 + 55
            int rai = 0;
            int baku = 0;
            Map<Integer, ItemDto> itemMap = GlobalContext.getItemMap();
            for (int itemid : this.slot) {
                if (-1 != itemid) {
                    ItemDto item = itemMap.get(itemid);
                    if (item != null) {
                        rai += item.getParam().getRaig();
                        baku += item.getParam().getBaku();
                    }
                }
            }
            return (int) Math.round(((this.getKaryoku() + rai) * 1.5d) + (baku * 2) + 55);
        default:
            return this.getKaryoku() + 5;
        }
    }

    /**
     * 雷撃戦火力
     * @return 雷撃戦火力
     */
    public int getRaigekiPower() {
        return this.getRaisou() + 5;
    }

    /**
     * 対潜火力
     * @return 対潜火力
     */
    public int getTaisenPower() {
        // [ 艦船の対潜 ÷ 5 ] + 装備の対潜 × 2 + 25
        int taisenShip = this.getTaisen();
        int taisenItem = 0;
        Map<Integer, ItemDto> itemMap = GlobalContext.getItemMap();
        for (int itemid : this.slot) {
            if (-1 != itemid) {
                ItemDto item = itemMap.get(itemid);
                if (item != null) {
                    int taisen = item.getParam().getTais();
                    taisenShip -= taisen;
                    taisenItem += taisen;
                }
            }
        }
        return (int) Math.round(Math.floor(taisenShip / 5d) + (taisenItem * 2) + 25);
    }

    /**
     * 夜戦火力
     * @return 夜戦火力
     */
    public int getYasenPower() {
        return this.getKaryoku() + this.getRaisou();
    }

    /**
     * データの更新に使ったJSON
     * @return json
     */
    public JsonObject getJson() {
        if (this.json == null)
            return null;

        return JsonUtils.fromString(this.json);
    }

    /**
     * @param onslot2
     */
    public void setOnslot(int[] onslot) {
        this.onslot = onslot;
    }

    /**
     * 補助装備ID
     * @return
     */
    public int getSlotEx() {
        return this.slotEx;
    }

    /**
     * 補助装備
     * @return
     */
    public ItemDto getSlotExItem() {
        return this.slotExItem;
    }
}