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
        if (date == null) {
            throw new NullPointerException();
        }
        this.date = date;
    }

    @Override
    public String toString() {
        return format.format(this.date);
    }

    @Override
    public int compareTo(DateTimeString o) {
        return Long.compare(this.date.getTime(), o.date.getTime());
    }
}
