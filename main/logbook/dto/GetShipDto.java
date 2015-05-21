package logbook.dto;

import java.util.Calendar;
import java.util.Date;

/**
 * 建造した艦娘を表します
 */
public final class GetShipDto extends AbstractDto {

    /** 日付 */
    private final Date getDate;

    /** 種類(大型艦建造:1, 通常艦建造:0) */
    private final boolean oogata;

    /** 建造した艦娘 */
    private String name;
    private int shipId;
    private String type;

    /** 投入資源 */
    private final ResourceItemDto resources;

    /** 秘書艦 */
    private final String secretly;
    private final int secretlyId;

    /** 司令部Lv */
    private final int hqLevel;

    /** 空きドック */
    private int freeDock;

    /**
     * コンストラクター
     * 
     * @param object 艦娘情報
     * @param resources 投入資源
     */
    public GetShipDto(boolean oogata, ResourceItemDto resources, ShipDto secretly, int hqLevel,
            int freeDock) {
        this.getDate = Calendar.getInstance().getTime();
        this.oogata = oogata;
        this.name = null;
        this.shipId = -1;
        this.type = null;
        this.resources = resources;
        this.secretly = secretly.getFriendlyName();
        this.secretlyId = secretly.getId();
        this.hqLevel = hqLevel;
        this.freeDock = freeDock;
    }

    public GetShipDto(Date date, String buildType, String name, String type, ResourceItemDto resources,
            String secretly, int hqLevel, int freeDock) {

        this.getDate = date;
        this.oogata = buildType.startsWith("大型");
        this.name = name;
        this.shipId = -1;
        this.type = type;
        this.resources = resources;
        this.secretly = secretly;
        this.secretlyId = -1;
        this.hqLevel = hqLevel;
        this.freeDock = freeDock;
    }

    /**
     * @return 建造日時
     */
    public Date getGetDate() {
        return this.getDate;
    }

    public void setShip(ShipInfoDto ship) {
        if (ship != null) {
            this.name = ship.getName();
            this.shipId = ship.getShipId();
            this.type = ship.getType();
        }
    }

    /**
     * @return oogata
     */
    public boolean isOogata() {
        return this.oogata;
    }

    /**
     * @return 名前
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return shipId
     */
    public int getShipId() {
        return this.shipId;
    }

    /**
     * @return 艦種
     */
    public String getType() {
        return this.type;
    }

    /**
     * @return 建造種類
     */
    public String getBuildType() {
        if (this.oogata) {
            return "大型艦建造";
        }
        return "通常艦建造";
    }

    /**
     * @return 燃料
     */
    public int getFuel() {
        return this.resources.getFuel();
    }

    /**
     * @return 弾薬
     */
    public int getAmmo() {
        return this.resources.getAmmo();
    }

    /**
     * @return 鋼材
     */
    public int getMetal() {
        return this.resources.getMetal();
    }

    /**
     * @return ボーキサイト
     */
    public int getBauxite() {
        return this.resources.getBauxite();
    }

    /**
     * @return 開発資材
     */
    public Integer getResearchMaterials() {
        return this.resources.getResearch();
    }

    /**
     * @return 空きドック
     */
    public int getFreeDock() {
        return this.freeDock;
    }

    /**
     * @param freeDock 空きドック
     */
    public void setFreeDock(int freeDock) {
        this.freeDock = freeDock;
    }

    /**
     * @return 秘書艦
     */
    public String getSecretary() {
        return this.secretly;
    }

    /**
     * @return secretlyId
     */
    public int getSecretlyId() {
        return this.secretlyId;
    }

    /**
     * @return 司令部Lv
     */
    public int getHqLevel() {
        return this.hqLevel;
    }
}
