package chiralsoftware.ftpserver.ftplet;

import static chiralsoftware.ftpserver.ftplet.VirtualFtpFile.hash;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import org.apache.ftpserver.command.AbstractCommand;
import org.apache.ftpserver.ftplet.DataConnection;
import org.apache.ftpserver.ftplet.DataConnectionFactory;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import org.apache.ftpserver.ftplet.FtpException;
import static org.apache.ftpserver.ftplet.FtpReply.REPLY_150_FILE_STATUS_OKAY;
import static org.apache.ftpserver.ftplet.FtpReply.REPLY_226_CLOSING_DATA_CONNECTION;
import static org.apache.ftpserver.ftplet.FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION;
import static org.apache.ftpserver.ftplet.FtpReply.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED;
import static org.apache.ftpserver.ftplet.FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS;
import static org.apache.ftpserver.ftplet.FtpReply.REPLY_503_BAD_SEQUENCE_OF_COMMANDS;
import static org.apache.ftpserver.ftplet.FtpReply.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.IODataConnectionFactory;
import org.apache.ftpserver.impl.LocalizedDataTransferFtpReply;
import org.apache.ftpserver.impl.LocalizedFtpReply;

/**
 * Implement a STOR command that can process data in ways other than writing to
 * the filesystem
 */
public class StreamStor extends AbstractCommand {

    private static final Logger LOG = Logger.getLogger(StreamStor.class.getName());

    @Override
    public void execute(final FtpIoSession session,
            final FtpServerContext context, final FtpRequest request)
            throws IOException, FtpException {
        final String fileName = request.getArgument();
        LOG.info("File name from command: " + fileName);

        if (fileName == null) {
            LOG.warning("fileName was null so sending syntax error");
            session.write(LocalizedDataTransferFtpReply
                    .translate(session, request, context,
                            REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                            "STOR", null, null));
            return;
        }
        final DataConnectionFactory connFactory = session.getDataConnection();
        if (connFactory instanceof IODataConnectionFactory) {
            final InetAddress address = ((IODataConnectionFactory) connFactory).getInetAddress();
            if (address == null) {
                session.write(new DefaultFtpReply(REPLY_503_BAD_SEQUENCE_OF_COMMANDS,
                        "PORT or PASV must be issued first"));
                return;
            }
        }
        // get data connection
        session.write(LocalizedFtpReply.translate(session, request, context,
                REPLY_150_FILE_STATUS_OKAY, "STOR",
                fileName)).awaitUninterruptibly(10000);
        final DataConnection dataConnection;
        try {
            dataConnection = session.getDataConnection().openConnection();
        } catch (Exception e) {
            LOG.log(INFO, "Couldn't get the input data stream", e);
            session.write(LocalizedDataTransferFtpReply.translate(session, request, context,
                    REPLY_425_CANT_OPEN_DATA_CONNECTION, "STOR",
                    fileName, new VirtualFtpFile(fileName)));
            return;
        }
        boolean failure = false;
        long transferSize = -1;
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transferSize = dataConnection.transferFromClient(session.getFtpletSession(), baos);
            baos.close();
            final byte[] bytes = baos.toByteArray();
            LOG.info("Yeehaw, I transfered: " + bytes.length + " bytes, which should equal: " + transferSize
                    + " and has md5=" + hash(bytes));
        } catch (SocketException se) {
            LOG.log(INFO, "Socket closed, didn't complete transfer", se);
            failure = true;
            session.write(LocalizedDataTransferFtpReply.translate(session, request, context,
                    REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED,
                    "STOR", fileName, new VirtualFtpFile(fileName)));
        } catch (IOException ioe) {
            failure = true;
            session.write(LocalizedDataTransferFtpReply
                    .translate(session, request, context,
                            REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN,
                            "STOR", fileName, new VirtualFtpFile(fileName)));
        }
        if (!failure) {
            session.write(LocalizedDataTransferFtpReply.translate(session, request, context,
                    REPLY_226_CLOSING_DATA_CONNECTION, "STOR",
                    fileName, new VirtualFtpFile(fileName), transferSize));

        }
        session.resetState();
        session.getDataConnection().closeDataConnection();
        LOG.info("I'm done.");
    }

}
