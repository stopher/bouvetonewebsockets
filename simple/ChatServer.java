import java.io.IOException;

import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.WebSocketServer;

public class SampleServer extends WebSocketServer {

    public SampleServer(int port) {
        super(port);
    }

    public void onClientOpen(WebSocket conn) {
        try {
            this.sendToAll(conn + " entered the room!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println(conn + " entered the room!");
    }

    public void onClientClose(WebSocket conn) {
        try {
            this.sendToAll(conn + " has left the room!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println(conn + " has left the room!");
    }

    public void onClientMessage(WebSocket conn, String message) {
        try {
            this.sendToAll(conn + ": " + message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println(conn + ": " + message);
    }

    public void onError(Throwable ex) {
      ex.printStackTrace();
    }

    public static void main(String[] args) {
        int port = 8887;
        try {
            port = Integer.parseInt(args[0]);
        } catch(Exception ex) {}
        SampleServer s = new SampleServer(port);
        s.start();
        System.out.println("Server started on port: " + s.getPort());
    }
}