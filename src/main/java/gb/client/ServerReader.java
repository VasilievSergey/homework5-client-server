package gb.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ServerReader implements Runnable {
    private final Socket serverSocket;

    public ServerReader(Socket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try (Scanner in = new Scanner(serverSocket.getInputStream())) {
            while (in.hasNext()) {
                String input = in.nextLine();
                System.out.println(input);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при отключении чтении с сервера: " + e.getMessage());
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Ошибка при отключении от сервера: " + e.getMessage());
        }
    }

}