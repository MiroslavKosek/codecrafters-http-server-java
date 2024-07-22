import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  public static void main(String[] args) {
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
    try (clientSocket) {
      InputStream input = clientSocket.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(input));
      String line = reader.readLine();
      
      System.out.println(line);
      
      String[] HttpRequest = line.split(" ", 0);

      OutputStream output = clientSocket.getOutputStream();
      String[] str = HttpRequest[1].split("/");

      if (HttpRequest[1].equals("/")) {
        System.out.println("version");
        String response = "HTTP/1.1 200 OK\r\n"
                          + "Content-Type: text/plain\r\n"
                          + "Content-Length: 0\r\n\r\n";
        output.write(response.getBytes());
      }
      else if (str[1].equals("user-agent")) {
        reader.readLine();
        String useragent = reader.readLine().split("\\s+")[1];
        String reply = String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %s\r\n\r\n%s\r\n", useragent.length(), useragent);
        output.write(reply.getBytes());
      }
      else if ((str.length > 2 && str[1].equals("echo"))) {
        String responsebody = str[2];
        String finalstr = "HTTP/1.1 200 OK\r\n"
                          + "Content-Type: text/plain\r\n"
                          + "Content-Length: " + responsebody.length() +
                          "\r\n\r\n" + responsebody;
        output.write(finalstr.getBytes());
      }
      else {
        output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
      }

      output.flush();
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
