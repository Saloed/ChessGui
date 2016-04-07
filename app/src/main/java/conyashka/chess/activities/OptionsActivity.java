package conyashka.chess.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import conyashka.chess.R;
import conyashka.chess.database.BoardAdapter;


public class OptionsActivity extends PreferenceActivity {

    private final BoardAdapter mSA = new BoardAdapter(this);

    private AlertDialog.Builder confirmDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.options);



        confirmDialog = new AlertDialog.Builder(this);
        confirmDialog.setMessage(R.string.confirm);
        confirmDialog.setCancelable(true);
        confirmDialog.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        //drop table and create a fresh one
                        mSA.drop();
                        mSA.create();
                        dialog.cancel();
                    }
                });
        confirmDialog.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        Preference myPref = findPreference("reset");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                confirmDialog.show();
                return true;
            }
        });
    }

}