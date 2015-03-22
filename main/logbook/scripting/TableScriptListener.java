/**
 * 
 */
package logbook.scripting;

/**
 * テーブルのカラム拡張用スクリプトが実装すべきインターフェースのベースクラス
 * @author Nekopanda
 */
public interface TableScriptListener {
    /**
     * 拡張するカラムに対応するヘッダーを返します
     * @return　拡張するカラムに対応するヘッダー
     */
    public String[] header();
}
