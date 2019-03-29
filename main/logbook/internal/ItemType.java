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
            this.put(21, "回転翼機");
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
            this.put(32, "水上艦要員");
            this.put(33, "大型飛行艇");
            this.put(34, "戦闘食料");
            this.put(35, "洋上補給");
            this.put(36, "特型内火艇");
            this.put(37, "陸上攻撃機");
            this.put(38, "局地戦闘機");
            this.put(39, "噴式戦闘爆撃機");
            this.put(40, "噴式戦闘爆撃機");
            this.put(41, "輸送機材");
            this.put(42, "潜水艦装備");
            this.put(43, "水上戦闘機");
            this.put(44, "陸軍戦闘機");
            this.put(45, "夜間戦闘機");
            this.put(46, "夜間攻撃機");
            this.put(47, "陸上攻撃機");
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
