/**
 * 
 */
package logbook.gui.logic;

/**
 * HPなどのスラッシュ(/)で区切られた数値ペア
 * @author Nekopanda
 */
public class HpString extends IntegerPair {

    public HpString(int now, int max) {
        super(now, max, "%d/%d");
    }

}
