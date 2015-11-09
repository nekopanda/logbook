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
import logbook.dto.ItemInfoDto;
import logbook.dto.JsonData;
import logbook.dto.ShipInfoDto;
import logbook.dto.UseItemDto;
import logbook.gui.ApplicationMain;
import logbook.util.BeanUtils;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Nekopanda
 *
 */
public class MasterData {

    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(MasterData.class);

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
        try {
            MasterData masterData = BeanUtils.readObject(AppConstants.MASTER_DATA_CONFIG, MasterData.class);
            if ((masterData != null) && (masterData.getVersion() >= 2)) {
                Holder.instance = masterData;
                Holder.instance.start2.loadCompleted();
            }
        } catch (Exception e) {
            LOG.get().warn("艦娘のIDと名前の紐付けを設定ファイルから読み込みますに失敗しました", e);
        }
        if (Holder.instance == null) {
            Holder.instance = new MasterData();
        }
    }

    public static final boolean INIT_COMPLETE;
    static {
        load();
        INIT_COMPLETE = true;
    }

    /** バージョン
     * 0: 初期
     * 1: 1.6.3以降
     * 2: 1.8.7以降
     */
    private int version = 2;

    private Start2Dto start2 = new Start2Dto();

    /** マップクリア情報 -1: 非表示 or 未更新 0: クリアしていない 1: クリア済み */
    private Map<Integer, Integer> mapState = new TreeMap<Integer, Integer>();

    /** 遠征クリア情報 -1: 非表示 or 未更新 0: NEW 1: 無印 2:　達成 */
    private Map<Integer, Integer> missionState = new TreeMap<Integer, Integer>();

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

    public static MasterData get() {
        return Holder.instance;
    }

    public static Start2Dto getMaster() {
        return Holder.instance.getStart2();
    }

    private void doMater(JsonObject data) {
        this.start2 = new Start2Dto(data);
        this.lastUpdateTime = new Date();
        modified = true;
    }

    /** 艦種情報を取得 */
    public static ShipTypeDto getShipType(int stype) {
        return Holder.instance.start2.getShipType(stype);
    }

    /** （装備でない）アイテム情報を取得 */
    public static UseItemInfoDto getUseItem(int id) {
        return Holder.instance.start2.getUseItem(id);
    }

    /** マップ情報を取得 */
    public static MapInfoDto getMapInfo(int area, int no) {
        return Holder.instance.start2.getMapInfo(area, no);
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
        return this.start2.getTime();
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
     * @return version
     */
    public int getVersion() {
        return this.version;
    }

    /**
     * @param version セットする version
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @return start2
     */
    public Start2Dto getStart2() {
        return this.start2;
    }

    /**
     * @param start2 セットする start2
     */
    public void setStart2(Start2Dto start2) {
        this.start2 = start2;
    }

    /**
     * api_start2
     * @author Nekopanda
     */
    public class Start2Dto extends JsonData {

        /** 最終更新日時 */
        private Date time = new Date(0);

        /** 艦娘 */
        private final Map<Integer, ShipInfoDto> ships = new TreeMap<>();

        /** アイテム */
        private final Map<Integer, ItemInfoDto> items = new TreeMap<>();

        /** 1-, 2-, ... , イベント海域 */
        private final ArrayList<MapAreaDto> maparea = new ArrayList<MapAreaDto>();

        /** 1-1, 1-2, ... , 2-1, 2-2, ... */
        private final Map<Integer, MapInfoDto> mapinfo = new TreeMap<Integer, MapInfoDto>();

        /** 全ての遠征  */
        private final Map<Integer, MissionDto> mission = new TreeMap<Integer, MissionDto>();

        /** 艦種 */
        private final List<ShipTypeDto> stype = new ArrayList<ShipTypeDto>();

        /** UseItem　（バケツとか、家具箱とか） */
        private final Map<Integer, UseItemInfoDto> useItem = new TreeMap<Integer, UseItemInfoDto>();

        public Start2Dto() {
        }

        public Start2Dto(JsonObject json) {
            super(json);
            this.readJson();
        }

        public void loadCompleted() {
            this.readJson();
        }

        private void readJson() {

            // 艦種
            JsonArray json_stype = this.json.getJsonArray("api_mst_stype");
            if (json_stype != null) {
                this.stype.clear();
                for (JsonValue elem : json_stype) {
                    this.stype.add(new ShipTypeDto((JsonObject) elem));
                }
            }

            // 装備一覧
            JsonArray apiMstSlotitem = this.json.getJsonArray("api_mst_slotitem");
            if (apiMstSlotitem != null) {
                this.items.clear();
                for (int i = 0; i < apiMstSlotitem.size(); i++) {
                    JsonObject object = (JsonObject) apiMstSlotitem.get(i);
                    ItemInfoDto item = new ItemInfoDto(object);
                    int id = object.getJsonNumber("api_id").intValue();
                    this.items.put(id, item);
                }
            }

            // 艦娘一覧
            JsonArray apiMstShip = this.json.getJsonArray("api_mst_ship");
            if (apiMstShip != null) {
                this.ships.clear();
                for (int i = 0; i < apiMstShip.size(); i++) {
                    JsonObject object = (JsonObject) apiMstShip.get(i);
                    int id = object.getInt("api_id");
                    this.ships.put(id, this.toShipInfoDto(object));
                }
            }
            this.setCharId();

            JsonArray json_maparea = this.json.getJsonArray("api_mst_maparea");
            if (json_maparea != null) {
                this.maparea.clear();
                for (JsonValue elem : json_maparea) {
                    this.maparea.add(new MapAreaDto((JsonObject) elem));
                }
            }

            JsonArray json_mapinfo = this.json.getJsonArray("api_mst_mapinfo");
            if (json_mapinfo != null) {
                this.mapinfo.clear();
                for (JsonValue elem : json_mapinfo) {
                    MapInfoDto dto = new MapInfoDto((JsonObject) elem);
                    this.mapinfo.put(dto.getId(), dto);
                }
            }

            JsonArray json_mission = this.json.getJsonArray("api_mst_mission");
            if (json_mission != null) {
                this.mission.clear();
                for (JsonValue elem : json_mission) {
                    MissionDto dto = new MissionDto((JsonObject) elem);
                    this.mission.put(dto.getId(), dto);
                }
            }

            JsonArray json_useitem = this.json.getJsonArray("api_mst_useitem");
            if (json_useitem != null) {
                this.useItem.clear();
                for (JsonValue elem : json_useitem) {
                    UseItemInfoDto dto = new UseItemInfoDto((JsonObject) elem);
                    this.useItem.put(dto.getId(), dto);
                }
            }

            this.time = new Date();
        }

        private void visitShip(ShipInfoDto ship, int[] charId) {
            if ((ship != null) && (ship.getData() == null)) {
                ship.setData(charId);

                if (ship.getAftershipid() != 0) {
                    this.visitShip(this.ships.get(ship.getAftershipid()), charId);
                }
                if (ship.getBeforeshpids() != null) {
                    for (int shipid : ship.getBeforeshpids()) {
                        this.visitShip(this.ships.get(shipid), charId);
                    }
                }
            }
        }

        /**
         * 初期艦IDを計算します
         */
        private void setCharId() {
            // リセット
            for (ShipInfoDto dto : this.ships.values()) {
                dto.setData(null);
                dto.setBeforeshpids(null);
            }
            // beforeshipidsを生成
            for (ShipInfoDto dto : this.ships.values()) {
                if (dto.getAftershipid() != 0) {
                    ShipInfoDto afterShip = this.ships.get(dto.getAftershipid());
                    if (afterShip != null) {
                        afterShip.setBeforeshpids(ArrayUtils.add(afterShip.getBeforeshpids(), dto.getShipId()));
                    }
                }
            }
            // 同じ島に同じint[]を配置
            for (ShipInfoDto dto : this.ships.values()) {
                this.visitShip(dto, new int[] { -1 });
            }
            // 初期艦のIDを探す
            for (ShipInfoDto dto : this.ships.values()) {
                if (dto.getBeforeshpids() == null) {
                    int[] charId = (int[]) dto.getData();
                    // 複数見つかったら（普通はありえないが）小さい方にしておく
                    if ((charId[0] == -1) || (charId[0] > dto.getShipId())) {
                        charId[0] = dto.getShipId();
                    }
                }
            }
            // IDをセット
            for (ShipInfoDto dto : this.ships.values()) {
                int[] charId = (int[]) dto.getData();
                dto.setCharId(charId[0]);
                dto.setData(null);
            }
        }

        /**
         * 艦娘を作成します
         *
         * @param object
         * @return
         */
        private ShipInfoDto toShipInfoDto(JsonObject object) {
            String name = object.getString("api_name");

            if ("なし".equals(name)) {
                return ShipInfoDto.EMPTY;
            }

            return new ShipInfoDto(object);
        }

        /** 艦種情報を取得 */
        public ShipTypeDto getShipType(int stype) {
            if ((stype <= 0) || (stype > this.stype.size())) {
                return null;
            }
            return this.stype.get(stype - 1);
        }

        /** （装備でない）アイテム情報を取得 */
        public UseItemInfoDto getUseItem(int id) {
            Integer key = id;
            return this.useItem.get(key);
        }

        /** マップ情報を取得 */
        public MapInfoDto getMapInfo(int area, int no) {
            int id = (area * 10) + no;
            return this.mapinfo.get(id);
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
         * @return mapinfo
         */
        public Map<Integer, MapInfoDto> getMapinfo() {
            return this.mapinfo;
        }

        /**
         * @return mission
         */
        public Map<Integer, MissionDto> getMission() {
            return this.mission;
        }

        /**
         * @return stype
         */
        public List<ShipTypeDto> getStype() {
            return this.stype;
        }

        /**
         * @return useItem
         */
        public Map<Integer, UseItemInfoDto> getUseItem() {
            return this.useItem;
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
         * @return ships
         */
        public Map<Integer, ShipInfoDto> getShips() {
            return this.ships;
        }

        /**
         * @return items
         */
        public Map<Integer, ItemInfoDto> getItems() {
            return this.items;
        }
    }

    /**
     * api_mst_maparea
     * @author Nekopanda
     */
    public static class MapAreaDto extends JsonData {

        public MapAreaDto() {
        }

        public MapAreaDto(JsonObject json) {
            super(json);
        }

        /**
         * @return id
         */
        public int getId() {
            return this.json.getInt("api_id");
        }

        /**
         * @return name
         */
        public String getName() {
            return this.json.getString("api_name");
        }
    }

    /**
     * api_mst_mapinfo
     * @author Nekopanda
     */
    public static class MapInfoDto extends JsonData {

        public MapInfoDto() {
        }

        public MapInfoDto(JsonObject json) {
            super(json);
        }

        /**
         * @return id
         */
        public int getId() {
            return this.json.getInt("api_id");
        }

        /**
         * @return maparea_id
         */
        public int getMaparea_id() {
            return this.json.getInt("api_maparea_id");
        }

        /**
         * @return no
         */
        public int getNo() {
            return this.json.getInt("api_no");
        }

        /**
         * @return name
         */
        public String getName() {
            return this.json.getString("api_name");
        }

    }

    /**
     * api_mst_mission
     * @author Nekopanda
     */
    public static class MissionDto extends JsonData {

        public MissionDto() {
        }

        public MissionDto(JsonObject json) {
            super(json);
        }

        /**
         * 遠征ID
         * @return id
         */
        public int getId() {
            return this.json.getInt("api_id");
        }

        /**
         * 遠征のマップエリアID
         * @return maparea_id
         */
        public int getMapareaId() {
            return this.json.getInt("api_maparea_id");
        }

        /**
         * 遠征名
         * @return name
         */
        public String getName() {
            return this.json.getString("api_name");
        }

        /**
         * 遠征にかかる時間
         * @return time
         */
        public int getTime() {
            return this.json.getInt("api_time");
        }

        /**
         * もらえるかもしれないアイテム
         * @return winItem
         */
        public UseItemDto[] getWinItem() {
            return new UseItemDto[] {
                    new UseItemDto(this.json.getJsonArray("api_win_item1")),
                    new UseItemDto(this.json.getJsonArray("api_win_item2")) };
        }
    }

    /**
     * api_mst_stype
     * @author Nekopanda
     */
    public static class ShipTypeDto extends JsonData {

        public ShipTypeDto() {
        }

        public ShipTypeDto(JsonObject json) {
            super(json);
        }

        /**
         * @return name
         */
        public String getName() {
            return this.json.getString("api_name");
        }

        /**
         * @return id
         */
        public int getId() {
            return this.json.getJsonNumber("api_id").intValue();
        }

        /**
         * @return equipType
         */
        public List<Boolean> getEquipType() {
            List<Boolean> equipType = new ArrayList<Boolean>();
            JsonObject json_equip_type = this.json.getJsonObject("api_equip_type");
            for (int i = 1;; ++i) {
                JsonNumber number = json_equip_type.getJsonNumber(String.valueOf(i));
                if (number == null)
                    break;
                equipType.add(number.intValue() != 0);
            }
            return equipType;
        }
    }

    /**
     * api_mst_useitem
     * @author Nekopanda
     */
    public static class UseItemInfoDto extends JsonData {

        public UseItemInfoDto() {
        }

        public UseItemInfoDto(JsonObject json) {
            super(json);
        }

        /**
         * @return id
         */
        public int getId() {
            return this.json.getJsonNumber("api_id").intValue();
        }

        /**
         * @return usetype
         */
        public int getUsetype() {
            return this.json.getJsonNumber("api_usetype").intValue();
        }

        /**
         * @return name
         */
        public String getName() {
            return this.json.getString("api_name");
        }

        /**
         * @return description
         */
        public String getDescription() {
            return this.json.getJsonArray("api_description").getString(0);
        }
    }
}
