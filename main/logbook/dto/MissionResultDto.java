package logbook.dto;

import java.util.Calendar;
import java.util.Date;

/**
 * 遠征の結果を表します
 *
 */
public final class MissionResultDto extends AbstractDto {

    /** 日付 */
    private final Date date;

    /** 失敗・成功 */
    private String clearResult;

    /** 遠征名 */
    private String questName;

    /** 燃料 */
    private int fuel;

    /** 弾薬 */
    private int ammo;

    /** 鋼材 */
    private int metal;

    /** ボーキイト */
    private int bauxite;

    /** アイテム */
    private ResourceItemDto items;

    /**
     * コンストラクター
     */
    public MissionResultDto() {
        this.date = Calendar.getInstance().getTime();
    }

    /**
     * 日付を取得する
     * 
     * @return 日付
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * 失敗・成功を取得する
     * 
     * @return 失敗・成功
     */
    public String getClearResult() {
        return this.clearResult;
    }

    /**
     * 失敗・成功をセットする
     * 
     * @param 失敗・成功フラグ
     */
    public void setClearResult(int clearResult) {
        this.clearResult = "成功";

        if (clearResult == 0) {
            this.clearResult = "失敗";
        } else if (clearResult == 2) {
            this.clearResult = "大成功";
        }
    }

    /**
     * 遠征名を取得する
     * 
     * @return 遠征名
     */
    public String getQuestName() {
        return this.questName;
    }

    /**
     * 遠征名をセットする
     * 
     * @param 遠征名
     */
    public void setQuestName(String questName) {
        this.questName = questName;
    }

    /**
     * 燃料を取得する
     * 
     * @return 燃料
     */
    public int getFuel() {
        return this.fuel;
    }

    /**
     * 燃料をセットする
     * 
     * @param 燃料
     */
    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    /**
     * 弾薬を取得する
     * 
     * @return 弾薬
     */
    public int getAmmo() {
        return this.ammo;
    }

    /**
     * 弾薬をセットする
     * 
     * @param 弾薬
     */
    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }

    /**
     * 鋼材を取得する
     * 
     * @return 鋼材
     */
    public int getMetal() {
        return this.metal;
    }

    /**
     * 鋼材をセットする
     * 
     * @param 鋼材
     */
    public void setMetal(int metal) {
        this.metal = metal;
    }

    /**
     * ボーキイトを取得する
     * 
     * @return ボーキイト
     */
    public int getBauxite() {
        return this.bauxite;
    }

    /**
     * ボーキイトをセットする
     * 
     * @param ボーキイト
     */
    public void setBauxite(int bauxite) {
        this.bauxite = bauxite;
    }

    /**
     * @return item
     */
    public ResourceItemDto getItems() {
        return this.items;
    }

    /**
     * @param item セットする item
     */
    public void setItems(ResourceItemDto items) {
        this.items = items;
    }
}
