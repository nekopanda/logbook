/**
 * 
 */
package logbook.config.bean;


/**
 * @author Nekopanda
 *
 */
public class TableConfigBean {

    public static class SortKey {
        public int index;
        public boolean order;

        public SortKey() {
        }

        public SortKey(int index, boolean order) {
            this.index = index;
            this.order = order;
        }
    }

    private boolean[] visibleColumn;

    private SortKey[] sortKeys;

    /**
     * @return visibleColumn
     */
    public boolean[] getVisibleColumn() {
        return this.visibleColumn;
    }

    /**
     * @param visibleColumn セットする visibleColumn
     */
    public void setVisibleColumn(boolean[] visibleColumn) {
        this.visibleColumn = visibleColumn;
    }

    /**
     * @return sortKeys
     */
    public SortKey[] getSortKeys() {
        return this.sortKeys;
    }

    /**
     * @param sortKeys セットする sortKeys
     */
    public void setSortKeys(SortKey[] sortKeys) {
        this.sortKeys = sortKeys;
    }
}
