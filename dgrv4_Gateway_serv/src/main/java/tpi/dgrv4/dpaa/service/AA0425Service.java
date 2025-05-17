package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0425Req;
import tpi.dgrv4.dpaa.vo.AA0425Resp;
import tpi.dgrv4.dpaa.vo.AA0425RespItem;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0425Service {
	private TPILogger logger = TPILogger.tl;
	
	private ObjectMapper objectMapper;
	private FileHelper fileHelper;
	private TsmpDpFileDao tsmpDpFileDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public AA0425Service(ObjectMapper objectMapper, FileHelper fileHelper, TsmpDpFileDao tsmpDpFileDao,
			ServiceConfig serviceConfig) {
		super();
		this.objectMapper = objectMapper;
		this.fileHelper = fileHelper;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.serviceConfig = serviceConfig;
	}

	public AA0425Resp previewByModifyBatch(TsmpAuthorization authorization, AA0425Req req) {
		AA0425Resp resp = new AA0425Resp();
		try {

			String fileName = req.getTempFileName();
			if (!StringUtils.hasLength(fileName)) {
				throw TsmpDpAaRtnCode._1350.throwing("fileName");
			}
			int sort = req.getSort();
			String refId = req.getRefId();
			if (!StringUtils.hasLength(refId)) {
				throw TsmpDpAaRtnCode._1350.throwing("refId");
			}
			Long refIdL = Long.valueOf(refId);

			// 找檔案
			List<TsmpDpFile> fileList = getTsmpDpFileDao()
					.findByRefFileCateCodeAndRefIdAndFileName(TsmpDpFileType.API_MODIFY_BATCH.code(), refIdL, fileName);
			if (CollectionUtils.isEmpty(fileList)) {
				throw TsmpDpAaRtnCode.NO_FILE.throwing();
			}
			byte[] fileContent = null;
			try {
				fileContent = getFileHelper().download(fileList.get(0));
			} catch (Exception e) {
				this.logger.debug(String.format("File download error: %s", StackTraceUtil.logStackTrace(e)));
			}
			if (fileContent == null || fileContent.length == 0) {
				throw TsmpDpAaRtnCode._1233.throwing();
			}
			List<AA0425RespItem> pagingList = new ArrayList<>();
			List<AA0425RespItem> list = new ArrayList<>();
			try {
				list = getObjectMapper().readValue(fileContent, new TypeReference<List<AA0425RespItem>>() {
				});
			} catch (Exception e) {
				this.logger
						.debug(String.format("Cannot convert json to AA0425Data: %s", StackTraceUtil.logStackTrace(e)));
			}
			for (AA0425RespItem aa0425RespItem : list) {
				if (aa0425RespItem.getSort() > sort && aa0425RespItem.getSort() <= sort + getPageSize()) {
					pagingList.add(aa0425RespItem);

				}
			}
			if (pagingList.size() == 0 || pagingList == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			Collections.sort(pagingList, Comparator.comparingInt(AA0425RespItem::getSort));

			resp.setApiList(pagingList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getDefaultPageSize();
		return pageSize;
	}
}
