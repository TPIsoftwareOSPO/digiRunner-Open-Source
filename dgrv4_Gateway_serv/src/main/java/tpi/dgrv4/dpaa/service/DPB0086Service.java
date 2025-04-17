package tpi.dgrv4.dpaa.service;

import java.util.List;

import jakarta.transaction.Transactional;

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
import tpi.dgrv4.dpaa.vo.DPB0086Req;
import tpi.dgrv4.dpaa.vo.DPB0086Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert2;
import tpi.dgrv4.entity.repository.TsmpClientCert2Dao;
import tpi.dgrv4.entity.repository.TsmpClientCertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0086Service {
	
	private TPILogger logger = TPILogger.tl;

	private TsmpClientCertDao tsmpClientCertDao;
	private TsmpClientCert2Dao tsmpClientCert2Dao;
	private BcryptParamHelper helper;
	
	@Autowired
	public DPB0086Service(TsmpClientCertDao tsmpClientCertDao, TsmpClientCert2Dao tsmpClientCert2Dao,
			BcryptParamHelper helper) {
		super();
		this.tsmpClientCertDao = tsmpClientCertDao;
		this.tsmpClientCert2Dao = tsmpClientCert2Dao;
		this.helper = helper;
	}

	@Transactional
	public DPB0086Resp deleteClientCA(TsmpAuthorization authorization, DPB0086Req req, ReqHeader reqHeader) {
		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			String clientId = req.getClientId();
			Long clientCertId = req.getClientCertId();
			Long clientCert2Id = req.getClientCert2Id();
			
			//chk param
			String certType = getCertType(req, locale);//解碼
			
			if(StringUtils.isEmpty(clientId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			if(TsmpDpCertType.JWE.value().equals(certType)) {
				if(StringUtils.isEmpty(clientCertId)) {
					throw TsmpDpAaRtnCode._1296.throwing();
				}
				
				List<TsmpClientCert> certList = getTsmpClientCertDao().findByClientIdAndClientCertId(clientId, clientCertId);
				if(certList == null || certList.isEmpty()) {
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				
				long flag = getTsmpClientCertDao().deleteByClientIdAndClientCertId(clientId, clientCertId);
				
			}else if(TsmpDpCertType.TLS.value().equals(certType)) {
				if(StringUtils.isEmpty(clientCert2Id)) {
					throw TsmpDpAaRtnCode._1296.throwing();
				}
				
				List<TsmpClientCert2> certList = getTsmpClientCert2Dao().findByClientIdAndClientCert2Id(clientId, clientCert2Id);
				if(certList == null || certList.isEmpty()) {
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				
				long flag = getTsmpClientCert2Dao().deleteByClientIdAndClientCert2Id(clientId, clientCert2Id);
 
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		DPB0086Resp resp = new DPB0086Resp();
		return resp;
	}
	
	public String getCertType(DPB0086Req req, String locale) {
		String encodeCertType = req.getEncodeCertType();//使用BcryptParam, ITEM_NO='CERT_TYPE', JWE 使用 TSMP_CLIENT_CERT, TLS 使用 TSMP_CLIENT_CERT2
		if (StringUtils.isEmpty(encodeCertType)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		String dpb0086_certType = getDecodeCertType(encodeCertType, locale);//解碼
		
		return dpb0086_certType;
	}
	
	public String getDecodeCertType(String encodeCertType, String locale) {
		String dpb0086_certType = null;
		try {
			dpb0086_certType = getBcryptParamHelper().decode(encodeCertType, "CERT_TYPE", locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return dpb0086_certType;
	}
 
	protected TsmpClientCertDao getTsmpClientCertDao() {
		return this.tsmpClientCertDao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.helper;
	}
	
	protected TsmpClientCert2Dao getTsmpClientCert2Dao() {
		return this.tsmpClientCert2Dao;
	}
}
