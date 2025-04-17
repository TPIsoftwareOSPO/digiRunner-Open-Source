package tpi.dgrv4.dpaa.service;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB9917Req;
import tpi.dgrv4.dpaa.vo.DPB9917Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;


@Service
public class DPB9917Service {

    private TsmpDpFileDao tsmpDpFileDao;
	private BcryptParamHelper bcryptParamHelper;
	private FileHelper fileHelper;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB9917Service(TsmpDpFileDao tsmpDpFileDao, BcryptParamHelper bcryptParamHelper, FileHelper fileHelper) {
		super();
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.fileHelper = fileHelper;
	}

	public DPB9917Resp addTsmpDpFile(TsmpAuthorization tsmpAuthorization, DPB9917Req req,ReqHeader reqHeader) {

        try {
        	String locale = ServiceUtil.getLocale(reqHeader.getLocale());
            String fileName = req.getFileName();
            Long refId = req.getRefId();
            String encodeRefFileCateCode = req.getRefFileCateCode();  
            String refFileCateCode = null;  
            String isTmpfile = req.getIsTmpfile();
            String isBLob="N";
            String tmpfileName =req.getTmpfileName();
            String createUser=tsmpAuthorization.getUserName();
            Date createDateTime=DateTimeUtil.now();
                
    		// 確定暫存檔是否存在
			TsmpDpFile tsmpDpFile = new TsmpDpFile();
			if (StringUtils.hasLength(tmpfileName)) {
				tsmpDpFile = getTsmpDpFile(tmpfileName);				
			}

       		//本筆資料是否使用BLOB
    		isBLob=checkIsBlob(tsmpDpFile);
    		//refFileCateCode解密
    		refFileCateCode = decodeRefFileCateCode(encodeRefFileCateCode,locale);
    		//確認是否有重複資料
            checkInformationRepeat(refFileCateCode,encodeRefFileCateCode,locale,fileName,refId);
            
            //新增
            insertTsmpDpFile(tsmpDpFile,fileName,refFileCateCode,refId,isBLob,isTmpfile,createUser,createDateTime);            
           
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1288.throwing();
        }
        
        DPB9917Resp resp = new DPB9917Resp();
        return resp;
    }

	protected String decodeRefFileCateCode(String refFileCateCode,String locale) {
		if (StringUtils.hasLength(refFileCateCode)) {
			try {
				refFileCateCode = getBcryptParamHelper().decode(refFileCateCode, "FILE_CATE_CODE",locale);
			} catch (BcryptParamDecodeException e) {
				throw TsmpDpAaRtnCode._1299.throwing();
			}
		}
		return refFileCateCode;
		
	}
	
	private TsmpDpFile getTsmpDpFile(String tmpfileName){
		List<TsmpDpFile> fileList = getTsmpDpFileDao().findByFileName(tmpfileName);
		if(CollectionUtils.isEmpty(fileList) || fileList.size() > 1) {
			throw TsmpDpAaRtnCode._1203.throwing();
		}
		return fileList.get(0);
	}
	
	private String checkIsBlob(TsmpDpFile tsmpDpFile) {
		
		if (tsmpDpFile.getBlobData() != null && tsmpDpFile.getBlobData().length > 0) {
			return "Y";
		}
		
		return "N";
	}
    
	private void checkInformationRepeat(String refFileCateCode,String encodeRefFileCateCode,String locale,String fileName,Long refId ) {
        List<TsmpDpFile> tsmpDpFileList = getTsmpDpFileDao().findByFileNameAndRefIdAndRefFileCateCode(fileName, refId, refFileCateCode);
        if (!CollectionUtils.isEmpty(tsmpDpFileList)) {
            throw TsmpDpAaRtnCode._1353.throwing(false, "fileName", fileName);
        }
	}
	
	private void insertTsmpDpFile(TsmpDpFile tsmpDpFile,String fileName,String refFileCateCode,Long refId,String isBLob,String isTmpfile,String createUser,Date createDateTime ) {
        tsmpDpFile.setFileName(fileName);
        String filePath=  Paths.get(refFileCateCode, String.valueOf(refId)).toString() + File.separator;
        tsmpDpFile.setFilePath(filePath);
        tsmpDpFile.setRefFileCateCode(refFileCateCode);
        tsmpDpFile.setIsBlob(isBLob);
        if (!StringUtils.hasLength(isTmpfile)) {
			isTmpfile = "N";
		}
        tsmpDpFile.setIsTmpfile(isTmpfile);
        tsmpDpFile.setRefId(refId);
        tsmpDpFile.setCreateUser(createUser);
        tsmpDpFile.setCreateDateTime(createDateTime);
        
        getTsmpDpFileDao().save(tsmpDpFile);
	}

    
    protected TsmpDpFileDao getTsmpDpFileDao() {
        return this.tsmpDpFileDao;
    }
    
    protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}
}
