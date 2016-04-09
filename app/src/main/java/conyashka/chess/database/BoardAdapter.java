package conyashka.chess.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

public class BoardAdapter {

    private final Context context;
    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public BoardAdapter(Context c) {
        context = c;
    }

    private BoardAdapter openToRead() {
        dbHelper = new DBHelper(context);
        db = dbHelper.getReadableDatabase();
        return this;
    }

    private BoardAdapter openToWrite() {
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    private void close() {
        db.close();
    }

    public long insertBoard(String name, String board, int finished) {
        String[] cols = DBHelper.TableBoardsCols;
        ContentValues contentValues = new ContentValues();
        contentValues.put(cols[1], name);
        contentValues.put(cols[2], board);
        contentValues.put(cols[3], ((Integer) finished).toString());
        openToWrite();
        long value = db.insert(DBHelper.TableBoards, null, contentValues);
        close();
        return value;
    }

    public Cursor queryBoards() {
        openToRead();
        return db.query(DBHelper.TableBoards,
                DBHelper.TableBoardsCols, null, null, null, null, null);
    }

    public void drop() {
        openToRead();
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TableBoards);
    }

    public void create() {
        db.execSQL(DBHelper.sqlCreateTableBoards);
    }

    public Cursor queryBoards(String name) {
        openToRead();
        String[] cols = DBHelper.TableBoardsCols;
        return db.query(DBHelper.TableBoards,
                cols, cols[1] + " = \"" + name + "\"", null, null, null, null);
    }

    public long count() {
        openToRead();
        long returnValue = DatabaseUtils.queryNumEntries(db, "boards");
        close();
        return returnValue;
    }
}