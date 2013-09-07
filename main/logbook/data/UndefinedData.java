package logbook.data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 同定されていない未加工のデータ
 * 
 */
public class UndefinedData implements Data {

    private static final Logger LOG = LogManager.getLogger(UndefinedData.class);

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
        this.request = Arrays.copyOf(request, request.length);
        this.response = Arrays.copyOf(response, response.length);
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
     * URLを返します
     * @return URL
     */
    public final String getUrl() {
        return this.url;
    }

    /**
     * レスポンスを返します
     * @return レスポンス
     */
    public final byte[] getResponse() {
        return Arrays.copyOf(this.response, this.response.length);
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
        try {
            if (this.getResponse().length != 0) {

                for (DataType entry : DataType.values()) {
                    if ((entry.getUrl() != null) && this.getUrl().endsWith(entry.getUrl())) {
                        try {
                            // リクエストのフィールドを復号します
                            Map<String, String> field = null;
                            if (this.request != null) {
                                field = getQueryMap(URLDecoder.decode(new String(this.request).trim(), "UTF-8"));
                            }
                            // レスポンスのJSONを復号します
                            InputStream stream = new ByteArrayInputStream(this.getResponse());
                            // レスポンスボディのJSONはsvdata=から始まるので除去します
                            stream.skip("svdata=".length());

                            JsonReader jsonreader = Json.createReader(stream);
                            JsonObject json = jsonreader.readObject();

                            return new ActionData(entry, this.getCreateDate(), json, field);
                        } catch (Exception e) {
                            return this;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("取得したデータの解析に失敗しました", e);
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
