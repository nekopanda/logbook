/**
 * 
 */
package logbook.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.constants.AppConstants;
import logbook.dto.UseItemDto;
import logbook.gui.ApplicationMain;
import logbook.util.BeanUtils;
import logbook.util.JsonUtils;

/**
 * @author Nekopanda
 *
 */
public class MasterData {

    private static class Holder {
        public static MasterData instance = null;
    }

    /** 変更があったか */
    private static boolean modified = false;

    /**
     * 
     * 設定ファイルに書き込みます
     */
    public static void store() throws IOException {
        if (Holder.instance == null) {
            return;
        }
        // 最終更新日時がファイル更新日時より新しい時だけ書き込む
        if (modified) {
            ApplicationMain.sysPrint("マスターファイル更新");
            BeanUtils.writeObject(AppConstants.MASTER_DATA_CONFIG, Holder.instance);
            modified = false;
        }
    }

    private static void load() {
        MasterData masterData = BeanUtils.readObject(AppConstants.MASTER_DATA_CONFIG, MasterData.class);
        if (masterData != null) {
            Holder.instance = masterData;
        }
        else {
            Holder.instance = new MasterData();
        }
    }

    public static final boolean INIT_COMPLETE;
    static {
        load();
        INIT_COMPLETE = true;
    }

    /** 1-, 2-, ... , イベント海域 */
    private ArrayList<MapAreaDto> maparea = new ArrayList<MapAreaDto>();

    /** 1-1, 1-2, ... , 2-1, 2-2, ... */
    private Map<Integer, MapInfoDto> mapinfo = new TreeMap<Integer, MapInfoDto>();

    /** 全ての遠征  */
    private Map<Integer, MissionDto> mission = new TreeMap<Integer, MissionDto>();

    /** マップクリア情報 -1: 非表示 or 未更新 0: クリアしていない 1: クリア済み */
    private Map<Integer, Integer> mapState = new TreeMap<Integer, Integer>();

    /** 遠征クリア情報 -1: 非表示 or 未更新 0: NEW 1: 無印 2:　達成 */
    private Map<Integer, Integer> missionState = new TreeMap<Integer, Integer>();

    /** 艦種 */
    private List<ShipTypeDto> stype = new ArrayList<ShipTypeDto>();

    /** UseItem　（バケツとか、家具箱とか） */
    private Map<Integer, UseItemInfoDto> useItem = new TreeMap<Integer, UseItemInfoDto>();

    /** 最後に START2 によるマスターデータを受け取った日時 */
    private Date masterUpdateTime = new Date(0);

    /** 最終更新日時 */
    private Date lastUpdateTime = new Date(0);

    public MasterData() {
    }

    /** START2のマスターデータで更新 */
    public static void updateMaster(JsonObject data) {
        Holder.instance.doMater(data);
    }

    /** 出撃マップを更新 */
    public static void updateMapInfo(JsonArray json_mapinfo) {
        Holder.instance.doMapInfo(json_mapinfo);
    }

    /** 遠征データを更新 */
    public static void updateMission(JsonArray json_mission) {
        Holder.instance.doMission(json_mission);
    }

    public static MasterData getInstance() {
        return Holder.instance;
    }

    /** 艦種情報を取得 */
    public static ShipTypeDto getShipType(int stype) {
        if ((stype <= 0) || (stype > Holder.instance.stype.size())) {
            return null;
        }
        return Holder.instance.stype.get(stype - 1);
    }

    /** （装備でない）アイテム情報を取得 */
    public static UseItemInfoDto getUseItem(int id) {
        Integer key = id;
        return Holder.instance.useItem.get(key);
    }

    /** マップ情報を取得 */
    public static MapInfoDto getMapInfo(int area, int no) {
        int id = (area * 10) + no;
        return Holder.instance.mapinfo.get(id);
    }

    private void doMater(JsonObject data) {

        JsonArray json_maparea = data.getJsonArray("api_mst_maparea");
        if (json_maparea != null) {
            this.maparea.clear();
            for (JsonValue elem : json_maparea) {
                this.maparea.add(new MapAreaDto((JsonObject) elem));
            }
        }

        JsonArray json_mapinfo = data.getJsonArray("api_mst_mapinfo");
        if (json_mapinfo != null) {
            this.mapinfo.clear();
            for (JsonValue elem : json_mapinfo) {
                MapInfoDto dto = new MapInfoDto((JsonObject) elem);
                this.mapinfo.put(dto.getId(), dto);
            }
        }

        JsonArray json_mission = data.getJsonArray("api_mst_mission");
        if (json_mission != null) {
            this.mission.clear();
            for (JsonValue elem : json_mission) {
                MissionDto dto = new MissionDto((JsonObject) elem);
                this.mission.put(dto.getId(), dto);
            }
        }

        JsonArray json_stype = data.getJsonArray("api_mst_stype");
        if (json_stype != null) {
            this.stype.clear();
            for (JsonValue elem : json_stype) {
                this.stype.add(new ShipTypeDto((JsonObject) elem));
            }
        }

        JsonArray json_useitem = data.getJsonArray("api_mst_useitem");
        if (json_useitem != null) {
            this.useItem.clear();
            for (JsonValue elem : json_useitem) {
                UseItemInfoDto dto = new UseItemInfoDto((JsonObject) elem);
                this.useItem.put(dto.getId(), dto);
            }
        }

        this.masterUpdateTime = new Date();
        this.lastUpdateTime = new Date();
        modified = true;
    }

