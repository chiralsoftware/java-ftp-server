package chiralsoftware.ftpserver.ftp;

import static java.util.List.of;
import java.util.Map;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toUnmodifiableList;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.AbstractUserManager;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

/**
 * Simple UserManager implementation for test purposes. This takes users defined in a Map
 * supplied in the constructor
 */
final class MyUserManager extends AbstractUserManager {

    private static final Logger LOG = Logger.getLogger(MyUserManager.class.getName());
    
    private final Map<String,String> userMap;
    
    MyUserManager(Map<String,String> userMap) {
        this.userMap = userMap;
    }

    @Override
    public User getUserByName(String name) throws FtpException {
        if(name == null) throw new IllegalArgumentException("name is null");
        
        if(! userMap.containsKey(name)) return null;
        
        final BaseUser user = new BaseUser();
        user.setEnabled(true);
        user.setHomeDirectory("/tmp");
        user.setName(name);
        user.setAuthorities(of(new WritePermission(), new ConcurrentLoginPermission(20, 20)));
        user.setPassword(userMap.get(name));
        user.setMaxIdleTime(60 * 10); // ten minutes
        return user;
    }

    private static final String[] emptyStringArray = new String[] { };
    
    @Override
    public String[] getAllUserNames() throws FtpException {
        return userMap.keySet().stream().sorted().collect(toUnmodifiableList()).toArray(emptyStringArray);
    }

    @Override
    public void delete(String name) throws FtpException {
        throw new UnsupportedOperationException("This is not a supported operation"); 
    }

    @Override
    public void save(User arg0) throws FtpException {
        throw new UnsupportedOperationException("Can't save user");
    }

    @Override
    public boolean doesExist(String name) throws FtpException {
        return userMap.containsKey(name);
    }

    @Override
    public User authenticate(Authentication authentication) throws AuthenticationFailedException {
        if(authentication == null)
            throw new IllegalArgumentException("authentication object can't be null");
        
        if(authentication instanceof AnonymousAuthentication) 
            throw new AuthenticationFailedException("Anonymous not supported");
        if(! (authentication instanceof UsernamePasswordAuthentication)) {
            LOG.warning("Attempting to authenticate with authentication class: " + authentication.getClass().getName() + " "
                    + "which is not an instance of: " + UsernamePasswordAuthentication.class.getName());
            throw new AuthenticationFailedException("Unexpected authentication class: " + authentication.getClass().getName());
        }
        final UsernamePasswordAuthentication upa = (UsernamePasswordAuthentication) authentication;
        if(! userMap.containsKey(upa.getUsername())) 
            throw new AuthenticationFailedException("User not found");
        if(! userMap.get(upa.getUsername()).equals(upa.getPassword()))
            throw new AuthenticationFailedException("Password was invalid");
        try {
            return getUserByName(upa.getUsername());
        } catch (FtpException ex) {
            LOG.log(SEVERE, "Some problem happened", ex);
            throw new AuthenticationFailedException("unknown problem", ex);
        }
    }

    @Override
    public String getAdminName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isAdmin(String name) throws FtpException {
        LOG.info("Checking if user: " + name + " is admin, always returning false");
        return false;
    }
    
}
