package chiralsoftware.ftpserver.controllers;

import chiralsoftware.ftpserver.ftp.MyFtpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;


/**
 * Add some rest interfaces here to modify operations of the server if needed
 */
@RestController
public class FtpController {

    @Autowired
    private MyFtpServer myFtpServer;

}
