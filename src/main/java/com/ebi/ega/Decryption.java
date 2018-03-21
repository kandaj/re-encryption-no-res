package com.ebi.ega;


import org.apache.commons.io.FileUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static java.security.MessageDigest.getInstance;

public class Decryption {
    public void Decrypt(String inFile, String outFile, String keyPath) {
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        byte[] salt = new byte[]{-12, 34, 1, 0, -98, -33, 78, 21};
        String key = null;
        try {
            in = new BufferedInputStream(new FileInputStream(inFile));
            out = new BufferedOutputStream(FileUtils.openOutputStream(new File(outFile)));
            int i;
            key = new Scanner(new File(keyPath)).useDelimiter("\\Z").next();
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 1024, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            byte[] random_iv = new byte[16];
            int read = in.read(random_iv, 0, 16);
            if (read != 16) {
                System.err.println("Oops, didnt find random_iv");
            }
            IvParameterSpec paramSpec = new IvParameterSpec(random_iv);
            Cipher cipher = null;
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(2, (Key)secret, paramSpec);
            CipherInputStream cIn = new CipherInputStream(in, cipher);
            byte[] block = new byte[4096];
            while ((i = cIn.read(block)) != -1) {
                out.write(block, 0, i);
                out.flush();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public String getMD5(ArrayList fileIndex, HashMap configKeyPath) throws FileNotFoundException, NoSuchAlgorithmException {
        String stagingSource = (String) ((EGAFile)fileIndex.get(0)).stagingSource;
        String InternalCGPKey = new Scanner(new File(configKeyPath.get("internal_key").toString())).useDelimiter("\\Z").next();
        String md5= null;
        MessageDigest digest = getInstance("MD5");
        BufferedInputStream in = null;
        byte[] salt = new byte[]{-12, 34, 1, 0, -98, -33, 78, 21};
        String key = null;
        try {
            in = new BufferedInputStream(new FileInputStream(stagingSource));
            int blockCount;
            key = InternalCGPKey;
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 1024, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            byte[] random_iv = new byte[16];
            int read = in.read(random_iv, 0, 16);
            if (read != 16) {
                System.err.println("Oops, didnt find random_iv");
            }
            IvParameterSpec paramSpec = new IvParameterSpec(random_iv);
            Cipher cipher = null;
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(2, (Key)secret, paramSpec);
            CipherInputStream cIn = new CipherInputStream(in, cipher);
            byte[] block = new byte[4096];

            while ((blockCount = cIn.read(block)) != -1) {
                digest.update(block, 0, blockCount);
            }
            byte[] bytes = digest.digest();

            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            //return complete hash
            md5 =  sb.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return md5;
    }
}
