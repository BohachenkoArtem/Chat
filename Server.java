/*
1. Запускаете Server. Вводите номер порта.
2. Запускаете ClientGuiController. Вводите localhost и номер порта из п.1, вводите имя пользователя.
На этом этапе чат уже работает.
Опционально:
3. Запустить консольного клиента Client.
4. Запустить бота BotClient.
Все запущенные пользователи (п. 2, 3, 4) будут отображаться в GUI версии чата.
*/
package com.javarush.task.task30.task3008;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.javarush.task.task30.task3008.ConsoleHelper.readInt;

public class Server {
    private static Map<String,Connection> connectionMap = new ConcurrentHashMap<String,Connection>();
  //відправка повідомлення всім співбесідникам
    public static void sendBroadcastMessage(Message message){

            try {
                for(Map.Entry<String,Connection> pair:connectionMap.entrySet())
                pair.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("При отправлении сообщения вознникла ошибка!");
            }

    }

    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket){
            this.socket = socket;
        }

        public void run(){
       // socket.getRemoteSocketAddress();
            ConsoleHelper.writeMessage("было установлено соединение с удаленным адресом"+socket.getRemoteSocketAddress());
            Connection connection=null;
            String userName=null;
        try {
                connection = new Connection(socket);


                userName= serverHandshake(connection);

                sendBroadcastMessage(new Message(MessageType.USER_ADDED,userName));

                sendListOfUsers(connection,userName);

                serverMainLoop(connection,userName);

                connection.close();
        }catch (IOException e){
            ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удальённым сервером!");
        }catch (ClassNotFoundException e){
            ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удальённым сервером!");
        }
            if (userName!=null) {connectionMap.remove(userName);
        sendBroadcastMessage(new Message(MessageType.USER_REMOVED,userName));}
        ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто");


        }

        private String serverHandshake(Connection connection) throws IOException,ClassNotFoundException{
        Message messageHello = new Message(MessageType.NAME_REQUEST,"Hallo! What is your name?");
        Message messageAnsver;
            String userName = null;
        boolean isNameGood = false;
        while (!isNameGood){
            connection.send(messageHello);
           messageAnsver=connection.receive();
           if(messageAnsver.getType()!=MessageType.USER_NAME) continue;
          userName = messageAnsver.getData();
           if(userName==null||userName.trim().equals("")) continue;
           boolean isNameInMap=false;
           for(Map.Entry<String,Connection> pair:connectionMap.entrySet())
                if(pair.getKey().equals(userName)) isNameInMap = true;
          if(isNameInMap) continue;
          connectionMap.put(userName,connection);
          connection.send(new Message(MessageType.NAME_ACCEPTED,"Your name is accept!"));
          isNameGood = true;

        }
        return userName;
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException{
        for(Map.Entry<String,Connection> pair :connectionMap.entrySet()){
            if(!pair.getKey().equals(userName))
            {Message message = new Message(MessageType.USER_ADDED,pair.getKey());
            connection.send(message);
            }

        }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
        while (true){
            Message message = connection.receive();
           if(message.getType()==MessageType.TEXT){
               String text = String.format("%s: %s",userName,message.getData());
               Message messageToAll = new Message(MessageType.TEXT,text);
               sendBroadcastMessage(messageToAll);
           } else if(message.getType()!=MessageType.TEXT)
           {ConsoleHelper.writeMessage("Error!");}

        }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Введите порт сервера: ");
        int port = readInt();
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Сервер запущен!");
        while (true){
           try {
               Socket socket = serverSocket.accept();
           
            new Handler(socket).start();
           // continue;
           }
            catch (Exception e){
               serverSocket.close();
                System.out.println(e.getMessage());
                break;
            }
        }

    }
}
