package logbook.data.context;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.config.AppConfig;
import logbook.config.KdockConfig;
import logbook.constants.AppConstants;
import logbook.data.Data;
import logbook.data.DataQueue;
import logbook.dto.BattleDto;
import logbook.dto.BattleResultDto;
import logbook.dto.CreateItemDto;
import logbook.dto.DeckMissionDto;
import logbook.dto.DockDto;
import logbook.dto.GetShipDto;
import logbook.dto.ItemDto;
import logbook.dto.MaterialDto;
import logbook.dto.MissionResultDto;
import logbook.dto.NdockDto;
import logbook.dto.QuestDto;
import logbook.dto.ResourceDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;
import logbook.gui.logic.CreateReportLogic;
import logbook.internal.Deck;
import logbook.internal.Item;
import logbook.internal.Ship;
import logbook.internal.ShipStyle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 遠征・入渠などの情報を管理します
 *
 */
public final class GlobalContext {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(GlobalContext.class);

    /** 装備Map */
    private static Map<Long, ItemDto> itemMap = new ConcurrentSkipListMap<Long, ItemDto>();

    /** 艦娘Map */
    private static Map<Long, ShipDto> shipMap = new ConcurrentSkipListMap<Long, ShipDto>();

    /** 秘書艦 */
    private static ShipDto secretary;

    /** 建造 */
    private static List<GetShipDto> getShipList = new ArrayList<GetShipDto>();

    /** 建造(投入資源) */
    private static Map<String, ResourceDto> getShipResource = new HashMap<String, ResourceDto>();

    /** 開発 */
    private static List<CreateItemDto> createItemList = new ArrayList<CreateItemDto>();

    /** 海戦・ドロップ */
    private static List<BattleResultDto> battleResultList = new ArrayList<BattleResultDto>();

    /** 遠征結果 */
    private static List<MissionResultDto> missionResultList = new ArrayList<MissionResultDto>();

    /** 司令部Lv */
    private static int hqLevel;

    /** 最大保有可能 艦娘数 */
    private static int maxChara;

    /** 最大保有可能 装備数 */
    private static int maxSlotitem;

    /** 最後に建造を行った建造ドック */
    private static String lastBuildKdock;

    /** 戦闘詳細 */
    private static BattleDto battle = null;

    /** 遠征リスト */
    private static DeckMissionDto[] deckMissions = new DeckMissionDto[] { DeckMissionDto.EMPTY, DeckMissionDto.EMPTY,
            DeckMissionDto.EMPTY };

    /** ドック */
    private static Map<String, DockDto> dock = new HashMap<String, DockDto>();

    /** 入渠リスト */
    private static NdockDto[] ndocks = new NdockDto[] { NdockDto.EMPTY, NdockDto.EMPTY, NdockDto.EMPTY,
            NdockDto.EMPTY };

    /** 任務Map */
    private static SortedMap<Integer, QuestDto> questMap = new ConcurrentSkipListMap<Integer, QuestDto>();

    /** 出撃中か */
    private static boolean[] isSortie = new boolean[4];

    /** 今いるマップ上のマスNo */
    private static int mapCellNo;

    /** 今いるマップ上のボスNo */
    private static int mapBossCellNo;

    /** ログキュー */
    private static Queue<String> consoleQueue = new ArrayBlockingQueue<String>(10);

    /** 保有資源・資材 */
    private static MaterialDto material = null;

    /** 最後に資源ログに追加した時間 */
    volatile private static Date materialLogLastUpdate = null;

    /** 連合艦隊 */
    private static boolean combined;

    /**
     * @return 装備Map
     */
    public static Map<Long, ItemDto> getItemMap() {
        return itemMap;
    }

    /**
     * 装備を復元する
     * @param map
     */
    public static void setItemMap(Map<Long, Integer> map) {
        for (Entry<Long, Integer> entry : map.entrySet()) {
            Object obj = entry.getValue();
            Integer id;
            if (obj instanceof Integer) {
                id = (Integer) obj;
            } else {
                // 旧設定ファイル用
                id = Integer.parseInt(obj.toString());
            }
            ItemDto item = Item.get(id);
            if (item != null) {
                itemMap.put(entry.getKey(), item);
            }
        }
    }

    /**
     * @return 艦娘Map
     */
    public static Map<Long, ShipDto> getShipMap() {
        return shipMap;
    }

    /**
     * @return 秘書艦
     */
    public static ShipDto getSecretary() {
        return secretary;
    }

    /**
     * @return 司令部Lv
     */
    public static int hqLevel() {
        return hqLevel;
    }

    /**
     * @return 最大保有可能 艦娘数
     */
    public static int maxChara() {
        return maxChara;
    }

