/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.dto;

/**
 * 艦娘の名前と種別を表します
 *
 */
public final class ShipInfoDto extends AbstractDto {

    /** 空の艦種 */
    public static final ShipInfoDto EMPTY = new ShipInfoDto("", "", "", 0, 0, 0);

    /** 名前 */
    private final String name;

    /** 艦種 */
    private final String type;

    /** 改レベル */
    private final int afterlv;

    /** flagshipもしくはelite (敵艦のみ) */
    private final String flagship;

    /** 弾 */
    private final int maxBull;

    /** 燃料 */
    private final int maxFuel;

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
     * @return flagshipもしくはelite
     */
    public String getFlagship() {
        return this.flagship;
    }

    /**
     * @return 改造レベル(改造ができない場合、0)
     */
    public int getAfterlv() {
        return this.afterlv;
    }

    /**
     * @return 弾
     */
    public int getMaxBull() {
        return this.maxBull;
    }

    /**
     * @return 燃料
     */
    public int getMaxFuel() {
        return this.maxFuel;
    }
}
