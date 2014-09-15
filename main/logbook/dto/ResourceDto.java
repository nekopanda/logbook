package logbook.dto;

/**
 * 投入資源と秘書艦を表します
 *
 */
public final class ResourceDto extends AbstractDto {

    /** 種類(大型艦建造:1, 通常艦建造:0) */
    private final Integer type;

    /** 燃料 */
    private final int fuel;

    /** 弾薬 */
    private final int ammo;

    /** 鋼材 */
    private final int metal;

    /** ボーキサイト */
    private final int bauxite;

    /** 開発資材 */
    private final Integer researchMaterials;

    /** 秘書艦 */
    private final ShipDto ship;

    /** 司令部Lv */
    private final int hqLevel;

    /** 空きドック */
    private Integer freeDock;

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
    public ResourceDto(int type, int fuel, int ammo, int metal, int bauxite, int researchMaterials,
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
    public ResourceDto(int fuel, int ammo, int metal, int bauxite, ShipDto ship, int hqLevel) {

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
    public Integer getType() {
        return this.type;
    }

    /**
     * @return 燃料
     */
    public int getFuel() {
        return this.fuel;
    }

    /**
     * @return 弾薬
     */
    public int getAmmo() {
        return this.ammo;
    }

    /**
     * @return 鋼材
     */
    public int getMetal() {
        return this.metal;
    }

    /**
     * @return ボーキサイト
     */
    public int getBauxite() {
        return this.bauxite;
    }

    /**
     * @return 開発資材
     */
    public Integer getResearchMaterials() {
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
    public int getFreeDock() {
        return this.freeDock != null ? this.freeDock : 0;
    }

    /**
     * @return 空きドックをセットする
     */
    public void setFreeDock(Integer freeDock) {
        if (this.freeDock == null) {
            this.freeDock = freeDock;
        }
    }
}
