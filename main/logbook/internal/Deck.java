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
    private static final Map<Integer, String> DECK = new ConcurrentHashMap<Integer, String>() {
        {
            this.put(1, "練習航海");
            this.put(2, "長距離練習航海");
            this.put(3, "警備任務");
            this.put(4, "対潜警戒任務");
            this.put(5, "海上護衛任務");
            this.put(6, "防空射撃演習");
            this.put(7, "観艦式予行");
            this.put(8, "観艦式");
            this.put(9, "タンカー護衛任務");
            this.put(10, "強行偵察任務");
            this.put(11, "ボーキサイト輸送任務");
            this.put(12, "資源輸送任務");
            this.put(13, "鼠輸送作戦");
            this.put(14, "包囲陸戦隊撤収作戦");
            this.put(15, "囮機動部隊支援作戦");
            this.put(16, "艦隊決戦援護作戦");
            this.put(17, "敵地偵察作戦");
            this.put(18, "航空機輸送作戦");
            this.put(19, "北号作戦");
            this.put(20, "潜水艦哨戒任務");
            this.put(21, "北方鼠輸送作戦");
            this.put(22, "艦隊演習");
            this.put(23, "航空戦艦運用演習");
            this.put(24, "<UNKNOWN>");
            this.put(25, "通商破壊作戦");
            this.put(26, "敵母港空襲作戦");
            this.put(27, "潜水艦通商破壊作戦");
            this.put(28, "西方海域封鎖作戦");
            this.put(29, "潜水艦派遣演習");
            this.put(30, "潜水艦派遣作戦");
            this.put(31, "海外艦との接触");
            this.put(32, "遠洋練習航海");
            this.put(33, "前衛支援任務");
            this.put(34, "艦隊決戦支援任務");
            this.put(35, "ＭＯ作戦");
            this.put(36, "水上機基地建設");
            this.put(37, "東京急行");
            this.put(38, "東京急行(弐)");
            this.put(39, "遠洋潜水艦作戦");
            this.put(40, "<UNKNOWN>");
            this.put(41, "<UNKNOWN>");
            this.put(42, "<UNKNOWN>");
            this.put(43, "<UNKNOWN>");
            this.put(44, "<UNKNOWN>");
            this.put(45, "<UNKNOWN>");
            this.put(46, "<UNKNOWN>");
            this.put(47, "<UNKNOWN>");
            this.put(48, "<UNKNOWN>");
            this.put(49, "<UNKNOWN>");
            this.put(50, "<UNKNOWN>");
            this.put(51, "<UNKNOWN>");
            this.put(52, "<UNKNOWN>");
            this.put(53, "<UNKNOWN>");
            this.put(54, "<UNKNOWN>");
            this.put(55, "<UNKNOWN>");
            this.put(56, "<UNKNOWN>");
            this.put(57, "<UNKNOWN>");
            this.put(58, "<UNKNOWN>");
            this.put(59, "<UNKNOWN>");
            this.put(60, "<UNKNOWN>");
            this.put(61, "<UNKNOWN>");
            this.put(62, "<UNKNOWN>");
            this.put(63, "<UNKNOWN>");
            this.put(64, "<UNKNOWN>");
            this.put(65, "<UNKNOWN>");
            this.put(66, "<UNKNOWN>");
            this.put(67, "<UNKNOWN>");
            this.put(68, "<UNKNOWN>");
            this.put(69, "<UNKNOWN>");
            this.put(70, "<UNKNOWN>");
            this.put(71, "<UNKNOWN>");
            this.put(72, "<UNKNOWN>");
            this.put(73, "<UNKNOWN>");
            this.put(74, "<UNKNOWN>");
            this.put(75, "<UNKNOWN>");
            this.put(76, "<UNKNOWN>");
            this.put(77, "<UNKNOWN>");
            this.put(78, "<UNKNOWN>");
            this.put(79, "<UNKNOWN>");
            this.put(80, "<UNKNOWN>");
            this.put(81, "<UNKNOWN>");
            this.put(82, "<UNKNOWN>");
            this.put(83, "<UNKNOWN>");
            this.put(84, "<UNKNOWN>");
            this.put(85, "<UNKNOWN>");
            this.put(86, "<UNKNOWN>");
            this.put(87, "<UNKNOWN>");
            this.put(88, "<UNKNOWN>");
            this.put(89, "<UNKNOWN>");
            this.put(90, "<UNKNOWN>");
            this.put(91, "<UNKNOWN>");
            this.put(92, "<UNKNOWN>");
            this.put(93, "<UNKNOWN>");
            this.put(94, "<UNKNOWN>");
            this.put(95, "<UNKNOWN>");
            this.put(96, "<UNKNOWN>");
            this.put(97, "<UNKNOWN>");
            this.put(98, "<UNKNOWN>");
            this.put(99, "<UNKNOWN>");
            this.put(100, "<UNKNOWN>");
            this.put(101, "<UNKNOWN>");
            this.put(102, "<UNKNOWN>");
            this.put(103, "<UNKNOWN>");
            this.put(104, "<UNKNOWN>");
            this.put(105, "<UNKNOWN>");
            this.put(106, "<UNKNOWN>");
            this.put(107, "<UNKNOWN>");
            this.put(108, "<UNKNOWN>");
            this.put(109, "前衛支援任務");
            this.put(110, "艦隊決戦支援任務");
            this.put(141, "前衛支援任務");
            this.put(142, "艦隊決戦支援任務");
        }
    };

    /**
     * 遠征を取得します
     * 
     * @param id ID
     * @return 遠征
     */
    public static String get(int id) {
        return DECK.get(id);
    }
}
