/**
 * 
 */
package logbook.dto;

import javax.json.JsonObject;

/**
 * @author Nekopanda
 *
 */
public class BasicInfoDto extends JsonData {

    public BasicInfoDto(JsonObject json) {
        super(json);
    }

    /**
     * ユーザのニックネーム
     * @return nickname
     */
    public String getNickname() {
        return this.json.getString("api_nickname");
    }

    /**
     * ユーザのメンバID
     * @return memberId
     */
    public long getMemberId() {
        return Long.parseLong(this.json.getString("api_member_id"));
    }

    /**
     * 開放された艦隊の数
     * @return deckCount
     */
    public int getDeckCount() {
        return this.json.getInt("api_count_deck");
    }

    /**
     * 開放された建造ドックの数
     * @return kdockCount
     */
    public int getKdockCount() {
        return this.json.getInt("api_count_kdock");
    }

    /**
     * 開放された入渠ドックの数
     * @return ndockCount
     */
    public int getNdockCount() {
        return this.json.getInt("api_count_ndock");
    }

    /**
     * 遠征出撃回数
     * @return missionCount
     */
    public int getMissionCount() {
        return this.json.getInt("api_ms_count");
    }

    /**
     * 遠征成功数
     * @return missionSuccess
     */
    public int getMissionSuccess() {
        return this.json.getInt("api_ms_success");
    }

    /**
     * 演習勝利数
     * @return practiceWin
     */
    public int getPracticeWin() {
        return this.json.getInt("api_pt_win");
    }

    /**
     * 演習敗北数
     * @return practiceLose
     */
    public int getPracticeLose() {
        return this.json.getInt("api_pt_lose");
    }

    /**
     * 出撃勝利数
     * @return sortieWin
     */
    public int getSortieWin() {
        return this.json.getInt("api_st_win");
    }

    /**
     * 出撃敗北数
     * @return sortieLose
     */
    public int getSortieLose() {
        return this.json.getInt("api_st_lose");
    }

    /**
     * 提督経験値
     * @return experience
     */
    public long getExperience() {
        return this.json.getInt("api_experience");
    }

    /**
     * 艦隊司令部Lv.
     * @return lv
     */
    public long getLevel() {
        return this.json.getInt("api_level");
    }
}
