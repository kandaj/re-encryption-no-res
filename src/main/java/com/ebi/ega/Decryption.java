package com.ebi.ega;


import org.apache.commons.io.FileUtils;

import java.io.*;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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
}
