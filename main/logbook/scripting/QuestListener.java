/**
 * 
 */
package logbook.scripting;

import logbook.dto.QuestDto;

/**
 * 任務一覧のカラム拡張スクリプトが実装すべきインターフェース
 * @author Nekopanda
 *
 */
public interface QuestListener extends TableScriptListener {
    /**
     * 任務一覧の内容更新開始時にbody()が呼ばれる前に呼び出されます。
     */
    public void begin();

    /**
     * 任務データに対する拡張表示内容を返します
     * @param data　任務データ
     * @return　表示内容
     */
    public Comparable[] body(QuestDto quest);

    /**
     * 任務一覧の内容更新が終了したときに呼び出されます。
     */
    public void end();
}
