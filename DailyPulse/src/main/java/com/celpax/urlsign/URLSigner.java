package com.celpax.urlsign;

import android.util.Base64;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

//import org.apache.commons.codec.binary.Base64;

/**
 * Utility class for Celpax URL signatures
 *
 * @author sergi
 *
 */
public class URLSigner {

    /**
     *
     * Signs the URL using the HMAC SHA512 algorithm
     *
     * @param url
     *            the URL to be signed
     * @param secret
     *            the key in Base64 format
     * @return the signed URL
     * @throws Exception
     */
    public static String sign(String url, String secret) throws Exception {
        byte[] plainSecret = Base64.decode(secret.getBytes("UTF-8"), Base64.NO_WRAP);
        Mac sha512SignatureKey = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretkey = new SecretKeySpec(plainSecret, "HmacSHA512");
        sha512SignatureKey.init(secretkey);
        byte[] signature = sha512SignatureKey.doFinal(getFormattedDate().getBytes("UTF-8"));

        Mac sha512Signature = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretkey2 = new SecretKeySpec(signature, "HmacSHA512");
        sha512Signature.init(secretkey2);

        byte[] mac_data = sha512Signature.doFinal(url.getBytes("UTF-8"));
        //return Base64.encodeBase64String(mac_data);
        return new String(Base64.encodeToString(mac_data, Base64.NO_WRAP));
    }

    /**
     * Returns the UTC date in YYYYMMDD format
     */
    public static String getFormattedDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Log.d("URLSigner", "DailyPulse: onClick: Today's date: " + formatter.format(new Date()));

        return formatter.format(new Date());
        //return "20160113";
    }
}