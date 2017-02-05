package it.ozimov.springboot.templating.mail;

import org.springframework.util.SocketUtils;

import java.io.IOException;

public class PortUtils {

    public static int randomFreePort() throws IOException {
        return SocketUtils.findAvailableTcpPort();
    }

}