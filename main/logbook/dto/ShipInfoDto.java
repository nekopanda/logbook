package logbook.dto;

import javax.json.JsonObject;

import logbook.internal.ShipParameterRecord;
import logbook.internal.ShipStyle;
import logbook.util.JsonUtils;

import org.apache.commons.lang3.StringUtils;

import com.dyuproject.protostuff.Tag;

/**
 * 艦娘の名前と種別を表します
 *
 */
public final class ShipInfoDto extends AbstractDto {

    /** 空の艦種 */
    public static final ShipInfoDto EMPTY = new ShipInfoDto();

    /** 名前 */
    @Tag(2)
    private String name;

    /** 艦ID */
    @Tag(1)
    private int shipId;

    /** 図鑑番号 */
    @Tag(25)
    private int sortNo;

    /** 艦種 */
    @Tag(3)
    private int stype;

    /** 改レベル */
    @Tag(5)
    private int afterlv;

    /** 改造後の艦 */
    @Tag(6)
    private int aftershipid;

    /** flagshipもしくはelite (敵艦のみ) */
    @Tag(7)
    private String flagship;

    /** 装備スロット数 */
    @Tag(26)
    private int slotNum;

    /** 弾 */
    @Tag(8)
    private int maxBull;

    /** 燃料 */
    @Tag(9)
    private int maxFuel;

    /** 近代化改修時のup項目 */
    @Tag(10)
    private int[] powup = null;

    /** スロット数最大 */
    @Tag(11)
    private int[] maxeq = null;

    /** パラメータ */
    @Tag(12)
    private ShipParameters param;

    /** パラメータMAX */
    @Tag(13)
    private ShipParameters max;

    @Tag(20)
    private String json;

    /**
     * コンストラクター
     */
    public ShipInfoDto() {
        this("", "", "", 0, 0, 0);
    }

    /**
     * コンストラクター
     */
    public ShipInfoDto(String name, String type, String flagship, int afterlv, int maxBull, int maxFuel) {
        this.name = name;
        this.afterlv = afterlv;
        this.flagship = flagship;
        this.maxBull = maxBull;
        this.maxFuel = maxFuel;
        this.param = new ShipParameters();
        this.max = new ShipParameters();
    }

    /**
     * コンストラクター
     */
    public ShipInfoDto(JsonObject object) {
        this.name = object.getString("api_name");
        this.shipId = object.getJsonNumber("api_id").intValue();
        this.flagship = object.getString("api_yomi");
        if ("-".equals(this.flagship)) {
            this.flagship = "";
        }
        this.stype = object.getJsonNumber("api_stype").intValue();
        this.slotNum = object.getInt("api_slot_num");

        if (!this.isEnemy()) {
            this.maxBull = object.getJsonNumber("api_bull_max").intValue();
            this.maxFuel = object.getJsonNumber("api_fuel_max").intValue();
        }

        boolean reduced = (object.get("api_maxeq") == null);

        if (!reduced) {
            this.sortNo = object.getJsonNumber("api_sortno").intValue();
            this.afterlv = object.getJsonNumber("api_afterlv").intValue();
            this.aftershipid = Integer.parseInt(object.getString("api_aftershipid"));
            this.powup = JsonUtils.getIntArray(object, "api_powup");
            this.maxeq = JsonUtils.getIntArray(object, "api_maxeq");
        }

        ShipParameters[] params = reduced ?
                ShipParameters.fromMasterEnemyShip(object) :
                ShipParameters.fromMasterShip(object);
        this.param = params[0];
        this.max = params[1];
        this.json = object.toString();
    }

    public String getFullName() {
        if (this.isEnemy() && !StringUtils.isEmpty(this.flagship)) {
            return this.name + " " + this.flagship;
        }
        return this.name;
    }

    public int[] getDefaultSlot() {
        ShipParameterRecord record = ShipParameterRecord.get(this.shipId);
        if ((record != null) && (record.getDefaultSlot() != null)) {
            return record.getDefaultSlot();
        }
        return new int[] { -1, -1, -1, -1, -1 };
    }

    /**
     * 名前を取得します。
     * @return 名前
     */
    public String getName() {
        return this.name;
    }

    /**
     * 名前を設定します。
     * @param name 名前
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 艦IDを設定します。
     * @param shipId 艦ID
     */
    public void setShipId(int shipId) {
        this.shipId = shipId;
    }

