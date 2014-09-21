package logbook.dto;

import javax.json.JsonObject;

import logbook.proto.LogbookEx.ShipInfoDtoPb;
import logbook.proto.Tag;
import logbook.util.JsonUtils;

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

    /** 艦種 */
    @Tag(3)
    private int stype;

    /** 艦種 */
    @Tag(4)
    private String type;

    /** 改レベル */
    @Tag(5)
    private int afterlv;

    /** 改造後の艦 */
    @Tag(6)
    private int aftershipid;

    /** flagshipもしくはelite (敵艦のみ) */
    @Tag(7)
    private String flagship;

    /** 弾 */
    @Tag(8)
    private int maxBull;

    /** 燃料 */
    @Tag(9)
    private int maxFuel;

    /** 近代化改修時のup項目 */
    @Tag(10)
    private int[] powup = new int[4];

    /** スロット数最大 */
    @Tag(11)
    private int[] maxeq = new int[5];

    /** 火力 */
    @Tag(12)
    private int houg;

    /** 火力(最大) */
    @Tag(13)
    private int hougMax;

    /** 雷装 */
    @Tag(14)
    private int raig;

    /** 雷装(最大) */
    @Tag(15)
    private int raigMax;

    /** 対空 */
    @Tag(16)
    private int tyku;

    /** 対空(最大) */
    @Tag(17)
    private int tykuMax;

    /** 装甲 */
    @Tag(18)
    private int souk;

    /** 装甲(最大) */
    @Tag(19)
    private int soukMax;

    /** 回避 */
    @Tag(20)
    private int kaih;

    /** 回避(最大) */
    @Tag(21)
    private int kaihMax;

    /** 対潜 */
    @Tag(22)
    private int tais;

    /** 対潜(最大) */
    @Tag(23)
    private int taisMax;

    /** 索敵 */
    @Tag(24)
    private int saku;

    /** 索敵(最大) */
    @Tag(25)
    private int sakuMax;

    /** 運 */
    @Tag(26)
    private int luck;

    /** 運(最大) */
    @Tag(27)
    private int luckMax;

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
        this.type = type;
        this.afterlv = afterlv;
        this.flagship = flagship;
        this.maxBull = maxBull;
        this.maxFuel = maxFuel;
    }

    /**
     * コンストラクター
     */
    public ShipInfoDto(JsonObject object) {
        this.shipId = object.getJsonNumber("api_id").intValue();
        this.stype = object.getJsonNumber("api_stype").intValue();
        this.flagship = object.getString("api_yomi");
        if ("-".equals(this.flagship)) {
            this.flagship = "";
        }
        this.afterlv = object.getJsonNumber("api_afterlv").intValue();
        this.aftershipid = Integer.parseInt(object.getString("api_aftershipid"));
        this.maxBull = 0;
        if (object.containsKey("api_bull_max")) {
            this.maxBull = object.getJsonNumber("api_bull_max").intValue();
        }
        this.maxFuel = 0;
        if (object.containsKey("api_fuel_max")) {
            this.maxFuel = object.getJsonNumber("api_fuel_max").intValue();
        }
        this.powup = JsonUtils.getIntArray(object, "api_powup");
        this.maxeq = JsonUtils.getIntArray(object, "api_maxeq");
        this.houg = object.getJsonArray("api_houg").getInt(0);
        this.hougMax = object.getJsonArray("api_houg").getInt(1);
        this.raig = object.getJsonArray("api_raig").getInt(0);
        this.raigMax = object.getJsonArray("api_raig").getInt(1);
        this.tyku = object.getJsonArray("api_tyku").getInt(0);
        this.tykuMax = object.getJsonArray("api_tyku").getInt(1);
        this.souk = object.getJsonArray("api_souk").getInt(0);
        this.soukMax = object.getJsonArray("api_souk").getInt(1);
        this.kaih = object.getJsonArray("api_kaih").getInt(0);
        this.kaihMax = object.getJsonArray("api_kaih").getInt(1);
        this.tais = object.getJsonArray("api_tais").getInt(0);
        this.taisMax = object.getJsonArray("api_tais").getInt(1);
        this.saku = object.getJsonArray("api_saku").getInt(0);
        this.sakuMax = object.getJsonArray("api_saku").getInt(1);
        this.luck = object.getJsonArray("api_luck").getInt(0);
        this.luckMax = object.getJsonArray("api_luck").getInt(1);
    }

    public ShipInfoDtoPb toProto() {
        ShipInfoDtoPb.Builder builder = ShipInfoDtoPb.newBuilder();
        if (this.name != null) {
            builder.setName(this.name);
        }
        builder.setShipId(this.shipId);
        builder.setStype(this.stype);
        if (this.type != null) {
            builder.setType(this.type);
        }
        builder.setAfterlv(this.afterlv);
        builder.setAftershipid(this.aftershipid);
        if (this.flagship != null) {
            builder.setFlagship(this.flagship);
        }
        builder.setMaxBull(this.maxBull);
        builder.setMaxFuel(this.maxFuel);
        if (this.powup != null) {
            for (int b : this.powup) {
                builder.addPowup(b);
            }
        }
        if (this.maxeq != null) {
            for (int b : this.maxeq) {
                builder.addMaxeq(b);
            }
        }
        return builder.build();
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
     * @return 改造後の艦ID(改造ができない場合、0)
     */
    public void setAftershipid(int aftershipid) {
        this.aftershipid = aftershipid;
    }

    /**
     * 艦種を設定します。
     * @param type 艦種
     */
    public int getStype() {
        return this.stype;
    }

    /**
     * 艦種を取得します。
     * @return 艦種
     */
    public String getType() {
        return this.type;
    }

    /**
     * 艦種を設定します。
     * @param type 艦種
     */
    public void setStype(int stype) {
        this.stype = stype;
    }

    /**
     * 艦種を設定します。
     * @param type 艦種
     */
    public void setType(String type) {
        this.type = type;
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
     * maxeqを設定します。
     * @param maxeq
     */
    public void setMaxeq(int[] maxeq) {
        this.maxeq = maxeq;
    }

    /**
     * @return houg
     */
    public int getHoug() {
        return this.houg;
    }

    /**
     * @param houg セットする houg
     */
    public void setHoug(int houg) {
        this.houg = houg;
    }

    /**
     * @return hougMax
     */
    public int getHougMax() {
        return this.hougMax;
    }

    /**
     * @param hougMax セットする hougMax
     */
    public void setHougMax(int hougMax) {
        this.hougMax = hougMax;
    }

    /**
     * @return raig
     */
    public int getRaig() {
        return this.raig;
    }

    /**
     * @param raig セットする raig
     */
    public void setRaig(int raig) {
        this.raig = raig;
    }

    /**
     * @return raigMax
     */
    public int getRaigMax() {
        return this.raigMax;
    }

    /**
     * @param raigMax セットする raigMax
     */
    public void setRaigMax(int raigMax) {
        this.raigMax = raigMax;
    }

    /**
     * @return tyku
     */
    public int getTyku() {
        return this.tyku;
    }

    /**
     * @param tyku セットする tyku
     */
    public void setTyku(int tyku) {
        this.tyku = tyku;
    }

    /**
     * @return tykuMax
     */
    public int getTykuMax() {
        return this.tykuMax;
    }

    /**
     * @param tykuMax セットする tykuMax
     */
    public void setTykuMax(int tykuMax) {
        this.tykuMax = tykuMax;
    }

    /**
     * @return souk
     */
    public int getSouk() {
        return this.souk;
    }

    /**
     * @param souk セットする souk
     */
    public void setSouk(int souk) {
        this.souk = souk;
    }

    /**
     * @return soukMax
     */
    public int getSoukMax() {
        return this.soukMax;
    }

    /**
     * @param soukMax セットする soukMax
     */
    public void setSoukMax(int soukMax) {
        this.soukMax = soukMax;
    }

    /**
     * @return kaih
     */
    public int getKaih() {
        return this.kaih;
    }

    /**
     * @param kaih セットする kaih
     */
    public void setKaih(int kaih) {
        this.kaih = kaih;
    }

    /**
     * @return kaihMax
     */
    public int getKaihMax() {
        return this.kaihMax;
    }

    /**
     * @param kaihMax セットする kaihMax
     */
    public void setKaihMax(int kaihMax) {
        this.kaihMax = kaihMax;
    }

    /**
     * @return tais
     */
    public int getTais() {
        return this.tais;
    }

    /**
     * @param tais セットする tais
     */
    public void setTais(int tais) {
        this.tais = tais;
    }

    /**
     * @return taisMax
     */
    public int getTaisMax() {
        return this.taisMax;
    }

    /**
     * @param taisMax セットする taisMax
     */
    public void setTaisMax(int taisMax) {
        this.taisMax = taisMax;
    }

    /**
     * @return saku
     */
    public int getSaku() {
        return this.saku;
    }

    /**
     * @param saku セットする saku
     */
    public void setSaku(int saku) {
        this.saku = saku;
    }

    /**
     * @return sakuMax
     */
    public int getSakuMax() {
        return this.sakuMax;
    }

    /**
     * @param sakuMax セットする sakuMax
     */
    public void setSakuMax(int sakuMax) {
        this.sakuMax = sakuMax;
    }

    /**
     * @return luck
     */
    public int getLuck() {
        return this.luck;
    }

    /**
     * @param luck セットする luck
     */
    public void setLuck(int luck) {
        this.luck = luck;
    }

    /**
     * @return luckMax
     */
    public int getLuckMax() {
        return this.luckMax;
    }

    /**
     * @param luckMax セットする luckMax
     */
    public void setLuckMax(int luckMax) {
        this.luckMax = luckMax;
    }
}
