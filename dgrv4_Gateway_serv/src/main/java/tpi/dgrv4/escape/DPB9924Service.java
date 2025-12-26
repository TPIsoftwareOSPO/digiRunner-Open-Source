package tpi.dgrv4.escape;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jakarta.transaction.Transactional;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9924Resp;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9924Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpDpItemsDao tsmpDpItemsDao;
	
	private static final long MAX_FILE_SIZE = 1000L * 1024 * 1024;
	
	@Autowired
	public DPB9924Service(TsmpDpItemsDao tsmpDpItemsDao) {
		super();
		this.tsmpDpItemsDao = tsmpDpItemsDao;
	}

	@Transactional
	public DPB9924Resp importTsmpDpItems(TsmpAuthorization tsmpAuthorization, MultipartFile mFile) {

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
		    
     		return new DPB9924Resp();
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
		     Iterator<Row> rows = sheet.iterator();
		     Map<TsmpDpItemsId, TsmpDpItems> fileMap = new HashMap<TsmpDpItemsId, TsmpDpItems>();
		     Map<TsmpDpItemsId, TsmpDpItems> itemsMap = new HashMap<TsmpDpItemsId, TsmpDpItems>();

		     while (rows.hasNext() ) {
		    	 if(isFirst) {
		    		 isFirst = false;
		    		 Row row = rows.next();
		    		 if(!"ITEM_ID".equalsIgnoreCase(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"ITEM_NO".equalsIgnoreCase(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"ITEM_NANE".equalsIgnoreCase(formatter.formatCellValue(row.getCell(2)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"SUBITEM_NO".equalsIgnoreCase(formatter.formatCellValue(row.getCell(3)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"SUBITEM_NAME".equalsIgnoreCase(formatter.formatCellValue(row.getCell(4)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"SORT_BY".equalsIgnoreCase(formatter.formatCellValue(row.getCell(5)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"IS_DEFAULT".equalsIgnoreCase(formatter.formatCellValue(row.getCell(6)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"PARAM1".equalsIgnoreCase(formatter.formatCellValue(row.getCell(7)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"PARAM2".equalsIgnoreCase(formatter.formatCellValue(row.getCell(8)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"PARAM3".equalsIgnoreCase(formatter.formatCellValue(row.getCell(9)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"PARAM4".equalsIgnoreCase(formatter.formatCellValue(row.getCell(10)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"PARAM5".equalsIgnoreCase(formatter.formatCellValue(row.getCell(11)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"LOCALE".equalsIgnoreCase(formatter.formatCellValue(row.getCell(12)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    	 }else {
		    		 Row row = rows.next();
		    		 //空白列不處理
		    		 if(!StringUtils.hasText(formatter.formatCellValue(row.getCell(0))) && 
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(1))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(2))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(3))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(4))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(5))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(6))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(7))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(8))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(9))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(10))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(11))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(12)))) {
		    			 continue;
		    		 }
		    		 
		    		 
		    		 if(!StringUtils.hasText(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("ITEM_ID");
		    		 }
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("ITEM_NO");
		    		 }
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(2)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("ITEM_NANE");
		    		 }
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(3)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("SUBITEM_NO");
		    		 }
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(4)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("SUBITEM_NAME");
		    		 }
		    		 if(!StringUtils.hasText(formatter.formatCellValue(row.getCell(5)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("SORT_BY");
		    		 }
		    		 if(!StringUtils.hasText(formatter.formatCellValue(row.getCell(12)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("LOCALE");
		    		 }
		    		 
		    		 TsmpDpItems vo = parseRowToEntity(row);
		    		 TsmpDpItemsId pk = new TsmpDpItemsId(vo.getItemNo(), vo.getSubitemNo(), vo.getLocale());
		    		 fileMap.put(pk, vo);
		    	 }
		     }
		     
		     
		     List<TsmpDpItems> itemsList = getTsmpDpItemsDao().findAll();
		     for(TsmpDpItems items : itemsList) {
		    	 itemsMap.put(new TsmpDpItemsId(items.getItemNo(), items.getSubitemNo(), items.getLocale()), items);
		     }
		     
		     List<TsmpDpItems> changeItemsList = new ArrayList<TsmpDpItems>();
		     for (Map.Entry<TsmpDpItemsId, TsmpDpItems> entry : fileMap.entrySet()) {
		    	TsmpDpItemsId k = entry.getKey();
		    	TsmpDpItems v = entry.getValue();
		    	
	            if(itemsMap.containsKey(k)) {
	            	TsmpDpItems dt =  itemsMap.get(k);
	            	dt.setItemId(v.getItemId());
	            	dt.setItemName(v.getItemName());
	            	dt.setSubitemName(v.getSubitemName());
	            	dt.setSortBy(v.getSortBy());
	            	dt.setIsDefault(v.getIsDefault());
	            	dt.setParam1(v.getParam1());
	            	dt.setParam2(v.getParam2());
	            	dt.setParam3(v.getParam3());
	            	dt.setParam4(v.getParam4());
	            	dt.setParam5(v.getParam5());
	            	dt.setUpdateDateTime(DateTimeUtil.now());
	            	dt.setUpdateUser(tsmpAuthorization.getUserName());
	            	changeItemsList.add(dt);
	            }else {
	            	v.setCreateDateTime(DateTimeUtil.now());
	            	v.setCreateUser(tsmpAuthorization.getUserName());
	            	changeItemsList.add(v);
	            }
		     }
		     getTsmpDpItemsDao().saveAll(changeItemsList);
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
	
	private TsmpDpItems parseRowToEntity(Row row) {
		TsmpDpItems tdt = new TsmpDpItems();
		DataFormatter formatter = new DataFormatter();
		tdt.setItemId(Long.parseLong(formatter.formatCellValue(row.getCell(0))));
		tdt.setItemNo(formatter.formatCellValue(row.getCell(1)));
		tdt.setItemName(formatter.formatCellValue(row.getCell(2)));
		tdt.setSubitemNo(formatter.formatCellValue(row.getCell(3)));
		tdt.setSubitemName(formatter.formatCellValue(row.getCell(4)));
		tdt.setSortBy(Integer.parseInt(formatter.formatCellValue(row.getCell(5))));
		tdt.setIsDefault(formatter.formatCellValue(row.getCell(6)));
		tdt.setParam1(formatter.formatCellValue(row.getCell(7)));
		tdt.setParam2(formatter.formatCellValue(row.getCell(8)));
		tdt.setParam3(formatter.formatCellValue(row.getCell(9)));
		tdt.setParam4(formatter.formatCellValue(row.getCell(10)));
		tdt.setParam5(formatter.formatCellValue(row.getCell(11)));
		tdt.setLocale(formatter.formatCellValue(row.getCell(12)));
		return tdt;
	}

	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return tsmpDpItemsDao;
	}
}
