/**
 * 
 */
package logbook.dto;

import javax.json.JsonObject;

import logbook.config.MasterDataConfig;
import logbook.internal.EnemyData;
import logbook.internal.MasterData;
import logbook.proto.LogbookEx.MapCellDtoPb;
import logbook.proto.Tag;

/**
 * @author Nekopanda
 *
 */
public class MapCellDto {

    /** マップ */
    @Tag(1)
    private int[] map = new int[3];
    /** 敵ID */
    @Tag(2)
    private int enemyId;
    /** マスの色 */
    @Tag(3)
    private int colorNo;
    /** ボスマス */
    @Tag(4)
    private int bosscellNo;
    @Tag(5)
    private EnemyData enemyData;

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
        this.enemyData = EnemyData.get(this.enemyId);
    }

    public MapCellDtoPb toProto() {
        MapCellDtoPb.Builder builder = MapCellDtoPb.newBuilder();
        if (this.map != null) {
            for (int b : this.map) {
                builder.addMap(b);
            }
        }
        builder.setEnemyId(this.enemyId);
        builder.setColorNo(this.colorNo);
        builder.setBosscellNo(this.bosscellNo);
        if (this.enemyData != null) {
            builder.setEnemyData(this.enemyData.toProto());
        }
        return builder.build();
    }

    private String toString(boolean detailed) {
        int id = (this.map[0] * 10) + this.map[1];
        String ret = "マップ:" + this.map[0] + "-" + this.map[1] + " セル:" + this.map[2];
        if (detailed) {
            MasterData.MapInfoDto mapInfo = MasterDataConfig.get().getMapinfo().get(id);
            if (mapInfo != null) {
                String mapName = mapInfo.getName();
                ret = "マップ: " + mapName + "(" + this.map[0] + "-" + this.map[1] + ") セル:" + this.map[2];
            }
        }
        if (this.enemyId != -1) {
            if (this.isBoss()) {
                ret += " (ボス)";
            }
            ret += " e_id:" + this.enemyId;
        }
        return ret;
    }

    @Override
    public String toString() {
        return this.toString(false);
    }

    public String detailedString() {
        return this.toString(true);
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
        return (this.bosscellNo == this.map[2]) || (this.colorNo == 5);
    }

    /**
     * @return enemyData
     */
    public EnemyData getEnemyData() {
        return this.enemyData;
    }

    /**
     * @param enemyData セットする enemyData
     */
    public void setEnemyData(EnemyData enemyData) {
        this.enemyData = enemyData;
    }

}
