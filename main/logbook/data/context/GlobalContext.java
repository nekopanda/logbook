/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.data.context;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.config.GlobalConfig;
import logbook.data.Data;
import logbook.data.DataQueue;
import logbook.dto.BattleResultDto;
import logbook.dto.CreateItemDto;
import logbook.dto.DeckMissionDto;
import logbook.dto.GetShipDto;
import logbook.dto.ItemDto;
import logbook.dto.ResourceDto;
import logbook.dto.ShipDto;
import logbook.internal.Deck;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 遠征・入渠などの情報を管理します
 *
 */
public final class GlobalContext {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(GlobalContext.class);

    /** ログに表示する日付書式 */
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat(GlobalConfig.DATE_SHORT_FORMAT);

    /** 装備Map */
    private static Map<String, ItemDto> itemMap = new ConcurrentHashMap<String, ItemDto>();

    /** 装備Map(敵) */
    private static Map<String, String> enemyItemMap = new ConcurrentHashMap<String, String>();

    /** 艦娘Map */
    private static Map<String, ShipDto> shipMap = new ConcurrentHashMap<String, ShipDto>();

    /** 秘書艦 */
    private static ShipDto secretary;

    /** 建造 */
    private static List<GetShipDto> getShipList = new ArrayList<GetShipDto>();

    /** 建造(装備名の確定待ち)(艦娘ID, ドックID) */
    private static Queue<String[]> getShipQueue = new ArrayBlockingQueue<String[]>(10);

    /** 建造(投入資源) */
    private static Map<String, ResourceDto> getShipResource = new HashMap<String, ResourceDto>();

    /** 開発 */
    private static List<CreateItemDto> createItemList = new ArrayList<CreateItemDto>();

    /** 開発(装備名の確定待ち) */
    private static Queue<CreateItemDto> createItemQueue = new ArrayBlockingQueue<CreateItemDto>(10);

    /** 海戦・ドロップ */
    private static List<BattleResultDto> battleResultList = new ArrayList<BattleResultDto>();

    /** 遠征1 */
    private static DeckMissionDto deck1Mission;

    /** 遠征1 */
    private static DeckMissionDto deck2Mission;

    /** 遠征1 */
    private static DeckMissionDto deck3Mission;

    /** 入渠1 艦娘ID */
    private static long ndock1id;

    /** 入渠1 お風呂から上がる時間 */
    private static Date ndock1time;

    /** 入渠2 艦娘ID */
    private static long ndock2id;

    /** 入渠2 お風呂から上がる時間 */
    private static Date ndock2time;

    /** 入渠3 艦娘ID */
    private static long ndock3id;

    /** 入渠3 お風呂から上がる時間 */
    private static Date ndock3time;

    /** 入渠4 艦娘ID */
    private static long ndock4id;

    /** 入渠4 お風呂から上がる時間 */
    private static Date ndock4time;

    /** ログキュー */
    private static Queue<String> consoleQueue = new ArrayBlockingQueue<String>(10);

    /**
     * @return 装備Map
     */
    public static Map<String, ItemDto> getItemMap() {
        return Collections.unmodifiableMap(itemMap);
    }

    /**
     * @return 敵装備Map
     */
    public static Map<String, String> getEnemyItemMap() {
        return Collections.unmodifiableMap(enemyItemMap);
    }

    /**
     * @return 艦娘Map
     */
    public static Map<String, ShipDto> getShipMap() {
        return Collections.unmodifiableMap(shipMap);
    }

    /**
     * @return 秘書艦
     */
    public static ShipDto getSecretary() {
        return secretary;
    }

    /**
     * @return 建造艦娘List
     */
    public static List<GetShipDto> getGetshipList() {
        return Collections.unmodifiableList(getShipList);
    }

    /**
     * @return 開発アイテムList
     */
    public static List<CreateItemDto> getCreateItemList() {
        return Collections.unmodifiableList(createItemList);
    }

    /**
     * @return 海戦・ドロップList
     */
    public static List<BattleResultDto> getBattleResultList() {
        return Collections.unmodifiableList(battleResultList);
    }

    /**
     * @return 遠征1
     */
    public static DeckMissionDto getDeck1Mission() {
        return deck1Mission;
    }

    /**
     * @return 遠征2
     */
    public static DeckMissionDto getDeck2Mission() {
        return deck2Mission;
    }

