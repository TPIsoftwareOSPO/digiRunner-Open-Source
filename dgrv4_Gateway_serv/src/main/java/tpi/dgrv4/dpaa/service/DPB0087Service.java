package tpi.dgrv4.dpaa.service;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.constant.TsmpDpCertType;
import tpi.dgrv4.dpaa.vo.DPB0087Req;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert2;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCertBasic;
import tpi.dgrv4.entity.repository.TsmpClientCert2Dao;
import tpi.dgrv4.entity.repository.TsmpClientCertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0087Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpClientCertDao tsmpClientCertDao;
	private TsmpClientCert2Dao tsmpClientCert2Dao;
	private BcryptParamHelper helper;

	@Autowired
	public DPB0087Service(TsmpClientCertDao tsmpClientCertDao, TsmpClientCert2Dao tsmpClientCert2Dao,
			BcryptParamHelper helper) {
		super();
		this.tsmpClientCertDao = tsmpClientCertDao;
		this.tsmpClientCert2Dao = tsmpClientCert2Dao;
		this.helper = helper;
	}

	public ResponseEntity<byte[]> downLoadPEMFile(TsmpAuthorization auth, DPB0087Req req, ReqHeader reqHeader){
		try {
			String certType = getCertType(req, reqHeader.getLocale());//解碼
			
			List<Long> clientCertIds = req.getIds();
			if (clientCertIds == null || clientCertIds.isEmpty()) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			List<TsmpClientCertBasic> certList = getCertList(clientCertIds, certType);
			if (certList == null || certList.isEmpty()) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			byte[] content = getPemFilesZip(certList);
			if (content == null || content.length == 0) {
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			
			String zipFileName = getZipFileName();
			
			return ResponseEntity.ok()
					.contentLength(content.length)
					.header(HttpHeaders.CONTENT_TYPE, "application/zip")
					.header(HttpHeaders.CONTENT_DISPOSITION, getContentDisposition(zipFileName))
					.body(content);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	public String getCertType(DPB0087Req req, String locale) {
		String encodeCertType = req.getEncodeCertType();//使用BcryptParam, ITEM_NO='CERT_TYPE', JWE 使用 TSMP_CLIENT_CERT, TLS 使用 TSMP_CLIENT_CERT2
		if (StringUtils.isEmpty(encodeCertType)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		String dpb0087_certType = getDecodeCertType(encodeCertType, locale);//解碼
		
		return dpb0087_certType;
	}
	
	public String getDecodeCertType(String encodeCertType, String locale) {
		String dpb0087_certType = null;
		try {
			dpb0087_certType = getBcryptParamHelper().decode(encodeCertType, "CERT_TYPE", locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return dpb0087_certType;
	}

	private List<TsmpClientCertBasic> getCertList(List<Long> clientCertIds, String certType){
		List<TsmpClientCertBasic> certList = new ArrayList<>();
		TsmpClientCertBasic cert = new TsmpClientCertBasic();
		for(Long clientCertId : clientCertIds) {
			if(TsmpDpCertType.JWE.value().equals(certType)) {
				Optional<TsmpClientCert> opt = getTsmpClientCertDao().findById(clientCertId);
				if (!opt.isPresent()) {
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				cert = opt.get();
				
			}else if(TsmpDpCertType.TLS.value().equals(certType)) {
				Optional<TsmpClientCert2> opt = getTsmpClientCert2Dao().findById(clientCertId);
				if (!opt.isPresent()) {
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				cert = opt.get();
			}
			
			if (
				StringUtils.isEmpty(cert.getCertFileName()) ||
				cert.getFileContent() == null ||
				cert.getFileContent().length == 0
			) {
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			certList.add(cert);
		}
		return certList;
	}

	public byte[] getPemFilesZip(List<TsmpClientCertBasic> certList) {
		ByteArrayOutputStream baOs = new ByteArrayOutputStream();

		ZipOutputStream zipOs = null;
		try {
			// 設定壓縮流：直接寫入response，實現邊壓縮邊下載
			//zipos = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
			// 設定壓縮流：寫入暫存的ByteArray
			zipOs = new ZipOutputStream(baOs);
			zipOs.setMethod(ZipOutputStream.DEFLATED);// 設定壓縮方法

			// 將資料寫入壓縮流
			String fileName = null;
			byte[] content = null;
			for (TsmpClientCertBasic cert : certList) {
				if(cert instanceof TsmpClientCert) {
					fileName = composeCertFileName(((TsmpClientCert)cert).getClientCertId(), cert.getCertFileName());
					
				}else if(cert instanceof TsmpClientCert2) {
					fileName = composeCertFileName(((TsmpClientCert2)cert).getClientCert2Id(), cert.getCertFileName());
				}
				content = cert.getFileContent();
				
				// 新增ZipEntry，並將ZipEntry寫入檔案流
				zipOs.putNextEntry(new ZipEntry(fileName));
				zipOs.write(content);
				zipOs.flush();
				zipOs.closeEntry();
			}
		} catch (Exception e){
			this.logger.error(StackTraceUtil.logStackTrace(e));
		} finally {
			try {
				if (zipOs != null) {
					zipOs.close();
				}
				baOs.flush();
				baOs.close();
			} catch (Exception e) {
				this.logger.error(StackTraceUtil.logStackTrace(e));
			}
		}

		return baOs.toByteArray();
	}

	private String composeCertFileName(Long clientCertId, String certFileName) {
		return String.valueOf(clientCertId).concat("-").concat(certFileName);
	}

	private String getZipFileName() {
		return "PEM.zip";
	}

	/**
	 * Content-Disposition: attachment;
	 * filename="$encoded_fname";
	 * filename*=utf-8''$encoded_fname
	 * @param filename
	 * @return
	 */
	private String getContentDisposition(String filename) {
		int dpb0087_idx = filename.lastIndexOf(".");
		String encoded_fname = filename.substring(0, dpb0087_idx);
		try {
			encoded_fname = URLEncoder.encode(encoded_fname, StandardCharsets.UTF_8.displayName());
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
		encoded_fname += filename.substring(dpb0087_idx);

		String rtn = "attachment; ";
		rtn += "filename=\"" + encoded_fname + "\"; ";
		rtn += "filename*=utf-8''" + encoded_fname;

		return rtn;
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
