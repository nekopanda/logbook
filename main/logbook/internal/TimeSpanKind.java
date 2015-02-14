/**
 * 
 */
package logbook.internal;

import java.util.Date;

/**
 * @author Nekopanda
 *
 */
public enum TimeSpanKind {
    LAST_HOUR(1, "直近1時間"),
    LAST_6HOURS(6, "直近6時間"),
    LAST_12HOURS(12, "直近12時間"),
    LAST_24HOURS(24, "直近24時間"),
    LAST_3DAYS(24 * 3, "直近3日間"),
    LAST_7DAYS(24 * 7, "直近7日間"),
    ALL(10 * 365 * 24, "すべて"),
    MANUAL(0, "日付指定");

    private final long mills;
    private final String desc;

    private TimeSpanKind(int hours, String desc) {
        this.mills = hours * 3600l * 1000l;
        this.desc = desc;
    }

    public Date getFrom() {
        Date d = new Date();
        d.setTime(d.getTime() - this.mills);
        return d;
    }

    public Date getTo() {
        return new Date();
    }

    /**
     * @return mills
     */
    public long getMills() {
        return this.mills;
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
