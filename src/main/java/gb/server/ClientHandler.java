package gb.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final PrintWriter out;
    private final Map<String, ClientHandler> clients;

    public ClientHandler(Socket clientSocket, Map<String, ClientHandler> clients) throws IOException {
        this.clientSocket = clientSocket;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.clients = clients;
    }
    public Socket getClientSocket() {
        return clientSocket;
    }

    @Override
    public void run() {
        try (Scanner in = new Scanner(clientSocket.getInputStream())) {
            while (!clientSocket.isClosed()) {
                try {
                    String input = in.nextLine();
                    messageCreator(input);
                } catch (RuntimeException e) {
                    closeClientSocket();
                }
            }
        } catch (IOException e) {
            System.err.println("Произошла ошибка при взаимодействии с клиентом " + clientSocket + ": " + e.getMessage());
        }
    }
    public void send(String msg) {
        out.println(msg);
    }
    private void messageCreator(String input) {
        System.out.println(clientSocket + ": " + input);
        switch (input) {
            case "/exit": closeClientSocket();
                break;
            case "/all": {
                String allClients = "Список доступных клиентов: \n" + getAllClients();
                sendAddressMessage(getClientId(), allClients);
            }
            break;
            default: {
                String toClientId = getClientIdFromInput(input);
                if (toClientId == null) {
                    broadcastMessage(input);
                } else {
                    sendAddressMessage(toClientId, input);
                }
            }
        }
    }

    private String getAllClients() {
        return clients.entrySet().stream()
                .map(it -> "id = " + it.getKey() + ", client = " + it.getValue().getClientSocket())
                .collect(Collectors.joining("\n"));
    }

    private void broadcastMessage(String input) {
        clients.values().forEach(it ->
                {
                    if (it.clientSocket.equals(clientSocket)) it.send("Вы: " + input);
                    else it.send(clientSocket + ": " + input);
                }
        );
    }

    private String getClientId() {
        return clients.entrySet().stream()
                .filter(it-> it.getValue().equals(this))
                .map(Map.Entry::getKey).findAny().orElse(null);
    }

    private String getClientIdFromInput(String input) {
        String toClientId = null;
        if (input.startsWith("@")) {
            String[] parts = input.split("\\s+");
            if (parts.length > 0) {
                toClientId = parts[0].substring(1);
            }
        }
        return toClientId;
    }

    private void sendAddressMessage(String toClientId, String input) {
        ClientHandler toClient = clients.get(toClientId);
        if (toClient != null) {
            toClient.send(input.replace("@" + toClientId + " ", ""));
        } else {
            System.err.println("Не найден клиент с идентфиикатором: " + toClientId);
        }
    }

    private void closeClientSocket() {
        try {
            String message = "Клиент: " + clientSocket + " отключился от чата";
            clientSocket.close();
            System.out.println(message);
            clients.entrySet().removeIf(it-> it.getValue().equals(this));
            broadcastMessage(message);
        } catch (IOException e) {
            System.err.println("Ошибка при отключении клиента " + clientSocket + ": " + e.getMessage());
        }
    }

}