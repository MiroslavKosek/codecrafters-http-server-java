import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  private static String directory;

  public static void main(String[] args) {
    if (args.length > 1 && args[0].equals("--directory")) {
      directory = args[1];
    }

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    try (ServerSocket serverSocket = new ServerSocket(4221)) {
      serverSocket.setReuseAddress(true);
      System.out.println("Server is listening on port 4221");

      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Accepted new connection");
        executorService.submit(() -> handleClient(clientSocket));
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }

  private static void handleClient(Socket clientSocket) {
    try {
      BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

      String requestLine = inputStream.readLine();

      Map<String, String> headers = new HashMap<>();
      String headerLine;
      while (!(headerLine = inputStream.readLine()).isEmpty()) {
        String[] headerParts = headerLine.split(": ");
        headers.put(headerParts[0], headerParts[1]);
      }

      String urlPath = requestLine.split(" ")[1];
      OutputStream outputStream = clientSocket.getOutputStream();

      String httpResponse = getHttpResponse(urlPath, headers);
      outputStream.write(httpResponse.getBytes("UTF-8"));

      inputStream.close();
      outputStream.close();
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      try {
        if (clientSocket != null) {
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println("IOException: " + e.getMessage());
      }
    }
  }

  private static String getHttpResponse(String urlPath, Map<String, String> headers) {
    try {
      String httpResponse;
      
      if ("/".equals(urlPath)) {
        httpResponse = "HTTP/1.1 200 OK\r\n\r\n";
      }
      else if (urlPath.startsWith("/echo/")) {
        String echoStr = urlPath.substring(6);
        httpResponse =
            "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
            echoStr.length() + "\r\n\r\n" + echoStr;
      }
      else if ("/user-agent".equals(urlPath)) {
        String userAgent = headers.get("User-Agent");
        httpResponse =
            "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
            userAgent.length() + "\r\n\r\n" + userAgent;
      }
      else if (urlPath.startsWith("/files/")) {
        String filename = urlPath.substring(7);
        File file = new File(directory, filename);
        if (file.exists()) {
          byte[] fileContent = Files.readAllBytes(file.toPath());
          httpResponse =
              "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: " +
              fileContent.length + "\r\n\r\n" + new String(fileContent);
        }
        else {
          httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
        }
      }
      else {
        httpResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
      }
      
      return httpResponse;
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
    
    return urlPath;
  }
}