    /**
     * 艦IDを取得します。
     */
    public int getShipId() {
        return this.shipId;
    }

    /**
     * 改レベルを設定します。
     * @param afterlv 改レベル
     */
    public void setAfterlv(int afterlv) {
        this.afterlv = afterlv;
    }

    /**
     * @param aftershipid 改造後の艦ID(改造ができない場合、0)
     */
    public void setAftershipid(int aftershipid) {
        this.aftershipid = aftershipid;
    }

    /**
     * 艦種を設定します。
     */
    public int getStype() {
        return this.stype;
    }

    /**
     * 艦種を取得します。
     * @return 艦種
     */
    public String getType() {
        return ShipStyle.get(this.stype);
    }

    /**
     * 艦種を設定します。
     * @param stype 艦種
     */
    public void setStype(int stype) {
        this.stype = stype;
    }

    /**
     * @return 改造レベル(改造ができない場合、0)
     */
    public int getAfterlv() {
        return this.afterlv;
    }

    /**
     * @return 改造後の艦ID(改造ができない場合、0)
     */
    public int getAftershipid() {
        return this.aftershipid;
    }

    /**
     * flagshipもしくはelite (敵艦のみ)を取得します。
     * @return flagshipもしくはelite (敵艦のみ)
     */
    public String getFlagship() {
        return this.flagship;
    }

    /**
     * flagshipもしくはelite (敵艦のみ)を設定します。
     * @param flagship flagshipもしくはelite (敵艦のみ)
     */
    public void setFlagship(String flagship) {
        this.flagship = flagship;
    }

    /**
     * 弾を取得します。
     * @return 弾
     */
    public int getMaxBull() {
        return this.maxBull;
    }

    /**
     * 弾を設定します。
     * @param maxBull 弾
     */
    public void setMaxBull(int maxBull) {
        this.maxBull = maxBull;
    }

    /**
     * 燃料を取得します。
     * @return 燃料
     */
    public int getMaxFuel() {
        return this.maxFuel;
    }

    /**
     * 燃料を設定します。
     * @param maxFuel 燃料
     */
    public void setMaxFuel(int maxFuel) {
        this.maxFuel = maxFuel;
    }

    /**
     * powupを取得します。
     * @return powup
     */
    public int[] getPowup() {
        return this.powup;
    }

    /**
     * powupを設定します。
     * @param powup
     */
    public void setPowup(int[] powup) {
        this.powup = powup;
    }

    /**
     * maxeqを取得します。
     * @return maxeq
     */
    public int[] getMaxeq() {
        return this.maxeq;
    }

    /**
     * maxeqを取得します。
     * このオブジェクトにデータがない場合はShipParameterRecordに問い合わせます。
     * @return maxeq
     */
    public int[] getMaxeq2() {
        if (this.maxeq != null) {
            return this.maxeq;
        }
        ShipParameterRecord record = ShipParameterRecord.get(this.shipId);
        if ((record != null) && (record.getDefaultSlot() != null)) {
            return record.getMaxeq();
        }
        return null;
    }

    /**
     * maxeqを設定します。
     * @param maxeq
     */
    public void setMaxeq(int[] maxeq) {
        this.maxeq = maxeq;
    }

    /**
     * @return param
     */
    public ShipParameters getParam() {
        return this.param;
    }

    /**
     * @param param セットする param
     */
    public void setParam(ShipParameters param) {
        this.param = param;
    }

    /**
     * @return max
     */
    public ShipParameters getMax() {
        return this.max;
    }

    /**
     * @param max セットする max
     */
    public void setMax(ShipParameters max) {
        this.max = max;
    }

    /**
     * @return json
     */
    public JsonObject getJson() {
        return JsonUtils.fromString(this.json);
    }

    /**
     * @return sortNo
     */
    public int getSortNo() {
        return this.sortNo;
    }

    /**
     * @param sortNo セットする sortNo
     */
    public void setSortNo(int sortNo) {
        this.sortNo = sortNo;
    }

    /**
     * @return slotNum
     */
    public int getSlotNum() {
        return this.slotNum;
    }

    /**
     * @param slotNum セットする slotNum
     */
    public void setSlotNum(int slotNum) {
        this.slotNum = slotNum;
    }

    /**
     * @return
     */
    public boolean isEnemy() {
        return this.shipId > 500;
    }
}
