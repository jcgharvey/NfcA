package com.example.hopcardtrial;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DES {

	public static byte[] gen_sessionKey(byte[] b) {

		byte[] key = new byte[] { (byte) 0x0, (byte) 0x0, (byte) 0x0,
				(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
				(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
				(byte) 0x0, (byte) 0x0, (byte) 0x0 };
		byte[] response = decrypt(key, b);
		byte[] rndB = response;
		byte[] rndA = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00 };
		byte[] rndAB = new byte[16];
		System.arraycopy(rndA, 0, rndAB, 0, 8);
		rndB = leftShift(rndB);
		rndB = xorBytes(rndA, rndB);
		rndB = decrypt(key, rndB);
		System.arraycopy(rndB, 0, rndAB, 8, 8);
		return rndAB;
	}

	private static byte[] xorBytes(byte[] rndA, byte[] rndB) {
		// TODO Auto-generated method stub
		byte[] b = new byte[rndB.length];
		for (int i = 0; i < rndB.length; i++) {
			b[i] = (byte) (rndA[i] ^ rndB[i]);
		}
		return b;
	}

	public static byte[] leftShift(byte[] data) {
		// TODO Auto-generated method stub
		byte[] temp = new byte[data.length];
		temp[data.length - 1] = data[0];
		for (int i = 1; i < data.length; i++) {
			temp[i - 1] = data[i];
		}
		return temp;
	}

	public static byte[] decrypt(byte[] key, byte[] enciphered_data) {

		try {
			byte[] iv = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
					0x00 };
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
			SecretKey s = new SecretKeySpec(key, "DESede");
			Cipher cipher;
			cipher = Cipher.getInstance("DESede/CBC/NoPadding", "BC");
			cipher.init(Cipher.DECRYPT_MODE, s, ivParameterSpec);
			byte[] deciphered_data = cipher.doFinal(enciphered_data);
			return deciphered_data;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}