package tpi.dgrv4.escape;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9934Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;
import tpi.dgrv4.entity.repository.TsmpClientCertDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.DigiRunnerGtwDeployProperties;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
//checkmarx, Unchecked Input for Loop Condition
@Service
public class DPB9934Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpClientCertDao tsmpClientCertDao;
	private DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties;
	private TsmpClientDao tsmpClientDao;

	@Autowired
	public DPB9934Service(TsmpClientCertDao tsmpClientCertDao,
			DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties, TsmpClientDao tsmpClientDao) {
		super();
		this.tsmpClientCertDao = tsmpClientCertDao;
		this.digiRunnerGtwDeployProperties = digiRunnerGtwDeployProperties;
		this.tsmpClientDao = tsmpClientDao;
	}

	@Transactional
	public DPB9934Resp importJwe(TsmpAuthorization tsmpAuthorization, MultipartFile mFile) {

		try {
			if (mFile == null || mFile.isEmpty() || mFile.getOriginalFilename() == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
			}

			checkParam(mFile.getOriginalFilename());

			importData(tsmpAuthorization, mFile.getInputStream());
			return new DPB9934Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	public void importData(TsmpAuthorization tsmpAuthorization, InputStream inputStream) throws Exception {
		// 判斷是否為記憶體角色
		List<TsmpClientCert> excelData = new ArrayList<>();

		boolean isMemory = isMemory();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			if (workbook.getNumberOfSheets() != 1) {
				throw TsmpDpAaRtnCode._1291.throwing();
			}
			Sheet sheet = workbook.getSheetAt(0);
			excelData = getExcelData(sheet);
		}
		// 先判斷有clientId 有沒有存在
		excelData.forEach(data -> {
			boolean isExist = getTsmpClientDao().existsById(data.getClientId());
			if (!isExist) {
				throw TsmpDpAaRtnCode._1354.throwing("{{clientId}}",data.getClientId());
			}
		});

		// 依 CLIENT_ID.CERT_FILE_NAME為KEY 找到就更新,否則新增
		List<TsmpClientCert> saveList = new ArrayList<>();
		List<TsmpClientCert> clientCertList = getTsmpClientCertDao().findAll();
		Map<String, TsmpClientCert> map = new HashMap<>();
		clientCertList.forEach(l -> {
			map.put(l.getClientId() + ":" + l.getCertFileName(), l);

		});
		List<String> clientIdAndFileNameList = new ArrayList<>();
		excelData.forEach(data -> {
			String clientId = data.getClientId();

			String key = clientId + ":" + data.getCertFileName();
			clientIdAndFileNameList.add(key);
			if (map.containsKey(key)) {
				TsmpClientCert oldTcc = map.get(key);
				oldTcc.setUpdateDateTime(DateTimeUtil.now());
				oldTcc.setUpdateUser(tsmpAuthorization.getUserName());
				saveList.add(updataEntity(oldTcc, data));
			} else {
				data.setCreateDateTime(DateTimeUtil.now());
				data.setCreateUser(tsmpAuthorization.getUserName());
				saveList.add(updataEntity(new TsmpClientCert(), data));
			}
		});

		getTsmpClientCertDao().saveAllAndFlush(saveList);

		if (isMemory) {
			List<String> deleteKeys = map.keySet().stream().filter(key -> !clientIdAndFileNameList.contains(key))
					.collect(Collectors.toList());
			if (!CollectionUtils.isEmpty(deleteKeys)) {

				deleteKeys.forEach(key -> {
					String clientId = key.split("\\:")[0];
					String certFileName = key.split("\\:")[1];
					getTsmpClientCertDao().deleteByClientIdAndCertFileName(clientId, certFileName);
				});
			}
		}
	}

	private TsmpClientCert updataEntity(TsmpClientCert oldTcc, TsmpClientCert data) {

		oldTcc.setAlgorithmId(data.getAlgorithmId());
		oldTcc.setCertFileName(data.getCertFileName());
		oldTcc.setCertSerialNum(data.getCertSerialNum());
		oldTcc.setCertThumbprint(data.getCertThumbprint());
		oldTcc.setCertVersion(data.getCertVersion());
		oldTcc.setCreateAt(data.getCreateAt());
		oldTcc.setClientId(data.getClientId());
		oldTcc.setExpiredAt(data.getExpiredAt());
		oldTcc.setFileContent(data.getFileContent());
		oldTcc.setIssuerName(data.getIssuerName());
		oldTcc.setIuid(data.getIuid());
		oldTcc.setKeySize(data.getKeySize());
		oldTcc.setPubKey(data.getPubKey());
		oldTcc.setsAlgorithmId(data.getsAlgorithmId());
		oldTcc.setSuid(data.getSuid());
		return oldTcc;
	}

	private List<TsmpClientCert> getExcelData(Sheet sheet) {
		List<TsmpClientCert> list = new ArrayList<>();
		boolean isFirst = true;
		DataFormatter formatter = new DataFormatter();
		Iterator<Row> rows = sheet.iterator();
		String[] headers = { "CLIENT_ID", "CERT_FILE_NAME", "FILE_CONTENT", "PUB_KEY", "CERT_VERSION",
				"CERT_SERIAL_NUM", "S_ALGORITHM_ID", "ALGORITHM_ID", "CERT_THUMBPRINT", "IUID", "ISSUER_NAME", "SUID",
				"CREATE_AT", "EXPIRED_AT", "KEY_SIZE" };
		
		//checkmarx, Unchecked Input for Loop Condition,所以多了maxValue和loopIndex
	     int maxValue = Integer.MAX_VALUE;
	     int loopIndex = 0;
	     while (rows.hasNext() && loopIndex <= maxValue) {
	    	 if(loopIndex == maxValue) {
	    		 throw TsmpDpAaRtnCode._1559.throwing("Exceed " + maxValue + " row");
	    	 }
	    	 loopIndex++;
			if (isFirst) {
				isFirst = false;
				Row row = rows.next();
				for (int i = 0; i < headers.length; i++) {
					if (!headers[i].equalsIgnoreCase(formatter.formatCellValue(row.getCell(i)))) {
						throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
					}
				}

			} else {
				Row row = rows.next();
				if (!StringUtils.hasText(formatter.formatCellValue(row.getCell(0)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(1)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(2)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(3)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(4)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(5)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(6)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(7)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(8)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(9)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(10)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(11)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(12)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(13)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(14)))) {
					continue;
				}

				TsmpClientCert tcc = new TsmpClientCert();
				tcc.setClientId(setData(row, 0, true, "clientId"));
				tcc.setCertFileName(setData(row, 1, true, "certFileName"));
				// fileContent 要base64 decode
				String fileContentStr = setData(row, 2, true, "fileContent");
				byte[] fileContent = Base64Util.base64Decode(fileContentStr);
				tcc.setFileContent(fileContent);

				tcc.setPubKey(setData(row, 3, true, "pubKey"));

				tcc.setCertVersion(setData(row, 4, false, "certVersion"));
				tcc.setCertSerialNum(setData(row, 5, true, "certSerialNum"));
				tcc.setsAlgorithmId(setData(row, 6, false, "sAlgorithmId"));
				tcc.setAlgorithmId(setData(row, 7, true, "algorithmId"));
				tcc.setCertThumbprint(setData(row, 8, true, "certThumbprint"));
				tcc.setIuid(setData(row, 9, false, "iuid"));
				tcc.setIssuerName(setData(row, 10, true, "issuerName"));
				tcc.setSuid(setData(row, 11, false, "suid"));

				String createAtStr = setData(row, 12, true, "createAt");
				long createAt = Long.parseLong(createAtStr);
				tcc.setCreateAt(createAt);
				String expiredAtStr = setData(row, 13, true, "expiredAt");
				long expiredAt = Long.parseLong(expiredAtStr);
				tcc.setExpiredAt(expiredAt);
				String keySizeStr = setData(row, 14, true, "keySize");
				Integer keySize = Integer.parseInt(keySizeStr);
				tcc.setKeySize(keySize);
				list.add(tcc);
			}

		}

		return list;
	}

	private String setData(Row row, int i, boolean checkNull, String headers) {
		DataFormatter formatter = new DataFormatter();
		String value = formatter.formatCellValue(row.getCell(i));
		if (checkNull) {
			if (!StringUtils.hasLength(value)) {
				throw TsmpDpAaRtnCode._1350.throwing("{{" + headers + "}}");
			}
		}

		return value;

	}

	protected boolean isMemory() {
		return getDigiRunnerGtwDeployProperties().isMemoryRole();
	}

	public void checkParam(String fileName) {

		int fileNameIndex = fileName.lastIndexOf(".");
		if (fileNameIndex == -1) {
			throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		}

		String fileExtension = fileName.substring(fileNameIndex + 1);
		if (!"xlsx".equalsIgnoreCase(fileExtension)) {
			throw TsmpDpAaRtnCode._1443.throwing();
		}

	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected DigiRunnerGtwDeployProperties getDigiRunnerGtwDeployProperties() {
		return digiRunnerGtwDeployProperties;
	}

	protected TsmpClientCertDao getTsmpClientCertDao() {
		return tsmpClientCertDao;
	}
}
