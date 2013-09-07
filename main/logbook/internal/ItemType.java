/**
 * 
 */
package logbook.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * アイテム種別
 *
 */
public class ItemType {

    /**
     * アイテム種別プリセット値
     */
    private static final Map<String, String> ITEMTYPE = new ConcurrentHashMap<String, String>() {
        {
            this.put("1", "小口径主砲");
            this.put("2", "中口径主砲");
            this.put("3", "大口径主砲");
            this.put("4", "副砲");
            this.put("5", "魚雷");
            this.put("6", "艦上戦闘機");
            this.put("7", "艦上爆撃機");
            this.put("8", "艦上攻撃機");
            this.put("9", "艦上偵察機");
            this.put("10", "水上偵察機");
            this.put("11", "電波探信儀");
            this.put("12", "対空砲弾");
            this.put("13", "徹甲弾");
            this.put("14", "ダメコン");
            this.put("15", "対空機関砲");
            this.put("16", "高角砲");
            this.put("17", "爆雷投射機");
            this.put("18", "水中探信儀");
            this.put("19", "機関");
        }
    };

    /**
     * アイテム種別を取得します
     * 
     * @param type ID
     * @return アイテム種別
     */
    public static String get(String type) {
        return ITEMTYPE.get(type);
    }
}
