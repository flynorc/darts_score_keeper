package com.flynorc.dartscricketcounter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ResultsActivity extends AppCompatActivity {

    private String mLosingPlayerName, mWinningPlayerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        //get the player names from intent
        mLosingPlayerName = getIntent().getStringExtra("losingPlayerName");
        mWinningPlayerName = getIntent().getStringExtra("winningPlayerName");

        //update the text
        TextView resultsText = (TextView) findViewById(R.id.results_text_field);
        resultsText.setText(String.format(getResources().getString(R.string.result_text), mWinningPlayerName, mLosingPlayerName));
    }


    /*
     * onclick listeners
     */
    //back to start
    public void backToStart(View v) {
        Intent i = new Intent(ResultsActivity.this, MainActivity.class);
        startActivity(i);
    }

    public void playAgain(View v) {
        Intent i = new Intent(ResultsActivity.this, CricketScorerActivity.class);
        //put the player name information to the intent
        //losing player goes first in the next game
        i.putExtra("player1Name", mLosingPlayerName);
        i.putExtra("player2Name", mWinningPlayerName);

        startActivity(i);
        finish();
    }
}
