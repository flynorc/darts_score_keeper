package com.flynorc.dartscricketcounter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class CricketScorerActivity extends AppCompatActivity {

    private CricketScore player1, player2;
    private Boolean mIsPlayer1Turn;
    private int mDartsLeft;

    private TextView mPlayer1NameView, mPlayer2NameView, mPlayer1ScoreView, mPlayer2ScoreView, mPlayer1NrDartsView, mPlayer2NrDartsView;
    private RatingBar[] mPlayer1ProgressBars, mPlayer2ProgressBars;
    private Button mX2Button, mX3Button;

    private ImageView mDartboardImageView;

    //initialize the colors for comparing with the mask image to find out if any of the regions on the dartboard was "clicked"
    //red - bullseye, green - 20, blue - 19, yellow - 18, pink - 17, teal - 16, black - 15
    private final int[] mMaskColors = new int[] {
        Color.BLACK,
        Color.CYAN,
        Color.MAGENTA,
        Color.YELLOW,
        Color.BLUE,
        Color.GREEN,
        Color.RED
    };


    /*
     * save activity state on pause/rotate,...
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //save all the variables that need to be restored
        savedInstanceState.putParcelable("player1", player1);
        savedInstanceState.putParcelable("player2", player2);
        savedInstanceState.putBoolean("isPlayer1Turn", mIsPlayer1Turn);
        savedInstanceState.putInt("dartsLeft", mDartsLeft);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cricket_scorer);


        if (savedInstanceState == null) {
            //get the player names from intent
            String player1Name = getIntent().getStringExtra("player1Name");
            String player2Name = getIntent().getStringExtra("player2Name");

            initialize(player1Name, player2Name);
        }
        else {
            player1 = savedInstanceState.getParcelable("player1");
            player2 = savedInstanceState.getParcelable("player2");
            mIsPlayer1Turn= savedInstanceState.getBoolean("isPlayer1Turn");
            mDartsLeft= savedInstanceState.getInt("dartsLeft");

            //initialization not needed, all the data was restored from savedInstanceState so all that is left to do is to update the UI
            findElementsFromLayout();
            //show to the user who the active player is (who's turn it is)
            updateCurrentPlayerTextType();
            //show number of darts remaining in current players turn
            updateNrDarts();
            //show player points
            updateLayouts();
        }

        createOnTouchListenerForDartboardImage();
        updateScoreboard();
    }

    /*
     * function where we define onTouchListerner for dartboard image
     */
    private void createOnTouchListenerForDartboardImage() {
        //find the ImageView element
        mDartboardImageView = (ImageView) findViewById(R.id.dartboardImage);

        //create onTouchListener
        mDartboardImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                //get what type of action it was and coordinates
                final int action = ev.getAction();
                final int evX = (int) ev.getX();
                final int evY = (int) ev.getY();

                /*
                 * at the moment this could be a simple if statement, but to be more future proof in case we might need to do something on ACTION_DOWN
                 * it is done using switch
                 */
                switch (action) {
                    case MotionEvent.ACTION_UP :
                        // on the UP, we do the click action.
                        // the hidden image (image_areas) has 7 different hotspots on it.
                        // the colors are red - bullseye, green - 20, blue - 19, yellow - 18, pink - 17, teal - 16, black - 15
                        //get the color where the user "clicked" (released the click)
                        int touchColor = getHotspotColor (R.id.imageAreas, evX, evY);

                        // compare the touchColor to the expected values.
                        // to find out which number was "hit" and then call the functions to handle the darts logic (rules and such)
                        // note that we use a Color Tool object to test whether the observed color is close enough to the real color to count as a match.
                        // we do this because colors on the screen do not match the map exactly because of scaling and varying pixel density.
                        int tolerance = 25;

                        //loop over all colors (until we find a match)
                        for(int i=0; i < mMaskColors.length; i++) {
                            //check if it matches
                            if(closeMatch (mMaskColors[i], touchColor, tolerance)) {
                                //call the function that handles the hit
                                //and no need to look further, we found what color it was
                                hit(i, checkNrHits());
                                break;
                            }
                        }
                        break;
                } // end switch
                return true;
            }
        });
    }

    /*
     * check the multiplication buttons if we have a double or triple hit
     */
    private int checkNrHits() {
        //check if double button selected
        if(mX2Button.isSelected()) {
            return 2;
        }

        //check if triple button selected
        if(mX3Button.isSelected()) {
            return 3;
        }

        //no button selected, return 1
        return 1;
    }

    /*
     * here are all the game rules what should happen when current player hits a number
     * such as, checking if he has it still "open" - if so, add one more step toward closing it
     * if it is already closed, check if opponent has it closed and if he has not, add points to the current player,...
     * we also decrease number of darts available and check if its time to change the player already
     */
    private void hit(int index, int nrHits) {
        // check if tripple hit and index 6 (tripple 25 does not exist), notify user about problem
        if(nrHits == 3 && index == 6) {
            //notify user of error
            Toast toast = Toast.makeText(getApplicationContext(), R.string.trippleBullseyeNotice,Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL| Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            //return from the function (don't execute the rest of the code that follows in this function)
            return;
        }

        //loop for number of hits - to cover the double and triple hits
        for(int i=0; i<nrHits; i++) {
            if (mIsPlayer1Turn) {
                //player one hit something
                if (player1.isClosed(index)) {
                    if (!player2.isClosed(index)) {
                        //player 1 has closed it, but player 2 has not. Give points to player 1
                        player1.addScore(index);
                    }
                } else {
                    //player 1 has not closed it yet, add to hits
                    player1.hit(index);
                }
            } else {
                //player 2 hit something
                if (player2.isClosed(index)) {
                    if (!player1.isClosed(index)) {
                        //player 2 has closed it, but player 1 has not. Give points to player 2
                        player2.addScore(index);
                    }
                } else {
                    //player 2 has not closed it yet, add to hits
                    player2.hit(index);
                }
            }
        }

        //update the UI
        unselectMultiplyButtons();
        updateScore();
        updateProgressBar(index);

        //check if we have a winner yet
        checkWinCondition();

        //take one of the darts away, check if it is time to change the player already and give him 3 darts to use
        mDartsLeft--;
        if(mDartsLeft == 0) {
            changePlayer();
        }

        //update nr darts display
        updateNrDarts();

        //update the scoreboard, after that we are ready for the next action
        updateScoreboard();

    }

    /*
     * in order to win the player needs to hit each number 3 times AND have more points than his opponent
     */
    private void checkWinCondition() {
        //player 1 turn
        if(mIsPlayer1Turn) {
            //he has all the hits AND he has more points than player 2
            if(player1.allClosed() && (player1.getPoints() > player2.getPoints()) ) {
                //player 1 wins
                Intent i = new Intent(CricketScorerActivity.this, ResultsActivity.class);
                i.putExtra("losingPlayerName", player2.getName());
                i.putExtra("winningPlayerName", player1.getName());
                startActivity(i);
                finish();
            }
        }
        else {
            //player 2 turn
            //he has all the hits AND he has more points than player 2
            if(player2.allClosed() && (player2.getPoints() > player1.getPoints()) ) {
                //player 2 wins
                Intent i = new Intent(CricketScorerActivity.this, ResultsActivity.class);
                i.putExtra("losingPlayerName", player1.getName());
                i.putExtra("winningPlayerName", player2.getName());
                startActivity(i);
                finish();
            }
        }
    }

    /*
     * function that deselects both multiple hit buttons (x2 and x3)
     * we trigger that after every hit
     */
    private void unselectMultiplyButtons() {
        mX2Button.setSelected(false);
        mX2Button.setBackgroundColor(getResources().getColor(R.color.buttonDefault));

        mX3Button.setSelected(false);
        mX3Button.setBackgroundColor(getResources().getColor(R.color.buttonDefault));
    }

    /*
     * function responsible for updating the visual indication of progress
     * (how many out of the 3 hits required for each number has the user already made)
     */
    private void updateProgressBar(int index) {
        if(mIsPlayer1Turn) {
            mPlayer1ProgressBars[index].setRating(0.333f * player1.getNrHits(index));
        }
        else {
            mPlayer2ProgressBars[index].setRating(0.333f * player2.getNrHits(index));
        }
    }

    /*
     * function updates the number of points of the active user
     * (used every time when someone scores)
     */
    private void updateScore() {
        if(mIsPlayer1Turn) {
            mPlayer1ScoreView.setText(player1.getPoints() + "");
        }
        else {
            mPlayer2ScoreView.setText(player2.getPoints() + "");
        }
    }

    /*
     * function shows number of darts remaining in the currents player turn
     * (and makes sure the other player has no text in his indicator)
     */
    private void updateNrDarts() {
        if(mIsPlayer1Turn) {
            mPlayer1NrDartsView.setText(mDartsLeft + "");
            mPlayer2NrDartsView.setText("");
        }
        else {
            mPlayer1NrDartsView.setText("");
            mPlayer2NrDartsView.setText(mDartsLeft + "");
        }
    }

    /*
     * change the player who's turn it is now
     */
    public void changePlayer() {
        mIsPlayer1Turn = !mIsPlayer1Turn;

        String playerName;
        if(mIsPlayer1Turn) {
            playerName = player1.getName();
        }
        else {
            playerName = player2.getName();
        }

        //notify user
        Toast toast = Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.current_player_toast), playerName),Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL| Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();

        mDartsLeft = 3;
        updateNrDarts();

        //update display
        updateCurrentPlayerTextType();
    }


    /*
     * initialize all he variables that will be used in the course of one match
     * and show the initial values on the screen
     * some "showing/updating" of the data was extracted to individual functions,
     * mostly in cases when they make sense since they are also used in other methods of the activity
     */
    private void initialize(String player1Name, String player2Name) {
        //find elements from the layout that we need to manipulate later on
        findElementsFromLayout();

        //initialize the CricketScore objects to hold all the important player statistics
        player1 = new CricketScore(player1Name);
        player2 = new CricketScore(player2Name);

        //player 1 goes first and he has 3 darts
        mIsPlayer1Turn = true;
        mDartsLeft = 3;

        //show to the user who the active player is (who's turn it is)
        updateCurrentPlayerTextType();
        //show number of darts remaining in current players turn
        updateNrDarts();
        //show player points
        updateLayouts();

    }

    /*
     * update both player points - used on restore and when initializing
     */
    private void updateLayouts() {
        mPlayer1ScoreView.setText(player1.getPoints() + "");
        mPlayer2ScoreView.setText(player2.getPoints() + "");

        //show player names
        mPlayer1NameView.setText(player1.getName());
        mPlayer2NameView.setText(player2.getName());
    }

    /*
     * find (and store) references to the View elements we need to manipulate during the app execution
     * this is done in the initialization phase so the app does not to search for elements in the layout with every click, making it more responsive/faster
     */
    private void findElementsFromLayout() {
        mPlayer1NameView = (TextView) findViewById(R.id.player1_name);
        mPlayer2NameView = (TextView) findViewById(R.id.player2_name);

        mPlayer1ScoreView = (TextView) findViewById(R.id.player1_score);
        mPlayer2ScoreView = (TextView) findViewById(R.id.player2_score);

        //find nr darts left views
        mPlayer1NrDartsView = (TextView) findViewById(R.id.player1_nr_darts);
        mPlayer2NrDartsView = (TextView) findViewById(R.id.player2_nr_darts);

        //find the x2 and x3 buttons
        mX2Button = (Button) findViewById(R.id.x2_button);
        mX3Button = (Button) findViewById(R.id.x3_button);

        //initialize the references to progressbars
        mPlayer1ProgressBars = new RatingBar[7];
        mPlayer2ProgressBars = new RatingBar[7];

        for(int i=0; i<7; i++) {
            //get all the progress barrs for both players
            mPlayer1ProgressBars[i] = (RatingBar) findViewById(getResources().getIdentifier("progressbar_a_" + i, "id", getPackageName()));
            mPlayer2ProgressBars[i] = (RatingBar) findViewById(getResources().getIdentifier("progressbar_b_" + i, "id", getPackageName()));
        }
    }

    /*
     * update the text color and typeface to indicate which player's turn it is
     */
    private void updateCurrentPlayerTextType() {
        if(mIsPlayer1Turn) {
            mPlayer1NameView.setTypeface(null, Typeface.BOLD|Typeface.ITALIC);
            mPlayer1NameView.setTextColor(getResources().getColor(R.color.playerNameActive));

            mPlayer2NameView.setTypeface(null, Typeface.NORMAL);
            mPlayer2NameView.setTextColor(getResources().getColor(R.color.playerNamePasive));
        }
        else {
            mPlayer1NameView.setTypeface(null, Typeface.NORMAL);
            mPlayer1NameView.setTextColor(getResources().getColor(R.color.playerNamePasive));

            mPlayer2NameView.setTypeface(null, Typeface.BOLD|Typeface.ITALIC);
            mPlayer2NameView.setTextColor(getResources().getColor(R.color.playerNameActive));
        }
    }


    /*
     * update all the progressbars
     * used when resetting the game, otherwise we only update one at a time (the one that was hit)
     */
    private void updateScoreboard() {
        int[] player1Scores = player1.getScoreboard();
        int[] player2Scores = player2.getScoreboard();

        //update all the progressbars
        //if performance becomes a problem it would make sense to just update the one that has changed
        for(int i=0; i<mPlayer1ProgressBars.length; i++) {
            mPlayer1ProgressBars[i].setRating(0.333f * player1Scores[i]);
            mPlayer2ProgressBars[i].setRating(0.333f * player2Scores[i]);
        }
    }



    /*
     * helper function to get the color of the pixel from an image
     * used in finding out what the color is where the user has clicked on the dartboard
     * (on the mask where we define our "hit" fields for individual numbers)
     */
    private int getHotspotColor (int hotspotId, int x, int y) {
        ImageView img = (ImageView) findViewById (hotspotId);
        img.setDrawingCacheEnabled(true);
        Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache());
        img.setDrawingCacheEnabled(false);
        return hotspots.getPixel(x, y);
    }

    /*
     * helper function to determine if two colors are similar enough to consider them a match
     * this is used in checking if user clicked on a part of the image that we are interested in and to locate which color he clicked on
     */
    private boolean closeMatch (int color1, int color2, int tolerance) {
        if ((int) Math.abs (Color.red (color1) - Color.red (color2)) > tolerance )
            return false;
        if ((int) Math.abs (Color.green (color1) - Color.green (color2)) > tolerance )
            return false;
        if ((int) Math.abs (Color.blue (color1) - Color.blue (color2)) > tolerance )
            return false;
        return true;
    }



    /*
     * button onclick methods
     */

    /*
     * endTurn just calls the changePlayer function where all the logic is handled for when one user ends his turn
     */
    public void endTurn(View v) {
        changePlayer();
    }

    /*
     * reset function calls the initialize method (that resets all the progress to 0)
     * and calls updateScoreboard to show the changes to the user
     */
    public void reset(View v) {
        initialize(player1.getName(), player2.getName());

        updateScoreboard();

    }

    /*
     * onClick handler for x2 (double hit) button
     * if button was previously selected we now deselect it (and vice versa)
     * at the same time we make sure button x3 (tripple hit) is deselected, this only needs to happen when x2 was not selected to begin with
     */
    public void multiHit2(View v) {
        //update the selected value and background color of x2 button
        if(mX2Button.isSelected()) {
            mX2Button.setSelected(false);
            mX2Button.setBackgroundColor(getResources().getColor(R.color.buttonDefault));
        }
        else {
            //select the x2 button and change the background color
            mX2Button.setSelected(true);
            mX2Button.setBackgroundColor(getResources().getColor(R.color.buttonSelected));
            //make sure x3 button is not selected and has background color of not selected button
            mX3Button.setSelected(false);
            mX3Button.setBackgroundColor(getResources().getColor(R.color.buttonDefault));
        }

    }

    /*
     * onClick handler for x3 (tripple hit) button
     * if button was previously selected we now deselect it (and vice versa)
     * at the same time we make sure button x2 (double hit) is deselected, this only needs to happen when x3 was not selected to begin with
     */
    public void multiHit3(View v) {
        //update the selected value and background color of x3 button
        if(mX3Button.isSelected()) {
            mX3Button.setSelected(false);
            mX3Button.setBackgroundColor(getResources().getColor(R.color.buttonDefault));
        }
        else {
            //select the x3 button and change the background color
            mX3Button.setSelected(true);
            mX3Button.setBackgroundColor(getResources().getColor(R.color.buttonSelected));
            //make sure x2 button is not selected and has correct background color
            mX2Button.setSelected(false);
            mX2Button.setBackgroundColor(getResources().getColor(R.color.buttonDefault));
        }

    }
}
