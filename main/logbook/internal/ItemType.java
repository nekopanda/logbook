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
    private static final Map<Integer, String> ITEMTYPE = new ConcurrentHashMap<Integer, String>() {
        {
            this.put(1, "小口径主砲");
            this.put(2, "中口径主砲");
            this.put(3, "大口径主砲");
            this.put(4, "副砲");
            this.put(5, "魚雷");
            this.put(6, "艦上戦闘機");
            this.put(7, "艦上爆撃機");
            this.put(8, "艦上攻撃機");
            this.put(9, "艦上偵察機");
            this.put(10, "水上偵察機");
            this.put(11, "電波探信儀");
            this.put(12, "対空強化弾");
            this.put(13, "徹甲弾");
            this.put(14, "ダメコン");
            this.put(15, "機銃");
            this.put(16, "高角砲");
            this.put(17, "爆雷投射機");
            this.put(18, "ソナー");
            this.put(19, "機関部強化");
            this.put(20, "上陸用舟艇");
            this.put(21, "オートジャイロ");
            this.put(22, "指揮連絡機");
            this.put(23, "追加装甲");
            this.put(24, "探照灯");
            this.put(25, "簡易輸送部材");
            this.put(26, "艦艇修理施設");
            this.put(27, "照明弾");
            this.put(28, "司令部施設");
            this.put(29, "航空要員");
            this.put(30, "高射装置");
            this.put(31, "対地装備");
        }
    };

    /**
     * アイテム種別を取得します
     * 
     * @param type ID
     * @return アイテム種別
     */
    public static String get(Integer type) {
        return ITEMTYPE.get(type);
    }
}
