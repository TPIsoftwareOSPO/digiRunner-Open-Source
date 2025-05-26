package tpi.dgrv4.dpaa.service;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9933Req;
import tpi.dgrv4.dpaa.vo.DPB9933Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;
import tpi.dgrv4.entity.repository.TsmpClientCertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9933Service {
	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientCertDao tsmpClientCertDao;

	@Autowired
	public DPB9933Service(TsmpClientCertDao tsmpClientCertDao) {
		super();
		this.tsmpClientCertDao = tsmpClientCertDao;
	}

	public DPB9933Resp exportJwe(TsmpAuthorization tsmpAuthorization, DPB9933Req req, HttpServletResponse response) {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			OutputStream outputStream = response.getOutputStream();

			XSSFSheet sheet = workbook.createSheet("TSMP_CLIENT_CERT");
			DataFormat format = workbook.createDataFormat();
			short txtformat = format.getFormat("@");
			// 抬頭
			this.writeHeader(workbook, sheet);

			// 資料
			this.writeData(sheet, txtformat);
			doExportJwe(response);

			// 寫入串流
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return new DPB9933Resp();

	}

	private void doExportJwe(HttpServletResponse response) {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmm");
		String nowDateTime = dateFormatter.format(new Date());

		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=JWE_" + nowDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);
		  //checkmarx, Missing HSTS Header
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
        
	}

	private void writeData(XSSFSheet sheet, short txtformat) {
		List<TsmpClientCert> list = getTsmpClientCertDao().findAll();
		if (CollectionUtils.isEmpty(list)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		int rowIndex = 1;
//		"CLIENT_ID", "CERT_FILE_NAME", "FILE_CONTENT", "PUB_KEY", "CERT_VERSION",
//		"CERT_SERIAL_NUM", "S_ALGORITHM_ID", "ALGORITHM_ID", "CERT_THUMBPRINT", "IUID", "ISSUER_NAME", "SUID",
//		"CREATE_AT", "EXPIRED_AT", "KEY_SIZE" 
		for (TsmpClientCert vo : list) {
			Row row = sheet.createRow(rowIndex++);
			Cell cell = row.createCell(0);
			cell.setCellValue(vo.getClientId());

			cell = row.createCell(1);
			cell.setCellValue(vo.getCertFileName());

			cell = row.createCell(2);
			cell.setCellValue(Base64Util.base64EncodeWithoutPadding(vo.getFileContent()));

			cell = row.createCell(3);
			cell.setCellValue(vo.getPubKey());

			cell = row.createCell(4);
			cell.setCellValue(vo.getCertVersion());

			cell = row.createCell(5);
			cell.setCellValue(vo.getCertSerialNum());
			cellStyle.setDataFormat(txtformat);
			cell.setCellStyle(cellStyle);
			
			
			cell = row.createCell(6);
			cell.setCellValue(vo.getsAlgorithmId());

			cell = row.createCell(7);
			cell.setCellValue(vo.getAlgorithmId());

			cell = row.createCell(8);
			cell.setCellValue(vo.getCertThumbprint());

			cell = row.createCell(9);
			cell.setCellValue(vo.getIuid());

			cell = row.createCell(10);
			cell.setCellValue(vo.getIssuerName());

			cell = row.createCell(11);
			cell.setCellValue(vo.getSuid());

			cell = row.createCell(12);
			cell.setCellValue(String.valueOf(vo.getCreateAt()));
			cellStyle.setDataFormat(txtformat);
			cell.setCellStyle(cellStyle);
			cell = row.createCell(13);
			cell.setCellValue(String.valueOf(vo.getExpiredAt()));
			cellStyle.setDataFormat(txtformat);
			cell.setCellStyle(cellStyle);

			cell = row.createCell(14);
			cell.setCellValue(vo.getKeySize());
		}

		sheet.setColumnWidth(0, 20 * 256);
		sheet.setColumnWidth(1, 30 * 256);
		sheet.setColumnWidth(2, 50 * 256);
		sheet.setColumnWidth(3, 50 * 256);
		sheet.setColumnWidth(4, 20 * 256);
		sheet.setColumnWidth(5, 20 * 256);
		sheet.setColumnWidth(6, 20 * 256);
		sheet.setColumnWidth(7, 20 * 256);
		sheet.setColumnWidth(8, 50 * 256);
		sheet.setColumnWidth(9, 20 * 256);
		sheet.setColumnWidth(10, 20 * 256);
		sheet.setColumnWidth(11, 20 * 256);
		sheet.setColumnWidth(12, 20 * 256);
		sheet.setColumnWidth(13, 20 * 256);
		sheet.setColumnWidth(14, 20 * 256);

	}

	private void writeHeader(XSSFWorkbook wb, XSSFSheet sheet) {

		// 粗體字
		XSSFCellStyle cellStyle = wb.createCellStyle();
		XSSFFont font = wb.createFont();
		font.setBold(true);
		cellStyle.setFont(font);
		// 置中
		cellStyle.setAlignment(HorizontalAlignment.CENTER);

		Row row = sheet.createRow(0);

		String[] headers = { "CLIENT_ID", "CERT_FILE_NAME", "FILE_CONTENT", "PUB_KEY", "CERT_VERSION",
				"CERT_SERIAL_NUM", "S_ALGORITHM_ID", "ALGORITHM_ID", "CERT_THUMBPRINT", "IUID", "ISSUER_NAME", "SUID",
				"CREATE_AT", "EXPIRED_AT", "KEY_SIZE" };

		int columnIndex = 0;
		for (String h : headers) {
			Cell cell = row.createCell(columnIndex++);
			cell.setCellValue(h);
			cell.setCellStyle(cellStyle);
		}
	}

	protected TsmpClientCertDao getTsmpClientCertDao() {
		return tsmpClientCertDao;
	}
}
