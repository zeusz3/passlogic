package gr.zcat.passlogic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.TreeSet;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
	
	private final Hashtable<Byte, String> allowedSymbols = new Hashtable<Byte, String>(256);
	private final char[] allSymbols = {'a', '<', 'b', ',', 'c', '.', 'd', '>', 'e', '?', 'f', '/', 'g', ';', 'h', ':', 'i',
	        '|', 'j', '}', 'k', ']', 'l', '[', 'm', '{', 'n', '=', 'o', '+', 'p', '-', 'q', '_', 'r', ')', 's', '(', 't', '*', 'u', '&', 'v',
	        '^', 'w', '%', 'x', '$', 'y', '#', 'z', '@', 'A', '!', 'B', '~', 'C','`', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
	        'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
	private final TreeSet<String> bannedSymbols = new TreeSet<String>();
	private SharedPreferences sharedPref;
	//{'\\', '\"', '\''};
	private int outputLength, numberOfLogics;
	private LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ll= new LinearLayout(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        initializeView();
    }
    
    private void initializeView() {
    	numberOfLogics = Integer.parseInt(sharedPref.getString("numberOfLogics", "5"));
    	ll.removeAllViews();
    	ll.setOrientation(LinearLayout.VERTICAL);
    	LayoutParams lp = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	for(int i = 0; i < numberOfLogics; i++) {
    		EditText et = new EditText(this);
    		et.setId(i);
    		et.setHint("Logic"+(i+1));
    		et.setLayoutParams(lp);
    		ll.addView(et);
    	}
    	Button cypher = new Button(this);
    	lp = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	cypher.setText(R.string.cypher);
    	cypher.setLayoutParams(lp);
    	View.OnClickListener cypherBTNListener = new View.OnClickListener() {
    		  public void onClick(View v) {
    			  getPass();
    		  }
    	};
    	cypher.setOnClickListener(cypherBTNListener);
    	ll.addView(cypher);
    	this.setContentView(ll);
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
    
    public void getPass() {
    	initializeDict();
    	outputLength = Integer.parseInt(sharedPref.getString("outputLength", "12"));
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");
			byte[][] hash = new byte[numberOfLogics][];
			for(int i = 0; i < numberOfLogics; i++) {
				hash[i] = md.digest(((EditText) findViewById(i)).getText().toString().getBytes());
			}
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
			Toast.makeText(this, "no such algo", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
    
    private void initializeDict() {
    	if(bannedSymbols.size() > 0) {
    		bannedSymbols.clear();
    	}
        for(int i = 0; i < allSymbols.length; i++) {
        	if(!sharedPref.getBoolean(String.valueOf(allSymbols[i]), false)) {
        		bannedSymbols.add(String.valueOf(allSymbols[i]));
        	}
        }
        int j = 0;
    	for(int i = 0; i < 256; i++) {
            if(!bannedSymbols.contains(String.valueOf(allSymbols[j % allSymbols.length]))) {
                allowedSymbols.put((byte)i, String.valueOf(allSymbols[j % allSymbols.length]));
            } else {
                while(bannedSymbols.contains(String.valueOf(allSymbols[++j % allSymbols.length]))) {
                }
                allowedSymbols.put((byte)i, String.valueOf(allSymbols[j % allSymbols.length]));
            }
            j++;
    	}
    }
}
