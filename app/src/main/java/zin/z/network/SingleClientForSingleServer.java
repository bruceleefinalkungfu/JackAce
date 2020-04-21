package zin.z.network;

import java.net.*;
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
public class SingleClientForSingleServer implements Runnable {

    private Socket            socket    = null;
    private DataOutputStream  out       = null;

    private ServerClientEventCallback callback;

    private boolean           connected = false;
    private DataInputStream   in        = null;
    private ExecutorService   executorService;

    public void connect(String address, int port, ServerClientEventCallback callback) {
        try {
            socket = new Socket(address, port);
            out = new DataOutputStream(socket.getOutputStream());
            connected = true;
            this.callback = callback;
            in = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(this);
        } catch (Exception e) {
            callback.publishEvent(
                    ServerClientEvent.CLIENT_FAILED_TO_CONNECT_TO_SERVER + " address: " +
                                  address +
                                  " port: " +
                                  port +
                                  " " +
                                  e.getMessage());
            connected = false;
        }
    }

    public void sendMessage(String msg) {
        if (!connected)
            throw new RuntimeException("Impossible Exception. Client isn't connected to any server.");
        try {
            out.writeUTF(msg);
            callback.publishEvent(ServerClientEvent.MESSAGE_SENT_SUCCESSFULLY + " " + msg);
        } catch (IOException i) {
            callback.publishEvent(ServerClientEvent.FAILED_TO_SEND_THE_MESSAGE + " " + msg);
        }
    }

    public void disconnect() {
        try {
            connected = false;
            out.close();
            in.close();
            socket.close();
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
        if( ! connected) return;
        for (;;) {
            if (!connected)
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
        SingleClientForSingleServer client = new SingleClientForSingleServer();
        client.connect("127.0.0.1", 5000, ServerClientEventCallback.instance());
        client.sendMessage("I, client sent you msg");
        Thread.sleep(2000);
        // client.disconnect();
    }

}
