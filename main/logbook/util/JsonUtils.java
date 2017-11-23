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
        if (jsonArray == null)
            return new int[0];
        int[] ret = new int[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            ret[i] = ((JsonNumber) jsonArray.get(i)).intValue();
        }
        return ret;
    }

    public static int[] getIntArray(JsonObject object, String name) {
        return toIntArray(getJsonArray(object, name));
    }

    public static boolean hasKey(JsonObject object, String name) {
        JsonValue value = object.get(name);
        if ((value == null) || (value == JsonValue.NULL)) {
            return false;
        }
        return true;
    }

    // null対応
    public static JsonArray getJsonArray(JsonObject object, String name) {
        if (hasKey(object, name)) {
            return object.getJsonArray(name);
        }
        return null;
    }

    // null対応
    public static JsonArray getJsonArray(JsonArray object, int index) {
        if (object == null)
            return null;
        if (object.size() <= index)
            return null;
        JsonValue value = object.get(index);
        if ((value == null) || (value == JsonValue.NULL)) {
            return null;
        }
        return object.getJsonArray(index);
    }

    // null対応
    public static JsonObject getJsonObject(JsonObject object, String name) {
        if (hasKey(object, name)) {
            return object.getJsonObject(name);
        }
        return null;
    }

    public static JsonObject fromString(String str) {
        JsonReader jsonReader = Json.createReader(new StringReader(str));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }
}
