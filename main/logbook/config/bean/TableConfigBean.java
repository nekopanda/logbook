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

    private int[] columnWidth;

    private int[] columnOrder;

    private SortKey[] sortKeys;

    private String[] headerNames;

    private boolean cyclicReload;

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
     * @return columnWidth
     */
    public int[] getColumnWidth() {
        return this.columnWidth;
    }

    /**
     * @param columnWidth セットする columnWidth
     */
    public void setColumnWidth(int[] columnWidth) {
        this.columnWidth = columnWidth;
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

    /**
     * @return columnOrder
     */
    public int[] getColumnOrder() {
        return this.columnOrder;
    }

    /**
     * @param columnOrder セットする columnOrder
     */
    public void setColumnOrder(int[] columnOrder) {
        this.columnOrder = columnOrder;
    }

    /**
     * @return cyclicReload
     */
    public boolean isCyclicReload() {
        return this.cyclicReload;
    }

    /**
     * @param cyclicReload セットする cyclicReload
     */
    public void setCyclicReload(boolean cyclicReload) {
        this.cyclicReload = cyclicReload;
    }

    /**
     * @return headerNames
     */
    public String[] getHeaderNames() {
        return this.headerNames;
    }

    /**
     * @param headerNames セットする headerNames
     */
    public void setHeaderNames(String[] headerNames) {
        this.headerNames = headerNames;
    }
}
