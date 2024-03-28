package com.github.starnowski.db2.fun;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import jakarta.xml.bind.DatatypeConverter;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumMD5Utils {

    public static String calculateMD5ChecksumForByteArrayWithFirstStrategy(byte[] array) throws NoSuchAlgorithmException {
        MessageDigest mdInstance = MessageDigest.getInstance("MD5");
        mdInstance.update(array);
        return DatatypeConverter.printHexBinary(mdInstance.digest()).toUpperCase();
    }

    public static String calculateMD5ChecksumForByteArrayWithSecondStrategy(byte[] array) {
        return DigestUtils.md5Hex(array).toUpperCase();
    }

    public static String calculateMD5ChecksumForByteArrayWithThirdStrategy(byte[] array) throws NoSuchAlgorithmException {
        byte[] hash = MessageDigest.getInstance("MD5").digest(array);
        return new BigInteger(1, hash).toString(16).toUpperCase();
    }
    public static String calculateMD5ChecksumForByteArrayWithFourthStrategy(byte[] array) throws NoSuchAlgorithmException, IOException {
        ByteSource byteSource = ByteSource.wrap(array);
        HashCode hc = byteSource.hash(Hashing.md5());
        return hc.toString().toUpperCase();
    }
}
