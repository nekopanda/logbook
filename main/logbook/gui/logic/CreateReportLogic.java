package logbook.gui.logic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import logbook.config.AppConfig;
import logbook.config.MasterDataConfig;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.BattleExDto;
import logbook.dto.BattleResultDto;
import logbook.dto.CreateItemDto;
import logbook.dto.DockDto;
import logbook.dto.EnemyShipDto;
import logbook.dto.GetShipDto;
import logbook.dto.ItemDto;
import logbook.dto.LostEntityDto;
import logbook.dto.MaterialDto;
import logbook.dto.MissionResultDto;
import logbook.dto.QuestDto;
import logbook.dto.ResourceItemDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipFilterDto;
import logbook.dto.ShipInfoDto;
import logbook.dto.ShipParameters;
import logbook.dto.UseItemDto;
import logbook.internal.BattleResultFilter;
import logbook.internal.BattleResultServer;
import logbook.internal.MasterData;
import logbook.internal.Ship;
import logbook.util.ReportUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 各種報告書を作成します
 *
 */
public final class CreateReportLogic {

    /** テーブルアイテム作成(デフォルト) */
    public static final TableItemCreator DEFAULT_TABLE_ITEM_CREATOR = new TableItemCreator() {

        @Override
        public void init() {
        }

        @Override
        public TableItem create(Table table, Comparable[] text, int count) {
            TableItem item = new TableItem(table, SWT.NONE);
            // 偶数行に背景色を付ける
            if ((count % 2) != 0) {
                item.setBackground(SWTResourceManager.getColor(AppConstants.ROW_BACKGROUND));
            }
            item.setText(toStringArray(text));
            return item;
        }
    };

    /** テーブルアイテム作成(所有艦娘一覧) */
    public static final TableItemCreator SHIP_LIST_TABLE_ITEM_CREATOR = new TableItemCreator() {

        private Set<Integer> deckmissions;

        private Set<Integer> docks;

        @Override
        public void init() {
            this.deckmissions = GlobalContext.getMissionShipSet();
            this.docks = GlobalContext.getNDockShipSet();
        }

        @Override
        public TableItem create(Table table, Comparable[] text, int count) {
            // 艦娘
            ShipDto ship = (ShipDto) ((TableRowHeader) text[0]).get();

            TableItem item = new TableItem(table, SWT.NONE);

            item.setData(ship);

            // 偶数行に背景色を付ける
            if ((count % 2) != 0) {
                item.setBackground(SWTResourceManager.getColor(AppConstants.ROW_BACKGROUND));
            }

            // 疲労
            int cond = ship.getCond();
            if (cond <= AppConstants.COND_RED) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
            } else if (cond <= AppConstants.COND_ORANGE) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
            }

