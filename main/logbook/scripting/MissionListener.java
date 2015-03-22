/**
 * 
 */
package logbook.scripting;

import logbook.internal.MasterData.MissionDto;

/**
 * 遠征一覧のカラムを拡張するスクリプトが実装すべきインターフェース
 * @author Nekopanda
 */
public interface MissionListener extends TableScriptListener {
    /**
     * 遠征一覧の内容更新開始時にbody()が呼ばれる前に呼び出されます。
     * @param fleetid 選択中の艦隊(2～4)
     */
    void begin(int fleetid);

    /**
     * 遠征データに対する拡張表示内容を返します
     * @param data　遠征データ
     * @return　表示内容
     */
    Comparable[] body(MissionDto data);

    /**
     * 遠征一覧の内容更新が終了したときに呼び出されます。
     */
    void end();
}
