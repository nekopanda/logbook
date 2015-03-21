/**
 * 
 */
package logbook.util;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

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

    public static boolean hasKey(JsonObject object, String name) {
        JsonValue value = object.get(name);
        if ((value == null) || (value == JsonValue.NULL)) {
            return false;
        }
        return true;
    }

    public static JsonObject fromString(String str) {
        JsonReader jsonReader = Json.createReader(new StringReader(str));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }
}
