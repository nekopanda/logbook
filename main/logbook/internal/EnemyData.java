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
import logbook.dto.ShipInfoDto;
import logbook.gui.ApplicationMain;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.dyuproject.protostuff.Tag;

/**
 * @author Nekopanda
 *
 */
public class EnemyData {

    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(EnemyData.class);
    private static Map<Integer, EnemyData> ENEMY = new TreeMap<Integer, EnemyData>();

    /** 変更があったか */
    private static boolean modified = false;

    // 始めてアクセスがあった時に読み込む
    public static final boolean INIT_COMPLETE;
    static {
        try {
            load();
        } catch (IOException e) {
            LOG.warn("e_idと敵艦隊の対応ファイル読み込みに失敗しました", e);
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

    public static void store() throws IOException {
        // 変更があったときだけ書き込む
        if (modified) {
            try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new BufferedOutputStream(
                    new FileOutputStream(AppConstants.ENEMY_DATA_FILE)), AppConstants.CHARSET),
                    CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)) {
                List<String> flatten = new ArrayList<String>();
                for (Entry<Integer, EnemyData> e : ENEMY.entrySet()) {
                    EnemyData data = e.getValue();
                    flatten.add(String.valueOf(data.getEnemyId()));
                    for (int s : data.getEnemyShipsId()) {
                        flatten.add(String.valueOf(s));
                    }
                    flatten.add(data.getFormation());
                    if (data.getEnemyName() != null) {
                        flatten.add(data.getEnemyName());
                    }
                    writer.writeNext(flatten.toArray(new String[flatten.size()]));
                    flatten.clear();
                }
            }
            ApplicationMain.sysPrint("Enemyファイル更新");
            modified = false;
        }
    }

    public static void load() throws IOException {
        if (AppConstants.ENEMY_DATA_FILE.exists()) {

            Map<String, ShipInfoDto> nameMap = Ship.getEnemyNameMap();

            CSVReader reader = new CSVReader(new InputStreamReader(
                    new FileInputStream(AppConstants.ENEMY_DATA_FILE), AppConstants.CHARSET));
            for (String[] entry : reader.readAll()) {
                if (entry.length >= 8) {
                    int id = Integer.parseInt(entry[0]);
                    int[] enemyShipsId = new int[6];
                    for (int i = 0; i < 6; ++i) {
                        String shipdata = entry[i + 1];
                        int shipId = -1;
                        if (!StringUtils.isEmpty(shipdata)) {
                            if (NumberUtils.isNumber(shipdata)) {
                                if (shipId > 0) {
                                    shipId = Integer.parseInt(shipdata);
                                }
                            }
                            else {
                                ShipInfoDto shipinfo = nameMap.get(shipdata);
                                if (shipinfo != null) {
                                    shipId = shipinfo.getShipId();
                                }
                            }
                        }
                        enemyShipsId[i] = shipId;
                    }
                    String name = (entry.length >= 9) ? entry[8] : null;
                    ENEMY.put(id, new EnemyData(id, name, enemyShipsId, entry[7]));
                }
            }
            reader.close();
        }
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
