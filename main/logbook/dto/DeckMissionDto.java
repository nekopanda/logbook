package logbook.dto;

import java.util.Date;
import java.util.Set;

import logbook.internal.Deck;

/**
 * 遠征を表します
 *
 */
public final class DeckMissionDto extends AbstractDto {

    public static final DeckMissionDto EMPTY = new DeckMissionDto();

    /** 艦隊名 */
    private final String name;

    /** 遠征名 */
    private final int missionId;

    /** 遠征名 */
    private final String mission;

    /** 帰投時間 */
    private final Date time;

    /** 艦隊 */
    private final int fleetid;

    /** 艦娘 */
    private final Set<Integer> ships;

    /**
     * コンストラクター
     */
    private DeckMissionDto() {
        this.name = null;
        this.missionId = -1;
        this.mission = null;
        this.time = null;
        this.fleetid = 0;
        this.ships = null;
    }

    /**
     * コンストラクター
     */
    public DeckMissionDto(String name, int missionId, Date time, int fleetid, Set<Integer> ships) {
        this.name = name;
        this.missionId = missionId;
        this.mission = Deck.get(missionId);
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
     * @return 遠征ID
     */
    public int getMissionId() {
        return this.missionId;
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
    public int getFleetid() {
        return this.fleetid;
    }

    /**
     * @return 艦娘
     */
    public Set<Integer> getShips() {
        return this.ships;
    }
}
