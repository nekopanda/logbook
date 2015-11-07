package logbook.dto;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import logbook.internal.Deck;
import logbook.internal.MasterData;
import logbook.scripting.ScriptData;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 遠征を表します
 *
 */
public final class DeckMissionDto extends AbstractDto {

    public static final DeckMissionDto EMPTY = new DeckMissionDto();

    /** 艦隊名 */
    private String name;

    /** 遠征名 */
    private int missionId;

    /** 遠征名 */
    private String mission;

    /** 帰投時間 */
    private Date time;

    /** 艦隊 */
    private int fleetid;

    /** 艦娘 */
    private List<Integer> ships;

    /**
     * コンストラクター
     */
    public DeckMissionDto() {
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
    public DeckMissionDto(String name, int missionId, Date time, int fleetid, int[] ships) {
        this.name = name;
        this.missionId = missionId;
        String missionName = MasterData.getMaster().getMissionName(missionId);
        if (missionName == null) {
            missionName = Deck.get(missionId);
        }
        this.mission = missionName;
        this.time = time;
        this.fleetid = fleetid;
        this.ships = Arrays.asList(ArrayUtils.toObject(ships));
    }

    public String getDisplayText(String parameter) {
        String checkResult = "";
        if (parameter != null) {
            Object resultObj = ScriptData.getData(parameter);
            if (resultObj != null) {
                checkResult = resultObj.toString();
            }
        }
        return checkResult + this.mission;
    }

    /**
     * 艦隊名
     * @return 艦隊名
     */
    public String getName() {
        return this.name;
    }

    /**
     * 遠征ID
     * @return 遠征ID
     */
    public int getMissionId() {
        return this.missionId;
    }

    /**
     * 遠征名
     * @return 遠征名
     */
    public String getMission() {
        return this.mission;
    }

    /**
     * 帰投時間
     * @return 帰投時間
     */
    public Date getTime() {
        return this.time;
    }

    /**
     * 艦隊
     * @return 艦隊
     */
    public int getFleetid() {
        return this.fleetid;
    }

    /**
     * 艦娘
     * @return 艦娘
     */
    public List<Integer> getShips() {
        return this.ships;
    }

    /**
     * @param name セットする name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param missionId セットする missionId
     */
    public void setMissionId(int missionId) {
        this.missionId = missionId;
    }

    /**
     * @param mission セットする mission
     */
    public void setMission(String mission) {
        this.mission = mission;
    }

    /**
     * @param time セットする time
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * @param fleetid セットする fleetid
     */
    public void setFleetid(int fleetid) {
        this.fleetid = fleetid;
    }

    /**
     * @param ships セットする ships
     */
    public void setShips(List<Integer> ships) {
        this.ships = ships;
    }
}
