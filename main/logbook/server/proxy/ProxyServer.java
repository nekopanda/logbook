package logbook.server.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * プロキシサーバーです
 *
 */
public final class ProxyServer {

    private static final Logger LOG = LogManager.getLogger(ProxyServer.class);

    private static Server server;

    public static void start(int port) {
        try {
            QueuedThreadPool threadpool = new QueuedThreadPool();
            threadpool.setMinThreads(2);

            server = new Server(threadpool);

            ServerConnector connector = new ServerConnector(server, 1, 1);
            connector.setPort(port);
            server.setConnectors(new Connector[] { connector });

            ServletHandler servletHandler = new ServletHandler();
            servletHandler.addServletWithMapping(ReverseProxyServlet.class, "/*");
            servletHandler.setServer(server);

            server.setHandler(servletHandler);

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
