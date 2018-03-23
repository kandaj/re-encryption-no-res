package com.ebi.ega;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;


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

        ReEncryption reEncryption = context.getBean("reEncryption", ReEncryption.class);

        Decryption decryption = context.getBean("decryption", Decryption.class);

        //get file information
        fileIndex = getFileIndex.getFileIndex(args[0],audit);

        //rencrypt GPG file with internal md5
        reEncryption.ReEncrypt(fileIndex,configKeyPath);

        //get unencrypted md5 for cip file
        String unEncryptedMD5 = decryption.getMD5(fileIndex,configKeyPath);

        String submitterUnencryptMD5 =  (String) ((EGAFile)fileIndex.get(0)).unencryptedMD5;
        String stagingSource = (String) ((EGAFile)fileIndex.get(0)).stagingSource;
        String encryptedFileMD5 = (String) getMD5ForFile.getMD5(stagingSource);


        if (unEncryptedMD5.equals(submitterUnencryptMD5)){
            File file = new File(stagingSource);
            long fileSize = (long) file.length();
            String fileType = (String) ((EGAFile)fileIndex.get(0)).fileType;
            String fileStableID = (String) ((EGAFile)fileIndex.get(0)).stableID;
            String profilerFileName = (String) ((EGAFile)fileIndex.get(0)).profilerFileName;

            System.out.println(unEncryptedMD5);
            System.out.println(encryptedFileMD5);

            //put entry into profiler
            profilerAdaptor.addFile(profilerFileName,encryptedFileMD5,fileSize,fileType,fileStableID,profiler);

            //update audit file name
            updateArchiveStatus(fileStableID,profilerFileName, audit);

            //insert/update internal encrypted md5
            updateAuditMD5(fileStableID,encryptedFileMD5, audit);
        } else {
            System.out.println("MD5 mismatch deleting staging file ...");
            File stagingFile = new File(stagingSource);
            stagingFile.delete();
        }
    }

    public static void updateArchiveStatus(String stableID, String fileName, DataSource audit) throws SQLException {
        Connection conn = audit.getConnection();
        PreparedStatement ps = conn.prepareStatement("UPDATE audit_file set archive_status_id=14, file_name=? where stable_id=?");
        try {
            ps.setString(1, fileName);
            ps.setString(2, stableID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("file Status for "+stableID+" not updated");
            e.printStackTrace();
        }
    }

    public static void updateAuditMD5(String stableID, String md5, DataSource audit) throws SQLException {
        java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
        Connection conn = audit.getConnection();
        String sql = "SELECT * FROM audit_md5 WHERE process_step='EGA_internal calculated md5' AND file_stable_id='" + stableID + "'";
        Statement stmt = null;
        PreparedStatement ps = null;

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                ps = conn.prepareStatement("UPDATE audit_md5  SET md5_checksum=? WHERE process_step=? and file_stable_id=?");
                ps.setString(1, md5);
                ps.setString(2, "EGA_internal calculated md5");
                ps.setString(3, stableID);

            } else {
                ps = conn.prepareStatement("INSERT INTO audit_md5 (process_step, md5_checksum, file_stable_id, timestamp) VALUES (?,?,?,?)");
                ps.setString(1, "EGA_internal calculated md5");
                ps.setString(2, md5);
                ps.setString(3, stableID);
                ps.setTimestamp(4, date);
            }
            ps.executeUpdate();
            rs.close();
        } catch (SQLException e) {
            System.out.println("Failed SQL: "+sql);
        }
    }

}

