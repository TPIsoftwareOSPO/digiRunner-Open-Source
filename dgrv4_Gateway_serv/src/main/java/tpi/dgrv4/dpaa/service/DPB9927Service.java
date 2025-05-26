package tpi.dgrv4.dpaa.service;

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
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB9927Req;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpReportUrlDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DPB9927Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpFuncDao tsmpFuncDao;
	private TsmpReportUrlDao tsmpReportUrlDao;
	private BcryptParamHelper bcryptParamHelper;

	@Autowired
	public DPB9927Service(TsmpFuncDao tsmpFuncDao, TsmpReportUrlDao tsmpReportUrlDao,
			BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpFuncDao = tsmpFuncDao;
		this.tsmpReportUrlDao = tsmpReportUrlDao;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	public void exportTsmpFunc(TsmpAuthorization tsmpAuthorization, DPB9927Req req, ReqHeader reqHeader, HttpServletResponse response) {
		
		try(XSSFWorkbook workbook = new XSSFWorkbook()){	
			if(!StringUtils.hasText(req.getFuncType())) {
				throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
			}
			OutputStream outputStream = response.getOutputStream();
			
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			String funcType = getFuncTypeByBcryptParamHelper(req.getFuncType(), locale);

			XSSFSheet sheet1 = workbook.createSheet("TsmpFunc");

			//抬頭
			this.writeHeaderFunc(workbook, sheet1);
			
			//資料
			List<String> funcCodeList =  this.writeDataFunc(sheet1, funcType);
			
			sheet1.setColumnWidth(0, 5120); //20*256
		    sheet1.setColumnWidth(1, 5120); //20*256
		    sheet1.setColumnWidth(2, 5120); //20*256
		    sheet1.setColumnWidth(3, 10240); //40*256
		    sheet1.setColumnWidth(4, 5120); //20*256
		    sheet1.setColumnWidth(5, 5120); //20*256
		    
		    XSSFSheet sheet2 = workbook.createSheet("TsmpReportUrl");

			//抬頭
			this.writeHeaderReportUrl(workbook, sheet2);
			
			//資料
			this.writeDataReportUrl(sheet2, funcCodeList);
			
			sheet2.setColumnWidth(0, 5120); //20*256
			sheet2.setColumnWidth(1, 5120); //20*256
			sheet2.setColumnWidth(2, 65280); //255*256
		    
			doExportTsmpFunc(response, req, reqHeader);
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
	
	private void writeHeaderFunc(XSSFWorkbook wb, XSSFSheet sheet) {
		
		//粗體字
		XSSFCellStyle cellStyle = wb.createCellStyle();
		XSSFFont font = wb.createFont();
		font.setBold(true);
		cellStyle.setFont(font);
		//置中
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		
        Row row = sheet.createRow(0);
        
        String[] headers = {"FUNC_CODE","FUNC_NAME","FUNC_NAME_EN","FUNC_DESC","LOCALE","FUNC_TYPE"};
        
        int columnIndex = 0;
        for(String h : headers) {
        	Cell cell = row.createCell(columnIndex++);
            cell.setCellValue(h);
            cell.setCellStyle(cellStyle);
        }
	}
	
	private List<String> writeDataFunc(XSSFSheet sheet, String funcType) {
		List<String> funcCodeList = new ArrayList<String>();
		List<TsmpFunc> list = getTsmpFuncDao().findAllByFuncType(funcType);
		if(CollectionUtils.isEmpty(list)) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		int rowIndex = 1;
		for(TsmpFunc vo : list) {
			funcCodeList.add(vo.getFuncCode());
			Row row = sheet.createRow(rowIndex++);
			Cell cell = row.createCell(0);
	        cell.setCellValue(vo.getFuncCode());
	        
			cell = row.createCell(1);
	        cell.setCellValue(vo.getFuncName());
	        
	        cell = row.createCell(2);
	        cell.setCellValue(vo.getFuncNameEn());
	        
	        cell = row.createCell(3);
	        cell.setCellValue(vo.getFuncDesc());
	        
	        cell = row.createCell(4);
	        cell.setCellValue(vo.getLocale());
	        
	        cell = row.createCell(5);
	        cell.setCellValue(vo.getFuncType());
		}
		
		return funcCodeList;
	}
	
	private void writeHeaderReportUrl(XSSFWorkbook wb, XSSFSheet sheet) {
		
		//粗體字
		XSSFCellStyle cellStyle = wb.createCellStyle();
		XSSFFont font = wb.createFont();
		font.setBold(true);
		cellStyle.setFont(font);
		//置中
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		
        Row row = sheet.createRow(0);
        
        String[] headers = {"REPORT_ID","TIME_RANGE","REPORT_URL"};
        
        int columnIndex = 0;
        for(String h : headers) {
        	Cell cell = row.createCell(columnIndex++);
            cell.setCellValue(h);
            cell.setCellStyle(cellStyle);
        }
	}
	
	private void writeDataReportUrl(XSSFSheet sheet, List<String> funcCodeList) {
		List<TsmpReportUrl> list = getTsmpReportUrlDao().queryAllByReportId(funcCodeList);
		
		int rowIndex = 1;
		for(TsmpReportUrl vo : list) {
			Row row = sheet.createRow(rowIndex++);
			Cell cell = row.createCell(0);
	        cell.setCellValue(vo.getReportId());
	        
			cell = row.createCell(1);
	        cell.setCellValue(vo.getTimeRange());
	        
	        cell = row.createCell(2);
	        cell.setCellValue(vo.getReportUrl());
		}
	}
	
	/**
	 * 後台-tsmpaa( v3.8) API
	 * 
	 * 使用BcryptParam, 
	 * ITEM_NO='FUNC_TYPE' , DB儲存值對應代碼如下:
	 * DB值 (SUBITEM_NO) = 功能類型; 
	 * 0=原功能, 1=嵌入功能
	 * @param encodeFuncType
	 * @return
	 */
	public String getFuncTypeByBcryptParamHelper(String encodeFuncType, String locale) {
		String funcType = null;
		try {
			funcType = getBcryptParamHelper().decode(encodeFuncType, "FUNC_TYPE", BcryptFieldValueEnum.SUBITEM_NO, locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return funcType;
	}
	
	private void doExportTsmpFunc(HttpServletResponse response, DPB9927Req req, ReqHeader reqHeader) {
		if(!StringUtils.hasText(req.getFuncType())) {
			throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
		}
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmm");
        String nowDateTime = dateFormatter.format(new Date());
        String funcType = getFuncTypeByBcryptParamHelper(req.getFuncType(), reqHeader.getLocale());
        String fileName = "0".equals(funcType)?"TsmpFunc":"EmbededFunc";
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename="+fileName+"_" + nowDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        //checkmarx, Missing HSTS Header
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
        
	}

	protected TsmpFuncDao getTsmpFuncDao() {
		return tsmpFuncDao;
	}
	
	protected TsmpReportUrlDao getTsmpReportUrlDao() {
		return tsmpReportUrlDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
}
