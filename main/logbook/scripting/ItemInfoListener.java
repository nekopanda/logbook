/**
 * 
 */
package logbook.scripting;

import logbook.gui.logic.ItemInfo;

/**
 * 所有装備一覧のカラム拡張スクリプトが実装すべきインターフェース
 * @author Nekopanda
 */
public interface ItemInfoListener extends TableScriptListener {
    /**
     * 装備一覧の内容更新開始時にbody()が呼ばれる前に呼び出されます。
     */
    public void begin();

    /**
     * 装備データに対する拡張表示内容を返します
     * @param data　装備データ
     * @return　表示内容
     */
    public Comparable[] body(ItemInfo item);

    /**
     * 装備一覧の内容更新が終了したときに呼び出されます。
     */
    public void end();
}
