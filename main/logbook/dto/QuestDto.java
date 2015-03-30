package logbook.dto;

import java.util.Date;

import javax.json.JsonObject;

/**
 * 任務を表します
 *
 */
public final class QuestDto extends JsonData {

    private Date time = new Date();

    private int page;

    private int pos;

    public QuestDto(JsonObject json, int page, int pos) {
        super(json);
        this.page = page;
        this.pos = pos;
    }

    public String getStateString() {
        switch (this.getState()) {
        case 2:
            return "遂行中";
        case 3:
            return "達成";
        default:
            return "";
        }
    }

    public String getProgressString() {
        switch (this.getProgressFlag()) {
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
        return this.json.getInt("api_no");
    }

    /**
     * api_categoryを取得します。
     * @return api_category
     */
    public int getCategory() {
        return this.json.getInt("api_category");
    }

    /**
     * api_typeを取得します。
     * @return api_type
     */
    public int getType() {
        return this.json.getInt("api_type");
    }

    /**
     * api_stateを取得します。
     * @return api_state
     */
    public int getState() {
        return this.json.getInt("api_state");
    }

    /**
     * api_titleを取得します。
     * @return api_title
     */
    public String getTitle() {
        return this.json.getString("api_title");
    }

    /**
     * api_detailを取得します。
     * @return api_detail
     */
    public String getDetail() {
        return this.json.getString("api_detail");
    }

    /**
     * 燃料を取得します。
     * @return 燃料
     */
    public int getFuel() {
        return this.json.getJsonArray("api_get_material").getInt(0);
    }

    /**
     * 弾薬を取得します。
     * @return 弾薬
     */
    public int getAmmo() {
        return this.json.getJsonArray("api_get_material").getInt(1);
    }

    /**
     * 鋼材を取得します。
     * @return 鋼材
     */
    public int getMetal() {
        return this.json.getJsonArray("api_get_material").getInt(2);
    }

    /**
     * ボーキサイトを取得します。
     * @return ボーキサイト
     */
    public int getBauxite() {
        return this.json.getJsonArray("api_get_material").getInt(3);
    }

    /**
     * api_bonus_flagを取得します。
     * @return api_bonus_flag
     */
    public int getBonusFlag() {
        return this.json.getInt("api_bonus_flag");
    }

    /**
     * api_progress_flag (0:50%未満, 1:50%以上, 2:80%以上) を取得します。
     * @return api_progress_flag
     */
    public int getProgressFlag() {
        return this.json.getInt("api_progress_flag");
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
