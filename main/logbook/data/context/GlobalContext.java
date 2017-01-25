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
import logbook.config.UserDataConfig;
import logbook.constants.AppConstants;
import logbook.data.Data;
import logbook.data.EventListener;
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
import logbook.internal.AkashiTimer;
import logbook.internal.BattleResultServer;
import logbook.internal.CondTiming;
import logbook.internal.Item;
import logbook.internal.LoggerHolder;
import logbook.internal.MasterData;
import logbook.internal.Ship;
import logbook.internal.ShipParameterRecord.UpdateShipParameter;
import logbook.scripting.EventListenerProxy;
import logbook.util.JsonUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolTip;

/**
 * ゲームのユーザ情報を管理します
 */
public final class GlobalContext {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(GlobalContext.class);

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

    /** 前回の遠征*/
    private static DeckMissionDto[] previousMissions = new DeckMissionDto[] { DeckMissionDto.EMPTY,
            DeckMissionDto.EMPTY, DeckMissionDto.EMPTY };

    /** ドック */
    private static Map<String, DockDto> dock = new TreeMap<String, DockDto>();

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

    /** 出撃(START)か */
    private static boolean isStart;

    /**　ユーザ基本情報 */
    private static BasicInfoDto basic;

    /** updateContext() が呼ばれた数 */
    private static int updateCounter = 0;

    /** 保有資源・資材 */
    private static MaterialDto material = null;

    /** 最後に資源ログに追加した時間 */
    volatile private static Date materialLogLastUpdate = null;

    /** 連合艦隊の種類 */
    private static int combinedKind;

    /** 情報の取得状態 0:母港情報未受信 1:正常 2:マスターデータの更新が必要 3:アカウントが変わった！   */
    private static int state = 0;

    /** 疲労回復タイマー */
    private static CondTiming condTiming = new CondTiming();

    /** 泊地修理タイマー */
    private static AkashiTimer akashiTimer = new AkashiTimer();

    /** まだ削除してない轟沈艦 */
    private static List<ShipDto> sunkShips = new ArrayList<ShipDto>();

    /** 次に入手した艦に割り当てるID */
    private static int nextShipId;

    /** 次に入手した装備に割り当てるID */
    private static int nextSlotitemId;

    /** ShipParameterRecord更新ハンドラ */
    private static UpdateShipParameter updateShipParameter = new UpdateShipParameter();

    private static List<EventListener> eventListeners = new ArrayList<>();

    // 始めてアクセスがあった時に読み込む
    public static final boolean INIT_COMPLETE;
    static {
        UserDataConfig.load();
        INIT_COMPLETE = true;
    }

    private static enum MATERIAL_DIFF {
        NEW_VALUE, OBTAINED, CONSUMED, NONE;
    }

    public static void load(UserDataConfig config) {
        Collection<ItemDto> items = config.getItems();
        if ((items != null) && (items.size() > 0)) {
            for (ItemDto item : items) {
                GlobalContext.itemMap.put(item.getId(), item);
            }
        }
        DeckMissionDto[] previousMissions = config.getPreviousMissions();
        if (previousMissions != null) {
            for (int i = 0; i < previousMissions.length; ++i) {
                if (previousMissions[i] == null) {
                    previousMissions[i] = DeckMissionDto.EMPTY;
                }
            }
            GlobalContext.previousMissions = previousMissions;
        }
        CondTiming.TimeSpan condTiming = config.getCondTiming();
        if (condTiming != null) {
            GlobalContext.condTiming.setUpdateTiming(condTiming);
        }
        Date akashiStartTime = config.getAkashiStartTime();
        GlobalContext.akashiTimer.setStartTime(akashiStartTime);
    }

    /**
     * 装備Map
     * @return 装備Map
     */
    public static Map<Integer, ItemDto> getItemMap() {
        return itemMap;
    }

    /**
     * アイテムIDからアイテム
     * @param itemId
     * @return
     */
    public static ItemDto getItem(int itemId) {
        if (itemId == -1) {
            return null;
        }
        ItemDto item = itemMap.get(itemId);
        if (item == null) {
            ItemDto dto = new ItemDto();
            dto.setInfo(Item.UNKNOWN);
            return dto;
        }
        return item;
    }

    /**
     * 艦娘Map
     * @return 艦娘Map
     */
    public static Map<Integer, ShipDto> getShipMap() {
        return shipMap;
    }

    /**
     * 秘書艦
     * @return 秘書艦
     */
    public static ShipDto getSecretary() {
        return secretary;
    }

    /**
     * 司令部Lv
     * @return 司令部Lv
     */
    public static int hqLevel() {
        return hqLevel;
    }

    /**
     * 最大保有可能 艦娘数
     * @return 最大保有可能 艦娘数
     */
    public static int maxChara() {
        return maxChara;
    }

    /**
     * 最大保有可能 装備数
     * @return 最大保有可能 装備数
     */
    public static int maxSlotitem() {
        return maxSlotitem;
    }

    /**
     * 建造艦娘List
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
     * 開発アイテムList
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
     * 海戦・ドロップList
     * @return 海戦・ドロップList
     */
    public static List<BattleResultDto> getBattleResultList() {
        return battleResultList;
    }

    /**
     * 最後に行った海戦情報
     * @return 最後に行った海戦情報
     */
    public static BattleExDto getLastBattleDto() {
        return battle;
    }

    /**
     * 遠征結果
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
     * 遠征リスト
     * @return 遠征リスト
     */
    public static DeckMissionDto[] getDeckMissions() {
        return deckMissions;
    }

    /**
     * 前回の遠征リスト
     * @return 前回の遠征リスト
     */
    public static DeckMissionDto[] getPreviousMissions() {
        return previousMissions;
    }

    /**
     * 入渠リスト
     * @return 入渠リスト
     */
    public static NdockDto[] getNdocks() {
        return ndocks;
    }

    /**
     * 建造ドックリスト
     * @return 建造ドックリスト
     */
    public static KdockDto[] getKdocks() {
        return kdocks;
    }

    /**
     * 遠征中の艦セット
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
     * 入渠中の艦セット
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
     * 入渠中の艦セット
     * @return 入渠中の艦セット
     */
    public static Map<Integer, Date> getNDockCompleteTimeMap() {
        Map<Integer, Date> map = new HashMap<>();
        for (NdockDto ndock : ndocks) {
            if (ndock.getNdockid() != 0) {
                map.put(ndock.getNdockid(), ndock.getNdocktime());
            }
        }
        return map;
    }

