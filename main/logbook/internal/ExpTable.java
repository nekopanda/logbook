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

    public static final int MAX_LEVEL = 155;

    /**
     * 経験値テーブルプリセット値
     */
    private static final Map<Integer, Integer> EXP_TABLE = new LinkedHashMap<Integer, Integer>() {
        {
            this.put(1, 0);
            this.put(2, 100);
            this.put(3, 300);
            this.put(4, 600);
            this.put(5, 1000);
            this.put(6, 1500);
            this.put(7, 2100);
            this.put(8, 2800);
            this.put(9, 3600);
            this.put(10, 4500);
            this.put(11, 5500);
            this.put(12, 6600);
            this.put(13, 7800);
            this.put(14, 9100);
            this.put(15, 10500);
            this.put(16, 12000);
            this.put(17, 13600);
            this.put(18, 15300);
            this.put(19, 17100);
            this.put(20, 19000);
            this.put(21, 21000);
            this.put(22, 23100);
            this.put(23, 25300);
            this.put(24, 27600);
            this.put(25, 30000);
            this.put(26, 32500);
            this.put(27, 35100);
            this.put(28, 37800);
            this.put(29, 40600);
            this.put(30, 43500);
            this.put(31, 46500);
            this.put(32, 49600);
            this.put(33, 52800);
            this.put(34, 56100);
            this.put(35, 59500);
            this.put(36, 63000);
            this.put(37, 66600);
            this.put(38, 70300);
            this.put(39, 74100);
            this.put(40, 78000);
            this.put(41, 82000);
            this.put(42, 86100);
            this.put(43, 90300);
            this.put(44, 94600);
            this.put(45, 99000);
            this.put(46, 103500);
            this.put(47, 108100);
            this.put(48, 112800);
            this.put(49, 117600);
            this.put(50, 122500);
            this.put(51, 127500);
            this.put(52, 132700);
            this.put(53, 138100);
            this.put(54, 143700);
            this.put(55, 149500);
            this.put(56, 155500);
            this.put(57, 161700);
            this.put(58, 168100);
            this.put(59, 174700);
            this.put(60, 181500);
            this.put(61, 188500);
            this.put(62, 195800);
            this.put(63, 203400);
            this.put(64, 211300);
            this.put(65, 219500);
            this.put(66, 228000);
            this.put(67, 236800);
            this.put(68, 245900);
            this.put(69, 255300);
            this.put(70, 265000);
            this.put(71, 275000);
            this.put(72, 285400);
            this.put(73, 296200);
            this.put(74, 307400);
            this.put(75, 319000);
            this.put(76, 331000);
            this.put(77, 343400);
            this.put(78, 356200);
            this.put(79, 369400);
            this.put(80, 383000);
            this.put(81, 397000);
            this.put(82, 411500);
            this.put(83, 426500);
            this.put(84, 442000);
            this.put(85, 458000);
            this.put(86, 474500);
            this.put(87, 491500);
            this.put(88, 509000);
            this.put(89, 527000);
            this.put(90, 545500);
            this.put(91, 564500);
            this.put(92, 584500);
            this.put(93, 606500);
            this.put(94, 631500);
            this.put(95, 661500);
            this.put(96, 701500);
            this.put(97, 761500);
            this.put(98, 851500);
            this.put(99, 1000000);
            this.put(100, 1000000);
            this.put(101, 1010000);
            this.put(102, 1011000);
            this.put(103, 1013000);
            this.put(104, 1016000);
            this.put(105, 1020000);
            this.put(106, 1025000);
            this.put(107, 1031000);
            this.put(108, 1038000);
            this.put(109, 1046000);
            this.put(110, 1055000);
            this.put(111, 1065000);
            this.put(112, 1077000);
            this.put(113, 1091000);
            this.put(114, 1107000);
            this.put(115, 1125000);
            this.put(116, 1145000);
            this.put(117, 1168000);
            this.put(118, 1194000);
            this.put(119, 1223000);
            this.put(120, 1255000);
            this.put(121, 1290000);
            this.put(122, 1329000);
            this.put(123, 1372000);
            this.put(124, 1419000);
            this.put(125, 1470000);
            this.put(126, 1525000);
            this.put(127, 1584000);
            this.put(128, 1647000);
            this.put(129, 1714000);
            this.put(130, 1785000);
            this.put(131, 1860000);
            this.put(132, 1940000);
            this.put(133, 2025000);
            this.put(134, 2115000);
            this.put(135, 2210000);
            this.put(136, 2310000);
            this.put(137, 2415000);
            this.put(138, 2525000);
            this.put(139, 2640000);
            this.put(140, 2760000);
            this.put(141, 2887000);
            this.put(142, 3021000);
            this.put(143, 3162000);
            this.put(144, 3310000);
            this.put(145, 3465000);
            this.put(146, 3628000);
            this.put(147, 3799000);
            this.put(148, 3978000);
            this.put(149, 4165000);
            this.put(150, 4360000);
            this.put(151, 4564000);
            this.put(152, 4777000);
            this.put(153, 4999000);
            this.put(154, 5230000);
            this.put(155, 5470000);
        }
    };

    /**
     * 経験値テーブルを取得します
     * 
     * @return
     */
    public static Map<Integer, Integer> get() {
        return Collections.unmodifiableMap(EXP_TABLE);
    }
}
