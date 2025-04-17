package tpi.dgrv4.gateway.component.job.appt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.constant.HttpType;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpCoreTokenHelperCacheProxy;
import tpi.dgrv4.gateway.component.job.appt.HttpUtilJob.InParams.InArgs;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This job can call all public and static methods declared in {@link HttpUtil}
 * which return values are must be {@link HttpRespData}.<br>
 * To invoke methods declared in {@link HttpUtil}, <b>inParams</b> field of
 * {@link TsmpDpApptJob} must be deserializable from {@link InParams} object.
 * @author Kim
 *
 */
@SuppressWarnings("serial")
public class HttpUtilJob extends ApptJob {

	private TPILogger logger = TPILogger.tl;

	private final static String RESP_FILE_NAME = "HttpCall.txt";

	private FileHelper fileHelper;
	private ObjectMapper objectMapper;
	private TsmpCoreTokenHelperCacheProxy tsmpCoreTokenHelperCacheProxy;
	private TsmpSettingService tsmpSettingService;
	
	private String type;
	
	@Autowired
	public HttpUtilJob(TsmpDpApptJob tsmpDpApptJob, String type, FileHelper fileHelper, ObjectMapper objectMapper,
			TsmpCoreTokenHelperCacheProxy tsmpCoreTokenHelperCacheProxy, TsmpSettingService tsmpSettingService,
			ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.type = type;
		this.fileHelper = fileHelper;
		this.objectMapper = objectMapper;
		this.tsmpCoreTokenHelperCacheProxy = tsmpCoreTokenHelperCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
	}

	@Override
	public String runApptJob() throws Exception {
		// 1. 將 TsmpDpApptJob.inParams 的 JSON 轉型成內部物件
		InParams inParams = parseInParams();
		
		// 2. 檢查 inParams 內容是否正確
		checkInParams(inParams);
		
		Map<String, String> header = new HashMap<>();
		header.put("ApptJobId", getTsmpDpApptJob().getApptJobId()+"");
		putHeader(inParams, header);
		
		// 3. 依照type 去加入cAPI-Key, No-Auth, Basic
		inParams = putAuthentication(inParams, type);
		
		// 4. 依照 inParams 尋找 HttpUtil 的方法
		Method method = findMethod(inParams);

		// 5. 檢查欲呼叫的方法是否正確
		Object[] args = checkMethod(method, inParams);
		
		// 6. 呼叫並記錄回傳值
		step("Sending HTTP request");
		HttpRespData resp = getRespData(method, args);
		step("Receive HTTP response");
		
		// 6. 將 log 寫入 "執行訊息" 欄位
		updateStackTrace(resp);
		
		// 7. 將執行結果另存為排程相關檔案
		saveRespAsFile(resp);
		
		if(resp.statusCode >=200 && resp.statusCode < 400){
			return "SUCCESS";
		}else{
			return "FAILED";
		}
	}

	private InParams putAuthentication(InParams inParams, String type) throws Exception {
		Map<String, String> header = new HashMap<>();
		if (HttpType.BASIC.equals(type)) {
			String basic = getBasic(inParams);
			header.put("Authorization", basic);
			putHeader(inParams, header);
			
		}else if (HttpType.C_APIKEY.equals(type)) {
			String UUID = java.util.UUID.randomUUID().toString().toUpperCase();
			String ckey = CApiKeyUtils.signCKey(UUID);
			header.put(HttpType.C_APIKEY, ckey);			
			header.put("cuuid", UUID);
			putHeader(inParams, header);
		}
		return inParams;
	}
	
	private Boolean isCallComposer(InParams inParams) {
		for (InArgs inArg : inParams.getArgs()) {
	        if ("java.lang.String".equals(inArg.getType())) {
	        	if (inArg.getArg() instanceof String) {
	        	    String arg = (String) inArg.getArg();
	        	    List<String> addressList = getTsmpSettingService().getVal_TSMP_COMPOSER_ADDRESS();
	        	    if(addressList!=null) {
	        	    	for(String address : addressList) {
	        	    		if(arg.indexOf(address) > -1) return true;
	        	    	}
	        	    }
	        	    
	        	}
	        }
	    }
		return false;
	}

