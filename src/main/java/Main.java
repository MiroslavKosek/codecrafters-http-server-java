import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    try {
      ServerSocket serverSocket = new ServerSocket(4221);
    
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);
    
      Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
      System.out.println("accepted new connection");

      InputStream input = clientSocket.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(input));
      String line = reader.readLine();
      
      System.out.println(line);
      
      String[] HttpRequest = line.split(" ", 0);
      
      if (HttpRequest[1].equals("/")) {
        clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
      }
      else if (HttpRequest[1].startsWith("/echo/")) {
        String msg = HttpRequest[1].substring(6);
        String header = String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %d\r\n\r\n%s", msg.length(), msg);
        clientSocket.getOutputStream().write(header.getBytes());
      }
      else {
        clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
      }

      serverSocket.close();
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
