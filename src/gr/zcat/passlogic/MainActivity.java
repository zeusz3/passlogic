package gr.zcat.passlogic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.TreeSet;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements OnSharedPreferenceChangeListener {
	
	private final Hashtable<Byte, String> allowedSymbols = new Hashtable<Byte, String>(256);
	private final char[] allSymbols = {'a', '<', 'b', ',', 'c', '.', 'd', '>', 'e', '?', 'f', '/', 'g', ';', 'h', ':', 'i',
	        '|', 'j', '}', 'k', ']', 'l', '[', 'm', '{', 'n', '=', 'o', '+', 'p', '-', 'q', '_', 'r', ')', 's', '(', 't', '*', 'u', '&', 'v',
	        '^', 'w', '%', 'x', '$', 'y', '#', 'z', '@', 'A', '!', 'B', '~', 'C','`', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
	        'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
	private final TreeSet<String> bannedSymbols = new TreeSet<String>();
	private SharedPreferences sharedPref;
	//{'\\', '\"', '\''};
	private int outputLength, numberOfLogics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        numberOfLogics = Integer.parseInt(sharedPref.getString("numberOfLogics", "5"));
        outputLength = Integer.parseInt(sharedPref.getString("outputLength", "12"));
        initializeDict();
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, PrefsActivity.class);
            startActivityForResult(intent, 0); 
      
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("outputLength")) {
            outputLength = Integer.parseInt(sharedPref.getString("outputLength", "12"));
        }
        Toast.makeText(getApplicationContext(), outputLength, Toast.LENGTH_SHORT).show();
    }
    
    public void getPass(View view) {
		EditText editText1 = (EditText) findViewById(R.id.logicETX1);
		EditText editText2 = (EditText) findViewById(R.id.logicETX2);
		EditText editText3 = (EditText) findViewById(R.id.logicETX3);
		EditText editText4 = (EditText) findViewById(R.id.logicETX4);
		EditText editText5 = (EditText) findViewById(R.id.logicETX5);
		//EditText passLogicETX = (EditText) findViewById(R.id.passLogicETX);
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");
			byte[][] hash = new byte[numberOfLogics][];
			hash[0] = md.digest(editText1.getText().toString().getBytes());
			hash[1] = md.digest(editText2.getText().toString().getBytes());
			hash[2] = md.digest(editText3.getText().toString().getBytes());
			hash[3] = md.digest(editText4.getText().toString().getBytes());
			hash[4] = md.digest(editText5.getText().toString().getBytes());
			StringBuilder sb = new StringBuilder(hash[0].length);
			for(int i = 0; i < hash[0].length && i < outputLength*(hash[0].length/outputLength); i+=(hash[0].length/(outputLength))) {
                int b = 0;
                for (int j = 0; j < numberOfLogics; j++) {
                    b ^= hash[j][i];
                }
                sb.append(allowedSymbols.get((byte)b));
            }
			AlertDialog.Builder ad = new AlertDialog.Builder(this);
			ad.setMessage(sb.toString());
			AlertDialog dialog = ad.create();
			dialog.show();
		} catch (NoSuchAlgorithmException e) {
			editText2.setText("no such algo");
			e.printStackTrace();
		}
	}
    
    private void initializeDict() {
        for(int i = 0; i < allSymbols.length; i++) {
        	if(!sharedPref.getBoolean(String.valueOf(allSymbols[i]), true)) {
        		bannedSymbols.add(String.valueOf(allSymbols[i]));
        	}
        }
        Toast.makeText(getApplicationContext(), String.valueOf(bannedSymbols.size()), Toast.LENGTH_SHORT).show();
        int j = 0;
    	for(int i = 0; i < 256; i++) {
            if(!bannedSymbols.contains(String.valueOf(allSymbols[j % 81]))) {
                allowedSymbols.put((byte)i, String.valueOf(allSymbols[j % 81]));
            } else {
                while(bannedSymbols.contains(String.valueOf(allSymbols[++j % 81]))) {
                        //j++;
                }
                allowedSymbols.put((byte)i, String.valueOf(allSymbols[j % 81]));
            }
            j++;
    	}
    }
}
