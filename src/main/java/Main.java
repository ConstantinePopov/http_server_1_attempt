import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        Server server = new Server(9999, 64);
        server.start();

    }
}
