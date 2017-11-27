package com.parad.security;

/**
 * Created by parad on 26.11.2017.
 */

import android.util.Log;


import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import android.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;


public class AesCrypt {

    public static String encode(String str) {
        String text = str;
        String key = "asdfghjklkjzxcvb";
        SecretKeySpec sks = null;
        final byte[] keyBytes = key.getBytes();

        // Original text
        // Set up secret key spec for 128-bit AES encryption and decryption


            sks = new SecretKeySpec(keyBytes, "AES");


        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(text.getBytes());
        } catch (Exception e) {
            Log.e("Crypto", "AES encryption error");
        }
        text = Base64.encodeToString(encodedBytes, Base64.DEFAULT) + "::" + key;
        String text1 = "";

        for (String retval : text.split("\n")) {
            text1 += retval;
        }


        return text1;


    }

    public static String decode(String str, String key){
        String text = str;
        SecretKeySpec sks = null;

        // Original text
        // Set up secret key spec for 128-bit AES encryption and decryption
        final byte[] keyBytes = key.getBytes();


            sks = new SecretKeySpec(keyBytes, "AES");



        byte[] encodedBytes = null;
        encodedBytes = Base64.decode(text.getBytes(), Base64.DEFAULT);
        // Decode the encoded data with AES
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, sks);
            decodedBytes = c.doFinal(encodedBytes);
        } catch (Exception e) {
            Log.e("Crypto", "AES decryption error");
        }
        return new String(decodedBytes);


    }


}
