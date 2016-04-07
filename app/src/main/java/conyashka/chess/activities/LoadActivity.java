package conyashka.chess.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

import conyashka.chess.R;
import conyashka.chess.database.BoardAdapter;
import conyashka.chess.database.DBHelper;

/**
 * Created by admin on 05.04.2016.
 */
public class LoadActivity extends ListActivity {

    private final BoardAdapter mSA = new BoardAdapter(this);
    private SimpleCursorAdapter mCA;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Cursor cursor = mSA.queryBoards();
        String cols[] = DBHelper.TableBoardsCols;
        String from[] = {cols[1]};
        int to[] = {R.id.name};
        startManagingCursor(cursor);
        mCA = new SimpleCursorAdapter(this, R.layout.activity_load, cursor, from, to);


        mCA.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                ((Button) view).setText((cursor.getString(cursor.getColumnIndex("name"))));
                return true;
            }
        });

        setListAdapter(mCA);

    }

    public void gameChosen(View view) {

        Button text = (Button) view;
        Cursor cursor = mSA.queryBoards(text.getText().toString());
        cursor.moveToNext();
        //System.out.println("bla :" + cursor.getString(2));

        Intent theIntent = new Intent(this, GameActivity.class);
        theIntent.removeExtra("LoadGame");
        theIntent.putExtra("LoadGame", true);
        theIntent.putExtra("BoardState", cursor.getString(2));
        theIntent.putExtra("turn", cursor.getInt(5));

        startActivity(theIntent);
/*
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra("index", index);
        startActivity(intent);*/
    }
}