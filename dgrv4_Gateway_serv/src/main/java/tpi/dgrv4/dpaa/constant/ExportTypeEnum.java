package tpi.dgrv4.dpaa.constant;

import lombok.Getter;


public enum ExportTypeEnum {
    EXCEL("xlsx"),
    CSV("csv"),
    JSON("json");


    private String extension;

   private ExportTypeEnum(String extension) {
        this.extension = extension;
    }

    public  String  getType(){
        return this.name();
    }
    public String getExtension(){
       return this.extension;
    }
}
