/**
 * 
 */
package logbook.gui.logic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import logbook.constants.AppConstants;

/**
 * @author Nekopanda
 *
 */
public class DateTimeString implements Comparable<DateTimeString> {
    private static DateFormat format = new SimpleDateFormat(AppConstants.DATE_FORMAT);
    private final Date date;

    public DateTimeString(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return format.format(this.date);
    }

    @Override
    public int compareTo(DateTimeString o) {
        long o1 = (this.date == null) ? 0 : this.date.getTime();
        long o2 = (o.date == null) ? 0 : o.date.getTime();
        return Long.compare(o1, o2);
    }
}
