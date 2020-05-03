package zin.game.card.jackace;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import zin.game.card.Card;
import zin.game.card.jackace.network.ClientOnline;
import zin.game.card.jackace.network.ServerOnline;
import zin.game.jackace.JackAceGameActivity;
import zin.z.network.ServerClientEventCallback;
import zin.z.network.SingleClientForSingleServer;
import zin.z.network.SingleServerForSingleClient;

import static zin.game.card.jackace.JackAceGameEvent.I_DONT_NEED_CARD_ANYMORE;
import static zin.game.card.jackace.JackAceGameEvent.I_LOST;
import static zin.game.card.jackace.JackAceGameEvent.I_NEED_CARD;

public class JackAceOnline {
    public static Retrofit retrofit;
    static {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …
// add logging as last interceptor
        httpClient.addInterceptor(logging);  // <-- this is the important line!
        retrofit = new Retrofit.Builder()
                .baseUrl("https://zinbotml.000webhostapp.com/")
                //.baseUrl("http://zinbot.tk/")
            .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build();
    }

    private static JackAcePlayer serverPlayer, clientPlayer;
    private static ServerOnline server = new ServerOnline();
    private static ClientOnline client = new ClientOnline();
    private static JackAceDeck deck = new JackAceDeck();
    private static JackAceGameActivity activity;
    public static int I_DONT_NEED_IT_ANYMORE_COUNT = 0;

    public static void declareResultsIfPossible(String role) {
        if(I_DONT_NEED_IT_ANYMORE_COUNT == 2) {
            disableButtons();
            if(serverPlayer.getTotal() == clientPlayer.getTotal()) {
                JackAceGameActivity.myScore.setText("Game draw!!");
            } else if(serverPlayer.getTotal() > clientPlayer.getTotal() && role.equals("SERVER")) {
                JackAceGameActivity.myScore.setText("You won!!");
            } else if (serverPlayer.getTotal() < clientPlayer.getTotal() && role.equals("SERVER")) {
                JackAceGameActivity.myScore.setText("You lost!!");
            } else if (serverPlayer.getTotal() > clientPlayer.getTotal() && role.equals("CLIENT")) {
                JackAceGameActivity.myScore.setText("You lost!!");
            } else if (serverPlayer.getTotal() < clientPlayer.getTotal() && role.equals("CLIENT")) {
                JackAceGameActivity.myScore.setText("You won!!");
            }
        }
    }

    public static void disableButtons() {
        JackAceGameActivity.takeCard.setEnabled(false);
        JackAceGameActivity.dontTakeCard.setEnabled(false);
    }

    public static void enableButtons() {
        JackAceGameActivity.takeCard.setEnabled(true);
        JackAceGameActivity.dontTakeCard.setEnabled(true);
    }

