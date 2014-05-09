package logbook.dto;

/**
 * 投入資源と秘書艦を表します
 *
 */
public final class ResourceDto extends AbstractDto {

    /** 種類(大型艦建造:1, 通常艦建造:0) */
    private final String type;

    /** 燃料 */
    private final String fuel;

    /** 弾薬 */
    private final String ammo;

    /** 鋼材 */
    private final String metal;

    /** ボーキサイト */
    private final String bauxite;

    /** 開発資材 */
    private final String researchMaterials;

    /** 秘書艦 */
    private final ShipDto ship;

    /** 司令部Lv */
    private final int hqLevel;

    /** 空きドック */
    private String freeDock;

    /**
     * コンストラクター(建造)
     * 
     * @param type 建造種類
     * @param fuel 燃料
     * @param ammo 弾薬
     * @param metal 鋼材
     * @param bauxite ボーキサイト
     * @param researchMaterials 開発資材
     * @param ship 秘書艦
     */
    public ResourceDto(String type, String fuel, String ammo, String metal, String bauxite, String researchMaterials,
            ShipDto ship, int hqLevel) {

        this.type = type;
        this.fuel = fuel;
        this.ammo = ammo;
        this.metal = metal;
        this.bauxite = bauxite;
        this.researchMaterials = researchMaterials;
        this.ship = ship;
        this.hqLevel = hqLevel;
    }

    /**
     * コンストラクター(開発)
     * 
     * @param fuel 燃料
     * @param ammo 弾薬
     * @param metal 鋼材
     * @param bauxite ボーキサイト
     * @param ship 秘書艦
     */
    public ResourceDto(String fuel, String ammo, String metal, String bauxite, ShipDto ship, int hqLevel) {

        this.type = null;
        this.fuel = fuel;
        this.ammo = ammo;
        this.metal = metal;
        this.bauxite = bauxite;
        this.researchMaterials = null;
        this.ship = ship;
        this.hqLevel = hqLevel;
    }

    /**
     * @return 種類
     */
    public String getType() {
        return this.type;
    }

    /**
     * @return 燃料
     */
    public String getFuel() {
        return this.fuel;
    }

    /**
     * @return 弾薬
     */
    public String getAmmo() {
        return this.ammo;
    }

    /**
     * @return 鋼材
     */
    public String getMetal() {
        return this.metal;
    }

    /**
     * @return ボーキサイト
     */
    public String getBauxite() {
        return this.bauxite;
    }

    /**
     * @return 開発資材
     */
    public String getResearchMaterials() {
        return this.researchMaterials;
    }

    /**
     * @return 秘書艦
     */
    public ShipDto getSecretary() {
        return this.ship;
    }

    /**
     * @return 司令部Lv
     */
    public int getHqLevel() {
        return this.hqLevel;
    }

    /**
     * @return 空きドック
     */
    public String getFreeDock() {
        return this.freeDock != null ? this.freeDock : "0";
    }

    /**
     * @return 空きドックをセットする
     */
    public void setFreeDock(String freeDock) {
        if (this.freeDock == null) {
            this.freeDock = freeDock;
        }
    }
}
