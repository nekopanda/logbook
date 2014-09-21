package logbook.dto;

import java.util.Calendar;
import java.util.Date;

import javax.json.JsonNumber;
import javax.json.JsonObject;

import logbook.constants.AppConstants;
import logbook.internal.ExpTable;
import logbook.internal.Ship;
import logbook.proto.LogbookEx.ShipDtoPb;
import logbook.proto.Tag;
import logbook.util.JsonUtils;

/**
 * 艦娘を表します
 *
 */
public final class ShipDto extends ShipBaseDto {

    /** 日時 */
    private final Calendar time = Calendar.getInstance();

    /** 日時 */
    private final Calendar condClearTime = Calendar.getInstance();

    /** 艦娘個人を識別するID */
    @Tag(1)
    private final int id;

    /** キャラクタ識別ID（その艦の最終形の艦ID） */
    @Tag(2)
    private final int charId;

    private final int sortno;

    /** 鍵付き */
    @Tag(3)
    private final boolean locked;

    /** 艦隊ID */
    @Tag(4)
    private String fleetid = "";

    private int fleetpos;

    /** Lv */
    @Tag(7)
    private final int lv;

    /** 疲労 */
    @Tag(8)
    private final int cond;

    /** 入渠時間 */
    @Tag(9)
    private final long docktime;

    /** 修復資材 燃料 */
    @Tag(10)
    private final int dockfuel;

    /** 修復資材 鋼材 */
    @Tag(11)
    private final int dockmetal;

    /** 残弾 */
    @Tag(12)
    private int bull;

    /** 残燃料 */
    @Tag(14)
    private int fuel;

    /** 経験値 */
    @Tag(16)
    private final int exp;

    /** HP */
    @Tag(17)
    private int nowhp;

    /** MaxHP */
    @Tag(18)
    private final int maxhp;

    /** 搭載可能装備数 */
    @Tag(19)
    private final int slotnum;

    /** 装備 */
    @Tag(20)
    private final int[] slot;

    /** 艦載機の搭載数 */
    @Tag(21)
    private final int[] onslot;

    /** 火力 */
    @Tag(22)
    private final int karyoku;

    /** 火力(最大) */
    @Tag(23)
    private final int karyokuMax;

    /** 雷装 */
    @Tag(24)
    private final int raisou;

    /** 雷装(最大) */
    @Tag(25)
    private final int raisouMax;

    /** 対空 */
    @Tag(26)
    private final int taiku;

    /** 対空(最大) */
    @Tag(27)
    private final int taikuMax;

    /** 装甲 */
    @Tag(28)
    private final int soukou;

    /** 装甲(最大) */
    @Tag(29)
    private final int soukouMax;

    /** 回避 */
    @Tag(30)
    private final int kaihi;

    /** 回避(最大) */
    @Tag(31)
    private final int kaihiMax;

    /** 対潜 */
    @Tag(32)
    private final int taisen;

    /** 対潜(最大) */
    @Tag(33)
    private final int taisenMax;

    /** 索敵 */
    @Tag(34)
    private final int sakuteki;

    /** 索敵(最大) */
    @Tag(35)
    private final int sakutekiMax;

    /** 運 */
    @Tag(36)
    private final int lucky;

    /** 運(最大) */
    @Tag(37)
    private final int luckyMax;

