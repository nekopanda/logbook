package logbook.data;

import java.util.Date;
import java.util.Map;

import javax.json.JsonObject;

import logbook.dto.AbstractDto;

/**
 * アプリケーションで使用するデータを表します
 * 
 */
public final class ActionData extends AbstractDto implements Data {

    private final DataType type;

    private final Date date;

    private final JsonObject json;

    private final Map<String, String> postField;

    public ActionData(DataType type, Date createDate, JsonObject json, Map<String, String> postField) {
        this.type = type;
        this.date = createDate;
        this.json = json;
        this.postField = postField;
    }

    @Override
    public DataType getDataType() {
        return this.type;
    }

    @Override
    public Date getCreateDate() {
        return this.date;
    }

    @Override
    public JsonObject getJsonObject() {
        return this.json;
    }

    @Override
    public String getField(String key) {
        if (this.postField != null) {
            return this.postField.get(key);
        }
        return null;
    }

}
