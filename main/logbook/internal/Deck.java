package logbook.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 遠征
 *
 */
public final class Deck {

    /**
     * 遠征プリセット値
     */
    private static final Map<String, String> DECK = new ConcurrentHashMap<String, String>() {
        {
            this.put("1", "練習航海");
            this.put("2", "長距離練習航海");
            this.put("3", "警備任務");
            this.put("4", "対潜警戒任務");
            this.put("5", "海上護衛任務");
            this.put("6", "防空射撃演習");
            this.put("7", "観艦式予行");
            this.put("8", "観艦式");
            this.put("9", "タンカー護衛任務");
            this.put("10", "強行偵察任務");
            this.put("11", "ボーキサイト輸送任務");
            this.put("12", "資源輸送任務");
            this.put("13", "鼠輸送作戦");
            this.put("14", "包囲陸戦隊撤収作戦");
            this.put("15", "囮機動部隊支援作戦");
            this.put("16", "艦隊決戦援護作戦");
            this.put("17", "敵地偵察作戦");
            this.put("18", "航空機輸送作戦");
            this.put("19", "北号作戦");
            this.put("20", "潜水艦哨戒任務");
            this.put("21", "<UNKNOWN>");
            this.put("22", "<UNKNOWN>");
            this.put("23", "<UNKNOWN>");
            this.put("24", "<UNKNOWN>");
            this.put("25", "通商破壊作戦");
            this.put("26", "敵母港空襲作戦");
            this.put("27", "潜水艦通商破壊作戦");
            this.put("28", "<UNKNOWN>");
            this.put("29", "<UNKNOWN>");
            this.put("30", "<UNKNOWN>");
            this.put("31", "<UNKNOWN>");
            this.put("32", "<UNKNOWN>");
            this.put("33", "前衛支援任務");
            this.put("34", "艦隊決戦支援任務");
            this.put("35", "ＭＯ作戦");
            this.put("36", "水上機基地建設");
            this.put("37", "<UNKNOWN>");
            this.put("38", "<UNKNOWN>");
            this.put("39", "<UNKNOWN>");
            this.put("40", "<UNKNOWN>");
        }
    };

    /**
     * 遠征を取得します
     * 
     * @param id ID
     * @return 遠征
     */
    public static String get(String id) {
        return DECK.get(id);
    }
}
