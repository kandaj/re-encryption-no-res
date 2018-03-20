package com.ebi.ega;

import name.neuhalfen.projects.crypto.bouncycastle.openpgp.BouncyGPG;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.callbacks.KeyringConfigCallbacks;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfig;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfigs;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import java.io.*;
import java.security.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


/**
 * @author imssbora
 */

public class MainApp {

    public static ArrayList fileIndex;

    public static void main(String[] args) throws IOException, SQLException, NoSuchAlgorithmException {

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        DataSource audit = context.getBean("audit",DataSource.class);

        DataSource profiler = context.getBean("profiler",DataSource.class);

        GetFileIndex getFileIndex = context.getBean("getFileIndex", GetFileIndex.class);

        HashMap configKeyPath = context.getBean("getConfigProperty", HashMap.class);

        GetMD5ForFile getMD5ForFile = context.getBean("getMD5ForFile", GetMD5ForFile.class);

        ProfilerAdaptor profilerAdaptor = context.getBean("profilerAdaptor", ProfilerAdaptor.class);

        fileIndex = new GetFileIndex().getFileIndex(args[0],audit);

        System.out.println(fileIndex);

        reEncryptGPGFile(fileIndex,configKeyPath);
        String unEncryptedMD5 = getDecryptMD5(fileIndex,configKeyPath);
        System.out.println(unEncryptedMD5);
    }

    public static void updateArchiveStatus(String stableID, DataSource audit) throws SQLException {
        Connection conn = audit.getConnection();
        PreparedStatement ps = conn.prepareStatement("UPDATE audit_file set archive_status_id=14 where stable_id=?");
        try {
            ps.setString(1, stableID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("file Status for "+stableID+" not updated");
            e.printStackTrace();
        }
    }

    public static void reEncryptGPGFile(ArrayList fileIndex, HashMap configKeyPath) throws IOException, NoSuchAlgorithmException {

        String fileBaseName = (String) ((EGAFile)fileIndex.get(0)).baseName;
        String fileSource = (String) ((EGAFile)fileIndex.get(0)).fileSource;
        String stagingSource = (String) ((EGAFile)fileIndex.get(0)).stagingSource;
        String box = (String) ((EGAFile)fileIndex.get(0)).box;


        String InternalCGPKey = new Scanner(new File(configKeyPath.get("internal_key").toString())).useDelimiter("\\Z").next();
        String gpgPassPhrase = new Scanner(new File(configKeyPath.get("public_key").toString())).useDelimiter("\\Z").next();
        if(box.equals("ega-box-03")) {
            gpgPassPhrase = new Scanner(new File(configKeyPath.get("sanger_key").toString())).useDelimiter("\\Z").next();
        }
        String gpgPublicKey = new String(configKeyPath.get("gpg_public_key").toString());
        String gpgPrivateKey = new String(configKeyPath.get("gpg_private_key").toString());

        Security.addProvider(new BouncyCastleProvider());

        File pubFile = new File (gpgPublicKey);
        File priFile = new File (gpgPrivateKey);


        final KeyringConfig keyringConfig = KeyringConfigs
                .withKeyRingsFromFiles(
                        pubFile,
                        priFile,
                        KeyringConfigCallbacks.withPassword(gpgPassPhrase));
        try (
                FileInputStream cipherTextStream = new FileInputStream(fileSource);
                BufferedOutputStream encryptFile = new BufferedOutputStream(FileUtils.openOutputStream(new File(stagingSource)));

                final InputStream plaintextStream = BouncyGPG
                        .decryptAndVerifyStream()
                        .withConfig(keyringConfig)
                        .andIgnoreSignatures()
                        .fromEncryptedInputStream(cipherTextStream)

        ) {
            //encryption
            byte[] buffer = new byte[1024]; // Adjust if you want
            int bytesRead;
            SecretKey secretKey = Encryption.getKey(InternalCGPKey.toCharArray(), 256);
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
        }
    }

    private static String getDecryptMD5(ArrayList fileIndex, HashMap configKeyPath) throws NoSuchAlgorithmException, FileNotFoundException {
        String stagingSource = (String) ((EGAFile)fileIndex.get(0)).stagingSource;
        String InternalCGPKey = new Scanner(new File(configKeyPath.get("internal_key").toString())).useDelimiter("\\Z").next();
        String md5= null;
        MessageDigest digest = MessageDigest.getInstance("MD5");
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

