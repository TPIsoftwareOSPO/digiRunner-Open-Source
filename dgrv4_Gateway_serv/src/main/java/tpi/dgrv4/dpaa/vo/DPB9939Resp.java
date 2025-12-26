package tpi.dgrv4.dpaa.vo;

public class DPB9939Resp {
    private String auth;
    private String statusApiUrl;
    private boolean isConnection;
    private String resp;

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getStatusApiUrl() {
        return statusApiUrl;
    }

    public void setStatusApiUrl(String statusApiUrl) {
        this.statusApiUrl = statusApiUrl;
    }

    public boolean isConnection() {
        return isConnection;
    }

    public void setConnection(boolean connection) {
        this.isConnection = connection;
    }

    public String getResp() {
        return resp;
    }

    public void setResp(String resp) {
        this.resp = resp;
    }
}
