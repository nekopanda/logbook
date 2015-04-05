package logbook.gui.logic;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * テーブルの行を作成するインターフェイスです
 */
public interface TableItemCreator {
    /**
     * テーブルリロード時に行作成前に呼び出されます。
     * @param header テーブルのヘッダ
     */
    void begin(String[] header);

    /**
     * 行作成時に呼び出されます
     * @param table テーブル
     * @param data 該当行の項目データ
     * @param index 行番号（上から0始まり）
     * @return 作成したTableItem
     */
    TableItem create(Table table, Comparable[] data, int index);

    /**
     * テーブルリロード時に行作成が終了したときに呼び出されます。
     */
    void end();
}
