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
import java.util.Set;
import java.util.TreeMap;

import logbook.constants.AppConstants;
import logbook.dto.BattleExDto;
import logbook.dto.ShipInfoDto;
import logbook.gui.ApplicationMain;

import org.apache.commons.lang3.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.dyuproject.protostuff.Tag;

/**
 * @author Nekopanda
 *
 */
public class EnemyData {

    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(EnemyData.class);
    private static Map<Integer, EnemyData> ENEMY = new TreeMap<Integer, EnemyData>();

    /** 変更があったか */
    private static boolean modified = false;

    // 始めてアクセスがあった時に読み込む
    public static final boolean INIT_COMPLETE;
    static {
        try {
            load();
        } catch (IOException e) {
            LOG.get().warn("e_idと敵艦隊の対応ファイル読み込みに失敗しました", e);
        }
        INIT_COMPLETE = true;
    }

    @Tag(1)
    private final int enemyId;
    @Tag(2)
    private final String enemyName;
    //@Tag(3)
    //private final String[] enemyShips;
    @Tag(4)
    private final String formation;
    @Tag(5)
    private final int[] enemyShipsId;

    @Override
    public boolean equals(Object o) {
        if (o instanceof EnemyData) {
            EnemyData e = (EnemyData) o;
            if ((e.enemyId == this.enemyId) &&
                    e.enemyName.equals(this.enemyName) &&
                    Arrays.equals(e.enemyShipsId, this.enemyShipsId) &&
                    e.formation.equals(this.formation)) {
                return true;
            }
        }
        return false;
    }

    public EnemyData(int enemyId, String enemyName, int[] enemyShipsId, String formation) {
        this.enemyId = enemyId;
        this.enemyName = enemyName;
        this.enemyShipsId = enemyShipsId;
        this.formation = formation;
    }

    /**
     * 敵編成を設定します
     */
    public static void set(int id, EnemyData item) {
        EnemyData old = ENEMY.get(id);
        if ((old != null) && old.equals(item)) {
            // 更新する必要なし
            return;
        }
        ENEMY.put(id, item);
        modified = true;
    }

    /**
     * 敵編成を取得します
     * 
     * @param id エネミーID
     * @return 敵編成
     */
    public static EnemyData get(int id) {
        return ENEMY.get(id);
    }

    /**
     * IDの一覧を取得します
     * 
     * @return IDの一覧
     */
    public static Set<Integer> keySet() {
        return ENEMY.keySet();
    }

    private static String[] getHeader() {
        return new String[] {
                "敵編成ID", "敵艦隊名", "陣形", "敵1番艦", "敵2番艦", "敵3番艦", "敵4番艦", "敵5番艦", "敵6番艦"
        };
    }

    public static void store() throws IOException {
        // 変更があったときだけ書き込む
        if (modified) {
            try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new BufferedOutputStream(
                    new FileOutputStream(AppConstants.ENEMY_DATA_FILE)), AppConstants.CHARSET),
                    CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)) {
                List<String> flatten = new ArrayList<String>();
                writer.writeNext(getHeader());
                for (Entry<Integer, EnemyData> e : ENEMY.entrySet()) {
                    EnemyData data = e.getValue();
                    flatten.add(String.valueOf(data.getEnemyId()));
                    flatten.add(data.getEnemyName());
                    flatten.add(String.valueOf(BattleExDto.fromFormation(data.getFormation())));
                    for (int s : data.getEnemyShipsId()) {
                        flatten.add(String.valueOf(s));
                    }
                    writer.writeNext(flatten.toArray(new String[flatten.size()]));
                    flatten.clear();
                }
            }
            ApplicationMain.sysPrint("Enemyファイル更新");
            modified = false;
        }
    }

    private static void readOldfile() {
        if (AppConstants.ENEMY_DATA_OLD_FILE.exists()) {
            try {
                Map<Integer, EnemyData> TMP = new TreeMap<Integer, EnemyData>();
                Map<String, ShipInfoDto> nameMap = Ship.getEnemyNameMap();
                try (CSVReader reader = new CSVReader(new InputStreamReader(
                        new FileInputStream(AppConstants.ENEMY_DATA_OLD_FILE), AppConstants.CHARSET))) {
                    for (String[] entry : reader.readAll()) {
                        if (entry.length >= 8) {
                            int id = Integer.parseInt(entry[0]);
                            int[] enemyShipsId = new int[6];
                            for (int i = 0; i < 6; ++i) {
                                String shipdata = entry[i + 1];
                                int shipId = -1;
                                if (!StringUtils.isEmpty(shipdata)) {
                                    ShipInfoDto shipinfo = nameMap.get(shipdata);
                                    if (shipinfo != null) {
                                        shipId = shipinfo.getShipId();
                                    }
                                }
                                enemyShipsId[i] = shipId;
                            }
                            String name = (entry.length >= 9) ? entry[8] : "";
                            TMP.put(id, new EnemyData(id, name, enemyShipsId, entry[7]));
                        }
                    }
                }

                for (EnemyData data : TMP.values()) {
                    if (!ENEMY.containsKey(data.enemyId)) {
                        ENEMY.put(data.enemyId, data);
                        modified = true;
                    }
                }
            } catch (IOException e) {
                LOG.get().warn("旧敵データファイル読み込み失敗", e);
            }
        }
    }

    public static void load() throws IOException {
        if (AppConstants.ENEMY_DATA_FILE.exists()) {
            try (CSVReader reader = new CSVReader(new InputStreamReader(
                    new FileInputStream(AppConstants.ENEMY_DATA_FILE), AppConstants.CHARSET))) {
                int line = 0;
                for (String[] entry : reader.readAll()) {
                    if (line++ == 0)
                        continue; // ヘッダーを飛ばす

                    if (entry.length >= 9) {
                        int id = Integer.parseInt(entry[0]);
                        String name = entry[1];
                        String formation = BattleExDto.toFormation(Integer.parseInt(entry[2]));
                        int[] enemyShipsId = new int[6];
                        for (int i = 0; i < 6; ++i) {
                            enemyShipsId[i] = Integer.parseInt(entry[i + 3]);
                        }
                        if (ENEMY.containsKey(id)) {
                            modified = true;
                        }
                        ENEMY.put(id, new EnemyData(id, name, enemyShipsId, formation));
                    }
                }
            }
        }
        readOldfile();
    }

    /**
     * @return enemyId
     */
    public int getEnemyId() {
        return this.enemyId;
    }

    public String getEnemyName() {
        return this.enemyName;
    }

    /**
     * @return enemyShips
     */
    public int[] getEnemyShipsId() {
        return this.enemyShipsId;
    }

    public String[] getEnemyShips() {
        String[] names = new String[6];
        for (int i = 0; i < 6; ++i) {
            String name = "";
            ShipInfoDto shipinfo = Ship.get(String.valueOf(this.enemyShipsId[i]));
            if (shipinfo != null) {
                name = shipinfo.getFullName();
            }
            names[i] = name;
        }
        return names;
    }

    /**
     * @return formation
     */
    public String getFormation() {
        return this.formation;
    }
}
