package com.ebi.ega;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ProfilerAdaptor {
    public DataSource profiler;

    public int addFile(String fileName, String md5, long bytes, String type, String stableId, DataSource profilerSource) throws SQLException {
        this.profiler = profilerSource;


        int fileId = -1;
        Connection connection = profiler.getConnection();
        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();


        try {
            if (this.insertFile(fileName, md5, type, bytes, stableId, statement)) {
                fileId = this.getFileId(stableId, statement);
                if (fileId != -1) {
                    if (this.insertFileIntoArchive(fileName, fileId, md5, bytes, statement)) {
                        connection.commit();
                    } else {
                        connection.rollback();
                    }
                } else {
                    connection.rollback();
                }
            }
        } catch (SQLException var13) {
            connection.rollback();
            throw var13;
        } finally {
            connection.setAutoCommit(true);
            statement.close();
            connection.close();
        }

        return 1;
    }

    private boolean insertFile(String auditFileName, String md5, String type, long bytes, String stableId, Statement statement) throws SQLException {


        String insertFileSQL = "INSERT INTO file (name, md5, type, size, host_id, withdrawn, ega_file_stable_id) VALUES ('" + auditFileName + "','" + md5 + "','" + type + "'," + bytes + ",1,0,'" + stableId + "')";

        int insertedRows;
        try {
            System.out.println("Inserting file "+auditFileName+" into 'file' table in Profiler");
            insertedRows = statement.executeUpdate(insertFileSQL);
        } catch (SQLException var11) {
            System.out.println("Failed SQL: "+insertFileSQL);
            throw var11;
        }

        return insertedRows == 1;
    }

    private int getFileId(String stableId, Statement statement) throws SQLException {
        int fileId = -1;
        String sql = "SELECT file_id FROM file WHERE ega_file_stable_id='" + stableId + "'";

        try {
            System.out.println("Getting 'file_id' for file "+fileId);
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                fileId = rs.getInt(1);
                System.out.println("File id found: "+fileId);
            } else {
                System.out.println("File "+stableId+" not found in 'file' table");
                System.out.println("SELECT: "+ sql);
            }

            rs.close();
            return fileId;
        } catch (SQLException var6) {
            System.out.println("Failed SQL: "+sql);
            throw var6;
        }
    }

    private boolean insertFileIntoArchive(String auditFileName, int insertedFileId, String md5, long bytes, Statement statement) throws SQLException {
        File file = new File(auditFileName);
        String fileName = file.getName();
        String parentDirectory = file.getParent();
        String relativePath = "/";
        if (parentDirectory != null && !parentDirectory.equals("/")) {
            relativePath = relativePath + parentDirectory;
        }

        String insertSql = "INSERT INTO archive (name, file_id, md5,size,relative_path,volume_name,priority,archive_action_id,archive_location_id) VALUES ('" + fileName + "'," + insertedFileId + ",'" + md5 + "'," + bytes + ",'" + relativePath + "','vol1',50,1,1)";

        try {
            System.out.println("Inserting file "+fileName+" into 'archive' table");
            int insertedRows = statement.executeUpdate(insertSql);
            return insertedRows == 1;
        } catch (SQLException var13) {
            System.out.println("Failed SQL: "+insertSql);
            throw var13;
        }
    }
}
