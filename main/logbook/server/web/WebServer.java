/**
 * 
 */
package logbook.server.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * Webサーバです
 *
 */
public class WebServer {

    private static final Logger LOG = LogManager.getLogger(WebServer.class);

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
            servletHandler.addServletWithMapping(QueryHandler.class, "/query");
            servletHandler.addServletWithMapping(QueryHandler.class, "/battle");

            server.setHandler(servletHandler);

            server.start();
        } catch (Exception e) {
            LOG.fatal("Webサーバーの起動に失敗しました", e);
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
