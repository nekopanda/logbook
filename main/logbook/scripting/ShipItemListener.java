/**
 * 
 */
package logbook.scripting;

import logbook.dto.ShipDto;
import logbook.dto.ShipFilterDto;

/**
 * 艦娘一覧のカラム拡張スクリプトが実装すべきインターフェース
 * @author Nekopanda
 */
public interface ShipItemListener extends TableScriptListener {
    /**
     * 艦娘一覧の内容更新開始時にbody()が呼ばれる前に呼び出されます。
     * @param specdiff 「成長の余地を表示」が選択されているか
     * @param filter フィルター
     */
    public void begin(boolean specdiff, ShipFilterDto filter, int specdisp);

    /**
     * 艦娘データに対する拡張表示内容を返します
     * @param ship　艦娘データ
     * @return　表示内容
     */
    public Comparable[] body(ShipDto ship);

    /**
     * 艦娘一覧の内容更新が終了したときに呼び出されます。
     */
    public void end();
}
