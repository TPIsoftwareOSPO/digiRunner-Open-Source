package tpi.dgrv4.dpaa.component;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.TsmpCertificate;


public class CertificateHelper {
	
	public static X509Certificate getCertificate(byte[] certStr){
		ByteArrayInputStream bytes = new ByteArrayInputStream(certStr);
		
		CertificateFactory certFactory;
		X509Certificate cert = null;
		try {
			certFactory = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) certFactory.generateCertificate(bytes);
		} catch (CertificateException e) {
			throw TsmpDpAaRtnCode._1291.throwing();
		}
		
		return cert;
	}
	
	/**
	 * 取得解析後的憑證資料
	 * 
	 * @param fileContent
	 * @return
	 */
	public static TsmpCertificate getTsmpCertificate(byte[] fileContent) {
		X509Certificate x509Cert = CertificateHelper.getCertificate(fileContent);
		
		TsmpCertificate c = new TsmpCertificate();
		c.setPubKey(ServiceUtil.base64Encode(x509Cert.getPublicKey().getEncoded()));//公鑰
		c.setCertVersion(x509Cert.getVersion() + "");//憑證版本
		c.setCertSerialNum(x509Cert.getSerialNumber().toString());//憑證序號
		c.setsAlgorithmId(x509Cert.getSigAlgName());//簽章演算法
		c.setAlgorithmId(x509Cert.getPublicKey().getAlgorithm());//公鑰演算法
		c.setCertThumbprint(ServiceUtil.base64Encode(x509Cert.getSignature()));//CA數位指紋
		c.setIuid("");//發行方ID
		String issuerName = "";		
		try {
			issuerName = getIssuerName(x509Cert);//發行方名稱
		} catch (InvalidNameException e1) {
			issuerName = "";
		}
		c.setIssuerName(issuerName);
		
		String subjectUniqueID = "";
		try {
			subjectUniqueID = getCommonName(x509Cert);//持有者身分ID
		} catch (InvalidNameException e) {
			subjectUniqueID = "";
		}
		c.setSuid(subjectUniqueID);
		c.setCreateAt(x509Cert.getNotBefore().getTime());//憑證創建日 (轉long)
		c.setExpiredAt(x509Cert.getNotAfter().getTime());//憑證到期日 (轉long)
		c.setKeySize(getKeySize(x509Cert));
		
		return c;
	}

	/** 
	 * 取得發行方名稱
	 * 
	 * 備註:
	 * see https://www.sslsupportdesk.com/java-code-signing-certificate-instructions/
	 * Fill out the applicable information:
	 * (1).First and Last Name? or Common Name (CN)
	 * (2).Organizational Unit (OU)
	 * (3).Organization (O)
	 * (4).Locality or City (L)
	 * (5).State or Province (ST)
	 * (6).Country Name (C)
	 */
	public static String getIssuerName(X509Certificate cert) throws InvalidNameException {		
		
		LdapName ldapDN = new LdapName(cert.getIssuerX500Principal().getName());
		String cn = "";
		for (Rdn rdn : ldapDN.getRdns()) {
			if (rdn.getType().equals("CN")) {
				cn = rdn.getValue().toString();
			}
		}
		return cn;
	}
	
	public static String getCommonName(X509Certificate cert)
	        throws InvalidNameException {
	    LdapName ldapDN
	            = new LdapName(cert.getSubjectX500Principal().getName());
	    String cn = "";
	    for (Rdn rdn : ldapDN.getRdns()) {
	        if (rdn.getType().equals("CN")) {
	            cn = rdn.getValue().toString();
	        }
	    }
	    return cn;
	}
	
	/**
	 * 取得 key size,不同公鑰演算法有不同取法
	 * 
	 * see https://stackoverflow.com/questions/32573317/how-to-determine-length-of-x509-public-key
	 * @return
	 */
	public static int getKeySize(X509Certificate cert) {
		PublicKey pk = cert.getPublicKey();
		
	    int len = -1;
	    if (pk instanceof RSAPublicKey) {
	        final RSAPublicKey rsapub = (RSAPublicKey) pk;
	        len = rsapub.getModulus().bitLength();
	        
	    } else if (pk instanceof DSAPublicKey) {
	        final DSAPublicKey dsapub = (DSAPublicKey) pk;
	        if ( dsapub.getParams() != null ) {
	            len = dsapub.getParams().getP().bitLength();
	        } else {
	            len = dsapub.getY().bitLength();
	        }
	    } 
	    return len;
	}
}
