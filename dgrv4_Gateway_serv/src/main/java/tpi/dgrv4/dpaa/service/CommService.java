package tpi.dgrv4.dpaa.service;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.ifs.TsmpCoreTokenBase;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommService {

    private TsmpCoreTokenBase tsmpCoreTokenBase;
    @Autowired(required = false)
    public CommService(TsmpCoreTokenBase tsmpCoreTokenBase) {
        this.tsmpCoreTokenBase = tsmpCoreTokenBase;
    }

    // Id轉換
    public Long getLongId(String id, String fieldName) {
        if (!StringUtils.hasLength(id)) {
            throw TsmpDpAaRtnCode._2025.throwing(fieldName);
        }
        return RandomSeqLongUtil.toLongValue(id);
    }

    public void checkPrivateKey(String value) throws Exception {
        if (!StringUtils.hasLength(value)) {
            throw TsmpDpAaRtnCode._2025.throwing("Client Key");
        }
        try {
            InputStream clientKeyStream = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
            PEMParser pemParser = new PEMParser(new InputStreamReader(clientKeyStream));
            Object keyObject = pemParser.readObject();

            if (keyObject == null) {
                throw TsmpDpAaRtnCode._1352.throwing("Client Key");
            }
            if (!(keyObject instanceof PEMKeyPair ||
                    keyObject instanceof PrivateKeyInfo ||
                    keyObject instanceof PKCS8EncodedKeySpec ||
                    keyObject instanceof PKCS8EncryptedPrivateKeyInfo)) {
                throw TsmpDpAaRtnCode._1352.throwing("Client Key");
            }
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1352.throwing("Client Key");

        }
    }




    public Date checkCertAndGetExpire(String name, String value)  {
        Date notAfter = null;
        try {
            if (!StringUtils.hasLength(value)) {
                throw TsmpDpAaRtnCode._2025.throwing("{{" + name + "}}");
            }
            // 將憑證字串轉換為輸入流
            ByteArrayInputStream inputStream = new ByteArrayInputStream(value.getBytes());
            // 建立一個 CertificateFactory 來處理 X.509 憑證
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // 解析憑證
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(inputStream);
            // 關閉輸入流
            inputStream.close();
            certificate.checkValidity();
            notAfter = certificate.getNotAfter();
        } catch (CertificateExpiredException e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            // 憑證過期
            throw TsmpDpAaRtnCode._1560.throwing();
        } catch (CertificateException e ) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            // 憑證格式錯誤
            throw TsmpDpAaRtnCode._1352.throwing(name);
        } catch (IOException e) {
            throw TsmpDpAaRtnCode._1297.throwing(name);
        }
        return notAfter;
    }

    // ENC加密
    public String getENCEncode(String value) throws Exception {
        String encoded = getTsmpCoreTokenBase().encrypt(value);
        encoded = String.format("ENC(%s)", encoded);
        TPILogger.tl.debug("ENC encode  " + encoded);
        return encoded;
    }
    public String getENCPlainVal(String val) throws Exception {
        Pattern pattern = Pattern.compile("^ENC\\((\\S+)\\)$");
        if (val==null) {val="";} // Oracle 取值會是 null
        Matcher matcher = pattern.matcher(val); // 不接受 null
        if (matcher.matches()) {
            val = matcher.group(1);
            return getTsmpCoreTokenBase().decrypt(val);
        }
        return val;
    }
    protected TsmpCoreTokenBase getTsmpCoreTokenBase() {
        return this.tsmpCoreTokenBase;
    }
}
