package com.example.hopcardtrial;

import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Attempt to read 14443-3 Smart card via NFC.
 * Features code from John Philip Bigcas:
 *   http://noobstah.blogspot.co.nz/2013/04/mifare-desfire-ev1-and-android.html
 */
public class MainActivity extends Activity {

	private IntentFilter[] intentFiltersArray;
	private String[][] techListsArray;
	private NfcAdapter mAdapter;
	private PendingIntent pendingIntent;
	private TextView mTransBytes;
	private TextView mDecId;
	private TextView mHexId;
	private TextView mInfo;
	private IsoDep mNfc;
	private StringBuilder mStringBuilder;

	// Desfire commands
	private static final byte SELECT_COMMAND = (byte) 0x5A;
	private static final byte AUTHENTICATE_COMMAND = (byte) 0x0A;
	private static final byte READ_DATA_COMMAND = (byte) 0xBD;
	private static final byte[] NATIVE_AUTHENTICATION_COMMAND = new byte[] {
			(byte) 0x0A, (byte) 0x00 };
	private static final byte[] NATIVE_SELECT_COMMAND = new byte[] {
			(byte) 0x5A, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mAdapter = NfcAdapter.getDefaultAdapter(this);

		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException();
		}

		intentFiltersArray = new IntentFilter[] { ndef, };
		techListsArray = new String[][] { new String[] { NfcA.class.getName() } };

		// Initialise TextView fields
		mHexId = (TextView) findViewById(R.id.hexId);
		mDecId = (TextView) findViewById(R.id.decId);
		mTransBytes = (TextView) findViewById(R.id.transceivableBytes);
		mInfo = (TextView) findViewById(R.id.infoView);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void processIntent(Intent intent) {
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		mNfc = IsoDep.get(tag);
		try {
			mNfc.connect();
			Log.v("tag", "connected.");
			byte[] id = mNfc.getTag().getId();
			Log.v("tag", "Got id from tag:" + id);
			mHexId.setText(getHex(id));
			mDecId.setText(getDec(id));
			mTransBytes.setText("" + mNfc.getMaxTransceiveLength());

			byte[] response = mNfc.transceive(NATIVE_SELECT_COMMAND);
			displayText("Select App", getHex(response));
			authenticate();
			String read = readCommand();
			displayText("Read",read);
			
		} catch (IOException e) {
			// TODO: handle exception
		} finally {
			if (mNfc != null) {
				try {
					mNfc.close();
				} catch (IOException e) {
					Log.v("tag", "error closing the tag");
				}
			}
		}
	}

	private String readCommand() {
		byte fileNo = (byte) 0x01;
		byte[] offset = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		byte[] length = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		byte[] message = new byte[8];
		message[0] = READ_DATA_COMMAND;
		message[1] = fileNo;

		System.arraycopy(offset, 0, message, 2, 3);
		System.arraycopy(length, 0, message, 2, 3);

		byte[] response;
		try {
			response = mNfc.transceive(message);
			Toast.makeText(this, "Response Length = " + response.length, Toast.LENGTH_LONG).show();
			return getHex(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Read failed";
	}

	private void authenticate() {
		// TODO Auto-generated method stub
		byte[] rndB = new byte[8];
		byte[] response;
		try {
			response = mNfc.transceive(NATIVE_AUTHENTICATION_COMMAND);
			System.arraycopy(response, 1, rndB, 0, 8);

			byte[] command = new byte[17];

			System.arraycopy(DES.gen_sessionKey(rndB), 0, command, 1, 16);
			command[0] = (byte) 0xAF;

			response = mNfc.transceive(command);
			displayText("Authentication Status",getHex(response));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getDec(byte[] bytes) {
		long result = 0;
		long factor = 1;
		for (int i = 0; i < bytes.length; ++i) {
			long value = bytes[i] & 0xffl;
			result += value * factor;
			factor *= 256l;
		}
		return result + "";
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
	
	private void displayText(String label, String text){
		if (mStringBuilder == null){
			mStringBuilder = new StringBuilder();
		}
		if (label != null){
			mStringBuilder.append(label);
			mStringBuilder.append(":");
		}
		mStringBuilder.append(text);
		mStringBuilder.append("\n");
		
		mInfo.setText(mStringBuilder.toString());
	}

	@Override
	public void onPause() {
		super.onPause();
		mAdapter.disableForegroundDispatch(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		mAdapter.enableForegroundDispatch(this, pendingIntent,
				intentFiltersArray, techListsArray);
	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.v("tag", "In onNewIntent");
		processIntent(intent);
	}

}
