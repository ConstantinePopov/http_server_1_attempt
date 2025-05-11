import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Request {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private List<NameValuePair> params;

    public Request() {

    }

    public Request(List<NameValuePair> params) {
        this.params = params;
    }

    public void parseRequest(BufferedInputStream in) throws IOException, URISyntaxException {

        final var allowedMethods = List.of(GET, POST);
        // лимит на request line + заголовки
        final var limit = 4096;
        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            return;
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return;
        }

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            return;
        }
        System.out.println(method);

        final var path = requestLine[1];
        if (!path.startsWith("/")) {
            return;
        }
        System.out.println(path);

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            return;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        System.out.println(headers);

        //парсим реквест с помощью URLEncodedUtils.parse

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(path), StandardCharsets.UTF_8);

    }

    public List<NameValuePair> getQueryParams() {
        return params;
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}