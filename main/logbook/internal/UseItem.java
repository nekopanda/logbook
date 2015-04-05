package logbook.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * アイテム
 *
 */
public class UseItem {

    /**
     * アイテムプリセット値
     */
    private static final Map<Integer, String> USE_ITEM = new ConcurrentHashMap<Integer, String>() {
        {
            this.put(10, "家具箱（小）");
            this.put(11, "家具箱（中）");
            this.put(12, "家具箱（大）");
            this.put(50, "応急修理要員");
            this.put(51, "応急修理女神");
            this.put(54, "給糧艦「間宮」");
            this.put(56, "艦娘からのチョコ");
            this.put(57, "勲章");
            this.put(59, "給糧艦「伊良湖」");
            this.put(62, "菱餅");
        }
    };

    /**
     * アイテムを取得します
     *
     * @param type ID
     * @return アイテム
     */
    public static String get(int type) {
        return USE_ITEM.get(type);
    }
}
