package chiralsoftware.ftpserver.ftplet;

import java.io.IOException;
import java.util.logging.Logger;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;

/**
 * Very basic Ftplet demonstration
 */
public final class SimpleFtplet extends DefaultFtplet {

    private static final Logger LOG = Logger.getLogger(SimpleFtplet.class.getName());

    @Override
    public void init(FtpletContext arg0) throws FtpException {
        LOG.info("Ftplet init() called");
    }

    @Override
    public void destroy() {
        LOG.info("Ftplet destroy() called");
    }

    @Override
    public FtpletResult beforeCommand(FtpSession session, FtpRequest request) throws FtpException, IOException {
        LOG.info("Ftplet beforeCommand called: " + request.getRequestLine());
        return null;
    }

    @Override
    public FtpletResult afterCommand(FtpSession arg0, FtpRequest arg1, FtpReply arg2) throws FtpException, IOException {
        LOG.info("afterCommand");
        return null;
    }

    @Override
    public FtpletResult onConnect(FtpSession arg0) throws FtpException, IOException {
        LOG.info("Got onConnect");
        return null;
    }

    @Override
    public FtpletResult onDisconnect(FtpSession arg0) throws FtpException, IOException {
        LOG.info("Got onDisconnect");
        return null;
    }
    
}
