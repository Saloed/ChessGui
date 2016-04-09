package conyashka.chess.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import conyashka.chess.R;
import conyashka.chess.board.Board;

public class GameActivity extends Activity {

    public static final String TAG = "APPLICATION_DEBUG";

    private static AlertDialog.Builder startDialog;
    private static AlertDialog.Builder finishedDialog;
    private static boolean finished;
    private boolean first = true;
    private Board theBoard;
    private boolean whitePlayerTurn = true;
    private TextView historyText;

    private boolean playVsComputer = false;
    private boolean playDemo = false;

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

//        int size = (getWindow().getAttributes().width) / 6;
//
//        Log.i(TAG, "size is " + size);

//        int size = 500;
//
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setAntiAlias(true);
//        paint.setFilterBitmap(true);
//        paint.setDither(true);
//
//        Canvas buttonCanvas = new Canvas();
//
//        Bitmap save = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.save),
//                size, size, true);
//
//        buttonCanvas.drawBitmap(save, 0, 0, paint);
//        Button saveButton = (Button) findViewById(R.id.saveButton);
//
//        saveButton.draw(buttonCanvas);

        //      saveButton.draw(buttonCanvas);

        //Construct our finished dialog

        Button saveButton = (Button) findViewById(R.id.saveButton);
        Button loadButton = (Button) findViewById(R.id.loadButton);
        Button helpButton = (Button) findViewById(R.id.helpButton);
        Button revertButton = (Button) findViewById(R.id.revertButton);
        Button settingsButton = (Button) findViewById(R.id.settingsButton);


        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theBoard.helpRequest();
            }
        });

        revertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theBoard.backTrack();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theBoard.saveToDatabase();
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theBoard.loadFromDatabase();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theBoard.editSettings();
            }
        });


        startDialog = new AlertDialog.Builder(this);
        startDialog.setCancelable(true);
        startDialog.setPositiveButton("Human", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                playDemo = false;
                playVsComputer = false;
                theBoard.acceptGameMode(playVsComputer, playDemo);
            }
        });
        startDialog.setNeutralButton("Computer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                playDemo = false;
                playVsComputer = true;
                theBoard.acceptGameMode(playVsComputer, playDemo);

            }
        });
        startDialog.setNegativeButton("Demo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                playDemo = true;
                playVsComputer = true;
                theBoard.acceptGameMode(playVsComputer, playDemo);

            }
        });

        startDialog.setMessage("Who is your opponent?");
        startDialog.show();

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


        historyText = (TextView) findViewById(R.id.textView);

        if (!whitePlayerTurn) {
            historyText.setBackgroundResource(R.drawable.back);
        } else {
            historyText.setBackgroundResource(R.drawable.back2);
        }

        //Log.i(TAG,"creation end");


    }


    /**
     * This function is called when a player has finished his move
     *
     * @param move
     */
    public void newTurn(String move) {

        //System.out.println("------------->" + theBoard.gameTerminal());
        whitePlayerTurn = !whitePlayerTurn;
        if ("".equals(move)) {

            CharSequence text = historyText.getText();
            String txt = String.valueOf(text);
            txt = txt.substring(0, txt.length() - 1);
            int pos = txt.lastIndexOf("\n");
            text = txt.substring(0, pos + 1);
            historyText.setText(text);
            whitePlayerTurn = true;
            historyText.setBackgroundResource(R.drawable.back2);
        } else if (!whitePlayerTurn) {
            historyText.append(" WHITE " + move + "   ");
            historyText.setBackgroundResource(R.drawable.back);
        } else {
            historyText.append("  BLACK " + move + " \n");
            historyText.setBackgroundResource(R.drawable.back2);
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
        theBoard.stopRequest();
    }

}