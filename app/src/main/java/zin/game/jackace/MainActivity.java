package zin.game.jackace;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import zin.game.card.jackace.JackAceGame;
import zin.game.card.jackace.JackAceGameEvent;
import zin.z.network.ServerClientEvent;
import zin.z.network.ServerClientEventCallback;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
        Button joinServerButton = (Button) findViewById(R.id.joinServerButton);
        final EditText enterPortServer = (EditText) findViewById(R.id.enterPortForServerInput);
        final EditText enterIPClient = (EditText) findViewById(R.id.enterIPForClientInput);
        final EditText enterPortClient = (EditText) findViewById(R.id.enterPortForClientInput);
        joinServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable editText = enterPortClient.getText();
                String str = "1234";
                if(editText != null && ! editText.toString().isEmpty()) {
                    str = editText.toString();
                }
                Integer port = Integer.parseInt(str);
                String ip = enterIPClient.getText().toString();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JackAceGame.joinServer(ip, port);
                    }
                });
                t.start();
                try {
                    t.join();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
                openClient_sever("CLIENT");
            }
        });
        Button startServerButton = (Button) findViewById(R.id.startServerButton);
        startServerButton.setText("Start server on "+ ip);
        startServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable editText = enterPortServer.getText();
                String str = "1234";
                if(editText != null && ! editText.toString().isEmpty()) {
                    str = editText.toString();
                }
                Integer port = Integer.parseInt(str);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JackAceGame.startServer(port);
                    }
                });
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                }
                openClient_sever("SERVER");
            }
        });
    }
    public void openClient_sever(String role) {
        Intent intent = new Intent (this, JackAceGameActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
    }
}
