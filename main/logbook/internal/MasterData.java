/**
 * 
 */
package logbook.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * @author Nekopanda
 *
 */
public class MasterData {

    /** 1-, 2-, ... , イベント海域 */
    private ArrayList<MapAreaDto> maparea = new ArrayList<MapAreaDto>();

    /** 1-1, 1-2, ... , 2-1, 2-2, ... */
    private Map<Integer, MapInfoDto> mapinfo = new HashMap<Integer, MapInfoDto>();

    /** 全ての任務  */
    private Map<Integer, MissionDto> mission = new HashMap<Integer, MissionDto>();

    /** マップクリア情報 -1: 非表示 or 未更新 0: クリアしていない 1: クリア済み */
    private Map<Integer, Integer> mapStatus = new HashMap<Integer, Integer>();

    /** 遠征クリア情報 -1: 非表示 or 未更新 0: NEW 1: 無印 2:　達成 */
    private Map<Integer, Integer> missionStatus = new HashMap<Integer, Integer>();

    /** 艦種 */
    private ArrayList<ShipTypeDto> stype = new ArrayList<ShipTypeDto>();

    /** 最終更新日時 */
    private Date lastUpdateTime = new Date();

    public MasterData() {
    }

    public void doMater(JsonObject json_data) {

        JsonArray json_maparea = json_data.getJsonArray("api_mst_maparea");
        if (json_maparea != null) {
            this.maparea.clear();
            for (JsonValue elem : json_maparea) {
                this.maparea.add(new MapAreaDto((JsonObject) elem));
            }
        }

        JsonArray json_mapinfo = json_data.getJsonArray("api_mst_mapinfo");
        if (json_mapinfo != null) {
            this.mapinfo.clear();
            for (JsonValue elem : json_mapinfo) {
                MapInfoDto dto = new MapInfoDto((JsonObject) elem);
                this.mapinfo.put(dto.getId(), dto);
            }
        }

        JsonArray json_mission = json_data.getJsonArray("api_mst_mission");
        if (json_mission != null) {
            this.mission.clear();
            for (JsonValue elem : json_mission) {
                MissionDto dto = new MissionDto((JsonObject) elem);
                this.mission.put(dto.getId(), dto);
            }
        }

        JsonArray json_stype = json_data.getJsonArray("api_mst_stype");
        if (json_stype != null) {
            this.getStype().clear();
            for (JsonValue elem : json_stype) {
                this.stype.add(new ShipTypeDto((JsonObject) elem));
            }
        }

        this.lastUpdateTime = new Date();
    }

    public void doMapInfo(JsonArray json_mapinfo) {
        if (json_mapinfo != null) {
            Map<Integer, Integer> newStatus = new HashMap<Integer, Integer>();
            for (JsonValue elem : json_mapinfo) {
                JsonObject obj = (JsonObject) elem;
                newStatus.put(obj.getInt("api_id"), obj.getInt("api_cleared"));
            }
            if (newStatus.equals(this.mapStatus) == false) {
                this.mapStatus = newStatus;
                this.lastUpdateTime = new Date();
            }
        }
    }

    public void doMission(JsonArray json_mission) {
        if (json_mission != null) {
            Map<Integer, Integer> newStatus = new HashMap<Integer, Integer>();
            for (JsonValue elem : json_mission) {
                JsonObject obj = (JsonObject) elem;
                newStatus.put(obj.getInt("api_mission_id"), obj.getInt("api_state"));
            }
            if (newStatus.equals(this.missionStatus) == false) {
                this.missionStatus = newStatus;
                this.lastUpdateTime = new Date();
            }
        }
    }

    /**
     * @return maparea
     */
    public ArrayList<MapAreaDto> getMaparea() {
        return this.maparea;
    }

    /**
     * @param maparea セットする maparea
     */
    public void setMaparea(ArrayList<MapAreaDto> maparea) {
        this.maparea = maparea;
    }

    /**
     * @return mapinfo
     */
    public Map<Integer, MapInfoDto> getMapinfo() {
        return this.mapinfo;
    }

    /**
     * @param mapinfo セットする mapinfo
     */
    public void setMapinfo(Map<Integer, MapInfoDto> mapinfo) {
        this.mapinfo = mapinfo;
    }

    /**
     * @return mission
     */
    public Map<Integer, MissionDto> getMission() {
        return this.mission;
    }

    /**
     * @param mission セットする mission
     */
    public void setMission(Map<Integer, MissionDto> mission) {
        this.mission = mission;
    }

    /**
     * @return stype
     */
    public ArrayList<ShipTypeDto> getStype() {
        return this.stype;
    }

    /**
     * @param stype セットする stype
     */
    public void setStype(ArrayList<ShipTypeDto> stype) {
        this.stype = stype;
    }

