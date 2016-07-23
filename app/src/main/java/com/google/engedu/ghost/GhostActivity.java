package com.google.engedu.ghost;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;


public class GhostActivity extends AppCompatActivity {
    private static final String COMPUTER_TURN = "Computer's turn";
    private static final String USER_TURN = "Your turn";
    private GhostDictionary dictionary;
    private boolean userTurn = false;
    private Random random = new Random();
    private String wordFragment = "";
    TextView statusTextView;
    TextView fragmentTextView;
    TextView userScoreView;
    TextView computerScoreView;
    Button resetButton;
    Button challengeButton;
    Switch addToEnd;
    private String gameStatus;
    private int userScore = 0;
    private int computerScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghost);
        AssetManager assetManager = getAssets();

        // grab views by their IDs
        statusTextView = (TextView) findViewById(R.id.gameStatus);
        fragmentTextView = (TextView) findViewById(R.id.ghostText);
        userScoreView = (TextView) findViewById(R.id.userScoreView);
        computerScoreView = (TextView) findViewById(R.id.computerScoreView);
        resetButton = (Button) findViewById(R.id.button2);
        challengeButton = (Button) findViewById(R.id.button);
        addToEnd = (Switch) findViewById(R.id.inputAtEnd);

        // start game with adding to the end of the string enabled
        addToEnd.setChecked(true);
        try {
            InputStream inputStream = assetManager.open("words.txt");
            dictionary = new SimpleDictionary(inputStream);
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        onStart(null);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent key){
        // check that the game is active
        if (challengeButton.isEnabled()) {

            // make sure the input is a letter
            if (Character.isLetter((char) key.getUnicodeChar())) {

                // determine if the letter should be added at the end or the beginning
                if (addToEnd.isChecked()){
                    wordFragment += (char) key.getUnicodeChar();
                }
                else {
                    wordFragment = (char) key.getUnicodeChar() + wordFragment;
                }
                fragmentTextView.setText(wordFragment);
                computerTurn();
                return true;
            }
        }
        return super.onKeyUp(keyCode, key);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ghost, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handler for the "Reset" button.
     * Randomly determines whether the game starts with a user turn or a computer turn.
     * @param view
     * @return true
     */
    public boolean onStart(View view) {
        userTurn = random.nextBoolean();
        fragmentTextView.setText("");
        if (userTurn) {
            statusTextView.setText(USER_TURN);
        } else {
            statusTextView.setText(COMPUTER_TURN);
            computerTurn();
        }
        return true;
    }

    private void computerTurn() {
        // check if the user finished turn by creating an actual word
        if (wordFragment.length() >= 4 && dictionary.isWord(wordFragment)) {
            statusTextView.setText("Computer Wins!");
            computerScore++;
            computerScoreView.setText("Computer Score: " + computerScore);
            challengeButton.setEnabled(false);
            return;
        }

        // retrieve a random word that includes wordFragment as prefix
        // if no word retrieved, computer wins
        String possible = dictionary.getGoodWordStartingWith(wordFragment);
        if (possible == null) {
            statusTextView.setText("Computer challenges and Wins!");
            computerScore++;
            computerScoreView.setText("Computer Score: " + computerScore);
            challengeButton.setEnabled(false);
            return;
        }

        // add next letter that would work to create a word
        else{
            wordFragment += possible.charAt(wordFragment.length());
            fragmentTextView.setText(wordFragment);
        }

        // Do computer turn stuff then make it the user's turn again
        userTurn = true;
        statusTextView.setText(USER_TURN);
    }
    public void challenge (View view){
        // check if the current word fragment is a real word or if that word cannot be a made into a word
        if ((wordFragment.length() >= 4 && dictionary.isWord(wordFragment)) || dictionary.getAnyWordStartingWith(wordFragment) == null){
            statusTextView.setText("User Wins!");
            userScore++;
            userScoreView.setText("User Score: " + userScore);
            challengeButton.setEnabled(false);
            return;
        }

        // challenge fails and the challenger loses the game
        else {
            statusTextView.setText("Challenge failed and Computer wins");
            computerScore++;
            computerScoreView.setText("Computer Score: " + computerScore);
            challengeButton.setEnabled(false);
        }
    }

    public void resetGame (View view){
        challengeButton.setEnabled(true);
        wordFragment = "";
        userTurn = random.nextBoolean();
        fragmentTextView.setText(wordFragment);
        if (userTurn) {
            statusTextView.setText(USER_TURN);
        } else {
            statusTextView.setText(COMPUTER_TURN);
            computerTurn();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        gameStatus = (String) statusTextView.getText();
        if (challengeButton.isEnabled())
            savedInstanceState.putBoolean("Challenge Button Status",true);
        else
            savedInstanceState.putBoolean("Challenge Button Status",false);
        if (addToEnd.isChecked())
            savedInstanceState.putBoolean("Switch Status",true);
        else
            savedInstanceState.putBoolean("Switch Status",false);
        savedInstanceState.putString("Game Status",gameStatus);
        savedInstanceState.putString("Word Fragment",wordFragment);
        savedInstanceState.putInt("User Score",userScore);
        savedInstanceState.putInt("Computer Score", computerScore);
    }
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        wordFragment = savedInstanceState.getString("Word Fragment");
        gameStatus = savedInstanceState.getString("Game Status");
        userScore = savedInstanceState.getInt("User Score");
        computerScore = savedInstanceState.getInt("Computer Score");
        fragmentTextView.setText(wordFragment);
        statusTextView.setText(gameStatus);
        computerScoreView.setText("Computer Score: " + computerScore);
        userScoreView.setText("User Score: " + userScore);
        challengeButton.setEnabled(savedInstanceState.getBoolean("Challenge Button Status"));
        addToEnd.setChecked(savedInstanceState.getBoolean("Switch Status"));
    }
}
