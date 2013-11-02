/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.data.context;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.config.GlobalConfig;
import logbook.data.Data;
import logbook.data.DataQueue;
import logbook.dto.AwaitingDecision;
import logbook.dto.BattleDto;
import logbook.dto.BattleResultDto;
import logbook.dto.CreateItemDto;
import logbook.dto.DeckMissionDto;
import logbook.dto.DockDto;
import logbook.dto.GetShipDto;
import logbook.dto.ItemDto;
import logbook.dto.NdockDto;
import logbook.dto.ResourceDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;
import logbook.internal.Deck;
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

    /** ログに表示する日付書式 */
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat(GlobalConfig.DATE_SHORT_FORMAT);

    /** 装備Map */
    private static Map<Long, ItemDto> itemMap = new ConcurrentSkipListMap<Long, ItemDto>();

    /** 艦娘Map */
    private static Map<Long, ShipDto> shipMap = new ConcurrentSkipListMap<Long, ShipDto>();

    /** 秘書艦 */
    private static ShipDto secretary;

    /** 建造 */
    private static List<GetShipDto> getShipList = new ArrayList<GetShipDto>();

    /** 建造(艦娘名の確定待ち) */
    private static Queue<AwaitingDecision> getShipQueue = new ArrayBlockingQueue<AwaitingDecision>(10);

    /** 建造(投入資源) */
    private static Map<String, ResourceDto> getShipResource = new HashMap<String, ResourceDto>();

    /** 開発 */
    private static List<CreateItemDto> createItemList = new ArrayList<CreateItemDto>();

    /** 開発(装備名の確定待ち) */
    private static Queue<CreateItemDto> createItemQueue = new ArrayBlockingQueue<CreateItemDto>(10);

    /** 海戦・ドロップ */
    private static List<BattleResultDto> battleResultList = new ArrayList<BattleResultDto>();

    /** 司令部Lv */
    private static int hqLevel;

    /** 戦闘詳細 */
    private static Queue<BattleDto> battleList = new ArrayBlockingQueue<BattleDto>(1);

    /** 遠征リスト */
    private static DeckMissionDto[] deckMissions = new DeckMissionDto[] { DeckMissionDto.EMPTY, DeckMissionDto.EMPTY,
            DeckMissionDto.EMPTY };

    /** ドック */
    private static Map<String, DockDto> dock = new HashMap<String, DockDto>();

    /** 入渠リスト */
    private static NdockDto[] ndocks = new NdockDto[] { NdockDto.EMPTY, NdockDto.EMPTY, NdockDto.EMPTY,
            NdockDto.EMPTY };

    /** ログキュー */
    private static Queue<String> consoleQueue = new ArrayBlockingQueue<String>(10);

    /**
     * @return 装備Map
     */
    public static Map<Long, ItemDto> getItemMap() {
        return Collections.unmodifiableMap(itemMap);
    }

    /**
     * @return 艦娘Map
     */
    public static Map<Long, ShipDto> getShipMap() {
        return Collections.unmodifiableMap(shipMap);
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
     * @return ドック
     */
    public static DockDto getDock(String id) {
        return dock.get(id);
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
    public static void updateContext() {
        Data data;
        while ((data = DataQueue.poll()) != null) {
            // json保存設定
            if (GlobalConfig.getStoreJson()) {
                doStoreJson(data);
            }
            switch (data.getDataType()) {
            // 保有装備
            case SLOTITEM_MEMBER:
                doSlotitemMember(data);
                break;
            // 保有艦
            case SHIP2:
                doShip2(data);
                break;
            // 基本
            case BASIC:
                doBasic(data);
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
            case BATTLE:
            case BATTLE_MIDNIGHT:
            case BATTLE_SP_MIDNIGHT:
            case BATTLE_NIGHT_TO_DAY:
                doBattle(data);
                break;
            // 海戦結果
            case BATTLERESULT:
                doBattleresult(data);
                break;
            // 艦隊
            case DECK:
                doDeck(data);
                break;
            // 艦娘一覧
            case SHIP_MASTER:
                doShipMaster(data);
                break;
            default:
                break;
            }
        }
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
            File file = new File(FilenameUtils.concat(GlobalConfig.getStoreJsonPath(), fname));

            FileUtils.write(file, data.getJsonObject().toString());
        } catch (IOException e) {
            LOG.warn("JSONオブジェクトを保存するに失敗しました", e);
            LOG.warn(data);
        }

    }

    /**
     * 海戦情報を更新します
     * @param data
     */
    private static void doBattle(Data data) {
        try {
            if (battleList.size() == 0) {
                JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
                battleList.add(new BattleDto(apidata));

                addConsole("海戦情報を更新しました");
            }
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
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            battleResultList.add(new BattleResultDto(apidata, battleList.poll()));

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
                            data.getField("api_item4"), secretary, hqLevel
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
            long shipid = apidata.getJsonNumber("api_id").longValue();
            String dock = data.getField("api_kdock_id");

            getShipQueue.add(new AwaitingDecision(shipid, dock));

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
                itemMap.put(Long.valueOf(item.getId()), item);
            }
            // 確定待ちの開発装備がある場合、装備の名前を確定させます
            CreateItemDto createitem;
            while ((createitem = createItemQueue.poll()) != null) {
                ItemDto item = itemMap.get(Long.valueOf(createitem.getId()));
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

            // 確定待ちの艦娘がある場合、艦娘の名前を確定させます
            AwaitingDecision shipInfo;
            while ((shipInfo = getShipQueue.poll()) != null) {
                ShipDto getShip = shipMap.get(Long.valueOf(shipInfo.getShipid()));
                if (getShip != null) {
                    getShipList.add(new GetShipDto(getShip, getShipResource.get(shipInfo.getDock())));
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
    }

    /**
     * 司令部Lvを更新する
     * 
     * @param data
     */
    private static void doBasic(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");

            int newhqLevel = apidata.getJsonNumber("api_level").intValue();
            if (hqLevel != newhqLevel) {
                hqLevel = newhqLevel;
                addConsole("司令部Lvを更新しました");
            }
        } catch (Exception e) {
            LOG.warn("司令部Lvを更新するに失敗しました", e);
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

            deckMissions = new DeckMissionDto[] { DeckMissionDto.EMPTY, DeckMissionDto.EMPTY, DeckMissionDto.EMPTY };

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
                deckMissions[i - 1] = new DeckMissionDto(name, mission, time, fleetid, ships);
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

            addConsole("入渠情報を更新しました");
        } catch (Exception e) {
            LOG.warn("入渠を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 艦娘一覧を更新します
     * 
     * @param data
     */
    private static void doShipMaster(Data data) {
        JsonArray apidata = data.getJsonObject().getJsonArray("api_data");
        for (int i = 0; i < apidata.size(); i++) {
            JsonObject object = (JsonObject) apidata.get(i);

            String id = object.getJsonNumber("api_id").toString();

            Ship.set(id, toShipInfoDto(object));
        }

        addConsole("艦娘一覧を更新しました");
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
        int afterlv = object.getJsonNumber("api_afterlv").intValue();
        String flagship = object.getString("api_yomi");
        if ("-".equals(flagship)) {
            flagship = "";
        }
        return new ShipInfoDto(name, type, afterlv, flagship);
    }

    private static void addConsole(Object message) {
        consoleQueue.add(FORMAT.format(Calendar.getInstance().getTime()) + "  " + message.toString());
    }
}
