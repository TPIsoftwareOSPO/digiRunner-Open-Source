package tpi.dgrv4.gateway.component.cache.proxy;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.IFileHelperCacheProxy;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.cache.core.AbstractCacheProxy;
import tpi.dgrv4.gateway.component.cache.core.GenericCache;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class FileHelperCacheProxy extends AbstractCacheProxy implements IFileHelperCacheProxy{
	private FileHelper fileHelper;

	@Autowired
	public FileHelperCacheProxy(FileHelper fileHelper, GenericCache genericCache, ObjectMapper objectMapper,
			ApplicationContext ctx, JobHelper jobHelper) {
		super(genericCache, objectMapper, ctx);
		this.fileHelper = fileHelper;
	}

	public byte[] downloadByPathAndName(String tsmpDpFilePath, String filename) {
		Supplier<byte[]> supplier = () -> {
			try {
				return getFileHelper().downloadByPathAndName(tsmpDpFilePath, filename);
			} catch (Exception e) {
				TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
				return null;
			}
		};
		return getOne("downloadByPathAndName", supplier, byte[].class, tsmpDpFilePath, filename).orElse(null);
	}

	@Override
	protected Class<?> getDaoClass() {
		return FileHelper.class;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}
	
}