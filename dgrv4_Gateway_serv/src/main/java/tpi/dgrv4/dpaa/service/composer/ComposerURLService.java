package tpi.dgrv4.dpaa.service.composer;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.escape.MailHelper;

@Service
public class ComposerURLService {

	private final String PATH_RESTART = "/editor/admin/tsmpApis/{{0}}/restart";

	private final String PATH_DELETE = "/editor/admin/tsmpApis/{{0}}";

	private final String PATH_STOP = "/editor/admin/tsmpApis/{{0}}/stop";

	private final String PATH_AUTH = "/editor/auth/token";
	
	private final String PATH_FIND_APPLICATIONS = "/editor/admin/tsmpApis/findApplicationsByModuleNameAndApiName";
	
	private final String PATH_FIND_NODES = "/editor/admin/tsmpApis/findNodesByAppId";
	
	private final String PATH_SAVE_COLLECTIONS = "/editor/admin/tsmpApis/saveToCollections";
	
	private final String PATH_FIND_All_NODES_V4 = "/editor/admin/export";	
	
	private final String PATH_SAVE_COLLECTIONS_V4 = "/editor/admin/import";
	
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public ComposerURLService(TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
	}

	public List<String> getRestartPaths(String apiUid) {
		return resolvePath(this.PATH_RESTART, apiUid);
	}

	public List<String> getDeletePaths(String apiUid) {
		return resolvePath(this.PATH_DELETE, apiUid);
	}

	public List<String> getStopPaths(String apiUid) {
		return resolvePath(this.PATH_STOP, apiUid);
	}

	/**
	 * 如果有多台主機，則僅需通知第一台 delete，其他通知 stop 即可
	 * @param apiUid
	 * @return
	 */
	public List<String> getDeleteAndStopPaths(String apiUid){
		List<String> deletePaths = getDeletePaths(apiUid);
		if (CollectionUtils.isEmpty(deletePaths)) {
			return new ArrayList<>();
		}
		
		if (deletePaths.size() == 1) {
			return deletePaths;
		} else {
			List<String> stopPaths = getStopPaths(apiUid);
			stopPaths.set(0, deletePaths.get(0));
			return stopPaths;
		}
	}

	public List<String> getAuthPaths() {
		return resolvePath(this.PATH_AUTH);
	}

	public List<String> getFindApplicationsPaths() {
		 List<String> list = resolvePath(this.PATH_FIND_APPLICATIONS);
		return list;
	}

	public List<String> getFindNodesPaths() {
		return resolvePath(this.PATH_FIND_NODES);
	}

	public List<String> getSaveCollectionsPaths() {
		return resolvePath(this.PATH_SAVE_COLLECTIONS);
	}
	
	public List<String> getSaveCollectionsPathsV4() {
		return resolvePath(this.PATH_SAVE_COLLECTIONS_V4);
	}

	/**
	 * @return ex: "https://192.168.0.1:8080"
	 */
	public List<String> getComposerIPs() {
		List<String> addressList = getTsmpSettingService().getVal_TSMP_COMPOSER_ADDRESS();
		return addressList;
	}

	public List<String> resolvePath(String path, String... args) {
		Map<String, String> params = null;
		if (args != null) {
			params = new HashMap<>();
			int i = 0;
			for(String arg : args) {
				params.put(String.valueOf(i), arg);
				i++;
			}
		}
		
		final String p = MailHelper.buildContent(path, params);
		
		return getComposerIPs().stream().map((ip) -> {
			return ip.concat(p);
		}).collect(toList());
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
	
	public List<String> getFindAllNodesPaths() {
		 List<String> list = resolvePath(this.PATH_FIND_All_NODES_V4);
		return list;
	}

}