    public static ServerClientEventCallback serverCallback = new ServerClientEventCallback() {

        @Override
        public void onReceiveMessage(String receivedMessage) {
            String[] split = receivedMessage.split(" ");
            activity.runOnUiThread(() -> {
                try {
                    JackAceGameEvent event = JackAceGameEvent.valueOf(split[0]);
                    switch(event) {
                        case I_NEED_CARD:{
                            Card c = get1Card();
                            clientPlayer.receiveOneCard(c);
                            I_DONT_NEED_IT_ANYMORE_COUNT = 0;
                            break;
                        }
                        case I_DONT_NEED_CARD_ANYMORE: {
                            enableButtons();
                            I_DONT_NEED_IT_ANYMORE_COUNT++;
                            declareResultsIfPossible("SERVER");
                            break;
                        }
                        case I_LOST: {
                            JackAceGameActivity.myScore.setText("You won!!");
                            disableButtons();
                            break;
                        }
                        default: {
                            //throw new RuntimeException("Impossible exception. Unexpected "+event);
                        }
                    }
                } catch (IllegalArgumentException e) {} catch(Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public void handleOtherEvent(String receivedMessage) {
            String[] split = receivedMessage.split(" ");
            try {
                JackAceGameEvent event = JackAceGameEvent.valueOf(split[0]);
                switch(event) {
                    case SERVER_RECEIVED_THE_CARD:
                        I_DONT_NEED_IT_ANYMORE_COUNT = 0;
                        server.sendMessage(receivedMessage);
                        JackAceGameActivity.myScore.setText("My Score : "+ serverPlayer.getTotal());
                        JackAceGameActivity.setImage(false, Integer.parseInt(split[1]), Card.Suit.valueOf(split[2]));
                        break;
                    case CLIENT_RECEIVED_THE_CARD: {
                        I_DONT_NEED_IT_ANYMORE_COUNT = 0;
                        server.sendMessage(receivedMessage);
                        JackAceGameActivity.opponentScore.setText("Opponent Score : "+ clientPlayer.getTotal());
                        JackAceGameActivity.setImage(true, Integer.parseInt(split[1]), Card.Suit.valueOf(split[2]));
                        break;
                    }
                    case I_LOST: {
                        server.sendMessage(I_LOST + "");
                        JackAceGameActivity.myScore.setText("I Lost!!");
                        disableButtons();
                        break;
                    }
                    default: {
                        //throw new RuntimeException("Impossible exception. Unexpected "+event);
                    }
                }
            } catch (IllegalArgumentException e) {}  catch(Exception e) {
                throw new RuntimeException(e);
            }

        }
    };
    public static ServerClientEventCallback clientCallback = new ServerClientEventCallback() {

        private Card createCard(String[] s) {
            return new Card(Integer.valueOf(s[1]), Card.Suit.valueOf(s[2]));
        }

        @Override
        public void onReceiveMessage(String receivedMessage) {
            String[] split = receivedMessage.split(" ");
            activity.runOnUiThread(() -> {
                try {
                    JackAceGameEvent event = JackAceGameEvent.valueOf(split[0]);
                    switch(event) {
                        case SERVER_RECEIVED_THE_CARD: {
                            I_DONT_NEED_IT_ANYMORE_COUNT = 0;
                            Card c = createCard(split);
                            serverPlayer.receiveOneCard(c);
                            JackAceGameActivity.myScore.setText("Opponent Score : " + serverPlayer.getTotal());
                            JackAceGameActivity.setImage(true, c);
                            break;
                        }
                        case CLIENT_RECEIVED_THE_CARD: {
                            I_DONT_NEED_IT_ANYMORE_COUNT = 0;
                            Card c = createCard(split);
                            clientPlayer.receiveOneCard(c);
                            JackAceGameActivity.opponentScore.setText("My Score : "+ clientPlayer.getTotal());
                            JackAceGameActivity.setImage(false, c);
                            break;
                        }
                        case I_DONT_NEED_CARD_ANYMORE: {
                            enableButtons();
                            I_DONT_NEED_IT_ANYMORE_COUNT++;
                            declareResultsIfPossible("CLIENT");
                            break;
                        }
                        case I_LOST: {
                            JackAceGameActivity.myScore.setText("You won!!");
                            disableButtons();
                            break;
                        }
                        default: {
                            //throw new RuntimeException("Impossible exception. Unexpected "+event);
                        }
                    }
                } catch (IllegalArgumentException e) {}  catch(Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public void handleOtherEvent(String receivedMessage) {
            String[] split = receivedMessage.split(" ");
            try {
                JackAceGameEvent event = JackAceGameEvent.valueOf(split[0]);
                switch(event) {
                    case I_NEED_CARD:{
                        client.sendMessage(I_NEED_CARD + "");
                        break;
                    }
                    case I_LOST: {
                        client.sendMessage(I_LOST + "");
                        break;
                    }
                    default: {
                        //throw new RuntimeException("Impossible exception. Unexpected "+event);
                    }
                }
            } catch (IllegalArgumentException e) {}  catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    };

    public static Card get1Card() {
        return deck.getNCards(1).get(0);
    }

    private static JackAcePlayer currentPlayer(String role) {
        return role.equals("SERVER") ? serverPlayer : clientPlayer;
    }

    public static void startServerPlayerGame () {
        serverPlayer = new JackAcePlayer(serverCallback, "SERVER");
        clientPlayer = new JackAcePlayer(serverCallback, "CLIENT");
        deck = new JackAceDeck();
        List<JackAceCard> serverCard = deck.getNCards(1);
        List<JackAceCard> clientCard = deck.getNCards(1);
        serverPlayer.receiveOneCard(serverCard.get(0));
        clientPlayer.receiveOneCard(clientCard.get(0));
        /*
        serverCard = deck.getNCards(1);
        clientCard = deck.getNCards(1);
        serverPlayer.receiveOneCard(serverCard.get(0));
        clientPlayer.receiveOneCard(clientCard.get(0));
        //*/
    }
    public static void startClientPlayerGame() {
        serverPlayer = new JackAcePlayer(clientCallback, "SERVER");
        clientPlayer = new JackAcePlayer(clientCallback, "CLIENT");
        disableButtons();
    }

    public static void startGame(String role, String serverId, String clientId) {
        if ((role.equals("SERVER"))) {
            startServer(serverId, clientId);
        } else {
            joinServer(serverId, clientId);
        }
    }
    public static void startGameFromTheBeginning(JackAceGameActivity a, String role, String serverId, String clientId) {
        activity = a;
        startGame(role, serverId, clientId);
    }
    public static void startServer(String serverId, String clientId) {
        server.startServer(serverId, clientId, serverCallback);
        startServerPlayerGame();
    }
    public static void joinServer(String serverId, String clientId) {
        client.connect(serverId, clientId, clientCallback);
        startClientPlayerGame();
    }
    public static void requestCard(String role) {
        Card c = get1Card();
        if(role.equals("SERVER")) {
            serverPlayer.receiveOneCard(c);
        } else {
            clientCallback.publishEvent(I_NEED_CARD + "");
        }
    }
    public static void endChance(String role) {
        I_DONT_NEED_IT_ANYMORE_COUNT++;
        declareResultsIfPossible(role);
        if(role.equals("SERVER")) {
            server.sendMessage(I_DONT_NEED_CARD_ANYMORE + "");
        } else {
            client.sendMessage(I_DONT_NEED_CARD_ANYMORE + "");
        }
        disableButtons();
    }

    public static void restart() {

    }

    public static void resetAll() {
        I_DONT_NEED_IT_ANYMORE_COUNT = 0;
    }
}
