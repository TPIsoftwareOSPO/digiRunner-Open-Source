package tpi.dgrv4.dpaa.service;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
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
import tpi.dgrv4.dpaa.vo.DPB9931Req;
import tpi.dgrv4.entity.entity.DgrWebSocketMapping;
import tpi.dgrv4.entity.repository.DgrWebSocketMappingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9931Service {
	private TPILogger logger = TPILogger.tl;

	private DgrWebSocketMappingDao dgrWebSocketMappingDao;

	@Autowired
	public DPB9931Service(DgrWebSocketMappingDao dgrWebSocketMappingDao) {
		super();
		this.dgrWebSocketMappingDao = dgrWebSocketMappingDao;
	}

	public void exportWebsocketProxy(TsmpAuthorization tsmpAuthorization, DPB9931Req req, HttpServletResponse response) {

		try (XSSFWorkbook workbook = new XSSFWorkbook()){
			OutputStream outputStream = response.getOutputStream();
			XSSFSheet sheet = workbook.createSheet();

			//抬頭
			this.writeHeader(workbook, sheet);
			
			//資料
			this.writeData(sheet);
			
			sheet.setColumnWidth(0, 5120); //20*256
		    sheet.setColumnWidth(1, 5120); //20*256
		    sheet.setColumnWidth(2, 7680); //30*256
		    sheet.setColumnWidth(3, 12800); //50*256
		    
		    doExportWebsocketProxy(response);
			//寫入串流
			workbook.write(outputStream);
			workbook.close();
		    outputStream.close();
		    
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		} 

	}
	
	private void writeHeader(XSSFWorkbook wb, XSSFSheet sheet) {
		
		//粗體字
		XSSFCellStyle cellStyle = wb.createCellStyle();
		XSSFFont font = wb.createFont();
		font.setBold(true);
		cellStyle.setFont(font);
		//置中
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		
        Row row = sheet.createRow(0);
        
        String[] headers = {"SITE_NAME","TARGET_WS","MEMO","AUTH"};
        
        int columnIndex = 0;
        for(String h : headers) {
        	Cell cell = row.createCell(columnIndex++);
            cell.setCellValue(h);
            cell.setCellStyle(cellStyle);
        }
	}
	
	private void writeData(XSSFSheet sheet) {
		List<DgrWebSocketMapping> list = getDgrWebSocketMappingDao().findAll();
		if(CollectionUtils.isEmpty(list)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		int rowIndex = 1;
		for(DgrWebSocketMapping vo : list) {
			Row row = sheet.createRow(rowIndex++);
			Cell cell = row.createCell(0);
	        cell.setCellValue(vo.getSiteName());
	        
			cell = row.createCell(1);
	        cell.setCellValue(vo.getTargetWs());
	        
	        cell = row.createCell(2);
	        cell.setCellValue(vo.getMemo());
	        
	        cell = row.createCell(3);
	        cell.setCellValue(vo.getAuth());
		}
	}
	
	private void doExportWebsocketProxy(HttpServletResponse response) {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmm");
        String nowDateTime = dateFormatter.format(new Date());
        
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=WebSocket_" + nowDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        //checkmarx, Missing HSTS Header
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
        
	}

	protected DgrWebSocketMappingDao getDgrWebSocketMappingDao() {
		return dgrWebSocketMappingDao;
	}

}
