package logbook.server.proxy;

import logbook.config.AppConfig;
import logbook.gui.ApplicationMain;
import logbook.internal.LoggerHolder;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * プロキシサーバーです
 *
 */
public final class ProxyServer {

    private static final LoggerHolder LOG = new LoggerHolder(ProxyServer.class);

    private static Server server;

    private static String host;
    private static int port;
    private static String proxyHost;
    private static int proxyPort;

    public static void start() {
        try {
            QueuedThreadPool threadpool = new QueuedThreadPool();
            threadpool.setMinThreads(2);

            server = new Server(threadpool);
            updateSetting();
            setConnector();
            /*// httpsをプロキシできないので下のコードに移行
                        ServletHandler servletHandler = new ServletHandler();
                        servletHandler.addServletWithMapping(ReverseProxyServlet.class, "/*");
                        servletHandler.setServer(server);

                        server.setHandler(servletHandler);
            */
            // httpsをプロキシできるようにConnectHandlerを設定
            ConnectHandler proxy = new ConnectHandler();
            server.setHandler(proxy);

            // httpはこっちのハンドラでプロキシ
            ServletContextHandler context = new ServletContextHandler(proxy, "/", ServletContextHandler.SESSIONS);
            ServletHolder proxyServlet = new ServletHolder(new ReverseProxyServlet());
            proxyServlet.setInitParameter("timeout", "600000");
            context.addServlet(proxyServlet, "/*");

            server.start();
        } catch (Exception e) {
            LOG.get().fatal("Proxyサーバーの起動に失敗しました", e);
            throw new RuntimeException(e);
        }
    }

    public static void restart() {
        try {
            if (server == null) {
                return;
            }
            if (updateSetting()) {
                server.stop();
                setConnector();
                server.start();
                ApplicationMain.logPrint("プロキシサーバを再起動しました");
            }
        } catch (Exception e) {
            LOG.get().fatal("Proxyサーバーの起動に失敗しました", e);
            throw new RuntimeException(e);
        }
    }

    public static void end() {
        try {
            if (server != null) {
                server.stop();
                server.join();
                server = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * AppConfigの設定をローカルにコピーします。その際、更新があったか判定します。
     * @return 更新があった
     */
    private static boolean updateSetting() {
        String newHost = null;
        if (AppConfig.get().isAllowOnlyFromLocalhost() && AppConfig.get().isCloseOutsidePort()) {
            newHost = "localhost";
        }
        int newPort = AppConfig.get().getListenPort();
        String newProxyHost = null;
        int newProxyPort = 0;
        if (AppConfig.get().isUseProxy()) {
            newProxyHost = AppConfig.get().getProxyHost();
            newProxyPort = AppConfig.get().getProxyPort();
        }

        if (StringUtils.equals(newHost, host) && (newPort == port) &&
                StringUtils.equals(newProxyHost, proxyHost) && (newProxyPort == proxyPort)) {
            return false;
        }

        host = newHost;
        port = newPort;
        proxyHost = newProxyHost;
        proxyPort = newProxyPort;
        return true;
    }

    private static void setConnector() {
        ServerConnector connector = new ServerConnector(server, 1, 1);
        connector.setPort(port);
        connector.setHost(host);
        server.setConnectors(new Connector[] { connector });
    }
}
