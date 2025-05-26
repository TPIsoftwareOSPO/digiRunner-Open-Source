package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.dpaa.vo.DPB0056Req;
import tpi.dgrv4.dpaa.vo.DPB0056Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0056Service {

	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0056Service(TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpDpFileDao tsmpDpFileDao, FileHelper fileHelper) {
		super();
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}

	public DPB0056Resp queryThemeByPk(TsmpAuthorization authorization, DPB0056Req req) {
		Long themeId = req.getThemeId();
		if (themeId == null) {
			throw TsmpDpAaRtnCode._1208.throwing();
		}

		Optional<TsmpDpThemeCategory> opt = getTsmpDpThemeCategoryDao().findById(themeId);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode._1208.throwing();
		}

		TsmpDpThemeCategory theme = opt.get();

		String dataStatus = theme.getDataStatus();
		String dataStatusName = TsmpDpDataStatus.text(dataStatus);
		String orgId = theme.getOrgId();
		String orgName = getOrgName(orgId);
		TsmpDpFile file = getDpFile(themeId);
		Long fileId = null;
		String fileName = new String();
		String filePath = new String();
		if (file != null) {
			fileId = file.getFileId();
			fileName = file.getFileName();
			filePath = file.getFilePath() + fileName;
		}

		DPB0056Resp resp = new DPB0056Resp();
		resp.setThemeId(theme.getApiThemeId());
		resp.setThemeName(theme.getApiThemeName());
		resp.setDataStatus(dataStatus);
		resp.setDataStatusName(dataStatusName);
		resp.setDataSort(theme.getDataSort());
		resp.setOrgId(orgId);
		resp.setOrgName(orgName);
		resp.setFileId(fileId);
		resp.setFileName(fileName);
		resp.setFilePath(filePath);
		resp.setLv(theme.getVersion());
		return resp;
	}

	private String getOrgName(String orgId) {
		if (StringUtils.isEmpty(orgId)) {
			return new String();
		}
		
		Optional<TsmpOrganization> opt = getTsmpOrganizationDao().findById(orgId);
		if (opt.isPresent()) {
			return opt.get().getOrgName();
		}

		return new String();
	}

	private TsmpDpFile getDpFile(Long refId) {
		List<TsmpDpFile> iconList = getTsmpDpFileDao()//
				.findByRefFileCateCodeAndRefId(TsmpDpFileType.API_TH.value(), refId);

		if (iconList != null && !iconList.isEmpty()) {
			return iconList.get(0);
		}

		return null;
	}

	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
