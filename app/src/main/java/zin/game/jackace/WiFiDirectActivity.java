package zin.game.jackace;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.abemart.wroup.client.WroupClient;
import com.abemart.wroup.common.WiFiDirectBroadcastReceiver;
import com.abemart.wroup.common.WiFiP2PError;
import com.abemart.wroup.common.WiFiP2PInstance;
import com.abemart.wroup.common.WroupDevice;
import com.abemart.wroup.common.WroupServiceDevice;
import com.abemart.wroup.common.listeners.ClientConnectedListener;
import com.abemart.wroup.common.listeners.ClientDisconnectedListener;
import com.abemart.wroup.common.listeners.DataReceivedListener;
import com.abemart.wroup.common.listeners.ServiceDiscoveredListener;
import com.abemart.wroup.common.listeners.ServiceRegisteredListener;
import com.abemart.wroup.common.messages.MessageWrapper;
import com.abemart.wroup.service.WroupService;
import com.google.gson.Gson;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import zin.game.card.jackace.JackAceGame;
import zin.game.card.jackace.JackAceGameConnection;
import zin.game.card.jackace.JackAceOnline;
import zin.game.card.jackace.network.JackAceConnectionService;
import zin.game.card.jackace.network.ResponseWrapper;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WiFiDirectActivity extends AppCompatActivity {

    public Retrofit retrofit = JackAceOnline.retrofit;
    public Gson gson = new Gson();

    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_direct);
        try {
            JackAceConnectionService service = retrofit.create(JackAceConnectionService.class);

            TextView textView = (TextView) findViewById(R.id.textToShow);

            Button joinServerButton = (Button) findViewById(R.id.joinServerButton);
            joinServerButton.setOnClickListener(new View.OnClickListener() {
                String serverId, clientId;
                @Override
                public void onClick(View view) {
                    service.findGamesToJoin("db", "zin-game-jackace-connection", "get", "client_id")
                            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .flatMap(w -> {
                                if (w.getStatus() == 200) {
                                    List<JackAceGameConnection> g = w.getData();
                                    if (g.size() == 0) {
                                        setText("No server is running. Try again or create your own server");
                                        return Single.just("404");
                                    } else {
                                        String myID = UUID.randomUUID().toString();
                                        JackAceGameConnection joined = g.get(0);
                                        joined.setClient_id(myID);
                                        serverId = joined.getServer_id();
                                        clientId = myID;
                                        return service.joinGame("db", "zin-game-jackace-connection", "update", "server_id", joined.getServer_id(), joined)
                                                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
                                    }
                                } else {
                                    setText("Error "+gson.toJson(w.getData()));
                                    return Single.just("ERROR");
                                }
                            })
                            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Object>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onSuccess(Object o) {
                            if(! (o instanceof String)) {
                                Intent intent = new Intent(WiFiDirectActivity.this, JackAceGameActivity.class);
                                intent.putExtra("role", "CLIENT");
                                intent.putExtra("client_id", clientId);
                                intent.putExtra("server_id", serverId);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            setText("Error "+e.getMessage());
                        }
                    });
                }

                private void setText(String text) {
                    WiFiDirectActivity.this.runOnUiThread(() -> {
                        textView.setText(text);
                    });
                }
            });
            Button startServerButton = (Button) findViewById(R.id.startServerButton);
            startServerButton.setOnClickListener(new View.OnClickListener() {
                Disposable disposable;
                @Override
                public void onClick(View view) {
                    createGame();
                }

                private void setText(String text) {
                    WiFiDirectActivity.this.runOnUiThread(() -> {
                        textView.setText(text);
                    });
                }

                private void checkIfSomeoneJoined(JackAceGameConnection connection) {
                    service.checkIfSomeoneJoined
                            ("db", "zin-game-jackace-connection", "get", "server_id", connection.getServer_id())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Object>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                    disposables.add(d);
                                }
                                public JackAceGameConnection fromJson(String data) {
                                    data = data.substring(data.indexOf('['), data.indexOf(']'));
                                    data = data.replaceAll("\\[", "")
                                            .replaceAll("\\]", "");;
                                    return gson.fromJson(data, JackAceGameConnection.class);
                                }
                                @Override
                                public void onSuccess(Object jackAceGameConnectionResponseWrapper) {
                                    JackAceGameConnection g = fromJson(gson.toJson(jackAceGameConnectionResponseWrapper));
                                    if (g.getClient_id() != null && !g.getClient_id().isEmpty()) {
                                        if(! disposable.isDisposed())
                                            disposable.dispose();
                                        Intent intent = new Intent(WiFiDirectActivity.this, JackAceGameActivity.class);
                                        intent.putExtra("role", "SERVER");
                                        intent.putExtra("client_id", g.getClient_id());
                                        intent.putExtra("server_id", g.getServer_id());
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    setText("Error : " + e.getMessage());
                                }
                            });
                }

                private void createGame() {
                    JackAceGameConnection connection = new JackAceGameConnection();
                    //connection.setServer_id("f931623e-6e02-4fc1-962f-b05165b3af0a");
                    //*
                    connection.randomServerUUID();
                    service.startGame("db", "zin-game-jackace-connection", "create", connection)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<ResponseWrapper<Object>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(ResponseWrapper<Object> objectResponseWrapper) {
                            setText("Game created! Waiting for a player to join...");
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                    });
                    //*/
                    disposable = Observable.interval(3000, 3000,
                            TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(time -> checkIfSomeoneJoined(connection), e-> setText("Error: "+e.getMessage()));

                }
            });
        } catch (
                Throwable e) {
            e.printStackTrace();
        }
    }
}