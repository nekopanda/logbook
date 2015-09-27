/**
 * 
 */
package logbook.gui.logic;

/**
 * 比較可能な数値ペア
 * @author Nekopanda
 */
public class IntegerPair implements Comparable<IntegerPair> {
    protected final int now;
    protected final int max;
    protected final String format;

    public IntegerPair(int now, int max, String format) {
        this.now = now;
        this.max = max;
        this.format = format;
    }

    @Override
    public String toString() {
        return String.format(this.format, this.now, this.max);
    }

    @Override
    public int compareTo(IntegerPair o) {
        int ret = Integer.compare(this.now, o.now);
        if (ret == 0) {
            return Integer.compare(this.max, o.max);
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof IntegerPair) {
            IntegerPair o2 = (IntegerPair) o;
            return (o2.now == this.now) && (o2.max == this.max);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.now * this.max;
    }
}
