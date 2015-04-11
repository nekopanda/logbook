/**
 * 
 */
package logbook.scripting;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nekopanda
 *
 */
public class ScriptData {
    private static Map<String, Object> dataMap = new HashMap<>();

    public static void setData(String key, Object value) {
        dataMap.put(key, value);
    }

    public static Object getData(String key) {
        return dataMap.get(key);
    }
}
