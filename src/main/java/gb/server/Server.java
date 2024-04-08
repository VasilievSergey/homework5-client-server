package gb.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Server {
    public static final int PORT = 8181;

    public static void main(String[] args) {
        final Map<String, ClientHandler> clients = new HashMap<>();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Подключился новый клиент: " + clientSocket);

                    PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientOut.println("Подключение успешно. Пришлите свой идентификатор");

                    Scanner clientIn = new Scanner(clientSocket.getInputStream());
                    String clientId = clientIn.nextLine();
                    System.out.println("Идентификатор клиента " + clientSocket + ": " + clientId);

                    String allClients = clients.entrySet().stream()
                            .map(it -> "id = " + it.getKey() + ", client = " + it.getValue().getClientSocket())
                            .collect(Collectors.joining("\n"));
                    clientOut.println("Список доступных клиентов: \n" + allClients);

                    ClientHandler clientHandler = new ClientHandler(clientSocket, clients);
                    new Thread(clientHandler).start();
                    for (ClientHandler client : clients.values()) {
                        client.send("Подключился новый клиент: " + clientSocket + ", id = " + clientId);
                    }
                    clients.put(clientId, clientHandler);
                } catch (IOException e) {
                    System.err.println("Произошла ошибка при взаимодействии с клиентом: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось начать прослушивать порт " + PORT, e);
        }
    }

}