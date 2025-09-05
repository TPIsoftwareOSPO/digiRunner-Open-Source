package tpi.dgrv4.dpaa.service;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.crypto.fips.FipsSHS;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.dpaa.vo.IssuerDetail;
import tpi.dgrv4.dpaa.vo.SSLDecoderReq;
import tpi.dgrv4.dpaa.vo.SslDetailsResp;
import tpi.dgrv4.dpaa.vo.SubjectDetail;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Service
public class SSLDecoderService {

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private CommService commService;

    public SslDetailsResp getSSLInfo(SSLDecoderReq req) {
        SslDetailsResp details = new SslDetailsResp();
        try { // 輸入的憑證字串
            String value = req.getCert();
            getCommService().checkCertAndGetExpire("cert", value);
            // 將憑證字串轉換為輸入流
            ByteArrayInputStream inputStream = new ByteArrayInputStream(value.getBytes());
            // 建立一個 CertificateFactory 來處理 X.509 憑證
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // 解析憑證
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(inputStream);
            // 關閉輸入流
            inputStream.close();
            // 取得 Subject DN
            String subjectDN = certificate.getSubjectX500Principal().getName();
            X500Name x500Name = new X500Name(subjectDN);
            // 提取城市 (L) 和國家 (C)
            SubjectDetail subjectDetail = new SubjectDetail();
            subjectDetail.setCommon(getValue(BCStyle.CN, x500Name));
            subjectDetail.setCountry(getValue(BCStyle.C, x500Name));
            subjectDetail.setLocality(getValue(BCStyle.L, x500Name));
            subjectDetail.setOrganization(getValue(BCStyle.O, x500Name));
            subjectDetail.setOrganizationUnit(getValue(BCStyle.OU, x500Name));
            subjectDetail.setState(getValue(BCStyle.ST, x500Name));
            subjectDetail.seteMail(getValue(BCStyle.E, x500Name));
            details.setSubjectDetail(subjectDetail);
            details.setNotAfter(DateTimeUtil.dateTimeToString(certificate.getNotAfter(), DateTimeFormatEnum.西元年月日時分_2).orElse(null));
            details.setNotBefore(DateTimeUtil.dateTimeToString(certificate.getNotBefore(), DateTimeFormatEnum.西元年月日時分_2).orElse(null));
            String issuerDN = certificate.getIssuerX500Principal().getName();
            X500Name issuerX500Name = new X500Name(issuerDN);
            IssuerDetail issuerDetail = new IssuerDetail();
            issuerDetail.setCommon(getValue(BCStyle.CN, issuerX500Name));
            issuerDetail.setCountry(getValue(BCStyle.C, issuerX500Name));
            issuerDetail.setLocality(getValue(BCStyle.L, issuerX500Name));
            issuerDetail.setOrganization(getValue(BCStyle.O, issuerX500Name));
            issuerDetail.setOrganizationUnit(getValue(BCStyle.OU, issuerX500Name));
            issuerDetail.setState(getValue(BCStyle.ST, issuerX500Name));
            issuerDetail.seteMail(getValue(BCStyle.E, issuerX500Name));
            details.setIssuerDetail(issuerDetail);
            BigInteger serialNumber = certificate.getSerialNumber();
            details.setSerialNumber(serialNumber.toString());
            details.setFingerprintSha1(calculateFingerprint(certificate, "SHA-1"));
            details.setFingerprintMd5(calculateFingerprint(certificate, "MD5"));
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }

        return details;
    }


    private String calculateFingerprint(X509Certificate certificate, String algorithm) throws Exception {
        // Calculate the SHA-256 fingerprint
        byte[] fingerprintBytes = certificate.getEncoded();

        byte[] hashBytes = MessageDigest.getInstance(algorithm).digest(fingerprintBytes);
        return Hex.toHexString(hashBytes).toUpperCase();
    }

    private String getValue(ASN1ObjectIdentifier bcStyle, X500Name x500Name) {
        RDN[] rdnArray = x500Name.getRDNs(bcStyle);

        String retVal = null;
        for (RDN item : rdnArray) {
            retVal = item.getFirst().getValue().toString();
        }
        return retVal;
    }

}
