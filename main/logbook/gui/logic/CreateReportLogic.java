package logbook.gui.logic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.BattleExDto;
import logbook.dto.BattleResultDto;
import logbook.dto.CreateItemDto;
import logbook.dto.DockDto;
import logbook.dto.EnemyShipDto;
import logbook.dto.GetShipDto;
import logbook.dto.ItemDto;
import logbook.dto.ItemInfoDto;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import au.com.bytecode.opencsv.CSVReader;

/**
 * 各種報告書を作成します
 *
 */
public final class CreateReportLogic {

    public static Color getTableCondColor(int cond) {
        for (int i = 0; i < AppConstants.COND_TABLE_LOCOR.length; ++i) {
            if (cond >= AppConstants.COND_TABLE[i]) {
                return SWTResourceManager.getColor(AppConstants.COND_TABLE_LOCOR[i]);
            }
        }
        // 0より小さいってあり得ないけど
        return SWTResourceManager.getColor(AppConstants.COND_RED_COLOR);
    }

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
            /*
            int cond = ship.getCond();
            if (cond <= AppConstants.COND_RED) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
            } else if (cond <= AppConstants.COND_ORANGE) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
            } else if ((cond >= AppConstants.COND_DARK_GREEN) && (cond < AppConstants.COND_GREEN)) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.COND_DARK_GREEN_COLOR));
            } else if (cond >= AppConstants.COND_GREEN) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.COND_GREEN_COLOR));
            }
            */
            // 疲労は 12 番目
            item.setBackground(12, getTableCondColor(ship.getCond()));

