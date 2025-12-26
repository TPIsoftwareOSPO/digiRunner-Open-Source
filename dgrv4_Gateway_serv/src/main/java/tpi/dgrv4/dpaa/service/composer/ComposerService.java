package tpi.dgrv4.dpaa.service.composer;

import java.util.List;

public interface ComposerService {

	/**
	 * digiRunner 若有修改到 Composer 的 API 時，<br>
	 * 需要透過 <b>[PATCH]</b> /editor/admin/tsmpApis/{api_uid}/restart 這支 API<br>
	 * 告訴 Composer 哪支 API 被修改過，Composer 需要重新啟動該 API，才能完成修改				
	 *     2022/111/01 停用
	 * @param apiUid
	 * 
	 */
	void confirmAllNodes(String apiUUID);

	/**
	 * delete 已包含 stop 的動作，若對多台主機發送 delete， Composer 會報錯
	 *     2022/11/01 停用
	 * @param apiUUID
	 * 
	 */
	void deleteAndStopAllNodes(String apiUUID);

	List<String> getComposerIPs();
	
	String findAllNodesByModuleNameAndApiName(List<List<String>> request);
	
	String saveToCollectionsV4(List<List<String>> request);

}