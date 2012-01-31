package no.bouvet.bouvetone;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.WebSocketServer;


/**
 * BouvetOne Websockets server.
 * 
 * @author christopher.olaussen@bouvet.no
 *
 */
public class SampleServer extends WebSocketServer {
	
	private ConcurrentHashMap<WebSocket, Visitor> connections = new ConcurrentHashMap<WebSocket, Visitor>();
	
    public SampleServer(int port) {
        super(port);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void onClientOpen(WebSocket conn) {
        try {
        	
        	Visitor createdVisitor = new Visitor(conn.hashCode());
        	Visitor visitor = connections.putIfAbsent(conn, createdVisitor);
        	if(visitor == null) {
        		visitor = createdVisitor;
        	}
        	        	
        	Message nvm = new Message();
        	nvm.id = visitor.getId();
        	nvm.type ="NEWVISITOR";
        	
        	String nvmstring = new Gson().toJson(nvm);
        	System.out.println("onclientopen"+nvmstring);
        	
            this.sendToAll(nvmstring);
            
        	Iterator<Entry<WebSocket, Visitor>> iterator = connections.entrySet().iterator();
        	while(iterator.hasNext()) {
        		Entry<WebSocket, Visitor> player = iterator.next();
            	if(player.getValue().getId()!= visitor.getId()) {            		
	            	Message updateMessage = new Message();
	            	updateMessage.type = "UPDATE";
	            	updateMessage.id = player.getValue().getId();
	            	updateMessage.name = player.getValue().name;
	            	updateMessage.posx = player.getValue().posx;
	            	updateMessage.posy = player.getValue().posy;
	            	String string = new Gson().toJson(updateMessage);
	            	System.out.println(string);
	            	conn.send(string);
            	}
        	}
        	
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println(conn + " entered the room!");
    }

    
    public void onClientClose(WebSocket conn) {
        try {
        	
        	Visitor visitor = connections.remove(conn);
        	
        	Message msg = new Message();
        	msg.type = "LEFTVISITOR";
        	msg.id = visitor.getId();
        	
        	this.sendToAll(new Gson().toJson(msg));        	
        	
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println(conn + " has left the room!");
    }

    public void onClientMessage(WebSocket conn, String message) {
        try {
        	System.out.println("onclientmessage:"+message);
        	Gson gson = new Gson();
        	Message msg = gson.fromJson(message, Message.class);
        	
        	if("CHANGENAME".equals(msg.type)) {
        		connections.get(conn).name=msg.name;
        	}
        	        	
        	msg.id = connections.get(conn).getId();
        	msg.name = connections.get(conn).name;
        	
            this.sendToAll(gson.toJson(msg));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println(conn + ": " + message);
    }

    public void onError(Throwable ex) {
      ex.printStackTrace();
    }
        
    static class OBX implements Runnable {
    	
    	private WebSocketServer server;
    	public OBX(SampleServer server) {
    		this.server = server;
    	}
    	
		@Override
		public void run() {
			
			Random rand = new Random();
			while(true) {
				
				int randomNum = rand.nextInt(150 - 100 + 1) + 100;
				try {
					Message msg = new Message();
					msg.type="TICKER";
					msg.value=randomNum;
					server.sendToAll(new Gson().toJson(msg));
					try {Thread.sleep(30);} catch(InterruptedException e) {}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }

    public static void main(String[] args) {
        int port = 8887;
        try {
            port = Integer.parseInt(args[0]);
        } catch(Exception ex) {}
        
        SampleServer s = new SampleServer(port);
        s.start();
        
        OBX tickers = new OBX(s);
        new Thread(tickers).start();
        
        System.out.println("Server started on port: " + s.getPort());
    }
}