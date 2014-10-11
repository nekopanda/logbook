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
    private final String clearResult;

    /** 遠征名 */
    private final String questName;

    /** アイテム */
    private final ResourceItemDto res;

    /**
     * コンストラクター
     */
    public MissionResultDto(int clearResult, String questName, ResourceItemDto res) {
        this.date = Calendar.getInstance().getTime();
        this.clearResult = toClearResult(clearResult);
        this.questName = questName;
        this.res = res;
    }

    public MissionResultDto(Date date, String clearResult, String questName, ResourceItemDto res) {
        this.date = date;
        this.clearResult = clearResult;
        this.questName = questName;
        this.res = res;
    }

    private static String toClearResult(int flag) {
        if (flag == 0) {
            return "失敗";
        } else if (flag == 2) {
            return "大成功";
        }
        return "成功";
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
     * 遠征名を取得する
     * 
     * @return 遠征名
     */
    public String getQuestName() {
        return this.questName;
    }

    /**
     * 燃料を取得する
     * 
     * @return 燃料
     */
    public int getFuel() {
        return this.res.getFuel();
    }

    /**
     * 弾薬を取得する
     * 
     * @return 弾薬
     */
    public int getAmmo() {
        return this.res.getAmmo();
    }

    /**
     * 鋼材を取得する
     * 
     * @return 鋼材
     */
    public int getMetal() {
        return this.res.getMetal();
    }

    /**
     * ボーキイトを取得する
     * 
     * @return ボーキイト
     */
    public int getBauxite() {
        return this.res.getBauxite();
    }

    /**
     * @return item
     */
    public ResourceItemDto getResources() {
        return this.res;
    }
}
