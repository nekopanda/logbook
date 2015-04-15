package logbook.server.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import logbook.config.AppConfig;
import logbook.data.Data;
import logbook.data.DataType;
import logbook.data.UndefinedData;
import logbook.data.context.GlobalContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.Callback;
import org.eclipse.swt.widgets.Display;

/**
 * リバースプロキシ
 *
 */
public final class ReverseProxyServlet extends ProxyServlet {

    /** ライブラリバグ対応 (HttpRequest#queryを上書きする) */
    private static final Field QUERY_FIELD = getDeclaredField(HttpRequest.class, "query");

    /*
     * リモートホストがローカルループバックアドレス以外の場合400を返し通信しない
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        if (AppConfig.get().isAllowOnlyFromLocalhost() && !AppConfig.get().isCloseOutsidePort()) {
            if (!InetAddress.getByName(request.getRemoteAddr()).isLoopbackAddress()) {
                response.setStatus(400);
                return;
            }
        }
        super.service(request, response);
    }

    @Override
    protected String rewriteTarget(HttpServletRequest clientRequest)
    {
        if (!this.validateDestination(clientRequest.getServerName(), clientRequest.getServerPort()))
            return null;

        StringBuilder target = new StringBuilder();

        String url = clientRequest.getRequestURL().toString();
        for (int i = 0; i < url.length(); ++i) {
            char ch = url.charAt(i);
            if (" []".indexOf(ch) != -1) {
                target.append('%').append(Integer.toHexString(ch));
            }
            else {
                target.append(ch);
            }
        }

        String query = clientRequest.getQueryString();
        if (query != null) {
            target.append("?");

            // 有効なエンコードがされていないqueryで例外を吐くので無効な%は除去しておく
            for (int i = 0; i < query.length(); ++i) {
                char ch = query.charAt(i);
                if (ch == '%') {
                    if ((i + 2) < query.length()) {
                        char p1 = query.charAt(i + 1);
                        char p2 = query.charAt(i + 2);
                        if ((Character.digit(p1, 16) != -1) && (Character.digit(p2, 16) != -1)) {
                            // 有効な%だけ追加
                            target.append(ch).append(p1).append(p2);
                            i += 2;
                        }
                    }
                }
                else {
                    target.append(ch);
                }
            }
        }
        return target.toString();
    }

    /*
     * Hop-by-Hop ヘッダーを除去します
     */
    @Override
    protected void sendProxyRequest(HttpServletRequest clientRequest, HttpServletResponse proxyResponse,
            Request proxyRequest) {
        proxyRequest.onRequestContent(new RequestContentListener(clientRequest));

        // Hop-by-Hop ヘッダーを除去します
        proxyRequest.header(HttpHeader.VIA, null);
        proxyRequest.header(HttpHeader.X_FORWARDED_FOR, null);
        proxyRequest.header(HttpHeader.X_FORWARDED_PROTO, null);
        proxyRequest.header(HttpHeader.X_FORWARDED_HOST, null);
        proxyRequest.header(HttpHeader.X_FORWARDED_SERVER, null);
        proxyRequest.header("Origin", null);

        // ライブラリバグはとりあえず様子見
        //String queryString = ((org.eclipse.jetty.server.Request) clientRequest).getQueryString();
        //fixQueryString(proxyRequest, queryString);

        super.sendProxyRequest(clientRequest, proxyResponse, proxyRequest);
    }

    @Override
    protected String filterResponseHeader(HttpServletRequest request,
            String headerName,
            String headerValue)
    {
        // Content Encoding を取得する
        if (headerName.compareToIgnoreCase("Content-Encoding") == 0) {
            request.setAttribute(Filter.CONTENT_ENCODING, headerValue);
        }
        return super.filterResponseHeader(request, headerName, headerValue);
    }

