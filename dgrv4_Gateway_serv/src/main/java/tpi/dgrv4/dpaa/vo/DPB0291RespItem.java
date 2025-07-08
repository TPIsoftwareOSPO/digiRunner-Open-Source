package tpi.dgrv4.dpaa.vo;

import tpi.dgrv4.entity.entity.DgrGrpcProxyMap;

public class DPB0291RespItem {
    private String grpcProxyMapId;
    private String serviceName;
    private String proxyHostName;
    private String targetHostName;
    private String targetPort;
    private String connectTimeoutMs;
    private String sendTimeoutMs;
    private String readTimeoutMs;
    private String enable;
    private String createDateTime;
    private String createUser;
    private String updateDateTime;
    private String updateUser;

    // 從 DgrGrpcProxyMap 創建響應項目的工廠方法
    public static DPB0291RespItem fromEntity(DgrGrpcProxyMap entity) {
        DPB0291RespItem item = new DPB0291RespItem();
        item.setGrpcProxyMapId(String.valueOf(entity.getGrpcproxyMapId()));
        item.setServiceName(entity.getServiceName());
        item.setProxyHostName(entity.getProxyHostName());
        item.setTargetHostName(entity.getTargetHostName());
        item.setTargetPort(String.valueOf(entity.getTargetPort()));
        item.setConnectTimeoutMs(String.valueOf(entity.getConnectTimeoutMs()));
        item.setSendTimeoutMs(String.valueOf(entity.getSendTimeoutMs()));
        item.setReadTimeoutMs(String.valueOf(entity.getReadTimeoutMs()));
        item.setCreateUser(String.valueOf(entity.getCreateUser()));
        item.setCreateDateTime(String.valueOf(entity.getCreateDateTime().getTime()));
        item.setUpdateUser(String.valueOf(entity.getUpdateUser()));
        item.setUpdateDateTime(String.valueOf(entity.getUpdateDateTime().getTime()));
        item.setEnable(entity.getEnable());
        return item;
    }

    // Getters 和 Setters
    public String getGrpcProxyMapId() {
        return grpcProxyMapId;
    }

    public void setGrpcProxyMapId(String grpcProxyMapId) {
        this.grpcProxyMapId = grpcProxyMapId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getProxyHostName() {
        return proxyHostName;
    }

    public void setProxyHostName(String proxyHostName) {
        this.proxyHostName = proxyHostName;
    }

    public String getTargetHostName() {
        return targetHostName;
    }

    public void setTargetHostName(String targetHostName) {
        this.targetHostName = targetHostName;
    }

    public String getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(String targetPort) {
        this.targetPort = targetPort;
    }

    public String getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(String connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public String getSendTimeoutMs() {
        return sendTimeoutMs;
    }

    public void setSendTimeoutMs(String sendTimeoutMs) {
        this.sendTimeoutMs = sendTimeoutMs;
    }

    public String getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(String readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(String updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }
}