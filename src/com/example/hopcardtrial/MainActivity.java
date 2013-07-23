package com.example.hopcardtrial;

import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {

	private IntentFilter[] intentFiltersArray;
	private String[][] techListsArray;
	private NfcAdapter mAdapter;
	private PendingIntent pendingIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		
		pendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType("*/*");
		} catch (MalformedMimeTypeException e){
			throw new RuntimeException();
		}
		
		intentFiltersArray = new IntentFilter[] {ndef, };
		techListsArray = new String[][] { new String[] {NfcA.class.getName()}};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void processIntent(Intent intent) {
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		NfcA nfc = NfcA.get(tag);
		try {
			StringBuilder sb = new StringBuilder();
			nfc.connect();
			Log.v("tag", "connected.");
			byte[] id = nfc.getTag().getId();
			Log.v("tag", "Got id from tag:" + id);
			sb.append(getHex(id));
			Toast.makeText(this, "Hex: " + sb.toString(), Toast.LENGTH_LONG)
					.show();
			nfc.getMaxTransceiveLength();
		} catch (IOException e) {
			// TODO: handle exception
		} finally {
			if (nfc != null) {
				try {
					nfc.close();
				} catch (IOException e) {
					Log.v("tag", "error closing the tag");
				}
			}
		}
	}

	private String getHex(byte[] bytes) {
		Log.v("tag", "Getting hex");
		StringBuilder sb = new StringBuilder();
		for (int i = bytes.length - 1; i >= 0; --i) {
			int b = bytes[i] & 0xff;
			if (b < 0x10)
				sb.append('0');
			sb.append(Integer.toHexString(b));
			if (i > 0) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	@Override
	public void onPause(){
		super.onPause();
		mAdapter.disableForegroundDispatch(this);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		Log.v("tag", "In onNewIntent");
	    processIntent(intent);
	}
	
}
