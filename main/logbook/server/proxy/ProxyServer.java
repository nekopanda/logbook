package logbook.server.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger LOG = LogManager.getLogger(ProxyServer.class);

    private static Server server;

    public static void start(int port, String host) {
        try {
            QueuedThreadPool threadpool = new QueuedThreadPool();
            threadpool.setMinThreads(2);

            server = new Server(threadpool);

            ServerConnector connector = new ServerConnector(server, 1, 1);
            connector.setPort(port);
            connector.setHost(host);
            server.setConnectors(new Connector[] { connector });
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
            context.addServlet(proxyServlet, "/*");

            server.start();
        } catch (Exception e) {
            LOG.fatal("Proxyサーバーの起動に失敗しました", e);
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
}
