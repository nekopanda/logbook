/**
 * 
 */
package logbook.dto;

import java.util.Date;

/**
 * @author Nekopanda
 *
 */
public class KdockDto {

    public static final KdockDto EMPTY = new KdockDto(false, null);

    /** 今使用中？ */
    private final boolean nowUsing;

    /** 完成する時間 */
    private final Date kdocktime;

    /**
     * コンストラクター
     */
    public KdockDto(boolean nowUsing, Date kdocktime) {
        this.nowUsing = nowUsing;
        this.kdocktime = kdocktime;
    }

    /**
     * @return 今使用中？
     */
    public boolean getNowUsing() {
        return this.nowUsing;
    }

    /**
     * @return 完成する時間
     */
    public Date getKdocktime() {
        return this.kdocktime;
    }
}
