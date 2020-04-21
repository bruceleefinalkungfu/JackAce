package zin.z.network;

import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import zin.z.network.ServerClientEventCallback;

import java.io.*;

/**
 * {@link SingleServerForSingleClient} has to be start before {@link SingleClientForSingleServer}
 * Both can continue to send and receive messages synchronously until someone disconnects.
 * The received message will be handled by {@link ServerClientEventCallback#onReceiveMessage(String)}
 * The received message will be followed after the text {@link ServerClientEvent#MESSAGE_RECEIVED_SUCCESSFULLY}  
 * @author Anurag.Awasthi
 *
 */
public class SingleServerForSingleClient implements Runnable {

    private Socket            socket          = null;
    private ServerSocket      server          = null;
    private DataOutputStream  out             = null;
    private DataInputStream   in              = null;

    private ServerClientEventCallback callback;

    private boolean           clientConnected = false;
    private ExecutorService   executorService;

    private void notifyServerIp(ServerClientEventCallback callback) {
        try {
            callback.publishEvent(ServerClientEvent.NOTIFYING_SERVER_IP_INFORM_THE_CLIENT_OF_IT 
                    + " " + InetAddress.getLocalHost().getHostAddress());
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            String ip = "";
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;
    
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
    
                    // *EDIT*
                    if (addr instanceof Inet6Address) continue;
    
                    ip = addr.getHostAddress();
                    callback.publishEvent(ServerClientEvent
                            .NOTIFYING_SERVER_IP_INFORM_THE_CLIENT_OF_IT + " " 
                            +iface.getDisplayName() + " " + ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void startServer(int port, ServerClientEventCallback callback) {
        try {
            server = new ServerSocket(port);
            notifyServerIp(callback);
            
            socket = server.accept();
            out = new DataOutputStream(socket.getOutputStream());
            clientConnected = true;
            this.callback = callback;
            in = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(this);
        } catch (Exception e) {
            callback.publishEvent(
                    ServerClientEvent.SERVER_FAILED_TO_CONNECT_A_CLIENT + 
                                  " port: " +
                                  port +
                                  " " +
                                  e.getMessage());
            clientConnected = false;
        }
    }

    public void sendMessage(String msg) {
        if (!clientConnected)
            throw new RuntimeException("Impossible Exception. No client isn't connected to our server.");
        try {
            out.writeUTF(msg);
            callback.publishEvent(ServerClientEvent.MESSAGE_SENT_SUCCESSFULLY + " " + msg);
        } catch (IOException i) {
            callback.publishEvent(ServerClientEvent.FAILED_TO_SEND_THE_MESSAGE + " " + msg);
        } catch (Exception e) {
            disconnect();
        }
    }

    public void disconnect() {
        if( ! clientConnected) return;
        try {
            clientConnected = false;
            out.close();
            in.close();
            socket.close();
            server.close();
        } catch (IOException e) {
            callback.publishEvent(ServerClientEvent.EXCEPTION_TO_BE_IGNORED_DURING_DISCONNECT.toString());
        } finally {
            /**
             * Superstition : Wait 1 second before destroying the thread.
             * Let sockets be closed first so no IO read operation is blocking in run method
             */
            try { Thread.sleep(1000); } catch(Exception e) {}
            executorService.shutdown();
        }
    }


    @Override
    public void run() {
        for (;;) {
            if (!clientConnected)
                break;
            try {
                String msg = in.readUTF();
                callback.publishEvent(ServerClientEvent.MESSAGE_RECEIVED_SUCCESSFULLY + " " + msg);
            } catch (IOException e) {
                callback.publishEvent(ServerClientEvent.EXCEPTION_TO_BE_IGNORED_WHEN_FAILED_TO_RECEIVE_MESSAGE + " " + e.getMessage());
                break;
            }
        }
        disconnect();
    }

    public static void main(String[] args) throws Exception {
        SingleServerForSingleClient server = new SingleServerForSingleClient();
        server.startServer(5000, ServerClientEventCallback.instance());
        server.sendMessage("1st msg From server");
        server.sendMessage("server2 Hii");
        Thread.sleep(2000);
        server.disconnect();
    }
    
}
