package zin.game.jackace;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.Button;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;

import zin.game.card.Card;
import zin.game.card.jackace.JackAceGame;

public class JackAceGameActivity extends AppCompatActivity {

    private static int topCardsPointer, bottomCardsPointer;
    public static TextView opponentScore, myScore;
    public static Button takeCard, dontTakeCard;
    public static Button[] top = new Button[10];
    public static Button[] bottom = new Button[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int i=0;
        setContentView(R.layout.activity_jack_ace_game);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        try {
            top[i++] = (Button) findViewById(R.id.row1image1);
            top[i++] = (Button) findViewById(R.id.row1image2);
            top[i++] = (Button) findViewById(R.id.row1image3);
            top[i++] = (Button) findViewById(R.id.row1image4);
            top[i++] = (Button) findViewById(R.id.row1image5);
            top[i++] = (Button) findViewById(R.id.row2image1);
            top[i++] = (Button) findViewById(R.id.row2image2);
            top[i++] = (Button) findViewById(R.id.row2image3);
            top[i++] = (Button) findViewById(R.id.row2image4);
            top[i++] = (Button) findViewById(R.id.row2image5);
            i = 0;
            bottom = new Button[10];
            bottom[i++] = (Button) findViewById(R.id.row3image1);
            bottom[i++] = (Button) findViewById(R.id.row3image2);
            bottom[i++] = (Button) findViewById(R.id.row3image3);
            bottom[i++] = (Button) findViewById(R.id.row3image4);
            bottom[i++] = (Button) findViewById(R.id.row3image5);
            bottom[i++] = (Button) findViewById(R.id.row4image1);
            bottom[i++] = (Button) findViewById(R.id.row4image2);
            bottom[i++] = (Button) findViewById(R.id.row4image3);
            bottom[i++] = (Button) findViewById(R.id.row4image4);
            bottom[i++] = (Button) findViewById(R.id.row4image5);
            myScore = (TextView) findViewById(R.id.myScore);
            opponentScore = (TextView) findViewById(R.id.opponentScore);
            takeCard = (Button) findViewById(R.id.takeCard);
            dontTakeCard = (Button) findViewById(R.id.dontTakeCard);
            Intent intent = getIntent();
            final String role = intent.getStringExtra("role");
            JackAceGame.startGameFromTheBeginning(this, role);
            takeCard.setOnClickListener((view) -> {
                JackAceGame.requestCard(role);
            });
            dontTakeCard.setOnClickListener((v) -> {
                JackAceGame.endChance(role);
            });
        } catch(Exception e) {
            Intent intent = new Intent (this, ErrorActivity.class);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            intent.putExtra("error", sStackTrace.substring(0, 100));
            startActivity(intent);
        }
    }

    public static void setImage(boolean shouldSetTop, Card card) {
        setImage(shouldSetTop, card.getNumber(), card.getSuit());
    }

    public static void setImage(boolean shouldSetTop, int cardNumber, Card.Suit suit) {
        int pointer = shouldSetTop ? topCardsPointer : bottomCardsPointer;
        Button button = shouldSetTop ? top[topCardsPointer++] : bottom [bottomCardsPointer++];
        button.setText(Card.getActualCardValueInsteadOfNumber(cardNumber));
        button.setTextColor(Color.GREEN);
        switch (suit) {
            case CLUB:
                button.setBackgroundResource( R.drawable.club);
                break;
            case HEART:
                button.setBackgroundResource(R.drawable.hearts);
                break;
            case SPADE:
                button.setBackgroundResource(R.drawable.spade);
                break;
            case DIAMOND:
                button.setBackgroundResource(R.drawable.diamond);
                break;
        }
    }
}
