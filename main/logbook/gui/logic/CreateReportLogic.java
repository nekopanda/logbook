package logbook.gui.logic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
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
import logbook.dto.ShipDto;
import logbook.dto.ShipFilterDto;
import logbook.dto.ShipInfoDto;

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
        public TableItem create(Table table, String[] text, int count) {
            TableItem item = new TableItem(table, SWT.NONE);
            // 偶数行に背景色を付ける
            if ((count % 2) != 0) {
                item.setBackground(SWTResourceManager.getColor(AppConstants.ROW_BACKGROUND));
            }
            item.setText(text);
            return item;
        }
    };

    /** テーブルアイテム作成(所有艦娘一覧) */
    public static final TableItemCreator SHIP_LIST_TABLE_ITEM_CREATOR = new TableItemCreator() {

        private Set<Long> deckmissions;

        private Set<Long> docks;

        @Override
        public void init() {
            // 遠征
            this.deckmissions = new HashSet<Long>();
            for (DeckMissionDto deckMission : GlobalContext.getDeckMissions()) {
                if ((deckMission.getMission() != null) && (deckMission.getShips() != null)) {
                    this.deckmissions.addAll(deckMission.getShips());
                }
            }
            // 入渠
            this.docks = new HashSet<Long>();
            for (NdockDto ndock : GlobalContext.getNdocks()) {
                if (ndock.getNdockid() != 0) {
                    this.docks.add(ndock.getNdockid());
                }
            }
        }

        @Override
        public TableItem create(Table table, String[] text, int count) {
            // 艦娘
            Long ship = Long.valueOf(text[1]);

            TableItem item = new TableItem(table, SWT.NONE);
            // 偶数行に背景色を付ける
            if ((count % 2) != 0) {
                item.setBackground(SWTResourceManager.getColor(AppConstants.ROW_BACKGROUND));
            }

            // 疲労
            int cond = Integer.parseInt(text[5]);
            if (cond <= AppConstants.COND_RED) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
            } else if (cond <= AppConstants.COND_ORANGE) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
            } else if ((cond >= AppConstants.COND_DARK_GREEN) && (cond < AppConstants.COND_GREEN)) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.COND_DARK_GREEN_COLOR));
            } else if (cond >= AppConstants.COND_GREEN) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.COND_GREEN_COLOR));
            }

            // 遠征
            if (this.deckmissions.contains(ship)) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.MISSION_COLOR));
            }
            // 入渠
            if (this.docks.contains(ship)) {
                item.setForeground(SWTResourceManager.getColor(AppConstants.NDOCK_COLOR));
            }
            // Lv1の艦娘をグレー色にする
            if ("1".equals(text[7])) {
                item.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
            }

            item.setText(text);
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
        return new String[] { "", "日付", "海域", "マス", "ボス", "ランク",
                "艦隊行動", "味方陣形", "敵陣形", "敵艦隊", "ドロップ艦種", "ドロップ艦娘" };
    }

    /**
     * ドロップ報告書の内容
     * @return 内容
     */
    public static List<String[]> getBattleResultBody() {
        List<BattleResultDto> results = GlobalContext.getBattleResultList();
        List<Object[]> body = new ArrayList<Object[]>();

        SimpleDateFormat format = new SimpleDateFormat(AppConstants.DATE_FORMAT);

        for (int i = 0; i < results.size(); i++) {
            BattleResultDto item = results.get(i);
            BattleDto battle = item.getBattle();

            body.add(new Object[] {
                    Integer.toString(i + 1),
                    format.format(item.getBattleDate()),
                    item.getQuestName(),
                    item.getMapCellNo(),
                    item.getBossText(),
                    item.getRank(),
                    battle.getIntercept(),
                    battle.getFriendFormation(),
                    battle.getEnemyFormation(),
                    item.getEnemyName(),
                    item.getDropType(),
                    item.getDropName() });
        }
        return toListStringArray(body);
    }

    /**
     * ドロップ報告書のヘッダー(保存用)
     * 
     * @return ヘッダー
     */
    public static String[] getBattleResultStoreHeader() {
        return new String[] { "", "日付", "海域", "マス", "ボス", "ランク",
                "艦隊行動", "味方陣形", "敵陣形",
                "敵艦隊",
                "ドロップ艦種", "ドロップ艦娘",
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
    public static List<String[]> getBattleResultStoreBody(List<BattleResultDto> results) {
        List<Object[]> body = new ArrayList<Object[]>();

        SimpleDateFormat format = new SimpleDateFormat(AppConstants.DATE_FORMAT);

        for (int i = 0; i < results.size(); i++) {
            BattleResultDto item = results.get(i);
            BattleDto battle = item.getBattleDto();
            if (battle == null) {
                continue;
            }
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
                List<ShipInfoDto> enemyships = battle.getEnemy();
                int[] enowhps = battle.getNowEnemyHp();
                int[] emaxhps = battle.getMaxEnemyHp();
                for (int j = 0; j < enemyships.size(); j++) {
                    ShipInfoDto ship = enemyships.get(j);
                    if (!StringUtils.isEmpty(ship.getFlagship())) {
                        enemy[j] = ship.getName() + "(" + ship.getFlagship() + ")";
                    } else {
                        enemy[j] = ship.getName();
                    }
                    enemyHp[j] = enowhps[j] + "/" + emaxhps[j];
                }
            }

            body.add(new Object[] {
                    Integer.toString(i + 1),
                    format.format(item.getBattleDate()),
                    item.getQuestName(),
                    item.getMapCellNo(),
                    item.getBossText(),
                    item.getRank(),
                    battle.getIntercept(),
                    battle.getFriendFormation(),
                    battle.getEnemyFormation(),
                    item.getEnemyName(),
                    item.getDropType(),
                    item.getDropName(),
                    friend[0], friendHp[0],
                    friend[1], friendHp[1],
                    friend[2], friendHp[2],
                    friend[3], friendHp[3],
                    friend[4], friendHp[4],
                    friend[5], friendHp[5],
                    enemy[0], enemyHp[0],
                    enemy[1], enemyHp[1],
                    enemy[2], enemyHp[2],
                    enemy[3], enemyHp[3],
                    enemy[4], enemyHp[4],
                    enemy[5], enemyHp[5] });
        }
        return toListStringArray(body);
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
    public static List<String[]> getCreateShipBody(List<GetShipDto> ships) {
        List<Object[]> body = new ArrayList<Object[]>();
        for (int i = 0; i < ships.size(); i++) {
            GetShipDto ship = ships.get(i);
            body.add(new Object[] { Integer.toString(i + 1),
                    new SimpleDateFormat(AppConstants.DATE_FORMAT).format(ship.getGetDate()), ship.getBuildType(),
                    ship.getName(), ship.getType(), ship.getFuel(), ship.getAmmo(), ship.getMetal(), ship.getBauxite(),
                    ship.getResearchMaterials(), ship.getFreeDock(), ship.getSecretary(), ship.getHqLevel() });
        }
        return toListStringArray(body);
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
    public static List<String[]> getCreateItemBody(List<CreateItemDto> items) {
        List<Object[]> body = new ArrayList<Object[]>();

        for (int i = 0; i < items.size(); i++) {
            CreateItemDto item = items.get(i);
            String name = "失敗";
            String type = "";
            if (item.isCreateFlag()) {
                name = item.getName();
                type = item.getType();
            }
            body.add(new Object[] { Integer.toString(i + 1),
                    new SimpleDateFormat(AppConstants.DATE_FORMAT).format(item.getCreateDate()), name, type,
                    item.getFuel(), item.getAmmo(), item.getMetal(), item.getBauxite(), item.getSecretary(),
                    item.getHqLevel() });
        }
        return toListStringArray(body);
    }

    /**
     * 所有装備一覧のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getItemListHeader() {
        return new String[] { "", "名称", "種別", "個数", "火力", "命中", "射程", "運", "回避", "爆装", "雷装", "索敵", "対潜", "対空", "装甲" };
    }

    /**
     * 所有装備一覧の内容
     * 
     * @return 内容
     */
    public static List<String[]> getItemListBody() {
        Set<Entry<Long, ItemDto>> items = GlobalContext.getItemMap().entrySet();
        Map<ItemDto, Integer> itemCountMap = new HashMap<ItemDto, Integer>();

        for (Entry<Long, ItemDto> entry : items) {
            ItemDto item = entry.getValue();
            Integer count = itemCountMap.get(item);
            if (count == null) {
                count = 1;
            } else {
                count = count + 1;
            }
            itemCountMap.put(item, count);
        }

        List<Entry<ItemDto, Integer>> countitems = new ArrayList<Entry<ItemDto, Integer>>(itemCountMap.entrySet());
        Collections.sort(countitems, new Comparator<Entry<ItemDto, Integer>>() {
            @Override
            public int compare(Entry<ItemDto, Integer> o1, Entry<ItemDto, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        List<Object[]> body = new ArrayList<Object[]>();

        int count = 0;
        for (Entry<ItemDto, Integer> entry : countitems) {
            ItemDto item = entry.getKey();
            count++;
            body.add(new Object[] { count, item.getName(), item.getType(), entry.getValue(), item.getHoug(),
                    item.getHoum(), item.getLeng(), item.getLuck(), item.getHouk(), item.getBaku(), item.getRaig(),
                    item.getSaku(), item.getTais(), item.getTyku(), item.getSouk()
            });
        }
        return toListStringArray(body);
    }

    /**
     * 所有艦娘一覧のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getShipListHeader() {
        return new String[] { "", "ID", "艦隊", "名前", "艦種", "疲労", "回復", "Lv", "Next", "経験値", "制空", "装備1", "装備2",
                "装備3", "装備4", "HP", "火力", "雷装", "対空", "装甲", "回避", "対潜", "索敵", "運",
                "装備命中", "砲撃戦火力", "雷撃戦火力", "対潜火力", "夜戦火力" };
    }

    /**
     * 所有艦娘一覧の内容
     * 
     * @param specdiff 成長余地
     * @param filter 鍵付きのみ
     * @return 内容
     */
    public static List<String[]> getShipListBody(boolean specdiff, ShipFilterDto filter) {
        Set<Entry<Long, ShipDto>> ships = GlobalContext.getShipMap().entrySet();
        List<Object[]> body = new ArrayList<Object[]>();
        int count = 0;
        for (Entry<Long, ShipDto> entry : ships) {
            ShipDto ship = entry.getValue();

            if ((filter != null) && !shipFilter(ship, filter)) {
                continue;
            }

            count++;

            if (!specdiff) {
                // 通常
                body.add(new Object[] {
                        count,
                        ship.getId(),
                        ship.getFleetid(),
                        ship.getName(),
                        ship.getType(),
                        ship.getCond(),
                        ship.getCondClearDate(),
                        ship.getLv(),
                        ship.getNext(),
                        ship.getExp(),
                        ship.getSeiku(),
                        ship.getSlot().get(0),
                        ship.getSlot().get(1),
                        ship.getSlot().get(2),
                        ship.getSlot().get(3),
                        ship.getMaxhp(),
                        ship.getKaryoku(),
                        ship.getRaisou(),
                        ship.getTaiku(),
                        ship.getSoukou(),
                        ship.getKaihi(),
                        ship.getTaisen(),
                        ship.getSakuteki(),
                        ship.getLucky(),
                        ship.getAccuracy(),
                        ship.getHougekiPower(),
                        ship.getRaigekiPower(),
                        ship.getTaisenPower(),
                        ship.getYasenPower()
                });
            } else {
                // 成長の余地
                // 火力
                long karyoku = ship.getKaryokuMax() - ship.getKaryoku();
                // 雷装
                long raisou = ship.getRaisouMax() - ship.getRaisou();
                // 対空
                long taiku = ship.getTaikuMax() - ship.getTaiku();
                // 装甲
                long soukou = ship.getSoukouMax() - ship.getSoukou();
                // 回避
                long kaihi = ship.getKaihiMax() - ship.getKaihi();
                // 対潜
                long taisen = ship.getTaisenMax() - ship.getTaisen();
                // 索敵
                long sakuteki = ship.getSakutekiMax() - ship.getSakuteki();
                // 運
                long lucky = ship.getLuckyMax() - ship.getLucky();

                for (ItemDto item : ship.getItem()) {
                    if (item != null) {
                        karyoku += item.getHoug();
                        raisou += item.getRaig();
                        taiku += item.getTyku();
                        soukou += item.getSouk();
                        kaihi += item.getHouk();
                        taisen += item.getTais();
                        sakuteki += item.getSaku();
                        lucky += item.getLuck();
                    }
                }
                body.add(new Object[] {
                        count,
                        ship.getId(),
                        ship.getFleetid(),
                        ship.getName(),
                        ship.getType(),
                        ship.getCond(),
                        ship.getCondClearDate(),
                        ship.getLv(),
                        ship.getNext(),
                        ship.getExp(),
                        ship.getSeiku(),
                        ship.getSlot().get(0),
                        ship.getSlot().get(1),
                        ship.getSlot().get(2),
                        ship.getSlot().get(3),
                        ship.getMaxhp(),
                        karyoku,
                        raisou,
                        taiku,
                        soukou,
                        kaihi,
                        taisen,
                        sakuteki,
                        lucky,
                        ship.getAccuracy(),
                        ship.getHougekiPower(),
                        ship.getRaigekiPower(),
                        ship.getTaisenPower(),
                        ship.getYasenPower()
                });
            }
        }
        return toListStringArray(body);
    }

    /**
     * 遠征結果のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getCreateMissionResultHeader() {
        return new String[] { "", "日付", "結果", "遠征", "燃料", "弾薬", "鋼材", "ボーキ" };
    }

    /**
     * 遠征結果一覧の内容
     * 
     * @return 遠征結果
     */
    public static List<String[]> getMissionResultBody(List<MissionResultDto> resultlist) {
        List<Object[]> body = new ArrayList<Object[]>();

        for (int i = 0; i < resultlist.size(); i++) {
            MissionResultDto result = resultlist.get(i);

            body.add(new Object[] {
                    Integer.toString(i + 1),
                    new SimpleDateFormat(AppConstants.DATE_FORMAT).format(result.getDate()),
                    result.getClearResult(),
                    result.getQuestName(),
                    result.getFuel(),
                    result.getAmmo(),
                    result.getMetal(),
                    result.getBauxite()
            });
        }
        return toListStringArray(body);
    }

    /**
     * 任務一覧のヘッダー
     * 
     * @return
     */
    public static String[] getCreateQuestHeader() {
        return new String[] { "", "状態", "タイトル", "内容", "燃料", "弾薬", "鋼材", "ボーキ" };
    }

    /**
     * 任務一覧の内容
     * 
     * @return
     */
    public static List<String[]> getQuestBody() {
        List<Object[]> body = new ArrayList<Object[]>();

        for (Entry<Integer, QuestDto> entry : GlobalContext.getQuest().entrySet()) {
            QuestDto quest = entry.getValue();

            String state = "";
            switch (quest.getState()) {
            case 2:
                state = "遂行中";
                break;
            case 3:
                state = "達成";
                break;
            default:
                continue;
            }

            body.add(new Object[] {
                    quest.getNo(),
                    state,
                    quest.getTitle(),
                    quest.getDetail(),
                    quest.getFuel(),
                    quest.getAmmo(),
                    quest.getMetal(),
                    quest.getBauxite()
            });
        }
        return toListStringArray(body);
    }

    /**
     * 資材のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getMaterialHeader() {
        return new String[] { "", "日付", "燃料", "弾薬", "鋼材", "ボーキ", "高速修復材", "高速建造材", "開発資材" };
    }

    /**
     * 資材の内容
     * 
     * @param materials 資材
     * @return
     */
    public static List<String[]> getMaterialStoreBody(List<MaterialDto> materials) {
        List<String[]> body = new ArrayList<String[]>();

        for (int i = 0; i < materials.size(); i++) {
            MaterialDto material = materials.get(i);
            body.add(new String[] {
                    Integer.toString(i + 1),
                    new SimpleDateFormat(AppConstants.DATE_FORMAT).format(material.getTime()),
                    Integer.toString(material.getFuel()),
                    Integer.toString(material.getAmmo()),
                    Integer.toString(material.getMetal()),
                    Integer.toString(material.getBauxite()),
                    Integer.toString(material.getBucket()),
                    Integer.toString(material.getBurner()),
                    Integer.toString(material.getResearch())
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
    public static void writeCsvStripFirstColumn(File file, String[] header, List<String[]> body, boolean applend)
            throws IOException {
        // 報告書の項番を除く
        String[] copyheader = Arrays.copyOfRange(header, 1, header.length);
        List<String[]> copybody = new ArrayList<String[]>();
        for (String[] strings : body) {
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
    public static void writeCsv(File file, String[] header, List<String[]> body, boolean applend)
            throws IOException {
        OutputStream stream = new BufferedOutputStream(new FileOutputStream(file, applend));
        try {
            if (!file.exists() || (FileUtils.sizeOf(file) <= 0)) {
                IOUtils.write(StringUtils.join(header, ',') + "\r\n", stream, AppConstants.CHARSET);
            }
            for (String[] colums : body) {
                IOUtils.write(StringUtils.join(colums, ',') + "\r\n", stream, AppConstants.CHARSET);
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
    private static List<String[]> toListStringArray(List<Object[]> from) {
        List<String[]> body = new ArrayList<String[]>();
        for (Object[] objects : from) {
            String[] values = new String[objects.length];
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] != null) {
                    values[i] = String.valueOf(objects[i]);
                } else {
                    values[i] = "";
                }
            }
            body.add(values);
        }
        return body;
    }

    /**
     * 艦娘をフィルタします
     * 
     * @param ship 艦娘
     * @param filter フィルターオブジェクト
     * @return フィルタ結果
     */
    private static boolean shipFilter(ShipDto ship, ShipFilterDto filter) {
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
            List<ItemDto> item = ship.getItem();

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
                    for (ItemDto itemDto : item) {
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
                    for (ItemDto itemDto : item) {
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
        if (!filter.trainingShip) {
            if ("練習巡洋艦".equals(ship.getType())) {
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
    public static void storeBattleResultReport(BattleResultDto dto) {
        try {
            List<BattleResultDto> dtoList = Collections.singletonList(dto);

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
     * 遠征報告書を書き込む
     * 
     * @param dto 遠征結果
     */
    public static void storeCreateMissionReport(MissionResultDto dto) {
        try {
            List<MissionResultDto> dtoList = Collections.singletonList(dto);

            File report = getStoreFile(AppConstants.LOG_MISSION, AppConstants.LOG_MISSION_ALT);

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
            List<MaterialDto> dtoList = Collections.singletonList(material);

            File report = getStoreFile(AppConstants.LOG_RESOURCE, AppConstants.LOG_RESOURCE_ALT);

            CreateReportLogic.writeCsvStripFirstColumn(report,
                    CreateReportLogic.getMaterialHeader(),
                    CreateReportLogic.getMaterialStoreBody(dtoList), true);
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
        if (isLocked(report)) {
            // ロックされている場合は代替ファイルに書き込みます
            report = new File(FilenameUtils.concat(report.getParent(), altername));
        }
        return report;
    }

    /**
     * ファイルがロックされているかを確認します
     * 
     * @param file ファイル
     * @return
     * @throws IOException
     */
    private static boolean isLocked(File file) throws IOException {
        if (!file.isFile()) {
            return false;
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            try {
                FileChannel channel = raf.getChannel();
                FileLock lock = channel.tryLock();
                if (lock == null) {
                    return true;
                }
                lock.release();
                return false;
            } finally {
                raf.close();
            }
        } catch (FileNotFoundException e) {
            return true;
        }
    }
}
