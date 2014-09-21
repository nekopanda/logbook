/**
 * 
 */
package logbook.dto;

import logbook.internal.Ship;
import logbook.proto.Tag;

/**
 * @author Nekopanda
 *
 */
public class EnemyShipDto extends ShipBaseDto {

    /** 装備 */
    @Tag(20)
    private final int[] slot;

    /** 火力 */
    @Tag(22)
    private final int karyoku;

    /** 雷装 */
    @Tag(24)
    private final int raisou;

    /** 対空 */
    @Tag(26)
    private final int taiku;

    /** 装甲 */
    @Tag(28)
    private final int soukou;

    //

    public EnemyShipDto(int shipId, int[] slot, int[] param) {
        super(Ship.get(String.valueOf(shipId)));
        this.slot = slot;
        this.karyoku = param[0];
        this.raisou = param[0];
        this.taiku = param[0];
        this.soukou = param[0];

        // 上記４つ以外のパラメータはマスターデータと装備から計算
    }

    /**
     * @return 装備ID
     */
    @Override
    public int[] getItemId() {
        return this.slot;
    }
}
