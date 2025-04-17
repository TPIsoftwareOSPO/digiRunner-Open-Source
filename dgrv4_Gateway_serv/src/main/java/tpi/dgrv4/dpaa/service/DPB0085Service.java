package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.CertificateHelper;
import tpi.dgrv4.dpaa.constant.TsmpDpCertType;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0085Req;
import tpi.dgrv4.dpaa.vo.DPB0085Resp;
import tpi.dgrv4.dpaa.vo.TsmpCertificate;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert2;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCertBasic;
import tpi.dgrv4.entity.repository.TsmpClientCert2Dao;
import tpi.dgrv4.entity.repository.TsmpClientCertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0085Service {
	
	private TPILogger logger = TPILogger.tl;

	private TsmpClientCertDao tsmpClientCertDao;
	private TsmpClientCert2Dao tsmpClientCert2Dao; 
	private BcryptParamHelper helper;
	
	Sort sort = Sort.by(Sort.Direction.DESC, "expiredAt");//依憑證到期日,由新到舊排序

	@Autowired
	public DPB0085Service(TsmpClientCertDao tsmpClientCertDao, TsmpClientCert2Dao tsmpClientCert2Dao,
			BcryptParamHelper helper) {
		super();
		this.tsmpClientCertDao = tsmpClientCertDao;
		this.tsmpClientCert2Dao = tsmpClientCert2Dao;
		this.helper = helper;
	}

	public DPB0085Resp uploadClientCA(TsmpAuthorization authorization, DPB0085Req req, ReqHeader reqHeader) {
		DPB0085Resp resp = new DPB0085Resp();
		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			String userName = authorization.getUserName();
			String clientId = req.getClientId();
			Map<Long, Long> lockVersions = req.getLockVersions();
			String fileContent = req.getFileContent();
			
			String fileName = req.getFileName();
			
			//chk param
			String certType = getCertType(req, locale);//解碼
			
			if(StringUtils.isEmpty(clientId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if(StringUtils.isEmpty(fileContent)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if(StringUtils.isEmpty(fileName)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
 
			byte[] fileContentByte = ServiceUtil.base64Decode(fileContent);
			//取得解析後的憑證資料
			TsmpCertificate tsmpCert = CertificateHelper.getTsmpCertificate(fileContentByte);
			
			resp = uploadClientCA(tsmpCert, clientId, lockVersions, fileContentByte, fileName, userName, certType);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}
	
	public String getCertType(DPB0085Req req, String locale) {
		String encodeCertType = req.getEncodeCertType();//使用BcryptParam, ITEM_NO='CERT_TYPE', JWE 使用 TSMP_CLIENT_CERT, TLS 使用 TSMP_CLIENT_CERT2
		if (StringUtils.isEmpty(encodeCertType)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		String dpb0085_certType = getDecodeCertType(encodeCertType, locale);//解碼
		
		return dpb0085_certType;
	}
	
	public String getDecodeCertType(String encodeCertType, String locale) {
		String dpb0085_certType = null;
		try {
			dpb0085_certType = getBcryptParamHelper().decode(encodeCertType, "CERT_TYPE", locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return dpb0085_certType;
	}
	
	private List<TsmpClientCertBasic> getCertBasicList(String clientId, String certType) {
		List<TsmpClientCertBasic> certBasicList = new ArrayList<TsmpClientCertBasic>();
		if(TsmpDpCertType.JWE.value().equals(certType)) {
			List<TsmpClientCert> certList = getTsmpClientCertDao().findByClientId(clientId, sort);
			for (TsmpClientCert c : certList) {
				certBasicList.add(c);
			}
			
		}else if(TsmpDpCertType.TLS.value().equals(certType)) {
			List<TsmpClientCert2> certList = getTsmpClientCert2Dao().findByClientId(clientId, sort);
			for (TsmpClientCert2 c : certList) {
				certBasicList.add(c);
			}
		}
		
		return certBasicList;
	}
	
	public DPB0085Resp uploadClientCA(TsmpCertificate tsmpCert, String clientId, Map<Long, Long> lockVersions, 
			byte[] fileContentByte, String fileName, String userName, String certType){
		
		List<TsmpClientCertBasic> certBasicList = getCertBasicList(clientId, certType);
		
		TsmpClientCertBasic orig = null;
		
		long createAt = tsmpCert.getCreateAt();//憑證創建日
		long expiredAt = tsmpCert.getExpiredAt();//憑證到期日
		
		if(certBasicList != null && !certBasicList.isEmpty()) {
			if(certBasicList.size() > 2) {//超過2個憑證
				/*
				 * 前端也有做檢查,只要有2張就 alert 訊息 "已存在2張憑證,請先刪除一張",
				 * 流程跑不到這裡
				 */
				delete(certBasicList);
				certBasicList = getCertBasicList(clientId, certType);
			}
		}
		
		if(certBasicList == null || certBasicList.isEmpty()) {//沒有憑證
			//insert
			insertData(tsmpCert, clientId, fileContentByte, fileName, userName, certType);
			
		}else if(certBasicList.size() == 1) {//已有1個憑證
			orig = certBasicList.get(0);
			long origCreateAt = orig.getCreateAt();//憑證創建日
			long dpb0085_1_origExpiredAt = orig.getExpiredAt();//憑證到期日
			
			//判斷憑證是否相同
			if(createAt == origCreateAt && expiredAt == dpb0085_1_origExpiredAt) {//憑證日期相同
				//相同,update
				long dpb0085_1_clientCertId = -1;
				if(orig instanceof TsmpClientCert) {
					dpb0085_1_clientCertId = ((TsmpClientCert)orig).getClientCertId();
				}else if(orig instanceof TsmpClientCert2) {
					dpb0085_1_clientCertId = ((TsmpClientCert2)orig).getClientCert2Id();
				}
				
				long lv = getLv(lockVersions, dpb0085_1_clientCertId);
				updateData(tsmpCert, clientId, fileContentByte, fileName, userName, lv, orig);
			}else {
				//不同,insert
				insertData(tsmpCert, clientId, fileContentByte, fileName, userName, certType);
			}			
			
		}else if(certBasicList.size() == 2) {//已有2個憑證
			orig = certBasicList.get(0);
			long origCreateAt = orig.getCreateAt();//憑證創建日
			long dpb0085_2_origExpiredAt = orig.getExpiredAt();//憑證到期日
			
			//1.判斷和第1個憑證是否相同
			if(createAt == origCreateAt && expiredAt == dpb0085_2_origExpiredAt) {//憑證日期相同
				//相同,update
				long dpb0085_2_clientCertId = -1;
				if(orig instanceof TsmpClientCert) {
					dpb0085_2_clientCertId = ((TsmpClientCert)orig).getClientCertId();
				}else if(orig instanceof TsmpClientCert2) {
					dpb0085_2_clientCertId = ((TsmpClientCert2)orig).getClientCert2Id();
				}
				long lv = getLv(lockVersions, dpb0085_2_clientCertId);
				updateData(tsmpCert, clientId, fileContentByte, fileName, userName, lv, orig);
			}else {
				//不同
				//2.判斷和第2個憑證是否相同
				orig = certBasicList.get(1);
				origCreateAt = orig.getCreateAt();//憑證創建日
				dpb0085_2_origExpiredAt = orig.getExpiredAt();//憑證創建日
				
				if(createAt == origCreateAt && expiredAt == dpb0085_2_origExpiredAt) {//憑證日期相同
					//相同,update
					long clientCertId = -1;
					if(orig instanceof TsmpClientCert) {
						clientCertId = ((TsmpClientCert)orig).getClientCertId();
					}else if(orig instanceof TsmpClientCert2) {
						clientCertId = ((TsmpClientCert2)orig).getClientCert2Id();
					}
					long lv = getLv(lockVersions, clientCertId);
					updateData(tsmpCert, clientId, fileContentByte, fileName, userName, lv, orig);
				}else {
					//不同,delete最舊的 + insert
					if(expiredAt > dpb0085_2_origExpiredAt) {//比原來的憑證到期日新
						deleteAndSave(tsmpCert, orig, clientId, fileContentByte, fileName, userName, certType);
					}
				}
			}
		}

		DPB0085Resp resp = new DPB0085Resp();
		return resp;
	}
	
	@Transactional
	public void deleteAndSave(TsmpCertificate tsmpCert, TsmpClientCertBasic orig, String clientId,
			byte[] fileContentByte, String fileName, String userName, String certType) {
		if(orig instanceof TsmpClientCert) {
			getTsmpClientCertDao().delete((TsmpClientCert)orig);
		}else if(orig instanceof TsmpClientCert2) {
			getTsmpClientCert2Dao().delete((TsmpClientCert2)orig);
		}
		insertData(tsmpCert, clientId, fileContentByte, fileName, userName, certType);
	}
	
	/**
	 * 只保留2個最新的憑證,其他的刪除
	 * 
	 * @param certList
	 */
	@Transactional
	public void delete(List<TsmpClientCertBasic> certBasicList) {
		TsmpClientCertBasic orig;
		for (int i = 0; i < certBasicList.size(); i++) {
			orig = certBasicList.get(i);
			if(i > 1) {
				if(orig instanceof TsmpClientCert) {
					getTsmpClientCertDao().delete((TsmpClientCert)orig);
				}else if(orig instanceof TsmpClientCert2) {
					getTsmpClientCert2Dao().delete((TsmpClientCert2)orig);
				}
			}
		} 
	}
	
	private long getLv(Map<Long, Long> lockVersions, long clientCertId) {
		long lv = 0;
		if(lockVersions != null && !lockVersions.isEmpty()) {
			lv = lockVersions.get(clientCertId);
		}else {
			throw TsmpDpAaRtnCode._1290.throwing();
		}
		return lv;
	}
	
	private void updateData(TsmpCertificate tsmpCert, String clientId, byte[] fileContentByte, 
			String fileName, String userName, Long lv, TsmpClientCertBasic orig) {
		
		// 深層拷貝
		TsmpClientCertBasic c = new TsmpClientCertBasic();
		if(orig instanceof TsmpClientCert) {
			c = ServiceUtil.deepCopy((TsmpClientCert)orig, TsmpClientCert.class);
		}else if(orig instanceof TsmpClientCert2) {
			c = ServiceUtil.deepCopy((TsmpClientCert2)orig, TsmpClientCert2.class);
		}
		
		c.setUpdateDateTime(DateTimeUtil.now());
		c.setUpdateUser(userName);
		c.setVersion(lv);	
		
		saveData(tsmpCert, c, clientId, fileContentByte, fileName);
	}
	
	private void insertData(TsmpCertificate tsmpCert, String clientId, byte[] fileContentByte, 
			String fileName, String userName, String certType) {
		TsmpClientCertBasic c = new TsmpClientCertBasic();
		
		if(TsmpDpCertType.JWE.value().equals(certType)) {
			c = new TsmpClientCert();
		}else if(TsmpDpCertType.TLS.value().equals(certType)) {
			c = new TsmpClientCert2();
		}
		
		c.setCreateDateTime(DateTimeUtil.now());
		c.setCreateUser(userName);
		saveData(tsmpCert, c, clientId, fileContentByte, fileName);
	}
	
	private void saveData(TsmpCertificate tsmpCert, TsmpClientCertBasic c, String clientId,
			byte[] fileContentByte, String fileName) {
		
		c.setClientId(clientId);
		c.setCertFileName(fileName);
		c.setFileContent(fileContentByte);		
		
		c.setPubKey(tsmpCert.getPubKey());//公鑰
		c.setCertVersion(tsmpCert.getCertVersion());//憑證版本
		c.setCertSerialNum(tsmpCert.getCertSerialNum());//憑證序號
		c.setsAlgorithmId(tsmpCert.getsAlgorithmId());//簽章演算法
		c.setAlgorithmId(tsmpCert.getAlgorithmId());//公鑰演算法
		c.setCertThumbprint(tsmpCert.getCertThumbprint());//CA數位指紋
		c.setIuid(tsmpCert.getIuid());//發行方ID
		c.setIssuerName(tsmpCert.getIssuerName());//發行方名稱
		c.setSuid(tsmpCert.getSuid());//持有者身分ID
		c.setCreateAt(tsmpCert.getCreateAt());//憑證創建日 (轉long)
		c.setExpiredAt(tsmpCert.getExpiredAt());//憑證到期日 (轉long)
		c.setKeySize(tsmpCert.getKeySize());
		
		try {
			if(c instanceof TsmpClientCert) {
				c = getTsmpClientCertDao().saveAndFlush((TsmpClientCert)c);
			}else if(c instanceof TsmpClientCert2) {
				c = getTsmpClientCert2Dao().saveAndFlush((TsmpClientCert2)c);
			}
		} catch (ObjectOptimisticLockingFailureException e) {
			throw TsmpDpAaRtnCode.ERROR_DATA_EDITED.throwing();
		} catch (Exception e) {
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			}
		}
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
