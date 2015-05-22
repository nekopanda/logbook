package logbook.internal;

import java.util.Map;
import java.util.TreeMap;

import logbook.internal.MasterData.ShipTypeDto;

/**
 * 艦種
 * 
 */
public class ShipStyle {

    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(ShipStyle.class);

    /**
     * 艦種プリセット値
     */
    private static final Map<Integer, String> SHIPSTYLE = new TreeMap<Integer, String>() {
        {
            this.put(1, "海防艦");
            this.put(2, "駆逐艦");
            this.put(3, "軽巡洋艦");
            this.put(4, "重雷装巡洋艦");
            this.put(5, "重巡洋艦");
            this.put(6, "航空巡洋艦");
            this.put(7, "軽空母");
            this.put(8, "戦艦");
            this.put(9, "戦艦");
            this.put(10, "航空戦艦");
            this.put(11, "正規空母");
            this.put(12, "超弩級戦艦");
            this.put(13, "潜水艦");
            this.put(14, "潜水空母");
            this.put(15, "補給艦");
            this.put(16, "水上機母艦");
            this.put(17, "揚陸艦");
            this.put(18, "装甲空母");
            this.put(19, "工作艦");
            this.put(20, "潜水母艦");
            this.put(21, "練習巡洋艦");
        }
    };

    // 始めてアクセスがあった時に読み込む
    static {
        try {
            update();
        } catch (Exception e) {
            LOG.get().warn(e);
        }
    }

    /**
     * 艦種を取得します
     * 
     * @param id
     * @return 艦種
     */
    public static String get(int id) {
        return SHIPSTYLE.get(id);
    }

    public static Map<Integer, String> getMap() {
        return SHIPSTYLE;
    }

    /**
     * マスターデータから更新
     */
    public static void update() {
        for (ShipTypeDto entry : MasterData.get().getStart2().getStype()) {
            SHIPSTYLE.put(entry.getId(), entry.getName());
        }
    }
}
