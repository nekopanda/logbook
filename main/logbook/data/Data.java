package logbook.data;

import java.util.Date;

import javax.json.JsonObject;

/**
 * データを表します
 *
 */
public interface Data {

    DataType getDataType();

    Date getCreateDate();

    JsonObject getJsonObject();

    String getField(String key);
}
