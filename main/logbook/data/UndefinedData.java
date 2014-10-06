package logbook.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * 同定されていない未加工のデータ
 * 
 */
public class UndefinedData implements Data {

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
            DataType type = DataType.TYPEMAP.get(this.url);

            if (type != null) {
                try {
                    // リクエストのフィールドを復号します
                    Map<String, String> field = null;
                    if (this.request != null) {
                        field = getQueryMap(URLDecoder.decode(new String(this.request).trim(), "UTF-8"));
                    }
                    // レスポンスのJSONを復号します
                    InputStream stream = new ByteArrayInputStream(this.response);
                    if ((this.response[0] == (byte) 0x1f) && (this.response[1] == (byte) 0x8b)) {
                        // レスポンスの先頭2バイトが0x1f, 0x8bであればgzip圧縮されている
                        stream = new GZIPInputStream(stream);
                    }
                    // レスポンスボディのJSONはsvdata=から始まるので除去します
                    int read;
                    while (((read = stream.read()) != -1) && (read != '=')) {
                    }

                    JsonReader jsonreader = Json.createReader(stream);
                    JsonObject json = jsonreader.readObject();

                    return new ActionData(type, this.date, json, field);
                } catch (Exception e) {
                    return this;
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
