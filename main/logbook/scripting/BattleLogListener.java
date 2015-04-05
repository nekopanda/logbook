/**
 * 
 */
package logbook.scripting;

import logbook.dto.BattleExDto;

/**
 * ドロップ報告書のカラム拡張スクリプトが実装すべきインターフェース
 * @author Nekopanda
 */
public interface BattleLogListener extends TableScriptListener {
    /**
     * データベースのロード開始時あるいは再読み込み開始時にbody()が呼ばれる前に呼び出されます。
     */
    public void begin();

    /**
     * 戦闘結果に対する拡張表示内容を返します
     * @param battle　読み込んだ戦闘結果詳細データ
     * @return　表示内容
     */
    public Comparable[] body(BattleExDto battle);

    /**
     * データベースのロードが終了したときに呼び出されます。
     */
    public void end();
}
