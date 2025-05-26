package tpi.dgrv4.dpaa.service;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;

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

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9929Req;
import tpi.dgrv4.dpaa.vo.DPB9929Resp;
import tpi.dgrv4.entity.entity.DgrWebsite;
import tpi.dgrv4.entity.entity.DgrWebsiteDetail;
import tpi.dgrv4.entity.repository.DgrWebsiteDao;
import tpi.dgrv4.entity.repository.DgrWebsiteDetailDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9929Service {
	private TPILogger logger = TPILogger.tl;

	private DgrWebsiteDao dgrWebsiteDao;
	private DgrWebsiteDetailDao dgrWebsiteDetailDao;

	@Autowired
	public DPB9929Service(DgrWebsiteDao dgrWebsiteDao, DgrWebsiteDetailDao dgrWebsiteDetailDao) {
		super();
		this.dgrWebsiteDao = dgrWebsiteDao;
		this.dgrWebsiteDetailDao = dgrWebsiteDetailDao;
	}

	public DPB9929Resp exportWebsiteProxy(TsmpAuthorization tsmpAuthorization, DPB9929Req req,
			HttpServletResponse response) {

		try {

			List<DgrWebsite> list = getDgrWebsiteDao().findAll();
			if (CollectionUtils.isEmpty(list)) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			List<DgrWebsiteDetail> list2 = getDgrWebsiteDetailDao().findAll();
			if (CollectionUtils.isEmpty(list)) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			XSSFWorkbook workbook = new XSSFWorkbook();
			OutputStream outputStream = response.getOutputStream();
			// DGR_WEBSITE
			XSSFSheet sheet = workbook.createSheet("DGR_WEBSITE");
			String[] wsheaders = { "WEBSITE_NAME", "WEBSITE_STATUS", "REMARK", "AUTH", "SQL_INJECTION", "TRAFFIC",
					"XSS", "XXE", "TPS", "IGNORE_API", "SHOW_LOG" };
			DataFormat format = workbook.createDataFormat();
			short txtformat = format.getFormat("@");
			// DGR_WEBSITE 抬頭
			this.writeHeader(workbook, sheet, wsheaders);
			// DGR_WEBSITE 資料
			this.writeWSData(sheet, txtformat, list);
			Map<String, List<DgrWebsiteDetail>> finalDetailmap = mappingDgrWebsiteDetail(list, list2);
			// DGR_WEBSITE_DETAIL
			XSSFSheet sheet2 = workbook.createSheet("DGR_WEBSITE_DETAIL");
			String[] wsdheaders = { "WEBSITE_NAME", "PROBABILITY", "URL" };

			// 抬頭
			this.writeHeader(workbook, sheet2, wsdheaders);
			// 資料
			this.writeWSDData(sheet2, txtformat, finalDetailmap);
			doExportWebsiteProxy(response);
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
		return new DPB9929Resp();

	}

	private Map<String, List<DgrWebsiteDetail>> mappingDgrWebsiteDetail(List<DgrWebsite> list,
			List<DgrWebsiteDetail> list2) {
		Map<String, List<DgrWebsiteDetail>> map = new TreeMap<>();
		Map<Long, List<DgrWebsiteDetail>> map2 = list2.stream()
				.collect(Collectors.groupingBy(DgrWebsiteDetail::getDgrWebsiteId));

		list.forEach(l -> {
			map.put(l.getWebsiteName(), map2.get(l.getDgrWebsiteId()));
		});
		
		return map;
	}

	private void doExportWebsiteProxy(HttpServletResponse response) {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmm");
		String nowDateTime = dateFormatter.format(new Date());

		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Website_" + nowDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);
		  //checkmarx, Missing HSTS Header
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
        
	}

	private void writeWSDData(XSSFSheet sheet, short txtformat, Map<String, List<DgrWebsiteDetail>> finalDetailmap) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.RIGHT);
		// "WEBSITE_NAME", "PROBABILITY", "URL"
		int rowIndex = 1;
		for (Entry<String, List<DgrWebsiteDetail>> entry : finalDetailmap.entrySet()) {

			for (DgrWebsiteDetail vo : entry.getValue()) {
				Row row = sheet.createRow(rowIndex++);
				Cell cell = row.createCell(0);
				cell.setCellValue(entry.getKey());
				cell.setCellStyle(cellStyle);

				cell = row.createCell(1);
				cell.setCellValue(vo.getProbability());
				cell.setCellStyle(cellStyle);

				cell = row.createCell(2);
				cell.setCellValue(vo.getUrl());
				cell.setCellStyle(cellStyle);

				cell = row.createCell(3);
				cell.setCellValue(vo.getKeywordSearch());
				cell.setCellStyle(cellStyle);
			}

		}

		sheet.setColumnWidth(0, 5120); // 20*256
		sheet.setColumnWidth(1, 5120); // 20*256
		sheet.setColumnWidth(2, 12800); // 50*256
		sheet.setColumnWidth(3, 10240); // 40*256
	}

	private void writeWSData(XSSFSheet sheet, short txtformat, List<DgrWebsite> list) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.RIGHT);
		int rowIndex = 1;
		for (DgrWebsite vo : list) {
			Row row = sheet.createRow(rowIndex++);
			//  "WEBSITE_NAME", "WEBSITE_STATUS", "REMARK",
			// "AUTH", "SQL_INJECTION", "TRAFFIC", "XSS", "XXE", "TPS", "IGNORE_API",
			// "SHOW_LOG"
			Cell cell = row.createCell(0);
			cell.setCellValue(vo.getWebsiteName());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(1);
			cell.setCellValue(vo.getWebsiteStatus());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(2);
			cell.setCellValue(vo.getRemark());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(3);
			cell.setCellValue(vo.getAuth());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(4);
			cell.setCellValue(vo.getSqlInjection());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(5);
			cell.setCellValue(vo.getTraffic());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(6);
			cell.setCellValue(vo.getXss());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(7);
			cell.setCellValue(vo.getXxe());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(8);
			cell.setCellValue(vo.getTps());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(9);
			cell.setCellValue(vo.getIgnoreApi());
			cell.setCellStyle(cellStyle);

			cell = row.createCell(10);
			cell.setCellValue(vo.getShowLog());
			cell.setCellStyle(cellStyle);
		}
		sheet.setColumnWidth(0, 30*256); 
		sheet.setColumnWidth(1, 20*256); 
		sheet.setColumnWidth(2, 40*256); 
		sheet.setColumnWidth(3, 20*256); 
		sheet.setColumnWidth(4, 20*256); 
		sheet.setColumnWidth(5, 20*256); 
		sheet.setColumnWidth(6, 20*256); 
		sheet.setColumnWidth(7, 20*256); 
		sheet.setColumnWidth(8, 20*256);
		sheet.setColumnWidth(9, 30*256); 
		sheet.setColumnWidth(10, 20*256);

	}

	private void writeHeader(XSSFWorkbook wb, XSSFSheet sheet, String[] headers) {

		// 粗體字
		XSSFCellStyle cellStyle = wb.createCellStyle();
		XSSFFont font = wb.createFont();
		font.setBold(true);
		cellStyle.setFont(font);
		// 置中
		cellStyle.setAlignment(HorizontalAlignment.CENTER);

		Row row = sheet.createRow(0);

		int columnIndex = 0;
		for (String h : headers) {
			Cell cell = row.createCell(columnIndex++);
			cell.setCellValue(h);
			cell.setCellStyle(cellStyle);
		}
	}

	protected DgrWebsiteDao getDgrWebsiteDao() {
		return dgrWebsiteDao;
	}

	protected DgrWebsiteDetailDao getDgrWebsiteDetailDao() {
		return dgrWebsiteDetailDao;
	}
}
