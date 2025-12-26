package tpi.dgrv4.escape;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9926Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.TsmpDpMailTpltDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9926Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpDpMailTpltDao tsmpDpMailTpltDao;
	private static final long MAX_FILE_SIZE = 1000L * 1024 * 1024;
	private static final int MAX_ROWS_LIMIT = 1000000;
	
	@Autowired
	public DPB9926Service(TsmpDpMailTpltDao tsmpDpMailTpltDao) {
		super();
		this.tsmpDpMailTpltDao = tsmpDpMailTpltDao;
	}

	@Transactional
	public DPB9926Resp importTsmpDpMailTplt(TsmpAuthorization tsmpAuthorization, MultipartFile mFile) {

		try {
			if(mFile == null || mFile.isEmpty() || mFile.getOriginalFilename() == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
			}
			
			//checkmarx, Unchecked Input for Loop Condition
			if(mFile.getSize() > MAX_FILE_SIZE) {
	            throw TsmpDpAaRtnCode._1559.throwing("File size exceeds limit:"+(MAX_FILE_SIZE/1024/1024)+"MB");
	        }
			
			checkParam(mFile.getOriginalFilename());

     		importData(tsmpAuthorization, mFile.getInputStream());
		    
     		return new DPB9926Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		} 
	}
	
	public void importData(TsmpAuthorization tsmpAuthorization, InputStream inputStream) throws Exception {
		try (Workbook workbook = new XSSFWorkbook(inputStream);){
	
			 Sheet sheet = workbook.getSheetAt(0);
			 boolean isFirst = true;
			 DataFormatter formatter = new DataFormatter();
			 Map<String, TsmpDpMailTplt> fileDataList = new HashMap<String, TsmpDpMailTplt>();
		     Map<String, TsmpDpMailTplt> tsmpDpMailTpltMap = new HashMap<String, TsmpDpMailTplt>();
		     //checkmarx, Unchecked Input for Loop Condition
		     int lastRowNum = sheet.getLastRowNum();
		     if (lastRowNum <= 0) {
	             throw TsmpDpAaRtnCode._1559.throwing("Invalid row number: " + lastRowNum);
	         }
	         
	         if (lastRowNum > MAX_ROWS_LIMIT) {
	             throw TsmpDpAaRtnCode._1559.throwing("Row count exceeds limit. Max allowed: " + MAX_ROWS_LIMIT + ", found: " + lastRowNum);
	         }
		     final int safeLastRowNum = Math.min(lastRowNum, MAX_ROWS_LIMIT);
		     for(int rowIndex = 0; rowIndex <= safeLastRowNum; rowIndex++) {
		    	 Row row = sheet.getRow(rowIndex);
		    	 if(row == null) {
		    		 continue;
		    	 }
		    	 if(isFirst) {
		    		 isFirst = false;	    		 
		    		 if(!"CODE".equalsIgnoreCase(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"TEMPLATE_TXT".equalsIgnoreCase(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"REMARK".equalsIgnoreCase(formatter.formatCellValue(row.getCell(2)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    	 }else {
		    		 //空白列不處理
		    		 if(!StringUtils.hasText(formatter.formatCellValue(row.getCell(0))) && 
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(1))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(2)))) {
		    			 continue;
		    		 }
		    		 
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("CODE");
		    		 }
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("TEMPLATE_TXT");
		    		 }
		    		 TsmpDpMailTplt vo = parseRowToEntity(row);
		    		 fileDataList.put(vo.getCode(), vo);
		    	 }
		     }
			
		     
		     List<TsmpDpMailTplt> mailTpltList = getTsmpDpMailTpltDao().findAll();
		     for(TsmpDpMailTplt mailTplt : mailTpltList) {
		    	 tsmpDpMailTpltMap.put(mailTplt.getCode(), mailTplt);
		     }
		     
		     List<TsmpDpMailTplt> changeMailTpltList = new ArrayList<TsmpDpMailTplt>();
		     for (Map.Entry<String, TsmpDpMailTplt> entry : fileDataList.entrySet()) {
		    	String k = entry.getKey();
		    	TsmpDpMailTplt v = entry.getValue();
		    	
	            if(tsmpDpMailTpltMap.containsKey(k)) {
	            	TsmpDpMailTplt mailTplt =  tsmpDpMailTpltMap.get(k);
	            	mailTplt.setTemplateTxt(v.getTemplateTxt());
	            	mailTplt.setRemark(v.getRemark());
	            	mailTplt.setUpdateDateTime(DateTimeUtil.now());
	            	mailTplt.setUpdateUser(tsmpAuthorization.getUserName());
	            	changeMailTpltList.add(mailTplt);
	            }else {
	            	v.setCreateDateTime(DateTimeUtil.now());
	            	v.setCreateUser(tsmpAuthorization.getUserName());
	            	changeMailTpltList.add(v);
	            }
		     }
		     getTsmpDpMailTpltDao().saveAll(changeMailTpltList);
		}
	}
	
	public void checkParam(String fileName) {
		
		int fileNameIndex = fileName.lastIndexOf(".");
		if(fileNameIndex == -1) {
			throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		}
		
		String fileExtension = fileName.substring(fileNameIndex + 1);
		if(!"xlsx".equalsIgnoreCase(fileExtension)) {
			throw TsmpDpAaRtnCode._1443.throwing();
		}
	}
	
	private TsmpDpMailTplt parseRowToEntity(Row row) {
		TsmpDpMailTplt mailTplt = new TsmpDpMailTplt();
		DataFormatter formatter = new DataFormatter();
		mailTplt.setCode(formatter.formatCellValue(row.getCell(0)));
		mailTplt.setTemplateTxt(formatter.formatCellValue(row.getCell(1)));
		mailTplt.setRemark(formatter.formatCellValue(row.getCell(2)));
		return mailTplt;
	}

	protected TsmpDpMailTpltDao getTsmpDpMailTpltDao() {
		return tsmpDpMailTpltDao;
	}
}
