/**
 * 
 */
package logbook.dto;

import javax.json.JsonObject;

/**
 * @author Nekopanda
 *
 */
public class MapCellDto {

    /** マップ */
    private int[] map = new int[3];

    /** 敵ID */
    private int enemyId;

    /** マスの色 */
    private int colorNo;

    /** ボスマス */
    private int bosscellNo;

    public MapCellDto(JsonObject object) {
        this.map[0] = object.getInt("api_maparea_id");
        this.map[1] = object.getInt("api_mapinfo_no");
        this.map[2] = object.getInt("api_no");
        JsonObject enemydata = object.getJsonObject("api_enemy");
        if (enemydata != null) {
            this.enemyId = enemydata.getInt("api_enemy_id");
        }
        else {
            this.enemyId = -1;
        }
        this.colorNo = object.getInt("api_color_no");
        this.bosscellNo = object.getInt("api_bosscell_no");
    }

    @Override
    public String toString() {
        String ret = "行先 マップ:" + this.map[0] + "-" + this.map[1] + " セル:" + this.map[2];
        if (this.enemyId != -1) {
            if (this.bosscellNo == this.map[2]) {
                ret += " (ボス)";
            }
            ret += " e_id:" + this.enemyId;
        }
        return ret;
    }

    /**
     * @return map
     */
    public int[] getMap() {
        return this.map;
    }

    /**
     * @param map セットする map
     */
    public void setMap(int[] map) {
        this.map = map;
    }

    /**
     * @return enemyId
     */
    public int getEnemyId() {
        return this.enemyId;
    }

    /**
     * @param enemyId セットする enemyId
     */
    public void setEnemyId(int enemyId) {
        this.enemyId = enemyId;
    }

    /**
     * @return colorId
     */
    public int getColorNo() {
        return this.colorNo;
    }

    /**
     * @param colorId セットする colorId
     */
    public void setColorNo(int colorNo) {
        this.colorNo = colorNo;
    }

    /**
     * @return bosscellNo
     */
    public int getBosscellNo() {
        return this.bosscellNo;
    }

    /**
     * @param bosscellNo セットする bosscellNo
     */
    public void setBosscellNo(int bosscellNo) {
        this.bosscellNo = bosscellNo;
    }

    public boolean isBoss() {
        return this.bosscellNo == this.map[2];
    }

}
