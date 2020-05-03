package zin.game.card.jackace;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.UUID;

public class JackAceGameConnection {
    @SerializedName("SERVER_ID")
    private String server_id;
    @SerializedName("CLIENT_ID")
    private String client_id;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JackAceGameConnection() {
    }

    public void randomServerUUID() {
        setServer_id(UUID.randomUUID().toString());
    }

    public String getServer_id() {
        return server_id;
    }

    public void setServer_id(String server_id) {
        this.server_id = server_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }
}
