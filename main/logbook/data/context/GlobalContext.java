package logbook.data.context;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.config.AppConfig;
import logbook.config.ItemConfig;
import logbook.config.KdockConfig;
import logbook.constants.AppConstants;
import logbook.data.Data;
import logbook.dto.BasicInfoDto;
import logbook.dto.BattleExDto;
import logbook.dto.BattleExDto.Phase;
import logbook.dto.BattlePhaseKind;
import logbook.dto.BattleResultDto;
import logbook.dto.CreateItemDto;
import logbook.dto.DeckMissionDto;
import logbook.dto.DockDto;
import logbook.dto.GetShipDto;
import logbook.dto.ItemDto;
import logbook.dto.ItemInfoDto;
import logbook.dto.KdockDto;
import logbook.dto.LostEntityDto;
import logbook.dto.MapCellDto;
import logbook.dto.MaterialDto;
import logbook.dto.MissionResultDto;
import logbook.dto.NdockDto;
import logbook.dto.PracticeUserDetailDto;
import logbook.dto.PracticeUserDto;
import logbook.dto.QuestDto;
import logbook.dto.ResourceItemDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;
import logbook.gui.ApplicationMain;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.Sound;
import logbook.internal.BattleResultServer;
import logbook.internal.EnemyData;
import logbook.internal.Item;
import logbook.internal.MasterData;
import logbook.internal.Ship;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolTip;

/**
 * 遠征・入渠などの情報を管理します
 *
 */
public final class GlobalContext {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(GlobalContext.class);

    /** 装備Map */
    private static Map<Integer, ItemDto> itemMap = new TreeMap<Integer, ItemDto>();

    /** 艦娘Map */
    private static Map<Integer, ShipDto> shipMap = new TreeMap<Integer, ShipDto>();

    /** 秘書艦 */
    private static ShipDto secretary;

    /** 建造 */
    private static List<GetShipDto> getShipList = new ArrayList<GetShipDto>();

    /** 建造(投入資源) */
    private static Map<String, GetShipDto> getShipResource = new HashMap<String, GetShipDto>();

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

    /** 現在のマップ位置 */
    private static MapCellDto mapCellDto = null;

    /** 戦闘詳細 */
    private static BattleExDto battle = null;

    /** 遠征リスト */
    private static DeckMissionDto[] deckMissions = new DeckMissionDto[] { DeckMissionDto.EMPTY, DeckMissionDto.EMPTY,
            DeckMissionDto.EMPTY };

    /** ドック */
    private static Map<String, DockDto> dock = new HashMap<String, DockDto>();

    /** 入渠リスト */
    private static NdockDto[] ndocks = new NdockDto[] { NdockDto.EMPTY, NdockDto.EMPTY, NdockDto.EMPTY,
            NdockDto.EMPTY };

    /** 建造リスト */
    private static KdockDto[] kdocks = new KdockDto[] { KdockDto.EMPTY, KdockDto.EMPTY, KdockDto.EMPTY,
            KdockDto.EMPTY };

    /** 演習リスト */
    private static PracticeUserDto[] practiceUser = new PracticeUserDto[] { null, null, null, null, null };

    /** 最後に演習リストが更新された時間 */
    private static Date practiceUserLastUpdate = null;

    /** 任務Map */
    private static ArrayList<QuestDto> questList = new ArrayList<QuestDto>();

    /** 最後に任務情報を受け取った時間 */
    private static Date questLastUpdate;

    /** 出撃中か */
    private static boolean[] isSortie = new boolean[4];

    /**　ユーザ基本情報 */
    private static BasicInfoDto basic;

    /** updateContext() が呼ばれた数 */
    private static int updateCounter = 0;

    /** 保有資源・資材 */
    private static MaterialDto material = null;

    /** 最後に資源ログに追加した時間 */
    volatile private static Date materialLogLastUpdate = null;

    /** 連合艦隊 */
    private static boolean combined;

    /** 情報の取得状態 0:母港情報未受信 1:正常 2:マスターデータの更新が必要 3:アカウントが変わった！   */
    private static int state = 0;

    // 始めてアクセスがあった時に読み込む
    public static final boolean INIT_COMPLETE;
    static {
        ItemConfig.load();
        INIT_COMPLETE = true;
    }

    private static enum MATERIAL_DIFF {
        NEW_VALUE,
        OBTAINED,
        CONSUMED,
        NONE;
    }

    /**
     * @return 装備Map
     */
    public static Map<Integer, ItemDto> getItemMap() {
        return itemMap;
    }

    /**
     * 装備を復元する
     * @param map
     */
    public static void setItemMap(Collection<ItemDto> items) {
        for (ItemDto item : items) {
            int id = item.getSlotitemId();
            ItemInfoDto info = Item.get(id);
            if (info != null) {
                item.setInfo(info);
                itemMap.put(item.getId(), item);
            }
        }
    }

