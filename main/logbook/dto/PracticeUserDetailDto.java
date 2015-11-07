/**
 * 
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

import logbook.internal.Ship;

/**
 * @author Nekopanda
 *
 */
public class PracticeUserDetailDto extends PracticeUserDto {

    /** rank */
    private String rank;

    /** ships */
    private List<ShipInfoDto> ships = new ArrayList<ShipInfoDto>();

    /** ships level */
    private final int[] shipsLevel = { 1, 1, 1, 1, 1, 1 };

    public PracticeUserDetailDto(JsonObject obj) {
        super(obj.getInt("api_member_id"),
                obj.getString("api_nickname"),
                obj.getString("api_cmt"),
                obj.getInt("api_level"));
        JsonArray shipsinfo = obj.getJsonObject("api_deck").getJsonArray("api_ships");
        for (int i = 0; i < shipsinfo.size(); i++) {
            JsonObject s = shipsinfo.getJsonObject(i);
            if (s.getInt("api_id") != -1) {
                this.ships.add(Ship.get(s.getInt("api_ship_id")));
                this.shipsLevel[i] = s.getInt("api_level");
            }
        }
    }

    /**
     * @return rank
     */
    public String getRank() {
        return this.rank;
    }

    /**
     * @param rank セットする rank
     */
    public void setRank(String rank) {
        this.rank = rank;
    }

    /**
     * @return ships
     */
    public List<ShipInfoDto> getShips() {
        return this.ships;
    }

    /**
     * @param ships セットする ships
     */
    public void setShips(List<ShipInfoDto> ships) {
        this.ships = ships;
    }

    /**
     * @return shipsLevel
     */
    public int[] getShipsLevel() {
        return this.shipsLevel;
    }

}
