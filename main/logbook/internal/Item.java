package logbook.internal;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logbook.dto.ItemInfoDto;
import logbook.dto.ShipParameters;

import org.apache.commons.lang3.StringUtils;

/**
 * アイテム
 *
 */
public class Item {

    public static final ItemInfoDto UNKNOWN = new ItemInfoDto(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "<UNKNOWN>", 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0);

    /**
     * アイテムプリセット値
     */
    private static final Map<Integer, ItemInfoDto> ITEM = new HashMap<Integer, ItemInfoDto>() {
        {
            this.put(1, new ItemInfoDto(1, 1, 1, 0, 0, 0, 1, 0, 0, 1, 0, "12cm単装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 1));
            this.put(2, new ItemInfoDto(2, 1, 1, 0, 0, 0, 2, 0, 0, 1, 0, "12.7cm連装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(3, new ItemInfoDto(3, 1, 16, 0, 0, 0, 2, 0, 0, 1, 0, "10cm連装高角砲", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 7));
            this.put(4, new ItemInfoDto(4, 2, 2, 0, 0, 0, 2, 0, 1, 2, 0, "14cm単装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(5, new ItemInfoDto(5, 2, 2, 0, 0, 0, 7, 0, 1, 2, 0, "15.5cm三連装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 4));
            this.put(6, new ItemInfoDto(6, 2, 2, 0, 0, 0, 8, 0, 0, 2, 0, "20.3cm連装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 3));
            this.put(7, new ItemInfoDto(7, 3, 3, 0, 0, 0, 15, 0, 0, 3, 0, "35.6cm連装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 4));
            this.put(8, new ItemInfoDto(8, 3, 3, 0, 0, 0, 20, 0, 0, 3, 0, "41cm連装砲", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 4));
            this.put(9, new ItemInfoDto(9, 3, 3, 0, 0, 0, 26, 0, 0, 4, 0, "46cm三連装砲", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 5));
            this.put(10, new ItemInfoDto(10, 4, 16, 0, 0, 0, 2, 0, 1, 1, 0, "12.7cm連装高角砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 4));
            this.put(11, new ItemInfoDto(11, 4, 4, 0, 0, 0, 2, 0, 1, 2, 0, "15.2cm単装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(12, new ItemInfoDto(12, 4, 4, 0, 0, 0, 7, 0, 2, 2, 0, "15.5cm三連装副砲", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 3));
            this.put(13, new ItemInfoDto(13, 5, 5, 0, 0, 0, 0, 0, 0, 1, 0, "61cm三連装魚雷", 5, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(14, new ItemInfoDto(14, 5, 5, 0, 0, 0, 0, 0, 0, 1, 0, "61cm四連装魚雷", 7, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(15, new ItemInfoDto(15, 5, 5, 0, 0, 0, 0, 0, 0, 1, 0, "61cm四連装(酸素)魚雷", 10, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(16, new ItemInfoDto(16, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, "九七式艦攻", 5, 0, 16, 0,
                    0, 1, 0, 0, 0, 4, 0));
            this.put(17, new ItemInfoDto(17, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, "天山", 7, 0, 24, 1,
                    0, 1, 0, 0, 0, 3, 0));
            this.put(18, new ItemInfoDto(18, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, "流星", 10, 0, 56, 2,
                    0, 1, 0, 0, 0, 4, 0));
            this.put(19, new ItemInfoDto(19, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, "九六式艦戦", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(20, new ItemInfoDto(20, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, "零式艦戦21型", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 5));
            this.put(21, new ItemInfoDto(21, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, "零式艦戦52型", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 6));
            this.put(22, new ItemInfoDto(22, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, "烈風", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 10));
            this.put(23, new ItemInfoDto(23, 7, 7, 0, 0, 5, 0, 0, 0, 0, 0, "九九式艦爆", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 3, 0));
            this.put(24, new ItemInfoDto(24, 7, 7, 0, 0, 8, 0, 0, 0, 0, 0, "彗星", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 3, 0));
            this.put(25, new ItemInfoDto(25, 10, 10, 0, 0, 1, 0, 0, 1, 0, 0, "零式水上偵察機", 0, 0, 10, 0,
                    0, 5, 0, 0, 0, 2, 1));
            this.put(26, new ItemInfoDto(26, 11, 10, 0, 0, 4, 0, 0, 1, 0, 0, "瑞雲", 0, 0, 13, 1,
                    0, 6, 0, 0, 0, 4, 2));
            this.put(27, new ItemInfoDto(27, 12, 11, 0, 0, 0, 0, 0, 1, 0, 0, "13号対空電探", 0, 0, 3, 1,
                    0, 3, 0, 0, 0, 0, 2));
            this.put(28, new ItemInfoDto(28, 12, 11, 0, 0, 0, 0, 0, 3, 0, 0, "22号対水上電探", 0, 0, 10, 1,
                    0, 5, 0, 0, 0, 0, 0));
            this.put(29, new ItemInfoDto(29, 12, 11, 0, 0, 0, 0, 0, 5, 0, 0, "33号対水上電探", 0, 0, 12, 2,
                    0, 7, 0, 0, 0, 0, 0));
            this.put(30, new ItemInfoDto(30, 13, 11, 0, 0, 0, 0, 0, 2, 0, 0, "21号対空電探", 0, 0, 3, 2,
                    0, 4, 0, 0, 0, 0, 4));
            this.put(31, new ItemInfoDto(31, 13, 11, 0, 0, 0, 0, 0, 8, 0, 0, "32号対水上電探", 0, 0, 15, 3,
                    0, 10, 0, 0, 0, 0, 0));
            this.put(32, new ItemInfoDto(32, 13, 11, 0, 0, 0, 0, 0, 4, 0, 0, "14号対空電探", 0, 0, 3, 4,
                    0, 5, 0, 0, 0, 0, 6));
            this.put(33, new ItemInfoDto(33, 17, 19, 0, 0, 0, 0, 6, 0, 0, 0, "改良型艦本式タービン", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(34, new ItemInfoDto(34, 17, 19, 0, 0, 0, 0, 10, 0, 0, 0, "強化型艦本式缶", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(35, new ItemInfoDto(35, 18, 12, 0, 0, 0, 0, 0, 0, 0, 0, "三式弾", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 5));
            this.put(36, new ItemInfoDto(36, 19, 13, 0, 0, 0, 8, 0, 1, 0, 0, "九一式徹甲弾", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(37, new ItemInfoDto(37, 21, 15, 0, 0, 0, 0, 1, 0, 0, 0, "7.7mm機銃", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(38, new ItemInfoDto(38, 21, 15, 0, 0, 0, 0, 1, 0, 0, 0, "12.7mm単装機銃", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 3));
            this.put(39, new ItemInfoDto(39, 21, 15, 0, 0, 0, 0, 1, 0, 0, 0, "25mm連装機銃", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 5));
            this.put(40, new ItemInfoDto(40, 21, 15, 0, 0, 0, 0, 1, 0, 0, 0, "25mm三連装機銃", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 6));
            this.put(41, new ItemInfoDto(41, 22, 5, 0, 0, 0, 0, 0, 0, 0, 0, "甲標的", 12, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(42, new ItemInfoDto(42, 23, 14, 0, 0, 0, 0, 0, 0, 0, 0, "応急修理要員", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(43, new ItemInfoDto(43, 23, 14, 0, 0, 0, 0, 0, 0, 0, 0, "応急修理女神", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(44, new ItemInfoDto(44, 15, 17, 0, 0, 0, 0, 0, 0, 0, 0, "九四式爆雷投射機", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 5, 0));
            this.put(45, new ItemInfoDto(45, 15, 17, 0, 0, 0, 0, 0, 0, 0, 0, "三式爆雷投射機", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 8, 0));
            this.put(46, new ItemInfoDto(46, 14, 18, 0, 0, 0, 0, 0, 1, 0, 0, "九三式水中聴音機", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 6, 0));
            this.put(47, new ItemInfoDto(47, 14, 18, 0, 0, 0, 0, 0, 2, 0, 0, "三式水中探信儀", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 10, 0));
            this.put(48, new ItemInfoDto(48, 1, 16, 0, 0, 0, 1, 0, 0, 1, 0, "12.7cm単装高角砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 3));
            this.put(49, new ItemInfoDto(49, 21, 15, 0, 0, 0, 0, 1, 0, 0, 0, "25mm単装機銃", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 4));
            this.put(50, new ItemInfoDto(50, 2, 2, 0, 0, 0, 10, 0, 0, 2, 0, "20.3cm(3号)連装砲", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 4));
            this.put(51, new ItemInfoDto(51, 21, 15, 0, 0, 0, 0, 0, 0, 0, 0, "12cm30連装噴進砲", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 8));
            this.put(52, new ItemInfoDto(52, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, "流星改", 13, 0, 0, 3,
                    0, 2, 0, 0, 0, 3, 0));
            this.put(53, new ItemInfoDto(53, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, "烈風改", 0, 0, 0, 4,
                    0, 0, 0, 0, 0, 0, 12));
            this.put(54, new ItemInfoDto(54, 9, 9, 0, 0, 0, 0, 0, 2, 0, 0, "彩雲", 0, 0, 0, 2,
                    0, 9, 0, 0, 0, 0, 0));
            this.put(55, new ItemInfoDto(55, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, "紫電改二", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 9));
            this.put(56, new ItemInfoDto(56, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, "震電改", 0, 0, 0, 5,
                    0, 0, 0, 0, 0, 0, 15));
            this.put(57, new ItemInfoDto(57, 7, 7, 0, 0, 10, 0, 0, 0, 0, 0, "彗星一二型甲", 0, 0, 0, 2,
                    0, 1, 0, 0, 0, 3, 0));
            this.put(58, new ItemInfoDto(58, 5, 5, 0, 0, 0, 0, 0, 1, 1, 0, "61cm五連装(酸素)魚雷", 12, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(59, new ItemInfoDto(59, 10, 10, 0, 0, 1, 0, 0, 2, 0, 0, "零式水上観測機", 0, 0, 0, 1,
                    0, 6, 0, 0, 0, 4, 2));
            this.put(60, new ItemInfoDto(60, 7, 7, 0, 0, 4, 0, 0, 0, 0, 0, "零式艦戦62型(爆戦)", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 3, 4));
            this.put(61, new ItemInfoDto(61, 9, 9, 0, 0, 0, 0, 0, 3, 0, 0, "二式艦上偵察機", 0, 0, 0, 1,
                    0, 7, 0, 0, 0, 0, 1));
            this.put(62, new ItemInfoDto(62, 11, 10, 0, 0, 11, 0, 0, 1, 0, 0, "試製晴嵐", 0, 0, 0, 4,
                    0, 6, 0, 0, 0, 6, 0));
            this.put(63, new ItemInfoDto(63, 1, 1, 0, 0, 0, 3, 0, 0, 1, 0, "12.7cm連装砲B型改二", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(64, new ItemInfoDto(64, 7, 7, 0, 0, 9, 0, 0, 1, 0, 0, "Ju87C改", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 5, 0));
            this.put(65, new ItemInfoDto(65, 2, 2, 0, 0, 0, 4, 0, 3, 2, 0, "15.2cm連装砲", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 3));
            this.put(66, new ItemInfoDto(66, 4, 16, 0, 0, 0, 1, 0, 2, 1, 0, "8cm高角砲", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 6));
            this.put(67, new ItemInfoDto(67, 5, 5, 0, 0, 0, 0, 0, 2, 1, 0, "53cm艦首(酸素)魚雷", 15, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(68, new ItemInfoDto(68, 24, 20, 0, 0, 0, 0, 0, 0, 0, 0, "大発動艇", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(69, new ItemInfoDto(69, 25, 21, 0, 0, 0, 0, 0, 1, 0, 0, "カ号観測機", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 9, 0));
            this.put(70, new ItemInfoDto(70, 26, 22, 0, 0, 0, 0, 0, 2, 0, 0, "三式指揮連絡機(対潜)", 0, 0, 0, 1,
                    0, 1, 0, 0, 0, 7, 0));
            this.put(71, new ItemInfoDto(71, 4, 16, 0, 0, 0, 1, 0, 1, 1, 0, "10cm連装高角砲(砲架)", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 7));
            this.put(72, new ItemInfoDto(72, 27, 23, 0, 0, 0, 0, -2, 0, 0, 0, "増設バルジ(中型艦)", 0, 0, 0, 2,
                    0, 0, 0, 7, 0, 0, 0));
            this.put(73, new ItemInfoDto(73, 28, 23, 0, 0, 0, 0, -3, 0, 0, 0, "増設バルジ(大型艦)", 0, 0, 0, 2,
                    0, 0, 0, 9, 0, 0, 0));
            this.put(74, new ItemInfoDto(74, 29, 24, 0, 0, 0, 0, 0, 0, 0, 0, "探照灯", 0, 0, 0, 0,
                    0, 2, 0, 0, 0, 0, 0));
            this.put(75, new ItemInfoDto(75, 30, 25, 0, 0, 0, 0, 0, 0, 0, 0, "ドラム缶(輸送用)", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(76, new ItemInfoDto(76, 3, 3, 0, 0, 0, 16, 0, 1, 3, 0, "38cm連装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 1));
            this.put(77, new ItemInfoDto(77, 4, 4, 0, 0, 0, 4, 0, 2, 2, 0, "15cm連装副砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(78, new ItemInfoDto(78, 1, 1, 0, 0, 0, 2, 0, 1, 1, 0, "12.7cm単装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(79, new ItemInfoDto(79, 11, 10, 0, 0, 6, 0, 0, 1, 0, 0, "瑞雲(六三四空)", 0, 0, 0, 2,
                    0, 6, 0, 0, 0, 5, 2));
            this.put(80, new ItemInfoDto(80, 11, 10, 0, 0, 7, 0, 0, 1, 0, 0, "瑞雲12型", 0, 0, 0, 3,
                    0, 6, 0, 0, 0, 5, 3));
            this.put(81, new ItemInfoDto(81, 11, 10, 0, 0, 9, 0, 0, 1, 0, 0, "瑞雲12型(六三四空)", 0, 0, 0, 4,
                    0, 7, 0, 0, 0, 6, 3));
            this.put(82, new ItemInfoDto(82, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, "九七式艦攻(九三一空)", 6, 0, 0, 2,
                    0, 2, 0, 0, 0, 7, 0));
            this.put(83, new ItemInfoDto(83, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, "天山(九三一空)", 9, 0, 0, 3,
                    0, 2, 0, 0, 0, 8, 0));
            this.put(84, new ItemInfoDto(84, 21, 15, 0, 0, 0, 0, 0, 1, 0, 0, "2cm 四連装FlaK 38", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 7));
            this.put(85, new ItemInfoDto(85, 21, 15, 0, 0, 0, 1, 0, 1, 0, 0, "3.7cm FlaK M42", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 8));
            this.put(86, new ItemInfoDto(86, 31, 26, 0, 0, 0, 0, 0, 0, 0, 0, "艦艇修理施設", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(87, new ItemInfoDto(87, 17, 19, 0, 0, 0, 0, 13, 0, 0, 0, "新型高温高圧缶", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(88, new ItemInfoDto(88, 12, 11, 0, 0, 0, 0, 0, 8, 0, 0, "22号対水上電探改四", 0, 0, 0, 3,
                    0, 5, 0, 0, 0, 2, 0));
            this.put(89, new ItemInfoDto(89, 13, 11, 0, 0, 0, 0, 1, 3, 0, 0, "21号対空電探改", 0, 0, 0, 3,
                    0, 6, 0, 0, 0, 0, 5));
            this.put(90, new ItemInfoDto(90, 2, 2, 0, 0, 0, 9, 0, 1, 2, 0, "20.3cm(2号)連装砲", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 3));
            this.put(91, new ItemInfoDto(91, 1, 16, 0, 0, 0, 2, 1, 1, 1, 0, "12.7cm連装高角砲(後期型)", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 1, 5));
            this.put(92, new ItemInfoDto(92, 21, 15, 0, 0, 0, 0, 1, 0, 0, 0, "毘式40mm連装機銃", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 6));
            this.put(93, new ItemInfoDto(93, 8, 8, 0, 0, 0, 0, 0, 3, 0, 0, "九七式艦攻(友永隊)", 11, 0, 0, 4,
                    0, 4, 0, 0, 0, 5, 1));
            this.put(94, new ItemInfoDto(94, 8, 8, 0, 0, 0, 0, 0, 3, 0, 0, "天山一二型(友永隊)", 14, 0, 0, 5,
                    0, 5, 0, 0, 0, 6, 1));
            this.put(95, new ItemInfoDto(95, 32, 5, 0, 0, 0, 0, 0, 3, 1, 0, "潜水艦53cm艦首魚雷(8門)", 16, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(96, new ItemInfoDto(96, 6, 6, 0, 0, 0, 0, 2, 2, 0, 0, "零式艦戦21型(熟練)", 0, 0, 0, 3,
                    0, 1, 0, 0, 0, 0, 8));
            this.put(97, new ItemInfoDto(97, 7, 7, 0, 0, 7, 0, 0, 2, 0, 0, "九九式艦爆(熟練)", 0, 0, 0, 3,
                    0, 2, 0, 0, 0, 4, 1));
            this.put(98, new ItemInfoDto(98, 8, 8, 0, 0, 0, 0, 0, 2, 0, 0, "九七式艦攻(熟練)", 8, 0, 0, 3,
                    0, 2, 0, 0, 0, 5, 0));
            this.put(99, new ItemInfoDto(99, 7, 7, 0, 0, 10, 0, 0, 4, 0, 0, "九九式艦爆(江草隊)", 0, 0, 0, 4,
                    0, 3, 0, 0, 0, 5, 0));
            this.put(100, new ItemInfoDto(100, 7, 7, 0, 0, 13, 0, 0, 4, 0, 0, "彗星(江草隊)", 0, 0, 0, 5,
                    0, 4, 0, 0, 0, 5, 1));
            this.put(101, new ItemInfoDto(101, 33, 27, 0, 0, 0, 0, 0, 0, 0, 0, "照明弾", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(102, new ItemInfoDto(102, 10, 10, 0, 0, 0, 0, 0, 1, 0, 0, "九八式水上偵察機(夜偵)", 0, 0, 0, 3,
                    0, 3, 0, 0, 0, 1, 0));
            this.put(103, new ItemInfoDto(103, 3, 3, 0, 0, 0, 18, 0, 2, 3, 0, "試製35.6cm三連装砲", 0, 0, 0, 4,
                    0, 0, 0, 0, 0, 0, 5));
            this.put(104, new ItemInfoDto(104, 3, 3, 0, 0, 0, 15, 1, 1, 3, 0, "35.6cm連装砲(ダズル迷彩)", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 5));
            this.put(105, new ItemInfoDto(105, 3, 3, 0, 0, 0, 22, 0, 2, 3, 0, "試製41cm三連装砲", 0, 0, 0, 4,
                    0, 0, 0, 0, 0, 0, 5));
            this.put(106, new ItemInfoDto(106, 12, 11, 0, 0, 0, 0, 1, 2, 0, 0, "13号対空電探改", 0, 0, 0, 3,
                    0, 4, 0, 0, 0, 0, 4));
            this.put(107, new ItemInfoDto(107, 34, 28, 0, 0, 0, 0, 1, 1, 0, 0, "艦隊司令部施設", 0, 0, 0, 5,
                    0, 1, 0, 0, 0, 0, 1));
            this.put(108, new ItemInfoDto(108, 35, 29, 0, 0, 0, 10, 0, 1, 3, 0, "熟練艦載機整備員", 0, 0, 0, 3,
                    0, 1, 0, 0, 0, 0, 1));
            this.put(109, new ItemInfoDto(109, 6, 6, 0, 0, 0, 0, 1, 1, 0, 0, "零戦52型丙(六〇一空)", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 9));
            this.put(110, new ItemInfoDto(110, 6, 6, 0, 0, 0, 0, 2, 1, 0, 0, "烈風(六〇一空)", 0, 0, 0, 4,
                    0, 0, 0, 0, 0, 0, 11));
            this.put(111, new ItemInfoDto(111, 7, 7, 0, 0, 11, 0, 0, 1, 0, 0, "彗星(六〇一空)", 0, 0, 0, 3,
                    0, 1, 0, 0, 0, 4, 0));
            this.put(112, new ItemInfoDto(112, 8, 8, 0, 0, 0, 0, 0, 1, 0, 0, "天山(六〇一空)", 10, 0, 0, 3,
                    0, 2, 0, 0, 0, 4, 0));
            this.put(113, new ItemInfoDto(113, 8, 8, 0, 0, 0, 0, 0, 1, 0, 0, "流星(六〇一空)", 13, 0, 0, 4,
                    0, 3, 0, 0, 0, 5, 0));
            this.put(114, new ItemInfoDto(114, 3, 3, 0, 0, 0, 17, 0, 3, 3, 0, "38cm連装砲改", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(115, new ItemInfoDto(115, 10, 10, 0, 0, 1, 0, 0, 2, 0, 0, "Ar196改", 0, 0, 0, 2,
                    0, 5, 0, 0, 0, 5, 1));
            this.put(116, new ItemInfoDto(116, 19, 13, 0, 0, 0, 9, 0, 2, 0, 0, "一式徹甲弾", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(117, new ItemInfoDto(117, 3, 3, 0, 0, 0, 23, 0, 1, 4, 0, "試製46cm連装砲", 0, 0, 0, 4,
                    0, 0, 0, 0, 0, 0, 4));
            this.put(118, new ItemInfoDto(118, 10, 10, 0, 0, 1, 0, 0, 1, 0, 0, "紫雲", 0, 0, 0, 4,
                    0, 8, 0, 0, 0, 2, 0));
            this.put(119, new ItemInfoDto(119, 2, 2, 0, 0, 0, 3, 0, 2, 2, 0, "14cm連装砲", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(120, new ItemInfoDto(120, 36, 30, 0, 0, 0, 0, 1, 0, 0, 0, "91式高射装置", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(121, new ItemInfoDto(121, 36, 30, 0, 0, 0, 0, 1, 0, 0, 0, "94式高射装置", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 3));
            this.put(122, new ItemInfoDto(122, 1, 16, 0, 0, 0, 3, 1, 1, 1, 0, "10cm連装高角砲+高射装置", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 10));
            this.put(123, new ItemInfoDto(123, 2, 2, 0, 0, 0, 10, 0, 3, 2, 0, "SKC34 20.3cm連装砲", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(124, new ItemInfoDto(124, 13, 11, 0, 0, 0, 3, 0, 10, 0, 0, "FuMO25 レーダー", 0, 0, 0, 4,
                    0, 9, 0, 0, 0, 0, 7));
            this.put(125, new ItemInfoDto(125, 5, 5, 0, 0, 0, 0, 1, 0, 1, 0, "61cm三連装(酸素)魚雷", 8, 0, 0, 2,
                    0, 0, 0, 1, 0, 0, 0));
            this.put(126, new ItemInfoDto(126, 37, 31, 0, 0, 0, 1, 0, 0, 1, 0, "WG42 (Wurfgerät 42)", 0, 0, 0, 4,
                    0, 0, 0, -1, 0, 0, 0));
            this.put(127, new ItemInfoDto(127, 32, 5, 0, 0, 0, 0, 2, 7, 1, 0, "試製FaT仕様九五式酸素魚雷改", 14, 0, 0, 5,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(128, new ItemInfoDto(128, 3, 3, 0, 0, 0, 30, -1, 1, 4, 0, "試製51cm連装砲", 0, 0, 0, 5,
                    0, 0, 0, 0, 0, 0, 5));
            this.put(501, new ItemInfoDto(501, 1, 1, 0, 0, 0, 1, 0, 0, 1, 0, "5inch単装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(502, new ItemInfoDto(502, 1, 1, 0, 0, 0, 2, 0, 0, 1, 0, "5inch連装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(503, new ItemInfoDto(503, 1, 16, 0, 0, 0, 1, 0, 0, 1, 0, "3inch単装高角砲", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 1));
            this.put(504, new ItemInfoDto(504, 2, 2, 0, 0, 0, 2, 0, 0, 2, 0, "5inch単装高射砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(505, new ItemInfoDto(505, 2, 2, 0, 0, 0, 8, 0, 0, 2, 0, "8inch三連装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(506, new ItemInfoDto(506, 2, 2, 0, 0, 0, 3, 0, 0, 2, 0, "6inch連装速射砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 3));
            this.put(507, new ItemInfoDto(507, 3, 3, 0, 0, 0, 10, 0, 0, 3, 0, "14inch連装砲", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 4));
            this.put(508, new ItemInfoDto(508, 3, 3, 0, 0, 0, 15, 0, 0, 3, 0, "16inch連装砲", 0, 0, 16, 1,
                    0, 0, 0, 0, 0, 0, 4));
            this.put(509, new ItemInfoDto(509, 3, 3, 0, 0, 0, 20, 0, 0, 3, 0, "16inch三連装砲", 0, 0, 10, 2,
                    0, 0, 0, 0, 0, 0, 5));
            this.put(510, new ItemInfoDto(510, 4, 16, 0, 0, 0, 1, 0, 0, 1, 0, "5inch単装高射砲", 0, 0, 10, 0,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(511, new ItemInfoDto(511, 4, 4, 0, 0, 0, 1, 0, 0, 2, 0, "6inch単装砲", 0, 0, 13, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(512, new ItemInfoDto(512, 4, 4, 0, 0, 0, 7, 0, 0, 2, 0, "12.5inch連装副砲", 0, 0, 3, 1,
                    0, 0, 0, 0, 0, 0, 3));
            this.put(513, new ItemInfoDto(513, 5, 5, 0, 0, 0, 0, 0, 0, 1, 0, "21inch魚雷前期型", 2, 0, 8, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(514, new ItemInfoDto(514, 5, 5, 0, 0, 0, 0, 0, 0, 1, 0, "21inch魚雷後期型", 5, 0, 6, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(515, new ItemInfoDto(515, 5, 5, 0, 0, 0, 0, 0, 0, 1, 0, "高速深海魚雷", 10, 0, 4, 1,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(516, new ItemInfoDto(516, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, "深海棲艦攻", 4, 0, 0, 0,
                    0, 5, 0, 0, 0, 2, 0));
            this.put(517, new ItemInfoDto(517, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, "深海棲艦攻 Mark.II", 6, 0, 2, 1,
                    0, 5, 0, 0, 0, 4, 0));
            this.put(518, new ItemInfoDto(518, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, "深海棲艦攻 Mark.III", 11, 0, 0, 2,
                    0, 5, 0, 0, 0, 7, 4));
            this.put(519, new ItemInfoDto(519, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, "深海棲艦戦", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(520, new ItemInfoDto(520, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, "深海棲艦戦 Mark.II", 0, 0, 24, 0,
                    0, 0, 0, 0, 0, 0, 5));
            this.put(521, new ItemInfoDto(521, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, "深海棲艦戦 Mark.III", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 9));
            this.put(522, new ItemInfoDto(522, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, "飛び魚艦戦", 0, 0, 7, 3,
                    0, 0, 0, 0, 0, 0, 13));
            this.put(523, new ItemInfoDto(523, 7, 7, 0, 0, 3, 0, 0, 0, 0, 0, "深海棲艦爆", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 1, 0));
            this.put(524, new ItemInfoDto(524, 7, 7, 0, 0, 6, 0, 0, 0, 0, 0, "深海棲艦爆 Mark.II", 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 2, 0));
            this.put(525, new ItemInfoDto(525, 10, 10, 0, 0, 1, 0, 0, 0, 0, 0, "深海棲艦偵察機", 0, 0, 0, 0,
                    0, 5, 0, 0, 0, 1, 1));
            this.put(526, new ItemInfoDto(526, 10, 10, 0, 0, 4, 0, 0, 0, 0, 0, "飛び魚偵察機", 0, 0, 0, 1,
                    0, 10, 0, 0, 0, 2, 2));
            this.put(527, new ItemInfoDto(527, 12, 11, 0, 0, 0, 0, 0, 5, 0, 0, "対空レーダ― Mark.I", 0, 0, 0, 1,
                    0, 5, 0, 0, 0, 0, 5));
            this.put(528, new ItemInfoDto(528, 12, 11, 0, 0, 0, 0, 0, 10, 0, 0, "水上レーダ― Mark.I", 0, 0, 0, 1,
                    0, 5, 0, 0, 0, 0, 0));
            this.put(529, new ItemInfoDto(529, 12, 11, 0, 0, 0, 0, 0, 15, 0, 0, "水上レーダ― Mark.II", 0, 0, 0, 2,
                    0, 10, 0, 0, 0, 0, 0));
            this.put(530, new ItemInfoDto(530, 13, 11, 0, 0, 0, 0, 0, 5, 0, 0, "対空レーダ― Mark.II", 0, 0, 21, 2,
                    0, 10, 0, 0, 0, 0, 10));
            this.put(531, new ItemInfoDto(531, 13, 11, 0, 0, 0, 0, 3, 24, 0, 0, "深海水上レーダー", 0, 0, 13, 3,
                    0, 16, 0, 0, 0, 5, 5));
            this.put(532, new ItemInfoDto(532, 13, 11, 0, 0, 0, 0, 2, 16, 0, 0, "深海対空レーダ―", 0, 0, 13, 4,
                    0, 12, 0, 0, 0, 5, 18));
            this.put(533, new ItemInfoDto(533, 17, 19, 0, 0, 0, 0, 10, 0, 0, 0, "改良型深海タービン", 0, 0, 17, 0,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(534, new ItemInfoDto(534, 17, 19, 0, 0, 0, 0, 15, 0, 0, 0, "強化型深海缶", 0, 0, 4, 1,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(535, new ItemInfoDto(535, 18, 12, 0, 0, 0, 0, 0, 0, 0, 0, "対空散弾", 0, 0, 10, 0,
                    0, 0, 0, 0, 0, 0, 10));
            this.put(536, new ItemInfoDto(536, 19, 13, 0, 0, 0, 15, 0, 5, 0, 0, "劣化徹甲弾", 0, 0, 8, 1,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(537, new ItemInfoDto(537, 21, 15, 0, 0, 0, 0, 0, 0, 0, 0, "12.7mm機銃", 0, 0, 5, 0,
                    0, 0, 0, 0, 0, 0, 2));
            this.put(538, new ItemInfoDto(538, 21, 15, 0, 0, 0, 0, 0, 0, 0, 0, "20mm機銃", 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 4));
            this.put(539, new ItemInfoDto(539, 21, 15, 0, 0, 0, 0, 0, 0, 0, 0, "40mm二連装機関砲", 0, 0, 3, 1,
                    0, 0, 0, 0, 0, 0, 8));
            this.put(540, new ItemInfoDto(540, 21, 15, 0, 0, 0, 0, 0, 0, 0, 0, "40mm四連装機関砲", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 12));
            this.put(541, new ItemInfoDto(541, 22, 5, 0, 0, 0, 0, 0, 5, 0, 0, "深海烏賊魚雷", 18, 0, 0, 4,
                    0, 0, 0, 0, 0, 0, 0));
            this.put(542, new ItemInfoDto(542, 15, 17, 0, 0, 0, 0, 0, 0, 0, 0, "深海爆雷投射機", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 7, 0));
            this.put(543, new ItemInfoDto(543, 14, 18, 0, 0, 0, 0, 0, 0, 0, 0, "深海ソナー", 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 9, 0));
            this.put(544, new ItemInfoDto(544, 15, 17, 0, 0, 0, 0, 0, 0, 0, 0, "深海爆雷投射機 Mk.II", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 13, 0));
            this.put(545, new ItemInfoDto(545, 14, 18, 0, 0, 0, 0, 0, 0, 0, 0, "深海ソナー Mk.II", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 16, 0));
            this.put(546, new ItemInfoDto(546, 7, 7, 0, 0, 10, 0, 0, 0, 0, 0, "飛び魚艦爆", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 7, 8));
            this.put(547, new ItemInfoDto(547, 6, 6, 0, 0, 0, 0, 0, 1, 0, 0, "深海猫艦戦", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 10));
            this.put(548, new ItemInfoDto(548, 7, 7, 0, 0, 11, 0, 0, 3, 0, 0, "深海地獄艦爆", 0, 0, 0, 3,
                    0, 3, 0, 0, 0, 4, 0));
            this.put(549, new ItemInfoDto(549, 8, 8, 0, 0, 0, 0, 0, 2, 0, 0, "深海復讐艦攻", 13, 0, 0, 3,
                    0, 5, 0, 0, 0, 5, 4));
            this.put(550, new ItemInfoDto(550, 1, 16, 0, 0, 0, 2, 0, 3, 2, 0, "5inch連装両用莢砲", 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 9));
            this.put(551, new ItemInfoDto(551, 3, 3, 0, 0, 0, 27, 0, 3, 3, 0, "20inch連装砲", 0, 0, 0, 4,
                    0, 0, 0, 0, 0, 0, 4));
        }
    };

    /**
     * アイテムを取得します
     * 
     * @param id ID
     * @return アイテム
     */
    public static ItemInfoDto get(int id) {
        ItemInfoDto dto = getMap().get(id);
        if (dto == null) {
            dto = ITEM.get(id);
        }
        return dto;
    }

    /**
     * IDの一覧を取得します
     * 
     * @return IDの一覧
     */
    public static Set<Integer> keySet() {
        return getMap().keySet();
    }

    public static Map<Integer, ItemInfoDto> getMap() {
        return MasterData.get().getStart2().getItems();
    }

    public static List<ItemInfoDto> fromIdList(int[] slot) {
        List<ItemInfoDto> items = new ArrayList<ItemInfoDto>();
        for (int itemid : slot) {
            if (-1 != itemid) {
                ItemInfoDto item = getMap().get(itemid);
                if (item != null) {
                    items.add(item);
                } else {
                    items.add(Item.UNKNOWN);
                }
            }
            else {
                items.add(null);
            }
        }
        return items;
    }

    public static void dumpCSV(OutputStreamWriter fw) throws IOException {
        fw.write(StringUtils.join(new String[] {
                "名前", "ID", "大分類", "種別", "装備種別", "表示分類", "火力", "雷装", "爆装", "対空", "対潜", "索敵", "命中", "射程", "運", "雷撃命中" },
                ','));
        fw.write("\n");

        for (Integer key : Item.keySet()) {
            ItemInfoDto dto = Item.get(key);
            ShipParameters param = dto.getParam();
            if (dto.getName().length() > 0) {
                fw.write(StringUtils.join(new String[] {
                        dto.getName(), // 名前
                        Integer.toString(dto.getId()), // ID
                        Integer.toString(dto.getType0()), // 
                        Integer.toString(dto.getType1()),
                        Integer.toString(dto.getType2()),
                        Integer.toString(dto.getType3()),
                        Integer.toString(param.getHoug()),
                        Integer.toString(param.getRaig()),
                        Integer.toString(param.getBaku()),
                        Integer.toString(param.getTyku()),
                        Integer.toString(param.getTais()),
                        Integer.toString(param.getSaku()),
                        Integer.toString(param.getHoum()),
                        Integer.toString(param.getLeng()),
                        Integer.toString(param.getLuck()),
                        Integer.toString(param.getSouk()) }, ','));
                fw.write("\n");
            }
        }
    }
}