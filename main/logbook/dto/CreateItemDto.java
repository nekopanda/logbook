/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
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
    private final boolean createFlag;

    /** ID */
    private final long id;

    /** 名称 */
    private String name;

    /** 種別 */
    private String type;

    /** 投入資源 */
    private final ResourceDto resources;

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     * @param resources 投入資源
     */
    public CreateItemDto(JsonObject object, ResourceDto resources) {

        this.createDate = Calendar.getInstance().getTime();
        this.createFlag = object.getJsonNumber("api_create_flag").longValue() != 0;
        if (this.createFlag) {
            this.id = object.getJsonNumber("api_id").longValue();
        } else {
            this.id = 0;
        }
        this.resources = resources != null ? resources : new ResourceDto("0", "0", "0", "0", null);
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
     * @return ID
     */
    public long getId() {
        return this.id;
    }

    /**
     * @return 燃料
     */
    public String getFuel() {
        return this.resources.getFuel();
    }

    /**
     * @return 弾薬
     */
    public String getAmmo() {
        return this.resources.getAmmo();
    }

    /**
     * @return 鋼材
     */
    public String getMetal() {
        return this.resources.getMetal();
    }

    /**
     * @return ボーキサイト
     */
    public String getBauxite() {
        return this.resources.getBauxite();
    }

    /**
     * @return 秘書艦
     */
    public String getSecretary() {
        ShipDto ship = this.resources.getSecretary();
        if (ship != null) {
            return ship.getName() + "(Lv" + ship.getLv() + ")";
        }
        return "";
    }
}