    /**
     * @return mapStatus
     */
    public Map<Integer, Integer> getMapStatus() {
        return this.mapStatus;
    }

    /**
     * @param mapStatus セットする mapStatus
     */
    public void setMapStatus(Map<Integer, Integer> mapStatus) {
        this.mapStatus = mapStatus;
    }

    /**
     * @return missionStatus
     */
    public Map<Integer, Integer> getMissionStatus() {
        return this.missionStatus;
    }

    /**
     * @param missionStatus セットする missionStatus
     */
    public void setMissionStatus(Map<Integer, Integer> missionStatus) {
        this.missionStatus = missionStatus;
    }

    /**
     * @return lastUpdateTime
     */
    public Date getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    /**
     * @param lastUpdateTime セットする lastUpdateTime
     */
    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public static class MapAreaDto {
        private int id;
        private String name;

        public MapAreaDto() {
        }

        public MapAreaDto(JsonObject json_data) {
            this.id = json_data.getInt("api_id");
            this.name = json_data.getString("api_name");
        }

        /**
         * @return id
         */
        public int getId() {
            return this.id;
        }

        /**
         * @param id セットする id
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * @return name
         */
        public String getName() {
            return this.name;
        }

        /**
         * @param name セットする name
         */
        public void setName(String name) {
            this.name = name;
        }
    }

    public static class MapInfoDto {
        private int id;
        private int maparea_id;
        private int no;
        private String name;

        public MapInfoDto() {
        }

        public MapInfoDto(JsonObject json_data) {
            this.id = json_data.getInt("api_id");
            this.maparea_id = json_data.getInt("api_maparea_id");
            this.no = json_data.getInt("api_no");
            this.name = json_data.getString("api_name");
        }

        /**
         * @return id
         */
        public int getId() {
            return this.id;
        }

        /**
         * @param id セットする id
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * @return maparea_id
         */
        public int getMaparea_id() {
            return this.maparea_id;
        }

        /**
         * @param maparea_id セットする maparea_id
         */
        public void setMaparea_id(int maparea_id) {
            this.maparea_id = maparea_id;
        }

        /**
         * @return no
         */
        public int getNo() {
            return this.no;
        }

        /**
         * @param no セットする no
         */
        public void setNo(int no) {
            this.no = no;
        }

        /**
         * @return name
         */
        public String getName() {
            return this.name;
        }

        /**
         * @param name セットする name
         */
        public void setName(String name) {
            this.name = name;
        }
    }

    public static class MissionDto {
        private int id;
        private int maparea_id;
        private String name;
        private int time;

        // 

        public MissionDto() {
        }

        public MissionDto(JsonObject json_data) {
            this.id = json_data.getInt("api_id");
            this.maparea_id = json_data.getInt("api_maparea_id");
            this.name = json_data.getString("api_name");
            this.time = json_data.getInt("api_time");
        }

        /**
         * @return id
         */
        public int getId() {
            return this.id;
        }

        /**
         * @param id セットする id
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * @return maparea_id
         */
        public int getMaparea_id() {
            return this.maparea_id;
        }

        /**
         * @param maparea_id セットする maparea_id
         */
        public void setMaparea_id(int maparea_id) {
            this.maparea_id = maparea_id;
        }

        /**
         * @return name
         */
        public String getName() {
            return this.name;
        }

        /**
         * @param name セットする name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return time
         */
        public int getTime() {
            return this.time;
        }

        /**
         * @param time セットする time
         */
        public void setTime(int time) {
            this.time = time;
        }
    }

    public static class ShipTypeDto {
        private String name;
        private int id;
        private ArrayList<Boolean> equipType = new ArrayList<Boolean>();

        public ShipTypeDto() {
        }

        public ShipTypeDto(JsonObject object) {
            this.name = object.getString("api_name");
            this.id = object.getJsonNumber("api_id").intValue();
            JsonObject json_equip_type = object.getJsonObject("api_equip_type");
            for (int i = 1;; ++i) {
                JsonNumber number = json_equip_type.getJsonNumber(String.valueOf(i));
                if (number == null)
                    break;
                this.equipType.add(number.intValue() != 0);
            }
        }

        /**
         * @return name
         */
        public String getName() {
            return this.name;
        }

        /**
         * @param name セットする name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return id
         */
        public int getId() {
            return this.id;
        }

        /**
         * @param id セットする id
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * @return equipType
         */
        public ArrayList<Boolean> getEquipType() {
            return this.equipType;
        }

        /**
         * @param equipType セットする equipType
         */
        public void setEquipType(ArrayList<Boolean> equipType) {
            this.equipType = equipType;
        }
    }
}
