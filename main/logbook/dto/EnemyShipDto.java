/**
 * 
 */
package logbook.dto;

import logbook.proto.LogbookEx.EnemyShipDtoPb;
import logbook.proto.Tag;

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

    public EnemyShipDto(int shipId, int[] slot, int[] param) {
        super(shipId, slot);
        this.karyoku = param[0];
        this.raisou = param[0];
        this.taiku = param[0];
        this.soukou = param[0];
    }

    public EnemyShipDtoPb toProto() {
        EnemyShipDtoPb.Builder builder = EnemyShipDtoPb.newBuilder();
        builder.setKaryoku(this.karyoku);
        builder.setRaisou(this.raisou);
        builder.setTaiku(this.taiku);
        builder.setSoukou(this.soukou);
        if (this.shipInfo != null) {
            builder.setShipInfo(this.shipInfo.toProto());
        }
        if (this.slot != null) {
            for (int b : this.slot) {
                builder.addSlot(b);
            }
        }
        if (this.slotItem != null) {
            for (ItemDto b : this.slotItem) {
                if (b != null) {
                    builder.addSlotItem(b.toProto());
                }
            }
        }
        if (this.param != null) {
            builder.setParam(this.param.toProto());
        }
        if (this.slotParam != null) {
            builder.setSlotParam(this.slotParam.toProto());
        }
        return builder.build();
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
