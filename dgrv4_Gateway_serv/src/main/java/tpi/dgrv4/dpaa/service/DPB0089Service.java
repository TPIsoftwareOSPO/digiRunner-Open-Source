package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.constant.TsmpDpCertType;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0089Req;
import tpi.dgrv4.dpaa.vo.DPB0089Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert2;
import tpi.dgrv4.entity.repository.TsmpClientCert2Dao;
import tpi.dgrv4.entity.repository.TsmpClientCertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0089Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientCertDao tsmpClientCertDao;
	private TsmpClientCert2Dao tsmpClientCert2Dao;
	private BcryptParamHelper helper;
	
	@Autowired
	public DPB0089Service(TsmpClientCertDao tsmpClientCertDao, TsmpClientCert2Dao tsmpClientCert2Dao,
			BcryptParamHelper helper) {
		super();
		this.tsmpClientCertDao = tsmpClientCertDao;
		this.tsmpClientCert2Dao = tsmpClientCert2Dao;
		this.helper = helper;
	}

	public DPB0089Resp returnTextFIle(TsmpAuthorization tsmpAuthorization, DPB0089Req req, ReqHeader reqHeader) {
		DPB0089Resp resp = new DPB0089Resp();
		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			String certType = getCertType(req, locale);//解碼
			
			List<Long> clientCertIdList = req.getIds();
			if(clientCertIdList == null || clientCertIdList.isEmpty()) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			String allFileContentStr = "";
			for (Long clientCertId : clientCertIdList) {
				String fileContent = null;
				if(TsmpDpCertType.JWE.value().equals(certType)) {
					Optional<TsmpClientCert> opt_cert = getTsmpClientCertDao().findById(clientCertId);
					if (opt_cert.isPresent()) {
						TsmpClientCert cert = opt_cert.get();
						fileContent = new String(cert.getFileContent());
					}
				}else if(TsmpDpCertType.TLS.value().equals(certType)) {
					Optional<TsmpClientCert2> opt_cert = getTsmpClientCert2Dao().findById(clientCertId);
					if (opt_cert.isPresent()) {
						TsmpClientCert2 cert = opt_cert.get();
						fileContent = new String(cert.getFileContent());
					}
				}
				
				if (!StringUtils.isEmpty(fileContent)) {
					if(!allFileContentStr.equals("")) {
						allFileContentStr += "\n\n";
					}
					allFileContentStr += fileContent;
				}
				
				if(allFileContentStr.trim().equals("")) {
					throw TsmpDpAaRtnCode._1298.throwing();
				}
			}
			resp.setFileContentStr(allFileContentStr);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	public String getCertType(DPB0089Req req, String locale) {
		String encodeCertType = req.getEncodeCertType();//使用BcryptParam, ITEM_NO='CERT_TYPE', JWE 使用 TSMP_CLIENT_CERT, TLS 使用 TSMP_CLIENT_CERT2
		if (StringUtils.isEmpty(encodeCertType)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		String certType = getDecodeCertType(encodeCertType, locale);//解碼
		
		return certType;
	}
	
	public String getDecodeCertType(String encodeCertType, String locale) {
		String certType = null;
		try {
			certType = getBcryptParamHelper().decode(encodeCertType, "CERT_TYPE", locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return certType;
	}
 
	protected TsmpClientCertDao getTsmpClientCertDao() {
		return this.tsmpClientCertDao;
	}
	
	protected TsmpClientCert2Dao getTsmpClientCert2Dao() {
		return this.tsmpClientCert2Dao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.helper;
	}
}
