package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.constant.AnalyzeClientRelatedDataStatus;
import tpi.dgrv4.dpaa.vo.AA1121Resp;
import tpi.dgrv4.dpaa.vo.AA1128Req;
import tpi.dgrv4.dpaa.vo.AA1128Resp;
import tpi.dgrv4.entity.entity.DgrImportClientRelatedTemp;
import tpi.dgrv4.entity.repository.DgrImportClientRelatedTempDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1128Service {
	private TPILogger logger = TPILogger.tl;

	private ObjectMapper objectMapper;
	private DgrImportClientRelatedTempDao dgrImportClientRelatedTempDao;
	
	@Autowired
	public AA1128Service(ObjectMapper objectMapper, DgrImportClientRelatedTempDao dgrImportClientRelatedTempDao) {
		super();
		this.objectMapper = objectMapper;
		this.dgrImportClientRelatedTempDao = dgrImportClientRelatedTempDao;
	}

	public AA1128Resp importClientRelatedAllCover(TsmpAuthorization tsmpAuthorization, AA1128Req req) {

		try {

			checkParam(req);
			DgrImportClientRelatedTemp tempVo = getDgrImportClientRelatedTempDao().findById(Long.valueOf(req.getLongId())).orElse(null);
			if(tempVo == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			//將分析資料的狀態是的R改成C
			byte[] arrAnalyze = tempVo.getAnalyzeClientRelated();
			AA1121Resp analyzeVo = getObjectMapper().readValue(arrAnalyze, AA1121Resp.class);
			analyzeVo.getClientList().forEach(vo->{
				if(AnalyzeClientRelatedDataStatus.R.name().equals(vo.getDataStatus())){
					vo.setDataStatus(AnalyzeClientRelatedDataStatus.C.name());
				}
			});
			analyzeVo.getGroupAuthList().forEach(vo->{
				if(AnalyzeClientRelatedDataStatus.R.name().equals(vo.getDataStatus())){
					vo.setDataStatus(AnalyzeClientRelatedDataStatus.C.name());
				}
			});
			analyzeVo.getGroupList().forEach(vo->{
				if(AnalyzeClientRelatedDataStatus.R.name().equals(vo.getDataStatus())){
					vo.setDataStatus(AnalyzeClientRelatedDataStatus.C.name());
				}
			});
			analyzeVo.getSecurityLevelList().forEach(vo->{
				if(AnalyzeClientRelatedDataStatus.R.name().equals(vo.getDataStatus())){
					vo.setDataStatus(AnalyzeClientRelatedDataStatus.C.name());
				}
			});
			analyzeVo.getVgroupList().forEach(vo->{
				if(AnalyzeClientRelatedDataStatus.R.name().equals(vo.getDataStatus())){
					vo.setDataStatus(AnalyzeClientRelatedDataStatus.C.name());
				}
			});
			analyzeVo.getRdbConnectionList().forEach(vo->{
				if(AnalyzeClientRelatedDataStatus.R.name().equals(vo.getDataStatus())){
					vo.setDataStatus(AnalyzeClientRelatedDataStatus.C.name());
				}
			});
			
			//存資料
			arrAnalyze = getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(analyzeVo);
			tempVo.setAnalyzeClientRelated(arrAnalyze);
			tempVo.setUpdateUser(tsmpAuthorization.getUserName());
			tempVo.setUpdateDateTime(DateTimeUtil.now());
			getDgrImportClientRelatedTempDao().save(tempVo);
			
     		return new AA1128Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		} 
	}
	
	private void checkParam(AA1128Req req) {
		if(!StringUtils.hasText(req.getLongId())) {
			throw TsmpDpAaRtnCode._1350.throwing("{{longId}}");
		}
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected DgrImportClientRelatedTempDao getDgrImportClientRelatedTempDao() {
		return dgrImportClientRelatedTempDao;
	}


}
