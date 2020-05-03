package zin.game.card.jackace;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class JackAceGameMessage {
    @SerializedName("msg_id")
    private String msg_id;
    @SerializedName("MSG")
    private String msg;
    @SerializedName("PRODUCED_BY_UUID")
    private String produced_by_uuid;
    @SerializedName("Card")
    private String card;
    @SerializedName("IS_READ")
    private int is_read;
    @SerializedName("PRODUCED_BY_ROLE")
    private String produced_by_role;
    @SerializedName("SERVER_ID")
    private String server_id;

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public void randomUUID() {
        setMsg_id(UUID.randomUUID().toString());
    }

    public String getServer_id() {
        return server_id;
    }

    public void setServer_id(String server_id) {
        this.server_id = server_id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getProduced_by_uuid() {
        return produced_by_uuid;
    }

    public void setProduced_by_uuid(String produced_by_uuid) {
        this.produced_by_uuid = produced_by_uuid;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public int isIs_read() {
        return this.is_read;
    }

    public void setIs_read(int is_read) {
        this.is_read = is_read;
    }

    public String getProduced_by_role() {
        return produced_by_role;
    }

    public void setProduced_by_role(String produced_by_role) {
        this.produced_by_role = produced_by_role;
    }
}
