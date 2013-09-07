package logbook.server.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * プロキシサーバーです
 *
 */
public final class ProxyServer extends Thread {

    private static final Logger LOG = LogManager.getLogger(ProxyServer.class);

    private static ProxyServer proxyServer;

    private final int port;

    private Server server;

    private ProxyServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            this.server = new Server(this.port);

            ServletHandler servletHandler = new ServletHandler();
            servletHandler.addServletWithMapping(ReverseProxyServlet.class, "/*");

            this.server.setHandler(servletHandler);

            this.server.start();
            this.server.join();
        } catch (Exception e) {
            LOG.fatal("サーバーの起動に失敗しました", e);
        }
    }

    private void shutdown() {
        try {
            this.server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void start(int port) {
        proxyServer = new ProxyServer(port);
        proxyServer.setDaemon(true);
        proxyServer.start();
    }

    public static void end() {
        proxyServer.shutdown();
    }
}
