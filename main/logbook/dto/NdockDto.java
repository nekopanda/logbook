package logbook.dto;

import java.util.Date;

/**
 * 入渠ドックを表します
 *
 */
public final class NdockDto extends AbstractDto {

    public static final NdockDto EMPTY = new NdockDto(0, null);

    /** 艦娘ID */
    private final int ndockid;

    /** お風呂から上がる時間 */
    private final Date ndocktime;

    /**
     * コンストラクター
     */
    public NdockDto(int ndockid, Date ndocktime) {
        this.ndockid = ndockid;
        this.ndocktime = ndocktime;
    }

    /**
     * @return 艦娘ID
     */
    public int getNdockid() {
        return this.ndockid;
    }

    /**
     * @return お風呂から上がる時間
     */
    public Date getNdocktime() {
        return this.ndocktime;
    }
}
