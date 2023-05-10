package app.rdrx.directory.model.service;

public class PathSlice {

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_PARAM = "param";

    private String type;
    private String value;
    
    public PathSlice(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    
}
