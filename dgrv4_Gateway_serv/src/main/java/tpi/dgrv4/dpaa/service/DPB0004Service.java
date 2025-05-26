package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.constant.TsmpDpRegStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB0004Client;
import tpi.dgrv4.dpaa.vo.DPB0004File;
import tpi.dgrv4.dpaa.vo.DPB0004Req;
import tpi.dgrv4.dpaa.vo.DPB0004Resp;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.ServiceConfig;

//@Service
public class DPB0004Service {

	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0004Service(TsmpDpClientextDao tsmpDpClientextDao, TsmpDpFileDao tsmpDpFileDao,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0004Resp queryUnReleaseMember(String clientId, DPB0004Req req) {
		List<TsmpDpClientext> unreleasedList = getUnreleasedList(req);
		if (unreleasedList == null || unreleasedList.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_DISAPPROVED_MEMBER.throwing();
		}
		
		DPB0004Resp resp = new DPB0004Resp();
		List<DPB0004Client> dpb0004ClientList = getDpb0004ClientList(unreleasedList);
		resp.setClientList(dpb0004ClientList);
		return resp;
	}

	private List<TsmpDpClientext> getUnreleasedList(DPB0004Req req) {
		List<TsmpDpClientext> unreleasedList = null;

		List<String> regStatusList = new ArrayList<>();
		regStatusList.add(TsmpDpRegStatus.REVIEWING.value());	// 送審
		regStatusList.add(TsmpDpRegStatus.RESUBMIT.value());	// 重新送審
		String lastId = req.getClientId();
		String[] words = getKeywords(req.getKeyword(), " ");
		Integer pageSize = getPageSize();
		unreleasedList = getTsmpDpClientextDao().queryLikeRegStatus(regStatusList//
				, words, lastId, pageSize);

		return unreleasedList;
	}

	private List<DPB0004Client> getDpb0004ClientList(List<TsmpDpClientext> unreleasedList) {
		List<DPB0004Client> dpb0004ClientList = new ArrayList<>();

		String resubmitDatetime;
		String regStatus;
		List<DPB0004File> dpb0004FileList;
		DPB0004Client dpb0004Client;
		for(TsmpDpClientext ext : unreleasedList) {
			dpb0004Client = new DPB0004Client();
			dpb0004Client.setRefClientId(ext.getClientId());
			dpb0004Client.setApplyPurpose(ext.getContentTxt());
			dpb0004Client.setReviewRemark(ext.getReviewRemark());
			dpb0004Client.setRefReviewUser(ext.getRefReviewUser());	// refReviewUser直接就是放userName
			resubmitDatetime = getResubmitDatetime(ext.getResubmitDateTime());
			dpb0004Client.setResubmitDateTime(resubmitDatetime);
			regStatus = ext.getRegStatus();
			regStatus = TsmpDpRegStatus.getText(regStatus);
			dpb0004Client.setRegStatus(regStatus);
			dpb0004FileList = getDpb0004FileList(ext.getClientSeqId());
			dpb0004Client.setFileList(dpb0004FileList);

			dpb0004ClientList.add(dpb0004Client);
		}
		
		return dpb0004ClientList;
	}

	private String getResubmitDatetime(Date resubmitDatetime) {
		if (resubmitDatetime != null) {
			Optional<String> opt = DateTimeUtil.dateTimeToString(resubmitDatetime, //
					DateTimeFormatEnum.西元年月日);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private List<DPB0004File> getDpb0004FileList(Long refId) {
		List<DPB0004File> dpb0004FileList = new ArrayList<>();

		if (refId != null) {
			String refFileCateCode = TsmpDpFileType.MEMBER_APPLY.value();
			List<TsmpDpFile> fileList = getTsmpDpFileDao().findByRefFileCateCodeAndRefId(//
					refFileCateCode, refId);
			if (fileList != null && !fileList.isEmpty()) {
				DPB0004File dpb0004File;
				for(TsmpDpFile tsmpDpFile : fileList) {
					dpb0004File = new DPB0004File();
					dpb0004File.setFileName(tsmpDpFile.getFileName());
					dpb0004File.setFilePath(tsmpDpFile.getFilePath());
					dpb0004File.setFileId(tsmpDpFile.getFileId());
					dpb0004FileList.add(dpb0004File);
				}
			}
		}
		
		return dpb0004FileList;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return this.tsmpDpClientextDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0004");
		return this.pageSize;
	}

}
