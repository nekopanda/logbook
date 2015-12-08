/**
 * 
 */
package logbook.dto;

import javax.json.JsonObject;

import logbook.internal.EnemyData;
import logbook.internal.MasterData;

import com.dyuproject.protostuff.Tag;

/**
 * @author Nekopanda
 *
 */
public class MapCellDto implements Comparable<MapCellDto> {

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
    @Tag(10)
    private boolean start;
    @Tag(11)
    private int eventId = -1;
    @Tag(12)
    private int eventKind;

    public MapCellDto(JsonObject object, boolean start) {
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
        this.eventId = object.getInt("api_event_id");
        this.eventKind = object.getInt("api_event_kind");
        this.enemyData = EnemyData.get(this.enemyId);
        this.start = start;
    }

    private String getNextKind() {
        if (this.eventId == -1) {
            if (this.isBoss()) {
                return "ボス";
            }
            return null;
        }

        switch (this.eventId) {
        case 2:
            return "資源獲得";
        case 3:
            return "渦潮";
        case 4:
            return "戦闘";
        case 5:
            return "ボス";
        case 6:
            switch (this.eventKind) {
            case 1:
                return "敵影を見ず";
            case 2:
                return "能動分岐";
            }
            return "気のせい";
        case 7:
            if (this.eventKind == 0) {
                return "航空偵察";
            }
            return "航空戦";
        case 8:
            return "船団護衛成功";
        case 9:
            return "揚陸地点";
        }

        return null;
    }

    private String toString(boolean detailed, boolean withBoss) {
        String ret = "マップ:" + this.map[0] + "-" + this.map[1] + " セル:" + this.map[2];
        if (detailed) {
            MasterData.MapInfoDto mapInfo = MasterData.getMapInfo(this.map[0], this.map[1]);
            if (mapInfo != null) {
                String mapName = mapInfo.getName();
                ret = "マップ: " + mapName + "(" + this.map[0] + "-" + this.map[1] + ") セル:" + this.map[2];
            }
        }
        if (withBoss) {
            String next = this.getNextKind();
            if (next != null) {
                ret += " (" + next + ")";
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        return this.toString(false, true);
    }

    public String getReportString() {
        return this.toString(false, false);
    }

    public String detailedString() {
        return this.toString(true, true);
    }

    public String getAreaString() {
        String ret = this.map[0] + "-" + this.map[1];
        MasterData.MapInfoDto mapInfo = MasterData.getMapInfo(this.map[0], this.map[1]);
        if (mapInfo != null) {
            ret += " (" + mapInfo.getName() + ")";
        }
        return ret;
    }

    @Override
    public int compareTo(MapCellDto arg0) {
        int ret = 0;
        for (int i = 0; (i < 3) && (ret == 0); ++i) {
            ret = Integer.compare(this.map[i], arg0.map[i]);
        }
        return ret;
    }

    /** @return area * 10 + no */
    public int getAreaId() {
        return (this.map[0] * 10) + this.map[1];
    }

    /**
     * マップ
     * 3-2-1レベリングのポイントだったら[3,2,2]
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
     * エネミーID
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
     * 色
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

    /**
     * 出撃直後か？
     * @return start
     */
    public boolean isStart() {
        return this.start;
    }

    /**
     * @param start セットする start
     */
    public void setStart(boolean start) {
        this.start = start;
    }

    /**
     * @return eventId
     */
    public int getEventId() {
        return this.eventId;
    }

    /**
     * @param eventId セットする eventId
     */
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    /**
     * @return eventKind
     */
    public int getEventKind() {
        return this.eventKind;
    }

    /**
     * @param eventKind セットする eventKind
     */
    public void setEventKind(int eventKind) {
        this.eventKind = eventKind;
    }

}
