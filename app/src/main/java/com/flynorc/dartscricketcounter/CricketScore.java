package com.flynorc.dartscricketcounter;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Flynorc on 05-Apr-17.
 */

/*
 * Object representing one players stats in a dartboard game of Cricket
 * we are only interested when player throws a dart in fields 15-20 and bullseye, all other fields are not relavant for the game
 * so we create a smaller array of 7 elements and map the hits to those numbers 15 to 1st element, 16 to 2nd, bullseye to last elemnt
 * I'm not sure what the common practice is in regard of checking for indexOutOfBound, but since it is only used in the simple CricketScorerActivity
 * and all calls from there will include only indexes 0-6 I left the checks out.
 */
public class CricketScore implements Parcelable {
    private String mPlayerName;
    private int mPoints;
    private int[] mScoreboard;

    private static final int[] pointsMapping = new int[] {15, 16, 17, 18, 19, 20, 25};

    public CricketScore(String playerName) {
        //initialize the player name to the parameter that was passed, reset points and number of hits for each relevant field (15-20 & bullseye)
        mPlayerName = playerName;
        mPoints = 0;
        mScoreboard =  new int[] {0, 0, 0, 0, 0, 0, 0};
    }



    public int getPoints() {
        return mPoints;
    }

    public String getName() {
        return mPlayerName;
    }

    public int[] getScoreboard() {
        return mScoreboard;
    }

    public int getNrHits(int index) {
        return mScoreboard[index];
    }

    /*
     * method checks if user already hit the element at given index 3 times or not
     */
    public boolean isClosed(int index) {
        return mScoreboard[index] == 3;
    }

    /*
     * method increments the number of hits to the given index
     */
    public void hit(int index) {
        //only do so if maximum of 3 hits has not yet been reached
        if(mScoreboard[index] < 3) {
            mScoreboard[index]++;
        }
    }

    public boolean allClosed() {
        //loop over all the elements and return false as soon as we find one that is not closed
        //since all elements need to be closed. If the loop does not find one element that is not closed, we return true (all are indeed closed)
        for(int i=0; i<mScoreboard.length; i++) {
            if(!isClosed(i)) {
                return false;
            }
        }

        return true;
    }

    /*
     * method adds the number of points correcponding to the index the user "hit"
     * this should be called when user has already closed that element (number), but opponent has not
     */
    public void addScore(int index) {
        mPoints += pointsMapping[index];
    }


    public String toString() {
        return mPlayerName + " has: " + mPoints;
    }


    /*
     * implementing parcelable
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mPlayerName);
        dest.writeInt(mPoints);
        dest.writeIntArray(mScoreboard);
    }

    protected CricketScore(Parcel in) {
        mPlayerName = in.readString();
        mPoints = in.readInt();
        mScoreboard = in.createIntArray();
    }

    public static final Creator<CricketScore> CREATOR = new Creator<CricketScore>() {
        @Override
        public CricketScore createFromParcel(Parcel in) {
            return new CricketScore(in);
        }

        @Override
        public CricketScore[] newArray(int size) {
            return new CricketScore[size];
        }
    };
}
