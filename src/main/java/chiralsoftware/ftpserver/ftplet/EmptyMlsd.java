package chiralsoftware.ftpserver.ftplet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import org.apache.ftpserver.command.AbstractCommand;
import org.apache.ftpserver.command.impl.listing.ListArgument;
import static org.apache.ftpserver.command.impl.listing.ListArgumentParser.parse;
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
import static org.apache.ftpserver.impl.LocalizedDataTransferFtpReply.translate;

/**
 * Implement the MLSD command and always return an empty directory
 */
public class EmptyMlsd extends AbstractCommand {

    private static final Logger LOG = Logger.getLogger(EmptyMlsd.class.getName());

    @Override
    public void execute(FtpIoSession session,
            FtpServerContext context, FtpRequest request)
            throws IOException, FtpException {
        LOG.info("Returning an empty list");
        try {
            // reset state variables
            session.resetState();

            // parse argument
            final ListArgument parsedArg = parse(request.getArgument());
            final DataConnectionFactory connFactory = session.getDataConnection();
            if (connFactory instanceof IODataConnectionFactory) {
                final InetAddress address = ((IODataConnectionFactory) connFactory)
                        .getInetAddress();
                if (address == null) {
                    session.write(new DefaultFtpReply(
                            REPLY_503_BAD_SEQUENCE_OF_COMMANDS,
                            "PORT or PASV must be issued first"));
                    return;
                }
            }

            // get data connection
            session.write(translate(session, request, context,
                    REPLY_150_FILE_STATUS_OKAY, "MLSD", null));
            final VirtualFtpFile virtualFtpFile
                    = new VirtualFtpFile(parsedArg.getFile());

            DataConnection dataConnection;
            try {
                dataConnection = session.getDataConnection().openConnection();
            } catch (Exception e) {
                LOG.log(INFO, "Exception getting the output data stream", e);
                session.write(translate(session, request, context,
                        REPLY_425_CANT_OPEN_DATA_CONNECTION, "MLSD",
                        null, virtualFtpFile));
                return;
            }

            // transfer listing data
            boolean failure = false;
            try {
                dataConnection.transferToClient(session.getFtpletSession(), "");
            } catch (SocketException ex) {
                LOG.log(INFO, "Socket exception during list transfer", ex);
                failure = true;
                session.write(translate(session, request, context,
                        REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED,
                        "MLSD", null, virtualFtpFile));
            } catch (IOException ex) {
                LOG.log(INFO, "IOException during list transfer", ex);
                failure = true;
                session.write(translate(session, request, context,
                        REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN,
                        "MLSD", null, virtualFtpFile));
            } catch (IllegalArgumentException e) {
                LOG.log(INFO, "Illegal list syntax: " + request.getArgument(), e);
                // if listing syntax error - send message
                session.write(translate(session, request,
                        context,
                        REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                        "MLSD", null, virtualFtpFile));
            }

            // if data transfer ok - send transfer complete message
            if (!failure) {
                session.write(translate(session, request, context,
                        REPLY_226_CLOSING_DATA_CONNECTION, "MLSD",
                        null, virtualFtpFile, 0));
            }
        } finally {
            session.getDataConnection().closeDataConnection();
        }
    }

}
