/**
 * 
 */
package logbook.gui.logic;

/**
 * a(+b)など上昇値も併記する表示
 * @author Nekopanda
 */
public class ShipStatusString extends IntegerPair {

    public ShipStatusString(int main, int sup) {
        super(main, sup, "%d+%d");
    }

    @Override
    public String toString() {
        if (this.max != 0) {
            return String.format(this.format, this.now, this.max);
        }
        else {
            return Integer.toString(this.now);
        }
    }
}
