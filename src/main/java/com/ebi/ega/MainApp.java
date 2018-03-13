package com.ebi.ega;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @author imssbora
 */

public class MainApp {

    public static HashMap fileIndex;

    public static void main(String[] args) throws IOException, SQLException, NoSuchAlgorithmException {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        DataSource audit = context.getBean("audit",DataSource.class);

        DataSource profiler = context.getBean("profiler",DataSource.class);

        GetFileIndex getFileIndex = context.getBean("getFileIndex", GetFileIndex.class);

        String internalKeyPath = context.getBean("getConfigProperty", String.class);

        Encryption encryption = context.getBean("encryption", Encryption.class);

        Decryption decryption = context.getBean("decryption", Decryption.class);

        GetMD5ForFile getMD5ForFile = context.getBean("getMD5ForFile", GetMD5ForFile.class);

        ProfilerAdaptor profilerAdaptor = context.getBean("profilerAdaptor", ProfilerAdaptor.class);


        fileIndex = new GetFileIndex().getFileIndex(audit);

        for (Object key : fileIndex.keySet()) {

            String fileStableID = (String) key;
            String fileBaseName = (String) ((EGAFile) fileIndex.get(key)).baseName;
            String fileSource = (String) ((EGAFile) fileIndex.get(key)).fileSource;
            String stagingSource = (String) ((EGAFile) fileIndex.get(key)).stagingSource;
            String profilerFileName = (String) ((EGAFile) fileIndex.get(key)).profilerFileName;
            String fileType = (String) ((EGAFile) fileIndex.get(key)).fileType;
            String decryptedFileSource = "/tmp/"+fileBaseName;

            encryption.Encrypt(fileSource,stagingSource, internalKeyPath);
            decryption.Decrypt(stagingSource,"/tmp/"+fileBaseName, internalKeyPath);
            String submitterFileMD5 = (String) getMD5ForFile.getMD5(fileSource);
            String decryptedFileMD5 = (String) getMD5ForFile.getMD5(decryptedFileSource);
            String encryptedFileMD5 = (String) getMD5ForFile.getMD5(stagingSource);

            File file = new File(stagingSource);
            long fileSize = (long) file.length();
            System.out.println(submitterFileMD5);
            System.out.println(decryptedFileMD5);
            if(submitterFileMD5.equals(decryptedFileMD5)){
                profilerAdaptor.addFile(profilerFileName,encryptedFileMD5,fileSize,fileType,fileStableID,profiler);
                File decryFile = new File(decryptedFileSource);
                decryFile.delete();
                updateArchiveStatus(fileStableID, audit);

            } else{
                System.out.println("MD5 not correct: "+fileStableID);
            }
        }
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

}

