/**
 * 
 */
package logbook.dto;

import javax.json.JsonObject;

/**
 * @author Nekopanda
 *
 */
public class PracticeUserDto {

    /** 演習相手のID */
    private int id;

    /** 演習相手の名前 */
    private String name;

    /** 状態 */
    private int state;

    public PracticeUserDto(JsonObject obj) {
        this.id = obj.getInt("api_enemy_id");
        this.name = obj.getString("api_enemy_name");
        this.state = obj.getInt("api_state");
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
     * @return state
     */
    public int getState() {
        return this.state;
    }

    /**
     * @param state セットする state
     */
    public void setState(int state) {
        this.state = state;
    }
}