    /**
     * 艦娘が入渠しているかを調べます
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

    /**
     * 演習相手リスト
     * @return
     */
    public static PracticeUserDto[] getPracticeUser() {
        return practiceUser;
    }

    /**
     * 演習リストが最後に更新された時間
     * @return
     */
    public static Date getPracticeUserLastUpdate() {
        return practiceUserLastUpdate;
    }

    /**
     * 艦隊が遠征中かを調べます
     * @param idstr 艦隊ID（1～）
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
     * ドック
     * @param id 番号 "1"～"4"
     * @return ドック
     */
    public static DockDto getDock(String id) {
        return dock.get(id);
    }

    /**
     * ドックMap
     * @return ドックMap キーは"1"～"4"
     */
    public static Map<String, DockDto> getDock() {
        return dock;
    }

    /**
     * 出撃艦隊情報
     * @return
     */
    public static boolean[] getIsSortie() {
        return isSortie;
    }

    /**
     * 今いるマス
     * @return
     */
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

    /**
     * 任務が最後に更新された時間
     * @return
     */
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
        return (combinedKind != 0);
    }

    /**
     * データ受信状態
     * @return 0:母港情報未受信 1:正常 2:未取得のデータ有り
     */
    public static int getState() {
        return state;
    }

    /**
     * @return condTiming
     */
    public static CondTiming getCondTiming() {
        return condTiming;
    }

    /**
     * @return akashiRepairStart
     */
    public static AkashiTimer getAkashiTimer() {
        return akashiTimer;
    }

    /**
     * リクエスト・レスポンスを受け取るEventListener登録
     */
    public static void addEventListener(EventListener listener) {
        if (eventListeners.indexOf(listener) == -1) {
            eventListeners.add(listener);
        }
    }

    /**
     * リクエスト・レスポンスを受け取るEventListener登録解除
     */
    public static void removeEventListener(EventListener listener) {
        eventListeners.remove(listener);
    }

    /**
     * 情報を更新します
     *
     * @param data リクエスト・レスポンスデータ
     */
    public static void updateContext(Data data) {
        // json保存設定
        if (AppConfig.get().isStoreJson()) {
            doStoreJson(data);
        }

        JsonValue apidata = data.getJsonObject().get("api_data");
        switch (data.getDataType()) {
        // 補給
        case CHARGE:
            doCharge(data, apidata);
            break;
        // 編成
        case CHANGE:
            doChange(data, apidata);
            break;
        // 編成
        case PRESET_SELECT:
            doPresetSelect(data, apidata);
            break;
        // 母港
        case PORT:
            doPort(data, apidata);
            break;
        // 保有装備
        case SLOTITEM_MEMBER:
            doSlotitemMember(data, apidata);
            break;
        // 保有艦
        case SHIP3:
            doShipDeck(data, apidata);
            break;
        // 保有艦
        case SHIP2:
            doShip2(data, apidata);
            break;
        // 出撃中の更新
        case SHIP_DECK:
            doShipDeck(data, apidata);
            break;
        // 基本
        case BASIC:
            doBasic(data, apidata);
            break;
        // 資材
        case MATERIAL:
            doMaterial(data, apidata);
            break;
        // 遠征(帰還)
        case MISSION_RESULT:
            doMissionResult(data, apidata);
            break;
        // 入渠
        case NDOCK:
            doNdock(data, apidata);
            break;
        // 建造
        case CREATE_SHIP:
            doCreateship(data, apidata);
            break;
        // 建造ドック
        case KDOCK:
            doKdock(data, apidata);
            break;
        // 建造(入手)
        case GET_SHIP:
            doGetship(data, apidata);
            break;
        // 装備開発
        case CREATE_ITEM:
            doCreateitem(data, apidata);
            break;
        // 解体
        case DESTROY_SHIP:
            doDestroyShip(data, apidata);
            break;
        // 廃棄
        case DESTROY_ITEM2:
            doDestroyItem2(data, apidata);
            break;
        // 近代化改修
        case POWERUP:
            doPowerup(data, apidata);
            break;
        // 装備位置交換
        case SLOT_EXCHANGE_INDEX:
            doSlotExchangeIndex(data, apidata);
            break;
        // 艦娘ロック操作
        case LOCK_SHIP:
            doLockShip(data, apidata);
            break;
        // 装備ロック操作
        case LOCK_SLOTITEM:
            doLockSlotitem(data, apidata);
            break;
        // 装備改修
        case REMODEL_SLOT:
            doRemodelSlot(data, apidata);
            break;
        // 海戦
        case BATTLE:
            doBattle(data, apidata, BattlePhaseKind.BATTLE);
            break;
        // 海戦
        case BATTLE_MIDNIGHT:
            doBattle(data, apidata, BattlePhaseKind.MIDNIGHT);
            break;
        // 海戦
        case BATTLE_SP_MIDNIGHT:
            doBattle(data, apidata, BattlePhaseKind.SP_MIDNIGHT);
            break;
        // 海戦
        case BATTLE_NIGHT_TO_DAY:
            doBattle(data, apidata, BattlePhaseKind.NIGHT_TO_DAY);
            break;
        // 海戦
        case AIR_BATTLE:
            doBattle(data, apidata, BattlePhaseKind.AIR_BATTLE);
            break;
        // 海戦
        case LD_AIRBATTLE:
            doBattle(data, apidata, BattlePhaseKind.LD_AIRBATTLE);
            break;
        // 海戦
        case COMBINED_AIR_BATTLE:
            doBattle(data, apidata, BattlePhaseKind.COMBINED_AIR);
            break;
        // 海戦
        case COMBINED_LD_AIRBATTLE:
            doBattle(data, apidata, BattlePhaseKind.COMBINED_LD_AIR);
            break;
        // 海戦
        case COMBINED_BATTLE:
            doBattle(data, apidata, BattlePhaseKind.COMBINED_BATTLE);
            break;
        // 海戦
        case COMBINED_BATTLE_MIDNIGHT:
            doBattle(data, apidata, BattlePhaseKind.COMBINED_MIDNIGHT);
            break;
        // 海戦
        case COMBINED_BATTLE_SP_MIDNIGHT:
            doBattle(data, apidata, BattlePhaseKind.COMBINED_SP_MIDNIGHT);
            break;
        case COMBINED_BATTLE_WATER:
            doBattle(data, apidata, BattlePhaseKind.COMBINED_BATTLE_WATER);
            break;
        case COMBINED_EC_BATTLE:
            doBattle(data, apidata, BattlePhaseKind.COMBINED_EC_BATTLE);
            break;
        case COMBINED_EC_BATTLE_MIDNIGHT:
            doBattle(data, apidata, BattlePhaseKind.COMBINED_EC_BATTLE_MIDNIGHT);
            break;
        case COMBINED_EACH_BATTLE:
            doBattle(data, apidata, BattlePhaseKind.COMBINED_EACH_BATTLE);
            break;
        case COMBINED_EACH_BATTLE_WATER:
            doBattle(data, apidata, BattlePhaseKind.COMBINED_EACH_BATTLE_WATER);
            break;
        // 海戦結果
        case BATTLE_RESULT:
            doBattleresult(data, apidata);
            break;
        // 海戦結果
        case COMBINED_BATTLE_RESULT:
            doBattleresult(data, apidata);
            break;
        // 退避した
        case COMBINED_BATTLE_GOBACK_PORT:
            doBattleGobackPort(data, apidata);
            break;
        // 演習
        case PRACTICE_BATTLE:
            doBattle(data, apidata, BattlePhaseKind.PRACTICE_BATTLE);
            break;
        // 演習
        case PRACTICE_BATTLE_MIDNIGHT:
            doBattle(data, apidata, BattlePhaseKind.PRACTICE_MIDNIGHT);
            break;
        // 演習結果
        case PRACTICE_BATTLE_RESULT:
            doBattleresult(data, apidata);
            break;
        // 艦隊
        case DECK:
            doDeck(data, apidata);
            break;
        // 出撃
        case START:
            doStart(data, apidata);
            break;
        // 進撃
        case NEXT:
            doNext(data, apidata);
            break;
        // 任務
        case QUEST_LIST:
            doQuest(data, apidata);
            break;
        // 任務消化
        case QUEST_CLEAR:
            doQuestClear(data, apidata);
            break;
        // 設定
        case START2:
            doStart2(data, apidata);
            break;
        // マップ情報
        case MAPINFO:
            doMapInfo(data, apidata);
            break;
        // 任務情報
        case MISSION:
            doMission(data, apidata);
            break;
        // 演習
        case PRACTICE:
            doPractice(data, apidata);
            break;
        // 演習情報
        case PRACTICE_ENEMYINFO:
            doPracticeEnemyinfo(data, apidata);
            break;
        // 連合艦隊
        case COMBINED:
            doCombined(data, apidata);
            break;
        // 入渠開始
        case NYUKYO_START:
            doNyukyoStart(data, apidata);
            break;
        // 高速修復
        case NYUKYO_SPEEDCHANGE:
            doSpeedChange(data, apidata);
            break;
        // 改造
        case REMODELING:
            doRemodeling(data, apidata);
            break;
        // 疲労度回復アイテム使用
        case ITEMUSE_COND:
            doItemuseCond(data, apidata);
            break;
        // ログイン時のユーザ情報
        case REQUIRE_INFO:
            doRequireInfo(data, apidata);
            break;
        // 基地航空隊出撃
        case START_AIR_BASE:
            doStartAirBase(data, apidata);
            break;
        default:
            break;
        }
        ++updateCounter;

        // ユーザスクリプト呼び出し
        EventListenerProxy.get().update(data.getDataType(), data);
        // 登録リスナ呼び出し
        for (EventListener listener : eventListeners) {
            listener.update(data.getDataType(), data);
        }
    }

    /**
     * updateContext()が呼ばれた数
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
            LOG.get().warn("JSONオブジェクトを保存するに失敗しました", e);
            LOG.get().warn(data);
        }

    }

    /**
     * ログイン時のユーザ情報
     * @param data
     */
    private static void doRequireInfo(Data data, JsonValue json) {
        try {

            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;

                doSlotitemMember(data, apidata.get("api_slot_item"));
                doKdock(data, apidata.get("api_kdock"));
            }

            addUpdateLog("ユーザ情報を更新しました");
        } catch (Exception e) {
            LOG.get().warn("ユーザ情報を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 補給を更新します
     * @param data
     */
    private static void doCharge(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
                JsonArray ships = apidata.getJsonArray("api_ship");
                for (JsonValue shipval : ships) {
                    JsonObject shipobj = (JsonObject) shipval;

                    int shipid = shipobj.getInt("api_id");

                    ShipDto ship = shipMap.get(shipid);
                    if (ship != null) {
                        int fuel = shipobj.getInt("api_fuel");
                        int bull = shipobj.getInt("api_bull");
                        int[] onslot = JsonUtils.getIntArray(shipobj, "api_onslot");

                        ship.setFuel(fuel);
                        ship.setBull(bull);
                        ship.setOnslot(onslot);

                        String fleetid = ship.getFleetid();
                        if (fleetid != null) {
                            DockDto dockdto = dock.get(fleetid);
                            if (dockdto != null) {
                                dockdto.setUpdate(true);
                            }
                        }
                    }
                }
                addUpdateLog("補給しました");
            }
        } catch (Exception e) {
            LOG.get().warn("補給を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    private static boolean isFlagshipAkashi(DockDto dock) {
        if (dock != null) {
            return dock.isFlagshipAkashi();
        }
        return false;
    }

    /**
     * 編成を更新します
     * @param data
     */
    private static void doChange(Data data, JsonValue json) {
        try {
            String fleetid = data.getField("api_id");
            int shipid = Integer.valueOf(data.getField("api_ship_id"));
            int shipidx = Integer.valueOf(data.getField("api_ship_idx"));

            DockDto dockdto = dock.get(fleetid);

            if (dockdto != null) {
                List<ShipDto> ships = dockdto.getShips();
                DockDto rdock = null;

                if (shipidx == -1) {
                    // 旗艦以外解除
                    for (int i = 1; i < ships.size(); ++i) {
                        ships.get(i).setFleetid("");
                    }
                    dockdto.removeExceptFlagship();
                    dockdto.setUpdate(true);
                }
                else {
                    // 入れ替えまたは外す
                    // 入れ替え前の艦娘(いない場合はnull)
                    ShipDto cship = (shipidx < ships.size()) ? ships.get(shipidx) : null;
                    // 入れる艦娘(外す場合はnull)
                    ShipDto rship = shipMap.get(shipid);
                    // 入れる艦娘の現在の所属艦隊(ない場合はnull)
                    rdock = (rship != null) ? dock.get(rship.getFleetid()) : null;
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

                    // 泊地修理判定
                    if (isFlagshipAkashi(dockdto) || isFlagshipAkashi(rdock)) {
                        akashiTimer.reset();
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
            LOG.get().warn("編成を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * @param data
     */
    private static void doPresetSelect(Data data, JsonValue json) {
        try {
            // 他の艦隊にいる艦娘は展開されない前提

            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
                int fleetid = apidata.getInt("api_id");

                // 展開前の艦娘から艦隊情報を取り除く
                DockDto dockdto = dock.get(String.valueOf(fleetid));
                if (dockdto != null) {
                    dockdto.removeFleetIdFromShips();
                }

                String name = apidata.getString("api_name");
                int[] shipIds = JsonUtils.getIntArray(apidata, "api_ship");
                setFleetInfo(fleetid, name, shipIds);
            }

            addUpdateLog("プリセットを展開しました");
        } catch (Exception e) {
            LOG.get().warn("プリセットを展開しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    private static boolean isAkashiRepairEnabled() {
        for (DockDto dock : dock.values()) {
            if (dock.isAkashiRepairEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 母港を更新します
     * @param data
     */
    private static void doPort(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
                // 出撃中ではない
                boolean endSortie = false;
                for (int i = 0; i < isSortie.length; ++i) {
                    if (isSortie[i]) {
                        DockDto sortieDock = dock.get(Integer.toString(i + 1));
                        if (sortieDock != null) {
                            // 退避情報をクリア
                            sortieDock.setEscaped(null);
                            sortieDock.setUpdate(true);
                        }
                        endSortie = true;
                    }
                }
                if (endSortie) {
                    ApplicationMain.main.endSortie();
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
                doBasic(data, apidata.get("api_basic"));
                //addConsole("司令部を更新しました");

                // 保有資材を更新する
                doMaterial(data, apidata.get("api_material"));
                //addConsole("保有資材を更新しました");

                // 入渠の状態を更新する
                // 入渠終了処理を行わないと泊地修理が誤ってリセットされるため先に行う
                doNdock(data, apidata.get("api_ndock"));
                //addConsole("入渠情報を更新しました");

                // 保有艦娘を更新する
                sunkShips.clear();
                boolean condUpdated = false;
                boolean hpUpdated = false;
                Map<Integer, ShipDto> oldShipMap = shipMap;
                shipMap = new TreeMap<>();
                JsonArray apiShip = apidata.getJsonArray("api_ship");
                for (int i = 0; i < apiShip.size(); i++) {
                    ShipDto ship = new ShipDto((JsonObject) apiShip.get(i));
                    addShip(ship);

                    ShipDto oldShip = oldShipMap.get(ship.getId());
                    if (oldShip != null) {
                        // 疲労度に変化があったか
                        if (oldShip.getCond() != ship.getCond()) {
                            condUpdated = true;
                        }
                        // HPに変化があったか
                        if (oldShip.getNowhp() != ship.getNowhp()) {
                            hpUpdated = true;
                        }
                    }
                }
                // 疲労回復タイミング更新
                condTiming.onPort(condUpdated);

                // 泊地修理タイマー更新
                if (hpUpdated) {
                    // 実際に回復があったのでリセット
                    akashiTimer.reset();
                }
                else if ((akashiTimer.getStartTime() != null) && (isAkashiRepairEnabled() == false)) {
                    // 泊地修理していなくてもカウンタは回っているが、泊地修理編成でない場合回復がないので
                    // 20分経過していたらリセットしておく
                    // サーバ側で20分経過したかどうかは正確には分からないが知る術がない
                    long elapsed = new Date().getTime() - akashiTimer.getStartTime().getTime();
                    if (elapsed > AkashiTimer.MINIMUM_TIME) {
                        akashiTimer.reset();
                    }
                }

                JsonArray apiDeckPort = apidata.getJsonArray("api_deck_port");
                doDeck(apiDeckPort);
                //addConsole("保有艦娘情報を更新しました");

                //addConsole("遠征情報を更新しました");

                // 連合艦隊を更新する
                combinedKind = 0;
                if (apidata.containsKey("api_combined_flag")) {
                    combinedKind = apidata.getInt("api_combined_flag");
                    //addConsole("連合艦隊を更新しました");
                }

                updateShipParameter.sortieEnd();
                state = checkDataState(endSortie);

                addUpdateLog("母港情報を更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("母港を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 艦娘の現在のHPに反映＋轟沈判定
     * @param ship
     * @param nowhp
     * @param sunkShips
     */
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
    private static void doBattle(Data data, JsonValue json, BattlePhaseKind phaseKind) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
                if (battle == null) {
                    battle = new BattleExDto(data.getCreateDate());
                    battle.setBasicInfo(maxChara - shipMap.size(), maxSlotitem - itemMap.size());
                    battle.setCombinedKind(combinedKind);
                }
                BattleExDto.Phase phase = battle.addPhase(apidata, phaseKind);

                if (battle.getDock() == null) { // 情報が不足しているので処理しない
                    battle = null;
                    return;
                }

                List<ShipDto> ships = battle.getFriends().get(0).getShips();
                int[] nowFriendHp = phase.getNowFriendHp();

                if (battle.getDock().getShips().size() != nowFriendHp.length) { // 情報が不足しているので処理しない
                    battle = null;
                    return;
                }

                if ((phaseKind != BattlePhaseKind.PRACTICE_BATTLE) &&
                        (phaseKind != BattlePhaseKind.PRACTICE_MIDNIGHT)) { // 演習ではやらない
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

                    if (battle.getPhaseList().size() == 1) {
                        updateShipParameter.battleStart();
                    }
                }

                addUpdateLog("海戦情報を更新しました");
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

                // 出撃していない場合は出撃させる
                boolean needToStart = false;
                for (DockDto dock : battle.getFriends()) {
                    int index = Integer.parseInt(dock.getId()) - 1;
                    if (!isSortie[index]) {
                        needToStart = true;
                        isSortie[index] = true;
                    }
                }
                if (needToStart) {
                    ApplicationMain.main.startSortie();
                }
                ApplicationMain.main.updateBattle(battle);
            }

        } catch (Exception e) {
            LOG.get().warn("海戦情報を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 海戦情報を更新します
     * @param data
     */
    private static void doBattleresult(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
                if (battle != null) {
                    battle.setResult(apidata, mapCellDto);

                    if (battle.isCompleteResult()) { // 情報が不足している場合は記録しない
                        BattleResultServer.get().addNewResult(battle);
                    }

                    // ランクが合っているかチェック
                    Phase lastPhase = battle.getLastPhase();
                    if (!battle.getRank().equals(lastPhase.getEstimatedRank())) {
                        LOG.get().info("戦闘結果判定ミス: 正解ランク:" + battle.getRank() + " " + lastPhase.getRankCalcInfo(battle));
                    }

                    if (battle.isPractice() == false) { // 演習は記録しない
                        //battleResultList.add(battle);
                        CreateReportLogic.storeBattleResultReport(battle);

                        /*
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
                        */

                        // ドロップ艦を追加
                        if (battle.isDropShip()) {
                            ShipInfoDto shipinfo = Ship.get(battle.getDropShipId());
                            int[] slotitemids = shipinfo.getDefaultSlot();
                            int[] slotids = new int[slotitemids.length];
                            for (int i = 0; i < slotitemids.length; ++i) {
                                int slotitemid = slotitemids[i];
                                int slotid = -1;
                                if (slotitemid != -1) {
                                    ItemInfoDto info = Item.get(slotitemid);
                                    if (info != null) {
                                        ItemDto item = new ItemDto(info, nextSlotitemId++);
                                        slotid = item.getId();
                                        itemMap.put(slotid, item);
                                    }
                                }
                                slotids[i] = slotid;
                            }
                            ShipDto dropShip = new ShipDto(nextShipId++, shipinfo, slotids);
                            shipMap.put(dropShip.getId(), dropShip);
                        }
                    }

                    // 警告を出すためにバージョンアップ
                    battle.getDock().setUpdate(true);
                    if (battle.isCombined()) {
                        battle.getDockCombined().setUpdate(true);
                    }
                }

                // 出撃を更新
                isStart = false;
                addUpdateLog("海戦結果を更新しました");

                // ドロップを表示
                if (battle != null) {
                    if (AppConfig.get().isPrintDropLog()) {
                        if (battle.isDropShip()) {
                            addConsole(battle.getDropName() + "がドロップしました");
                        }
                        if (battle.isDropItem()) {
                            addConsole(battle.getDropItemName() + "がドロップしました");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.get().warn("海戦結果を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 護衛退避情報を更新します
     * @param data
     */
    private static void doBattleGobackPort(Data data, JsonValue json) {
        try {
            if (battle != null) {
                int[] escapeInfo = battle.getEscapeInfo();
                if ((battle.getEscaped() != null) && (escapeInfo != null)) {
                    // 退避を選択したので退避した艦を追加しておく
                    boolean[] escaped = battle.getEscaped().clone();
                    escaped[escapeInfo[0]] = true;
                    escaped[escapeInfo[1]] = true;
                    for (int i = 0; i < 2; ++i) {
                        battle.getFriends().get(i).setEscaped(
                                Arrays.copyOfRange(escaped, i * 6, (i + 1) * 6));
                    }

                    // 更新
                    battle.getDock().setUpdate(true);
                    battle.getDockCombined().setUpdate(true);
                }
            }
            addConsole("護衛退避しました");
        } catch (Exception e) {
            LOG.get().warn("護衛退避を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 建造(投入資源)情報を更新します
     * @param data
     */
    private static void doCreateship(Data data, JsonValue json) {
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

            // 資源に反映させてレポート
            updateDetailedMaterial("建造", res, MATERIAL_DIFF.CONSUMED);

            addUpdateLog("建造(投入資源)情報を更新しました");
        } catch (Exception e) {
            LOG.get().warn("建造(投入資源)情報を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 建造を更新します
     * @param data
     */
    private static void doKdock(Data data, JsonValue json) {
        try {
            if (json instanceof JsonArray) {
                JsonArray apidata = (JsonArray) json;

                // 建造ドックの空きをカウントします
                if (lastBuildKdock != null) {
                    GetShipDto resource = getShipResource.get(lastBuildKdock);
                    if (resource != null) {
                        int kdockid = Integer.parseInt(lastBuildKdock);
                        ShipInfoDto shipinfo = null;
                        int freecount = 0;
                        for (int i = 0; i < apidata.size(); i++) {
                            JsonObject jsonkdock = (JsonObject) apidata.get(i);
                            if (jsonkdock.getInt("api_id") == kdockid) {
                                int shipId = jsonkdock.getInt("api_created_ship_id");
                                shipinfo = Ship.get(shipId);
                            }

                            int state = jsonkdock.getJsonNumber("api_state").intValue();
                            if (state == 0) {
                                freecount++;
                            }
                        }
                        // 建造ドックの空き、艦娘をセットします
                        resource.setFreeDock(freecount);
                        resource.setShip(shipinfo);
                        // 追加
                        getShipList.add(resource);
                        CreateReportLogic.storeCreateShipReport(resource);
                    }
                    lastBuildKdock = null;
                }

                // 建造ドック更新
                doKdockSub(apidata);

                addUpdateLog("建造を更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("建造を更新しますに失敗しました", e);
            LOG.get().warn(data);
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
    private static void doGetship(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
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
                addShip(ship);
                // 投入資源を除去する
                getShipResource.remove(dock);

                // 建造ドック更新
                doKdockSub(apidata.getJsonArray("api_kdock"));

                //state = checkDataState();

                addUpdateLog("建造(入手)情報を更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("建造(入手)情報を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 装備開発情報を更新します
     *
     * @param data
     */
    private static void doCreateitem(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;

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
                }
                else {
                    createItemList.add(createitem);
                }
                CreateReportLogic.storeCreateItemReport(createitem);

                // 資源に反映させてレポート
                JsonArray newMaterial = apidata.getJsonArray("api_material");
                ResourceItemDto items = new ResourceItemDto();
                items.loadMaterialFronJson(newMaterial);
                updateDetailedMaterial("装備開発", items, MATERIAL_DIFF.NEW_VALUE);

                //state = checkDataState();

                addUpdateLog("装備開発情報を更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("装備開発情報を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 保有装備を更新します
     *
     * @param data
     */
    private static void doSlotitemMember(Data data, JsonValue json) {
        try {
            if (json instanceof JsonArray) {
                JsonArray apidata = (JsonArray) json;
                // 破棄
                itemMap.clear();
                for (int i = 0; i < apidata.size(); i++) {
                    JsonObject object = (JsonObject) apidata.get(i);
                    addSlotitem(object);
                }

                addUpdateLog("保有装備情報を更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("保有装備を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 保有艦娘を更新します
     *
     * @param data
     */
    private static void doShipDeck(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;

                // 轟沈艦があるときはこのタイミングで削除
                if (sunkShips.size() > 0) {
                    for (ShipDto ship : sunkShips) {
                        shipMap.remove(ship.getId());
                    }
                    sunkShips.clear();
                }

                // 艦娘を更新
                JsonArray shipdata = apidata.getJsonArray("api_ship_data");
                for (int i = 0; i < shipdata.size(); i++) {
                    ShipDto ship = new ShipDto((JsonObject) shipdata.get(i));
                    shipMap.put(ship.getId(), ship);
                }

                // 艦隊を設定
                doDeck(apidata.getJsonArray("api_deck_data"));

                if (battle != null) {
                    ApplicationMain.main.updateSortieDock();
                }

                //state = checkDataState();

                addUpdateLog("保有艦娘情報３を更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("保有艦娘を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 保有艦娘を更新します
     *
     * @param data
     */
    private static void doShip2(Data data, JsonValue json) {
        try {
            if (json instanceof JsonArray) {
                JsonArray apidata = (JsonArray) json;
                // 情報を破棄
                shipMap.clear();
                for (int i = 0; i < apidata.size(); i++) {
                    addShip(new ShipDto((JsonObject) apidata.get(i)));
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

                addUpdateLog("保有艦娘情報２を更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("保有艦娘を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 艦隊を更新します
     *
     * @param data
     */
    private static void doDeck(Data data, JsonValue json) {
        try {
            if (json instanceof JsonArray) {
                JsonArray apidata = (JsonArray) json;
                // 艦隊IDをクリアします
                for (DockDto dockdto : dock.values()) {
                    for (ShipDto ship : dockdto.getShips()) {
                        ship.setFleetid("");
                    }
                }
                doDeck(apidata);

                addUpdateLog("艦隊を更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("艦隊を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    private static void setFleetInfo(int fleetid, String name, int[] shipIds) {
        String fleetidstr = String.valueOf(fleetid);
        DockDto dockdto = new DockDto(fleetidstr, name, dock.get(fleetidstr));
        dock.put(fleetidstr, dockdto);

        for (int j = 0; j < shipIds.length; j++) {
            int shipId = shipIds[j];

            ShipDto ship = shipMap.get(shipId);
            if (ship != null) {
                dockdto.addShip(ship);

                if ((fleetid == 1) && (j == 0)) {
                    setSecretary(ship);
                }
                // 艦隊IDを設定
                ship.setFleetid(fleetidstr);
                ship.setFleetpos(j);
            }
        }

    }

    /**
     * 艦隊と遠征の状態を更新します
     *
     * @param apidata
     */
    private static void doDeck(JsonArray apidata) {
        for (int i = 0; i < apidata.size(); i++) {
            JsonObject jsonObject = (JsonObject) apidata.get(i);
            int fleetid = jsonObject.getInt("api_id");
            String name = jsonObject.getString("api_name");
            int[] shipIds = JsonUtils.getIntArray(jsonObject, "api_ship");

            setFleetInfo(fleetid, name, shipIds);

            if (fleetid >= 2) {
                JsonArray jmission = jsonObject.getJsonArray("api_mission");
                int section = ((JsonNumber) jmission.get(1)).intValue();
                long milis = ((JsonNumber) jmission.get(2)).longValue();
                Date time = null;
                if (milis > 0) {
                    time = new Date(milis);
                }
                int index = fleetid - 2;
                deckMissions[index] = new DeckMissionDto(name, section, time, fleetid, shipIds);
                if (milis > 0) {
                    previousMissions[index] = deckMissions[index];
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
    private static void doDestroyShip(Data data, JsonValue json) {
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
            LOG.get().warn("艦娘を解体しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 装備を廃棄します
     * @param data
     */
    private static void doDestroyItem2(Data data, JsonValue json) {
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
            LOG.get().warn("装備を廃棄しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 近代化改修します
     * @param data
     */
    private static void doPowerup(Data data, JsonValue json) {
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
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
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
            }
            addUpdateLog("近代化改修しました");
        } catch (Exception e) {
            LOG.get().warn("近代化改修しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * @param data
     */
    private static void doSlotExchangeIndex(Data data, JsonValue json) {
        try {
            int shipId = Integer.parseInt(data.getField("api_id"));
            ShipDto ship = shipMap.get(shipId);
            if (ship != null) {
                if (json instanceof JsonObject) {
                    JsonObject apidata = (JsonObject) json;
                    ship.setSlotFromJson(apidata);

                    // 次アップデート
                    String fleetid = ship.getFleetid();
                    if (fleetid != null) {
                        DockDto dockdto = dock.get(fleetid);
                        if (dockdto != null) {
                            dockdto.setUpdate(true);
                        }
                    }
                }
            }

            addUpdateLog("装備状態を更新しました");
        } catch (Exception e) {
            LOG.get().warn("装備状態の更新に失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 艦娘ロックを更新する
     *
     * @param data
     */
    private static void doLockShip(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
                int shipId = Integer.valueOf(data.getField("api_ship_id"));
                boolean locked = apidata.getInt("api_locked") != 0;

                ShipDto dto = shipMap.get(shipId);
                if (dto != null) {
                    dto.setLocked(locked);
                }

                addUpdateLog("艦娘ロックを更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("艦娘ロックを更新するに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 装備ロックを更新する
     *
     * @param data
     */
    private static void doLockSlotitem(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
                int slotitemId = Integer.valueOf(data.getField("api_slotitem_id"));
                boolean locked = apidata.getInt("api_locked") != 0;

                ItemDto dto = itemMap.get(slotitemId);
                if (dto != null) {
                    dto.setLocked(locked);
                }

                addUpdateLog("装備ロックを更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("装備ロックを更新するに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 司令部を更新する
     *
     * @param data
     */
    private static void doBasic(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
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
        } catch (Exception e) {
            LOG.get().warn("司令部を更新するに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 保有資材を更新する
     * 記録にBasic情報を使うので更新してから呼び出すこと
     * @param data
     */
    private static void doMaterial(Data data, JsonValue json) {
        try {
            if (json instanceof JsonArray) {
                JsonArray apidata = (JsonArray) json;

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
                        || (TimeUnit.MILLISECONDS
                                .toSeconds(time.getTime() - materialLogLastUpdate.getTime()) > AppConfig.get()
                                .getMaterialLogInterval())) {
                    CreateReportLogic.storeMaterialReport(material, basic);

                    materialLogLastUpdate = time;
                }
            }
        } catch (Exception e) {
            LOG.get().warn("保有資材を更新するに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 遠征(帰還)を更新します
     *
     * @param data
     */
    private static void doMissionResult(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;

                int clearResult = apidata.getJsonNumber("api_clear_result").intValue();
                String questName = apidata.getString("api_quest_name");
                ResourceItemDto res = new ResourceItemDto();

                if (clearResult != 0) {
                    // 資源に反映させてレポート
                    res.loadMissionResult(apidata);
                    updateDetailedMaterial("遠征帰還", res, MATERIAL_DIFF.OBTAINED);
                }

                // 遠征により疲労度が変化しているので
                condTiming.ignoreNext();

                MissionResultDto result = new MissionResultDto(clearResult, questName, res);

                CreateReportLogic.storeMissionReport(result);
                missionResultList.add(result);

                state = checkDataState();

                addUpdateLog("遠征(帰還)情報を更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("遠征(帰還)を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 入渠を更新します
     * @param data
     */
    private static void doNdock(Data data, JsonValue json) {
        try {
            if (json instanceof JsonArray) {
                JsonArray apidata = (JsonArray) json;

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

                addUpdateLog("入渠情報を更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("入渠を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    // 修理が終わった艦はHPを回復させる
    private static void ndockFinished(int shipId) {
        ShipDto ship = shipMap.get(shipId);
        if (ship != null) {
            // 回復させる
            ship.setNowhp(ship.getMaxhp());
            if (ship.getCond() < 40) {
                ship.setCond(40);
            }
            ship.setDockTime(0);
        }
        // 修理が終わったことにより疲労度が変わっているので
        condTiming.ignoreNext();
        // 次アップデート
        String fleetid = ship.getFleetid();
        if (fleetid != null) {
            DockDto dockdto = dock.get(fleetid);
            if (dockdto != null) {
                dockdto.setUpdate(true);
            }
        }
    }

    /**
     * 入渠開始
     * @param apidata
     */
    private static void doNyukyoStart(Data data, JsonValue json) {
        try {
            int id = Integer.valueOf(data.getField("api_ship_id"));
            boolean highspeed = data.getField("api_highspeed").equals("1");

            if (highspeed) {
                ndockFinished(id);
            }

            // 次アップデート
            ShipDto ship = shipMap.get(id);
            if (ship != null) {
                String fleetid = ship.getFleetid();
                if (fleetid != null) {
                    DockDto dockdto = dock.get(fleetid);
                    if (dockdto != null) {
                        dockdto.setUpdate(true);
                    }
                }
            }

            // 高速修復出ない場合は直後にndockが送られてくる
            addUpdateLog("入渠情報を更新しました");
        } catch (Exception e) {
            LOG.get().warn("入渠を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 入渠中に高速修復を使った
     * @param apidata
     */
    private static void doSpeedChange(Data data, JsonValue json) {
        try {
            int id = Integer.valueOf(data.getField("api_ndock_id"));

            ndockFinished(ndocks[id - 1].getNdockid());
            ndocks[id - 1] = NdockDto.EMPTY;

            addUpdateLog("バケツを使いました");
        } catch (Exception e) {
            LOG.get().warn("入渠を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 改造
     * @param data
     */
    private static void doRemodeling(Data data, JsonValue json) {
        try {
            // 改造で疲労度が変わっているので
            condTiming.ignoreNext();

            addUpdateLog("改造しました");
        } catch (Exception e) {
            LOG.get().warn("改造を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 疲労回復アイテム使用
     * @param data
     */
    private static void doItemuseCond(Data data, JsonValue json) {
        try {
            // 疲労度が変わっているので
            condTiming.ignoreNext();

            addUpdateLog("疲労回復アイテム使用しました");
        } catch (Exception e) {
            LOG.get().warn("疲労回復アイテム使用を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 任務を更新します
     *
     * @param data
     */
    private static void doQuest(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;

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
                            QuestDto quest = new QuestDto(questobject, disp_page, pos++);
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
            }
        } catch (Exception e) {
            LOG.get().warn("任務を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 消化した任務を除去します
     *
     * @param data
     */
    private static void doQuestClear(Data data, JsonValue json) {
        try {
            /*
            String idstr = data.getField("api_quest_id");
            if (idstr != null) {
                Integer id = Integer.valueOf(idstr);
                questMap.remove(id);
            }
            */
            // 資源に反映させてレポート
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
                ResourceItemDto items = new ResourceItemDto();
                items.loadQuestClear(apidata);
                updateDetailedMaterial("任務をクリア", items, MATERIAL_DIFF.OBTAINED);

                addUpdateLog("任務をクリアしました");
            }
        } catch (Exception e) {
            LOG.get().warn("消化した任務を除去しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 出撃を更新します
     *
     * @param data
     */
    private static void doStart(Data data, JsonValue json) {
        try {
            String idstr = data.getField("api_deck_id");
            if (idstr != null) {
                int id = Integer.parseInt(idstr);
                isSortie[id - 1] = true;
                // 連合艦隊
                if ((id == 1) && (combinedKind != 0)) {
                    isSortie[1] = true;
                }
            }
            // 出撃を更新
            isStart = true;

            // 出撃により疲労度が変わっているので
            condTiming.ignoreNext();

            // 出撃準備
            updateShipParameter.sortieStart();

            battle = null;

            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
                mapCellDto = new MapCellDto(apidata, isStart);
            }
            updateDetailedMaterial("出撃", null, MATERIAL_DIFF.NONE);

            ApplicationMain.main.startSortie();
            ApplicationMain.main.updateMapCell(mapCellDto);

            addUpdateLog("出撃しました");
            if (AppConfig.get().isPrintSortieLog())
                addConsole("行先 " + mapCellDto.toString());
        } catch (Exception e) {
            LOG.get().warn("出撃を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 進撃を更新します
     *
     * @param data
     */
    private static void doNext(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;

                mapCellDto = new MapCellDto(apidata, isStart);

                battle = null;

                ApplicationMain.main.updateMapCell(mapCellDto);
                if (AppConfig.get().isPrintSortieLog())
                    addConsole("行先 " + mapCellDto.toString());
            }
        } catch (Exception e) {
            LOG.get().warn("進撃を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 基地航空隊出撃を更新します
     *
     * @param data
     */
    private static void doStartAirBase(Data data, JsonValue json) {
        try {
            for (int i = 1; i <= 4; ++i) {
                String strikePoint = data.getField("api_strike_point_" + i);
                if (strikePoint != null) {
                    addConsole("基地航空隊 " + i + " -> " + strikePoint);
                }
            }
        } catch (Exception e) {
            LOG.get().warn("基地航空隊出撃しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 設定を更新します
     *
     * @param data
     */
    private static void doStart2(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
                MasterData.updateMaster(apidata);
            }

            addConsole("マスターデータを更新しました");
        } catch (Exception e) {
            LOG.get().warn("設定を更新しますに失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * マップ情報を処理します
     *
     * @param data
     */
    private static void doMapInfo(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonValue api_map_info = ((JsonObject) json).get("api_map_info");
                if (api_map_info instanceof JsonArray) {
                    JsonArray apidata = (JsonArray) api_map_info;
                    MasterData.updateMapInfo(apidata);

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
                }
            }
        } catch (Exception e) {
            LOG.get().warn("マップ情報更新に失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 任務情報を処理します
     *
     * @param data
     */
    private static void doMission(Data data, JsonValue json) {
        try {
            if (json instanceof JsonArray) {
                JsonArray apidata = (JsonArray) json;
                MasterData.updateMission(apidata);
            }
        } catch (Exception e) {
            LOG.get().warn("任務情報更新に失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 演習情報を処理します
     *
     * @param data
     */
    private static void doPractice(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonValue api_list = ((JsonObject) json).get("api_list");
                if (api_list instanceof JsonArray) {
                    JsonArray apidata = (JsonArray) api_list;
                    for (int i = 0; i < apidata.size(); ++i) {
                        PracticeUserDto dto = new PracticeUserDto((JsonObject) apidata.get(i));
                        if ((practiceUser[i] == null) || (practiceUser[i].getId() != dto.getId()))
                            practiceUser[i] = dto;
                        else
                            // stateだけ更新
                            practiceUser[i].setState(dto.getState());
                    }
                }
                practiceUserLastUpdate = new Date();
                addUpdateLog("演習情報を更新しました");
            }
        } catch (Exception e) {
            LOG.get().warn("演習情報更新に失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 演習相手艦隊情報を処理します
     * @param data
     */
    private static void doPracticeEnemyinfo(Data data, JsonValue json) {
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
            LOG.get().warn("演習相手艦隊情報更新に失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 連合艦隊操作を処理します
     *
     * @param data
     */
    private static void doCombined(Data data, JsonValue json) {
        try {
            JsonObject apidata = data.getJsonObject().getJsonObject("api_data");
            combinedKind = apidata.getInt("api_combined");
            for (int i = 0; i < 2; ++i) {
                DockDto dockdto = dock.get(Integer.toString(i + 1));
                if (dockdto != null) {
                    dockdto.setUpdate(true);
                }
            }
            addUpdateLog("連合艦隊情報を更新しました");
        } catch (Exception e) {
            LOG.get().warn("連合艦隊情報更新に失敗しました", e);
            LOG.get().warn(data);
        }
    }

    /**
     * 装備改修
     */
    private static void doRemodelSlot(Data data, JsonValue json) {
        try {
            if (json instanceof JsonObject) {
                JsonObject apidata = (JsonObject) json;
                if (apidata.getInt("api_remodel_flag") != 0) { // 成功した時だけ
                    // 改修したアイテムを更新
                    addSlotitem(apidata.getJsonObject("api_after_slot"));
                }

                // 消費した装備アイテムを削除
                if (JsonUtils.hasKey(apidata, "api_use_slot_id")) {
                    JsonArray useSlotId = apidata.getJsonArray("api_use_slot_id");
                    for (int i = 0; i < useSlotId.size(); ++i) {
                        itemMap.remove(useSlotId.getInt(i));
                    }
                }

                // 資源に反映させてレポート
                JsonArray newMaterial = apidata.getJsonArray("api_after_material");
                ResourceItemDto items = new ResourceItemDto();
                items.loadMaterialFronJson(newMaterial);
                updateDetailedMaterial("装備改修", items, MATERIAL_DIFF.NEW_VALUE);
            }

            addUpdateLog("装備改修情報を更新しました");
        } catch (Exception e) {
            LOG.get().warn("装備改修更新に失敗しました", e);
            LOG.get().warn(data);
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
                LOG.get().warn("ダメージ計算ミスが発生しています。" + new_ship.getName() + "の現在のHPは" + new_ship.getNowhp()
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
        return checkDataState(false);
    }

    /**
     * 取得した情報に不完全なものがないかチェック
     * @param ignoreSlotitem 艦娘の装備データチェックをスキップするか
     * @return　新しいstate
     */
    private static int checkDataState(boolean ignoreSlotitem) {
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
        if (!ignoreSlotitem) {
            for (ShipDto ship : shipMap.values()) {
                for (int itemId : ship.getItemId()) {
                    if (itemId != -1) {
                        if (itemMap.containsKey(itemId) == false) {
                            return 2;
                        }
                    }
                }
            }
        }
        return 1; // 正常
    }

    /** 艦娘をshipMapに追加 */
    private static void addShip(ShipDto ship) {
        if (nextShipId <= ship.getId()) {
            nextShipId = ship.getId() + 1;
        }
        shipMap.put(ship.getId(), ship);
    }

    /** 装備アイテムをitemMapに追加 */
    private static ItemDto addSlotitem(JsonObject object) {
        int slotitemId = object.getInt("api_slotitem_id");
        ItemInfoDto info = Item.get(slotitemId);
        if (info != null) {
            ItemDto dto = new ItemDto(info, object);
            itemMap.put(dto.getId(), dto);
            if (nextSlotitemId <= dto.getId()) {
                nextSlotitemId = dto.getId() + 1;
            }
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
                CreateReportLogic.storeMaterialReport(material, basic);
            }
        }
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