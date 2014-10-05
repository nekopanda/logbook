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
public class PracticeUserExDto {

    /** 演習相手のID */
    private int id;

    /** 演習相手の名前 */
    private String name;

    /** 演習相手のComment */
    private String comment;

    /** level */
    private final int level;

    /** rank */
    private String rank;

    /** ships */
    private List<ShipInfoDto> ships = new ArrayList<ShipInfoDto>();

    /** ships level */
    private final int[] shipsLevel = { 1, 1, 1, 1, 1, 1 };

    public PracticeUserExDto(JsonObject obj) {
        this.id = obj.getInt("api_member_id");
        this.name = obj.getString("api_nickname");
        this.level = obj.getInt("api_level");
        JsonArray shipsinfo = obj.getJsonObject("api_deck").getJsonArray("api_ships");
        for (int i = 0; i < shipsinfo.size(); i++) {
            JsonObject s = shipsinfo.getJsonObject(i);
            if (s.getInt("api_id") != -1) {
                this.ships.add(Ship.get(Integer.toString(s.getInt("api_ship_id"))));
                this.shipsLevel[i] = s.getInt("api_level");
            }
        }
    }

    /**
     * @return id
     */
    public int getId() {
        return this.id;
    }

    /**
     * @param id セットする id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name セットする name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return comment
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * @param comment セットする comment
     */
    public void setComment(String comment) {
        this.comment = comment;
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
     * @return level
     */
    public int getLevel() {
        return this.level;
    }

    /**
     * @return shipsLevel
     */
    public int[] getShipsLevel() {
        return this.shipsLevel;
    }

}