    private void doMapInfo(JsonArray json_mapinfo) {
        if (json_mapinfo != null) {
            Map<Integer, Integer> newState = new HashMap<Integer, Integer>();
            for (JsonValue elem : json_mapinfo) {
                JsonObject obj = (JsonObject) elem;
                newState.put(obj.getInt("api_id"), obj.getInt("api_cleared"));
            }
            if (newState.equals(this.mapState) == false) {
                this.mapState = newState;
                this.lastUpdateTime = new Date();
                modified = true;
            }
        }
    }

    private void doMission(JsonArray json_mission) {
        if (json_mission != null) {
            Map<Integer, Integer> newState = new HashMap<Integer, Integer>();
            for (JsonValue elem : json_mission) {
                JsonObject obj = (JsonObject) elem;
                newState.put(obj.getInt("api_mission_id"), obj.getInt("api_state"));
            }
            if (newState.equals(this.missionState) == false) {
                this.missionState = newState;
                this.lastUpdateTime = new Date();
                modified = true;
            }
        }
    }

    public String getMissionName(int missionId) {
        MissionDto dto = this.mission.get(missionId);
        if (dto != null) {
            return dto.getName();
        }
        return null;
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
    public List<ShipTypeDto> getStype() {
        return this.stype;
    }

    /**
     * @param stype セットする stype
     */
    public void setStype(List<ShipTypeDto> stype) {
        this.stype = stype;
    }

    /**
     * @return mapState
     */
    public Map<Integer, Integer> getMapState() {
        return this.mapState;
    }

    /**
     * @param mapState セットする mapState
     */
    public void setMapState(Map<Integer, Integer> mapState) {
        this.mapState = mapState;
    }

    /**
     * @return missionState
     */
    public Map<Integer, Integer> getMissionState() {
        return this.missionState;
    }

    /**
     * @param missionState セットする missionState
     */
    public void setMissionState(Map<Integer, Integer> missionState) {
        this.missionState = missionState;
    }

    /**
     * @return masterUpdateTime
     */
    public Date getMasterUpdateTime() {
        return this.masterUpdateTime;
    }

    /**
     * @param masterUpdateTime セットする masterUpdateTime
     */
    public void setMasterUpdateTime(Date masterUpdateTime) {
        this.masterUpdateTime = masterUpdateTime;
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

    /**
     * @return useItem
     */
    public Map<Integer, UseItemInfoDto> getUseItem() {
        return this.useItem;
    }

    /**
     * @param useItem セットする useItem
     */
    public void setUseItem(Map<Integer, UseItemInfoDto> useItem) {
        this.useItem = useItem;
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
        private int mapareaId;
        private int no;
        private String name;

        public MapInfoDto() {
        }

        public MapInfoDto(JsonObject json_data) {
            this.id = json_data.getInt("api_id");
            this.mapareaId = json_data.getInt("api_maparea_id");
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
            return this.mapareaId;
        }

        /**
         * @param mapareaId セットする maparea_id
         */
        public void setMaparea_id(int mapareaId) {
            this.mapareaId = mapareaId;
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
        private UseItemDto[] winItem = new UseItemDto[2];

        // 

        public MissionDto() {
        }

        public MissionDto(JsonObject data) {
            this.id = data.getInt("api_id");
            this.maparea_id = data.getInt("api_maparea_id");
            this.name = data.getString("api_name");
            this.time = data.getInt("api_time");
            this.winItem[0] = new UseItemDto(data.getJsonArray("api_win_item1"));
            this.winItem[1] = new UseItemDto(data.getJsonArray("api_win_item2"));
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
         * @param mapareaId セットする maparea_id
         */
        public void setMaparea_id(int mapareaId) {
            this.maparea_id = mapareaId;
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

        /**
         * @return winItem
         */
        public UseItemDto[] getWinItem() {
            return this.winItem;
        }

        /**
         * @param winItem セットする winItem
         */
        public void setWinItem(UseItemDto[] winItem) {
            this.winItem = winItem;
        }
    }

    public static class ShipTypeDto {
        private String name;
        private int id;
        private ArrayList<Boolean> equipType = new ArrayList<Boolean>();
        private String json;

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
            this.json = object.toString();
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

        /**
         * @return json
         */
        public JsonObject getJson() {
            return JsonUtils.fromString(this.json);
        }
    }

    public static class UseItemInfoDto {
        private int id;
        private int usetype;
        private String name;
        private String description;

        public UseItemInfoDto() {
        }

        public UseItemInfoDto(JsonObject object) {
            this.setId(object.getJsonNumber("api_id").intValue());
            this.setUsetype(object.getJsonNumber("api_usetype").intValue());
            this.setName(object.getString("api_name"));
            this.setDescription(object.getJsonArray("api_description").getString(0));
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
         * @return usetype
         */
        public int getUsetype() {
            return this.usetype;
        }

        /**
         * @param usetype セットする usetype
         */
        public void setUsetype(int usetype) {
            this.usetype = usetype;
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
         * @return description
         */
        public String getDescription() {
            return this.description;
        }

        /**
         * @param description セットする description
         */
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