    /**
     * @return 最大保有可能 装備数
     */
    public static int maxSlotitem() {
        return maxSlotitem;
    }

    /**
     * @return 建造艦娘List
     */
    public static List<GetShipDto> getGetshipList() {
        return getShipList;
    }

    /**
     * @return 開発アイテムList
     */
    public static List<CreateItemDto> getCreateItemList() {
        return createItemList;
    }

    /**
     * @return 海戦・ドロップList
     */
    public static List<BattleResultDto> getBattleResultList() {
        return battleResultList;
    }

    /**
     * @return 遠征結果
     */
    public static List<MissionResultDto> getMissionResultList() {
        return missionResultList;
    }

    /**
     * @return 遠征リスト
     */
    public static DeckMissionDto[] getDeckMissions() {
        return deckMissions;
    }

    /**
     * @return 入渠リスト
     */
    public static NdockDto[] getNdocks() {
        return ndocks;
    }

    /**
     * 艦娘が入渠しているかを調べます
     * 
     * @param ship 艦娘
     * @return 入渠している場合true
     */
    public static boolean isNdock(ShipDto ship) {
        return isNdock(ship.getId());
    }

    /**
     * 艦娘が入渠しているかを調べます
     * @param ship 艦娘ID
     * @return 入渠している場合true
     */
    public static boolean isNdock(long ship) {
        for (NdockDto ndock : ndocks) {
            if (ship == ndock.getNdockid()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 艦隊が遠征中かを調べます
     * 
     * @param 
     */
    public static boolean isMission(String idstr) {
        int id = Integer.parseInt(idstr);
        for (int i = 0; i < deckMissions.length; i++) {
            if ((deckMissions[i].getMission() != null) && (deckMissions[i].getFleetid() == id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return ドック
     */
    public static DockDto getDock(String id) {
        return dock.get(id);
    }

    /**
     * 任務を取得します
     * @return 任務
     */
    public static Map<Integer, QuestDto> getQuest() {
        return questMap;
    }

    /**
     * 出撃中かを調べます
     * @return 出撃中
     */
    public static boolean isSortie(String idstr) {
        int id = Integer.parseInt(idstr);
        return isSortie[id - 1];
    }

    /**
     * 保有資材を取得します
     * @return 保有資材
     */
    @CheckForNull
    public static MaterialDto getMaterial() {
        return material;
    }

    /**
     * @return ログメッセージ
     */
    public static String getConsoleMessage() {
        return consoleQueue.poll();
    }

    /**
     * 情報を更新します
     * 
     * @return 更新する情報があった場合trueを返します
     */
    public static boolean updateContext() {
        boolean update = false;
        Data data;
        while ((data = DataQueue.poll()) != null) {
            update = true;
            // json保存設定
            if (AppConfig.get().isStoreJson()) {
                doStoreJson(data);
            }
            switch (data.getDataType()) {
            // 補給
            case CHARGE:
                doCharge(data);
                break;
            // 編成
            case CHANGE:
                doChange(data);
                break;
            // 母港
            case PORT:
                doPort(data);
                break;
            // 保有装備
            case SLOTITEM_MEMBER:
                doSlotitemMember(data);
                break;
            // 保有艦
            case SHIP3:
                doShip3(data);
                break;
            // 保有艦
            case SHIP2:
                doShip2(data);
                break;
            // 基本
            case BASIC:
                doBasic(data);
                break;
            // 資材
            case MATERIAL:
                doMaterial(data);
                break;
            // 遠征(帰還)
            case MISSION_RESULT:
                doMissionResult(data);
                break;
            // 入渠
            case NDOCK:
                doNdock(data);
                break;
            // 建造
            case CREATE_SHIP:
                doCreateship(data);
                break;
            // 建造ドック
            case KDOCK:
                doKdock(data);
                break;
            // 建造(入手)
            case GET_SHIP:
                doGetship(data);
                break;
            // 装備開発
            case CREATE_ITEM:
                doCreateitem(data);
                break;
            // 解体
            case DESTROY_SHIP:
                doDestroyShip(data);
                break;
            // 廃棄
            case DESTROY_ITEM2:
                doDestroyItem2(data);
                break;
            // 近代化改修
            case POWERUP:
                doPowerup(data);
                break;
            // 海戦
            case BATTLE:
                doBattle(data);
                break;
            // 海戦
            case BATTLE_SP_MIDNIGHT:
                doBattle(data);
                break;
            // 海戦
            case BATTLE_NIGHT_TO_DAY:
                doBattle(data);
                break;
            // 海戦
            case COMBINED_AIR_BATTLE:
                doBattle(data);
                break;
            // 海戦
            case COMBINED_BATTLE:
                doBattle(data);
                break;
            // 海戦
            case COMBINED_BATTLE_WATER:
                doBattle(data);
                break;
            // 海戦結果
            case BATTLE_RESULT:
                doBattleresult(data);
                break;
            // 海戦結果
            case COMBINED_BATTLE_RESULT:
                doBattleresult(data);
                break;
            // 艦隊
            case DECK:
                doDeck(data);
                break;
            // 出撃
            case START:
                doStart(data);
                break;
            // 出撃
            case NEXT:
                doNext(data);
                break;
            // 任務
            case QUEST_LIST:
                doQuest(data);
                break;
            // 任務消化
            case QUEST_CLEAR:
                doQuestClear(data);
                break;
            // 設定
            case START2:
                doStart2(data);
                break;
            default:
                break;
            }
        }
        return update;
    }

    /**
     * JSONオブジェクトを保存する
     * @param data
     */
    private static void doStoreJson(Data data) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HHmmss.SSS");
            Date time = Calendar.getInstance().getTime();
            // ファイル名
            String fname = new StringBuilder().append(format.format(time)).append("_").append(data.getDataType())
                    .append(".json").toString();
            // ファイルパス
            File file = new File(FilenameUtils.concat(AppConfig.get().getStoreJsonPath(), fname));

            FileUtils.write(file, data.getJsonObject().toString());
        } catch (IOException e) {
            LOG.warn("JSONオブジェクトを保存するに失敗しました", e);
            LOG.warn(data);
        }

    }

    /**
     * 補給を更新します
     * @param data
     */
    private static void doCharge(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            if (apidata != null) {
                JsonArray ships = apidata.getJsonArray("api_ship");
                for (JsonValue shipval : ships) {
                    JsonObject shipobj = (JsonObject) shipval;

                    Long shipid = shipobj.getJsonNumber("api_id").longValue();
                    int fuel = shipobj.getJsonNumber("api_fuel").intValue();
                    int bull = shipobj.getJsonNumber("api_bull").intValue();

                    ShipDto ship = shipMap.get(shipid);
                    if (ship != null) {
                        ship.setFuel(fuel);
                        ship.setBull(bull);

                        String fleetid = ship.getFleetid();
                        if (fleetid != null) {
                            DockDto dockdto = dock.get(fleetid);
                            if (dockdto != null) {
                                dockdto.setUpdate(true);
                            }
                        }
                    }
                }
                addConsole("補給を更新しました");
            }
        } catch (Exception e) {
            LOG.warn("補給を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 編成を更新します
     * @param data
     */
    private static void doChange(Data data) {
        try {
            String fleetid = data.getField("api_id");
            long shipid = Long.parseLong(data.getField("api_ship_id"));
            int shipidx = Integer.parseInt(data.getField("api_ship_idx"));

            DockDto dockdto = dock.get(fleetid);

            if (dockdto != null) {
                List<ShipDto> ships = dockdto.getShips();

                DockDto newdock = new DockDto(dockdto.getId(), dockdto.getName());
                if (shipidx == -1) {
                    for (int i = 1; i < ships.size(); i++) {
                        // 艦隊IDを外す
                        ships.get(i).setFleetid(null);
                    }
                    // 旗艦以外解除
                    newdock.addShip(ships.get(0));
                } else {
                    // 入れ替えまたは外す
                    // 入れ替え後の艦娘(外す場合はnull)
                    ShipDto rship = shipMap.get(shipid);
                    ShipDto[] shiparray = new ShipDto[7];

                    for (int i = 0; i < ships.size(); i++) {
                        // 艦隊IDを一旦全部外す
                        ships.get(i).setFleetid(null);
                        shiparray[i] = ships.get(i);
                    }
                    for (int i = 0; i < ships.size(); i++) {
                        if (rship == ships.get(i)) {
                            shiparray[i] = shiparray[shipidx];
                        }
                    }
                    shiparray[shipidx] = rship;
                    for (ShipDto shipdto : shiparray) {
                        if (shipdto != null) {
                            shipdto.setFleetid(fleetid);
                            newdock.addShip(shipdto);
                        }
                    }
                }
                if ("1".equals(fleetid)) {
                    // 秘書艦を再設定
                    setSecretary(newdock.getShips().get(0));
                }
                dock.put(fleetid, newdock);
            }
        } catch (Exception e) {
            LOG.warn("編成を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 母港を更新します
     * @param data
     */
    private static void doPort(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            if (apidata != null) {
                // 出撃中ではない
                Arrays.fill(isSortie, false);

                // 基本情報を更新する
                JsonObject apiBasic = apidata.getJsonObject("api_basic");
                doBasicSub(apiBasic);
                addConsole("司令部を更新しました");

                // 保有資材を更新する
                JsonArray apiMaterial = apidata.getJsonArray("api_material");
                doMaterialSub(apiMaterial);
                addConsole("保有資材を更新しました");

                // 保有艦娘を更新する
                JsonArray apiShip = apidata.getJsonArray("api_ship");
                for (int i = 0; i < apiShip.size(); i++) {
                    ShipDto ship = new ShipDto((JsonObject) apiShip.get(i));
                    shipMap.put(Long.valueOf(ship.getId()), ship);
                }
                JsonArray apiDeckPort = apidata.getJsonArray("api_deck_port");
                doDeck(apiDeckPort);
                addConsole("保有艦娘情報を更新しました");

                // 入渠の状態を更新する
                JsonArray apiNdock = apidata.getJsonArray("api_ndock");
                doNdockSub(apiNdock);
                addConsole("入渠情報を更新しました");

                // 遠征の状態を更新する
                deckMissions = new DeckMissionDto[] { DeckMissionDto.EMPTY, DeckMissionDto.EMPTY, DeckMissionDto.EMPTY };
                for (int i = 1; i < apiDeckPort.size(); i++) {
                    JsonObject object = (JsonObject) apiDeckPort.get(i);
                    String name = object.getString("api_name");
                    JsonArray jmission = object.getJsonArray("api_mission");

                    int section = ((JsonNumber) jmission.get(1)).intValue();
                    String mission = Deck.get(section);
                    long milis = ((JsonNumber) jmission.get(2)).longValue();
                    long fleetid = object.getJsonNumber("api_id").longValue();

                    Set<Long> ships = new LinkedHashSet<Long>();
                    JsonArray shiparray = object.getJsonArray("api_ship");
                    for (JsonValue jsonValue : shiparray) {
                        long shipid = ((JsonNumber) jsonValue).longValue();
                        if (shipid != -1) {
                            ships.add(shipid);
                        }
                    }

                    Date time = null;
                    if (milis > 0) {
                        time = new Date(milis);
                    }
                    deckMissions[i - 1] = new DeckMissionDto(name, mission, time, fleetid, ships);
                }
                addConsole("遠征情報を更新しました");

                // 連合艦隊を更新する
                combined = false;
                if (apidata.containsKey("api_combined_flag")) {
                    switch (apidata.getJsonNumber("api_combined_flag").intValue()) {
                    case 1:
                        combined = true;
                        break;
                    default:
                        break;
                    }
                    addConsole("連合艦隊を更新しました");
                }
            }
        } catch (Exception e) {
            LOG.warn("母港を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 海戦情報を更新します
     * @param data
     */
    private static void doBattle(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            battle = new BattleDto(apidata);

            addConsole("海戦情報を更新しました");
        } catch (Exception e) {
            LOG.warn("海戦情報を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 海戦情報を更新します
     * @param data
     */
    private static void doBattleresult(Data data) {
        try {
            if (battle != null) {
                JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
                BattleResultDto dto = new BattleResultDto(apidata, mapCellNo, mapBossCellNo, battle);
                battleResultList.add(dto);
                CreateReportLogic.storeBattleResultReport(dto);

                for (int i = 0; i < (battleResultList.size() - AppConstants.MAX_LOG_SIZE); i++) {
                    battleResultList.remove(0);
                }
            }
            addConsole("海戦情報を更新しました");
        } catch (Exception e) {
            LOG.warn("海戦情報を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 建造(投入資源)情報を更新します
     * @param data
     */
    private static void doCreateship(Data data) {
        try {
            String kdockid = data.getField("api_kdock_id");
            // 投入資源
            ResourceDto resource = new ResourceDto(
                    data.getField("api_large_flag"),
                    data.getField("api_item1"),
                    data.getField("api_item2"),
                    data.getField("api_item3"),
                    data.getField("api_item4"),
                    data.getField("api_item5"),
                    secretary, hqLevel
                    );
            lastBuildKdock = kdockid;
            getShipResource.put(kdockid, resource);
            KdockConfig.store(kdockid, resource);

            addConsole("建造(投入資源)情報を更新しました");
        } catch (Exception e) {
            LOG.warn("建造(投入資源)情報を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 建造を更新します
     * @param data
     */
    private static void doKdock(Data data) {
        try {
            // 建造ドックの空きをカウントします
            if (lastBuildKdock != null) {
                ResourceDto resource = getShipResource.get(lastBuildKdock);
                if (resource != null) {
                    int freecount = 0;
                    JsonArray apidata = data.getJsonObject().getJsonArray("api_data");
                    for (int i = 0; i < apidata.size(); i++) {
                        int state = ((JsonObject) apidata.get(i)).getJsonNumber("api_state").intValue();
                        if (state == 0) {
                            freecount++;
                        }
                    }
                    // 建造ドックの空きをセットします
                    resource.setFreeDock(Integer.toString(freecount));
                    KdockConfig.store(lastBuildKdock, resource);
                }
            }
            addConsole("建造を更新しました");
        } catch (Exception e) {
            LOG.warn("建造を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 建造(入手)情報を更新します
     * @param data
     */
    private static void doGetship(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            String dock = data.getField("api_kdock_id");

            // 艦娘の装備を追加します
            if (!apidata.isNull("api_slotitem")) {
                JsonArray slotitem = apidata.getJsonArray("api_slotitem");
                for (int i = 0; i < slotitem.size(); i++) {
                    JsonObject object = (JsonObject) slotitem.get(i);
                    int typeid = object.getJsonNumber("api_slotitem_id").intValue();
                    Long id = object.getJsonNumber("api_id").longValue();
                    ItemDto item = Item.get(typeid);
                    if (item != null) {
                        itemMap.put(id, item);
                    }
                }
            }
            // 艦娘を追加します
            JsonObject apiShip = apidata.getJsonObject("api_ship");
            ShipDto ship = new ShipDto(apiShip);
            shipMap.put(Long.valueOf(ship.getId()), ship);
            // 投入資源を取得する
            ResourceDto resource = getShipResource.get(dock);
            if (resource == null) {
                resource = KdockConfig.load(dock);
            }
            GetShipDto dto = new GetShipDto(ship, resource);
            getShipList.add(dto);
            CreateReportLogic.storeCreateShipReport(dto);
            // 投入資源を除去する
            getShipResource.remove(dock);
            KdockConfig.remove(dock);

            addConsole("建造(入手)情報を更新しました");
        } catch (Exception e) {
            LOG.warn("建造(入手)情報を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 装備開発情報を更新します
     * 
     * @param data
     */
    private static void doCreateitem(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");

            // 投入資源
            ResourceDto resources = new ResourceDto(data.getField("api_item1"), data.getField("api_item2"),
                    data.getField("api_item3"), data.getField("api_item4"), secretary, hqLevel);

            CreateItemDto createitem = new CreateItemDto(apidata, resources);
            if (createitem.isCreateFlag()) {
                JsonObject object = apidata.getJsonObject("api_slot_item");
                int typeid = object.getJsonNumber("api_slotitem_id").intValue();
                Long id = object.getJsonNumber("api_id").longValue();
                ItemDto item = Item.get(typeid);
                if (item != null) {
                    itemMap.put(id, item);

                    createitem.setName(item.getName());
                    createitem.setType(item.getType());
                    createItemList.add(createitem);
                }
            } else {
                createItemList.add(createitem);
            }
            CreateReportLogic.storeCreateItemReport(createitem);

            for (int i = 0; i < (createItemList.size() - AppConstants.MAX_LOG_SIZE); i++) {
                createItemList.remove(0);
            }
            addConsole("装備開発情報を更新しました");
        } catch (Exception e) {
            LOG.warn("装備開発情報を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 保有装備を更新します
     * 
     * @param data
     */
    private static void doSlotitemMember(Data data) {
        try {
            JsonArray apidata = data.getJsonObject().getJsonArray("api_data");
            // 破棄
            itemMap.clear();
            for (int i = 0; i < apidata.size(); i++) {
                JsonObject object = (JsonObject) apidata.get(i);
                int typeid = object.getJsonNumber("api_slotitem_id").intValue();
                Long id = object.getJsonNumber("api_id").longValue();
                ItemDto item = Item.get(typeid);
                if (item != null) {
                    itemMap.put(id, item);
                }
            }

            addConsole("保有装備情報を更新しました");
        } catch (Exception e) {
            LOG.warn("保有装備を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 保有艦娘を更新します
     * 
     * @param data
     */
    private static void doShip3(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");

            String shipidstr = data.getField("api_shipid");
            JsonArray shipdata = apidata.getJsonArray("api_ship_data");

            if (shipidstr != null) {
                // 艦娘の指定がある場合は艦娘を差し替える
                Long shipid = Long.parseLong(shipidstr);
                for (int i = 0; i < shipdata.size(); i++) {
                    ShipDto ship = new ShipDto((JsonObject) shipdata.get(i));
                    shipMap.put(shipid, ship);
                }
            } else {
                // 情報を破棄
                shipMap.clear();
                for (int i = 0; i < shipdata.size(); i++) {
                    ShipDto ship = new ShipDto((JsonObject) shipdata.get(i));
                    shipMap.put(Long.valueOf(ship.getId()), ship);
                }
            }
            // 艦隊を設定
            doDeck(apidata.getJsonArray("api_deck_data"));

            addConsole("保有艦娘情報を更新しました");
        } catch (Exception e) {
            LOG.warn("保有艦娘を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 保有艦娘を更新します
     * 
     * @param data
     */
    private static void doShip2(Data data) {
        try {
            JsonArray apidata = data.getJsonObject().getJsonArray("api_data");
            // 情報を破棄
            shipMap.clear();
            for (int i = 0; i < apidata.size(); i++) {
                ShipDto ship = new ShipDto((JsonObject) apidata.get(i));
                shipMap.put(Long.valueOf(ship.getId()), ship);
            }
            // 艦隊を設定
            doDeck(data.getJsonObject().getJsonArray("api_data_deck"));

            addConsole("保有艦娘情報を更新しました");
        } catch (Exception e) {
            LOG.warn("保有艦娘を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 艦隊を更新します
     * 
     * @param data
     */
    private static void doDeck(Data data) {
        try {
            JsonArray apidata = data.getJsonObject().getJsonArray("api_data");
            // 艦隊IDをクリアします
            for (DockDto dockdto : dock.values()) {
                for (ShipDto ship : dockdto.getShips()) {
                    ship.setFleetid("");
                }
            }
            doDeck(apidata);
            addConsole("艦隊を更新しました");
        } catch (Exception e) {
            LOG.warn("艦隊を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 艦隊を設定します
     * 
     * @param apidata
     */
    private static void doDeck(JsonArray apidata) {
        dock.clear();
        for (int i = 0; i < apidata.size(); i++) {
            JsonObject jsonObject = (JsonObject) apidata.get(i);
            String fleetid = Long.toString(jsonObject.getJsonNumber("api_id").longValue());
            String name = jsonObject.getString("api_name");
            JsonArray apiship = jsonObject.getJsonArray("api_ship");

            DockDto dockdto = new DockDto(fleetid, name);
            dock.put(fleetid, dockdto);

            for (int j = 0; j < apiship.size(); j++) {
                Long shipid = Long.valueOf(((JsonNumber) apiship.get(j)).longValue());
                ShipDto ship = shipMap.get(shipid);

                if (ship != null) {
                    dockdto.addShip(ship);

                    if ((i == 0) && (j == 0)) {
                        setSecretary(ship);
                    }
                    // 艦隊IDを設定
                    ship.setFleetid(fleetid);
                }
            }
        }
    }

    /**
     * 秘書艦を設定します
     * 
     * @param ship
     */
    private static void setSecretary(ShipDto ship) {
        if ((secretary == null) || (ship.getId() != secretary.getId())) {
            addConsole(ship.getName() + "(Lv" + ship.getLv() + ")" + " が秘書艦に任命されました");
        }
        // 秘書艦を設定
        secretary = ship;
    }

    /**
     * 艦娘を解体します
     * @param data
     */
    private static void doDestroyShip(Data data) {
        try {
            Long shipid = Long.parseLong(data.getField("api_ship_id"));
            ShipDto ship = shipMap.get(shipid);
            if (ship != null) {
                // 持っている装備を廃棄する
                List<Long> items = ship.getItemId();
                for (Long item : items) {
                    itemMap.remove(item);
                }
                // 艦娘を外す
                shipMap.remove(ship.getId());
            }

            addConsole("艦娘を解体しました");
        } catch (Exception e) {
            LOG.warn("艦娘を解体しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 装備を廃棄します
     * @param data
     */
    private static void doDestroyItem2(Data data) {
        try {
            String itemids = data.getField("api_slotitem_ids");
            for (String itemid : itemids.split(",")) {
                Long item = Long.parseLong(itemid);
                itemMap.remove(item);
            }
            addConsole("装備を廃棄しました");
        } catch (Exception e) {
            LOG.warn("装備を廃棄しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 近代化改修します
     * @param data
     */
    private static void doPowerup(Data data) {
        try {
            String shipids = data.getField("api_id_items");
            for (String shipid : shipids.split(",")) {
                ShipDto ship = shipMap.get(Long.parseLong(shipid));
                if (ship != null) {
                    // 持っている装備を廃棄する
                    List<Long> items = ship.getItemId();
                    for (Long item : items) {
                        itemMap.remove(item);
                    }
                    // 艦娘を外す
                    shipMap.remove(ship.getId());
                }
            }
            addConsole("装備を廃棄しました");
        } catch (Exception e) {
            LOG.warn("装備を廃棄しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 司令部を更新する
     * 
     * @param data
     */
    private static void doBasic(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            doBasicSub(apidata);

            addConsole("司令部を更新しました");
        } catch (Exception e) {
            LOG.warn("司令部を更新するに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 司令部を更新する
     * 
     * @param apidata
     */
    private static void doBasicSub(JsonObject apidata) {
        // 指令部Lv
        hqLevel = apidata.getJsonNumber("api_level").intValue();
        // 最大所有艦娘数
        maxChara = apidata.getJsonNumber("api_max_chara").intValue();
        // 最大所有装備数
        maxSlotitem = apidata.getJsonNumber("api_max_slotitem").intValue();
    }

    /**
     * 保有資材を更新する
     * 
     * @param data
     */
    private static void doMaterial(Data data) {
        try {
            JsonArray apidata = data.getJsonObject().getJsonArray("api_data");

            doMaterialSub(apidata);

            addConsole("保有資材を更新しました");
        } catch (Exception e) {
            LOG.warn("保有資材を更新するに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 保有資材を更新する
     * 
     * @param apidata
     */
    private static void doMaterialSub(JsonArray apidata) {
        Date time = Calendar.getInstance().getTime();
        MaterialDto dto = new MaterialDto();
        dto.setTime(time);

        for (JsonValue value : apidata) {
            JsonObject entry = (JsonObject) value;

            switch (entry.getInt("api_id")) {
            case AppConstants.MATERIAL_FUEL:
                dto.setFuel(entry.getInt("api_value"));
                break;
            case AppConstants.MATERIAL_AMMO:
                dto.setAmmo(entry.getInt("api_value"));
                break;
            case AppConstants.MATERIAL_METAL:
                dto.setMetal(entry.getInt("api_value"));
                break;
            case AppConstants.MATERIAL_BAUXITE:
                dto.setBauxite(entry.getInt("api_value"));
                break;
            case AppConstants.MATERIAL_BURNER:
                dto.setBurner(entry.getInt("api_value"));
                break;
            case AppConstants.MATERIAL_BUCKET:
                dto.setBucket(entry.getInt("api_value"));
                break;
            case AppConstants.MATERIAL_RESEARCH:
                dto.setResearch(entry.getInt("api_value"));
                break;
            default:
                break;
            }
        }
        material = dto;

        // 資材ログに書き込む
        if ((materialLogLastUpdate == null)
                || (TimeUnit.MILLISECONDS.toSeconds(time.getTime() - materialLogLastUpdate.getTime()) >
                AppConfig.get().getMaterialLogInterval())) {
            CreateReportLogic.storeMaterialReport(material);

            materialLogLastUpdate = time;
        }
    }

    /**
     * 遠征(帰還)を更新します
     * 
     * @param data
     */
    private static void doMissionResult(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");

            MissionResultDto result = new MissionResultDto();

            int clearResult = apidata.getJsonNumber("api_clear_result").intValue();
            result.setClearResult(clearResult);
            result.setQuestName(apidata.getString("api_quest_name"));

            if (clearResult != 0) {
                JsonArray material = apidata.getJsonArray("api_get_material");
                result.setFuel(material.getJsonNumber(0).toString());
                result.setAmmo(material.getJsonNumber(1).toString());
                result.setMetal(material.getJsonNumber(2).toString());
                result.setBauxite(material.getJsonNumber(3).toString());
            }

            CreateReportLogic.storeCreateMissionReport(result);
            missionResultList.add(result);

            for (int i = 0; i < (missionResultList.size() - AppConstants.MAX_LOG_SIZE); i++) {
                missionResultList.remove(0);
            }
            addConsole("遠征(帰還)情報を更新しました");
        } catch (Exception e) {
            LOG.warn("遠征(帰還)を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 入渠を更新します
     * @param data
     */
    private static void doNdock(Data data) {
        try {
            JsonArray apidata = data.getJsonObject().getJsonArray("api_data");

            doNdockSub(apidata);

            addConsole("入渠情報を更新しました");
        } catch (Exception e) {
            LOG.warn("入渠を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 入渠を更新します
     * @param apidata
     */
    private static void doNdockSub(JsonArray apidata) {
        ndocks = new NdockDto[] { NdockDto.EMPTY, NdockDto.EMPTY, NdockDto.EMPTY, NdockDto.EMPTY };

        for (int i = 0; i < apidata.size(); i++) {
            JsonObject object = (JsonObject) apidata.get(i);
            long id = object.getJsonNumber("api_ship_id").longValue();
            long milis = object.getJsonNumber("api_complete_time").longValue();

            Date time = null;
            if (milis > 0) {
                time = new Date(milis);
            }
            ndocks[i] = new NdockDto(id, time);
        }
    }

    /**
     * 任務を更新します
     * 
     * @param data
     */
    private static void doQuest(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            if (!apidata.isNull("api_list")) {
                JsonArray apilist = apidata.getJsonArray("api_list");
                for (JsonValue value : apilist) {
                    if (value instanceof JsonObject) {
                        JsonObject questobject = (JsonObject) value;
                        // 任務を作成
                        QuestDto quest = new QuestDto();
                        quest.setNo(questobject.getInt("api_no"));
                        quest.setCategory(questobject.getInt("api_category"));
                        quest.setType(questobject.getInt("api_type"));
                        quest.setState(questobject.getInt("api_state"));
                        quest.setTitle(questobject.getString("api_title"));
                        quest.setDetail(questobject.getString("api_detail"));
                        JsonArray material = questobject.getJsonArray("api_get_material");
                        quest.setFuel(material.getJsonNumber(0).toString());
                        quest.setAmmo(material.getJsonNumber(1).toString());
                        quest.setMetal(material.getJsonNumber(2).toString());
                        quest.setBauxite(material.getJsonNumber(3).toString());
                        quest.setBonusFlag(questobject.getInt("api_bonus_flag"));
                        quest.setProgressFlag(questobject.getInt("api_progress_flag"));

                        questMap.put(quest.getNo(), quest);
                    }
                }
            }
            addConsole("任務を更新しました");
        } catch (Exception e) {
            LOG.warn("任務を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 消化した任務を除去します
     * 
     * @param data
     */
    private static void doQuestClear(Data data) {
        try {
            String idstr = data.getField("api_quest_id");
            if (idstr != null) {
                Integer id = Integer.valueOf(idstr);
                questMap.remove(id);
            }
        } catch (Exception e) {
            LOG.warn("消化した任務を除去しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 出撃を更新します
     * 
     * @param data
     */
    private static void doStart(Data data) {
        try {
            String idstr = data.getField("api_deck_id");
            if (idstr != null) {
                int id = Integer.parseInt(idstr);
                isSortie[id - 1] = true;
            }
            // 連合艦隊第2艦隊の出撃
            if (combined) {
                isSortie[1] = true;
            }

            JsonObject obj = data.getJsonObject().getJsonObject("api_data");

            mapCellNo = obj.getJsonNumber("api_no").intValue();
            mapBossCellNo = obj.getJsonNumber("api_bosscell_no").intValue();

            addConsole("出撃を更新しました");
        } catch (Exception e) {
            LOG.warn("出撃を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 進撃を更新します
     * 
     * @param data
     */
    private static void doNext(Data data) {
        try {
            JsonObject obj = data.getJsonObject().getJsonObject("api_data");

            mapCellNo = obj.getJsonNumber("api_no").intValue();
            mapBossCellNo = obj.getJsonNumber("api_bosscell_no").intValue();

            addConsole("進撃を更新しました");
        } catch (Exception e) {
            LOG.warn("進撃を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 設定を更新します
     * 
     * @param data
     */
    private static void doStart2(Data data) {
        try {
            JsonObject obj = data.getJsonObject().getJsonObject("api_data");
            if (obj != null) {
                // 艦娘一覧
                JsonArray apiMstShip = obj.getJsonArray("api_mst_ship");
                for (int i = 0; i < apiMstShip.size(); i++) {
                    JsonObject object = (JsonObject) apiMstShip.get(i);
                    String id = object.getJsonNumber("api_id").toString();
                    Ship.set(id, toShipInfoDto(object));
                }
                addConsole("艦娘一覧を更新しました");

                // 装備一覧
                JsonArray apiMstSlotitem = obj.getJsonArray("api_mst_slotitem");
                for (int i = 0; i < apiMstSlotitem.size(); i++) {
                    JsonObject object = (JsonObject) apiMstSlotitem.get(i);
                    ItemDto item = new ItemDto(object);
                    int id = object.getJsonNumber("api_id").intValue();
                    Item.set(id, item);
                }
                addConsole("装備一覧を更新しました");
            }

            addConsole("設定を更新しました");
        } catch (Exception e) {
            LOG.warn("設定を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 艦娘を作成します
     * 
     * @param object
     * @return
     */
    private static ShipInfoDto toShipInfoDto(JsonObject object) {
        String name = object.getString("api_name");

        if ("なし".equals(name)) {
            return ShipInfoDto.EMPTY;
        }

        String type = ShipStyle.get(object.getJsonNumber("api_stype").toString());
        String flagship = object.getString("api_yomi");
        if ("-".equals(flagship)) {
            flagship = "";
        }
        int afterlv = object.getJsonNumber("api_afterlv").intValue();
        int maxBull = 0;
        if (object.containsKey("api_bull_max")) {
            maxBull = object.getJsonNumber("api_bull_max").intValue();
        }
        int maxFuel = 0;
        if (object.containsKey("api_fuel_max")) {
            maxFuel = object.getJsonNumber("api_fuel_max").intValue();
        }
        return new ShipInfoDto(name, type, flagship, afterlv, maxBull, maxFuel);
    }

    private static void addConsole(Object message) {
        consoleQueue.offer(new SimpleDateFormat(AppConstants.DATE_SHORT_FORMAT)
                .format(Calendar.getInstance().getTime())
                + "  " + message.toString());
    }
}
