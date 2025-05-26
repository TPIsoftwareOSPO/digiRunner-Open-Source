package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0082Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0082Service {

	private TPILogger logger = TPILogger.tl;

	private FileHelper fileHelper;
	
	@Autowired
	public DPB0082Service(FileHelper fileHelper) {
		super();
		this.fileHelper = fileHelper;
	}

	public DPB0082Resp uploadFile2(TsmpAuthorization tsmpAuthorization, String decFileName, byte[] fileContent) {
		DPB0082Resp resp = new DPB0082Resp();
		
		try {
			
			if (StringUtils.isEmpty(decFileName) || //
					fileContent == null || fileContent.length == 0) {
				throw TsmpDpAaRtnCode.FAIL_CREATE_File.throwing();
			}
			
			String tempFileName = null;
			try {
				TsmpDpFile vo = getFileHelper().uploadTemp(tsmpAuthorization.getUserName(), decFileName, fileContent);
				if(vo != null) {
					tempFileName = vo.getFileName();
				}
				//Path path = getFileHelper().uploadTemp(decFileName, fileContent);// 若檔名有重複, 上傳後會自動更名
				//if(path != null) {
					//tempFileName = path.getFileName().toString();
				//}
			} catch (Exception e) {
				throw TsmpDpAaRtnCode.FAIL_CREATE_File.throwing();
			}
			resp.setTempFileName(tempFileName);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}
	
	private byte[] getDecodeVal(String str) {
		if (StringUtils.isEmpty(str)) {
			return null;
		}
		// image base64
		if (str.indexOf(",") != -1) {
			str = str.split(",")[1];
		}
		return ServiceUtil.base64Decode(str);
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}
	
}
