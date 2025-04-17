package tpi.dgrv4.dpaa.service;						
						
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.DpOpenApiDocServiceIfs;
import tpi.dgrv4.dpaa.component.DpOpenApiDocServiceImplOAS2;
import tpi.dgrv4.dpaa.component.DpOpenApiDocServiceImpl_OAS2_DGRC;
import tpi.dgrv4.dpaa.component.DpOpenApiDocServiceImpl_OAS3;
import tpi.dgrv4.dpaa.component.DpOpenApiDocServiceImpl_OAS3_DGRC;
import tpi.dgrv4.dpaa.vo.AA0315Req;
import tpi.dgrv4.dpaa.vo.AA0315Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;						
						
@Service						
public class AA0315Service {						
						
	private TPILogger logger = TPILogger.tl;
	
	private TsmpDpFileDao tsmpDpFileDao;					
	private FileHelper fileHelper; 
	private TsmpSettingService tsmpSettingService;
	
	@Autowired
	public AA0315Service(TsmpDpFileDao tsmpDpFileDao, FileHelper fileHelper, TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
		this.tsmpSettingService = tsmpSettingService;
	}

	/*
	 * 利用 FileHelper.download(TsmpDpFile) 方法將 blob_data 取出轉換成 byte[]
	 * 每種文件格式包含的節點資訊都不同，請參考 這裡 擷取出 Response 所需的資訊，處理過後填入對應的 Response 欄位。
	 * (可參考 digiRunner v2 - AA0315 的解析方式)
	 * ※ 注意：須同時支援 OAS 2.0 及 OAS 3.0 兩種文件架構的解析	
	 */
	public AA0315Resp uploadOpenApiDoc(TsmpAuthorization auth, AA0315Req req) {					
		AA0315Resp resp = new AA0315Resp();				
		try {				
			// chk param 
			TsmpDpFile tsmpDpFile = checkParam(auth, req);
			
			 //如果是 json，可使用 com.fasterxml.jackson.databind.ObjectMapper 讀取 byte[] 文檔內容，以操作 JsonNode 的方式進行解析
			 //(如果是 yaml 檔則可用 new ObjectMapper(new YAMLFactory()) 方式讀取)	
			String tempFileName = req.getTempFileName();
			String extFileName = geExtFileName(tempFileName);
			ObjectMapper objMapper = new ObjectMapper();
			if ("json".equalsIgnoreCase(extFileName)) {
				objMapper = new ObjectMapper();
			} else if ("yml".equalsIgnoreCase(extFileName) 
					|| "yaml".equalsIgnoreCase(extFileName)) {
				objMapper = new ObjectMapper(new YAMLFactory());
			}
			
			byte[] byteData = getFileHelper().download(tsmpDpFile);
			String fileData = new String(byteData);
			JsonNode rootNode = objMapper.readTree(fileData);

			Integer routingMode = null ;
			
			if (req.getType() == null) {
				routingMode = getTsmpSettingService().getVal_DGR_PATHS_COMPATIBILITY();
			}else {
				routingMode = req.getType();
			}

			if (!(routingMode.intValue() == 0 || routingMode.intValue() == 1)) {
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			
			/*
			 * 先判斷出上傳的文件屬於哪種規格
			 * 如果上傳的副檔名是 json 或 yml，表示上傳的是 OAS 規格 (2.0 或 3.0)	
			 * 如果 json 或 yml 的根節點下有出現"swagger"子節點，且值為"2.0"，表示文件規格符合 OAS 2.0；如果有出現"openapi"子節點，則表示符合 OAS 3.0
			 */
			DpOpenApiDocServiceIfs ifs = null;
			if (rootNode.has("swagger")) {
				String swagger = rootNode.get("swagger").asText();
				if("2.0".equals(swagger)) {
					//文件規格為 OAS 2.0
					if (routingMode.intValue()==0) {
						//0為tsmpc
						ifs = new DpOpenApiDocServiceImplOAS2();
					}else if (routingMode.intValue()==1) {
						//1為dgrc
						ifs = new DpOpenApiDocServiceImpl_OAS2_DGRC();
					}					
				}else {
					throw TsmpDpAaRtnCode._1296.throwing();
				}
			}else if(rootNode.has("openapi")) {
				//文件規格為 OAS 3.0
				if (routingMode.intValue()==0) {
					//0為tsmpc
					ifs = new DpOpenApiDocServiceImpl_OAS3();
				}else if (routingMode.intValue()==1) {
					//1為dgrc
					ifs = new DpOpenApiDocServiceImpl_OAS3_DGRC();
				}
			}
			if(ifs != null) {
				resp = ifs.parseData(req, resp, fileData, extFileName);
				if (req.getType()==null) {
					resp.setType(routingMode);
				}else {
					resp.setType(req.getType());	
				}
			}
		} catch (TsmpDpAaException e) {				
			throw e;			
		} catch (Exception e) {				
			//5. 其餘解析時發生的錯誤，皆 throw 1291，並以 log.debug 詳細紀錄之。
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1291.throwing();			
		}				
		return resp;				
	}		
	
	private TsmpDpFile checkParam(TsmpAuthorization auth, AA0315Req req) throws SQLException {
		String tempFileName = req.getTempFileName();
		//若未傳入 tempFileName 則 throw 1296。
		if(StringUtils.isEmpty(tempFileName)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		//若 TSMP_DP_FILE.file_name 之副檔名不是 "json"、"yml"或"yaml"，則 throw 1291	
		String extFileName = geExtFileName(tempFileName);
		if(!("json").equalsIgnoreCase(extFileName) 
				&& !("yml").equalsIgnoreCase(extFileName) 
				&& !("yaml").equalsIgnoreCase(extFileName)) {
			throw TsmpDpAaRtnCode._1291.throwing();
		}
		
		//若傳入的 tempFileName 不存在於 TSMP_DP_FILE 中，則 throw 1129。
		//TsmpDpFileDao.findByRefFileCateCodeAndRefIdAndFileName(TsmpDpFileType.TEMP, 1L, tempSwaggerFileName)	
		List<TsmpDpFile> fileList = getTsmpDpFileDao().findByRefFileCateCodeAndRefIdAndFileName(TsmpDpFileType.TEMP.value(), 
				-1L, tempFileName);
		if (fileList == null || fileList.isEmpty()) {			
			throw TsmpDpAaRtnCode.NO_FILE.throwing();		
		}			
		
		//若查詢到的 TSMP_DP_FILE.blob_data 是空值，則 throw 1233。	
		TsmpDpFile tsmpDpFile = fileList.get(0);
		byte[] blobData = tsmpDpFile.getBlobData();
		if(blobData == null || blobData.length == 0) {
			throw TsmpDpAaRtnCode._1233.throwing();
		}

		return tsmpDpFile;
	}
	
	/**
	 * 取得副檔名
	 * @return
	 */
	private String geExtFileName(String tempFileName) {
		String extFileName = "";
		int index = tempFileName.lastIndexOf(".");
		if(index > -1) {
			extFileName = tempFileName.substring(index + 1);
		}
		return extFileName;
	}
	
	protected TsmpDpFileDao getTsmpDpFileDao() {					
		return tsmpDpFileDao;				
	}
	
	protected FileHelper getFileHelper() {					
		return fileHelper;				
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	
}						
						