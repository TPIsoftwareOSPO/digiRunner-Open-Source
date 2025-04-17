package tpi.dgrv4.dpaa.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0057Req;
import tpi.dgrv4.dpaa.vo.DPB0057Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0057Service {
	
	private TPILogger logger = TPILogger.tl;

	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0057Service(TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao, TsmpDpFileDao tsmpDpFileDao,
			TsmpOrganizationDao tsmpOrganizationDao, FileHelper fileHelper) {
		super();
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB0057Resp deleteTheme(TsmpAuthorization authorization, DPB0057Req req) {
		List<Long> themeIds = req.getDelList();
		String orgId = authorization.getOrgId();
		
		if(StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (themeIds == null || themeIds.isEmpty()) {
			 throw TsmpDpAaRtnCode._1209.throwing();
		}
		
		//找出包含此 orgId 及其向下的所有組織
		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
		
		//檢查所選主題組織是否都在本組織以下,否則不能更新
		checkThemeOrg(themeIds, orgDescList);
		
		Map<Long, String> resultMap = new LinkedHashMap<>();
		Map<Long, String> resultFileMap = new LinkedHashMap<>();
		
		String delErrMsg = null;
		for(Long themeId : themeIds) {
			delFiles(themeId, resultFileMap);

			delErrMsg = del(themeId);

			if (StringUtils.isEmpty(delErrMsg)) {
				resultMap.put(themeId, "Ok");
			} else {
				resultMap.put(themeId, "Fail");
			}
		}

		DPB0057Resp resp = new DPB0057Resp();
		resp.setResultMap(resultMap);
		resp.setResultFileMap(resultFileMap);
		return resp;
	}
	
	/**
	 * 檢查若所選的主題有部份不屬於組織,則丟出刪除失敗
	 * 
	 * @param apiList
	 * @param orgDescList
	 */
	public void checkThemeOrg(List<Long> themeIds, List<String> orgDescList) {
		boolean isOrgErr = false;
		for (Long themeId : themeIds) {
			Optional<TsmpDpThemeCategory> opt_theme = getTsmpDpThemeCategoryDao().findById(themeId);
			TsmpDpThemeCategory theme = null;
			if(opt_theme.isPresent()) {
				theme = opt_theme.get();
				String orgId = theme.getOrgId();
				if (!StringUtils.isEmpty(orgId)) {
					if ( CollectionUtils.isEmpty(orgDescList) || !orgDescList.contains(orgId) ) {
						isOrgErr = true;
						break;
					}
				}
			}
		}
		if(isOrgErr) {
			throw TsmpDpAaRtnCode._1221.throwing();
		}
	}
	

	private void delFiles(Long refId, Map<Long, String> resultFileMap) {
		List<TsmpDpFile> files = getTsmpDpFileDao() //
			.findByRefFileCateCodeAndRefId(TsmpDpFileType.API_TH.value(), refId);
		if (files == null || files.isEmpty()) {
			return;
		}

		boolean delError = false;
		for(TsmpDpFile file : files) {
			try {
				// 實體檔案如果不存在，當作刪除失敗
				boolean isSuccess = false;
				
				if("Y".equals(file.getIsBlob()) && file.getBlobData() != null) {
					getTsmpDpFileDao().delete(file);
					isSuccess = true;
				}else {
					isSuccess = getFileHelper() //
							.remove01(file.getFilePath(), file.getFileName(), (filename) -> {
						getTsmpDpFileDao().delete(file);
					});	
				}
				
				resultFileMap.put(file.getFileId(), (isSuccess?"Ok":"Fail"));

				if (!isSuccess) {
					delError = true;
				}
			} catch (Exception e) {
				delError = true;
				resultFileMap.put(file.getFileId(), "Fail");
			}
		}

		if (!delError) {
			// 把資料夾一起刪除
			String filePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.API_TH, refId);
			try {
				getFileHelper().remove01(filePath, null, null);
			} catch (Exception e) {
				// Do nothing...
				this.logger.error(StackTraceUtil.logStackTrace(e));
			}
		}
	}

	private String del(Long themeId) {
		if (themeId == null) {
			return "id為空值";
		}
		
		Optional<TsmpDpThemeCategory> opt = getTsmpDpThemeCategoryDao().findById(themeId);
		if (!opt.isPresent()) {
			return "id不存在";
		}
		
		TsmpDpThemeCategory theme = opt.get();

		getTsmpDpThemeCategoryDao().delete(theme);

		boolean isExists = getTsmpDpThemeCategoryDao().existsById(themeId);
		if (isExists) {
			return "刪除失敗";
		}

		return null;
	}

	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
}
