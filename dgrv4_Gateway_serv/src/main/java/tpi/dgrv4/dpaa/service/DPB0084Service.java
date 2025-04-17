package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.base64Encode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.constant.TsmpDpCertType;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0084CertItem;
import tpi.dgrv4.dpaa.vo.DPB0084Req;
import tpi.dgrv4.dpaa.vo.DPB0084Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert2;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCertBasic;
import tpi.dgrv4.entity.repository.TsmpClientCert2Dao;
import tpi.dgrv4.entity.repository.TsmpClientCertDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0084Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientCertDao tsmpClientCertDao;
	private TsmpClientDao tsmpClientDao;
	private TsmpClientCert2Dao tsmpClientCert2Dao;
	private BcryptParamHelper helper;
	
	@Autowired
	public DPB0084Service(TsmpClientCertDao tsmpClientCertDao, TsmpClientDao tsmpClientDao,
			TsmpClientCert2Dao tsmpClientCert2Dao, BcryptParamHelper helper) {
		super();
		this.tsmpClientCertDao = tsmpClientCertDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpClientCert2Dao = tsmpClientCert2Dao;
		this.helper = helper;
	}

	public DPB0084Resp queryClientByCid(TsmpAuthorization tsmpAuthorization, DPB0084Req req, ReqHeader reqHeader) {
		DPB0084Resp resp = new DPB0084Resp();
		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());

			String certType = getCertType(req, locale);//解碼
			
			String clientId = req.getClientId();
			if(StringUtils.isEmpty(clientId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			Optional<TsmpClient> opt = getTsmpClientDao().findById(clientId);
			if (!opt.isPresent()) {
				this.logger.error("用戶不存在: " + clientId);
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			
			List<TsmpClientCertBasic> certBasicList = new ArrayList<TsmpClientCertBasic>();
			List<DPB0084CertItem> certItemList = null;
			if(TsmpDpCertType.JWE.value().equals(certType)) {
				List<TsmpClientCert> certList = getTsmpClientCertDao().findByClientId(clientId);
				if (certList == null || certList.isEmpty()) {
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				
				for (TsmpClientCert c : certList) {
					certBasicList.add(c);
				}
				
			}else if(TsmpDpCertType.TLS.value().equals(certType)) {
				List<TsmpClientCert2> certList = getTsmpClientCert2Dao().findByClientId(clientId);
				if (certList == null || certList.isEmpty()) {
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				
				for (TsmpClientCert2 c : certList) {
					certBasicList.add(c);
				}
			}
			certItemList = getCertItemList(certBasicList);
			
			resp.setCertList(certItemList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	public String getCertType(DPB0084Req req, String locale) {
		String encodeCertType = req.getEncodeCertType();//使用BcryptParam, ITEM_NO='CERT_TYPE', JWE 使用 TSMP_CLIENT_CERT, TLS 使用 TSMP_CLIENT_CERT2
		if (StringUtils.isEmpty(encodeCertType)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		String dpb0084_certType = getDecodeCertType(encodeCertType, locale);//解碼
		
		return dpb0084_certType;
	}
	
	public String getDecodeCertType(String encodeCertType, String locale) {
		String dpb0084_certType = null;
		try {
			dpb0084_certType = getBcryptParamHelper().decode(encodeCertType, "CERT_TYPE", locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return dpb0084_certType;
	}
	
	private List<DPB0084CertItem> getCertItemList(List<TsmpClientCertBasic> clientBasicList) {
		List<DPB0084CertItem> dataList = new ArrayList<DPB0084CertItem>();
		for (TsmpClientCertBasic client : clientBasicList) {
			DPB0084CertItem data = getCertItem(client);
			dataList.add(data);
		}
		return dataList;
	}
	
	private DPB0084CertItem getCertItem(TsmpClientCertBasic c) {
		String clientFileContent = getClientFileContent(c.getFileContent());
		String createAt = getCertDateTime(c.getCreateAt());
		String expiredAt = getCertDateTime(c.getExpiredAt());
		
		DPB0084CertItem data = new DPB0084CertItem();
		data.setAlogorithmID(c.getAlgorithmId());
		data.setCertSerialNum(c.getCertSerialNum());
		data.setCertThumbprint(c.getCertThumbprint());
		data.setCertVersion(nvl(c.getCertVersion()));
		if(c instanceof TsmpClientCert) {
			data.setClientCertId(((TsmpClientCert)c).getClientCertId());
		}else if(c instanceof TsmpClientCert2) {
			data.setClientCert2Id(((TsmpClientCert2)c).getClientCert2Id());
		}
		data.setClientFileContent(clientFileContent);
		data.setClientFileName(c.getCertFileName());
		data.setClientId(c.getClientId());
		data.setCreateAt(createAt);
		data.setCreateDateTime(getCreateDateTime(c.getCreateDateTime()));
		data.setCreateUser(nvl(c.getCreateUser()));
		data.setExpiredAt(expiredAt);
		data.setIssuerName(c.getIssuerName());
		data.setIuId(nvl(c.getIuid()));
		data.setLv(c.getVersion());
		data.setKeySize(c.getKeySize());
		data.setPubKey(c.getPubKey());
		data.setsAlgorithmID(nvl(c.getsAlgorithmId()));
		data.setsUid(nvl(c.getSuid()));
		return data;
	}

	private String getClientFileContent(byte[] fileContent) {
		if (fileContent == null || fileContent.length == 0) {
			return new String();
		}
		return base64Encode(fileContent);
	}

	private String getCertDateTime(Long timeInMillis) {
		Date dt = new Date(timeInMillis);
		String dtStr = DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日_2).orElse(null);
		this.logger.debug(String.format("certDt: %d -> %s", timeInMillis, dtStr));
		return dtStr;
	}

	private String nvl(Object obj) {
		if (obj == null) {
			return new String();
		}
		return String.valueOf(obj);
	}
	
	private String getCreateDateTime(Date createDateTime) {
		return DateTimeUtil.dateTimeToString(createDateTime, DateTimeFormatEnum.西元年月日時分_2).orElse(null);
	}

	protected TsmpClientCertDao getTsmpClientCertDao() {
		return this.tsmpClientCertDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}
	
	protected TsmpClientCert2Dao getTsmpClientCert2Dao() {
		return this.tsmpClientCert2Dao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.helper;
	}
}
