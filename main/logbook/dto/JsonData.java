/**
 * 
 */
package logbook.dto;

import javax.json.JsonObject;

import logbook.util.JsonUtils;

/**
 * @author Nekopanda
 *
 */
public class JsonData {
    protected JsonObject json;

    public JsonData() {
    }

    public JsonData(JsonObject json) {
        this.json = json;
    }

    /**
     * @return json
     */
    public JsonObject getJson() {
        return this.json;
    }

    public String getJsonString() {
        if (this.json == null)
            return null;
        return this.json.toString();
    }

    public void setJsonString(String json) {
        this.json = JsonUtils.fromString(json);
    }
}
