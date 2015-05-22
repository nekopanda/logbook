/**
 * 
 */
package logbook.internal;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.ItemInfoDto;
import logbook.dto.ShipBaseDto;
import logbook.dto.ShipDto;
import logbook.gui.ApplicationMain;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * 七四式互換ShipParameterRecord
 * ただし航海日誌拡張版が取得＆記録できるデータは敵艦、ドロップ艦の初期装備のみ
 * 
 * @author Nekopanda
 */
public class ShipParameterRecord {

    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(ShipParameterRecord.class);
    private static Map<Integer, ShipParameterRecord> SHIP = new TreeMap<Integer, ShipParameterRecord>();

    /** 変更があったか */
    private static boolean modified = false;

    // 始めてアクセスがあった時に読み込む
    public static final boolean INIT_COMPLETE;
    static {
        try {
            load();
        } catch (IOException e) {
            LOG.get().warn("艦パラメータファイル読み込みに失敗しました", e);
        }
        INIT_COMPLETE = true;
    }

    public static class UpdateShipParameter {

        private int maxShipId = -1;

        public void sortieStart() {
            this.maxShipId = 0;
            for (ShipDto ship : GlobalContext.getShipMap().values()) {
                if (this.maxShipId < ship.getId()) {
                    this.maxShipId = ship.getId();
                }
            }
        }

        public void sortieEnd() {
            if (this.maxShipId != -1) {
                for (ShipDto ship : GlobalContext.getShipMap().values()) {
                    if (this.maxShipId < ship.getId()) {
                        // 新しい艦娘
                        ShipParameterRecord.update(ship);
                    }
                }
            }
            this.maxShipId = -1;
        }

        public void battleStart() {
            for (ShipBaseDto ship : GlobalContext.getLastBattleDto().getEnemy()) {
                ShipParameterRecord.update(ship);
            }
        }
    }

    private final int shipId;
    private final String shipName;
    private final int[] ASW;
    private final int[] Evasion;
    private final int[] LOS;
    private int[] defaultSlot;
    private String albumMessage;

    private static int[] readIntegerArray(String[] data, int offset, int length) {
        int[] ret = new int[length];
        for (int i = 0; i < length; ++i) {
            if (data[offset + i].equals("null")) {
                return null;
            }
            ret[i] = Integer.parseInt(data[offset + i]);
        }
        return ret;
    }

    private static void addToString(List<String> data, int[] intarray) {
        for (int n : intarray) {
            data.add(String.valueOf(n));
        }
    }

    public ShipParameterRecord(String[] data) {
        this.shipId = Integer.parseInt(data[0]);
        this.shipName = data[1];
        this.ASW = readIntegerArray(data, 2, 3);
        this.Evasion = readIntegerArray(data, 5, 3);
        this.LOS = readIntegerArray(data, 8, 3);
        this.defaultSlot = readIntegerArray(data, 11, 5);
        this.albumMessage = data[16];
    }

    public ShipParameterRecord(int shipId, String shipName) {
        this.shipId = shipId;
        this.shipName = shipName;
        this.ASW = new int[] { 0, 9999, 9999 };
        this.Evasion = new int[] { 0, 9999, 9999 };
        this.LOS = new int[] { 0, 9999, 9999 };
    }

    /** 装備を更新 */
    public static void update(ShipBaseDto ship) {
        ShipParameterRecord record = SHIP.get(ship.getShipId());
        if (record == null) {
            record = new ShipParameterRecord(ship.getShipId(), ship.getFullName());
        }
        int[] itemId = ship.getItemId();
        if (ship.isFriend()) {
            // 艦娘の場合は個別IDになっているので変換
            List<ItemInfoDto> items = ship.getItem();
            itemId = new int[itemId.length];
            for (int i = 0; i < itemId.length; ++i) {
                itemId[i] = ((i < items.size()) && (items.get(i) != null)) ? items.get(i).getId() : -1;
            }
        }
        if (Arrays.equals(record.defaultSlot, itemId) == false) {
            record.defaultSlot = itemId;
            SHIP.put(ship.getShipId(), record);
            modified = true;
            ApplicationMain.main.printMessage(ship.getFullName() + "の装備データを更新");
        }
    }

    public static ShipParameterRecord get(int id) {
        return SHIP.get(id);
    }

    private static String[] getHeader() {
        return new String[] {
                "艦船ID", "艦船名", "対潜初期下限", "対潜初期上限", "対潜最大",
                "回避初期下限", "回避初期上限", "回避最大", "索敵初期下限", "索敵初期上限", "索敵最大",
                "装備1", "装備2", "装備3", "装備4", "装備5", "図鑑説明"
        };
    }

    public static void store() throws IOException {
        // 変更があったときだけ書き込む
        if (modified) {
            try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new BufferedOutputStream(
                    new FileOutputStream(AppConstants.SHIP_PARAMETER_FILE)), AppConstants.CHARSET),
                    CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)) {
                List<String> flatten = new ArrayList<String>();
                writer.writeNext(getHeader());
                for (Entry<Integer, ShipParameterRecord> e : SHIP.entrySet()) {
                    ShipParameterRecord data = e.getValue();
                    flatten.add(String.valueOf(data.shipId));
                    flatten.add(data.shipName);
                    addToString(flatten, data.ASW);
                    addToString(flatten, data.Evasion);
                    addToString(flatten, data.LOS);
                    if (data.defaultSlot != null) {
                        addToString(flatten, data.defaultSlot);
                    }
                    else {
                        flatten.addAll(Arrays.asList(new String[] { "null", "null", "null", "null", "null" }));
                    }
                    flatten.add(data.albumMessage);
                    writer.writeNext(flatten.toArray(new String[0]));
                    flatten.clear();
                }
            }
            ApplicationMain.sysPrint("艦パラメータファイル更新");
            modified = false;
        }
    }

    public static void load() throws IOException {
        if (AppConstants.SHIP_PARAMETER_FILE.exists()) {
            CSVReader reader = new CSVReader(new InputStreamReader(
                    new FileInputStream(AppConstants.SHIP_PARAMETER_FILE), AppConstants.CHARSET));
            int line = 0;
            for (String[] entry : reader.readAll()) {
                if (line++ == 0)
                    continue; // ヘッダーを飛ばす

                if (entry.length >= 17) {
                    ShipParameterRecord record = new ShipParameterRecord(entry);
                    if (SHIP.containsKey(record.shipId)) {
                        modified = true;
                    }
                    SHIP.put(record.shipId, record);
                }
            }
            reader.close();
        }
    }

    /**
     * @return shipId
     */
    public int getShipId() {
        return this.shipId;
    }

    /**
     * @return defaultSlot
     */
    public int[] getDefaultSlot() {
        return this.defaultSlot;
    }
}
