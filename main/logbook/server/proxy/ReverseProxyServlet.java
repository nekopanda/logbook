/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.server.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import logbook.data.Data;
import logbook.data.DataProxy;
import logbook.data.DataType;
import logbook.data.UndefinedData;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.proxy.ProxyServlet;

/**
 * リバースプロキシ
 *
 */
public final class ReverseProxyServlet extends ProxyServlet {

    /** ライブラリバグ対応 (HttpRequest#queryを上書きする) */
    private static final Field QUERY_FIELD = getDeclaredField(HttpRequest.class, "query");

    /*
     * Hop-by-Hop ヘッダーを除去します
     */
    @Override
    protected void customizeProxyRequest(Request proxyRequest, HttpServletRequest request) {
        proxyRequest.onRequestContent(new RequestContentListener(request));

        // Hop-by-Hop ヘッダーを除去します
        proxyRequest.header(HttpHeader.VIA, null);
        proxyRequest.header(HttpHeader.X_FORWARDED_FOR, null);
        proxyRequest.header(HttpHeader.X_FORWARDED_PROTO, null);
        proxyRequest.header(HttpHeader.X_FORWARDED_HOST, null);
        proxyRequest.header(HttpHeader.X_FORWARDED_SERVER, null);
        proxyRequest.header("Origin", null);

        String queryString = ((org.eclipse.jetty.server.Request) request).getQueryString();
        fixQueryString(proxyRequest, queryString);

        super.customizeProxyRequest(proxyRequest, request);
    }

    /*
     * レスポンスが帰ってきた
     */
    @Override
    protected void onResponseContent(HttpServletRequest request, HttpServletResponse response,
            Response proxyResponse,
            byte[] buffer, int offset, int length) throws IOException {

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

        super.onResponseContent(request, response, proxyResponse, buffer, offset, length);
    }

    /*
     * レスポンスが完了した
     */
    @Override
    protected void onResponseSuccess(HttpServletRequest request, HttpServletResponse response,
            Response proxyResponse) {

        if (Filter.isNeed(request.getServerName(), response.getContentType())) {

            try {
                ByteArrayOutputStream stream = (ByteArrayOutputStream) request.getAttribute(Filter.RESPONSE_BODY);
                System.out.println(request.getRequestURI() + "?"
                        + URLDecoder.decode(new String((byte[]) request.getAttribute(Filter.REQUEST_BODY),
                                "UTF-8"), "UTF-8")
                        + ": "
                        + Calendar.getInstance().getTimeInMillis()
                        + ": "
                        + new String(stream.toByteArray()));
            } catch (UnsupportedEncodingException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
            }

            byte[] postField = (byte[]) request.getAttribute(Filter.REQUEST_BODY);
            ByteArrayOutputStream stream = (ByteArrayOutputStream) request.getAttribute(Filter.RESPONSE_BODY);
            if (stream != null) {
                // キャプチャしたバイト配列は何のデータかを決定する
                Data data = new UndefinedData(request.getRequestURI(), postField, stream.toByteArray()).toDefinedData();
                if (data.getDataType() != DataType.UNDEFINED) {
                    // 定義済みのデータの場合にキューに追加する
                    DataProxy.add(data);

                    // サーバー名が不明の場合、サーバー名をセットする
                    if (!Filter.isServerDetected()) {
                        Filter.setServerName(request.getServerName());
                    }
                }
            }
        }
        super.onResponseSuccess(request, response, proxyResponse);
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
