package tpi.dgrv4.gateway.component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sql.rowset.serial.SerialException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.CheckmarxCommUtils;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredTempFileJob;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredTempFileTsmpDpFileJob;
import tpi.dgrv4.entity.component.IFileHelper;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class FileHelper implements IFileHelper{
	
	private TPILogger logger;

	private final String UPLOAD_PREFIX;
	private final String UPLOAD_TEMP;
	private final Path TSMP_DP_AA_UPLOAD_PATH;
	private final Path TSMP_DP_AA_UPLOAD_TEMP;

	private JobHelper jobHelper;
	private ApplicationContext ctx;
	private TsmpDpFileDao tsmpDpFileDao;


	public FileHelper(
		@Value("") String uploadPrefix,
		TPILogger logger
	) {
		this(uploadPrefix, null, logger);
	}

	@Autowired
	public FileHelper(
		@Value("") String uploadPrefix,
		@Value("") String uploadTemp,
		TPILogger logger
	) {
		this.logger = logger;
		this.UPLOAD_PREFIX = (uploadPrefix == null ? "" : filterPath(uploadPrefix, false));
		this.UPLOAD_TEMP = (uploadTemp == null ? "" : filterPath(uploadTemp, false));
		this.TSMP_DP_AA_UPLOAD_PATH = resolveUploadPath(uploadPrefix);
		this.TSMP_DP_AA_UPLOAD_TEMP = resolveUploadTemp(this.TSMP_DP_AA_UPLOAD_PATH, uploadTemp);
	}
	
	@Autowired
	public void setFileHelper(JobHelper jobHelper, ApplicationContext ctx, TsmpDpFileDao tsmpDpFileDao) {
		this.jobHelper = jobHelper;
		this.ctx = ctx;
		this.tsmpDpFileDao = tsmpDpFileDao;
	}

	private final Path resolveUploadPath(String uploadPrefix) {
		Path path = null;

		try {
			/* 不再需要根目錄
			final URI root = getClassPathRoot();
			path = Paths.get(root);
			*/

			uploadPrefix = (uploadPrefix == null ? "" : filterPath(uploadPrefix, true));
			path = Paths.get(uploadPrefix);

		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		} finally {
			path = path == null ? Paths.get("") : path;
			this.logger.info("Initialize upload path: " + path.toAbsolutePath());
		}

		return path;
	}

	private final Path resolveUploadTemp(Path uploadPath, String uploadTemp) {
		Path path = null;

		try {
			if (uploadTemp == null) {
				uploadTemp = new String();
			}
			uploadTemp = filterPath(uploadTemp, true);

			if (uploadPath == null) {
				/* 不再需要根目錄
				final URI root = getClassPathRoot();
				uploadPath = Paths.get(root);
				 */
				path = Paths.get(uploadTemp);
			} else {
				path = uploadPath.resolve(uploadTemp);
			}
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		} finally {
			path = path == null ? Paths.get("") : path;
			this.logger.info("Initialize upload temp path: " + path.toAbsolutePath());
		}

		return path;
	}

	@SuppressWarnings("unused")
	private final URI getClassPathRoot() throws IOException {
		//final URI uri = getClass().getResource("/").toURI();
		final URI uri = new ClassPathResource("").getURI();
		
		this.logger.debugDelay2sec("Resource URI: " + uri);
		this.logger.debugDelay2sec("Resource path: " + Paths.get(uri));

		return uri;
	}

	/**
	 * 替換分隔符為系統預設
	 * @param path
	 * @param isTruncateLeadingSeperator
	 * @return
	 */
	public static final String filterPath(String path, boolean isTruncateLeadingSeperator) {
		if (path != null) {
			path = path.replaceAll("(\\\\+|/+)", Matcher.quoteReplacement(File.separator));
			if (isTruncateLeadingSeperator) {
				path = path.replaceFirst("^" + Matcher.quoteReplacement(File.separator), "");
			}
		}
		return path;
	}

	/**
	 * 組出符合 tsmp_dp_file.file_path 的路徑
	 * @param fileType
	 * @param refId
	 * @return
	 */
	public static final String getTsmpDpFilePath(TsmpDpFileType fileType, Long refId) {
		return Paths.get(fileType.value(), String.valueOf(refId)).toString() + File.separator;
	}

	/**
	 * 檢查該路徑下是否有重複的檔名,<br/>
	 * 若有重複則修改檔名<br/>
	 * ex: "檔名_yyyyMMddHHmmssSSS.txt"
	 * @param file
	 * @return
	 */
	public static final Path renameIfDuplicate(Path file) {
		if (Files.isRegularFile(file) && Files.exists(file)) {
			final String timestamp = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日時分秒毫秒_4).orElse(null);
			final String[] nameInfos = file.getFileName().toString().split("\\.");
			final String newFileName = nameInfos[0] + "_" + timestamp + (nameInfos.length > 1 ? "." + nameInfos[1] : "");
			file = file.resolveSibling(newFileName);
		}

		return file;
	}

	public String renameIfDuplicate(String refFileCateCode,Long refId, String fileName) {
		List<TsmpDpFile> list = getTsmpDpFileDao().findByRefFileCateCodeAndRefIdAndFileName(refFileCateCode, refId, fileName);
		if(list != null && list.size() > 0) {
			final String timestamp = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日時分秒毫秒_4).orElse(null);
			final String[] nameInfos = fileName.split("\\.");
			final String newFileName = nameInfos[0] + "_" + timestamp + (nameInfos.length > 1 ? "." + nameInfos[1] : "");
			return newFileName; 
		} else {
			return fileName;
		}
	}

	public final TsmpDpFile upload(String userName,TsmpDpFileType fileType, Long refId//
			, String filename, byte[] content, String isTmpfile) throws SerialException, SQLException {
        return upload(userName, fileType, refId
    			, filename, content, isTmpfile, false);
	}
	
	public final TsmpDpFile upload(String userName,TsmpDpFileType fileType, Long refId//
			, String filename, byte[] content, String isTmpfile, boolean isSaveFlush) throws SerialException, SQLException {
		
		String tsmpDpFilePath = getTsmpDpFilePath(fileType, refId);
		String newFileName = renameIfDuplicate(fileType.value(), refId, filename);
		
		TsmpDpFile fileVo = new TsmpDpFile();
		fileVo.setIsTmpfile(isTmpfile);
		fileVo.setIsBlob("Y");
		fileVo.setRefFileCateCode(fileType.value());
        fileVo.setRefId(refId);
        fileVo.setFilePath(tsmpDpFilePath);
        fileVo.setFileName(newFileName);
        fileVo.setBlobData(content);
        fileVo.setCreateUser(userName);	
        
        if(isSaveFlush) {
        	fileVo = getTsmpDpFileDao().saveAndFlush(fileVo);
        }else {
        	fileVo = getTsmpDpFileDao().save(fileVo);
        }
        
        return fileVo;
	}

	public final Path upload01(TsmpDpFileType fileType, Long refId//
			, String filename, byte[] content) throws IOException {
		// 創建上傳路徑
		String tsmpDpFilePath = getTsmpDpFilePath(fileType, refId);
		return upload01(tsmpDpFilePath, filename, content);
	}

	public final Path upload01(String tsmpDpFilePath, String filename, byte[] content) throws IOException {
		if (TSMP_DP_AA_UPLOAD_PATH == null ||
			filename == null || filename.isEmpty() || //
			content == null || content.length <= 0) {
			return null;
		}
		
		Path uploadFilePath = null;

		Path uploadFolder = TSMP_DP_AA_UPLOAD_PATH.resolve(tsmpDpFilePath);
		if (!Files.exists(uploadFolder)) {
			uploadFolder = Files.createDirectories(uploadFolder);
			this.logger.debug("Create upload paths: " + uploadFolder.toAbsolutePath());
		}

		if (Files.exists(uploadFolder)) {
			Path tempFilePath = uploadFolder.resolve(filename);
			tempFilePath = renameIfDuplicate(tempFilePath);
			this.logger.info("Uploading file to: " + tempFilePath.toAbsolutePath());
			uploadFilePath = Files.write(tempFilePath, content);
		}

		return uploadFilePath;
	}

	public final byte[] download01(TsmpDpFileType fileType, Long refId, String filename) throws IOException {
		String tsmpDpFilePath = getTsmpDpFilePath(fileType, refId);
		return download01(tsmpDpFilePath, filename);
	}
	
	public final byte[] downloadByTsmpDpFile(TsmpDpFileType fileType, Long refId, String fileName) throws SQLException {
		if(fileName == null) {
			return null;
		}
		
		List<TsmpDpFile> dpFileList = getTsmpDpFileDao().findByRefFileCateCodeAndRefIdAndFileName(fileType.value(), refId, fileName);
		if(dpFileList != null && dpFileList.size() > 0) {
			return download(dpFileList.get(0));
		}else {
			return null;
		}
	}

	public final byte[] download01(String tsmpDpFilePath, String filename) throws IOException {
		if (TSMP_DP_AA_UPLOAD_PATH == null ||
			filename == null || filename.isEmpty()) {
			return null;
		}

		Path uploadFolder = TSMP_DP_AA_UPLOAD_PATH.resolve(tsmpDpFilePath);
		Path targetFilePath = uploadFolder.resolve(filename);
		if (Files.exists(targetFilePath)) {
			this.logger.info("Downloading file from: " + targetFilePath.toAbsolutePath());
			//checkmarx, Absolute Path Traversal, Stored Absolute Path Traversal
			return CheckmarxCommUtils.sanitizeForCheckmarx(targetFilePath);
		}

		return null;
	}

	public final byte[] downloadByPathAndName(String tsmpDpFilePath, String filename) throws SQLException {
		List<TsmpDpFile> list = getTsmpDpFileDao().findByFilePathAndFileName(tsmpDpFilePath, filename);
		if(list.size() > 0) {
			return download(list.get(0));
		}else {
			return null;
		}
	}
	
	public final byte[] download(TsmpDpFile dpfile) throws SQLException {
		if(dpfile !=null && dpfile.getBlobData() != null) {
			byte[] blobData = dpfile.getBlobData();
			return Arrays.copyOf(blobData, blobData.length);
		}else {
			return null;
		}
	}

	public final boolean remove01(TsmpDpFileType fileType, Long refId, //
			String filename, Consumer<String> callback) throws Exception {
		String tsmpDpFilePath = getTsmpDpFilePath(fileType, refId);
		return remove01(tsmpDpFilePath, filename, callback);
	}

	/** 若未指定 filename, 則將 tsmpDpFilePath 下的檔案及該資料夾刪除 */
	public final boolean remove01(String tsmpDpFilePath, String filename //
			, Consumer<String> successCallback) throws Exception {
		if (TSMP_DP_AA_UPLOAD_PATH == null) {
			return false;
		}

		Path uploadFolder = TSMP_DP_AA_UPLOAD_PATH.resolve(tsmpDpFilePath);

		if (filename == null || filename.isEmpty()) {
			if (Files.notExists(uploadFolder)) {
				return true;
			}
			
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadFolder)) {
				Path file;
				boolean isSuccess;
				Iterator<Path> iterator = stream.iterator();
				while(iterator.hasNext()) {
					file = iterator.next();
					isSuccess = Files.deleteIfExists(file);
					this.logger.info("Removing file from: " + file.toAbsolutePath());
					if (isSuccess && successCallback != null) {
						successCallback.accept(file.getFileName().toString());
					}
				}

				if (!iterator.hasNext()) {
					this.logger.info("Removing directory: " + uploadFolder.toAbsolutePath());
					return Files.deleteIfExists(uploadFolder);
				}

				return false;
	       }
		} else {
			//checkmarx, Absolute Path Traversal, 已通過中風險
			filename = filename.replaceAll("\\.\\.", "").replaceAll("/", "").replaceAll("\\\\", "");
			
			Path targetFilePath = uploadFolder.resolve(filename);
			this.logger.info("Removing file from: " + targetFilePath.toAbsolutePath());
			boolean isSuccess = Files.deleteIfExists(targetFilePath);
			if (isSuccess && successCallback != null) {
				successCallback.accept(targetFilePath.getFileName().toString());
			}
			return isSuccess;
		}
	}

	public final boolean exists(TsmpDpFileType fileType, Long refId, String filename) {
		String tsmpDpFilePath = getTsmpDpFilePath(fileType, refId);
		return exists(tsmpDpFilePath, filename);
	}

	public final boolean exists(String tsmpDpFilePath, String filename) {
		if (TSMP_DP_AA_UPLOAD_PATH == null ||
			tsmpDpFilePath == null || tsmpDpFilePath.isEmpty()) {
			return false;
		}

		Path uploadFolder = TSMP_DP_AA_UPLOAD_PATH.resolve(tsmpDpFilePath);
		if (filename == null || filename.isEmpty()) {
			return Files.exists(uploadFolder);
		} else {
			Path targetFilePath = uploadFolder.resolve(filename);
			return Files.exists(targetFilePath);
		}
	}

	/**
	 * 上傳檔案到 /moduleShare/tsmpdpapi/storage/temp
	 * 並回傳檔名如: timestamp.wait.原始檔名
	 * @param filename
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public final Path uploadTemp01(String filename, byte[] content) throws Exception {
		final String newFilename = getTempFilename(filename);
		if (!StringUtils.hasLength(newFilename)) {
			throw new FileAlreadyExistsException("Unable to generate temp file name, please try later.");
		}
		this.logger.debug("Temp filename: " + newFilename);
		final Path path = upload01(getUploadTemp(), newFilename, content);
		// 發動排程,刪除過期暫存檔
		fireJob();
		return path;
	}
	
	public TsmpDpFile uploadTemp(String userName, String decFileName, byte[] fileContent) throws Exception {
		String tempPath = getUploadTemp();
		TsmpDpFile vo = new TsmpDpFile();
		vo.setFilePath(tempPath);
		vo.setRefFileCateCode(TsmpDpFileType.TEMP.value());
		vo.setRefId(-1L);
		vo.setIsBlob("Y");
		vo.setIsTmpfile("Y");
		vo.setCreateUser(userName);
		
		try {
			vo.setBlobData(fileContent);
		}catch(Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode.FAIL_CREATE_File.throwing();
		}
		
		long timestampMillis = Instant.now().toEpochMilli();
		for(int i=0;i<100;i++) {
			try {
				final String timestamp = String.valueOf(timestampMillis);
				final String newFilename = 	timestamp.concat(".wait.").concat(decFileName);
				vo.setFileName(newFilename);
				vo = getTsmpDpFileDao().save(vo);
				this.logger.debug("Temp filename: " + newFilename);
				break;
			}catch(Exception e) {
				timestampMillis++;
				if(i == 99) {
					logger.error(StackTraceUtil.logStackTrace(e));
					throw new FileAlreadyExistsException("Unable to generate temp file name, please try later.");
				}
			}
		}
		
		// 發動排程,刪除過期暫存檔
		fireTsmpDpFileJob();
		
		return vo;
	
	}

	/**
	 * 從 temp 資料夾複製檔案到指定路徑
	 * (由排程刪除過期的 temp 檔案)
	 * @param fileType
	 * @param refId
	 * @param tempFilename 暫存檔的檔名
	 * @return
	 */
	public final Path moveTemp01(TsmpDpFileType fileType, Long refId, String tempFilename) throws Exception {
		// 創建上傳路徑
		String tsmpDpFilePath = getTsmpDpFilePath(fileType, refId);
		return moveTemp01(tsmpDpFilePath, tempFilename);
	}

	/**
	 * 從 temp 資料夾複製檔案到指定路徑
	 * (由排程刪除過期的 temp 檔案)
	 * @param tsmpDpFilePath
	 * @param tempFilename
	 * @return
	 */
	public final Path moveTemp01(String tsmpDpFilePath, String tempFilename) throws Exception {
		if (getTsmpDpAaUploadPath() == null ||
			!StringUtils.hasLength(tsmpDpFilePath) || //
			!StringUtils.hasLength(tempFilename)) {
			return null;
		}

		// 檢查暫存檔是否存在
		boolean isTempFileExists = exists(getUploadTemp(), tempFilename);
		if(!isTempFileExists) {
			this.logger.error(String.format("File name %s doesn't exist under path %s", tempFilename, getTsmpDpAaUploadTemp().toString()));
			return null;
		}

		// 復原原始檔名
		final String origFilename = restoreOrginalFilename(tempFilename);
		if (!StringUtils.hasLength(origFilename)) {
			this.logger.error(String.format("Invalid temp file name: %s", tempFilename));
		}

		// 複製檔案到指定路徑
		final byte[] content = download01(getUploadTemp(), tempFilename);
		return upload01(tsmpDpFilePath, origFilename, content);
	}
	
	/**
	 * 從 temp 資料夾複製檔案到指定路徑
	 * (由排程刪除過期的 temp 檔案)
	 * @param userName
	 * @param refFileCateCode
	 * @param refId
	 * @param tempFilename
	 * @param isCreate
	 * @param isUpdate
	 * @return
	 */
	public final TsmpDpFile moveTemp(String userName, TsmpDpFileType refFileCateCode, Long refId, String tempFilename,boolean isCreate,boolean isUpdate) throws Exception {
		return moveTemp(userName, refFileCateCode, refId, tempFilename, isCreate, isUpdate, false);
	}
	
	/**
	 * 從 temp 資料夾複製檔案到指定路徑
	 * (由排程刪除過期的 temp 檔案)
	 * @param userName
	 * @param refFileCateCode
	 * @param refId
	 * @param tempFilename
	 * @param isCreate
	 * @param isUpdate
	 * @param isSaveFlush
	 * @return
	 */
	public final TsmpDpFile moveTemp(String userName, TsmpDpFileType refFileCateCode, Long refId, String tempFilename,boolean isCreate,boolean isUpdate,boolean isSaveFlush) throws Exception {
		if (!StringUtils.hasLength(userName) || !StringUtils.hasLength(tempFilename)) {
			return null;
		}

		List<TsmpDpFile> fileList = getTsmpDpFileDao().findByFileName(tempFilename);
		
		byte[] content = null;
		// 檢查暫存檔是否存在
		if(fileList == null || fileList.size() != 1) {
			this.logger.error(String.format("File name %s doesn't exist TSMP_DB_FILE", tempFilename));
			if(fileList != null) {
				this.logger.error(String.format("fileList.size=%d", fileList.size()));
			}
			//return null;
			
			boolean isTempFileExists = exists(getUploadTemp(), tempFilename);
			if(!isTempFileExists) {
				this.logger.error(String.format("File name %s doesn't exist under path %s", tempFilename, getTsmpDpAaUploadTemp().toString()));
				return null;
			}
			content = download01(getUploadTemp(), tempFilename);
		}

		// 復原原始檔名
		final String origFilename = restoreOrginalFilename(tempFilename);
		if (!StringUtils.hasLength(origFilename)) {
			this.logger.error(String.format("Invalid temp file name: %s", tempFilename));
			return null;
		}
		
		String tsmpDpFilePath =  getTsmpDpFilePath(refFileCateCode, refId);
		String newFileName = renameIfDuplicate(refFileCateCode.value(), refId, origFilename);
		
		TsmpDpFile fileVo = null;
		if(fileList == null || fileList.size() == 0) {
			fileVo = new TsmpDpFile();
		}else {
			fileVo = fileList.get(0);
		}
		
		
		/*
		if(!"Y".equals(fileVo.getIsBlob()) && fileVo.getBlobData() == null) {
			// 檢查暫存檔是否存在
			boolean isTempFileExists = exists(getUploadTemp(), tempFilename);
			if(!isTempFileExists) {
				this.logger.error("File name {} doesn't exist under path {}", tempFilename, getTsmpDpAaUploadTemp());
				return null;
			}
			
			final byte[] content = download01(getUploadTemp(), tempFilename);
			fileVo.setBlobData(new SerialBlob(content));
			fileVo.setIsBlob("Y");
		}*/
		
		fileVo.setIsBlob("Y");
		fileVo.setIsTmpfile("N");
		fileVo.setRefFileCateCode(refFileCateCode.value());
        fileVo.setRefId(refId);
        fileVo.setFilePath(tsmpDpFilePath);
        fileVo.setFileName(newFileName);
        
        if(content != null) {
        	fileVo.setBlobData(content);
        }else if(fileVo.getBlobData() != null) {
        	//不知為何sql server沒執行這一段的話會報錯
        	//byte[] tempBlob = fileVo.getBlobData().getBytes(1l, (int)fileVo.getBlobData().length()) ;
            //fileVo.setBlobData(tempBlob);
        }

        if(isCreate) {
        	fileVo.setCreateUser(userName);	
        }
        
        if(isUpdate) {
        	fileVo.setUpdateUser(userName);
        	fileVo.setUpdateDateTime(DateTimeUtil.now());
        }
        
        if(isSaveFlush) {
        	fileVo = getTsmpDpFileDao().saveAndFlush(fileVo);
        }else {
        	fileVo = getTsmpDpFileDao().save(fileVo);
        }
        
		return fileVo;
	}

	/**
	 * 此方法由 {@link DeleteExpiredTempFileJob#runJob} 呼叫
	 * 用以刪除過期的暫存檔
	 * @param expMs
	 * @return
	 * @throws IOException 
	 */
	public final int deleteExpiredTempFile(Long expMs) throws IOException {
		if (getTsmpDpAaUploadTemp() == null) {
			return 0;
		}

		final long expireTime = Instant.now().toEpochMilli() - expMs;
		Pattern p = Pattern.compile("(\\d*)\\.wait\\..*");
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			public boolean accept(Path file) throws IOException {
				String filename = file.getFileName().toString();
				// 從檔名取得時間戳記
				Matcher m = p.matcher(filename);
				if (m.matches()) {
					long timestamp = Long.valueOf(m.group(1));
					return timestamp < expireTime;
				}

				return false;
			}
		};

		int delCnt = 0;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(getTsmpDpAaUploadTemp(), filter)) {
			Path file;
			boolean isSuccess;
			Iterator<Path> iterator = stream.iterator();
			while(iterator.hasNext()) {
				file = iterator.next();
				isSuccess = Files.deleteIfExists(file);
				if (isSuccess) {
					this.logger.info("Removing temp file from: " + file.toAbsolutePath());
					delCnt++;
				} else {
					this.logger.error("Removing temp file error: " + file.toAbsolutePath());
				}
			}
		}
		return delCnt;
	}
		
	/**
	 * 用以刪除過期的暫存檔Db
	 * switchPatternFileName否要用檔名判斷時間
	 * @param expMs 
	 * @param switchPatternFileName
	 * @return
	 * @throws IOException 
	 */
	public final int deleteExpiredTempByTsmpDpFile(Long expMs, boolean switchPatternFileName) throws IOException {
		List<TsmpDpFile> list = getTsmpDpFileDao().findByIsTmpfile("Y");
		int delCnt = 0;
		final long expireTime = Instant.now().toEpochMilli() - expMs;
		
		Pattern p = Pattern.compile("(\\d*)\\.wait\\..*");
		if (switchPatternFileName) {
			for (TsmpDpFile dpFile : list) {
				String fileName = dpFile.getFileName();
				Matcher m = p.matcher(fileName);
				if (m.matches()) {
					long timestamp = Long.valueOf(m.group(1));
					if (timestamp < expireTime) {
						getTsmpDpFileDao().deleteById(dpFile.getFileId());

						delCnt++;
					}
				}
			}
		} else {
			for (TsmpDpFile dpFile : list) {
				Date lastDate = dpFile.getCreateDateTime();
				Date updateDateTime = dpFile.getUpdateDateTime();
				if (updateDateTime != null) {
					lastDate = updateDateTime;
				}
				Long lastLocalDateTime = lastDate.getTime();
				if (lastLocalDateTime < expireTime) {
					getTsmpDpFileDao().deleteById(dpFile.getFileId());
					delCnt++;
				}
			}
		}
		return delCnt;
	}
	

	protected void fireJob() {
		try {
			DeleteExpiredTempFileJob job = (DeleteExpiredTempFileJob) getCtx().getBean("deleteExpiredTempFileJob");
			getJobHelper().add(job);
		} catch (Exception e) {
			this.logger.error("Fail to fire job!\n" + StackTraceUtil.logStackTrace(e));
		}
	}
	
	public void fireTsmpDpFileJob() {
		try {
			DeleteExpiredTempFileTsmpDpFileJob job = (DeleteExpiredTempFileTsmpDpFileJob) getCtx().getBean("deleteExpiredTempFileTsmpDpFileJob", true);
			getJobHelper().add(job);
		} catch (Exception e) {
			this.logger.error("Fail to fire job!\n" + StackTraceUtil.logStackTrace(e));
		}
	}
	
	public void fireTsmpDpFileJob(boolean switchPatternFileName) {
		try {
			DeleteExpiredTempFileTsmpDpFileJob job = (DeleteExpiredTempFileTsmpDpFileJob) getCtx().getBean("deleteExpiredTempFileTsmpDpFileJob", switchPatternFileName);
			getJobHelper().add(job);
		} catch (Exception e) {
			this.logger.error("Fail to fire job!\n" + StackTraceUtil.logStackTrace(e));
		}
	}
	

	/**
	 * 取得一個不重複的暫存檔名
	 * 格式: timestamp.wait.原始檔名
	 * @param filename 原始檔名
	 * @return
	 */
	private final String getTempFilename(String filename) {
		long timestampMillis = Instant.now().toEpochMilli();
		boolean isExists = true;

		try {
			// 重試最多 100 次
			for(int i = 0; i < 100; i++) {
				final String timestamp = String.valueOf(timestampMillis);
				final String newFilename = timestamp.concat(".wait.").concat(filename);
				isExists = exists(getUploadTemp(), newFilename);
				if (isExists) {
					timestampMillis++;
				} else {
					return newFilename;
				}
			}
		} catch (Exception e) {
			logger.debug("" + e);
		}

		return new String();
	}

	/**
	 * 將暫存檔名復原成原始檔名
	 * 暫存檔名格式: timestamp.wait.原始檔名
	 * @param tempFilename
	 * @return
	 */
	public final String restoreOrginalFilename(String tempFilename) {
		Pattern p = Pattern.compile("\\d*\\.wait\\.(.*)");
		if (tempFilename==null) {tempFilename="";}
		Matcher m = p.matcher(tempFilename); // 不接受 null 
		if (m.matches()) {
			return m.group(1);
		}
		return new String();
	}
	
	/**
	 * 取得暫存檔實際路徑
	 * 
	 * @param tsmpDpFilePath
	 * @return
	 * @throws IOException
	 */
	public final String getTsmpDpFileAbsolutePath(String tsmpDpFilePath, String filename) throws IOException {
		Path folder = TSMP_DP_AA_UPLOAD_PATH.resolve(tsmpDpFilePath);
		Path tempFilePath = folder.resolve(filename);
		String absolutePath = tempFilePath.toAbsolutePath().toString();
		
		return absolutePath;		
	}

	public boolean isTempFile(String filename) {
		//這段 Regex 已被 Tom Review 過了, 故取消 hotspot 標記
		return (filename != null) && filename.matches("\\d.*\\.wait\\..*\\..*"); // NOSONAR
	}

	/**
	 * Copy all job related files
	 * @param fromRefFileCateCode
	 * @param fromRefId
	 * @param toRefFileCateCode
	 * @param toRefId
	 * @param isOverride
	 * @return
	 * @throws Exception
	 */
	public List<Long> copyFilesByRefFileCateCodeAndRefId(String fromRefFileCateCode, Long fromRefId, //
			String toRefFileCateCode, Long toRefId, boolean isOverride) throws Exception {
		return copyFilesByRefFileCateCodeAndRefId(fromRefFileCateCode, fromRefId, toRefFileCateCode, toRefId, //
			null, isOverride);
	}

	/**
	 * Copy job related files which are filtered
	 * @param fromRefFileCateCode
	 * @param fromRefId
	 * @param toRefFileCateCode
	 * @param toRefId
	 * @param fileFilter
	 * @param isOverride
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public List<Long> copyFilesByRefFileCateCodeAndRefId(String fromRefFileCateCode, Long fromRefId, //
		String toRefFileCateCode, Long toRefId, Predicate<TsmpDpFile> fileFilter, boolean isOverride) //
			throws Exception {
		
		if (fromRefFileCateCode.equals(toRefFileCateCode) && fromRefId.equals(toRefId)) {
			throw new Exception("Can't copy to the same source");
		}

		List<TsmpDpFile> files = getTsmpDpFileDao().findByRefFileCateCodeAndRefId(fromRefFileCateCode, fromRefId);
		if (fileFilter != null) {
			files = files.stream().filter(fileFilter).collect(Collectors.toList());
		}
		if (CollectionUtils.isEmpty(files)) {
			this.logger.debug("No files to copy");
			return Collections.emptyList();
		}
		
		List<Long> copiedFileIds = new ArrayList<>();
		TsmpDpFile toFile = null;
		for (TsmpDpFile fromFile : files) {
			// Preserve file content for deep copy
			byte[] content = null;
			try {
				content = download(fromFile);
			} catch (Exception e) {
				this.logger.error(String.format("Failed to download blobData: fileId=%d", fromFile.getFileId()));
				throw e;
			}
			
			// Check unique key
			List<TsmpDpFile> duplicatedFiles = getTsmpDpFileDao() //
				.findByRefFileCateCodeAndRefIdAndFileName(toRefFileCateCode, toRefId, fromFile.getFileName());
			if (CollectionUtils.isEmpty(duplicatedFiles)) {
				String filePath = Paths.get(toRefFileCateCode, String.valueOf(toRefId)).toString() + File.separator;
				toFile = new TsmpDpFile();
				toFile.setFileName(fromFile.getFileName());
				toFile.setFilePath(filePath);
				toFile.setRefFileCateCode(toRefFileCateCode);
				toFile.setRefId(toRefId);
			} else {
				if (!isOverride) {
					this.logger.debug(String.format( //
						"File name '%s' already exists under %s-%d", //
						fromFile.getFileName(), toRefFileCateCode, toRefId //
					));
					continue;
				}
				// Don't deep copy blobData
				toFile = duplicatedFiles.get(0);
				toFile.setBlobData(null);
				toFile = ServiceUtil.deepCopy(toFile, TsmpDpFile.class);
				toFile.setUpdateDateTime(DateTimeUtil.now());
				toFile.setUpdateUser("SYSTEM");
			}

			// Copy blob data
			try {
				if(content != null) {
					toFile.setBlobData(content);
		        }
			} catch (Exception e) {
				this.logger.error(String.format("Failed to copy file '%s' from %s-%d to %s-%d: \n%s", //
					fromFile.getFileName(), //
					fromRefFileCateCode, fromRefId, //
					toRefFileCateCode, toRefId, //
					StackTraceUtil.logStackTrace(e)));
				throw e;
			}
			
			toFile.setIsBlob(fromFile.getIsBlob());
			toFile.setIsTmpfile(fromFile.getIsTmpfile());
			toFile = getTsmpDpFileDao().save(toFile);
			copiedFileIds.add(toFile.getFileId());
		}
		this.logger.debug(String.format("%d file(s) are copied successfully", copiedFileIds.size()));
		return copiedFileIds;
	}

	public String getUploadPrefix() {
		return UPLOAD_PREFIX;
	}

	public Path getTsmpDpAaUploadPath() {
		return TSMP_DP_AA_UPLOAD_PATH;
	}

	public String getUploadTemp() {
		return UPLOAD_TEMP;
	}

	public Path getTsmpDpAaUploadTemp() {
		return TSMP_DP_AA_UPLOAD_TEMP;
	}
	
	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

}
