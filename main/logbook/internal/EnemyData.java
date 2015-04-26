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
import logbook.gui.ApplicationMain;

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
    @Tag(3)
    private final String[] enemyShips;
    @Tag(4)
    private final String formation;

    @Override
    public boolean equals(Object o) {
        if (o instanceof EnemyData) {
            EnemyData e = (EnemyData) o;
            if ((e.enemyId == this.enemyId) &&
                    e.enemyName.equals(this.enemyName) &&
                    Arrays.equals(e.enemyShips, this.enemyShips) &&
                    e.formation.equals(this.formation)) {
                return true;
            }
        }
        return false;
    }

    public EnemyData(int enemyId, String enemyName, String[] enemyShips, String formation) {
        this.enemyId = enemyId;
        this.enemyName = enemyName;
        this.enemyShips = enemyShips;
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
                    new FileOutputStream(AppConstants.ENEMY_DATA_FILE)), AppConstants.CHARSET))) {
                List<String> flatten = new ArrayList<String>();
                for (Entry<Integer, EnemyData> e : ENEMY.entrySet()) {
                    EnemyData data = e.getValue();
                    flatten.add(String.valueOf(data.getEnemyId()));
                    for (String s : data.getEnemyShips()) {
                        flatten.add(s);
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
            CSVReader reader = new CSVReader(new InputStreamReader(
                    new FileInputStream(AppConstants.ENEMY_DATA_FILE), AppConstants.CHARSET));
            for (String[] entry : reader.readAll()) {
                if (entry.length >= 8) {
                    String[] enemyShips = new String[6];
                    System.arraycopy(entry, 1, enemyShips, 0, 6);
                    int id = Integer.parseInt(entry[0]);
                    String name = (entry.length >= 9) ? entry[8] : null;
                    ENEMY.put(id, new EnemyData(id, name, enemyShips, entry[7]));
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
    public String[] getEnemyShips() {
        return this.enemyShips;
    }

    /**
     * @return formation
     */
    public String getFormation() {
        return this.formation;
    }
}
