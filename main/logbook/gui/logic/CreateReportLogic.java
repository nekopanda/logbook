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
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import logbook.config.GlobalConfig;
import logbook.data.context.GlobalContext;
import logbook.dto.BattleResultDto;
import logbook.dto.CreateItemDto;
import logbook.dto.GetShipDto;
import logbook.dto.ItemDto;
import logbook.dto.ShipDto;

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
                item.setForeground(SWTResourceManager.getColor(255, 16, 0));
            } else if (cond <= 29) {
                item.setForeground(SWTResourceManager.getColor(255, 140, 0));
            }

            // 遠征
            Set<Long> deckmissions = new HashSet<Long>();
            if ((GlobalContext.getDeck1Mission() != null) && (GlobalContext.getDeck1Mission().getMission() != null)) {
                deckmissions.addAll(GlobalContext.getDeck1Mission().getShips());
            }
            if ((GlobalContext.getDeck2Mission() != null) && (GlobalContext.getDeck2Mission().getMission() != null)) {
                deckmissions.addAll(GlobalContext.getDeck2Mission().getShips());
            }
            if ((GlobalContext.getDeck3Mission() != null) && (GlobalContext.getDeck3Mission().getMission() != null)) {
                deckmissions.addAll(GlobalContext.getDeck3Mission().getShips());
            }
            if (deckmissions.contains(ship)) {
                item.setForeground(SWTResourceManager.getColor(102, 51, 255));
            }

            // 入渠
            Set<Long> docks = new HashSet<Long>();
            docks.add(GlobalContext.getNdock1id());
            docks.add(GlobalContext.getNdock2id());
            docks.add(GlobalContext.getNdock3id());
            docks.add(GlobalContext.getNdock4id());
            if (docks.contains(ship)) {
                item.setForeground(SWTResourceManager.getColor(0, 102, 153));
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
        return new String[] { "日付", "海域", "ランク", "敵艦隊", "ドロップ艦種", "ドロップ艦娘" };
    }

    /**
     * ドロップ報告書の内容
     * 
     * @return 内容
     */
    public static List<String[]> getBattleResultBody() {
        List<BattleResultDto> results = GlobalContext.getBattleResultList();
        List<Object[]> body = new ArrayList<Object[]>();

        for (BattleResultDto item : results) {
            body.add(new Object[] { FORMAT.format(item.getBattleDate()), item.getQuestName(),
                    item.getRank(), item.getEnemyName(), item.getDropType(), item.getDropName() });
        }
        return toListStringArray(body);
    }

    /**
     * 建造報告書のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getCreateShipHeader() {
        return new String[] { "日付", "名前", "艦種", "燃料", "弾薬", "鋼材", "ボーキ", "秘書艦" };
    }

    /**
     * 建造報告書の内容
     * 
     * @return 内容
     */
    public static List<String[]> getCreateShipBody() {
        List<GetShipDto> ships = GlobalContext.getGetshipList();
        List<Object[]> body = new ArrayList<Object[]>();
        for (GetShipDto ship : ships) {
            body.add(new Object[] { FORMAT.format(ship.getGetDate()), ship.getName(), ship.getType(),
                    ship.getFuel(), ship.getAmmo(), ship.getMetal(),
                    ship.getBauxite(), ship.getSecretary() });
        }
        return toListStringArray(body);
    }

    /**
     * 開発報告書のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getCreateItemHeader() {
        return new String[] { "日付", "開発装備", "種別", "燃料", "弾薬", "鋼材", "ボーキ", "秘書艦" };
    }

    /**
     * 開発報告書の内容
     * 
     * @return 内容
     */
    public static List<String[]> getCreateItemBody() {
        List<CreateItemDto> items = GlobalContext.getCreateItemList();
        List<Object[]> body = new ArrayList<Object[]>();

        for (CreateItemDto item : items) {
            String name = "失敗";
            String type = "";
            if (item.isCreateFlag()) {
                name = item.getName();
                type = item.getType();
            }
            body.add(new Object[] { FORMAT.format(item.getCreateDate()), name, type, item.getFuel(),
                    item.getAmmo(), item.getMetal(), item.getBauxite(), item.getSecretary() });
        }
        return toListStringArray(body);
    }

    /**
     * 所有装備一覧のヘッダー
     * 
     * @return ヘッダー
     */
    public static String[] getItemListHeader() {
        return new String[] { "名称", "種別", "火力", "命中", "回避", "射程", "運", "爆装", "雷装", "索敵", "対潜", "対空" };
    }

    /**
     * 所有装備一覧の内容
     * 
     * @return 内容
     */
    public static List<String[]> getItemListBody() {
        Set<Entry<Long, ItemDto>> items = GlobalContext.getItemMap().entrySet();
        List<Object[]> body = new ArrayList<Object[]>();
        for (Entry<Long, ItemDto> entry : items) {
            ItemDto item = entry.getValue();
            body.add(new Object[] { item.getName(), item.getType(), item.getHoug(), item.getHoum(),
                    item.getKaih(), item.getLeng(), item.getLuck(), item.getBaku(), item.getRaig(),
                    item.getSaku(), item.getTais(), item.getTyku()
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
