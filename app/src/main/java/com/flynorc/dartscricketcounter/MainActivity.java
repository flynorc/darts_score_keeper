package com.flynorc.dartscricketcounter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

public class MainActivity extends AppCompatActivity {

    private RatingBar mRatingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void startGame(View v) {
        Intent i = new Intent(MainActivity.this, CricketScorerActivity.class);

        //put the player name information to the intent
        EditText player1NameField = (EditText) findViewById(R.id.player1_name_field);
        EditText player2NameField = (EditText) findViewById(R.id.player2_name_field);
        i.putExtra("player1Name", player1NameField.getText().toString());
        i.putExtra("player2Name", player2NameField.getText().toString());

        startActivity(i);
    }






}
