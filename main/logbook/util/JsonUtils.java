/**
 * 
 */
package logbook.util;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;

/**
 * @author Nekopanda
 *
 */
public class JsonUtils {

    public static int[] toIntArray(JsonArray jsonArray) {
        int[] ret = new int[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            ret[i] = ((JsonNumber) jsonArray.get(i)).intValue();
        }
        return ret;
    }

    public static int[] getIntArray(JsonObject object, String name) {
        return toIntArray(object.getJsonArray(name));
    }
}
