package com.ebi.ega;

import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class EGAFile {

    public String fileSource;
    public String stableID;
    public String baseName;
    public String fileAccession;
    public String box;
    public String stagingSource;
    public String submitterFileName;
    public String fileType;
    public String profilerFileName;
    public String stagingSourcePath = "/nfs/ega/public/staging/";
    public String publicSourcePath = "/nfs/ega/public/box/";


    public EGAFile(String stableID,String file_name, String box, String file_type) throws IOException {
        this.stableID = stableID;
        this.submitterFileName = file_name;
        this.baseName = this.getFileBaseName(file_name);
        this.box = box;
        this.fileSource = this.getFileSource(file_name);
        this.fileAccession = fileAccession;
        this.stagingSource = this.getStaginSource(file_name);
        this.fileType = file_type;
        this.profilerFileName = this.getProfilerFileName(file_name);


    }
    public String getFileSource(String file_name) {

        if(file_name.substring(0, 3).equals("EGA") ) {
            String[] splitArray = file_name.split("/", 2);
            file_name = splitArray[splitArray.length-1];
        }

        return publicSourcePath+this.box+"/"+file_name;
    }

    public String getProfilerFileName(String file_name) {

        if(file_name.substring(file_name.length()-4).equals(".gpg")){
            file_name =  file_name.replace(".gpg",".cip");
        }
        return file_name;
    }

    public String getStaginSource(String file_name) {
        if(file_name.substring(file_name.length()-4).equals(".gpg")){
            file_name =  file_name.replace(".gpg",".cip");
        }
        return stagingSourcePath+file_name;
    }

    public String getFileBaseName(String file_name){
        String baseNameArray[] = file_name.split("/");
        String baseName = "";
        if(baseNameArray != null && baseNameArray.length > 0) {
            baseName = baseNameArray[baseNameArray.length - 1];
        }
        return baseName;
    }

}
