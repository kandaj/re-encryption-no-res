package com.ebi.ega;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

public class GetMD5ForFile {

    public String getMD5(String fileSource) {
        String md5 = null;

        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(fileSource);

            // md5Hex converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
            // The returned array will be double the length of the passed array, as it takes two characters to represent any given byte.

            md5 = DigestUtils.md5Hex(IOUtils.toByteArray(fileInputStream));

            fileInputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return md5;
    }
}