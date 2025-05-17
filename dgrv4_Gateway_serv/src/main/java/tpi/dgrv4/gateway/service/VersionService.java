package tpi.dgrv4.gateway.service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.IVersionService;
import tpi.dgrv4.entity.vo.VersionInfo;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.ClientKeeper;

@Service
public class VersionService implements IVersionService{
	// 是否為 Open Source 使用, 預設 'false' 
	// Whether it is Open Source, the default value is 'false'
	protected static boolean isOpenSource = true;

	/**
	 * 需要存取外界, 不加入 UT
	 */
	protected String getAllClientList() {
		if (TPILogger.lc == null) return null;
			
		String username = TPILogger.lc.userName;
		LinkedList<ClientKeeper> allClientList = null;
		allClientList = (LinkedList<ClientKeeper>)TPILogger.lc.paramObj.get("allClientList");

		List<ClientKeeper> clientKeeperList = null;
		ClientKeeper clientKeeper = null;
		if (allClientList != null) {
			clientKeeperList = allClientList.stream() //
					.filter(ck -> ck.getUsername().equals(username)) //
					.collect(Collectors.toList());
		}

		// 取 jar 檔的版本
		String version = null; // version:dgrv4-gateway-1.0.0-20230420_1100.jar 
		if (clientKeeperList != null && clientKeeperList.size() == 1) {
			clientKeeper = clientKeeperList.get(0);
			version = clientKeeper.getVersion();
		}
		return version;
	}
	
	/**
	 * 讀取 .txt 的內容版本
	 */
	protected String getTxtVersion() {
		String version = null;
		try (var is = new ClassPathResource("open-source-version.txt").getInputStream()) {
			version = new String(is.readAllBytes());
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		return version;
	}
	
	public String getProjectName() {
		return "digiRunner";
	}
	
	public VersionInfo getVersion() {
		VersionInfo v = new VersionInfo();
		if (isOpenSource) {
			v = getVersionForOpenSource();
		} else {
			v = getVersionForEnterprise();
		}

		return v;
	}

	/**
	 * for Enterprise
	 */
	public VersionInfo getVersionForEnterprise() {
		// POJO Data
		VersionInfo v = new VersionInfo();
		
		String version = getAllClientList(); // version:dgrv4-gateway-1.0.0-20230420_1100.jar 
		
		String MajorVersionNo = null;	// 1.0.0
		String strVersion = null;		// 1.0.0-20230420_1100
		if(version != null && StringUtils.hasText(version)) {
			
			version = version.replace("dgrv4-gateway-", "");
			version = version.replace(".jar", "");

			String[] arrVer = version.split("-");
			if (arrVer.length >= 1) {
				MajorVersionNo = arrVer[0];
			}
			if (arrVer.length == 1) {
				strVersion = MajorVersionNo;
			} else {
				if (arrVer.length >= 2) {
					strVersion = MajorVersionNo + "-" + arrVer[1];
				}
				if (arrVer.length >= 3) {
					strVersion = strVersion + "-" + arrVer[2];
				}
			}
		}
		v.majorVersionNo = MajorVersionNo;
		v.strVersion = strVersion;
		return v;
	}
 
	/**
	 * for Open Source <br>
	 * 此方法為讀取 open-source-version.txt 為顯示版本 <br>
	 * This method reads open-source-version.txt to display the version <br>
	 */
	public VersionInfo getVersionForOpenSource() {
		// POJO Data
		VersionInfo v = new VersionInfo();
		v.strVersion = "0.0.0"; // release-rc-v4.4.18.1-1-g5c5e4d8ac
		v.majorVersionNo = "0.0.0"; // v4.4.18.1
		
		String version = getTxtVersion();
		if (version != null && StringUtils.hasText(version)) {
			v.strVersion = version; // release-rc-v4.4.18.1-1-g5c5e4d8ac
			version = version.replace("release-", "");
			version = version.replace("rc-", "");
			v.majorVersionNo = version.split("-")[0]; // v4.4.18.1
		}

		return v;
	}
}
