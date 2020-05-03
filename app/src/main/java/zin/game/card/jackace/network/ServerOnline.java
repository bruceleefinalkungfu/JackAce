package zin.game.card.jackace.network;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import zin.game.card.jackace.JackAceGameEvent;
import zin.game.card.jackace.JackAceGameMessage;
import zin.game.card.jackace.JackAceOnline;
import zin.z.network.ServerClientEvent;
import zin.z.network.ServerClientEventCallback;

/**
 * @author Anurag.Awasthi
 */
public class ServerOnline extends GenericOnline {

    private ServerClientEventCallback callback;

    private boolean clientConnected = false;

    JackAceConnectionService service;
    String serverId, clientId;
    Disposable disposable;
    private Gson gson = new Gson();
    List<JackAceGameMessage> messagesToRead = new ArrayList<>();

    public void startServer(String serverId, String clientId, ServerClientEventCallback callback) {
        try {
            this.serverId = serverId;
            clientConnected = true;
            this.clientId = clientId;
            this.callback = callback;
            service = JackAceOnline.retrofit.create(JackAceConnectionService.class);
            disposable = Observable.interval(1000, 700,
                    TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> run(), e -> error(e));

        } catch (Exception e) {
            callback.publishEvent(
                    ServerClientEvent.SERVER_FAILED_TO_CONNECT_A_CLIENT +
                            " port: " +
                            " " +
                            e.getMessage());
            clientConnected = false;
        }
    }

    public JackAceGameMessage getMsg(String msg) {
        String split[] = msg.split(" ");
        JackAceGameEvent event = JackAceGameEvent.valueOf(split[0]);
        JackAceGameMessage jackAceGameMessage = new JackAceGameMessage();
        jackAceGameMessage.randomUUID();
        jackAceGameMessage.setMsg(event.toString());
        jackAceGameMessage.setServer_id(serverId);
        jackAceGameMessage.setIs_read(0);
        jackAceGameMessage.setProduced_by_role("SERVER");
        jackAceGameMessage.setProduced_by_uuid(serverId);
        switch (event) {
            case SERVER_RECEIVED_THE_CARD:
            case CLIENT_RECEIVED_THE_CARD: {
                jackAceGameMessage.setCard(split[1] + " " + split[2]);
                break;
            }
        }
        return jackAceGameMessage;
    }

    public String deduceMethod(JackAceGameMessage m) {
        return "create";
    }

    public void sendMessage(String msg) {
        try {
            JackAceGameMessage j = getMsg(msg);
            service.writeMessage("db", "zin-game-jackace-message", deduceMethod(j), j)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<ResponseWrapper<Object>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(ResponseWrapper<Object> objectResponseWrapper) {
                            String json = gson.toJson(objectResponseWrapper);
                             System.out.println(json);
                            if(objectResponseWrapper.getStatus() != 200)
                                throw new RuntimeException(gson.toJson(objectResponseWrapper.getData()));
                        }

                        @Override
                        public void onError(Throwable e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    });
            callback.publishEvent(ServerClientEvent.MESSAGE_SENT_SUCCESSFULLY + " " + msg);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void error(Throwable e) {
        throw new RuntimeException(e.getMessage());
    }


    //@Override
    public void run() {
        String msg = "";
        service.getUnreadMessages("db", "zin-game-jackace-message", "get",
                "is_read", "false", "produced_by_uuid", clientId)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ResponseWrapper<List<JackAceGameMessage>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(ResponseWrapper<List<JackAceGameMessage>> listResponseWrapper) {
                        if (listResponseWrapper.getStatus() == 200) {
                            List<JackAceGameMessage> s = listResponseWrapper.getData();
                            messagesToRead.addAll(s);
                            for (JackAceGameMessage m : s) {
                                if(isMsgAlreadyRead(m.getMsg_id()))
                                    continue;;
                                addMessageIdAsRead(m.getMsg_id());
                                callback.publishEvent(ServerClientEvent.MESSAGE_RECEIVED_SUCCESSFULLY + " " + getStrMsg(m));
                            }
                        } else {
                            throw new RuntimeException(gson.toJson(listResponseWrapper.getData()));
                        }
                    }

                    public String getStrMsg(JackAceGameMessage m) {
                        return m.getMsg();
                    }

                    @Override
                    public void onError(Throwable e) {
                        throw new RuntimeException(e.getMessage());
                    }
                });
        for(JackAceGameMessage m : messagesToRead) {
            if(m.isIs_read() == 1)
                continue;
            m.setIs_read(1);
            service.writeMessage("db", "zin-game-jackace-message",
                    "update", m)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<ResponseWrapper<Object>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(ResponseWrapper<Object> objectResponseWrapper) {
                            String json = gson.toJson(objectResponseWrapper);
                            System.out.println(json);
                            if(objectResponseWrapper.getStatus() != 200)
                                throw new RuntimeException(gson.toJson(objectResponseWrapper.getData()));
                        }

                        @Override
                        public void onError(Throwable e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    });
        }
        messagesToRead.clear();
    }

    public static void main(String[] args) throws Exception {
        ServerOnline server = new ServerOnline();
        server.startServer("server_id_test", "c", ServerClientEventCallback.instance());
        server.sendMessage("1st msg From server");
        server.sendMessage("server2 Hii");
        Thread.sleep(2000);
    }

}
