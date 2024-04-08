package gb.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

public class ServerWriter implements Runnable {
    private final Socket serverSocket;

    public ServerWriter(Socket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        Scanner consoleReader = new Scanner(System.in);
        try (PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true)) {
            while (true) {
                String msgFromConsole = consoleReader.nextLine();
                out.println(msgFromConsole);

                // uuid msg

                if (Objects.equals("/exit", msgFromConsole)) {
                    System.out.println("Отключение клиента");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при отправке на сервер: " + e.getMessage());
        }


        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Ошибка при отключении от сервера: " + e.getMessage());
        }
    }

}