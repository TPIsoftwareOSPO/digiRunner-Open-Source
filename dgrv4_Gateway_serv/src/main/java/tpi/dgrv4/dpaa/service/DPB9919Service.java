package tpi.dgrv4.dpaa.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9919Item;
import tpi.dgrv4.dpaa.vo.DPB9919Req;
import tpi.dgrv4.dpaa.vo.DPB9919Resp;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9919Service {

	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB9919Service(TsmpDpFileDao tsmpDpFileDao, FileHelper fileHelper) {
		super();
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}

	@Transactional
	public DPB9919Resp removeAndRestoreTsmpDpFile(TsmpAuthorization tsmpAuthorization, DPB9919Req req) {

		try {

			List<DPB9919Item> fileIdList = req.getFileList();
			String newIsTmpfile = req.getNewIsTmpfile();
			String updateUser = tsmpAuthorization.getUserName();
			Date updateDateTime = DateTimeUtil.now();

			// 若缺少必填參數則拋出 1296 錯誤。
			if (CollectionUtils.isEmpty(fileIdList) || !StringUtils.hasLength(newIsTmpfile)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			Long count = fileIdList.stream().filter(o -> o.getFileId() == null || o.getOriIsTmpFile() == null).count();
			if (count > 0) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}

			// 確認newIsTmpfile格式
			checkPattern(newIsTmpfile);

			// 確認item是否格式正確或都有值
			checkItemParameter(fileIdList);

			// 確定資料是否存在然後更新
			checkInformationStateAndUpdateTsmpDpFile(fileIdList, updateUser, updateDateTime, newIsTmpfile);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}

		DPB9919Resp resp = new DPB9919Resp();
		return resp;
	}

	private void checkItemParameter(List<DPB9919Item> fileIdList) {
		Long fileId;
		String oriIsTmpFile;
		for (DPB9919Item item : fileIdList) {
			fileId = item.getFileId();
			oriIsTmpFile = item.getOriIsTmpFile();

			if (fileId == null || !StringUtils.hasLength(oriIsTmpFile)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}

			checkPattern(oriIsTmpFile);
		}

	}

	private void checkPattern(String value) {
		String pattern = "^[Y|N]";
		boolean isMatch = Pattern.matches(pattern, value);
		if (!isMatch) {
			throw TsmpDpAaRtnCode._1290.throwing();
		}
	}

	private void checkInformationStateAndUpdateTsmpDpFile(List<DPB9919Item> fileIdList, String updateUser,
			Date updateDateTime, String newIsTmpfile) throws SQLException {

		String reqIsTmpfile = null;
		String dbIsTmpfile = null;
		for (DPB9919Item item : fileIdList) {
			reqIsTmpfile = item.getOriIsTmpFile();

			Optional<TsmpDpFile> opt = getTsmpDpFileDao().findById(item.getFileId());
			if (!opt.isPresent()) {
				logger.error(item.getFileId() + " check db is null");
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			TsmpDpFile tsmpDpFile = opt.get();
			dbIsTmpfile = tsmpDpFile.getIsTmpfile();
			boolean a = ("N".equals(reqIsTmpfile) && (dbIsTmpfile == null || "N".equals(dbIsTmpfile)));
			boolean b = reqIsTmpfile.equals(dbIsTmpfile);
			boolean isDataNotExists = !(a || b);
			if (isDataNotExists) {
				logger.error(item.getFileId() + " db is modified");
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			updateTsmpDpFile(updateUser, updateDateTime, tsmpDpFile, newIsTmpfile);
		}
	}

	private void updateTsmpDpFile(String updateUser, Date updateDateTime, TsmpDpFile tsmpDpFile, String newIsTmpfile)
			throws SQLException {

		byte[] bytes = getFileHelper().download(tsmpDpFile);

		tsmpDpFile.setIsTmpfile(newIsTmpfile);
		tsmpDpFile.setUpdateUser(updateUser);
		tsmpDpFile.setUpdateDateTime(updateDateTime);
		tsmpDpFile.setBlobData(bytes);

		getTsmpDpFileDao().save(tsmpDpFile);

	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
