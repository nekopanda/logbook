/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import logbook.config.AppConfig;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.widgets.Display;

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
                if ((entry.getUrl() != null) && this.url.endsWith(entry.getUrl())) {
                    try {
                        // リクエストのフィールドを復号します
                        Map<String, String> field = null;
                        if (this.request != null) {
                            field = getQueryMap(URLDecoder.decode(new String(this.request).trim(), "UTF-8"));
                        }
                        // レスポンスのJSONを復号します
                        InputStream stream = new ByteArrayInputStream(this.response);
                        // レスポンスボディのJSONはsvdata=から始まるので除去します
                        int read;
                        while (((read = stream.read()) != -1) && (read != '=')) {
                        }

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

    // 艦これ統計データベース(仮) に送信する
    private static String[] sendDatabaseUrls = new String[]
    {
            "api_port/port",
            "api_get_member/kdock",
            "api_get_member/ship2",
            "api_get_member/ship3",
            "api_req_hensei/change",
            "api_req_kousyou/createship",
            "api_req_kousyou/getship",
            "api_req_kousyou/createitem",
            "api_req_map/start",
            "api_req_map/next",
            "api_req_sortie/battle",
            "api_req_battle_midnight/battle",
            "api_req_battle_midnight/sp_midnight",
            "api_req_sortie/night_to_day",
            "api_req_sortie/battleresult",
            "api_req_practice/battle",
            "api_req_practice/battle_result",
    };
    private static Pattern apiTokenPattern = Pattern.compile("&api(_|%5F)token=[0-9a-f]+");

    public void sendToDatabase() {
        if (AppConfig.get().isSendDatabase() && (AppConfig.get().getAccessKey().length() > 0)) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    for (String entry : sendDatabaseUrls)
                    {
                        if (UndefinedData.this.url.endsWith(entry))
                        {
                            try {
                                // api_tokenを取り除く
                                String sendRequestBody = apiTokenPattern.matcher(
                                        new String(UndefinedData.this.request, "UTF-8"))
                                        .replaceAll("");
                                String sendResponseBody = new String(UndefinedData.this.response, "UTF-8");
                                Request.Post("http://api.kancolle-db.net/2/")
                                        .bodyForm(
                                                Form.form()
                                                        .add("token", AppConfig.get().getAccessKey())
                                                        // このクライアントのエージェントキー
                                                        .add("agent", "PEjwSXd9b7pGr3GuKzYh")
                                                        .add("url", UndefinedData.this.url)
                                                        .add("requestbody", sendRequestBody)
                                                        .add("responsebody", sendResponseBody)
                                                        .build())
                                        .execute().returnContent();
                            } catch (IOException e) {
                                LOG.warn("データベースへの送信に失敗しました", e);
                                LOG.warn(UndefinedData.this);
                            }
                            break;
                        }
                    }
                }
            });
        }
    }
}