	private String getBasic(InParams inParams) throws Exception {
		String userName = inParams.getUsername();
		String mima = inParams.getMima();
		if (!StringUtils.hasLength(userName) || !StringUtils.hasLength(mima)) {
			TPILogger.tl.error(" httpUtilJob username or mima is empty");
			throw new Exception("username or mima is empty");
		}
		mima = getENCDecode(mima);
		String merge = userName + ":" + mima;
		byte[] basicByte = merge.getBytes(StandardCharsets.UTF_8);
		String basicString = "Basic " + Base64Util.base64Encode(basicByte);
		return basicString;
	}
	
	private String getENCDecode(String value) {
		Pattern pattern = Pattern.compile("^ENC\\((\\S+)\\)$");
		Matcher matcher = pattern.matcher(value);
		if (matcher.matches()) {
			value = matcher.group(1);
			value = getTsmpCoreTokenHelperCacheProxy().decrypt(value);
		}
        return value;
    }

	private void putHeader(InParams inParams, Map<String, String> header) {
	    for (InArgs inArg : inParams.getArgs()) {
	        if ("java.util.Map".equals(inArg.getType())) {
	            Map<String, String> map = (Map<String, String>) inArg.getArg();
	            
	            // 創建一個新的 Map 來合併 header 和 map
	            Map<String, String> mergedMap = new HashMap<>(map);
	            
	            // 將 header 中不存在於 map 的項目加入到 mergedMap 中
	            for (Map.Entry<String, String> entry : header.entrySet()) {
	                String key = entry.getKey();
	                String value = entry.getValue();
	                mergedMap.putIfAbsent(key, value);
	            }
	            
	            // 更新 InArgs 中的 arg 為合併後的 Map
	            inArg.setArg(mergedMap);
	        }
	    }
	}

	protected InParams parseInParams() throws Exception {
		try {
			String json = getTsmpDpApptJob().getInParams();
			InParams inParams = getObjectMapper().readValue(json, InParams.class);
			return inParams;
		} catch (Exception e) {
			throw new Exception("參數設定錯誤", e);
		}
	}

