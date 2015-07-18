/**
 * 
 */
package logbook.dto;

import logbook.internal.Ship;

import com.dyuproject.protostuff.Tag;

/**
 * @author Nekopanda
 *
 */
public class EnemyShipDto extends ShipBaseDto {

    /** 火力 */
    @Tag(10)
    private final int karyoku;

    /** 雷装 */
    @Tag(11)
    private final int raisou;

    /** 対空 */
    @Tag(12)
    private final int taiku;

    /** 装甲 */
    @Tag(13)
    private final int soukou;

    /** レベル */
    @Tag(14)
    private final int lv;

    public EnemyShipDto(int shipId, int[] slot, int[] param, int lv) {
        super(Ship.get(String.valueOf(shipId)), slot, param, false);
        // ここで送られてくるパラメータは装備を含まない素の値
        this.karyoku = param[0];
        this.raisou = param[1];
        this.taiku = param[2];
        this.soukou = param[3];
        this.lv = lv;
    }

    @Override
    public int getLv() {
        return this.lv;
    }

}
