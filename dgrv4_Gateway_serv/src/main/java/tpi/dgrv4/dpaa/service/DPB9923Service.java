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
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9923Req;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9923Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpDpItemsDao tsmpDpItemsDao;

	@Autowired
	public DPB9923Service(TsmpDpItemsDao tsmpDpItemsDao) {
		super();
		this.tsmpDpItemsDao = tsmpDpItemsDao;
	}

	public void exportTsmpDpItems(TsmpAuthorization tsmpAuthorization, DPB9923Req req, HttpServletResponse response) {

		try (XSSFWorkbook workbook = new XSSFWorkbook()){
			OutputStream outputStream = response.getOutputStream();
			XSSFSheet sheet = workbook.createSheet();

			//抬頭
			this.writeHeader(workbook, sheet);
			
			//資料
			this.writeData(sheet);
			
			sheet.setColumnWidth(0, 2560); //10*256
		    sheet.setColumnWidth(1, 5120); //20*256
		    sheet.setColumnWidth(2, 12800); //50*256
		    sheet.setColumnWidth(3, 10240); //40*256
		    sheet.setColumnWidth(4, 12800); //50*256
		    sheet.setColumnWidth(5, 2560); //10*256
		    sheet.setColumnWidth(6, 3840); //10*256
		    sheet.setColumnWidth(7, 5120); //10*256
		    sheet.setColumnWidth(8, 5120); //20*256
		    sheet.setColumnWidth(9, 5120); //20*256
		    sheet.setColumnWidth(10, 5120); //20*256
		    sheet.setColumnWidth(11, 5120); //20*256
		    sheet.setColumnWidth(12, 3840); //15*256
		    
		    doExportTsmpDpItems(response);
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
        
        String[] headers = {"ITEM_ID","ITEM_NO","ITEM_NANE","SUBITEM_NO","SUBITEM_NAME","SORT_BY","IS_DEFAULT","PARAM1",
                            "PARAM2","PARAM3","PARAM4","PARAM5","LOCALE"};
        
        int columnIndex = 0;
        for(String h : headers) {
        	Cell cell = row.createCell(columnIndex++);
            cell.setCellValue(h);
            cell.setCellStyle(cellStyle);
        }
	}
	
	private void writeData(XSSFSheet sheet) {
		List<TsmpDpItems> list = getTsmpDpItemsDao().findAllByOrderByItemIdAsc();
		if(CollectionUtils.isEmpty(list)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		XSSFCellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		XSSFDataFormat format = sheet.getWorkbook().createDataFormat();
		short txtformat = format.getFormat("@");
		//設定字串
		cellStyle.setDataFormat(txtformat);
		//靠右
		cellStyle.setAlignment(HorizontalAlignment.RIGHT);
		
		int rowIndex = 1;
		for(TsmpDpItems vo : list) {
			Row row = sheet.createRow(rowIndex++);
			Cell cell = row.createCell(0);
	        cell.setCellValue(String.valueOf(vo.getItemId()));
	        
			cell = row.createCell(1);
	        cell.setCellValue(vo.getItemNo());
	        cell.setCellStyle(cellStyle);
	        
	        cell = row.createCell(2);
	        cell.setCellValue(vo.getItemName());
	        cell.setCellStyle(cellStyle);
	        
	        cell = row.createCell(3);
	        cell.setCellValue(vo.getSubitemNo());
	        cell.setCellStyle(cellStyle);
	        
	        cell = row.createCell(4);
	        cell.setCellValue(vo.getSubitemName());
	        cell.setCellStyle(cellStyle);
	        
	        cell = row.createCell(5);
	        cell.setCellValue(String.valueOf(vo.getSortBy()));
	        cell.setCellStyle(cellStyle);
	        
	        cell = row.createCell(6);
	        cell.setCellValue(vo.getIsDefault());
	        cell.setCellStyle(cellStyle);
	        
	        cell = row.createCell(7);
	        cell.setCellValue(vo.getParam1());
	        cell.setCellStyle(cellStyle);
	        
	        cell = row.createCell(8);
	        cell.setCellValue(vo.getParam2());
	        cell.setCellStyle(cellStyle);
	        
	        cell = row.createCell(9);
	        cell.setCellValue(vo.getParam3());
	        cell.setCellStyle(cellStyle);
	        
	        cell = row.createCell(10);
	        cell.setCellValue(vo.getParam4());
	        cell.setCellStyle(cellStyle);
	        
	        cell = row.createCell(11);
	        cell.setCellValue(vo.getParam5());
	        cell.setCellStyle(cellStyle);
	        
	        cell = row.createCell(12);
	        cell.setCellValue(vo.getLocale());
	        cell.setCellStyle(cellStyle);
		}
	}
	
	private void doExportTsmpDpItems(HttpServletResponse response) {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmm");
        String nowDateTime = dateFormatter.format(new Date());
        
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Items_" + nowDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        //checkmarx, Missing HSTS Header
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
        
	}

	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return tsmpDpItemsDao;
	}

}
