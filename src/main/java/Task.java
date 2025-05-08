import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Task {
    Socket socket;
    BufferedReader in;
    BufferedOutputStream out;
    List validPaths;

    public Task(Socket socket, List validPaths) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.validPaths = validPaths;
    }

    public void run() {
        try (
                final var dataFromSocket = socket.getInputStream();
                final var in = new BufferedReader(new InputStreamReader(dataFromSocket));
                final var inn = new BufferedInputStream(dataFromSocket);
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            final var parts = in.readLine().split(" ");

            if (parts.length != 3) {
                //socket.close();
            } else {
                final var path = parts[1];
                if (!validPaths.contains(path)) {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                } else {
                    final var filePath = Path.of(".", "public", path);
                    final var mimeType = Files.probeContentType(filePath);

                    // special case for classic
                    if (path.equals("/classic.html")) {
                        final var template = Files.readString(filePath);
                        final var content = template.replace(
                                "{time}",
                                LocalDateTime.now().toString()
                        ).getBytes();
                        out.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + content.length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        out.write(content);
                        out.flush();
                    } else {
                        final var length = Files.size(filePath);
                        out.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        Files.copy(filePath, out);
                        out.flush();
                    }
                }
            }
            Request request = new Request();
            request.parseRequest(inn);
        } catch (IOException e) {
            System.out.println("Задача не выполняется.");
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
