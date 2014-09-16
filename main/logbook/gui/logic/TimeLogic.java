package logbook.gui.logic;

/**
 * 時間を計算する
 *
 */
public class TimeLogic implements Comparable<TimeLogic> {

    private static final int ONE_MINUTES = 60;
    private static final int ONE_HOUR = 60 * 60;
    private static final int ONE_DAY = 60 * 60 * 24;

    private final long milliseconds;

    public TimeLogic(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public static TimeLogic fromSeconds(long seconds) {
        return new TimeLogic(seconds * 1000);
    }

    @Override
    public String toString() {
        return toDateRestString(this.milliseconds / 1000);
    }

    /* (非 Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TimeLogic arg0) {
        return Long.compare(this.milliseconds, arg0.milliseconds);
    }

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
