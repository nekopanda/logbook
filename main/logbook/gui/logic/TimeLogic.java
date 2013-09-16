/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.logic;

/**
 * 時間を計算する
 *
 */
public class TimeLogic {

    private static final int ONE_MINUTES = 60;
    private static final int ONE_HOUR = 60 * 60;
    private static final int ONE_DAY = 60 * 60 * 24;

    /**
     * 残り時間を見やすい形式に整形する
     * 
     * @param rest
     * @return
     */
    public static String toDateRestString(long rest) {
        if (rest > 0) {
            if (rest > ONE_DAY) {
                return (rest / ONE_DAY) + "日" + ((rest % ONE_DAY) / ONE_HOUR) + "時間"
                        + ((rest % ONE_HOUR) / ONE_MINUTES) + "分";
            } else if (rest > ONE_HOUR) {
                return (rest / ONE_HOUR) + "時間" + ((rest % ONE_HOUR) / ONE_MINUTES) + "分";
            } else if (rest > ONE_MINUTES) {
                return (rest / ONE_MINUTES) + "分" + (rest % ONE_MINUTES) + "秒";
            } else {
                return rest + "秒";
            }
        } else {
            return null;
        }
    }
}
