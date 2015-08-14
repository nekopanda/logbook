//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package logbook.server.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * Asynchronous ProxyServlet.
 * <p/>
 * Forwards requests to another server either as a standard web reverse proxy
 * (as defined by RFC2616) or as a transparent reverse proxy.
 * <p/>
 * To facilitate JMX monitoring, the {@link HttpClient} instance is set as context attribute,
 * prefixed with the servlet's name and exposed by the mechanism provided by
 * {@link ContextHandler#MANAGED_ATTRIBUTES}.
 * <p/>
 * The following init parameters may be used to configure the servlet:
 * <ul>
 * <li>hostHeader - forces the host header to a particular value</li>
 * <li>viaHost - the name to use in the Via header: Via: http/1.1 &lt;viaHost&gt;</li>
 * <li>whiteList - comma-separated list of allowed proxy hosts</li>
 * <li>blackList - comma-separated list of forbidden proxy hosts</li>
 * </ul>
 * <p/>
 * In addition, see {@link #createHttpClient()} for init parameters used to configure
 * the {@link HttpClient} instance.
 *
 * @see ConnectHandler
 */
public class ProxyServlet extends HttpServlet
{
    protected static final String ASYNC_CONTEXT = ProxyServlet.class.getName() + ".asyncContext";
    private static final Set<String> HOP_HEADERS = new HashSet<>();
    static
    {
        HOP_HEADERS.add("proxy-connection");
        HOP_HEADERS.add("connection");
        HOP_HEADERS.add("keep-alive");
        HOP_HEADERS.add("transfer-encoding");
        HOP_HEADERS.add("te");
        HOP_HEADERS.add("trailer");
        HOP_HEADERS.add("proxy-authorization");
        HOP_HEADERS.add("proxy-authenticate");
        HOP_HEADERS.add("upgrade");
    }

    private final Set<String> _whiteList = new HashSet<>();
    private final Set<String> _blackList = new HashSet<>();

    protected Logger _log;
    private String _hostHeader;
    private String _viaHost;
    private HttpClient _client;
    private long _timeout;

    @Override
    public void init() throws ServletException
    {
        this._log = this.createLogger();

        ServletConfig config = this.getServletConfig();

        this._hostHeader = config.getInitParameter("hostHeader");

        this._viaHost = config.getInitParameter("viaHost");
        if (this._viaHost == null)
            this._viaHost = viaHost();

        try
        {
            this._client = this.createHttpClient();

            // Put the HttpClient in the context to leverage ContextHandler.MANAGED_ATTRIBUTES
            this.getServletContext().setAttribute(config.getServletName() + ".HttpClient", this._client);

            String whiteList = config.getInitParameter("whiteList");
            if (whiteList != null)
                this.getWhiteListHosts().addAll(this.parseList(whiteList));

            String blackList = config.getInitParameter("blackList");
            if (blackList != null)
                this.getBlackListHosts().addAll(this.parseList(blackList));
        } catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    public long getTimeout()
    {
        return this._timeout;
    }

    public void setTimeout(long timeout)
    {
        this._timeout = timeout;
    }

    public Set<String> getWhiteListHosts()
    {
        return this._whiteList;
    }

    public Set<String> getBlackListHosts()
    {
        return this._blackList;
    }

    protected static String viaHost()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException x)
        {
            return "localhost";
        }
    }

    /**
     * @return a logger instance with a name derived from this servlet's name.
     */
    protected Logger createLogger()
    {
        String name = this.getServletConfig().getServletName();
        name = name.replace('-', '.');
        return Log.getLogger(name);
    }

    @Override
    public void destroy()
    {
        try
        {
            this._client.stop();
        } catch (Exception x)
        {
            this._log.debug(x);
        }
    }

    /**
     * Creates a {@link HttpClient} instance, configured with init parameters of this servlet.
     * <p/>
     * The init parameters used to configure the {@link HttpClient} instance are:
     * <table>
     * <thead>
     * <tr>
     * <th>init-param</th>
     * <th>default</th>
     * <th>description</th>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>maxThreads</td>
     * <td>256</td>
     * <td>The max number of threads of HttpClient's Executor</td>
     * </tr>
     * <tr>
     * <td>maxConnections</td>
     * <td>32768</td>
     * <td>The max number of connections per destination, see {@link HttpClient#setMaxConnectionsPerDestination(int)}</td>
     * </tr>
     * <tr>
     * <td>idleTimeout</td>
     * <td>30000</td>
     * <td>The idle timeout in milliseconds, see {@link HttpClient#setIdleTimeout(long)}</td>
     * </tr>
     * <tr>
     * <td>timeout</td>
     * <td>60000</td>
     * <td>The total timeout in milliseconds, see {@link Request#timeout(long, TimeUnit)}</td>
     * </tr>
     * <tr>
     * <td>requestBufferSize</td>
     * <td>HttpClient's default</td>
     * <td>The request buffer size, see {@link HttpClient#setRequestBufferSize(int)}</td>
     * </tr>
     * <tr>
     * <td>responseBufferSize</td>
     * <td>HttpClient's default</td>
     * <td>The response buffer size, see {@link HttpClient#setResponseBufferSize(int)}</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @return a {@link HttpClient} configured from the {@link #getServletConfig() servlet configuration}
     * @throws ServletException if the {@link HttpClient} cannot be created
     */
    protected HttpClient createHttpClient() throws ServletException
    {
        ServletConfig config = this.getServletConfig();

        HttpClient client = this.newHttpClient();
        // Redirects must be proxied as is, not followed
        client.setFollowRedirects(false);

        // Must not store cookies, otherwise cookies of different clients will mix
        client.setCookieStore(new HttpCookieStore.Empty());

        String value = config.getInitParameter("maxThreads");
        if (value == null)
            value = "256";
        QueuedThreadPool executor = new QueuedThreadPool(Integer.parseInt(value));
        String servletName = config.getServletName();
        int dot = servletName.lastIndexOf('.');
        if (dot >= 0)
            servletName = servletName.substring(dot + 1);
        executor.setName(servletName);
        client.setExecutor(executor);

        value = config.getInitParameter("maxConnections");
        if (value == null)
            value = "32768";
        client.setMaxConnectionsPerDestination(Integer.parseInt(value));

        value = config.getInitParameter("idleTimeout");
        if (value == null)
            value = "30000";
        client.setIdleTimeout(Long.parseLong(value));

        value = config.getInitParameter("timeout");
        if (value == null)
            value = "60000";
        this._timeout = Long.parseLong(value);

        value = config.getInitParameter("requestBufferSize");
        if (value != null)
            client.setRequestBufferSize(Integer.parseInt(value));

        value = config.getInitParameter("responseBufferSize");
        if (value != null)
            client.setResponseBufferSize(Integer.parseInt(value));

        try
        {
            client.start();

            // Content must not be decoded, otherwise the client gets confused
            client.getContentDecoderFactories().clear();

            return client;
        } catch (Exception x)
        {
            throw new ServletException(x);
        }
    }

    /**
     * @return a new HttpClient instance
     */
    protected HttpClient newHttpClient()
    {
        return new HttpClient();
    }

    private Set<String> parseList(String list)
    {
        Set<String> result = new HashSet<>();
        String[] hosts = list.split(",");
        for (String host : hosts)
        {
            host = host.trim();
            if (host.length() == 0)
                continue;
            result.add(host);
        }
        return result;
    }

    /**
     * Checks the given {@code host} and {@code port} against whitelist and blacklist.
     *
     * @param host the host to check
     * @param port the port to check
     * @return true if it is allowed to be proxy to the given host and port
     */
    public boolean validateDestination(String host, int port)
    {
        String hostPort = host + ":" + port;
        if (!this._whiteList.isEmpty())
        {
            if (!this._whiteList.contains(hostPort))
            {
                this._log.debug("Host {}:{} not whitelisted", host, port);
                return false;
            }
        }
        if (!this._blackList.isEmpty())
        {
            if (this._blackList.contains(hostPort))
            {
                this._log.debug("Host {}:{} blacklisted", host, port);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        final int requestId = this.getRequestId(request);

        URI rewrittenURI = this.rewriteURI(request);

        if (this._log.isDebugEnabled())
        {
            StringBuffer uri = request.getRequestURL();
            if (request.getQueryString() != null)
                uri.append("?").append(request.getQueryString());
            this._log.debug("{} rewriting: {} -> {}", requestId, uri, rewrittenURI);
        }

        if (rewrittenURI == null)
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        AsyncContext asyncContext = request.startAsync();
        // We do not timeout the continuation, but the proxy request
        asyncContext.setTimeout(0);
        request.setAttribute(ASYNC_CONTEXT, asyncContext);

        ProxyRequestHandler proxyRequestHandler = new ProxyRequestHandler(request, response, rewrittenURI);
        proxyRequestHandler.send();
    }

    private Request createProxyRequest(HttpServletRequest request, HttpServletResponse response, URI targetUri,
            ContentProvider contentProvider)
    {
        final Request proxyRequest = this._client.newRequest(targetUri)
                .method(HttpMethod.fromString(request.getMethod()))
                .version(HttpVersion.fromString(request.getProtocol()));

        // Copy headers
        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();)
        {
            String headerName = headerNames.nextElement();
            String lowerHeaderName = headerName.toLowerCase(Locale.ENGLISH);

            // Remove hop-by-hop headers
            if (HOP_HEADERS.contains(lowerHeaderName))
                continue;

            if ((this._hostHeader != null) && lowerHeaderName.equals("host"))
                continue;

            for (Enumeration<String> headerValues = request.getHeaders(headerName); headerValues.hasMoreElements();)
            {
                String headerValue = headerValues.nextElement();
                if (headerValue != null)
                    proxyRequest.header(headerName, headerValue);
            }
        }

        // Force the Host header if configured
        if (this._hostHeader != null)
            proxyRequest.header(HttpHeader.HOST, this._hostHeader);

        proxyRequest.content(contentProvider);
        this.customizeProxyRequest(proxyRequest, request);
        proxyRequest.timeout(this.getTimeout(), TimeUnit.MILLISECONDS);
        return proxyRequest;
    }

    protected void onResponseHeaders(HttpServletRequest request, HttpServletResponse response, Response proxyResponse)
    {
        for (HttpField field : proxyResponse.getHeaders())
        {
            String headerName = field.getName();
            String lowerHeaderName = headerName.toLowerCase(Locale.ENGLISH);
            if (HOP_HEADERS.contains(lowerHeaderName))
                continue;

            String newHeaderValue = this.filterResponseHeader(request, headerName, field.getValue());
            if ((newHeaderValue == null) || (newHeaderValue.trim().length() == 0))
                continue;

            response.addHeader(headerName, newHeaderValue);
        }
    }

    protected void onResponseContent(HttpServletRequest request, HttpServletResponse response, Response proxyResponse,
            byte[] buffer, int offset, int length) throws IOException
    {
        response.getOutputStream().write(buffer, offset, length);
        this._log.debug("{} proxying content to downstream: {} bytes", this.getRequestId(request), length);
    }

    protected void onResponseSuccess(HttpServletRequest request, HttpServletResponse response, Response proxyResponse)
    {
        AsyncContext asyncContext = (AsyncContext) request.getAttribute(ASYNC_CONTEXT);
        asyncContext.complete();
        this._log.debug("{} proxying successful", this.getRequestId(request));
    }

    protected void onResponseFailure(HttpServletRequest request, HttpServletResponse response, Response proxyResponse,
            Throwable failure)
    {
        this._log.debug(this.getRequestId(request) + " proxying failed", failure);
        if (!response.isCommitted())
        {
            if (failure instanceof TimeoutException)
                response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
            else
                response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        }
        AsyncContext asyncContext = (AsyncContext) request.getAttribute(ASYNC_CONTEXT);
        asyncContext.complete();
    }

    protected int getRequestId(HttpServletRequest request)
    {
        return System.identityHashCode(request);
    }

    protected URI rewriteURI(HttpServletRequest request)
    {
        if (!this.validateDestination(request.getServerName(), request.getServerPort()))
            return null;

        StringBuffer uri = request.getRequestURL();
        String query = request.getQueryString();
        if (query != null)
            uri.append("?").append(query);

        return URI.create(uri.toString());
    }

    /**
     * Extension point for subclasses to customize the proxy request.
     * The default implementation does nothing.
     *
     * @param proxyRequest the proxy request to customize
     * @param request the request to be proxied
     */
    protected void customizeProxyRequest(Request proxyRequest, HttpServletRequest request)
    {
    }

    /**
     * Extension point for remote server response header filtering.
     * The default implementation returns the header value as is.
     * If null is returned, this header won't be forwarded back to the client.
     *
     * @param headerName the header name
     * @param headerValue the header value
     * @param request the request to proxy
     * @return filteredHeaderValue the new header value
     */
    protected String filterResponseHeader(HttpServletRequest request, String headerName, String headerValue)
    {
        return headerValue;
    }

    /**
     * Transparent Proxy.
     * <p/>
     * This convenience extension to ProxyServlet configures the servlet as a transparent proxy.
     * The servlet is configured with init parameters:
     * <ul>
     * <li>proxyTo - a URI like http://host:80/context to which the request is proxied.
     * <li>prefix - a URI prefix that is striped from the start of the forwarded URI.
     * </ul>
     * For example, if a request is received at /foo/bar and the 'proxyTo' parameter is "http://host:80/context"
     * and the 'prefix' parameter is "/foo", then the request would be proxied to "http://host:80/context/bar".
     */
    public static class Transparent extends ProxyServlet
    {
        private String _proxyTo;
        private String _prefix;

        public Transparent()
        {
        }

        public Transparent(String proxyTo, String prefix)
        {
            this._proxyTo = URI.create(proxyTo).normalize().toString();
            this._prefix = URI.create(prefix).normalize().toString();
        }

        @Override
        public void init() throws ServletException
        {
            super.init();

            ServletConfig config = this.getServletConfig();

            String prefix = config.getInitParameter("prefix");
            this._prefix = prefix == null ? this._prefix : prefix;

            // Adjust prefix value to account for context path
            String contextPath = this.getServletContext().getContextPath();
            this._prefix = this._prefix == null ? contextPath : (contextPath + this._prefix);

            String proxyTo = config.getInitParameter("proxyTo");
            this._proxyTo = proxyTo == null ? this._proxyTo : proxyTo;

            if (this._proxyTo == null)
                throw new UnavailableException("Init parameter 'proxyTo' is required.");

            if (!this._prefix.startsWith("/"))
                throw new UnavailableException("Init parameter 'prefix' parameter must start with a '/'.");

            this._log.debug(config.getServletName() + " @ " + this._prefix + " to " + this._proxyTo);
        }

        @Override
        protected URI rewriteURI(HttpServletRequest request)
        {
            String path = request.getRequestURI();
            if (!path.startsWith(this._prefix))
                return null;

            StringBuilder uri = new StringBuilder(this._proxyTo);
            uri.append(path.substring(this._prefix.length()));
            String query = request.getQueryString();
            if (query != null)
                uri.append("?").append(query);
            URI rewrittenURI = URI.create(uri.toString()).normalize();

            if (!this.validateDestination(rewrittenURI.getHost(), rewrittenURI.getPort()))
                return null;

            return rewrittenURI;
        }
    }

    private class ProxyRequestHandler extends Response.Listener.Empty
    {
        // リトライのために記憶するデータ量
        private static final int RETRY_MAX_SIZE = 256 * 1024;

        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final URI targetUri;
        private final InputStream contentInputStream;

        private final ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();

        private boolean retryEnabled = true;

        public ProxyRequestHandler(HttpServletRequest request, HttpServletResponse response, URI targetUri)
                throws IOException
        {
            this.request = request;
            this.response = response;
            this.targetUri = targetUri;
            this.contentInputStream = request.getInputStream();
        }

        /**
         * retryEnabled の時だけだよ
         * @return
         */
        private ContentProvider createRetryContentProvider() {
            final int requestId = ProxyServlet.this.getRequestId(this.request);
            final HttpServletRequest request = this.request;

            return new InputStreamContentProvider(
                    new SequenceInputStream(new ByteArrayInputStream(this.contentBuffer.toByteArray()),
                            this.contentInputStream))
            {
                @Override
                public long getLength()
                {
                    return request.getContentLength();
                }

                @Override
                protected ByteBuffer onRead(byte[] buffer, int offset, int length)
                {
                    ProxyServlet.this._log
                            .debug("{} proxying content to upstream: {} bytes", requestId, length);
                    return super.onRead(buffer, offset, length);
                }
            };
        }

        public void send() {
            final int requestId = ProxyServlet.this.getRequestId(this.request);
            final HttpServletRequest request = this.request;
            final ByteArrayOutputStream contentBuffer = this.contentBuffer;

            Request proxyRequest = ProxyServlet.this.createProxyRequest(request, this.response, this.targetUri,
                    new InputStreamContentProvider(this.contentInputStream)
                    {
                        @Override
                        public long getLength()
                        {
                            return request.getContentLength();
                        }

                        @Override
                        protected ByteBuffer onRead(byte[] buffer, int offset, int length)
                        {
                            if (length > 0) {
                                if (contentBuffer.size() < RETRY_MAX_SIZE) {
                                    contentBuffer.write(buffer, offset, length);
                                }
                                else {
                                    // データが多すぎ、リトライ不可
                                    ProxyRequestHandler.this.retryEnabled = false;
                                }
                            }
                            ProxyServlet.this._log
                                    .debug("{} proxying content to upstream: {} bytes", requestId, length);
                            return super.onRead(buffer, offset, length);
                        }
                    });

            if (ProxyServlet.this._log.isDebugEnabled())
            {
                StringBuilder builder = new StringBuilder(this.request.getMethod());
                builder.append(" ").append(this.request.getRequestURI());
                String query = this.request.getQueryString();
                if (query != null)
                    builder.append("?").append(query);
                builder.append(" ").append(this.request.getProtocol()).append("\r\n");
                for (Enumeration<String> headerNames = this.request.getHeaderNames(); headerNames.hasMoreElements();)
                {
                    String headerName = headerNames.nextElement();
                    builder.append(headerName).append(": ");
                    for (Enumeration<String> headerValues = this.request.getHeaders(headerName); headerValues
                            .hasMoreElements();)
                    {
                        String headerValue = headerValues.nextElement();
                        if (headerValue != null)
                            builder.append(headerValue);
                        if (headerValues.hasMoreElements())
                            builder.append(",");
                    }
                    builder.append("\r\n");
                }
                builder.append("\r\n");

                ProxyServlet.this._log.debug("{} proxying to upstream:{}{}{}{}",
                        requestId,
                        System.lineSeparator(),
                        builder,
                        proxyRequest,
                        System.lineSeparator(),
                        proxyRequest.getHeaders().toString().trim());
            }

            proxyRequest.send(this);
        }

        @Override
        public void onBegin(Response proxyResponse)
        {
            // 返事があったらサーバ側での処理は完了しているのでリトライしない
            this.retryEnabled = false;

            this.response.setStatus(proxyResponse.getStatus());
        }

        @Override
        public void onHeaders(Response proxyResponse)
        {
            ProxyServlet.this.onResponseHeaders(this.request, this.response, proxyResponse);

            if (ProxyServlet.this._log.isDebugEnabled())
            {
                StringBuilder builder = new StringBuilder("\r\n");
                builder.append(this.request.getProtocol()).append(" ").append(this.response.getStatus()).append(" ")
                        .append(proxyResponse.getReason()).append("\r\n");
                for (String headerName : this.response.getHeaderNames())
                {
                    builder.append(headerName).append(": ");
                    for (Iterator<String> headerValues = this.response.getHeaders(headerName).iterator(); headerValues
                            .hasNext();)
                    {
                        String headerValue = headerValues.next();
                        if (headerValue != null)
                            builder.append(headerValue);
                        if (headerValues.hasNext())
                            builder.append(",");
                    }
                    builder.append("\r\n");
                }
                ProxyServlet.this._log.debug("{} proxying to downstream:{}{}{}{}{}",
                        ProxyServlet.this.getRequestId(this.request),
                        System.lineSeparator(),
                        proxyResponse,
                        System.lineSeparator(),
                        proxyResponse.getHeaders().toString().trim(),
                        System.lineSeparator(),
                        builder);
            }
        }

        @Override
        public void onContent(Response proxyResponse, ByteBuffer content)
        {
            byte[] buffer;
            int offset;
            int length = content.remaining();
            if (content.hasArray())
            {
                buffer = content.array();
                offset = content.arrayOffset();
            }
            else
            {
                buffer = new byte[length];
                content.get(buffer);
                offset = 0;
            }

            try
            {
                ProxyServlet.this.onResponseContent(this.request, this.response, proxyResponse, buffer, offset, length);
            } catch (IOException x)
            {
                proxyResponse.abort(x);
            }
        }

        @Override
        public void onSuccess(Response proxyResponse)
        {
            ProxyServlet.this.onResponseSuccess(this.request, this.response, proxyResponse);
        }

        private boolean isRetry(Throwable failure) {
            return this.retryEnabled &&
                    (failure instanceof EOFException) &&
                    (HttpVersion.fromString(this.request.getProtocol()) == HttpVersion.HTTP_1_1);
        }

        @Override
        public void onFailure(Response proxyResponse, Throwable failure)
        {
            if (!this.isRetry(failure)) {
                // リトライしない
                this.retryEnabled = false;
                ProxyServlet.this.onResponseFailure(this.request, this.response, proxyResponse, failure);
            }
        }

        @Override
        public void onComplete(Result result)
        {
            if (this.retryEnabled) {
                // 再度リトライはしない
                this.retryEnabled = false;
                ProxyServlet.this._log.debug("{} retrying proxy request", ProxyServlet.this.getRequestId(this.request));

                Request proxyRequest = ProxyServlet.this.createProxyRequest(this.request, this.response,
                        this.targetUri, this.createRetryContentProvider());
                proxyRequest.send(this);
            }
            else {
                ProxyServlet.this._log.debug("{} proxying complete", ProxyServlet.this.getRequestId(this.request));
            }
        }
    }
}
