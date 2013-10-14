/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.logic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import logbook.config.GlobalConfig;
import logbook.data.context.GlobalContext;
import logbook.dto.BattleDto;
import logbook.dto.BattleResultDto;
import logbook.dto.CreateItemDto;
import logbook.dto.DeckMissionDto;
import logbook.dto.DockDto;
import logbook.dto.GetShipDto;
import logbook.dto.ItemDto;
import logbook.dto.NdockDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
        public TableItem create(Table table, String[] text) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(text);
            return item;
        }
    };

    /** テーブルアイテム作成(所有艦娘一覧) */
    public static final TableItemCreator SHIP_LIST_TABLE_ITEM_CREATOR = new TableItemCreator() {
        @Override
        public TableItem create(Table table, String[] text) {
            // 艦娘
            Long ship = Long.valueOf(text[0]);

            TableItem item = new TableItem(table, SWT.NONE);
            // 疲労
            int cond = Integer.parseInt(text[2]);
            if (cond <= 15) {
                item.setForeground(SWTResourceManager.getColor(GlobalConfig.COND_RED_COLOR));
            } else if (cond <= 29) {
                item.setForeground(SWTResourceManager.getColor(GlobalConfig.COND_ORANGE_COLOR));
            }

            // 遠征
            Set<Long> deckmissions = new HashSet<Long>();
            for (DeckMissionDto deckMission : GlobalContext.getDeckMissions()) {
                if ((deckMission.getMission() != null) && (deckMission.getShips() != null)) {
                    deckmissions.addAll(deckMission.getShips());
                }
            }

            // 入渠
            Set<Long> docks = new HashSet<Long>();
            for (NdockDto ndock : GlobalContext.getNdocks()) {
                if (ndock.getNdockid() != 0) {
                    docks.add(ndock.getNdockid());
                }
            }

            if (deckmissions.contains(ship)) {
                item.setForeground(SWTResourceManager.getColor(GlobalConfig.MISSION_COLOR));
            }
            if (docks.contains(ship)) {
                item.setForeground(SWTResourceManager.getColor(GlobalConfig.NDOCK_COLOR));
            }

            item.setText(text);
            return item;
        }
    };

    /** 日付フォーマット */
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat(GlobalConfig.DATE_FORMAT);

    /**
     * ドロップ報告書のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getBattleResultHeader() {
        return new String[] { "", "日付", "海域", "ランク", "敵艦隊", "ドロップ艦種", "ドロップ艦娘" };
    }

    /**
     * ドロップ報告書の内容
     * 
     * @return 内容
     */
    public static List<String[]> getBattleResultBody() {
        List<BattleResultDto> results = GlobalContext.getBattleResultList();
        List<Object[]> body = new ArrayList<Object[]>();

        for (int i = 0; i < results.size(); i++) {
            BattleResultDto item = results.get(i);
            body.add(new Object[] { Integer.toString(i + 1), FORMAT.format(item.getBattleDate()), item.getQuestName(),
                    item.getRank(), item.getEnemyName(), item.getDropType(), item.getDropName() });
        }
        return toListStringArray(body);
    }

    /**
     * ドロップ報告書のヘッダー(保存用)
     * 
     * @return ヘッダー
     */
    public static String[] getBattleResultStoreHeader() {
        return new String[] { "", "日付", "海域", "ランク", "敵艦隊", "ドロップ艦種", "ドロップ艦娘",
                "見方艦1", "見方艦1HP",
                "見方艦2", "見方艦2HP",
                "見方艦3", "見方艦3HP",
                "見方艦4", "見方艦4HP",
                "見方艦5", "見方艦5HP",
                "見方艦6", "見方艦6HP",
                "敵艦1", "敵艦1HP",
                "敵艦2", "敵艦2HP",
                "敵艦3", "敵艦3HP",
                "敵艦4", "敵艦4HP",
                "敵艦5", "敵艦5HP",
                "敵艦6", "敵艦6HP" };
    }

    /**
     * ドロップ報告書の内容(保存用)
     * 
     * @return 内容
     */
    public static List<String[]> getBattleResultStoreBody() {
        List<BattleResultDto> results = GlobalContext.getBattleResultList();
        List<Object[]> body = new ArrayList<Object[]>();

        for (int i = 0; i < results.size(); i++) {
            BattleResultDto item = results.get(i);
            BattleDto battle = item.getBattleDto();
            String[] friend = new String[6];
            String[] friendHp = new String[6];
            String[] enemy = new String[6];
            String[] enemyHp = new String[6];

            Arrays.fill(friend, "");
            Arrays.fill(friendHp, "");
            Arrays.fill(enemy, "");
            Arrays.fill(enemyHp, "");

            if (battle != null) {
                DockDto dock = battle.getDock();
                if (dock != null) {
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
            }

            body.add(new Object[] { Integer.toString(i + 1), FORMAT.format(item.getBattleDate()), item.getQuestName(),
                    item.getRank(), item.getEnemyName(), item.getDropType(), item.getDropName(), friend[0],
                    friendHp[0], friend[1], friendHp[1], friend[2], friendHp[2], friend[3], friendHp[3], friend[4],
                    friendHp[4], friend[5], friendHp[5], enemy[0], enemyHp[0], enemy[1], enemyHp[1], enemy[2],
                    enemyHp[2], enemy[3], enemyHp[3], enemy[4], enemyHp[4], enemy[5], enemyHp[5] });
        }
        return toListStringArray(body);
    }

    /**
     * 建造報告書のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getCreateShipHeader() {
        return new String[] { "", "日付", "名前", "艦種", "燃料", "弾薬", "鋼材", "ボーキ", "秘書艦", "司令部Lv" };
    }

    /**
     * 建造報告書の内容
     * 
     * @return 内容
     */
    public static List<String[]> getCreateShipBody() {
        List<GetShipDto> ships = GlobalContext.getGetshipList();
        List<Object[]> body = new ArrayList<Object[]>();
        for (int i = 0; i < ships.size(); i++) {
            GetShipDto ship = ships.get(i);
            body.add(new Object[] { Integer.toString(i + 1), FORMAT.format(ship.getGetDate()), ship.getName(),
                    ship.getType(), ship.getFuel(), ship.getAmmo(), ship.getMetal(), ship.getBauxite(),
                    ship.getSecretary(), ship.getHqLevel() });
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
    public static List<String[]> getCreateItemBody() {
        List<CreateItemDto> items = GlobalContext.getCreateItemList();
        List<Object[]> body = new ArrayList<Object[]>();

        for (int i = 0; i < items.size(); i++) {
            CreateItemDto item = items.get(i);
            String name = "失敗";
            String type = "";
            if (item.isCreateFlag()) {
                name = item.getName();
                type = item.getType();
            }
            body.add(new Object[] { Integer.toString(i + 1), FORMAT.format(item.getCreateDate()), name, type,
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
        return new String[] { "名称", "種別", "個数", "火力", "命中", "回避", "射程", "運", "爆装", "雷装", "索敵", "対潜", "対空" };
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

        for (Entry<ItemDto, Integer> entry : countitems) {
            ItemDto item = entry.getKey();
            Integer count = entry.getValue();
            body.add(new Object[] { item.getName(), item.getType(), count.toString(), item.getHoug(), item.getHoum(),
                    item.getKaih(), item.getLeng(), item.getLuck(), item.getBaku(), item.getRaig(), item.getSaku(),
                    item.getTais(), item.getTyku()
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
        return new String[] { "艦娘ID", "艦隊", "疲労", "名前", "艦種", "Lv", "経験値", "HP", "装備1", "装備2", "装備3", "装備4",
                "火力", "雷装", "対空", "装甲", "回避", "対潜", "索敵", "運" };
    }

    /**
     * 所有艦娘一覧の内容
     * 
     * @return 内容
     */
    public static List<String[]> getShipListBody() {
        Set<Entry<Long, ShipDto>> ships = GlobalContext.getShipMap().entrySet();
        List<Object[]> body = new ArrayList<Object[]>();
        for (Entry<Long, ShipDto> entry : ships) {
            ShipDto ship = entry.getValue();
            body.add(new Object[] { ship.getId(), ship.getFleetid(), ship.getCond(), ship.getName(), ship.getType(),
                    ship.getLv(), ship.getExp(), ship.getMaxhp(), ship.getSlot().get(0), ship.getSlot().get(1),
                    ship.getSlot().get(2), ship.getSlot().get(3), ship.getKaryoku(), ship.getRaisou(),
                    ship.getTaiku(), ship.getSoukou(), ship.getKaihi(), ship.getTaisen(), ship.getSakuteki(),
                    ship.getLucky() });
        }
        return toListStringArray(body);
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
                IOUtils.write(StringUtils.join(header, ',') + "\r\n", stream, GlobalConfig.CHARSET);
            }
            for (String[] colums : body) {
                IOUtils.write(StringUtils.join(colums, ',') + "\r\n", stream, GlobalConfig.CHARSET);
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
}
