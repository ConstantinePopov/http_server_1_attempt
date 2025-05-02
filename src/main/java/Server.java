import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int socketNumber;
    ExecutorService threadPool;
    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");


    public Server(int socketNumber, int numberOfThreads) {
        this.socketNumber = socketNumber;
        this.threadPool = Executors.newFixedThreadPool(numberOfThreads);
    }

    public void start() throws IOException {
        try (final var serverSocket = new ServerSocket(socketNumber)) {
            while (true) {
                try {
                    final var socket = serverSocket.accept();
                    Task task = new Task(socket, validPaths);
                    threadPool.submit(() -> task.run());
                } catch (IOException e) {
                    System.out.println("Что-то не так с сервером...");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            threadPool.shutdown();
        }

    }
}