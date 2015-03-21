package logbook.gui.logic;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * テーブルの行を作成するインターフェイスです
 *
 */
public interface TableItemCreator {
    void begin(String[] header);

    TableItem create(Table table, Comparable[] text, int count);

    void end();
}