            // 遠征
            if (this.deckmissions.contains(ship.getId())) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.MISSION_COLOR));
            }
            // 入渠
            if (this.docks.contains(ship.getId())) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.NDOCK_COLOR));
            }

            item.setText(toStringArray(text));
            return item;
        }
    };

    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(CreateReportLogic.class);

    /**
     * ドロップ報告書のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getBattleResultHeader() {
        return new String[] { "", "日付", "海域", "マス", "ランク", "敵艦隊", "ドロップ艦種", "ドロップ艦娘", "大破艦" };
    }

    /**
     * ドロップ報告書の内容
     * @return 内容
     */
    public static List<Comparable[]> getBattleResultBody(BattleResultFilter filter) {
        List<BattleResultDto> results = BattleResultServer.get().getFilteredList(filter);
        List<Comparable[]> body = new ArrayList<Comparable[]>();

        for (int i = 0; i < results.size(); i++) {
            BattleResultDto item = results.get(i);
            body.add(new Comparable[] {
                    new TableRowHeader(i + 1, item),
                    new DateTimeString(item.getBattleDate()), item.getQuestName(),
                    item.getMapCell(), item.getRank(), item.getEnemyName(), item.getDropType(),
                    item.getDropName(), item.isHasTaiha() ? "あり" : "" });
        }
        return body;
    }

    /**
     * ドロップ報告書のヘッダー(保存用)
     * 
     * @return ヘッダー
     */
    public static String[] getBattleResultStoreHeader() {
        return new String[] { "", "日付", "海域", "マス", "ランク", "敵艦隊", "ドロップ艦種", "ドロップ艦娘",
                "味方艦1", "味方艦1HP",
                "味方艦2", "味方艦2HP",
                "味方艦3", "味方艦3HP",
                "味方艦4", "味方艦4HP",
                "味方艦5", "味方艦5HP",
                "味方艦6", "味方艦6HP",
                "敵艦1", "敵艦1HP",
                "敵艦2", "敵艦2HP",
                "敵艦3", "敵艦3HP",
                "敵艦4", "敵艦4HP",
                "敵艦5", "敵艦5HP",
                "敵艦6", "敵艦6HP" };
    }

    /**
     * ドロップ報告書の内容(保存用)
     * @param results ドロップ報告書
     * 
     * @return 内容
     */
    public static List<Comparable[]> getBattleResultStoreBody(List<BattleExDto> results) {
        List<Comparable[]> body = new ArrayList<Comparable[]>();

        for (int i = 0; i < results.size(); i++) {
            BattleExDto battle = results.get(i);
            String[] friend = new String[6];
            String[] friendHp = new String[6];
            String[] enemy = new String[6];
            String[] enemyHp = new String[6];

            Arrays.fill(friend, "");
            Arrays.fill(friendHp, "");
            Arrays.fill(enemy, "");
            Arrays.fill(enemyHp, "");

            List<DockDto> docks = battle.getFriends();
            if (docks != null) {
                DockDto dock = docks.get(0);
                List<ShipDto> friendships = dock.getShips();
                int[] fnowhps = battle.getNowFriendHp();
                int[] fmaxhps = battle.getMaxFriendHp();
                for (int j = 0; j < friendships.size(); j++) {
                    ShipDto ship = friendships.get(j);
                    friend[j] = ship.getName() + "(Lv" + ship.getLv() + ")";
                    friendHp[j] = fnowhps[j] + "/" + fmaxhps[j];
                }
                List<EnemyShipDto> enemyships = battle.getEnemy();
                int[] enowhps = battle.getNowEnemyHp();
                int[] emaxhps = battle.getMaxEnemyHp();
                for (int j = 0; j < enemyships.size(); j++) {
                    EnemyShipDto ship = enemyships.get(j);
                    enemy[j] = ship.getFriendlyName();
                    enemyHp[j] = enowhps[j] + "/" + emaxhps[j];
                }
            }

            body.add(new Comparable[] { Integer.toString(i + 1),
                    new DateTimeString(battle.getBattleDate()),
                    battle.getQuestName(),
                    battle.getMapCellDto().toString(), battle.getRank(), battle.getEnemyName(), battle.getDropType(),
                    battle.getDropName(),
                    friend[0], friendHp[0], friend[1], friendHp[1], friend[2], friendHp[2], friend[3], friendHp[3],
                    friend[4], friendHp[4], friend[5], friendHp[5], enemy[0], enemyHp[0], enemy[1], enemyHp[1],
                    enemy[2], enemyHp[2], enemy[3], enemyHp[3], enemy[4], enemyHp[4], enemy[5], enemyHp[5] });
        }
        return body;
    }

    /**
     * 建造報告書のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getCreateShipHeader() {
        return new String[] { "", "日付", "種類", "名前", "艦種", "燃料", "弾薬", "鋼材", "ボーキ", "開発資材", "空きドック", "秘書艦", "司令部Lv" };
    }

    /**
     * 建造報告書の内容
     * 
     * @return 内容
     */
    public static List<Comparable[]> getCreateShipBody(List<GetShipDto> ships) {
        List<Comparable[]> body = new ArrayList<Comparable[]>();
        for (int i = 0; i < ships.size(); i++) {
            GetShipDto ship = ships.get(i);
            body.add(new Comparable[] { Integer.toString(i + 1),
                    new DateTimeString(ship.getGetDate()), ship.getBuildType(),
                    ship.getName(), ship.getType(), ship.getFuel(), ship.getAmmo(), ship.getMetal(), ship.getBauxite(),
                    ship.getResearchMaterials(), ship.getFreeDock(), ship.getSecretary(), ship.getHqLevel() });
        }
        return body;
    }

    /**
     * 開発報告書のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getCreateItemHeader() {
        return new String[] { "", "日付", "開発装備", "種別", "燃料", "弾薬", "鋼材", "ボーキ", "秘書艦", "司令部Lv" };
    }

    /**
     * 開発報告書の内容
     * 
     * @return 内容
     */
    public static List<Comparable[]> getCreateItemBody(List<CreateItemDto> items) {
        List<Comparable[]> body = new ArrayList<Comparable[]>();

        for (int i = 0; i < items.size(); i++) {
            CreateItemDto item = items.get(i);
            String name = "失敗";
            String type = "";
            if (item.isCreateFlag()) {
                name = item.getName();
                type = item.getType();
            }
            body.add(new Comparable[] { Integer.toString(i + 1),
                    new DateTimeString(item.getCreateDate()), name, type,
                    item.getFuel(), item.getAmmo(), item.getMetal(), item.getBauxite(), item.getSecretary(),
                    item.getHqLevel() });
        }
        return body;
    }

    /**
     * 所有装備一覧のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getItemListHeader() {
        return new String[] { "", "名称", "種別", "個数", "火力", "命中", "射程", "運", "回避", "爆装", "雷装", "索敵", "対潜", "対空", "装甲",
                "装備してる艦娘" };
    }

    private static class ItemInfo {
        public ItemDto item;
        public int count = 1;
        public Map<ShipDto, Integer> shipMap = new TreeMap<>();

        public ItemInfo(ItemDto item) {
            this.item = item;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Entry<ShipDto, Integer> entry : this.shipMap.entrySet()) {
                if (count++ != 0) {
                    sb.append(",");
                }
                sb.append(entry.getKey().getFriendlyName()).append("x").append(entry.getValue());
            }
            // 長さ制限
            if (sb.length() > 100) {
                sb.setLength(100);
                sb.append(" ...");
            }
            return sb.toString();
        }
    }

    /**
     * 所有装備一覧の内容
     * 
     * @return 内容
     */
    public static List<Comparable[]> getItemListBody() {
        Map<ItemDto, ItemInfo> itemCountMap = new HashMap<ItemDto, ItemInfo>();

        for (ItemDto item : GlobalContext.getItemMap().values()) {
            ItemInfo info = itemCountMap.get(item);
            if (info == null) {
                info = new ItemInfo(item);
                itemCountMap.put(item, info);
            } else {
                info.count++;
            }
        }

        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            for (ItemDto item : ship.getItem()) {
                if (item != null) {
                    Map<ShipDto, Integer> shipMap = itemCountMap.get(item).shipMap;
                    Integer count = shipMap.get(ship);
                    if (count == null) {
                        count = 1;
                    }
                    else {
                        count++;
                    }
                    shipMap.put(ship, count);
                }
            }
        }

        List<ItemInfo> countitems = new ArrayList<ItemInfo>(itemCountMap.values());
        Collections.sort(countitems, new Comparator<ItemInfo>() {
            @Override
            public int compare(ItemInfo o1, ItemInfo o2) {
                return Integer.compare(o1.count, o2.count);
            }
        });

        List<Comparable[]> body = new ArrayList<Comparable[]>();

        int count = 0;
        for (ItemInfo itemInfo : countitems) {
            ItemDto item = itemInfo.item;
            ShipParameters param = item.getParam();
            count++;
            body.add(new Comparable[] { count, item.getName(), item.getTypeName(), itemInfo.count, param.getHoug(),
                    param.getHoum(), param.getLeng(), param.getLuck(), param.getHouk(), param.getBaku(),
                    param.getRaig(), param.getSaku(), param.getTais(), param.getTyku(), param.getSouk(),
                    itemInfo.toString()
            });
        }
        return body;
    }

    /**
     * 所有艦娘一覧のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getShipListHeader() {
        return new String[] {
                "",
                "ID",
                "鍵",//
                "艦隊",
                "Lv順", //
                "艦種順", //
                "NEW順", //
                "修理順", //
                "名前",
                "艦種",
                "艦ID",//
                "現在",//
                "疲労",
                "回復",
                "HP", //
                "燃料",//
                "弾薬",//
                "修理時間",//
                "燃料",//
                "鋼材",//
                "損傷",//
                "HP1あたり", //
                "Lv",
                "Next",
                "経験値",
                "制空",
                "索敵", //
                "装備1",
                "艦載機1", //
                "装備2",
                "艦載機2", //
                "装備3",
                "艦載機3", //
                "装備4",
                "艦載機4", //
                "耐久",
                "燃料",//
                "弾薬",//
                "火力",
                "雷装",
                "対空",
                "装甲",
                "回避",
                "対潜",
                "索敵",
                "運"
        };
    }

    /**
     * 所有艦娘一覧の内容
     * 
     * @param specdiff 成長余地
     * @param filter 鍵付きのみ
     * @return 内容
     */
    public static List<Comparable[]> getShipListBody(boolean specdiff, ShipFilterDto filter) {
        Set<Integer> missionSet = GlobalContext.getMissionShipSet();
        List<Comparable[]> body = new ArrayList<Comparable[]>();
        int count = 0;
        for (ShipWithSortNumber shipObj : getShipWithSortNumber()) {
            ShipDto ship = shipObj.ship;

            if ((filter != null) && !shipFilter(ship, filter)) {
                continue;
            }

            ShipInfoDto shipInfo = Ship.get(String.valueOf(ship.getShipId()));

            count++;

            String fleet = null;
            if (ship.isFleetMember()) {
                fleet = String.valueOf(ship.getFleetid()) + "-" + String.valueOf(ship.getFleetpos() + 1);
            }

            String now = "";
            if (missionSet.contains(ship.getId())) {
                now = "遠征中";
            }
            else if (GlobalContext.isNdock(ship.getId())) {
                now = "入渠中";
            }

            ShipParameters param = new ShipParameters();
            if (specdiff) {
                // 成長の余地 = (装備なしのMAX) + (装備による上昇分) - (装備込の現在値)
                param.add(ship.getMax());
                param.add(ship.getSlotParam());
                param.subtract(ship.getParam());
            }
            else {
                param.add(ship.getParam());
            }

            // HP1あたりの時間
            long dockTime = ship.getDocktime();
            long unitSeconds = ((long) (dockTime
                    / (float) (ship.getMaxhp() - ship.getNowhp()) / 1000));
            // 損傷
            String damage = "";
            if (ship.isBadlyDamage()) {
                damage = "大破";
            } else if (ship.isHalfDamage()) {
                damage = "中破";
            } else if (ship.isSlightDamage()) {
                damage = "小破";
            }

            // 艦載機数
            List<String> slotString = ship.getSlot();
            HpString[] onSlotString = new HpString[4];
            if (ship.canEquipPlane()) { // 飛行機を装備できる場合だけ
                List<ItemDto> slotItem = ship.getItem();
                int[] onSlot = ship.getOnSlot();
                int[] maxEq = shipInfo.getMaxeq();
                int slotNum = ship.getSlotNum();
                for (int i = 0; i < slotNum; ++i) {
                    ItemDto item = slotItem.get(i);
                    int cur = ((item != null) && item.isPlane()) ? onSlot[i] : 0;
                    int max = maxEq != null ? maxEq[i] : 0;
                    onSlotString[i] = new HpString(cur, max);
                }
            }

            body.add(new Comparable[] {
                    new TableRowHeader(count, ship),
                    ship.getId(),
                    ship.getLocked() ? "♥" : "",
                    fleet,
                    getPageNumber(shipObj.sortNumber[0]),
                    getPageNumber(shipObj.sortNumber[1]),
                    getPageNumber(shipObj.sortNumber[2]),
                    getPageNumber(shipObj.sortNumber[3]),
                    ship.getName(),
                    ship.getType(),
                    ship.getCharId(),
                    now,
                    ship.getCond(),
                    (ship.getCond() < 49) ? new TimeString(ship.getCondClearTime().getTime()) : null,
                    new HpString(ship.getNowhp(), ship.getMaxhp()),
                    new HpString(ship.getFuel(), ship.getFuelMax()),
                    new HpString(ship.getBull(), ship.getBullMax()),
                    dockTime > 0 ? new TimeLogic(ship.getDocktime()) : null,
                    dockTime > 0 ? ship.getDockfuel() : null,
                    dockTime > 0 ? ship.getDockmetal() : null,
                    damage,
                    dockTime > 0 ? TimeLogic.fromSeconds(unitSeconds) : null,
                    ship.getLv(),
                    ship.getNext(),
                    ship.getExp(),
                    ship.getSeiku(),
                    new SakutekiString(ship),
                    slotString.get(0),
                    onSlotString[0],
                    slotString.get(1),
                    onSlotString[1],
                    slotString.get(2),
                    onSlotString[2],
                    slotString.get(3),
                    onSlotString[3],
                    ship.getMaxhp(),
                    ship.getFuelMax(),
                    ship.getBullMax(),
                    param.getKaryoku(),
                    param.getRaisou(),
                    param.getTaiku(),
                    param.getSoukou(),
                    param.getKaihi(),
                    param.getTaisen(),
                    param.getSakuteki(),
                    param.getLucky()
            });
        }
        return body;
    }

    private static IntegerPair getPageNumber(int index) {
        return new IntegerPair((index / 10) + 1, (index % 10) + 1, "-");
    }

    private static class ShipWithSortNumber {
        public ShipDto ship;
        /** Lv順, 艦種順, NEW順, 修理順 (ゼロ始まり) */
        public int[] sortNumber = new int[4];

        public ShipWithSortNumber(ShipDto ship) {
            this.ship = ship;
        }
    }

    private static class ShipComparatorBase implements Comparator<ShipWithSortNumber> {
        @Override
        public int compare(ShipWithSortNumber o1, ShipWithSortNumber o2) {
            int ret = Integer.compare(o1.ship.getSortno(), o2.ship.getSortno());
            if (ret == 0) {
                ret = Integer.compare(o1.ship.getId(), o2.ship.getId());
            }
            return ret;
        }
    }

    private static void genSortNumber(List<ShipWithSortNumber> list, int index, Comparator<ShipWithSortNumber> comp) {
        Collections.sort(list, comp);
        for (int i = 0; i < list.size(); ++i) {
            list.get(i).sortNumber[index] = i;
        }
    }

    private static List<ShipWithSortNumber> getShipWithSortNumber() {
        List<ShipWithSortNumber> ships = new ArrayList<ShipWithSortNumber>();
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            ships.add(new ShipWithSortNumber(ship));
        }
        // Lv順
        genSortNumber(ships, 0, new ShipComparatorBase() {
            @Override
            public int compare(ShipWithSortNumber o1, ShipWithSortNumber o2) {
                int ret = -Integer.compare(o1.ship.getLv(), o2.ship.getLv());
                if (ret == 0) {
                    return super.compare(o1, o2);
                }
                return ret;
            }
        });
        // 艦種順
        genSortNumber(ships, 1, new Comparator<ShipWithSortNumber>() {
            @Override
            public int compare(ShipWithSortNumber o1, ShipWithSortNumber o2) {
                int ret = -Integer.compare(o1.ship.getShipInfo().getStype(), o2.ship.getShipInfo().getStype());
                if (ret == 0) {
                    ret = Integer.compare(o1.ship.getSortno(), o2.ship.getSortno());
                    /*// Lv順の後に安定ソートするので
                    if (ret == 0) {
                        ret = -Integer.compare(o1.ship.getLv(), o2.ship.getLv());
                        if (ret == 0) {
                            ret = Integer.compare(o1.ship.getId(), o2.ship.getId());
                        }
                    }
                    */
                }
                return ret;
            }
        });
        // NEW
        genSortNumber(ships, 2, new ShipComparatorBase() {
            @Override
            public int compare(ShipWithSortNumber o1, ShipWithSortNumber o2) {
                return -Integer.compare(o1.ship.getId(), o2.ship.getId());
            }
        });
        // 修理順
        genSortNumber(ships, 3, new ShipComparatorBase() {
            @Override
            public int compare(ShipWithSortNumber o1, ShipWithSortNumber o2) {
                double o1rate = (double) o1.ship.getNowhp() / (double) o1.ship.getMaxhp();
                double o2rate = (double) o2.ship.getNowhp() / (double) o2.ship.getMaxhp();
                int ret = Double.compare(o1rate, o2rate);
                if (ret == 0) {
                    return super.compare(o1, o2);
                }
                return ret;
            }
        });
        // 最後にID順にしておく
        Collections.sort(ships, new Comparator<ShipWithSortNumber>() {
            @Override
            public int compare(ShipWithSortNumber o1, ShipWithSortNumber o2) {
                return Integer.compare(o1.ship.getId(), o2.ship.getId());
            }
        });
        return ships;
    }

    /**
     * 遠征結果のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getCreateMissionResultHeader() {
        return new String[] { "", "日付", "結果", "遠征", "燃料", "弾薬", "鋼材", "ボーキ", "アイテム1", "個数", "アイテム2", "個数" };
    }

    /**
     * 遠征結果一覧の内容
     * 
     * @return 遠征結果
     */
    public static List<Comparable[]> getMissionResultBody(List<MissionResultDto> resultlist) {
        List<Comparable[]> body = new ArrayList<Comparable[]>();

        for (int i = 0; i < resultlist.size(); i++) {
            MissionResultDto result = resultlist.get(i);

            String[] itemName = new String[] { "", "" };
            String[] itemCount = new String[] { "", "" };
            MasterData masterData = MasterDataConfig.get();
            ResourceItemDto resItems = result.getItems();
            if (resItems != null) {
                Map<Integer, UseItemDto> items = resItems.getItems();
                int index = 0;
                for (UseItemDto item : items.values()) {
                    itemName[index] = item.getItemName(masterData);
                    itemCount[index] = String.valueOf(item.getItemCount());
                    if (++index >= 2)
                        break;
                }
            }

            body.add(new Comparable[] {
                    Integer.toString(i + 1),
                    new DateTimeString(result.getDate()),
                    result.getClearResult(),
                    result.getQuestName(),
                    result.getFuel(),
                    result.getAmmo(),
                    result.getMetal(),
                    result.getBauxite(),
                    itemName[0],
                    itemCount[0],
                    itemName[1],
                    itemCount[1],
            });
        }
        return body;
    }

    /**
     * 任務一覧のヘッダー
     * 
     * @return
     */
    public static String[] getCreateQuestHeader() {
        return new String[] { "No.", "表示位置", "状態", "タイトル", "内容", "燃料", "弾薬", "鋼材", "ボーキ" };
    }

    /**
     * 任務一覧の内容
     * 
     * @return
     */
    public static List<Comparable[]> getQuestBody() {
        List<Comparable[]> body = new ArrayList<Comparable[]>();

        for (QuestDto quest : GlobalContext.getQuest()) {
            if (quest == null)
                continue;

            String state = "";
            switch (quest.getState()) {
            case 1:
                state = "";
                break;
            case 2:
                state = "遂行中";
                break;
            case 3:
                state = "達成";
                break;
            default:
                continue;
            }

            body.add(new Comparable[] {
                    quest.getNo(),
                    "" + quest.getPage() + "-" + quest.getPos(),
                    state,
                    quest.getTitle(),
                    quest.getDetail(),
                    quest.getFuel(),
                    quest.getAmmo(),
                    quest.getMetal(),
                    quest.getBauxite()
            });
        }
        return body;
    }

    /**
     * 資材のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getMaterialHeader() {
        return new String[] { "", "日付", "直前のイベント", "燃料", "弾薬", "鋼材", "ボーキ", "高速修復材", "高速建造材", "開発資材" };
    }

    /**
     * 資材の内容
     * 
     * @param materials 資材
     * @return
     */
    public static List<Comparable[]> getMaterialStoreBody(List<MaterialDto> materials) {
        List<Comparable[]> body = new ArrayList<Comparable[]>();

        for (int i = 0; i < materials.size(); i++) {
            MaterialDto material = materials.get(i);
            body.add(new Comparable[] {
                    i + 1,
                    new DateTimeString(material.getTime()),
                    material.getEvent(),
                    material.getFuel(),
                    material.getAmmo(),
                    material.getMetal(),
                    material.getBauxite(),
                    material.getBucket(),
                    material.getBurner(),
                    material.getResearch()
            });
        }

        return body;
    }

    /**
     * ロストログのヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getLostHeader() {
        return new String[] { "", "日付", "種別", "個別ID", "名前", "原因" };
    }

    /**
     * ロストログの内容
     * 
     * @param lostList ロストデータ
     * @return
     */
    public static List<Comparable[]> getLostStoreBody(List<LostEntityDto> lostList) {
        List<Comparable[]> body = new ArrayList<Comparable[]>();

        for (int i = 0; i < lostList.size(); i++) {
            LostEntityDto lost = lostList.get(i);
            body.add(new Comparable[] {
                    i + 1,
                    new DateTimeString(lost.getTime()),
                    lost.getLostEntity(),
                    lost.getEntityId(),
                    lost.getName(),
                    lost.getEventCaused()
            });
        }

        return body;
    }

    /**
     * 報告書をCSVファイルに書き込む(最初の列を取り除く)
     * 
     * @param file ファイル
     * @param header ヘッダー
     * @param body 内容
     * @param applend 追記フラグ
     * @throws IOException
     */
    public static void writeCsvStripFirstColumn(File file, String[] header, List<Comparable[]> body, boolean applend)
            throws IOException {
        // 報告書の項番を除く
        String[] copyheader = Arrays.copyOfRange(header, 1, header.length);
        List<Comparable[]> copybody = new ArrayList<Comparable[]>();
        for (Comparable[] strings : body) {
            copybody.add(Arrays.copyOfRange(strings, 1, strings.length));
        }
        writeCsv(file, copyheader, copybody, applend);
    }

    /**
     * 報告書をCSVファイルに書き込む
     * 
     * @param file ファイル
     * @param header ヘッダー
     * @param body 内容
     * @param applend 追記フラグ
     * @throws IOException
     */
    public static void writeCsv(File file, String[] header, List<Comparable[]> body, boolean applend)
            throws IOException {
        OutputStream stream = new BufferedOutputStream(new FileOutputStream(file, applend));
        try {
            if (!file.exists() || (FileUtils.sizeOf(file) <= 0)) {
                IOUtils.write(StringUtils.join(header, ',') + "\r\n", stream, AppConstants.CHARSET);
            }
            for (Comparable[] colums : body) {
                IOUtils.write(StringUtils.join(colums.toString(), ',') + "\r\n", stream, AppConstants.CHARSET);
            }
        } finally {
            stream.close();
        }
    }

    /**
     * オブジェクト配列をテーブルウィジェットに表示できるように文字列に変換します
     * 
     * @param from テーブルに表示する内容
     * @return テーブルに表示する内容
     */
    public static String[] toStringArray(Comparable[] data) {
        String[] ret = new String[data.length];
        for (int i = 0; i < data.length; ++i) {
            if (data[i] == null) {
                ret[i] = "";
            }
            else {
                ret[i] = data[i].toString();
            }
        }
        return ret;
    }

    /**
     * 艦娘をフィルタします
     * 
     * @param ship 艦娘
     * @param filter フィルターオブジェクト
     * @return フィルタ結果
     */
    private static boolean shipFilter(ShipDto ship, ShipFilterDto filter) {
        // 名前でフィルタ
        if (!StringUtils.isEmpty(filter.nametext)) {
            // テキストが入力されている場合処理する
            if (filter.regexp) {
                // 正規表現で検索
                try {
                    filter.namepattern = Pattern.compile(filter.nametext);
                } catch (PatternSyntaxException e) {
                    // 無効な正規表現はfalseを返す
                    return false;
                }
                Matcher matcher = filter.namepattern.matcher(ship.getName());
                if (!matcher.find()) {
                    // マッチしない
                    return false;
                }
            } else {
                // 部分一致で検索する
                if (ship.getName().indexOf(filter.nametext) == -1) {
                    return false;
                }
            }
        }
        // 艦種でフィルタ
        if (!filter.destroyer) {
            if ("駆逐艦".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.lightCruiser) {
            if ("軽巡洋艦".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.torpedoCruiser) {
            if ("重雷装巡洋艦".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.heavyCruiser) {
            if ("重巡洋艦".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.flyingDeckCruiser) {
            if ("航空巡洋艦".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.seaplaneTender) {
            if ("水上機母艦".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.escortCarrier) {
            if ("軽空母".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.carrier) {
            if ("正規空母".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.battleship) {
            if ("戦艦".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.flyingDeckBattleship) {
            if ("航空戦艦".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.submarine) {
            if ("潜水艦".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.carrierSubmarine) {
            if ("潜水空母".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.landingship) {
            if ("揚陸艦".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.armoredcarrier) {
            if ("装甲空母".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.repairship) {
            if ("工作艦".equals(ship.getType())) {
                return false;
            }
        }
        if (!filter.submarineTender) {
            if ("潜水母艦".equals(ship.getType())) {
                return false;
            }
        }
        // グループでフィルタ
        if (filter.group != null) {
            if (!filter.group.getShips().contains(ship.getId())) {
                return false;
            }
        }
        // 装備でフィルタ
        if (!StringUtils.isEmpty(filter.itemname)) {
            List<ItemDto> item = ship.getItem();
            boolean hit = false;
            for (ItemDto itemDto : item) {
                if (itemDto != null) {
                    if (filter.itemname.equals(itemDto.getName())) {
                        hit = true;
                        break;
                    }
                }
            }
            if (!hit) {
                return false;
            }
        }
        // 艦隊に所属
        if (!filter.onfleet) {
            if (!StringUtils.isEmpty(ship.getFleetid())) {
                return false;
            }
        }
        // 艦隊に非所属
        if (!filter.notonfleet) {
            if (StringUtils.isEmpty(ship.getFleetid())) {
                return false;
            }
        }
        // 鍵付き
        if (!filter.locked) {
            if (ship.getLocked()) {
                return false;
            }
        }
        // 鍵付きではない
        if (!filter.notlocked) {
            if (!ship.getLocked()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 海戦・ドロップ報告書を書き込む
     * 
     * @param dto 海戦・ドロップ報告
     */
    public static void storeBattleResultReport(BattleExDto dto) {
        try {
            List<BattleExDto> dtoList = Collections.singletonList(dto);

            File report = getStoreFile("海戦・ドロップ報告書.csv", "海戦・ドロップ報告書_alternativefile.csv");

            CreateReportLogic.writeCsvStripFirstColumn(report,
                    CreateReportLogic.getBattleResultStoreHeader(),
                    CreateReportLogic.getBattleResultStoreBody(dtoList), true);
        } catch (IOException e) {
            LOG.warn("報告書の保存に失敗しました", e);
        }
    }

    /**
     * 建造報告書を書き込む
     * 
     * @param dto 建造報告
     */
    public static void storeCreateShipReport(GetShipDto dto) {
        try {
            List<GetShipDto> dtoList = Collections.singletonList(dto);

            File report = getStoreFile("建造報告書.csv", "建造報告書_alternativefile.csv");

            CreateReportLogic.writeCsvStripFirstColumn(report,
                    CreateReportLogic.getCreateShipHeader(),
                    CreateReportLogic.getCreateShipBody(dtoList), true);
        } catch (IOException e) {
            LOG.warn("報告書の保存に失敗しました", e);
        }
    }

    /**
     * 開発報告書を書き込む
     * 
     * @param dto 開発報告
     */
    public static void storeCreateItemReport(CreateItemDto dto) {
        try {
            List<CreateItemDto> dtoList = Collections.singletonList(dto);

            File report = getStoreFile("開発報告書.csv", "開発報告書_alternativefile.csv");

            CreateReportLogic.writeCsvStripFirstColumn(report,
                    CreateReportLogic.getCreateItemHeader(),
                    CreateReportLogic.getCreateItemBody(dtoList), true);
        } catch (IOException e) {
            LOG.warn("報告書の保存に失敗しました", e);
        }
    }

    /**
     * 遠征報告書を書き込む
     * 
     * @param dto 遠征結果
     */
    public static void storeCreateMissionReport(MissionResultDto dto) {
        try {
            List<MissionResultDto> dtoList = Collections.singletonList(dto);

            File report = getStoreFile("遠征報告書.csv", "遠征報告書_alternativefile.csv");

            CreateReportLogic.writeCsvStripFirstColumn(report,
                    CreateReportLogic.getCreateMissionResultHeader(),
                    CreateReportLogic.getMissionResultBody(dtoList), true);
        } catch (IOException e) {
            LOG.warn("報告書の保存に失敗しました", e);
        }
    }

    /**
     * 資材ログを書き込む
     * 
     * @param material 資材
     */
    public static void storeMaterialReport(MaterialDto material) {
        try {
            if (material != null) {
                List<MaterialDto> dtoList = Collections.singletonList(material);

                File report = getStoreFile("資材ログ.csv", "資材ログ_alternativefile.csv");

                CreateReportLogic.writeCsvStripFirstColumn(report,
                        CreateReportLogic.getMaterialHeader(),
                        CreateReportLogic.getMaterialStoreBody(dtoList), true);
            }
        } catch (IOException e) {
            LOG.warn("報告書の保存に失敗しました", e);
        }
    }

    /**
     * 解体・廃棄ログを書き込む
     * 
     * @param dtoList 解体・廃棄情報
     */
    public static void storeLostReport(List<LostEntityDto> dtoList) {
        try {
            if (dtoList != null) {
                File report = getStoreFile("解体・廃棄ログ.csv", "解体・廃棄ログ_alternativefile.csv");

                CreateReportLogic.writeCsvStripFirstColumn(report,
                        CreateReportLogic.getLostHeader(),
                        CreateReportLogic.getLostStoreBody(dtoList), true);
            }
        } catch (IOException e) {
            LOG.warn("報告書の保存に失敗しました", e);
        }
    }

    /**
     * 書き込み先のファイルを返します
     * 
     * @param name ファイル名
     * @param altername 代替ファイル名
     * @return File
     * @throws IOException
     */
    private static File getStoreFile(String name, String altername) throws IOException {
        // 報告書の保存先にファイルを保存します
        File report = new File(FilenameUtils.concat(AppConfig.get().getReportPath(), name));
        if ((report.getParentFile() == null) && report.mkdirs()) {
            // 報告書の保存先ディレクトリが無く、ディレクトリの作成に失敗した場合はカレントフォルダにファイルを保存
            report = new File(name);
        }
        if (ReportUtils.isLocked(report)) {
            // ロックされている場合は代替ファイルに書き込みます
            report = new File(FilenameUtils.concat(report.getParent(), altername));
        }
        else {
            File alt_report = new File(FilenameUtils.concat(report.getParent(), altername));
            if (alt_report.exists() && !ReportUtils.isLocked(alt_report) && (FileUtils.sizeOf(alt_report) > 0)) {
                mergeAltFile(report, alt_report);
            }
        }
        return report;
    }

    /**
     * alternativeファイルを本体にマージして削除します
     * 
     * @param report ファイル本体
     * @param alt_report alternativeファイル
     * @return
     * @throws IOException
     */
    private static void mergeAltFile(File report, File alt_report) throws IOException {
        // report が空ファイルの場合は、alt ファイルをリネームして終了
        if (!report.exists() || (FileUtils.sizeOf(report) <= 0)) {
            report.delete();
            alt_report.renameTo(report);
            return;
        }
        OutputStream report_stream = new BufferedOutputStream(new FileOutputStream(report, true));
        InputStream alt_stream = new BufferedInputStream(new FileInputStream(alt_report));
        try {
            List<String> lines = IOUtils.readLines(alt_stream, AppConstants.CHARSET);
            // タイトル行は削除
            lines.remove(0);
            IOUtils.writeLines(lines, "\r\n", report_stream, AppConstants.CHARSET);
        } finally {
            report_stream.close();
            alt_stream.close();
        }
        alt_report.delete();
    }
}