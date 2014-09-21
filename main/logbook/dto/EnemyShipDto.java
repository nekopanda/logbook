/**
 * 
 */
package logbook.dto;

import logbook.proto.Tag;

/**
 * @author Nekopanda
 *
 */
public class EnemyShipDto extends ShipBaseDto {

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

    public EnemyShipDto(int shipId, int[] slot, int[] param) {
        super(shipId, slot);
        this.karyoku = param[0];
        this.raisou = param[0];
        this.taiku = param[0];
        this.soukou = param[0];
    }

    /**
     * @return 火力
     */
    @Override
    public int getKaryoku() {
        return this.karyoku;
    }

    /**
     * @return 雷装
     */
    @Override
    public int getRaisou() {
        return this.raisou;
    }

    /**
     * @return 対空
     */
    @Override
    public int getTaiku() {
        return this.taiku;
    }

    /**
     * @return 装甲
     */
    @Override
    public int getSoukou() {
        return this.soukou;
    }

}
