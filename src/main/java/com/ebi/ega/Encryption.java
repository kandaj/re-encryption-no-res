package com.ebi.ega;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;

@Component
public class Encryption {
    public void Encrypt(String inFile, String outFile, String keyPath) {

        String inputFile = inFile;
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        CipherOutputStream cipherOutputStream = null;
        String outputFile = outFile;
        String encryptionKeyPath = keyPath;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(inputFile));
            bufferedOutputStream = new BufferedOutputStream(FileUtils.openOutputStream(new File(outputFile)));
            int n;
            SecretKey secretKey = Encryption.getKey(new Scanner(new File(encryptionKeyPath)).useDelimiter("\\Z").next().toCharArray(), 256);
            byte[] arrby = new byte[16];
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.nextBytes(arrby);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(arrby);
            bufferedOutputStream.write(arrby);
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            System.out.println((Key)secretKey);
            cipher.init(1, (Key)secretKey, ivParameterSpec);
            cipherOutputStream = new CipherOutputStream(bufferedOutputStream, cipher);
            byte[] arrby2 = new byte[8192];
            while ((n = bufferedInputStream.read(arrby2)) != -1) {
                cipherOutputStream.write(arrby2, 0, n);
                cipherOutputStream.flush();
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        finally {
            try {
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
                if (cipherOutputStream != null) {
                    cipherOutputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }

    }


    public static SecretKey getKey(char[] arrc, int n) {
        byte[] arrby = new byte[]{-12, 34, 1, 0, -98, -33, 78, 21};
        SecretKeyFactory secretKeyFactory = null;
        SecretKeySpec secretKeySpec = null;
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec pBEKeySpec = new PBEKeySpec(arrc, arrby, 1024, n);
            SecretKey secretKey = secretKeyFactory.generateSecret(pBEKeySpec);
            secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException generalSecurityException) {
            generalSecurityException.printStackTrace();
        }
        return secretKeySpec;
    }

}
