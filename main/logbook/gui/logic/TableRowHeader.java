/**
 * 
 */
package logbook.gui.logic;

/**
 * @author Nekopanda
 * データを埋め込んだ番号
 */
public class TableRowHeader implements Comparable<TableRowHeader> {
    private int number;
    private final Object data;

    public TableRowHeader(int number, Object data) {
        this.number = number;
        this.data = data;
    }

    public Object get() {
        return this.data;
    }

    @Override
    public String toString() {
        return String.valueOf(this.number);
    }

    @Override
    public int compareTo(TableRowHeader o) {
        return Integer.compare(this.number, o.number);
    }

    /**
     * @return number
     */
    public int getNumber() {
        return this.number;
    }

    /**
     * @param number セットする number
     */
    public void setNumber(int number) {
        this.number = number;
    }
}
