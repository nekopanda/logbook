package logbook.dto;

import java.util.Calendar;
import java.util.Date;

import javax.json.JsonObject;

/**
 * 開発した装備を表します
 */
public final class CreateItemDto extends AbstractDto {

    /** 日付 */
    private final Date createDate;

    /** 成功フラグ */
    private boolean createFlag;

    /** 名称 */
    private String name;

    /** 種別 */
    private String type;

    /** 投入資源 */
    private final ResourceItemDto resources;

    /** 秘書艦 */
    private final String secretly;

    /** 司令部Lv */
    private final int hqLevel;

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     * @param resources 投入資源
     */
    public CreateItemDto(JsonObject object, ResourceItemDto resources, ShipDto recretly, int hqLevel) {

        this.createDate = Calendar.getInstance().getTime();
        this.createFlag = object.getJsonNumber("api_create_flag").longValue() != 0;
        this.resources = resources;
        this.secretly = recretly.getFriendlyName();
        this.hqLevel = hqLevel;
    }

    public CreateItemDto(Date date, boolean createFlag, String name, String type, ResourceItemDto resources,
            String secretly, int hqLevel) {
        this.createDate = date;
        this.createFlag = createFlag;
        this.name = name;
        this.type = type;
        this.resources = resources;
        this.secretly = secretly;
        this.hqLevel = hqLevel;
    }

    /**
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 種別
     */
    public String getType() {
        return this.type;
    }

    /**
     * @param type 種別
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return 日付
     */
    public Date getCreateDate() {
        return this.createDate;
    }

    /**
     * @return 成功フラグ
     */
    public boolean isCreateFlag() {
        return this.createFlag;
    }

    /**
     * @param flag 成功フラグ
     */
    public void setCreateFlag(boolean flag) {
        this.createFlag = flag;
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
     * @return 秘書艦
     */
    public String getSecretary() {
        return this.secretly;
    }

    /**
     * @return 司令部Lv
     */
    public int getHqLevel() {
        return this.hqLevel;
    }
}
