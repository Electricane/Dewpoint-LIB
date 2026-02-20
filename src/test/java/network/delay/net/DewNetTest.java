package network.delay.net;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.ServerSocket;
import static org.junit.jupiter.api.Assertions.*;

class DewNetTest {

    @Test
    void testFindFreePort() throws IOException {
        int port = DewNet.findFreePort();
        
        assertTrue(port > 0 && port <= 65535, "Port should be within valid range");
        
        try (ServerSocket socket = new ServerSocket(port)) {
            assertTrue(socket.isBound());
        }
    }

    @Test
    void testIsReachable() throws IOException {
        int port = DewNet.findFreePort();
        
        assertFalse(DewNet.isReachable("localhost", port, 100));
        
        try (ServerSocket server = new ServerSocket(port)) {
            assertTrue(DewNet.isReachable("localhost", port, 500));
        }
    }

    @Test
    void testGetHostname() {
        String hostname = DewNet.getHostname();
        assertNotNull(hostname);
        assertFalse(hostname.isEmpty());
        assertNotEquals("unknown", hostname, "Hostname should ideally be resolved on most systems");
    }

    @Test
    void testReadUrlInvalid() {
        assertThrows(IOException.class, () -> {
            DewNet.readUrl("http://this.domain.does.not.exist.at.all.com");
        });
    }
}