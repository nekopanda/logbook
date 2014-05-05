package logbook.dto;

import java.util.Date;
import java.util.Set;

/**
 * 遠征を表します
 *
 */
public final class DeckMissionDto extends AbstractDto {

    public static final DeckMissionDto EMPTY = new DeckMissionDto(null, null, null, 0, null);

    /** 艦隊名 */
    private final String name;

    /** 遠征名 */
    private final String mission;

    /** 帰投時間 */
    private final Date time;

    /** 艦隊 */
    private final long fleetid;

    /** 艦娘 */
    private final Set<Long> ships;

    /**
     * コンストラクター
     */
    public DeckMissionDto(String name, String mission, Date time, long fleetid, Set<Long> ships) {
        this.name = name;
        this.mission = mission;
        this.time = time;
        this.fleetid = fleetid;
        this.ships = ships;
    }

    /**
     * @return 艦隊名
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return 遠征名
     */
    public String getMission() {
        return this.mission;
    }

    /**
     * @return 帰投時間
     */
    public Date getTime() {
        return this.time;
    }

    /**
     * @return 艦隊
     */
    public long getFleetid() {
        return this.fleetid;
    }

    /**
     * @return 艦娘
     */
    public Set<Long> getShips() {
        return this.ships;
    }
}
