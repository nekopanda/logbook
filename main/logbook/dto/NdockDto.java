/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.dto;

import java.util.Date;

/**
 * 入渠ドックを表します
 *
 */
public final class NdockDto extends AbstractDto {

    public static final NdockDto EMPTY = new NdockDto(0, null);

    /** 艦娘ID */
    private final long ndockid;

    /** お風呂から上がる時間 */
    private final Date ndocktime;

    /**
     * コンストラクター
     */
    public NdockDto(long ndockid, Date ndocktime) {
        this.ndockid = ndockid;
        this.ndocktime = ndocktime;
    }

    /**
     * @return 艦娘ID
     */
    public long getNdockid() {
        return this.ndockid;
    }

    /**
     * @return お風呂から上がる時間
     */
    public Date getNdocktime() {
        return this.ndocktime;
    }
}
