package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread{

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(userName+" присоединился к чату");
        }
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName+" покинул чат");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected=clientConnected;
          synchronized (Client.this){
              Client.this.notify();}
        }
        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if(message.getType()==MessageType.NAME_REQUEST) {
                     connection.send(new Message(MessageType.USER_NAME,getUserName())); }
                    else if(message.getType()==MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    return;}
                    else throw new IOException("Unexpected MessageType");
            }
        }
        protected void clientMainLoop() throws IOException, ClassNotFoundException{
           while (true){Message message = connection.receive();
            if(message.getType()==MessageType.TEXT) processIncomingMessage(message.getData());
            else if(message.getType()==MessageType.USER_ADDED) informAboutAddingNewUser(message.getData());
            else if(message.getType()==MessageType.USER_REMOVED) informAboutDeletingNewUser(message.getData());
            else throw new IOException("Unexpected MessageType");}
        }
        public void run(){
            String address = getServerAddress();
            int port = getServerPort();
            try {
                Socket socket = new Socket(address,port);
               connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();

            } catch (IOException e) {
               notifyConnectionStatusChanged(false);
            }catch (ClassNotFoundException e){
                notifyConnectionStatusChanged(false);
            }
        }

    }
    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this)
        {
            try {
                this.wait();
                if(clientConnected){ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");}
                else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Возникла ошибка работы нытей!");
            }
        }
        while (clientConnected){
           String line = ConsoleHelper.readString();
           if(line.equals("exit")){clientConnected=false;}
           else if(shouldSendTextFromConsole()) sendTextMessage(line);
        }

    }

    protected String getServerAddress(){
       return ConsoleHelper.readString();
    }
    protected int getServerPort(){
        return ConsoleHelper.readInt();
    }
    protected String getUserName(){
        return ConsoleHelper.readString();
    }
    protected  boolean shouldSendTextFromConsole(){
        return true;
    }
    protected  SocketThread getSocketThread(){
        SocketThread socketThread = new SocketThread();
        return socketThread;
    }
    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT,text));
        } catch (IOException e) {
           ConsoleHelper.writeMessage("Возникла ошибка при отправке сообения! Соединение прервано!");
           clientConnected = false;
        }
    }
    public static void main(String[] args){
        new Client().run();
    }
}
