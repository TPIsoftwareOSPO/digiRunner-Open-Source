package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.base64Encode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0014Req;
import tpi.dgrv4.dpaa.vo.DPB0014Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiApp;
import tpi.dgrv4.entity.entity.sql.TsmpDpApp;
import tpi.dgrv4.entity.entity.sql.TsmpDpAppCategory;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpDpApiAppDao;
import tpi.dgrv4.entity.repository.TsmpDpAppCategoryDao;
import tpi.dgrv4.entity.repository.TsmpDpAppDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0014Service {

	private TPILogger logger = TPILogger.tl;;

	private TsmpDpAppDao tsmpDpAppDao;
	private TsmpDpApiAppDao tsmpDpApiAppDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpDpAppCategoryDao tsmpDpAppCategoryDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0014Service(TsmpDpAppDao tsmpDpAppDao, TsmpDpApiAppDao tsmpDpApiAppDao, TsmpDpFileDao tsmpDpFileDao,
			TsmpApiDao tsmpApiDao, TsmpDpAppCategoryDao tsmpDpAppCategoryDao, FileHelper fileHelper) {
		super();
		this.tsmpDpAppDao = tsmpDpAppDao;
		this.tsmpDpApiAppDao = tsmpDpApiAppDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpDpAppCategoryDao = tsmpDpAppCategoryDao;
		this.fileHelper = fileHelper;
	}

	public DPB0014Resp queryAppById(TsmpAuthorization authorization, DPB0014Req req) {
		Long appId = req.getAppId();
		if (appId == null) {
			throw TsmpDpAaRtnCode.NO_APP_DATA.throwing();
		}

		Optional<TsmpDpApp> opt = getTsmpDpAppDao().findById(appId);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode.NO_APP_DATA.throwing();
		}

		TsmpDpApp app = opt.get();
		String appCateName = getAppCateName(app.getRefAppCateId());

		DPB0014Resp resp = new DPB0014Resp();
		resp.setName(app.getName());
		resp.setRefAppCateId(app.getRefAppCateId());
		resp.setRefAppCateName(appCateName);
		resp.setIntro(app.getIntro());
		resp.setAuthor(app.getAuthor());
		resp.setDataStatus(TsmpDpDataStatus.text(app.getDataStatus()));
		List<Map<String, String>> apiUids = getApiUids(app.getAppId());
		resp.setOrgUseApis(apiUids);
		List<Map<String, String>> introFileList = getIntroFileList(app.getAppId());
		resp.setOrgIntroFiles(introFileList);
		setRespOrgIcon(resp, app.getAppId());
		return resp;
	}

	private String getAppCateName(Long appCateId) {
		Optional<TsmpDpAppCategory> opt = getTsmpDpAppCategoryDao().findById(appCateId);
		if (opt.isPresent()) {
			return opt.get().getAppCateName();
		}
		return new String();
	}

	private List<Map<String, String>> getApiUids(Long refAppId) {
		List<TsmpDpApiApp> apiApps = getTsmpDpApiAppDao().findAllByRefAppId(refAppId);
		if (apiApps != null) {
			return apiApps.stream().map((apiApp) -> {
				Map<String, String> map = new HashMap<String, String>();
				String apiName = getApiName(apiApp.getRefApiUid());
				map.put(apiApp.getRefApiUid(), apiName);
				return map;
			}).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private String getApiName(String apiUid) {
		List<TsmpApi> list = getTsmpApiDao().findByApiUid(apiUid);
		if (list != null && !list.isEmpty()) {
			TsmpApi api = list.get(0);
			return api.getApiName();
		}
		return null;
	}

	private void setRespOrgIcon(DPB0014Resp resp, Long refId) {
		List<TsmpDpFile> iconList = getTsmpDpFileDao()//
				.findByRefFileCateCodeAndRefId(TsmpDpFileType.APP_IMAGE.value(), refId);

		if (iconList != null && !iconList.isEmpty()) {
			TsmpDpFile icon = iconList.get(0);
			byte[] iconFile = null;
			try {
				if("Y".equals(icon.getIsBlob())) {
					iconFile = getFileHelper().download(icon);
				}else {
					iconFile = getFileHelper().download01(icon.getFilePath(), icon.getFileName());
				}
				
				if (iconFile != null && iconFile.length > 0) {
					String orgIcon = base64Encode(iconFile);
					resp.setOrgIcon(orgIcon);
					resp.setOrgIconFileName(icon.getFileName());
				}
			} catch (Exception e) {
				this.logger.error(StackTraceUtil.logStackTrace(e));
			}
		}
	}

	private List<Map<String, String>> getIntroFileList(Long refId) {
		List<TsmpDpFile> fileList = getTsmpDpFileDao()//
				.findByRefFileCateCodeAndRefId(TsmpDpFileType.APP_ATTACHMENT.value(), refId);

		if (fileList != null && !fileList.isEmpty()) {
			List<Map<String, String>> introFileList = new ArrayList<>();
			
			for(TsmpDpFile file : fileList) {
				try {
					byte[] data = null;
					if("Y".equals(file.getIsBlob())) {
						data = getFileHelper().download(file);
					}else {
						data = getFileHelper().download01(file.getFilePath(), file.getFileName());
					}
					
					String base64EncodedData = new String();
					if (data != null && data.length > 0) {
						base64EncodedData = base64Encode(data);
					}
					Map<String, String> map = new HashMap<String, String>();
					map.put(file.getFileName(), base64EncodedData);
					introFileList.add(map);
				} catch (Exception e) {
					this.logger.error(StackTraceUtil.logStackTrace(e));
					throw TsmpDpAaRtnCode.NO_APP_DATA.throwing();
				}
			}

			return introFileList;
		}

		return Collections.emptyList();
	}

	protected TsmpDpAppDao getTsmpDpAppDao() {
		return this.tsmpDpAppDao;
	}

	protected TsmpDpApiAppDao getTsmpDpApiAppDao() {
		return this.tsmpDpApiAppDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpDpAppCategoryDao getTsmpDpAppCategoryDao() {
		return this.tsmpDpAppCategoryDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}