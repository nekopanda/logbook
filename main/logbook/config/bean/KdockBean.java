package logbook.config.bean;

/**
 * 建造ドックの投入資源
 *
 */
public final class KdockBean {

    /** 種類 */
    private String type;

    /** 燃料 */
    private String fuel;

    /** 弾薬 */
    private String ammo;

    /** 鋼材 */
    private String metal;

    /** ボーキサイト  */
    private String bauxite;

    /** 開発資材 */
    private String researchMaterials;

    /** 秘書艦 */
    private long shipId;

    /** 司令部Lv */
    private int hqLevel;

    /** 空きドック */
    private String freeDock;

    /**
     * 種類を取得します。
     * @return 種類
     */
    public String getType() {
        return this.type;
    }

    /**
     * 種類を設定します。
     * @param type 種類
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 燃料を取得します。
     * @return 燃料
     */
    public String getFuel() {
        return this.fuel;
    }

    /**
     * 燃料を設定します。
     * @param fuel 燃料
     */
    public void setFuel(String fuel) {
        this.fuel = fuel;
    }

    /**
     * 弾薬を取得します。
     * @return 弾薬
     */
    public String getAmmo() {
        return this.ammo;
    }

    /**
     * 弾薬を設定します。
     * @param ammo 弾薬
     */
    public void setAmmo(String ammo) {
        this.ammo = ammo;
    }

    /**
     * 鋼材を取得します。
     * @return 鋼材
     */
    public String getMetal() {
        return this.metal;
    }

    /**
     * 鋼材を設定します。
     * @param metal 鋼材
     */
    public void setMetal(String metal) {
        this.metal = metal;
    }

    /**
     * ボーキサイトを取得します。
     * @return ボーキサイト
     */
    public String getBauxite() {
        return this.bauxite;
    }

    /**
     * ボーキサイトを設定します。
     * @param bauxite ボーキサイト
     */
    public void setBauxite(String bauxite) {
        this.bauxite = bauxite;
    }

    /**
     * 開発資材を取得します。
     * @return 開発資材
     */
    public String getResearchMaterials() {
        return this.researchMaterials;
    }

    /**
     * 開発資材を設定します。
     * @param researchMaterials 開発資材
     */
    public void setResearchMaterials(String researchMaterials) {
        this.researchMaterials = researchMaterials;
    }

    /**
     * 秘書艦を取得します。
     * @return 秘書艦
     */
    public long getShipId() {
        return this.shipId;
    }

    /**
     * 秘書艦を設定します。
     * @param shipId 秘書艦
     */
    public void setShipId(long shipId) {
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
    public String getFreeDock() {
        return this.freeDock;
    }

    /**
     * 空きドックを設定します。
     * @param freeDock 空きドック
     */
    public void setFreeDock(String freeDock) {
        this.freeDock = freeDock;
    }

}
