package tpi.dgrv4.dpaa.vo;

public class SslDetailsResp {
    private SubjectDetail  subjectDetail;
    private IssuerDetail issuerDetail;
    private String notBefore;
    private String notAfter;
    private String serialNumber;
    private String fingerprintSha1 ;
    private String fingerprintMd5 ;

    public String getFingerprintSha1() {
        return fingerprintSha1;
    }

    public void setFingerprintSha1(String fingerprintSha1) {
        this.fingerprintSha1 = fingerprintSha1;
    }

    public String getFingerprintMd5() {
        return fingerprintMd5;
    }

    public void setFingerprintMd5(String fingerprintMd5) {
        this.fingerprintMd5 = fingerprintMd5;
    }

    public SubjectDetail getSubjectDetail() {
        return subjectDetail;
    }

    public void setSubjectDetail(SubjectDetail subjectDetail) {
        this.subjectDetail = subjectDetail;
    }

    public IssuerDetail getIssuerDetail() {
        return issuerDetail;
    }

    public void setIssuerDetail(IssuerDetail issuerDetail) {
        this.issuerDetail = issuerDetail;
    }

    public String getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(String notBefore) {
        this.notBefore = notBefore;
    }

    public String getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(String notAfter) {
        this.notAfter = notAfter;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }


}
