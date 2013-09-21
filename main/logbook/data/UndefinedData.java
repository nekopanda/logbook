/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * 同定されていない未加工のデータ
 * 
 */
public class UndefinedData implements Data {

    private static final long SKIP_BYTES = "svdata=".length();

    private final String url;

    private final byte[] request;

    private final byte[] response;

    private final Date date;

    /**
     * 未加工データのコンストラクター
     * 
     * @param url URL
     * @param response レスポンスのバイト配列
     */
    public UndefinedData(String url, byte[] request, byte[] response) {
        this.url = url;
        this.request = request;
        this.response = response;
        this.date = Calendar.getInstance().getTime();
    }

    @Override
    public final DataType getDataType() {
        return DataType.UNDEFINED;
    }

    @Override
    public final Date getCreateDate() {
        return (Date) this.date.clone();
    }

    @Override
    public final JsonObject getJsonObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final String getField(String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * 未定義のデータを同定します
     * 同定出来ない場合の型はUndefeatedDataです
     * </p>
     * 
     * @param plaindata
     * @return
     */
    public final Data toDefinedData() {
        if (this.response.length != 0) {

            for (DataType entry : DataType.values()) {
                if ((this.url != null) && this.url.endsWith(entry.getUrl())) {
                    try {
                        // リクエストのフィールドを復号します
                        Map<String, String> field = null;
                        if (this.request != null) {
                            field = getQueryMap(URLDecoder.decode(new String(this.request).trim(), "UTF-8"));
                        }
                        // レスポンスのJSONを復号します
                        InputStream stream = new ByteArrayInputStream(this.response);
                        // レスポンスボディのJSONはsvdata=から始まるので除去します
                        stream.skip(SKIP_BYTES);

                        JsonReader jsonreader = Json.createReader(stream);
                        JsonObject json = jsonreader.readObject();

                        return new ActionData(entry, this.date, json, field);
                    } catch (Exception e) {
                        return this;
                    }
                }
            }
        }
        return this;
    }

    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String[] splited = param.split("=");
            String name = splited[0];
            String value = null;
            if (splited.length == 2) {
                value = splited[1];
            }
            map.put(name, value);
        }
        return map;
    }
}
