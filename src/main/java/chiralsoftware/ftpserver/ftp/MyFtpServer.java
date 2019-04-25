package chiralsoftware.ftpserver.ftp;

import org.apache.ftpserver.listener.ListenerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Objects;
import static java.util.logging.Level.WARNING;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;

/**
 * 
 */
@Component
public class MyFtpServer {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(MyFtpServer.class.getName());

    private FtpServer ftpServer;

    @Value("${ftp.server.host:localhost}")
    private String host;
    @Value("${ftp.server.port:2121}")
    private int port;
    @Value("${ftp.server.passive-ports:5050-5070}")
    private String passivePorts;
    @Value("${ftp.max-login:20}")
    private Integer maxLogin;
    @Value("${ftp.max-threads:100}")
    private Integer maxThreads;

    @PostConstruct
    public void start() {

        final FtpServerFactory serverFactory = new FtpServerFactory();

        final ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(false);
        connectionConfigFactory.setMaxLogins(maxLogin);
        connectionConfigFactory.setMaxThreads(maxThreads);
        serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());

        final ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(port);
        if (!Objects.equals(passivePorts, "")) {
            final DataConnectionConfigurationFactory dataConnectionConfFactory = 
                    new DataConnectionConfigurationFactory();
            LOG.info("couldn't open passive ports: " + passivePorts);
            dataConnectionConfFactory.setPassivePorts(passivePorts);
            if (!(Objects.equals(host, "localhost") || Objects.equals(host, "127.0.0.1"))) {
                LOG.info("host problem: " + host);
                dataConnectionConfFactory.setPassiveExternalAddress(host);
            }
            listenerFactory.setDataConnectionConfiguration(
                    dataConnectionConfFactory.createDataConnectionConfiguration());
        }

        serverFactory.addListener("default", listenerFactory.createListener());
        serverFactory.setUserManager(new MyUserManager(Map.of("bob", "password")));

        ftpServer = serverFactory.createServer();
        try {
            ftpServer.start();
        } catch (FtpException e) {
            LOG.log(WARNING, "ftp", e);
            throw new RuntimeException(e);
        }
        LOG.info("ftp: " + port);
    }

    @PreDestroy
    public void stop() {
        if (ftpServer != null) {
            ftpServer.stop();
        }
    }

}
