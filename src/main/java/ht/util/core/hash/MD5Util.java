package ht.util.core.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 13, 2005 Time: 5:15:10 PM
 */
public class MD5Util {
    public static final String getMessageDigestAsString(String s) {
        return getMessageDigestAsString(s.getBytes());
    }

    public static final String getMessageDigestAsString(byte[] defaultBytes) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(defaultBytes);
            byte messageDigest[] = algorithm.digest();

            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < messageDigest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }
            return messageDigest.toString();

        } catch (NoSuchAlgorithmException nsae) {
            return null;
        }
    }
}
