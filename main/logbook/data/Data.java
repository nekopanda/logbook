/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
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
