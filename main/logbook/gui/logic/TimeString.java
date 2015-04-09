/**
 * 
 */
package logbook.gui.logic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Nekopanda
 *
 */
public class TimeString implements Comparable<TimeString> {
    private static DateFormat format = new SimpleDateFormat("HH:mm:ss");
    private final Date date;

    public TimeString(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        if (this.date == null)
            return "";
        return format.format(this.date);
    }

    @Override
    public int compareTo(TimeString o) {
        long o1 = (this.date == null) ? 0 : this.date.getTime();
        long o2 = (o.date == null) ? 0 : o.date.getTime();
        return Long.compare(o1, o2);
    }

}
