package gb.client;

import gb.server.Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

public class Client {

    public static void main(String[] args) {
        try {
            Socket serverSocket = new Socket("localhost", Server.PORT);
            System.out.println("Подключились к серверу: tcp://localhost:" + Server.PORT);

            // Читаем с сервера приветственное сообщение
            Scanner serverIn = new Scanner(serverSocket.getInputStream());
            String input = serverIn.nextLine();
            System.out.println(input);

            // Отправили идентфиикатор на сервер
            new PrintWriter(serverSocket.getOutputStream(), true).println(UUID.randomUUID());

            new Thread(new ServerReader(serverSocket)).start();
            new Thread(new ServerWriter(serverSocket)).start();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось подключиться к серверу: " + e.getMessage(), e);
        }
    }

}