package it.polimi.ingsw.network.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.ingsw.model.InterfaceAdapter;
import it.polimi.ingsw.network.messages.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private int id;

    private Socket socket;
    private Server server;
    private Thread pinger;
    private Thread timer;

    private Scanner in;
    private PrintWriter out;

    private Gson gson;

    private boolean isConnected;
    private boolean ping;
    private boolean timeout;
    private ServerVisitorHandler serverVisitorHandler;

    private static int globalCounter = 0;

    public int getId() {
        return id;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void closeConnection() throws InterruptedException {
        Message message = new DisconnectionMessage();

        out.println(gson.toJson(message, Message.class));
        server.removeVirtualClient(id);
        Thread.sleep(3000);
        out.close();
        in.close();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("User disconnected with id: " + id);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }

    public Thread getPinger() {
        return pinger;
    }

    public void stopTimer(){
        if(timer != null){
            timer.interrupt();
            timer = null;
        }
    }
    public void startTimer(int ms){
        timer = new Thread(() -> {
            try {
                Thread.sleep(ms);
                timeout=true;
            } catch (InterruptedException e) {  }
        });
        timer.start();
    }
    public Scanner getIn() { return in; }

    public PrintWriter getOut() { return out; }

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.isConnected = true;
        this.serverVisitorHandler = new ServerVisitorHandler();
        globalCounter++;
        this.id = globalCounter;
        this.timeout = false;
        this.pinger = new Thread(() -> {
            ping = true;
            while (ping) {
                try {
                    ping = false;
                    send(new PingRequest());
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("Player disconnected");
            server.handleDisconnection(id);
            isConnected = false;
        });
    }

    public void run() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ServerMessage.class, new InterfaceAdapter<ServerMessage>());
        builder.registerTypeAdapter(ClientMessage.class, new InterfaceAdapter<ClientMessage>());
        builder.registerTypeAdapter(Message.class, new InterfaceAdapter<Message>());
        gson = builder.create();
        try {
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            send(new RegisterRequest());
            startTimer(50000);

            String jsonString = "";
            while (!jsonString.equals("quit")) {
                try {
                    jsonString = in.nextLine();
                } catch (NoSuchElementException e){
                    System.out.println("Disconnecting client handler number " + id + " . . .");
                    return;
                }
                Message message = gson.fromJson(jsonString, Message.class);
                handleMessage(message);
            }
            // closing streams and socket
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void send(Message message) { //it's one line but it's nice to have for other classes
        out.println(gson.toJson(message, Message.class));
    }

    public boolean isTimeout() {
        return timeout;
    }

    public Server getServer() {
        return server;
    }

    public Gson getGson() {
        return gson;
    }

    public void setPing(boolean ping) {
        this.ping = ping;
    }

    public void handleMessage(Message message) {
        ServerMessage serverMessage = (ServerMessage) message;
        serverMessage.accept(serverVisitorHandler, this);
    }
}