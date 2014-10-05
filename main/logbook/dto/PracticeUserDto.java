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

    /** 演習相手のComment */
    private String comment;

    /** level */
    private final int level;

    /** 状態 */
    private int state;

    protected PracticeUserDto(int id, String name, String comment, int level) {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.level = level;
        this.state = 0;
    }

    public PracticeUserDto(JsonObject obj) {
        this.id = obj.getInt("api_enemy_id");
        this.name = obj.getString("api_enemy_name");
        this.comment = obj.getString("api_enemy_comment");
        this.level = obj.getInt("api_enemy_level");
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
     * @return level
     */
    public int getLevel() {
        return this.level;
    }
}