	@SuppressWarnings("rawtypes")
	protected void checkInParams(InParams inParams) throws Exception {
		if (ObjectUtils.isEmpty(inParams)) {
			throw new Exception("空白的參數設定");
		}
		String methodName = inParams.getMethodName();
		if (!StringUtils.hasLength(methodName)) {
			throw new Exception("必須指定方法名稱(methodName)");
		}
		List<InParams.InArgs> args = inParams.getArgs();
		if (!CollectionUtils.isEmpty(args)) {
			String type = null;
			Class clazz = null;
			ClassLoader classLoader = getClass().getClassLoader();
			try {
				for (InParams.InArgs arg : args) {
					type = arg.getType();
					clazz = ClassUtils.forName(type, classLoader);
					arg.setClazz(clazz);
				}
			} catch (Exception e) {
				throw new Exception("無法載入指定的類別名稱: " + type, e);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected Method findMethod(InParams inParams) throws Exception {
		String methodName = inParams.getMethodName();
		Class[] paramTypes = toParamTypes(inParams.getArgs());

		// 確認是否找得到該方法
		Method method = null;
		try {
			method = ClassUtils.getMethod(HttpUtil.class, methodName, paramTypes);
        } catch (Exception e) {
			String className = HttpUtil.class.getName();
			List<String> typeNames = toTypeNames(inParams.getArgs());
			String methodDesc = String.format("%s(%s)", methodName, //
				(CollectionUtils.isEmpty(typeNames)) ? "" : String.join(", ", typeNames));
			throw new Exception("在 " + className + " 中找不到指定的方法: " + methodDesc, e);
		}
		return method;
	}

	@SuppressWarnings("rawtypes")
	protected Object[] checkMethod(Method method, InParams inParams) throws Exception {
		// 確認該方法的回傳值是 HttpRespData
		Class returnType = method.getReturnType();
		if (!HttpRespData.class.equals(returnType)) {
			throw new Exception("方法的回傳值必須是: " + HttpRespData.class.getName());
		}

		int modifier = method.getModifiers();
		if (!(Modifier.isPublic(modifier) && Modifier.isStatic(modifier))) {
			throw new Exception("只允許呼叫宣告為 public static 的方法");
		}
		
		Object[] args = toObjectArgs(inParams.getArgs());

		// 應該不會跑到這, 因為如果參數數量錯了, 在前一個步驟(搜尋 method)的時候就會找不到
		int parameterCount = method.getParameterCount();
		if (parameterCount != args.length) {
			throw new Exception("該方法需要 " + parameterCount + " 個參數, 您設定了 " + args.length + " 個");
		}
		
		Class argType = null;
		Class paramType = null;
		Class[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterCount; i++) {
			if (args[i] == null) {
				continue;
			}

			paramType = parameterTypes[i];
			argType = args[i].getClass();
			// Considering wrapper classes like Boolean, Integer...
			if (ClassUtils.isAssignable(paramType, argType) == false) {
				throw new Exception("方法中第 " + (i + 1) + " 個參數的型態為 " + paramType.getName() + ", "
					+ "與傳入的 " + argType.getName() + " 物件型態不符");
			}
		}
		
		return args;
	}

	protected HttpRespData getRespData(Method method, Object...args) throws Exception {
		this.logger.debug(String.format("Invoking method: %s", method.toString()));
		HttpRespData resp = HttpRespData.class.cast( //
			method.invoke(null, args) //
		);
		// 當isisEnableInputStream 要做fetchByte否則沒有respStr
		if (resp.isEnableInputStream)
			resp.fetchByte();
		return resp;
	}

	protected void updateStackTrace(HttpRespData resp) {
		String stackTrace = resp.getLogStr();
		stackTrace = stackTrace.substring(0, Math.min(stackTrace.length(), MAX_STACK_TRACE_LENGTH));
		getTsmpDpApptJob().setStackTrace(stackTrace);
		save();
	}

	protected void saveRespAsFile(HttpRespData resp) {
		String logStr = resp.getLogStr();
		if (!StringUtils.hasLength(logStr)) {
			return;
		}
		
		String userName = getTsmpDpApptJob().getUpdateUser();
		TsmpDpFileType fileType = TsmpDpFileType.TSMP_DP_APPT_JOB;
		Long refId = getTsmpDpApptJob().getApptJobId();
		String filename = String.format("%d-%s", refId, RESP_FILE_NAME);
		byte[] content = logStr.getBytes(StandardCharsets.UTF_8);
		String isTmpFile = "N";
		try {
			getFileHelper().upload(userName, fileType, refId, filename, content, isTmpFile);
		} catch (SQLException e) {
			this.logger.warn("儲存 API 回傳資料時發生錯誤");
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
	}

	@SuppressWarnings("rawtypes")
	private Class[] toParamTypes(List<InParams.InArgs> args) {
		List<Class> types = getFromArgs(args, (arg) -> arg.getClazz());
		return types.stream().toArray(Class[]::new);
	}

	private List<String> toTypeNames(List<InParams.InArgs> args) {
		List<String> names = getFromArgs(args, (arg) -> arg.getType());
		return names;
	}

	private Object[] toObjectArgs(List<InParams.InArgs> args) {
		List<Object> objs = getFromArgs(args, (arg) -> arg.getArg());
		return objs.stream().toArray(Object[]::new);
	}

	private <R> List<R> getFromArgs(List<InParams.InArgs> args, Function<InParams.InArgs, R> func) {
		return Optional.ofNullable(args) //
			.map((list) -> list.stream().map(func).collect(Collectors.toList())) //
			.orElse(Collections.emptyList());
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}
	
    protected TsmpCoreTokenHelperCacheProxy getTsmpCoreTokenHelperCacheProxy() {
        return tsmpCoreTokenHelperCacheProxy;
    }
    
    protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected static class InParams {
		
		private String methodName;

		private List<InArgs> args;
		
		private String username;
		
		private String mima;
		

		public String getMethodName() {
			return methodName;
		}

		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}

		public List<InArgs> getArgs() {
			return args;
		}

		public void setArgs(List<InArgs> args) {
			this.args = args;
		}
		
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getMima() {
			return mima;
		}

		public void setMima(String mima) {
			this.mima = mima;
		}

		@SuppressWarnings("rawtypes")
		protected static class InArgs {
			
			private String type;

			private Object arg;

			@JsonIgnore
			private Class clazz;

			public String getType() {
				return type;
			}

			public void setType(String type) {
				this.type = type;
			}

			public Object getArg() {
				return arg;
			}

			public void setArg(Object arg) {
				this.arg = arg;
			}

			public Class getClazz() {
				return clazz;
			}

			public void setClazz(Class clazz) {
				this.clazz = clazz;
			}

		}

	}

}