    /*
     * レスポンスが帰ってきた
     */
    @Override
    protected void onResponseContent(HttpServletRequest request, HttpServletResponse response,
            Response proxyResponse,
            byte[] buffer, int offset, int length, Callback callback) {

        // フィルタークラスで必要かどうかを判別後、必要であれば内容をキャプチャする
        // 注意: 1回のリクエストで複数回の応答が帰ってくるので全ての応答をキャプチャする必要がある
        if (Filter.isNeed(request.getServerName(), response.getContentType())) {
            ByteArrayOutputStream stream = (ByteArrayOutputStream) request.getAttribute(Filter.RESPONSE_BODY);
            if (stream == null) {
                stream = new ByteArrayOutputStream();
                request.setAttribute(Filter.RESPONSE_BODY, stream);
            }
            // ストリームに書き込む
            stream.write(buffer, offset, length);
        }

        super.onResponseContent(request, response, proxyResponse, buffer, offset, length, callback);
    }

    /*
     * レスポンスが完了した
     */
    @Override
    protected void onProxyResponseSuccess(HttpServletRequest clientRequest, HttpServletResponse proxyResponse,
            Response serverResponse) {

        if (Filter.isNeed(clientRequest.getServerName(), proxyResponse.getContentType())) {
            byte[] postField = (byte[]) clientRequest.getAttribute(Filter.REQUEST_BODY);
            ByteArrayOutputStream stream = (ByteArrayOutputStream) clientRequest.getAttribute(Filter.RESPONSE_BODY);
            if (stream != null) {
                byte[] responseBody = stream.toByteArray();

                // 圧縮されていたら解凍する
                String contentEncoding = (String) clientRequest.getAttribute(Filter.CONTENT_ENCODING);
                if ((contentEncoding != null) && contentEncoding.equals("gzip")) {
                    try {
                        responseBody = IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(responseBody)));
                    } catch (IOException e) {
                        //
                    }
                }

                final UndefinedData rawData = new UndefinedData(clientRequest.getRequestURL().toString(),
                        clientRequest.getRequestURI(), postField, responseBody);
                final String serverName = clientRequest.getServerName();

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        // 統計データベース(http://kancolle-db.net/)に送信する
                        DatabaseClient.send(rawData);

                        // キャプチャしたバイト配列は何のデータかを決定する
                        Data data = rawData.toDefinedData();
                        if (data.getDataType() != DataType.UNDEFINED) {
                            // 定義済みのデータの場合にキューに追加する
                            GlobalContext.updateContext(data);

                            // サーバー名が不明の場合、サーバー名をセットする
                            if (!Filter.isServerDetected()) {
                                Filter.setServerName(serverName);
                            }
                        }
                    }
                });
            }
        }
        super.onProxyResponseSuccess(clientRequest, proxyResponse, serverResponse);
    }

    /*
     * HttpClientを作成する
     */
    @Override
    protected HttpClient newHttpClient() {
        HttpClient client = super.newHttpClient();
        // プロキシを設定する
        if (AppConfig.get().isUseProxy()) {
            // ポート
            int port = AppConfig.get().getProxyPort();
            // ホスト
            String host = AppConfig.get().getProxyHost();
            // 設定する
            //client.setProxyConfiguration(new ProxyConfiguration(host, port));
            client.getProxyConfiguration().getProxies().add(new HttpProxy(host, port));
        }
        return client;
    }

    /**
     * private フィールドを取得する
     * @param clazz クラス
     * @param string フィールド名
     * @return フィールドオブジェクト
     */
    private static <T> Field getDeclaredField(Class<T> clazz, String string) {
        try {
            Field field = clazz.getDeclaredField(string);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>
     * ライブラリのバグを修正します<br>
     * URLにマルチバイト文字が含まれている場合にURLが正しく組み立てられないバグを修正します
     * </p>
     */
    private static void fixQueryString(Request proxyRequest, String queryString) {
        if (!StringUtils.isEmpty(queryString)) {
            if (proxyRequest instanceof HttpRequest) {
                try {
                    QUERY_FIELD.set(proxyRequest, queryString);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}