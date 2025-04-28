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
    private int numberOfThreads;
    ExecutorService threadPool;

    public Server(int socketNumber, int numberOfThreads) {
        this.socketNumber = socketNumber;
        this.numberOfThreads = numberOfThreads;
    }

    public void start(List validPaths) throws IOException {
        try (final var serverSocket = new ServerSocket(socketNumber)) {
            threadPool = Executors.newFixedThreadPool(numberOfThreads);
            while (true) {
                try (
                        final var socket = serverSocket.accept();
                        final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        final var out = new BufferedOutputStream(socket.getOutputStream());
                ) {
                    Task task = new Task(socket, in, out, validPaths);
                    threadPool.execute(task);
                } catch (IOException e) {
                    System.out.println("Что-то не так с сервером...");
                } finally {
                    threadPool.shutdown();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}