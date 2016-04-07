package conyashka.chess.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import conyashka.chess.R;
import conyashka.chess.board.Board;

public class GameActivity extends Activity {

    public static final String TAG="APPLICATION_DEBUG";

    private static AlertDialog.Builder finishedDialog;
    private static boolean finished;
    private boolean first = true;
    private Board theBoard;
    private boolean whitePlayerTurn = true;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Log.i(TAG,"creation start");

        Bundle extras = getIntent().getExtras();

        SharedPreferences shareSettings = getSharedPreferences("MyPrefs", 0);
        SharedPreferences defaultSettings = PreferenceManager.getDefaultSharedPreferences(this);

        shareSettings.edit().putBoolean("is_first_time", false).apply();

        String lineNumbers = defaultSettings.getString("showLineNumbers", getString(R.string.optionLineNumberValues_OnlyIfBigEnough));
        String soundVolume = defaultSettings.getString("soundVolume", "0");
        theBoard = (Board) findViewById(R.id.board);


        theBoard.setPreferences(Integer.parseInt(soundVolume));


        //Construct our finished dialog

        finishedDialog = new AlertDialog.Builder(this);
        finishedDialog.setCancelable(true);
        finishedDialog.setPositiveButton(R.string.mainMenu,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        finishedDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        TextView p1 = (TextView) findViewById(R.id.player1);
        TextView p2 = (TextView) findViewById(R.id.player2);


        if (!whitePlayerTurn) {
            p2.setBackgroundResource(R.drawable.back2);
            p1.setBackgroundResource(R.drawable.back);
        } else {
            p2.setBackgroundResource(R.drawable.back);
            p1.setBackgroundResource(R.drawable.back2);
        }

        //Log.i(TAG,"creation end");

    }


    /**
     * This function is called when a player has finished his move
     */
    public void newTurn() {

        //System.out.println("------------->" + theBoard.gameTerminal());
        TextView p1 = (TextView) findViewById(R.id.player1);
        TextView p2 = (TextView) findViewById(R.id.player2);
        whitePlayerTurn = !whitePlayerTurn;


        if (!whitePlayerTurn) {
            p2.setBackgroundResource(R.drawable.back2);
            p1.setBackgroundResource(R.drawable.back);
        } else {
            p2.setBackgroundResource(R.drawable.back);
            p1.setBackgroundResource(R.drawable.back2);

        }
    }


    public void playerwon(int winner) {

        if (winner != 0) {
            theBoard.finished();
            finishedDialog.setMessage("White won the game \n");
            finishedDialog.show();


        } else {
            theBoard.finished();
            finishedDialog.setMessage("Black won the game \n");
            finishedDialog.show();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //save the current position in preference
        SharedPreferences tempSettings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = tempSettings.edit();

        editor.putBoolean("playerTurn", whitePlayerTurn);
        editor.putString("fen", theBoard.getGameState());

        // Commit the edits!
        editor.apply();
    }

}