    /**
     * @return 艦娘Map
     */
    public static Map<Integer, ShipDto> getShipMap() {
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
     * @param list 建造艦娘List
     */
    public static void addGetshipList(List<GetShipDto> list) {
        getShipList.addAll(list);
    }

    /**
     * @return 開発アイテムList
     */
    public static List<CreateItemDto> getCreateItemList() {
        return createItemList;
    }

    /**
     * @param list 開発アイテムList
     */
    public static void addCreateItemList(List<CreateItemDto> list) {
        createItemList.addAll(list);
    }

    /**
     * @return 海戦・ドロップList
     */
    public static List<BattleResultDto> getBattleResultList() {
        return battleResultList;
    }

    /**
     * @return 最後に行った海戦情報
     */
    public static BattleExDto getLastBattleDto() {
        return battle;
    }

    /**
     * @return 遠征結果
     */
    public static List<MissionResultDto> getMissionResultList() {
        return missionResultList;
    }

    /**
     * @param list 遠征結果
     */
    public static void addMissionResultList(List<MissionResultDto> list) {
        missionResultList.addAll(list);
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
     * @return 建造ドックリスト
     */
    public static KdockDto[] getKdocks() {
        return kdocks;
    }

    /**
     * @return 遠征中の艦セット
     */
    public static Set<Integer> getMissionShipSet() {
        Set<Integer> set = new HashSet<Integer>();
        for (DeckMissionDto deckMission : deckMissions) {
            if ((deckMission.getMission() != null) && (deckMission.getShips() != null)) {
                set.addAll(deckMission.getShips());
            }
        }
        return set;
    }

    /**
     * @return 入渠中の艦セット
     */
    public static Set<Integer> getNDockShipSet() {
        Set<Integer> set = new HashSet<Integer>();
        for (NdockDto ndock : ndocks) {
            if (ndock.getNdockid() != 0) {
                set.add(ndock.getNdockid());
            }
        }
        return set;
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
    public static boolean isNdock(int ship) {
        for (NdockDto ndock : ndocks) {
            if (ship == ndock.getNdockid()) {
                return true;
            }
        }
        return false;
    }

    public static PracticeUserDto[] getPracticeUser() {
        return practiceUser;
    }

    public static Date getPracticeUserLastUpdate() {
        return practiceUserLastUpdate;
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
     * @return ドックMap
     */
    public static Map<String, DockDto> getDock() {
        return dock;
    }

    public static boolean[] getIsSortie() {
        return isSortie;
    }

    public static MapCellDto getSortieMap() {
        return mapCellDto;
    }

    /**
     * 任務を取得します
     * @return 任務
     */
    public static List<QuestDto> getQuest() {
        return questList;
    }

    public static Date getQuestLastUpdate() {
        return questLastUpdate;
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
     * 提督の名前や入渠ドックの数など基本的なユーザの情報を取得します
     * @return 基本ユーザ情報
     */
    @CheckForNull
    public static BasicInfoDto getBasicInfo() {
        return basic;
    }

    /**
     * 連合艦隊を組んでいるかを取得します
     * @return 連合艦隊を組んでいるか
     */
    public static boolean isCombined() {
        return combined;
    }

    /**
     * データ受信状態
     * @return 0:母港情報未受信 1:正常 2:未取得のデータ有り
     */
    public static int getState() {
        return state;
    }

    /**
     * 情報を更新します
     * 
     * @return 更新する情報があった場合trueを返します
     */
    public static void updateContext(Data data) {
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
        // 艦娘ロック操作
        case LOCK_SHIP:
            doLockShip(data);
            break;
        // 装備ロック操作
        case LOCK_SLOTITEM:
            doLockSlotitem(data);
            break;
        // 装備改修
        case REMODEL_SLOT:
            doRemodelSlot(data);
            break;
        // 海戦
        case BATTLE:
            doBattle(data, BattlePhaseKind.BATTLE);
            break;
        // 海戦
        case BATTLE_MIDNIGHT:
            doBattle(data, BattlePhaseKind.MIDNIGHT);
            break;
        // 海戦
        case BATTLE_SP_MIDNIGHT:
            doBattle(data, BattlePhaseKind.SP_MIDNIGHT);
            break;
        // 海戦
        case BATTLE_NIGHT_TO_DAY:
            doBattle(data, BattlePhaseKind.NIGHT_TO_DAY);
            break;
        // 海戦
        case COMBINED_AIR_BATTLE:
            doBattle(data, BattlePhaseKind.COMBINED_AIR);
            break;
        // 海戦
        case COMBINED_BATTLE:
            doBattle(data, BattlePhaseKind.COMBINED_BATTLE);
            break;
        // 海戦
        case COMBINED_BATTLE_MIDNIGHT:
            doBattle(data, BattlePhaseKind.COMBINED_MIDNIGHT);
            break;
        // 海戦
        case COMBINED_BATTLE_SP_MIDNIGHT:
            doBattle(data, BattlePhaseKind.COMBINED_SP_MIDNIGHT);
            break;
        case COMBINED_BATTLE_WATER:
            doBattle(data, BattlePhaseKind.COMBINED_BATTLE_WATER);
            break;
        // 海戦結果
        case BATTLE_RESULT:
            doBattleresult(data);
            break;
        // 海戦結果
        case COMBINED_BATTLE_RESULT:
            doBattleresult(data);
            break;
        // 演習
        case PRACTICE_BATTLE:
            doBattle(data, BattlePhaseKind.PRACTICE_BATTLE);
            break;
        // 演習
        case PRACTICE_BATTLE_MIDNIGHT:
            doBattle(data, BattlePhaseKind.PRACTICE_MIDNIGHT);
            break;
        // 演習結果
        case PRACTICE_BATTLE_RESULT:
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
        // 進撃
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
        // マップ情報
        case MAPINFO:
            doMapInfo(data);
            break;
        // 任務情報
        case MISSION:
            doMission(data);
            break;
        // 演習
        case PRACTICE:
            doPractice(data);
            break;
        // 演習情報 
        case PRACTICE_ENEMYINFO:
            doPracticeEnemyinfo(data);
            break;
        // 連合艦隊
        case COMBINED:
            doCombined(data);
            break;
        // 入渠開始
        case NYUKYO_START:
            doNyukyoStart(data);
            break;
        // 高速修復
        case NYUKYO_SPEEDCHANGE:
            doSpeedChange(data);
            break;
        default:
            break;
        }
        ++updateCounter;
    }

    /** 
     * @return updateContext()が呼ばれた数
     */
    public static int getUpdateCounter() {
        return updateCounter;
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

            FileUtils.write(file, data.getJsonObject().toString(), Charset.forName("UTF-8"));
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

                    int shipid = shipobj.getInt("api_id");
                    int fuel = shipobj.getInt("api_fuel");
                    int bull = shipobj.getInt("api_bull");

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
                addUpdateLog("補給を更新しました");
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
            int shipid = Integer.valueOf(data.getField("api_ship_id"));
            int shipidx = Integer.valueOf(data.getField("api_ship_idx"));

            DockDto dockdto = dock.get(fleetid);

            if (dockdto != null) {
                List<ShipDto> ships = dockdto.getShips();

                if (shipidx == -1) {
                    // 旗艦以外解除
                    for (int i = 1; i < ships.size(); ++i) {
                        ships.get(i).setFleetid("");
                    }
                    dockdto.removeExceptFlagship();
                } else {
                    // 入れ替えまたは外す
                    // 入れ替え前の艦娘(いない場合はnull)
                    ShipDto cship = (shipidx < ships.size()) ? ships.get(shipidx) : null;
                    // 入れる艦娘(外す場合はnull)
                    ShipDto rship = shipMap.get(shipid);
                    // 入れる艦娘の現在の所属艦隊(ない場合はnull)
                    DockDto rdock = (rship != null) ? dock.get(rship.getFleetid()) : null;
                    int rdockPos = (rship != null) ? rship.getFleetpos() : 0;

                    // 艦隊IDを一旦全部外す
                    dockdto.removeFleetIdFromShips();
                    if (rdock != null) {
                        rdock.removeFleetIdFromShips();
                    }

                    // 入れる艦娘の前の位置を処理
                    if (rdock != null) {
                        // この場合 rship != null は既知
                        if (cship != null) {
                            // 入れ替え
                            rdock.setShip(rdockPos, cship);
                        }
                        else {
                            // 取る
                            rdock.removeShip(rship);
                        }
                    }

                    // 入れる位置を処理
                    if (rship == null) {
                        // 取る
                        dockdto.removeShip(cship);
                    }
                    else if (cship != null) {
                        // rship != null && cship != null
                        // 入れ替え
                        dockdto.setShip(shipidx, rship);
                    }
                    else {
                        // rship != null && cship == null
                        // 入れる
                        dockdto.addShip(rship);
                    }

                    // 艦隊IDを付け直す
                    dockdto.updateFleetIdOfShips();
                    dockdto.setUpdate(true);
                    if (rdock != null) {
                        rdock.updateFleetIdOfShips();
                        rdock.setUpdate(true);
                    }
                }
                DockDto firstdock = dock.get("1");
                if (firstdock != null) {
                    // 秘書艦を再設定
                    setSecretary(firstdock.getShips().get(0));
                }
            }
            addUpdateLog("編成を更新しました");
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
                for (int i = 0; i < isSortie.length; ++i) {
                    if (isSortie[i]) {
                        ApplicationMain.main.endSortie();
                        break;
                    }
                }
                Arrays.fill(isSortie, false);

                // 戦闘結果がある場合、ダメージ計算があっているか検証します
                if ((battle != null) && (battle.getDock() != null) && (battle.isPractice() == false)) {
                    checkBattleDamage(battle.getFriends().get(0).getShips(), battle.getLastPhase().getNowFriendHp());
                    if (battle.isCombined()) {
                        checkBattleDamage(battle.getFriends().get(1).getShips(),
                                battle.getLastPhase().getNowFriendHpCombined());
                    }
                }
                mapCellDto = null;
                battle = null;

                // 基本情報を更新する
                JsonObject apiBasic = apidata.getJsonObject("api_basic");
                doBasicSub(apiBasic);
                //addConsole("司令部を更新しました");

                // 保有資材を更新する
                JsonArray apiMaterial = apidata.getJsonArray("api_material");
                doMaterialSub(apiMaterial);
                //addConsole("保有資材を更新しました");

                // 保有艦娘を更新する
                shipMap.clear();
                JsonArray apiShip = apidata.getJsonArray("api_ship");
                for (int i = 0; i < apiShip.size(); i++) {
                    ShipDto ship = new ShipDto((JsonObject) apiShip.get(i));
                    shipMap.put(Integer.valueOf(ship.getId()), ship);
                }
                JsonArray apiDeckPort = apidata.getJsonArray("api_deck_port");
                doDeck(apiDeckPort);
                //addConsole("保有艦娘情報を更新しました");

                // 入渠の状態を更新する
                JsonArray apiNdock = apidata.getJsonArray("api_ndock");
                doNdockSub(apiNdock);
                //addConsole("入渠情報を更新しました");

                //addConsole("遠征情報を更新しました");

                // 連合艦隊を更新する
                combined = false;
                if (apidata.containsKey("api_combined_flag")) {
                    switch (apidata.getJsonNumber("api_combined_flag").intValue()) {
                    case 1:
                    case 2:
                        combined = true;
                        break;
                    default:
                        break;
                    }
                    //addConsole("連合艦隊を更新しました");
                }

                state = checkDataState();

                addUpdateLog("母港情報を更新しました");
            }
        } catch (Exception e) {
            LOG.warn("母港を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    private static void checkShipSunk(ShipDto ship, int nowhp, List<ShipDto> sunkShips) {
        if (ship.getNowhp() > 0) { // 轟沈している艦は更新しない
            ship.setNowhp(nowhp);
            if (ship.getNowhp() == 0) { // 轟沈した
                sunkShips.add(ship);
                CreateReportLogic.storeLostReport(LostEntityDto.make(ship, "艦娘の轟沈"));
            }
        }
    }

    /**
     * 海戦情報を更新します
     * @param data
     */
    private static void doBattle(Data data, BattlePhaseKind phaseKind) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            if (battle == null) {
                battle = new BattleExDto(data.getCreateDate());
            }
            BattleExDto.Phase phase = battle.addPhase(apidata, phaseKind);

            if (battle.getDock() == null) { // 情報が不足しているので処理しない
                battle = null;
                return;
            }

            List<ShipDto> sunkShips = new ArrayList<ShipDto>();
            List<ShipDto> ships = battle.getFriends().get(0).getShips();
            int[] nowFriendHp = phase.getNowFriendHp();

            if (battle.getDock().getShips().size() != nowFriendHp.length) { // 情報が不足しているので処理しない
                battle = null;
                return;
            }

            if ((phaseKind != BattlePhaseKind.PRACTICE_BATTLE) &&
                    (phaseKind != BattlePhaseKind.PRACTICE_MIDNIGHT))
            { // 演習ではやらない
                for (int i = 0; i < ships.size(); ++i) {
                    checkShipSunk(ships.get(i), nowFriendHp[i], sunkShips);
                }
                if (battle.isCombined()) {
                    List<ShipDto> shipsCombined = battle.getFriends().get(1).getShips();
                    int[] nowFriendHpCombined = phase.getNowFriendHpCombined();
                    for (int i = 0; i < shipsCombined.size(); ++i) {
                        checkShipSunk(shipsCombined.get(i), nowFriendHpCombined[i], sunkShips);
                    }
                }
            }

            addConsole("海戦情報を更新しました");
            if (AppConfig.get().isPrintSortieLog()) {
                addConsole("自=" + Arrays.toString(phase.getNowFriendHp()));
                if (battle.isCombined()) {
                    addConsole("連=" + Arrays.toString(phase.getNowFriendHpCombined()));
                }
                addConsole("敵=" + Arrays.toString(phase.getNowEnemyHp()));
                addConsole("→ " + phase.getEstimatedRank().toString());
            }
            if (AppConfig.get().isPrintSunkLog()) {
                for (ShipDto ship : sunkShips) {
                    addConsole(ship.getName() + "(id:" + ship.getId() + ",lv:" + ship.getLv() + ") 轟沈しました！");
                }
            }

            if (mapCellDto == null) {
                // 出撃していない場合は出撃させる
                for (DockDto dock : battle.getFriends()) {
                    isSortie[Integer.parseInt(dock.getId()) - 1] = true;
                }
                ApplicationMain.main.startSortie();
            }
            ApplicationMain.main.updateBattle(battle);

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
                battle.setResult(apidata, mapCellDto);

                if (battle.isCompleteResult()) { // 情報が不足している場合は記録しない
                    BattleResultServer.get().addNewResult(battle);
                }

                if (battle.isPractice() == false) { // 演習は記録しない
                    //battleResultList.add(battle);
                    CreateReportLogic.storeBattleResultReport(battle);

                    // EnemyData更新
                    if (mapCellDto != null) {
                        int enemyId = mapCellDto.getEnemyId();
                        EnemyData enemyData = battle.getEnemyData(enemyId, battle.getEnemyName());
                        if ((mapCellDto.getEnemyData() == null) || (mapCellDto.getEnemyData().getEnemyName() == null)) {
                            addConsole("eid=" + enemyId + "の敵編成をデータべースに追加");
                        }
                        EnemyData.set(enemyId, enemyData);
                        mapCellDto.setEnemyData(enemyData);
                    }
                }

                // 警告を出すためにバージョンアップ
                battle.getDock().setUpdate(true);
                if (battle.isCombined()) {
                    battle.getDockCombined().setUpdate(true);
                }

                // ランクが合っているかチェック
                Phase lastPhase = battle.getLastPhase();
                if (!battle.getRank().equals(lastPhase.getEstimatedRank())) {
                    LOG.info("戦闘結果判定ミス: 正解ランク:" + battle.getRank() + " " + lastPhase.getRankCalcInfo(battle));
                }

                for (int i = 0; i < (battleResultList.size() - AppConstants.MAX_LOG_SIZE); i++) {
                    battleResultList.remove(0);
                }
            }
            addUpdateLog("海戦結果を更新しました");
        } catch (Exception e) {
            LOG.warn("海戦結果を更新しますに失敗しました", e);
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
            ResourceItemDto res = new ResourceItemDto();
            res.loadBaseMaterialsFromField(data);
            res.setResearchMaterials(Integer.parseInt(data.getField("api_item5")));
            GetShipDto resource = new GetShipDto(
                    Integer.parseInt(data.getField("api_large_flag")) == 1,
                    res, secretary, hqLevel, -1);
            lastBuildKdock = kdockid;
            getShipResource.put(kdockid, resource);
            KdockConfig.store(kdockid, resource);

            // 資源に反映させてレポート
            updateDetailedMaterial("建造", res, MATERIAL_DIFF.CONSUMED);

            addUpdateLog("建造(投入資源)情報を更新しました");
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
            JsonArray apidata = data.getJsonObject().getJsonArray("api_data");

            // 建造ドックの空きをカウントします
            if (lastBuildKdock != null) {
                GetShipDto resource = getShipResource.get(lastBuildKdock);
                if (resource != null) {
                    int freecount = 0;
                    for (int i = 0; i < apidata.size(); i++) {
                        int state = ((JsonObject) apidata.get(i)).getJsonNumber("api_state").intValue();
                        if (state == 0) {
                            freecount++;
                        }
                    }
                    // 建造ドックの空きをセットします
                    resource.setFreeDock(freecount);
                    KdockConfig.store(lastBuildKdock, resource);
                }
            }

            // 建造ドック更新
            doKdockSub(apidata);

            addUpdateLog("建造を更新しました");
        } catch (Exception e) {
            LOG.warn("建造を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    private static void doKdockSub(JsonArray apidata) {
        kdocks = new KdockDto[] { KdockDto.EMPTY, KdockDto.EMPTY, KdockDto.EMPTY, KdockDto.EMPTY };
        for (int i = 0; i < apidata.size(); i++) {
            JsonObject object = (JsonObject) apidata.get(i);
            int state = object.getJsonNumber("api_state").intValue();
            long milis = object.getJsonNumber("api_complete_time").longValue();

            Date time = null;
            if (milis > 0) {
                time = new Date(milis);
                kdocks[i] = new KdockDto(true, time);
            }
            else {
                // 完了してる or 空いてる
                kdocks[i] = new KdockDto(state == 3, null);
            }
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
            JsonValue slotitem = apidata.get("api_slotitem");
            // まるゆは JsonValue.NULL になるので注意！
            if ((slotitem != null) && (slotitem != JsonValue.NULL)) {
                JsonArray slotitemArray = (JsonArray) slotitem;
                for (int i = 0; i < slotitemArray.size(); i++) {
                    addSlotitem((JsonObject) slotitemArray.get(i));
                }
            }
            // 艦娘を追加します
            JsonObject apiShip = apidata.getJsonObject("api_ship");
            ShipDto ship = new ShipDto(apiShip);
            shipMap.put(Integer.valueOf(ship.getId()), ship);
            // 投入資源を取得する
            GetShipDto dto = getShipResource.get(dock);
            if (dto == null) {
                dto = KdockConfig.load(dock);
            }
            dto.setShip(ship);
            getShipList.add(dto);
            CreateReportLogic.storeCreateShipReport(dto);
            // 投入資源を除去する
            getShipResource.remove(dock);
            KdockConfig.remove(dock);

            // 建造ドック更新
            doKdockSub(apidata.getJsonArray("api_kdock"));

            state = checkDataState();

            addUpdateLog("建造(入手)情報を更新しました");
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
            ResourceItemDto res = new ResourceItemDto();
            res.loadBaseMaterialsFromField(data);
            CreateItemDto createitem = new CreateItemDto(apidata, res, secretary, hqLevel);
            if (createitem.isCreateFlag()) {
                ItemDto item = addSlotitem(apidata.getJsonObject("api_slot_item"));
                if (item != null) {
                    createitem.setName(item.getName());
                    createitem.setType(item.getTypeName());
                    createItemList.add(createitem);
                }
            } else {
                createItemList.add(createitem);
            }
            CreateReportLogic.storeCreateItemReport(createitem);

            // 資源に反映させてレポート
            JsonArray newMaterial = apidata.getJsonArray("api_material");
            ResourceItemDto items = new ResourceItemDto();
            items.loadMaterialFronJson(newMaterial);
            updateDetailedMaterial("装備開発", items, MATERIAL_DIFF.NEW_VALUE);

            state = checkDataState();

            addUpdateLog("装備開発情報を更新しました");
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
                addSlotitem(object);
            }

            state = checkDataState();

            addUpdateLog("保有装備情報を更新しました");
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
                int shipid = Integer.parseInt(shipidstr);
                for (int i = 0; i < shipdata.size(); i++) {
                    ShipDto ship = new ShipDto((JsonObject) shipdata.get(i));
                    shipMap.put(shipid, ship);
                }
            } else {
                // 情報を破棄
                shipMap.clear();
                for (int i = 0; i < shipdata.size(); i++) {
                    ShipDto ship = new ShipDto((JsonObject) shipdata.get(i));
                    shipMap.put(ship.getId(), ship);
                }
            }
            // 艦隊を設定
            doDeck(apidata.getJsonArray("api_deck_data"));

            state = checkDataState();

            addUpdateLog("保有艦娘情報３を更新しました");
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
                shipMap.put(ship.getId(), ship);
            }

            // 戦闘結果がある場合、ダメージ計算があっているか検証します
            if ((battle != null) && (battle.getDock() != null) && (battle.isPractice() == false)) {
                checkBattleDamage(battle.getDock().getShips(), battle.getNowFriendHp());
                if (battle.isCombined()) {
                    checkBattleDamage(battle.getDockCombined().getShips(), battle.getNowFriendHpCombined());
                }
            }

            // 艦隊を設定
            doDeck(data.getJsonObject().getJsonArray("api_data_deck"));

            if (battle != null) {
                ApplicationMain.main.updateSortieDock();
            }

            battle = null;

            addUpdateLog("保有艦娘情報２を更新しました");
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

            addUpdateLog("艦隊を更新しました");
        } catch (Exception e) {
            LOG.warn("艦隊を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 艦隊と遠征の状態を更新します
     * 
     * @param apidata
     */
    private static void doDeck(JsonArray apidata) {
        dock.clear();
        for (int i = 0; i < apidata.size(); i++) {
            JsonObject jsonObject = (JsonObject) apidata.get(i);
            int fleetid = jsonObject.getInt("api_id");
            String fleetidstr = String.valueOf(fleetid);
            String name = jsonObject.getString("api_name");
            JsonArray apiship = jsonObject.getJsonArray("api_ship");

            DockDto dockdto = new DockDto(fleetidstr, name);
            List<Integer> shipIds = new ArrayList<Integer>();
            dock.put(fleetidstr, dockdto);

            for (int j = 0; j < apiship.size(); j++) {
                int shipId = apiship.getInt(j);
                shipIds.add(shipId);

                ShipDto ship = shipMap.get(shipId);
                if (ship != null) {
                    dockdto.addShip(ship);

                    if ((i == 0) && (j == 0)) {
                        setSecretary(ship);
                    }
                    // 艦隊IDを設定
                    ship.setFleetid(fleetidstr);
                    ship.setFleetpos(j);
                }
            }

            if (i >= 1) {
                JsonArray jmission = jsonObject.getJsonArray("api_mission");
                int section = ((JsonNumber) jmission.get(1)).intValue();
                long milis = ((JsonNumber) jmission.get(2)).longValue();
                Date time = null;
                if (milis > 0) {
                    time = new Date(milis);
                }
                deckMissions[i - 1] = new DeckMissionDto(name, section, time, fleetid, shipIds);
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
            int shipid = Integer.valueOf(data.getField("api_ship_id"));
            ShipDto ship = shipMap.get(shipid);
            if (ship != null) {
                // レポート
                CreateReportLogic.storeLostReport(LostEntityDto.make(ship, "艦娘の解体"));

                // 持っている装備を廃棄する
                for (int item : ship.getItemId()) {
                    itemMap.remove(item);
                }
                // 艦娘を外す
                shipMap.remove(ship.getId());
                // 艦隊からも外す
                String fleetid = ship.getFleetid();
                if (fleetid != null) {
                    DockDto dockdto = dock.get(fleetid);
                    if (dockdto != null) {
                        dockdto.removeShip(ship);
                        dockdto.setUpdate(true);
                    }
                }
            }

            addUpdateLog("艦娘を解体しました");
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
            List<LostEntityDto> dtoList = new ArrayList<LostEntityDto>();
            for (String itemid : itemids.split(",")) {
                int item = Integer.valueOf(itemid);
                ItemDto itemDto = itemMap.get(item);
                if (itemDto != null) {
                    dtoList.add(LostEntityDto.make(item, itemDto));
                }
                itemMap.remove(item);
            }
            // 記録する
            CreateReportLogic.storeLostReport(dtoList);
            addUpdateLog("装備を廃棄しました");
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
            // 近代化改修に使った艦を取り除く
            String shipids = data.getField("api_id_items");
            for (String shipid : shipids.split(",")) {
                ShipDto ship = shipMap.get(Integer.valueOf(shipid));
                if (ship != null) {
                    // 記録する
                    CreateReportLogic.storeLostReport(LostEntityDto.make(ship, "近代化改修"));
                    // 持っている装備を廃棄する
                    for (int item : ship.getItemId()) {
                        itemMap.remove(item);
                    }
                    // 艦娘を外す
                    shipMap.remove(ship.getId());
                    // 艦隊からも外す
                    String fleetid = ship.getFleetid();
                    if (fleetid != null) {
                        DockDto dockdto = dock.get(fleetid);
                        if (dockdto != null) {
                            dockdto.removeShip(ship);
                            dockdto.setUpdate(true);
                            dockdto.updateFleetIdOfShips();
                        }
                    }
                }
            }

            // 近代化改修された艦を更新する
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            ShipDto ship = new ShipDto(apidata.getJsonObject("api_ship"));
            int id = ship.getId();
            // 艦隊情報を引き継ぐ
            ShipDto oldShip = shipMap.get(id);
            String fleetid = oldShip.getFleetid();
            if (fleetid != null) {
                DockDto dockdto = dock.get(fleetid);
                if (dockdto != null) {
                    ship.setFleetid(fleetid);
                    ship.setFleetpos(oldShip.getFleetpos());
                    dockdto.setShip(ship.getFleetpos(), ship);
                }
            }
            shipMap.put(id, ship);

            addUpdateLog("近代化改修しました");
        } catch (Exception e) {
            LOG.warn("近代化改修しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 艦娘ロックを更新する
     * 
     * @param data
     */
    private static void doLockShip(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            int shipId = Integer.valueOf(data.getField("api_ship_id"));
            boolean locked = apidata.getInt("api_locked") != 0;

            ShipDto dto = shipMap.get(shipId);
            if (dto != null) {
                dto.setLocked(locked);
            }

            addUpdateLog("艦娘ロックを更新しました");
        } catch (Exception e) {
            LOG.warn("艦娘ロックを更新するに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 装備ロックを更新する
     * 
     * @param data
     */
    private static void doLockSlotitem(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            int slotitemId = Integer.valueOf(data.getField("api_slotitem_id"));
            boolean locked = apidata.getInt("api_locked") != 0;

            ItemDto dto = itemMap.get(slotitemId);
            if (dto != null) {
                dto.setLocked(locked);
            }

            addUpdateLog("装備ロックを更新しました");
        } catch (Exception e) {
            LOG.warn("装備ロックを更新するに失敗しました", e);
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

            addUpdateLog("司令部を更新しました");
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
        // 残り全部
        BasicInfoDto old = basic;
        basic = new BasicInfoDto(apidata);
        if ((old != null) && (old.getMemberId() != basic.getMemberId())) {
            // アカウントが変わった
            state = 3;
        }
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

            addUpdateLog("保有資材を更新しました");
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
        dto.setEvent("定期更新");

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
            case AppConstants.MATERIAL_SCREW:
                dto.setScrew(entry.getInt("api_value"));
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

            int clearResult = apidata.getJsonNumber("api_clear_result").intValue();
            String questName = apidata.getString("api_quest_name");
            ResourceItemDto res = new ResourceItemDto();

            if (clearResult != 0) {
                // 資源に反映させてレポート
                res.loadMissionResult(apidata);
                updateDetailedMaterial("遠征帰還", res, MATERIAL_DIFF.OBTAINED);
            }

            MissionResultDto result = new MissionResultDto(clearResult, questName, res);

            CreateReportLogic.storeMissionReport(result);
            missionResultList.add(result);

            state = checkDataState();

            addUpdateLog("遠征(帰還)情報を更新しました");
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

            addUpdateLog("入渠情報を更新しました");
        } catch (Exception e) {
            LOG.warn("入渠を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    // 修理が終わった艦はHPを回復させる
    private static void ndockFinished(int shipId) {
        ShipDto ship = shipMap.get(shipId);
        if (ship != null) {
            ship.setNowhp(ship.getMaxhp());
            ship.setDockTime(0);
        }
    }

    /**
     * 入渠を更新します
     * @param apidata
     */
    private static void doNdockSub(JsonArray apidata) {
        for (int i = 0; i < apidata.size(); i++) {
            JsonObject object = (JsonObject) apidata.get(i);
            int id = object.getJsonNumber("api_ship_id").intValue();
            long milis = object.getJsonNumber("api_complete_time").longValue();

            Date time = null;
            if (milis > 0) {
                time = new Date(milis);
                ndocks[i] = new NdockDto(id, time);
            }
            else if (ndocks[i].getNdocktime() != null) {
                ndockFinished(ndocks[i].getNdockid());
                ndocks[i] = NdockDto.EMPTY;
            }
        }
    }

    /**
     * 入渠開始
     * @param apidata
     */
    private static void doNyukyoStart(Data data) {
        try {
            int id = Integer.valueOf(data.getField("api_ship_id"));
            boolean highspeed = data.getField("api_highspeed").equals("1");

            if (highspeed) {
                ndockFinished(id);
            }

            // 高速修復出ない場合は直後にndockが送られてくる
            addUpdateLog("入渠情報を更新しました");
        } catch (Exception e) {
            LOG.warn("入渠を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 入渠中に高速修復を使った
     * @param apidata
     */
    private static void doSpeedChange(Data data) {
        try {
            int id = Integer.valueOf(data.getField("api_ndock_id"));

            ndockFinished(ndocks[id - 1].getNdockid());
            ndocks[id - 1] = NdockDto.EMPTY;

            addUpdateLog("バケツを使いました");
        } catch (Exception e) {
            LOG.warn("入渠を更新しますに失敗しました", e);
            LOG.warn(data);
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
            int items_per_page = 5;
            int disp_page = apidata.getJsonNumber("api_disp_page").intValue();
            int page_count = apidata.getJsonNumber("api_page_count").intValue();
            if (page_count == 0) { // 任務が１つもない時
                questList.clear();
                questLastUpdate = new Date();
            }
            else if ((disp_page > page_count) || apidata.isNull("api_list")) {
                // 表示ページが全体ページ数より後ろの場合は任務情報が何も送られてこない
            }
            else {
                Date now = new Date();
                // 足りない要素を足す
                for (int i = questList.size(); i < (page_count * items_per_page); ++i) {
                    questList.add(null);
                }
                // 余分な要素は削る
                for (int i = questList.size() - 1; i >= (page_count * items_per_page); --i) {
                    questList.remove(i);
                }
                int pos = 1;
                for (JsonValue value : apidata.getJsonArray("api_list")) {
                    if (value instanceof JsonObject) {
                        JsonObject questobject = (JsonObject) value;
                        // 任務を作成
                        int index = ((disp_page - 1) * items_per_page) + (pos - 1);
                        QuestDto quest = new QuestDto();
                        quest.setTime(now);
                        quest.setNo(questobject.getInt("api_no"));
                        quest.setPage(disp_page);
                        quest.setPos(pos++);
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

                        questList.set(index, quest);
                    }
                }
                if (pos <= items_per_page) {
                    // 空白がある場合は削る
                    for (int i = questList.size() - 1; i >= (((disp_page - 1) * items_per_page) + (pos - 1)); --i) {
                        questList.remove(i);
                    }
                }
                // 全て揃った？
                if (questList.contains(null) == false) {
                    Date updateTime = now;
                    for (QuestDto quest : questList) {
                        if (updateTime.after(quest.getTime())) {
                            updateTime = quest.getTime();
                        }
                    }
                    questLastUpdate = updateTime;
                }
            }
            addUpdateLog("任務を更新しました");
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
            /*
            String idstr = data.getField("api_quest_id");
            if (idstr != null) {
                Integer id = Integer.valueOf(idstr);
                questMap.remove(id);
            }
            */
            // 資源に反映させてレポート
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            ResourceItemDto items = new ResourceItemDto();
            items.loadQuestClear(apidata);
            updateDetailedMaterial("任務をクリア", items, MATERIAL_DIFF.OBTAINED);

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
                // 連合艦隊
                if ((id == 1) && combined) {
                    isSortie[1] = true;
                }
            }

            JsonObject obj = data.getJsonObject().getJsonObject("api_data");

            mapCellDto = new MapCellDto(obj);
            updateDetailedMaterial("出撃", null, MATERIAL_DIFF.NONE);

            ApplicationMain.main.startSortie();
            ApplicationMain.main.updateMapCell(mapCellDto);

            addUpdateLog("出撃を更新しました");
            if (AppConfig.get().isPrintSortieLog())
                addConsole("行先 " + mapCellDto.toString());
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

            mapCellDto = new MapCellDto(obj);
            ApplicationMain.main.updateMapCell(mapCellDto);
            if (AppConfig.get().isPrintSortieLog())
                addConsole("行先 " + mapCellDto.toString());
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
                addUpdateLog("艦娘一覧を更新しました");

                // 装備一覧
                JsonArray apiMstSlotitem = obj.getJsonArray("api_mst_slotitem");
                for (int i = 0; i < apiMstSlotitem.size(); i++) {
                    JsonObject object = (JsonObject) apiMstSlotitem.get(i);
                    ItemInfoDto item = new ItemInfoDto(object);
                    int id = object.getJsonNumber("api_id").intValue();
                    Item.set(id, item);
                }
                addUpdateLog("装備一覧を更新しました");

                MasterData.updateMaster(obj);
            }

            addConsole("マスターデータを更新しました");
        } catch (Exception e) {
            LOG.warn("設定を更新しますに失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * マップ情報を処理します
     * 
     * @param data
     */
    private static void doMapInfo(Data data) {
        try {
            JsonArray apidata = data.getJsonObject().getJsonArray("api_data");
            if (apidata != null) {
                MasterData.updateMapInfo(apidata);
            }
            int shipSpace = maxChara - shipMap.size();
            int itemSpace = maxSlotitem - itemMap.size();
            // 装備の空き枠が少ない時はバルーンを出す
            if (AppConfig.get().isEnableItemFullBalloonNotify() &&
                    (itemSpace <= AppConfig.get().getItemFullBalloonNotify())) {
                ToolTip tip = new ToolTip(ApplicationMain.main.getShell(), SWT.BALLOON
                        | SWT.ICON_ERROR);
                tip.setText("装備の空き枠警告");
                tip.setMessage("装備の空き枠があと" + itemSpace + "個しかありません");
                ApplicationMain.main.getTrayItem().setToolTip(tip);
                tip.setVisible(true);
                Sound.randomWarningPlay();
            }
            // 艦娘の空き枠が少ない時はバルーンを出す
            else if (AppConfig.get().isEnableShipFullBalloonNotify() &&
                    (shipSpace <= AppConfig.get().getShipFullBalloonNotify())) {
                ToolTip tip = new ToolTip(ApplicationMain.main.getShell(), SWT.BALLOON
                        | SWT.ICON_ERROR);
                tip.setText("母港の空き警告");
                tip.setMessage("母港の空きがあと" + shipSpace + "隻分しかありません");
                ApplicationMain.main.getTrayItem().setToolTip(tip);
                tip.setVisible(true);
                Sound.randomWarningPlay();
            }
        } catch (Exception e) {
            LOG.warn("マップ情報更新に失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 任務情報を処理します
     * 
     * @param data
     */
    private static void doMission(Data data) {
        try {
            JsonArray apidata = data.getJsonObject().getJsonArray("api_data");
            if (apidata != null) {
                MasterData.updateMission(apidata);
            }
        } catch (Exception e) {
            LOG.warn("任務情報更新に失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 演習情報を処理します
     * 
     * @param data
     */
    private static void doPractice(Data data) {
        try {
            JsonArray apidata = data.getJsonObject().getJsonArray("api_data");
            for (int i = 0; i < apidata.size(); ++i) {
                PracticeUserDto dto = new PracticeUserDto((JsonObject) apidata.get(i));
                if ((practiceUser[i] == null) || (practiceUser[i].getId() != dto.getId()))
                    practiceUser[i] = dto;
                else
                    // stateだけ更新
                    practiceUser[i].setState(dto.getState());
            }
            practiceUserLastUpdate = new Date();
            addUpdateLog("演習情報を更新しました");
        } catch (Exception e) {
            LOG.warn("演習情報更新に失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 演習相手艦隊情報を処理します
     * @param data
     */
    private static void doPracticeEnemyinfo(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            PracticeUserDetailDto dto = new PracticeUserDetailDto(apidata);

            // 持っている情報をアップデートする
            for (int i = 0; i < 5; ++i) {
                if ((practiceUser[i] != null) && (practiceUser[i].getId() == dto.getId())) {
                    practiceUser[i] = dto;
                    break;
                }
            }

            ApplicationMain.main.updateCalcPracticeExp(dto);
            addUpdateLog("演習相手艦隊情報を更新しました");
        } catch (Exception e) {
            LOG.warn("演習相手艦隊情報更新に失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 連合艦隊操作を処理します
     * 
     * @param data
     */
    private static void doCombined(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            combined = (apidata.getInt("api_combined") != 0);
            for (int i = 0; i < 2; ++i) {
                DockDto dockdto = dock.get(Integer.toString(i + 1));
                if (dockdto != null) {
                    dockdto.setUpdate(true);
                }
            }
            addUpdateLog("連合艦隊情報を更新しました");
        } catch (Exception e) {
            LOG.warn("連合艦隊情報更新に失敗しました", e);
            LOG.warn(data);
        }
    }

    /**
     * 装備改修
     */
    private static void doRemodelSlot(Data data) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            if (apidata != null) {
                if (apidata.getInt("api_remodel_flag") != 0) {
                    // 改修したアイテムを更新
                    addSlotitem(apidata.getJsonObject("api_after_slot"));
                }
            }
            addUpdateLog("装備改修を更新しました");
        } catch (Exception e) {
            LOG.warn("装備改修更新に失敗しました", e);
            LOG.warn(data);
        }
    }

    // 補助メソッド //

    /**
     * ダメージ計算があっているか検証します
     * @param dockShips 更新されたShipDto
     * @param nowhp ダメージ計算結果
     */
    private static void checkBattleDamage(List<ShipDto> dockShips, int[] nowhp) {
        for (int i = 0; i < dockShips.size(); ++i) {
            ShipDto new_ship = shipMap.get(dockShips.get(i).getId());
            if (new_ship == null)
                continue; // 轟沈した！
            if (new_ship.getNowhp() != nowhp[i]) {
                LOG.warn("ダメージ計算ミスが発生しています。" + new_ship.getName() + "の現在のHPは" + new_ship.getNowhp()
                        + "ですが、ダメージ計算では" + nowhp[i] + "と計算されていました。");
                addConsole("ダメージ計算ミス発生！（詳細はログ）");
            }
        }
    }

    /**
     * 取得した情報に不完全なものがないかチェック
     * @return　新しいstate
     */
    private static int checkDataState() {
        if (state == 3) {
            // アカウントが変わった場合はチェックするまでもない
            return state;
        }
        // 所有艦娘のマスターデータが全てあるか見る
        for (ShipDto ship : shipMap.values()) {
            if (ship.getShipInfo().getName().length() == 0) {
                return 2;
            }
        }
        // 艦娘の装備IDが全てあるか見る
        for (ShipDto ship : shipMap.values()) {
            for (int itemId : ship.getItemId()) {
                if (itemId != -1) {
                    if (itemMap.containsKey(itemId) == false) {
                        return 2;
                    }
                }
            }
        }
        return 1; // 正常
    }

    /** 装備アイテムをitemMapに追加 */
    private static ItemDto addSlotitem(JsonObject object) {
        int slotitemId = object.getInt("api_slotitem_id");
        ItemInfoDto info = Item.get(slotitemId);
        if (info != null) {
            ItemDto dto = new ItemDto(info, object);
            itemMap.put(dto.getId(), dto);
            return dto;
        }
        return null;
    }

    private static void updateDetailedMaterial(String ev, ResourceItemDto res, MATERIAL_DIFF diff) {
        if (material != null) {
            switch (diff) {
            case NEW_VALUE:
                material = res.toMaterialDto();
                break;
            case OBTAINED:
                material = material.clone().obtained(res);
                break;
            case CONSUMED:
                material = material.clone().consumed(res);
                break;
            default:
                break;
            }
            material.setEvent(ev);
            if (AppConfig.get().isMaterialLogDetail()) {
                CreateReportLogic.storeMaterialReport(material);
            }
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

        return new ShipInfoDto(object);
    }

    private static void addConsole(Object message) {
        ApplicationMain.main.printMessage(message.toString());
    }

    private static void addUpdateLog(Object message) {
        if (AppConfig.get().isPrintUpdateLog()) {
            addConsole(message);
        }
    }
}
