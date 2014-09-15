package logbook.config.bean;

/**
 * 建造ドックの投入資源
 *
 */
public final class KdockBean {

    /** 種類 */
    private Integer type;

    /** 燃料 */
    private int fuel;

    /** 弾薬 */
    private int ammo;

    /** 鋼材 */
    private int metal;

    /** ボーキサイト  */
    private int bauxite;

    /** 開発資材 */
    private Integer researchMaterials;

    /** 秘書艦 */
    private int shipId;

    /** 司令部Lv */
    private int hqLevel;

    /** 空きドック */
    private Integer freeDock;

    /**
     * 種類を取得します。
     * @return 種類
     */
    public Integer getType() {
        return this.type;
    }

    /**
     * 種類を設定します。
     * @param type 種類
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * 燃料を取得します。
     * @return 燃料
     */
    public int getFuel() {
        return this.fuel;
    }

    /**
     * 燃料を設定します。
     * @param fuel 燃料
     */
    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    /**
     * 弾薬を取得します。
     * @return 弾薬
     */
    public int getAmmo() {
        return this.ammo;
    }

    /**
     * 弾薬を設定します。
     * @param ammo 弾薬
     */
    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }

    /**
     * 鋼材を取得します。
     * @return 鋼材
     */
    public int getMetal() {
        return this.metal;
    }

    /**
     * 鋼材を設定します。
     * @param metal 鋼材
     */
    public void setMetal(int metal) {
        this.metal = metal;
    }

    /**
     * ボーキサイトを取得します。
     * @return ボーキサイト
     */
    public int getBauxite() {
        return this.bauxite;
    }

    /**
     * ボーキサイトを設定します。
     * @param bauxite ボーキサイト
     */
    public void setBauxite(int bauxite) {
        this.bauxite = bauxite;
    }

    /**
     * 開発資材を取得します。
     * @return 開発資材
     */
    public Integer getResearchMaterials() {
        return this.researchMaterials;
    }

    /**
     * 開発資材を設定します。
     * @param researchMaterials 開発資材
     */
    public void setResearchMaterials(Integer researchMaterials) {
        this.researchMaterials = researchMaterials;
    }

    /**
     * 秘書艦を取得します。
     * @return 秘書艦
     */
    public int getShipId() {
        return this.shipId;
    }

    /**
     * 秘書艦を設定します。
     * @param shipId 秘書艦
     */
    public void setShipId(int shipId) {
        this.shipId = shipId;
    }

    /**
     * 司令部Lvを取得します。
     * @return 司令部Lv
     */
    public int getHqLevel() {
        return this.hqLevel;
    }

    /**
     * 司令部Lvを設定します。
     * @param hqLevel 司令部Lv
     */
    public void setHqLevel(int hqLevel) {
        this.hqLevel = hqLevel;
    }

    /**
     * 空きドックを取得します。
     * @return 空きドック
     */
    public Integer getFreeDock() {
        return this.freeDock;
    }

    /**
     * 空きドックを設定します。
     * @param freeDock 空きドック
     */
    public void setFreeDock(Integer freeDock) {
        this.freeDock = freeDock;
    }

}
