/**
 * 
 */
package logbook.gui.logic;

/**
 * @author Nekopanda
 *
 */
public class IntegerPair implements Comparable<IntegerPair> {
    private final int now;
    private final int max;
    private final String separator;

    public IntegerPair(int now, int max, String separator) {
        this.now = now;
        this.max = max;
        this.separator = separator;
    }

    @Override
    public String toString() {
        return String.valueOf(this.now) + this.separator + String.valueOf(this.max);
    }

    @Override
    public int compareTo(IntegerPair o) {
        int ret = Integer.compare(this.now, o.now);
        if (ret == 0) {
            return Integer.compare(this.max, o.max);
        }
        return ret;
    }
}
