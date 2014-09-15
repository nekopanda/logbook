/**
 * 
 */
package logbook.gui.logic;

/**
 * @author Nekopanda
 *
 */
public class HpString implements Comparable<HpString> {
    private final int now;
    private final int max;

    public HpString(int now, int max) {
        this.now = now;
        this.max = max;
    }

    @Override
    public String toString() {
        return String.valueOf(this.now) + "/" + String.valueOf(this.max);
    }

    @Override
    public int compareTo(HpString o) {
        return Integer.compare(this.now, o.now);
    }
}
