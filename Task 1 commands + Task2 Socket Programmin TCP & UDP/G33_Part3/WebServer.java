import java.io.*;
import java.net.*;
import java.nio.file.Files;

import static java.lang.System.out;

public class WebServer {
    private static final int PORT = 1039;
    private static final String ROOT_DIR = "C:\\Users\\a\\IdeaProjects\\SocketProgramming\\out\\production\\SocketProgramming\\web";
    private static final String ROOT_IMG = "C:\\Users\\a\\IdeaProjects\\SocketProgramming\\out\\production\\SocketProgramming\\web\\Images";

    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("0.0.0.0"));
            out.println("Server is listening on port " + PORT + "...");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (!serverSocket.isClosed()) {
                        serverSocket.close();
                        out.println("Server socket closed.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                handleClientRequest(clientSocket);
            }
        } catch (BindException e) {
            System.err.println("Server exception: Address already in use. Port " + PORT + " might be occupied.");
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClientRequest(Socket clientSocket) {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
             BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream())) {

            String inputLine = input.readLine();
            if (inputLine == null) return;

            System.out.println("HTTP Request: " + inputLine);

            String[] requestParts = inputLine.split(" ");
            String method = requestParts[0];
            String fileRequested = requestParts[1];

            if (!method.equals("GET")) {
                send404(out, clientSocket);
                return;
            }

            if (fileRequested.equals("/")) {
                fileRequested = "/index.html";
            }

            // Remove any query strings from the fileRequested path
            int queryIndex = fileRequested.indexOf("?");
            if (queryIndex != -1) {
                fileRequested = fileRequested.substring(0, queryIndex);
            }

            File file = new File(ROOT_DIR + fileRequested);
            String contentType = getContentType(fileRequested);

            if (file.exists()) {
                byte[] fileData = Files.readAllBytes(file.toPath());
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: " + contentType);
                out.println("Content-Length: " + fileData.length);
                out.println("");
                out.flush();
                dataOut.write(fileData, 0, fileData.length);
                dataOut.flush();
            } else {
                File imgFile = new File(ROOT_IMG + fileRequested);
                if (imgFile.exists()) {
                    byte[] fileData = Files.readAllBytes(imgFile.toPath());
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: " + contentType);
                    out.println("Content-Length: " + fileData.length);
                    out.println("");
                    out.flush();
                    dataOut.write(fileData, 0, fileData.length);
                    dataOut.flush();
                } else {
                    send404(out, clientSocket);
                }
            }

        } catch (IOException e) {
            System.err.println("Client request handling exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html")) {
            return "text/html";
        } else if (fileRequested.endsWith(".css")) {
            return "text/css";
        } else if (fileRequested.endsWith(".png")) {
            return "image/png";
        } else if (fileRequested.endsWith(".jpg")) {
            return "image/jpeg";
        } else {
            return "text/plain";
        }
    }

    private static void send404(PrintWriter out, Socket clientSocket) {
        String ipAddress = clientSocket.getInetAddress().getHostAddress();
        String response = "<html><head><title>Error 404</title></head>"
                + "<body><h1 style='color: blue;'>The file is not found</h1>"
                + "<p><strong>Name: Wasfia Awwad</strong><br><strong>ID: 1211039</strong></p>"
                + "<p>Client IP: " + ipAddress + "<br>Port: " + PORT + "</p>"
                + "</body></html>";

        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: text/html");
        out.println("Content-Length: " + response.length());
        out.println("");
        out.println(response);
        out.flush();
    }
}
