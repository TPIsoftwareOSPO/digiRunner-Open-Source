package tpi.dgrv4.dpaa.vo;

public class DPB9938EsResp {
    private boolean isConnection;
    private String resp;

    public boolean isConnection() {
        return isConnection;
    }

    public void setConnection(boolean connection) {
        isConnection = connection;
    }

    public String getResp() {
        return resp;
    }

    public void setResp(String resp) {
        this.resp = resp;
    }
}
