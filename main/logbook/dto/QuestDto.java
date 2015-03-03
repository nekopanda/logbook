package logbook.dto;

import java.util.Date;

/**
 * 任務を表します
 *
 */
public final class QuestDto extends AbstractDto {

    private Date time;

    /** api_no */
    private int no;

    private int page;

    private int pos;

    /** api_category */
    private int category;

    /** api_type */
    private int type;

    /** api_state (1:特になし 2:遂行中 3:完了) */
    private int state;

    /** api_title */
    private String title;

    /** api_detail */
    private String detail;

    /** 燃料 */
    private String fuel;

    /** 弾薬 */
    private String ammo;

    /** 鋼材 */
    private String metal;

    /** ボーキサイト */
    private String bauxite;

    /** api_bonus_flag */
    private int bonusFlag;

    /** api_progress_flag (0:50%未満, 1:50%以上, 2:80%以上) */
    private int progressFlag;

    public String getStateString() {
        switch (this.state) {
        case 2:
            return "遂行中";
        case 3:
            return "達成";
        default:
            return "";
        }
    }

    public String getProgressString() {
        switch (this.progressFlag) {
        case 1:
            return "50%";
        case 2:
            return "80%";
        default:
            return "";
        }
    }

    /**
     * @return time
     */
    public Date getTime() {
        return this.time;
    }

    /**
     * @param time セットする time
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * api_noを取得します。
     * @return api_no
     */
    public int getNo() {
        return this.no;
    }

    /**
     * api_noを設定します。
     * @param no api_no
     */
    public void setNo(int no) {
        this.no = no;
    }

    /**
     * api_categoryを取得します。
     * @return api_category
     */
    public int getCategory() {
        return this.category;
    }

    /**
     * api_categoryを設定します。
     * @param category api_category
     */
    public void setCategory(int category) {
        this.category = category;
    }

    /**
     * api_typeを取得します。
     * @return api_type
     */
    public int getType() {
        return this.type;
    }

    /**
     * api_typeを設定します。
     * @param type api_type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * api_stateを取得します。
     * @return api_state
     */
    public int getState() {
        return this.state;
    }

    /**
     * api_stateを設定します。
     * @param state api_state
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * api_titleを取得します。
     * @return api_title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * api_titleを設定します。
     * @param title api_title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * api_detailを取得します。
     * @return api_detail
     */
    public String getDetail() {
        return this.detail;
    }

    /**
     * api_detailを設定します。
     * @param detail api_detail
     */
    public void setDetail(String detail) {
        this.detail = detail;
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
     * api_bonus_flagを取得します。
     * @return api_bonus_flag
     */
    public int getBonusFlag() {
        return this.bonusFlag;
    }

    /**
     * api_bonus_flagを設定します。
     * @param bonusFlag api_bonus_flag
     */
    public void setBonusFlag(int bonusFlag) {
        this.bonusFlag = bonusFlag;
    }

    /**
     * api_progress_flagを取得します。
     * @return api_progress_flag
     */
    public int getProgressFlag() {
        return this.progressFlag;
    }

    /**
     * api_progress_flagを設定します。
     * @param progressFlag api_progress_flag
     */
    public void setProgressFlag(int progressFlag) {
        this.progressFlag = progressFlag;
    }

    /**
     * @return page
     */
    public int getPage() {
        return this.page;
    }

    /**
     * @param page セットする page
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return pos
     */
    public int getPos() {
        return this.pos;
    }

    /**
     * @param pos セットする pos
     */
    public void setPos(int pos) {
        this.pos = pos;
    }

}
