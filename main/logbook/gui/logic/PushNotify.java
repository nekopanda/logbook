package logbook.gui.logic;

import static javax.json.stream.JsonParser.Event.VALUE_STRING;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.internal.LoggerHolder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Push通知
 *
 */
public final class PushNotify {

    /** 通知処理のキュー*/
    private static Queue<String[]> notifyQueue = new ArrayBlockingQueue<String[]>(10);

    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(PushNotify.class);

    /**
     * 通知メッセージを処理待ちキューに入れます
     * 
     * @param String メッセージ
     * @param Sgring イベント名
     * @param int    priority
     */
    public static void add(String notifymsg, String eventname, int priority) {
        notifyQueue.offer(new String[] { notifymsg, eventname, String.valueOf(priority) });
    }

    /**
     * 通知を実行します
     * 
     * @param String 通知メッセージ
     */
    public static void push(String[] msg) {

        if (AppConfig.get().getNotifyProwl()) {
            pushProwl(msg);
        }

        if (AppConfig.get().getNotifyNMA()) {
            pushNMA(msg);
        }

        if (AppConfig.get().getNotifyImKayac()) {
            pushImKayac(msg);
        }
    }

    /**
     * Prowlによる通知
     * 
     * @param String 通知メッセージ
     */
    private static void pushProwl(String[] msg) {

        StringBuilder postdata = new StringBuilder();
        String result = null;

        addPOSTData(postdata, "apikey", AppConfig.get().getProwlAPIKey());
        addPOSTData(postdata, "application", AppConstants.PUSH_NOTIFY_APPNAME);
        addPOSTData(postdata, "description", msg[0]);
        addPOSTData(postdata, "event", msg[1]);
        addPOSTData(postdata, "priority", msg[2]);

        try {
            result = HttpPOSTRequest(AppConstants.PUSH_NOTIFY_PROWL_URI, postdata);

            if (result != "") {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = factory.newDocumentBuilder();
                InputSource inStream = new InputSource();
                inStream.setCharacterStream(new StringReader(result));
                Document doc = db.parse(inStream);
                Element root = doc.getDocumentElement();
                if (root.getTagName().equals("prowl")) {
                    Node item = root.getFirstChild();
                    String childName = item.getNodeName();
                    if (!childName.equals("success")) {
                        LOG.get().warn("Prowl による Push 通知に失敗しました。", result);
                    }
                }
            }
        } catch (Exception e) {
            LOG.get().warn("Prowl による Push 通知に失敗しました。", e);
        }

    }

    /**
     * NMAによる通知
     * 
     * @param String 通知メッセージ
     */
    private static void pushNMA(String msg[]) {

        StringBuilder postdata = new StringBuilder();
        String result = null;

        addPOSTData(postdata, "apikey", AppConfig.get().getNMAAPIKey());
        addPOSTData(postdata, "application", AppConstants.PUSH_NOTIFY_APPNAME);
        addPOSTData(postdata, "description", msg[0]);
        addPOSTData(postdata, "event", msg[1]);
        addPOSTData(postdata, "priority", msg[2]);

        try {
            result = HttpPOSTRequest(AppConstants.PUSH_NOTIFY_NMA_URI, postdata);

            if (result != "") {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = factory.newDocumentBuilder();
                InputSource inStream = new InputSource();
                inStream.setCharacterStream(new StringReader(result));
                Document doc = db.parse(inStream);
                Element root = doc.getDocumentElement();
                if (root.getTagName().equals("nma")) {
                    Node item = root.getFirstChild();
                    String childName = item.getNodeName();
                    if (!childName.equals("success")) {
                        LOG.get().warn("NMA による Push 通知に失敗しました。", result);
                    }
                }
            }
        } catch (Exception e) {
            LOG.get().warn("NMA による Push 通知に失敗しました。", e);
        }

    }

    /**
     * ImKayacによる通知
     * 
     * @param String 通知メッセージ
     */
    private static void pushImKayac(String msg[]) {

        StringBuilder postdata = new StringBuilder();
        String result = null;

        try {
            if (AppConfig.get().getImKayacPrivateKey() != "") {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                String sigstr = msg[0] + AppConfig.get().getImKayacPrivateKey();
                StringBuffer buffer = new StringBuffer();
                md.update(sigstr.getBytes("UTF-8"));
                byte[] digest = md.digest();
                for (int i = 0; i < digest.length; i++) {
                    String tmpStr = Integer.toHexString(digest[i] & 0xff);
                    if (tmpStr.length() == 1) {
                        buffer.append('0').append(tmpStr);
                    } else {
                        buffer.append(tmpStr);
                    }
                }
                String sig = buffer.toString();
                addPOSTData(postdata, "sig", sig);
                addPOSTData(postdata, "message", msg[0]);
                result = HttpPOSTRequest(AppConstants.PUSH_NOTIFY_IMKAYAC_URI + AppConfig.get().getImKayacUserName(),
                        postdata);
            } else {
                addPOSTData(postdata, "message", msg[0]);
                addPOSTData(postdata, "password", AppConfig.get().getImKayacPasswd());
                result = HttpPOSTRequest(AppConstants.PUSH_NOTIFY_IMKAYAC_URI + AppConfig.get().getImKayacUserName(),
                        postdata);
            }
            JsonParser parser = Json.createParser(new StringReader(result));
            boolean postflag = false;
            while (parser.hasNext()) {
                JsonParser.Event event = parser.next();
                if (event == VALUE_STRING) {
                    if (parser.getString() == "posted") {
                        postflag = true;
                    }
                }
            }
            if (postflag = false) {
                LOG.get().warn("ImKayac による Push 通知に失敗しました", result);
            }
        } catch (Exception e) {
            LOG.get().warn("ImKayac による Push 通知に失敗しました", e);
        }

    }

    /**
     * HTTP POSTリクエストの送信
     * 
     * @param String URL
     * @param StringBuilder POSTデータ
     * 
     */
    private static String HttpPOSTRequest(String posturi, StringBuilder postsb) {
        HttpURLConnection connection = null;
        URL url = null;
        String postdata = postsb.toString();
        try {
            url = new URL(posturi);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(postdata);
            writer.flush();
            writer.close();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuffer response = new StringBuffer();
                String line = null;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                String resultStr = response.toString();
                return resultStr;
            } else {
                LOG.get().warn("Push 通知に失敗しました。HTTPレスポンスコード:" + connection.getResponseCode());
                return "";
            }
        } catch (Exception e) {
            LOG.get().warn("Push 通知に失敗しました", e);
            return "";
        }

    }

    /**
     * POSTデータの生成
    * @param sb StringBuilder url-form-encoded data
    * @param name Key name
    * @param value Value
    */
    private static void addPOSTData(StringBuilder sb, String name, String value)
    {
        if (sb.length() > 0) {
            sb.append("&");
        }
        try {
            sb.append(URLEncoder.encode(name, "UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.get().warn("POSTデータの生成に失敗しました。", e);
        }
    }

    /**
     * Push通知用スレッド
     * 
     */
    public static class PushNotifyThread extends Thread {

        /** ロガー */
        private static final LoggerHolder LOG = new LoggerHolder(PushNotifyThread.class);

        /**
         * 通知スレッド
         */
        public PushNotifyThread() {
            this.setName("logbook_pushnotify.push_thread");
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String msg[] = notifyQueue.poll();
                    if (msg != null) {
                        push(msg);
                    }
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                LOG.get().fatal("スレッドが異常終了しました", e);
                throw new RuntimeException(e);
            }
        }
    }
}
