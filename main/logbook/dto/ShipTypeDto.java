/**
 * 
 */
package logbook.dto;

import java.util.ArrayList;

import javax.json.JsonNumber;
import javax.json.JsonObject;

/**
 * @author iedeg_000
 *
 */
public class ShipTypeDto {

    private final String name;
    private final int id;
    private final ArrayList<Boolean> equipType = new ArrayList<Boolean>();

    public ShipTypeDto(JsonObject object) {
        this.name = object.getString("api_name");
        this.id = object.getJsonNumber("api_id").intValue();
        JsonObject json_equip_type = object.getJsonObject("api_equip_type");
        for (int i = 1;; ++i) {
            JsonNumber number = json_equip_type.getJsonNumber(String.valueOf(i));
            if (number == null)
                break;
            this.equipType.add(number.intValue() != 0);
        }
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public ArrayList<Boolean> getEquipType() {
        return this.equipType;
    }
}
