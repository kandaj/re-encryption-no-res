package com.ebi.ega;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GetFileIndex {
    public ArrayList stableIds;
    public ArrayList  getFileIndex(String stableID,DataSource audit) {
        ArrayList<Object> temp = new ArrayList<Object>();
        String query = "select * from audit_file where stable_id= ?";
        Connection conn = null;
        try {
            conn = audit.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1,  stableID);
            ResultSet rs = ps.executeQuery();
            while ( rs.next() )
            {
                EGAFile egaf=new EGAFile(stableID,rs.getString("file_name"),rs.getString("staging_source"),rs.getString("file_type"));
                temp.add(egaf);
            }
            rs.close();
            ps.close();
            conn.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {}
            }
        }

        return (ArrayList) temp;
    }

    public ArrayList<String> readInputFile() {
        ArrayList<String> list = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(System.getProperty("file")));

            String str=null;
            while((str = br.readLine()) != null){
                list.add(str);
            }
            br.close();

        } catch (FileNotFoundException e) {
            System.err.println("File not found");
        } catch (IOException e) {
            System.err.println("Unable to read the file.");
        }
        return ( list );
    }
}
