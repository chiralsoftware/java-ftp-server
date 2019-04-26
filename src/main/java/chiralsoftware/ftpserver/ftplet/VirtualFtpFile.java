package chiralsoftware.ftpserver.ftplet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.security.MessageDigest.getInstance;
import java.security.NoSuchAlgorithmException;
import static java.util.Collections.EMPTY_LIST;
import java.util.List;
import java.util.logging.Logger;
import org.apache.ftpserver.ftplet.FtpFile;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

/**
 *
 */
public final class VirtualFtpFile implements FtpFile {

    private static final Logger LOG = Logger.getLogger(VirtualFtpFile.class.getName());
    
    private final String fileName;
    
    public VirtualFtpFile(String fileName) {
        if(fileName == null) throw new NullPointerException("Can't create a VirtualFtpFile with null fileName");
        this.fileName = fileName;
    }

    @Override
    public String getAbsolutePath() {
        return fileName;
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean doesExist() {
        return true;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public String getOwnerName() {
        return "ftp";
    }

    @Override
    public String getGroupName() {
        return "nogroup";
    }

    @Override
    public int getLinkCount() {
        return 1;
    }

    @Override
    public long getLastModified() {
        return System.currentTimeMillis();
    }

    @Override
    public boolean setLastModified(long arg0) {
        return true;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public Object getPhysicalFile() {
        return null;
    }

    @Override
    public boolean mkdir() {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean move(FtpFile arg0) {
        return false;
    }

    @Override
    public List<? extends FtpFile> listFiles() {
        return EMPTY_LIST;
    }

    @Override
    public OutputStream createOutputStream(long arg0) throws IOException {
        LOG.info("Trying to create an outputStream for file name: " + fileName);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InputStream createInputStream(long arg0) throws IOException {
        LOG.info("Trying to create an inputStream for file name: " + fileName);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static String hash(byte[] bytes) throws IOException {
        try {
            return printHexBinary(getInstance("MD5").digest(bytes));
        } catch(NoSuchAlgorithmException nsae) {
            throw new IOException("Couldn't find the MD5 algorithm, your JRE is broken", nsae);
        }
    }
    
}