    /** */
    private final int lockedEquip;

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     */
    public ShipDto(JsonObject object) {
        super(object);

        this.id = object.getJsonNumber("api_id").intValue();
        this.locked = object.getJsonNumber("api_locked").intValue() == 1;

        int charId = this.getShipId();
        int afterShipId = this.getShipInfo().getAftershipid();
        while (afterShipId != 0) {
            charId = afterShipId;
            afterShipId = Ship.get(String.valueOf(afterShipId)).getAftershipid();
        }
        this.charId = charId;
        this.sortno = object.getInt("api_sortno");

        this.lv = object.getJsonNumber("api_lv").intValue();
        this.cond = object.getJsonNumber("api_cond").intValue();

        this.docktime = object.getJsonNumber("api_ndock_time").longValue();
        this.dockfuel = object.getJsonArray("api_ndock_item").getJsonNumber(0).intValue();
        this.dockmetal = object.getJsonArray("api_ndock_item").getJsonNumber(1).intValue();

        this.bull = object.getJsonNumber("api_bull").intValue();
        this.fuel = object.getJsonNumber("api_fuel").intValue();

        this.exp = object.getJsonArray("api_exp").getJsonNumber(0).intValue();
        this.nowhp = object.getJsonNumber("api_nowhp").intValue();
        this.maxhp = object.getJsonNumber("api_maxhp").intValue();
        this.slotnum = object.getJsonNumber("api_slotnum").intValue();
        this.slot = JsonUtils.getIntArray(object, "api_slot");
        this.onslot = JsonUtils.getIntArray(object, "api_onslot");
        this.karyoku = ((JsonNumber) object.getJsonArray("api_karyoku").get(0)).intValue();
        this.karyokuMax = ((JsonNumber) object.getJsonArray("api_karyoku").get(1)).intValue();
        this.raisou = ((JsonNumber) object.getJsonArray("api_raisou").get(0)).intValue();
        this.raisouMax = ((JsonNumber) object.getJsonArray("api_raisou").get(1)).intValue();
        this.taiku = ((JsonNumber) object.getJsonArray("api_taiku").get(0)).intValue();
        this.taikuMax = ((JsonNumber) object.getJsonArray("api_taiku").get(1)).intValue();
        this.soukou = ((JsonNumber) object.getJsonArray("api_soukou").get(0)).intValue();
        this.soukouMax = ((JsonNumber) object.getJsonArray("api_soukou").get(1)).intValue();
        this.kaihi = ((JsonNumber) object.getJsonArray("api_kaihi").get(0)).intValue();
        this.kaihiMax = ((JsonNumber) object.getJsonArray("api_kaihi").get(1)).intValue();
        this.taisen = ((JsonNumber) object.getJsonArray("api_taisen").get(0)).intValue();
        this.taisenMax = ((JsonNumber) object.getJsonArray("api_taisen").get(1)).intValue();
        this.sakuteki = ((JsonNumber) object.getJsonArray("api_sakuteki").get(0)).intValue();
        this.sakutekiMax = ((JsonNumber) object.getJsonArray("api_sakuteki").get(1)).intValue();
        this.lucky = ((JsonNumber) object.getJsonArray("api_lucky").get(0)).intValue();
        this.luckyMax = ((JsonNumber) object.getJsonArray("api_lucky").get(1)).intValue();
        this.lockedEquip = object.getJsonNumber("api_locked_equip").intValue();
        // 疲労が抜ける時間を計算する
        if (this.cond < 49) {
            this.condClearTime.add(Calendar.MINUTE, Math.max(49 - this.cond, 3));
        }
    }

    public ShipDtoPb toProto() {
        ShipDtoPb.Builder builder = ShipDtoPb.newBuilder();
        builder.setId(this.id);
        builder.setCharId(this.charId);
        builder.setLocked(this.locked);
        if (this.fleetid != null) {
            builder.setFleetid(this.fleetid);
        }
        builder.setLv(this.lv);
        builder.setCond(this.cond);
        builder.setDocktime(this.docktime);
        builder.setDockfuel(this.dockfuel);
        builder.setDockmetal(this.dockmetal);
        builder.setBull(this.bull);
        builder.setFuel(this.fuel);
        builder.setExp(this.exp);
        builder.setNowhp(this.nowhp);
        builder.setMaxhp(this.maxhp);
        builder.setSlotnum(this.slotnum);
        if (this.slot != null) {
            for (Integer b : this.slot) {
                if (b != null) {
                    builder.addSlot(b);
                }
            }
        }
        if (this.onslot != null) {
            for (Integer b : this.onslot) {
                if (b != null) {
                    builder.addOnslot(b);
                }
            }
        }
        builder.setKaryoku(this.karyoku);
        builder.setKaryokuMax(this.karyokuMax);
        builder.setRaisou(this.raisou);
        builder.setRaisouMax(this.raisouMax);
        builder.setTaiku(this.taiku);
        builder.setTaikuMax(this.taikuMax);
        builder.setSoukou(this.soukou);
        builder.setSoukouMax(this.soukouMax);
        builder.setKaihi(this.kaihi);
        builder.setKaihiMax(this.kaihiMax);
        builder.setTaisen(this.taisen);
        builder.setTaisenMax(this.taisenMax);
        builder.setSakuteki(this.sakuteki);
        builder.setSakutekiMax(this.sakutekiMax);
        builder.setLucky(this.lucky);
        builder.setLuckyMax(this.luckyMax);
        if (this.getShipInfo() != null) {
            builder.setShipInfo(this.getShipInfo().toProto());
        }
        return builder.build();
    }

