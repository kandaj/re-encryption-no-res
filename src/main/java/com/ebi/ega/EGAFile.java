package com.ebi.ega;

import java.io.IOException;

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
    public String unencryptedMD5;
    public String publicSourcePath = "/nfs/ega/public/box/";


    public EGAFile(String stableID,String file_name, String box, String file_type, String unencrypted_md5) throws IOException {
        this.stableID = stableID;
        this.submitterFileName = file_name;
        this.baseName = this.getFileBaseName(file_name);
        this.box = box;
        this.fileSource = this.getFileSource(file_name);
        this.fileAccession = fileAccession;
        this.fileType = file_type;
        this.profilerFileName = this.getProfilerFileName(file_name);
        this.unencryptedMD5 = unencrypted_md5;


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

    public String getFileBaseName(String file_name){
        String baseNameArray[] = file_name.split("/");
        String baseName = "";
        if(baseNameArray != null && baseNameArray.length > 0) {
            baseName = baseNameArray[baseNameArray.length - 1];
        }
        return baseName;
    }

}
