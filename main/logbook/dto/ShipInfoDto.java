package logbook.dto;

import logbook.internal.ShipStyle;
import logbook.proto.LogbookEx.ShipInfoDtoPb;
import logbook.proto.Tag;

import org.apache.commons.lang3.StringUtils;

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
    public ShipInfoDto(int shipId, String name, int stype, String flagship, int afterlv, int aftershipid, int maxBull,
            int maxFuel, int[] powup, int[] maxeq) {
        this.name = name;
        this.shipId = shipId;
        this.stype = stype;
        this.type = ShipStyle.get(String.valueOf(stype));
        this.afterlv = afterlv;
        this.aftershipid = aftershipid;
        this.flagship = flagship;
        this.maxBull = maxBull;
        this.maxFuel = maxFuel;
        this.powup = powup;
        this.maxeq = maxeq;
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

    public String getEnemyShipName() {
        String name = this.name;
        if (!StringUtils.isEmpty(this.flagship)) {
            name += " " + this.flagship;
        }
        return name;
    }
}
