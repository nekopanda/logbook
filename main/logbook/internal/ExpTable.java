/**
 * 
 */
package logbook.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 経験値テーブル
 *
 */
public class ExpTable {

    /**
     * 経験値テーブルプリセット値
     */
    private static final Map<Integer, Long> EXP_TABLE = new LinkedHashMap<Integer, Long>() {
        {
            this.put(1, 0l);
            this.put(2, 100l);
            this.put(3, 300l);
            this.put(4, 600l);
            this.put(5, 1000l);
            this.put(6, 1500l);
            this.put(7, 2100l);
            this.put(8, 2800l);
            this.put(9, 3600l);
            this.put(10, 4500l);
            this.put(11, 5500l);
            this.put(12, 6600l);
            this.put(13, 7800l);
            this.put(14, 9100l);
            this.put(15, 10500l);
            this.put(16, 12000l);
            this.put(17, 13600l);
            this.put(18, 15300l);
            this.put(19, 17100l);
            this.put(20, 19000l);
            this.put(21, 21000l);
            this.put(22, 23100l);
            this.put(23, 25300l);
            this.put(24, 27600l);
            this.put(25, 30000l);
            this.put(26, 32500l);
            this.put(27, 35100l);
            this.put(28, 37800l);
            this.put(29, 40600l);
            this.put(30, 43500l);
            this.put(31, 46500l);
            this.put(32, 49600l);
            this.put(33, 52800l);
            this.put(34, 56100l);
            this.put(35, 59500l);
            this.put(36, 63000l);
            this.put(37, 66600l);
            this.put(38, 70300l);
            this.put(39, 74100l);
            this.put(40, 78000l);
            this.put(41, 82000l);
            this.put(42, 86100l);
            this.put(43, 90300l);
            this.put(44, 94600l);
            this.put(45, 99000l);
            this.put(46, 103500l);
            this.put(47, 108100l);
            this.put(48, 112800l);
            this.put(49, 117600l);
            this.put(50, 122500l);
            this.put(51, 127500l);
            this.put(52, 132700l);
            this.put(53, 138100l);
            this.put(54, 143700l);
            this.put(55, 149500l);
            this.put(56, 155500l);
            this.put(57, 161700l);
            this.put(58, 168100l);
            this.put(59, 174700l);
            this.put(60, 181500l);
            this.put(61, 188500l);
            this.put(62, 195800l);
            this.put(63, 203400l);
            this.put(64, 211300l);
            this.put(65, 219500l);
            this.put(66, 228000l);
            this.put(67, 236800l);
            this.put(68, 245900l);
            this.put(69, 255300l);
            this.put(70, 265000l);
            this.put(71, 275000l);
            this.put(72, 285400l);
            this.put(73, 296200l);
            this.put(74, 307400l);
            this.put(75, 319000l);
            this.put(76, 331000l);
            this.put(77, 343400l);
            this.put(78, 356200l);
            this.put(79, 369400l);
            this.put(80, 383000l);
            this.put(81, 397000l);
            this.put(82, 411500l);
            this.put(83, 426500l);
            this.put(84, 442000l);
            this.put(85, 458000l);
            this.put(86, 474500l);
            this.put(87, 491500l);
            this.put(88, 509000l);
            this.put(89, 527000l);
            this.put(90, 545500l);
            this.put(91, 564500l);
            this.put(92, 584500l);
            this.put(93, 606500l);
            this.put(94, 631500l);
            this.put(95, 661500l);
            this.put(96, 701500l);
            this.put(97, 761500l);
            this.put(98, 851500l);
            this.put(99, 1000000l);
            this.put(100, 1000000l);
            this.put(101, 1010000l);
            this.put(102, 1011000l);
            this.put(103, 1013000l);
            this.put(104, 1016000l);
            this.put(105, 1020000l);
            this.put(106, 1025000l);
            this.put(107, 1031000l);
            this.put(108, 1038000l);
            this.put(109, 1046000l);
            this.put(110, 1055000l);
            this.put(111, 1065000l);
            this.put(112, 1077000l);
            this.put(113, 1091000l);
            this.put(114, 1107000l);
            this.put(115, 1125000l);
            this.put(116, 1145000l);
            this.put(117, 1168000l);
            this.put(118, 1194000l);
            this.put(119, 1223000l);
            this.put(120, 1255000l);
            this.put(121, 1290000l);
            this.put(122, 1329000l);
            this.put(123, 1372000l);
            this.put(124, 1419000l);
            this.put(125, 1470000l);
        }
    };

    /**
     * 経験値テーブルを取得します
     * 
     * @return
     */
    public static Map<Integer, Long> get() {
        return Collections.unmodifiableMap(EXP_TABLE);
    }
}