    /**
     * @return 遠征3
     */
    public static DeckMissionDto getDeck3Mission() {
        return deck3Mission;
    }

    /** 
     * @return 入渠1 艦娘ID
     */
    public static long getNdock1id() {
        return ndock1id;
    }

    /**
     *  @return 入渠1 お風呂から上がる時間
     */
    public static Date getNdock1time() {
        return ndock1time;
    }

    /**
     *  @return 入渠2 艦娘ID
     */
    public static long getNdock2id() {
        return ndock2id;
    }

    /** 
     * @return 入渠2 お風呂から上がる時間
     */
    public static Date getNdock2time() {
        return ndock2time;
    }

    /**
     *  @return 入渠3 艦娘ID
     */
    public static long getNdock3id() {
        return ndock3id;
    }

    /** 
     * @return 入渠3 お風呂から上がる時間
     */
    public static Date getNdock3time() {
        return ndock3time;
    }

    /**
     * @return 入渠4 艦娘ID
     */
    public static long getNdock4id() {
        return ndock4id;
    }

    /** 
     * @return 入渠4 お風呂から上がる時間
     */
    public static Date getNdock4time() {
        return ndock4time;
    }

    /**
     * @return ログメッセージ
     */
    public static String getConsoleMessage() {
        return consoleQueue.poll();
    }

    /**
     * 情報を更新します
     */
    public static final void updateContext() {
        Data data;
        while ((data = DataQueue.poll()) != null) {
            switch (data.getDataType()) {
            // 保有装備
            case SLOTITEM_MEMBER:
                doSlotitemMember(data);
                break;
            // 保有装備
            case SLOTITEM_MASTER:
                doSlotitemMaster(data);
                break;
            // 保有艦
            case SHIP2:
                doShip2(data);
                break;
            // 遠征
            case DECK_PORT:
                doDeckPort(data);
                break;
            // 入渠
            case NDOCK:
                doNdock(data);
                break;
            // 建造
            case CREATESHIP:
                doCreateship(data);
                break;
            // 建造(入手)
            case GETSHIP:
                doGetship(data);
                break;
            // 装備開発
            case CREATEITEM:
                doCreateitem(data);
                break;
            // 海戦
            case BATTLERESULT:
                doBattleresult(data);
                break;
            default:
                break;
            }
        }
    }

    /**
     * 海戦情報を更新します
     * @param data
     */
    private static void doBattleresult(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            battleResultList.add(new BattleResultDto(apidata));

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
            getShipResource.put(
                    data.getField("api_kdock_id"),
                    new ResourceDto(
                            data.getField("api_item1"), data.getField("api_item2"), data.getField("api_item3"),
                            data.getField("api_item4"), secretary
                    ));

            addConsole("建造(投入資源)情報を更新しました");
        } catch (Exception e) {
            LOG.warn("建造(投入資源)情報を更新しますに失敗しました", e);
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
            String shipid = Long.toString(apidata.getJsonNumber("api_id").longValue());
            String dock = data.getField("api_kdock_id");

            getShipQueue.add(new String[] { shipid, dock });

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
                    data.getField("api_item3"), data.getField("api_item4"), secretary);

            CreateItemDto item = new CreateItemDto(apidata, resources);
            if (item.isCreateFlag()) {
                createItemQueue.add(item);
            } else {
                createItemList.add(item);
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
                ItemDto item = new ItemDto(object);
                itemMap.put(Long.toString(item.getId()), item);
            }
            // 確定待ちの開発装備がある場合、装備の名前を確定させます
            CreateItemDto createitem;
            while ((createitem = createItemQueue.poll()) != null) {
                ItemDto item = itemMap.get(Long.toString(createitem.getId()));
                if (item != null) {
                    createitem.setName(item.getName());
                    createitem.setType(item.getType());
                    createItemList.add(createitem);
                } else {
                    createItemQueue.add(createitem);
                }
            }

