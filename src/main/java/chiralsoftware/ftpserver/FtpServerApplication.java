package chiralsoftware.ftpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FtpServerApplication {

    /**
     * Entry Method
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FtpServerApplication.class, args);
    }
}