    /**
     * @return 艦娘個人を識別するID
     */
    public int getId() {
        return this.id;
    }

    /**
     * @return 艦娘キャラを識別するID
     */
    public int getCharId() {
        return this.charId;
    }

    /**
     * @return sortno
     */
    public int getSortno() {
        return this.sortno;
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

    public boolean isFleetMember() {
        return (this.fleetid != null) && (this.fleetid.length() > 0);
    }

    /**
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
     * @return Lv
     */
    public int getLv() {
        return this.lv;
    }

    /**
     * @return 疲労
     */
    public int getCond() {
        return this.cond;
    }

    /**
     * @return 現在の疲労推定値（下限値）
     */
    public int getEstimatedCond() {
        if (this.cond >= 49)
            return this.cond;
        long elapsedTime = new Date().getTime() - this.time.getTime().getTime();
        int estimatedCond = (int) (this.cond + ((elapsedTime / (3 * 60 * 1000)) * 3));
        if (estimatedCond > 49)
            return 49;
        return estimatedCond;
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
    public int getDockfuel() {
        return this.dockfuel;
    }

    /**
     * @return 修復資材 鋼材
     */
    public int getDockmetal() {
        return this.dockmetal;
    }

    /**
     * @return 弾
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
     * @return 経験値
     */
    public int getExp() {
        return this.exp;
    }

    public void setNowhp(int v) {
        this.nowhp = v;
    }

    /**
     * @return HP
     */
    public int getNowhp() {
        return this.nowhp;
    }

    /**
     * @return MaxHP
     */
    public int getMaxhp() {
        return this.maxhp;
    }

    public int[] getRawSlot() {
        return this.slot;
    }

    public int getSlotNum() {
        return this.slotnum;
    }

    @Override
    public int[] getOnSlot() {
        return this.onslot;
    }

    /**
     * @return 装備ID
     */
    @Override
    public int[] getItemId() {
        return this.slot;
    }

    /**
     * @return 火力
     */
    @Override
    public int getKaryoku() {
        return this.karyoku;
    }

    /**
     * @return 火力(最大)
     */
    @Override
    public int getKaryokuMax() {
        return this.karyokuMax;
    }

    /**
     * @return 雷装
     */
    @Override
    public int getRaisou() {
        return this.raisou;
    }

    /**
     * @return 雷装(最大)
     */
    @Override
    public int getRaisouMax() {
        return this.raisouMax;
    }

    /**
     * @return 対空
     */
    @Override
    public int getTaiku() {
        return this.taiku;
    }

    /**
     * @return 対空(最大)
     */
    @Override
    public int getTaikuMax() {
        return this.taikuMax;
    }

    /**
     * @return 装甲
     */
    @Override
    public int getSoukou() {
        return this.soukou;
    }

    /**
     * @return 装甲(最大)
     */
    @Override
    public int getSoukouMax() {
        return this.soukouMax;
    }

    /**
     * @return 回避
     */
    @Override
    public int getKaihi() {
        return this.kaihi;
    }

    /**
     * @return 回避(最大)
     */
    @Override
    public int getKaihiMax() {
        return this.kaihiMax;
    }

    /**
     * @return 対潜
     */
    @Override
    public int getTaisen() {
        return this.taisen;
    }

    /**
     * @return 対潜(最大)
     */
    @Override
    public int getTaisenMax() {
        return this.taisenMax;
    }

    /**
     * @return 索敵
     */
    @Override
    public int getSakuteki() {
        return this.sakuteki;
    }

    /**
     * @return 索敵(最大)
     */
    @Override
    public int getSakutekiMax() {
        return this.sakutekiMax;
    }

    /**
     * @return 運
     */
    @Override
    public int getLucky() {
        return this.lucky;
    }

    /**
     * @return 運(最大)
     */
    @Override
    public int getLuckyMax() {
        return this.luckyMax;
    }

    /**
     * @return 次のレベルまでの経験値
     */
    public String getNext() {
        String next = "";
        Integer nextLvExp = ExpTable.get().get(this.lv + 1);
        if (nextLvExp != null) {
            next = String.valueOf(nextLvExp - this.exp);
        }
        return next;
    }

    /**
     * @return 疲労が抜けるまでの時間
     */
    public Calendar getCondClearTime() {
        return this.condClearTime;
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

    /**
     * lockedEquipを取得します。
     * @return lockedEquip
     */
    public int getLockedEquip() {
        return this.lockedEquip;
    }
}