            addConsole("保有装備情報を更新しました");
        } catch (Exception e) {
            LOG.warn("保有装備を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 保有装備を更新します
     * 
     * @param data
     */
    private static void doSlotitemMaster(Data data) {
        try {
            JsonArray apidata = data.getJsonObject().getJsonArray("api_data");
            for (int i = 0; i < apidata.size(); i++) {
                JsonObject object = (JsonObject) apidata.get(i);
                String id = Long.toString(object.getJsonNumber("api_id").longValue());
                String name = object.getString("api_name");
                enemyItemMap.put(id, name);
            }

            addConsole("保有装備情報(敵)を更新しました");
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
    private static void doShip2(Data data) {
        try {
            JsonArray apidata = data.getJsonObject().getJsonArray("api_data");
            // 情報を破棄
            shipMap.clear();
            for (int i = 0; i < apidata.size(); i++) {
                ShipDto ship = new ShipDto((JsonObject) apidata.get(i));
                shipMap.put(Long.toString(ship.getId()), ship);
            }
            // 艦隊IDを追加
            JsonArray apidatadeck = data.getJsonObject().getJsonArray("api_data_deck");
            for (int i = 0; i < apidatadeck.size(); i++) {
                JsonObject jsonObject = (JsonObject) apidatadeck.get(i);
                String fleetid = Long.toString(jsonObject.getJsonNumber("api_id").longValue());
                JsonArray apiship = jsonObject.getJsonArray("api_ship");
                for (int j = 0; j < apiship.size(); j++) {
                    String shipid = Long.toString(((JsonNumber) apiship.get(j)).longValue());
                    ShipDto ship = shipMap.get(shipid);
                    if (ship != null) {
                        if ((i == 0) && (j == 0)) {
                            if ((secretary == null) || (ship.getId() != secretary.getId())) {
                                addConsole(ship.getName() + "(Lv" + ship.getLv() + ")" + " が秘書艦に任命されました");
                            }
                            // 秘書艦を設定
                            secretary = ship;
                        }
                        // 艦隊IDを設定
                        ship.setFleetid(fleetid);
                    }
                }
            }

            // 確定待ちの艦娘がある場合、艦娘の名前を確定させます
            String[] shipInfo;
            while ((shipInfo = getShipQueue.poll()) != null) {
                ShipDto getShip = shipMap.get(shipInfo[0]);
                if (getShip != null) {
                    getShipList.add(new GetShipDto(getShip, getShipResource.get(shipInfo[1])));
                } else {
                    getShipQueue.add(shipInfo);
                }
            }

            addConsole("保有艦娘情報を更新しました");
        } catch (Exception e) {
            LOG.warn("保有艦娘を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 遠征を更新します
     * 
     * @param data
     */
    private static void doDeckPort(Data data) {
        try {
            JsonArray apidata = data.getJsonObject().getJsonArray("api_data");
            for (int i = 1; i < apidata.size(); i++) {
                JsonObject object = (JsonObject) apidata.get(i);
                String name = object.getString("api_name");
                JsonArray jmission = object.getJsonArray("api_mission");

                long section = ((JsonNumber) jmission.get(1)).longValue();
                String mission = Deck.get(Long.toString(section));
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

                switch (i) {
                case 1:
                    deck1Mission = new DeckMissionDto(name, mission, time, fleetid, ships);
                    break;
                case 2:
                    deck2Mission = new DeckMissionDto(name, mission, time, fleetid, ships);
                    break;
                case 3:
                    deck3Mission = new DeckMissionDto(name, mission, time, fleetid, ships);
                    break;
                default:
                    break;
                }
            }

            addConsole("遠征情報を更新しました");
        } catch (Exception e) {
            LOG.warn("遠征を更新しますに失敗しました", e);
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
            for (int i = 0; i < apidata.size(); i++) {
                JsonObject object = (JsonObject) apidata.get(i);
                long id = object.getJsonNumber("api_ship_id").longValue();
                long milis = object.getJsonNumber("api_complete_time").longValue();

                Date time = null;
                if (milis > 0) {
                    time = new Date(milis);
                }

                switch (i) {
                case 0:
                    ndock1id = id;
                    ndock1time = time;
                    break;
                case 1:
                    ndock2id = id;
                    ndock2time = time;
                    break;
                case 2:
                    ndock3id = id;
                    ndock3time = time;
                    break;
                case 3:
                    ndock4id = id;
                    ndock4time = time;
                    break;
                default:
                    break;
                }
            }

            addConsole("入渠情報を更新しました");
        } catch (Exception e) {
            LOG.warn("入渠を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    private static void addConsole(Object message) {
        consoleQueue.add(FORMAT.format(Calendar.getInstance().getTime()) + "  " + message.toString());
    }
}
