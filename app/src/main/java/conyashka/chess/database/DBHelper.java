package conyashka.chess.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String TableBoards = "boards";
    public static final String[] TableBoardsCols = {"_id", "name", "board", "finished"};
    public static final String sqlCreateTableBoards =
            "CREATE TABLE boards(" +
                    " _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " name TEXT unique," +
                    " board TEXT," +
                    " finished INTEGER" +
                    ");";
    private static final String DB_NAME = "ChessDB";
    private static final int DB_VERSION = 1;
    private static final String sqlDropTableBoards = "DROP TABLE IF EXISTS boards;";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sqlCreateTableBoards);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(sqlDropTableBoards);
        onCreate(db);
    }
}