            // 遠征
            if (this.deckmissions.contains(ship.getId())) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.MISSION_COLOR));
            }
            // 入渠
            if (this.docks.contains(ship.getId())) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.NDOCK_COLOR));
            }
            // Lv1の艦娘をグレー色にする
            /* ちょっとこれをやる意味がよく分からないのでコメントアウト
            if (ship.getLv() == 1) {
                item.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
            }
            */

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
        return new String[] { "No.", "日付", "海域", "マス", "出撃", "ランク", "敵艦隊", "ドロップ艦種", "ドロップ艦娘",
                "大破艦", "旗艦", "旗艦(第二艦隊)", "MVP", "MVP(第二艦隊)" };
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
                    new DateTimeString(item.getBattleDate()),
                    item.getQuestName(),
                    (item.getMapCell() != null) ? item.getMapCell().getReportString() : null,
                    item.getBossText(),
                    item.getRank(),
                    item.getEnemyName(),
                    item.getDropType(),
                    item.getScreenDropName(),
                    item.isHasTaiha() ? "あり" : "",
                    item.getFlagShip(),
                    item.getFlagShipCombined(),
                    item.getMvp(),
                    item.getMvpCombined() });
        }
        return body;
    }

    /**
     * ドロップ報告書のヘッダー(保存用)
     * 
     * @return ヘッダー
     */
    public static String[] getBattleResultStoreHeader() {
        return new String[] { "No.", "日付", "海域", "マス", "ランク", "敵艦隊", "ドロップ艦種", "ドロップ艦娘",
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

        SimpleDateFormat format = new SimpleDateFormat(AppConstants.DATE_FORMAT);

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

            String dropName = battle.getDropName();
            if (StringUtils.isEmpty(dropName) &&
                    (battle.getExVersion() >= 1) &&
                    (battle.getShipSpace() == 0)) {
                dropName = "※空きなし";
            }

            body.add(new Comparable[] { Integer.toString(i + 1),
                    new DateTimeString(battle.getBattleDate()),
                    battle.getQuestName(),
                    battle.getMapCellDto().toString(), battle.getRank(), battle.getEnemyName(), battle.getDropType(),
                    dropName,
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
        return new String[] { "No.", "日付", "種類", "名前", "艦種", "燃料", "弾薬", "鋼材", "ボーキ", "開発資材", "空きドック", "秘書艦", "司令部Lv" };
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
            body.add(new Comparable[] { i + 1,
                    new DateTimeString(ship.getGetDate()), ship.getBuildType(),
                    ship.getName(), ship.getType(), ship.getFuel(), ship.getAmmo(), ship.getMetal(), ship.getBauxite(),
                    ship.getResearchMaterials(), ship.getFreeDock(), ship.getSecretary(), ship.getHqLevel() });
        }
        return body;
    }

    public static List<GetShipDto> getCreateShip(List<String[]> resultlist) {
        List<GetShipDto> dtolist = new ArrayList<GetShipDto>();
        int[] numericFields = new int[] { 4, 5, 6, 7, 8, 9, 11 };
        for (int i = 0; i < resultlist.size(); ++i) {
            String[] record = resultlist.get(i);
            if (record.length >= 12) {
                Date date = readDate(record[0]);
                if (date != null) {
                    GetShipDto dto;
                    ResourceItemDto res = new ResourceItemDto();
                    if (isAllNumeric(record, numericFields)) {
                        res.loadBaseMaterialsFromString(record, 4);
                        res.setResearchMaterials(Integer.valueOf(record[8]));
                        dto = new GetShipDto(date, record[1], record[2], record[3],
                                res, record[10], Integer.valueOf(record[11]), Integer.valueOf(record[9]));
                    }
                    else { // 不完全なデータだが一応読みこんでおく
                        dto = new GetShipDto(date, record[1], record[2], record[3], res, record[10], 0, 0);
                    }
                    dtolist.add(dto);
                }
            }
        }
        return dtolist;
    }

    /**
     * 開発報告書のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getCreateItemHeader() {
        return new String[] { "No.", "日付", "開発装備", "種別", "燃料", "弾薬", "鋼材", "ボーキ", "秘書艦", "司令部Lv" };
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
            body.add(new Comparable[] { i + 1,
                    new DateTimeString(item.getCreateDate()), name, type,
                    item.getFuel(), item.getAmmo(), item.getMetal(), item.getBauxite(), item.getSecretary(),
                    item.getHqLevel() });
        }
        return body;
    }

    public static List<CreateItemDto> getCreateItem(List<String[]> resultlist) {
        List<CreateItemDto> dtolist = new ArrayList<CreateItemDto>();
        int[] numericFields = new int[] { 3, 4, 5, 6, 8 };
        for (int i = 0; i < resultlist.size(); ++i) {
            String[] record = resultlist.get(i);
            if (record.length >= 9) {
                Date date = readDate(record[0]);
                if (date != null) {
                    CreateItemDto dto;
                    ResourceItemDto res = new ResourceItemDto();
                    boolean createFlag = !record[1].equals("失敗");
                    if (isAllNumeric(record, numericFields)) {
                        res.loadBaseMaterialsFromString(record, 3);
                        dto = new CreateItemDto(date, createFlag, record[1], record[2], res,
                                record[7], Integer.valueOf(record[8]));
                    }
                    else { // 不完全なデータだが一応読みこんでおく
                        dto = new CreateItemDto(date, createFlag, record[1], record[2], res, record[7], 0);
                    }
                    dtolist.add(dto);
                }
            }
        }
        return dtolist;
    }

    /**
     * 所有装備一覧のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getItemListHeader() {
        return new String[] { "No.", "名称", "種別", "個数", "施錠", "改修",
                "火力", "命中", "射程", "運", "回避", "爆装", "雷装", "索敵", "対潜", "対空", "装甲",
                "装備してる艦娘" };
    }

    private static class ItemInfo {
        public ItemInfoDto item;
        public int count = 0;
        public int locked = 0;
        public int maxLevel = 0;
        public Map<ShipDto, Integer> shipMap = new TreeMap<>();

        public ItemInfo(ItemInfoDto item) {
            this.item = item;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            int count = 0;
            // 持っている個数で個数ソート
            List<Entry<ShipDto, Integer>> sorted = new ArrayList<>(this.shipMap.entrySet());
            Collections.sort(sorted, new Comparator<Entry<ShipDto, Integer>>() {
                @Override
                public int compare(Entry<ShipDto, Integer> o1, Entry<ShipDto, Integer> o2) {
                    return -Integer.compare(o1.getValue(), o2.getValue());
                }
            });
            for (Entry<ShipDto, Integer> entry : sorted) {
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
        Map<Integer, ItemInfo> itemCountMap = new HashMap<Integer, ItemInfo>();

        // 装備の個数を計算
        for (ItemDto item : GlobalContext.getItemMap().values()) {
            ItemInfo info = itemCountMap.get(item.getSlotitemId());
            if (info == null) {
                info = new ItemInfo(item.getInfo());
                itemCountMap.put(item.getSlotitemId(), info);
            }
            info.count++;
            if (item.isLocked()) {
                info.locked++;
            }
            info.maxLevel = Math.max(info.maxLevel, item.getLevel());
        }

        // 艦娘が持っている個数を計算
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            for (ItemInfoDto item : ship.getItem()) {
                if (item != null) {
                    ItemInfo info = itemCountMap.get(item.getId());
                    if (info != null) {
                        Integer count = info.shipMap.get(ship);
                        if (count == null) {
                            count = 1;
                        }
                        else {
                            count++;
                        }
                        info.shipMap.put(ship, count);
                    }
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
            ItemInfoDto item = itemInfo.item;
            ShipParameters param = item.getParam();
            String level = (itemInfo.maxLevel != 0) ? "★+" + itemInfo.maxLevel : null;
            count++;
            body.add(new Comparable[] { count, item.getName(), item.getTypeName(), itemInfo.count,
                    itemInfo.locked, level, param.getHoug(),
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
                "No.",
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
                "修理燃料",//
                "修理鋼材",//
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
                "運",
                "装備命中",
                "砲撃戦火力",
                "雷撃戦火力",
                "対潜火力",
                "夜戦火力"
        };
    }

    public static boolean[] getBathTableDefaultVisibles() {
        Map<String, Integer> colMap = AppConstants.BATHTABLE_COLUMN_MAP;
        String[] header = getShipListHeader();
        boolean[] visibles = new boolean[header.length];
        for (int i = 0; i < visibles.length; ++i) {
            visibles[i] = colMap.containsKey(header[i]);
        }
        return visibles;
    }

    public static int[] getBathTableDefaultColumnOrder() {
        Map<String, Integer> colMap = AppConstants.BATHTABLE_COLUMN_MAP;
        String[] header = getShipListHeader();
        int[] order = new int[header.length];
        int hiddenColIndex = colMap.size();
        for (int i = 0; i < order.length; ++i) {
            if (colMap.containsKey(header[i])) {
                order[colMap.get(header[i])] = i;
            }
            else {
                order[hiddenColIndex++] = i;
            }
        }
        if (hiddenColIndex != header.length) {
            LOG.warn("BATHTABLE_COLUMN_MAPに実際にはないカラムがあるようです");
        }
        return order;
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

            if ((filter != null) && !shipFilter(ship, filter, missionSet)) {
                continue;
            }

            ShipInfoDto shipInfo = Ship.get(String.valueOf(ship.getShipId()));

            count++;

            String fleet = null;
            if (ship.isFleetMember()) {
                fleet = String.valueOf(ship.getFleetid()) + "-" + String.valueOf(ship.getFleetpos() + 1);
            }

            String now = null;
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
            List<ItemDto> slotItems = ship.getItem2();
            String[] slotNames = new String[4];
            HpString[] onSlotString = new HpString[4];
            int[] onSlot = ship.getOnSlot();
            int[] maxEq = shipInfo.getMaxeq();
            int slotNum = ship.getSlotNum();
            for (int i = 0; i < slotNum; ++i) {
                ItemDto item = slotItems.get(i);
                if (ship.canEquipPlane()) { // 飛行機を装備できる場合だけ
                    int cur = ((item != null) && item.isPlane()) ? onSlot[i] : 0;
                    int max = maxEq != null ? maxEq[i] : 0;
                    onSlotString[i] = new HpString(cur, max);
                }
                if (item != null) {
                    slotNames[i] = item.getFriendlyName();
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
                    slotNames[0],
                    onSlotString[0],
                    slotNames[1],
                    onSlotString[1],
                    slotNames[2],
                    onSlotString[2],
                    slotNames[3],
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
                    param.getLucky(),
                    ship.getAccuracy(),
                    ship.getHougekiPower(),
                    ship.getRaigekiPower(),
                    ship.getTaisenPower(),
                    ship.getYasenPower()
            });
        }
        return body;
    }

    private static IntegerPair getPageNumber(int index) {
        return new IntegerPair((index / 10) + 1, (index % 10) + 1, "-");
    }

    public static class ShipWithSortNumber {
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

    public static List<ShipWithSortNumber> getShipWithSortNumber() {
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
    public static String[] getMissionResultHeader() {
        return new String[] { "No.", "日付", "結果", "遠征", "燃料", "弾薬", "鋼材", "ボーキ", "アイテム1", "個数", "アイテム2", "個数" };
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
            ResourceItemDto resItems = result.getResources();
            if (resItems != null) {
                Map<Integer, UseItemDto> items = resItems.getItems();
                int index = 0;
                for (UseItemDto item : items.values()) {
                    itemName[index] = item.getItemName();
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

    public static List<MissionResultDto> getMissionResult(List<String[]> resultlist) {
        List<MissionResultDto> dtolist = new ArrayList<MissionResultDto>();

        // アイテム名->IDへのマッピングを作成
        Map<String, Integer> itemNameMap = new HashMap<>();
        Map<Integer, MasterData.UseItemInfoDto> itemIdMap = MasterData.getInstance().getUseItem();
        for (MasterData.UseItemInfoDto item : itemIdMap.values()) {
            itemNameMap.put(item.getName(), item.getId());
        }

        for (String[] record : resultlist) {
            if (record.length >= 11) {
                Date date = readDate(record[0]);
                if (date != null) {
                    ResourceItemDto res = new ResourceItemDto();
                    if (record[3].length() > 0) { // 失敗の時はない
                        res.loadBaseMaterialsFromString(record, 3);
                        // アイテムを復元
                        for (int i = 0; i < 2; ++i) {
                            String name = record[(i * 2) + 7];
                            String count = record[(i * 2) + 8];
                            if (name.length() > 0) {
                                Integer id = itemNameMap.get(name);
                                if (id == null) {
                                    id = AppConstants.USEITEM_UNKNOWN;
                                }
                                res.setItem(id, Integer.valueOf(count));
                            }
                        }
                    }
                    MissionResultDto dto = new MissionResultDto(date, record[1], record[2], res);
                    dtolist.add(dto);
                }
            }
        }
        return dtolist;
    }

    /**
     * 任務一覧のヘッダー
     * 
     * @return
     */
    public static String[] getCreateQuestHeader() {
        return new String[] { "No.", "表示位置", "状態", "進捗", "タイトル", "内容", "燃料", "弾薬", "鋼材", "ボーキ" };
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

            body.add(new Comparable[] {
                    quest.getNo(),
                    "" + quest.getPage() + "-" + quest.getPos(),
                    quest.getStateString(),
                    quest.getProgressString(),
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
        return new String[] { "No.", "日付", "直前のイベント", "燃料", "弾薬", "鋼材", "ボーキ", "高速修復材", "高速建造材", "開発資材", "改修資材" };
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
                    material.getBucket(), // 間違えてバーナーとバケツを逆にしちゃったけど仕方ない・・・
                    material.getBurner(),
                    material.getResearch(),
                    material.getScrew()
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
        return new String[] { "No.", "日付", "種別", "個別ID", "名前", "原因" };
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
                IOUtils.write(StringUtils.join(toStringArray(colums), ',') + "\r\n", stream, AppConstants.CHARSET);
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
    private static boolean shipFilter(ShipDto ship, ShipFilterDto filter, Set<Integer> missionSet) {
        // テキストでフィルタ
        if (!StringUtils.isEmpty(filter.nametext)) {
            // 検索ワード
            String[] words = StringUtils.split(filter.nametext, " ");
            // 検索対象
            // 名前
            String name = ship.getName();
            // 艦種
            String type = ship.getType();
            // 装備
            List<ItemInfoDto> item = ship.getItem();

            // テキストが入力されている場合処理する
            if (filter.regexp) {
                // 正規表現で検索
                for (int i = 0; i < words.length; i++) {
                    Pattern pattern;
                    try {
                        pattern = Pattern.compile(words[i]);
                    } catch (PatternSyntaxException e) {
                        // 無効な正規表現はfalseを返す
                        return false;
                    }
                    boolean find = false;

                    // 名前で検索
                    find = find ? find : pattern.matcher(name).find();
                    // 艦種で検索
                    find = find ? find : pattern.matcher(type).find();
                    // 装備で検索
                    for (ItemInfoDto itemDto : item) {
                        if ((itemDto == null) || (itemDto.getName() == null)) {
                            find = find ? find : false;
                        } else {
                            find = find ? find : pattern.matcher(itemDto.getName()).find();
                        }
                    }

                    if (!find) {
                        // どれにもマッチしない場合
                        return false;
                    }
                }
            } else {
                // 部分一致で検索する
                for (int i = 0; i < words.length; i++) {
                    boolean find = false;

                    // 名前で検索
                    find = find ? find : name.indexOf(words[i]) != -1;
                    // 艦種で検索
                    find = find ? find : type.indexOf(words[i]) != -1;
                    // 装備で検索
                    for (ItemInfoDto itemDto : item) {
                        if ((itemDto == null) || (itemDto.getName() == null)) {
                            find = find ? find : false;
                        } else {
                            find = find ? find : itemDto.getName().indexOf(words[i]) != -1;
                        }
                    }

                    if (!find) {
                        // どれにもマッチしない場合
                        return false;
                    }
                }
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
        if (!filter.mission || !filter.notmission) {
            boolean isMission = missionSet.contains(ship.getId());
            // 遠征中
            if (!filter.mission && isMission) {
                return false;
            }
            // 遠征中ではない
            if (!filter.notmission && !isMission) {
                return false;
            }
        }
        if (!filter.needbath || !filter.notneedbath) {
            boolean needBath = (ship.getDocktime() > 0) && !GlobalContext.isNdock(ship.getId());
            // 要修理
            if (!filter.needbath && needBath) {
                return false;
            }
            // 修理の必要なし
            if (!filter.notneedbath && !needBath) {
                return false;
            }
        }

        if (filter.groupMode == false) {
            // 艦種でフィルタ
            if ((filter.enabledType != null) &&
                    (filter.enabledType[ship.getStype()] == false))
            {
                return false;
            }
        }
        else {
            // グループでフィルタ
            if (filter.group != null) {
                if (!filter.group.getShips().contains(ship.getId())) {
                    return false;
                }
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

            File report = getStoreFile(AppConstants.LOG_BATTLE_RESULT, AppConstants.LOG_BATTLE_RESULT_ALT);

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

            File report = getStoreFile(AppConstants.LOG_CREATE_SHIP, AppConstants.LOG_CREATE_SHIP_ALT);

            CreateReportLogic.writeCsvStripFirstColumn(report,
                    CreateReportLogic.getCreateShipHeader(),
                    CreateReportLogic.getCreateShipBody(dtoList), true);
        } catch (IOException e) {
            LOG.warn("報告書の保存に失敗しました", e);
        }
    }

    /**
     * 建造報告書を読み込む
     * 
     * @return 建造報告
     */
    public static List<GetShipDto> loadCreateShipReport() {
        List<GetShipDto> dtoList = null;
        try {
            File file = new File(FilenameUtils.concat(AppConfig.get().getReportPath(), AppConstants.LOG_CREATE_SHIP));
            if (file.exists()) {
                CSVReader reader = new CSVReader(new InputStreamReader(
                        new FileInputStream(file), AppConstants.CHARSET));
                dtoList = getCreateShip(reader.readAll());
                reader.close();
            }
        } catch (Exception e) {
            LOG.warn("建造報告書の読み込みに失敗しました", e);
        }
        return dtoList;
    }

    /**
     * 開発報告書を書き込む
     * 
     * @param dto 開発報告
     */
    public static void storeCreateItemReport(CreateItemDto dto) {
        try {
            List<CreateItemDto> dtoList = Collections.singletonList(dto);

            File report = getStoreFile(AppConstants.LOG_CREATE_ITEM, AppConstants.LOG_CREATE_ITEM_ALT);

            CreateReportLogic.writeCsvStripFirstColumn(report,
                    CreateReportLogic.getCreateItemHeader(),
                    CreateReportLogic.getCreateItemBody(dtoList), true);
        } catch (IOException e) {
            LOG.warn("報告書の保存に失敗しました", e);
        }
    }

    /**
     * 開発報告書を読み込む
     * 
     * @return 開発報告
     */
    public static List<CreateItemDto> loadCreateItemReport() {
        List<CreateItemDto> dtoList = null;
        try {
            File file = new File(FilenameUtils.concat(AppConfig.get().getReportPath(), AppConstants.LOG_CREATE_ITEM));
            if (file.exists()) {
                CSVReader reader = new CSVReader(new InputStreamReader(
                        new FileInputStream(file), AppConstants.CHARSET));
                dtoList = getCreateItem(reader.readAll());
                reader.close();
            }
        } catch (Exception e) {
            LOG.warn("開発報告書の読み込みに失敗しました", e);
        }
        return dtoList;
    }

    /**
     * 遠征報告書を書き込む
     * 
     * @param dto 遠征結果
     */
    public static void storeMissionReport(MissionResultDto dto) {
        try {
            List<MissionResultDto> dtoList = Collections.singletonList(dto);

            File report = getStoreFile(AppConstants.LOG_MISSION, AppConstants.LOG_MISSION_ALT);

            CreateReportLogic.writeCsvStripFirstColumn(report,
                    CreateReportLogic.getMissionResultHeader(),
                    CreateReportLogic.getMissionResultBody(dtoList), true);
        } catch (IOException e) {
            LOG.warn("報告書の保存に失敗しました", e);
        }
    }

    /**
     * 遠征報告書を読み込む
     * 
     * @return 遠征報告
     */
    public static List<MissionResultDto> loadMissionReport() {
        List<MissionResultDto> dtoList = null;
        try {
            File file = new File(FilenameUtils.concat(AppConfig.get().getReportPath(), AppConstants.LOG_MISSION));
            if (file.exists()) {
                CSVReader reader = new CSVReader(new InputStreamReader(
                        new FileInputStream(file), AppConstants.CHARSET));
                dtoList = getMissionResult(reader.readAll());
                reader.close();
            }
        } catch (Exception e) {
            LOG.warn("遠征報告書の読み込みに失敗しました", e);
        }
        return dtoList;
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

                File report = getStoreFile(AppConstants.LOG_RESOURCE, AppConstants.LOG_RESOURCE_ALT);

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
        File dir = report.getParentFile();
        if ((dir == null) || !(dir.exists() || dir.mkdirs())) {
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

    private static SimpleDateFormat[] dateFormats = new SimpleDateFormat[] {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), // オリジナルの記録フォーマット
            new SimpleDateFormat("yyyy/MM/dd HH:mm") // Excelで保存した時のフォーマット
    };

    public static Date readDate(String str) {
        ParsePosition pos = new ParsePosition(0);
        Date date = null;
        for (DateFormat format : dateFormats) {
            date = format.parse(str, pos);
            if (date != null) {
                break;
            }
        }
        return date;
    }

    /** 数値に変換できるかチェック
     *  */
    private static boolean isAllNumeric(String[] record, int[] indeces) {
        for (int idx : indeces) {
            if (StringUtils.isNumeric(record[idx]) == false)
                return false;
        }
        return true;
    }
}