package com.ebi.ega;

import name.neuhalfen.projects.crypto.bouncycastle.openpgp.BouncyGPG;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.callbacks.KeyringConfigCallbacks;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfig;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfigs;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

@Component
public class ReEncryption {
    public void ReEncrypt(ArrayList fileIndex, HashMap configKeyPath) throws FileNotFoundException {

        String fileSource = (String) ((EGAFile)fileIndex.get(0)).fileSource;
        String stagingSource = (String) ((EGAFile)fileIndex.get(0)).stagingSource;
        String box = (String) ((EGAFile)fileIndex.get(0)).box;


        String InternalCGPKey = new Scanner(new File(configKeyPath.get("internal_key").toString())).useDelimiter("\\Z").next();
        String gpgPassPhrase = new Scanner(new File(configKeyPath.get("public_key").toString())).useDelimiter("\\Z").next();
//        if(box.equals("ega-box-03")) {
//            gpgPassPhrase = new Scanner(new File(configKeyPath.get("sanger_key").toString())).useDelimiter("\\Z").next();
//        }

        Security.addProvider(new BouncyCastleProvider());

        File pubFile = new File ( new String(configKeyPath.get("gpg_public_key").toString()));
        File priFile = new File ( new String(configKeyPath.get("gpg_private_key").toString()));

        final int BUFFSIZE = 4096;
        final KeyringConfig keyringConfig = KeyringConfigs
                .withKeyRingsFromFiles(
                        pubFile,
                        priFile,
                        KeyringConfigCallbacks.withPassword(gpgPassPhrase));
        try (
                FileInputStream cipherTextStream = new FileInputStream(fileSource);
                BufferedOutputStream encryptFile = new BufferedOutputStream(FileUtils.openOutputStream(new File(stagingSource)),BUFFSIZE);

                final InputStream plaintextStream = BouncyGPG
                        .decryptAndVerifyStream()
                        .withConfig(keyringConfig)
                        .andIgnoreSignatures()
                        .fromEncryptedInputStream(cipherTextStream)

        ) {
            //encryption
            byte[] buffer = new byte[4096]; // Adjust if you want
            int bytesRead;
            SecretKey secretKey = this.getKey(InternalCGPKey.toCharArray(), 256);
            byte[] arrby = new byte[16];
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.nextBytes(arrby);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(arrby);
            encryptFile.write(arrby);
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(1, (Key)secretKey, ivParameterSpec);

            while ((bytesRead = plaintextStream.read(buffer)) != -1)
            {
                byte[] obuf = cipher.update(buffer, 0, bytesRead);
                if ( obuf != null ) encryptFile.write(obuf);
            }
            byte[] obuf = cipher.doFinal();
            if ( obuf != null ) encryptFile.write(obuf);

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
