/**
 * 
 */
package logbook.internal;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import logbook.constants.AppConstants;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.dyuproject.protostuff.Tag;

/**
 * @author Nekopanda
 *
 */
public class EnemyData {

    private static Map<Integer, EnemyData> ENEMY = new TreeMap<Integer, EnemyData>();

    @Tag(1)
    private final int enemyId;
    @Tag(2)
    private final String enemyName;
    @Tag(3)
    private final String[] enemyShips;
    @Tag(4)
    private final String formation;

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
        ENEMY.put(id, item);
    }

    /**
     * 敵編成を取得します
     * 
     * @param type ID
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
        CSVWriter writer = new CSVWriter(new OutputStreamWriter(
                new FileOutputStream(AppConstants.ENEMY_DATA_FILE), "MS932"));
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
        writer.close();
    }

    public static void load() throws IOException {
        if (AppConstants.ENEMY_DATA_FILE.exists()) {
            CSVReader reader = new CSVReader(new InputStreamReader(
                    new FileInputStream(AppConstants.ENEMY_DATA_FILE), "MS932"));
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
