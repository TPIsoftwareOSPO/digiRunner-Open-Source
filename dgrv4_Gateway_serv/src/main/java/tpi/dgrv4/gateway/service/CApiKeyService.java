package tpi.dgrv4.gateway.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.codec.utils.SHA256Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.TCP.Packet.RequireAllClientListPacket;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.ComposerInfoData;

@Service
public class CApiKeyService {
	
	//若keeper server沒開或發生問題,會造成無法連線(EX:slave),所以要有個暫存的來回傳,AA0325也會存
	public static ConcurrentHashMap<String, Long> tempComposerLiveMap = new ConcurrentHashMap<>();

	public void verifyCApiKey(HttpHeaders headers, boolean logs, boolean isCheckComposerId) {

		List<String> uuidList = headers.get("cuuid");
		// "_"符號會被底層擋掉，修改為"-"符號
		List<String> cApikeyList = headers.get("capi-key");
		if (logs) {
			headers.forEach((key, value) -> {
				TPILogger.tl.debugDelay2sec(String.format("Header '%s' = %s", key, value));
			});
		}

		if (CollectionUtils.isEmpty(uuidList)) {
			TPILogger.tl.error("uuid isEmpty");
			throw TsmpDpAaRtnCode._1522.throwing();
		}
		if (CollectionUtils.isEmpty(cApikeyList)) {
			TPILogger.tl.error("cApikey isEmpty");
			throw TsmpDpAaRtnCode._1522.throwing();
		}
		String uuid = uuidList.get(0);
		String cApikey = cApikeyList.get(0);
		checkParams(uuid, cApikey);
		String cuuid = uuid.toString().toUpperCase();
		boolean isValidate = false;
		List<String> composerIdList = null;
		if(isCheckComposerId) {
			composerIdList = getLiveComposerId();
			isValidate = CApiKeyUtils.verifyCKey(cuuid, cApikey, composerIdList);
		}else {
			isValidate = CApiKeyUtils.verifyCKey(cuuid, cApikey);
		}
		
		if (!isValidate) {
			TPILogger.tl.error("uuid :" + uuid);
			TPILogger.tl.error("cuuid :" + cuuid);
			TPILogger.tl.error("cApikey :" + cApikey);
			if(isCheckComposerId) {
				TPILogger.tl.error("composerIdList :" + composerIdList);
			}
			throw TsmpDpAaRtnCode._1522.throwing();
		}
	}

	private void checkParams(String uuid, String cApikey) {
		if (!StringUtils.hasText(uuid) || !StringUtils.hasText(cApikey)) {
			TPILogger.tl.error("uuid :" + uuid);
			TPILogger.tl.error("cApikey :" + cApikey);
			throw TsmpDpAaRtnCode._1522.throwing();
		}
	}
	
	protected List<String> getLiveComposerId() {
		if (TPILogger.lc != null) {
			TPILogger.lc.send(new RequireAllClientListPacket());
			
			synchronized (RequireAllClientListPacket.waitKey) {
				try {
					RequireAllClientListPacket.waitKey.wait();
				} catch (InterruptedException e) {
					TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
					Thread.currentThread().interrupt();
				}
			}
			
			LinkedList<ComposerInfoData> composerList = (LinkedList<ComposerInfoData>)TPILogger.lc.paramObj.get(RequireAllClientListPacket.allComposerListStr);
			List<String> encodeComposerIdList = new ArrayList<>();

			//儲存composerId
			for(ComposerInfoData composerVo : composerList) {
				String composerId = composerVo.getComposerID();
				byte shab[] = SHA256Util.getSHA256(composerId.getBytes());
				String encodeComposerId = Base64Util.base64URLEncode(shab);
				encodeComposerIdList.add(encodeComposerId);
			}
			
			//若為空,代表keeper server可能有問題,所以回傳temp
			if(CollectionUtils.isEmpty(encodeComposerIdList)) {
				TPILogger.tl.error("<Keeper Server> composer value is empty, return temp composer list" );
				return getRtnTempList(false);
			}
			
			//清除temp超過10秒的資訊
			if(tempComposerLiveMap.size() > 100) {
				getRtnTempList(true);
			}
			
			return encodeComposerIdList;
		} else {
			TPILogger.tl.debug("<Keeper Server> Lost Connection, return temp composer list" );
			return getRtnTempList(false);
		}
	}
	
	private List<String> getRtnTempList(boolean isOnlyClear){
		long nowTimeMills = System.currentTimeMillis();
		List<String> rtnList = new ArrayList<>();
		List<String> removeList = new ArrayList<>();
		tempComposerLiveMap.forEach((composerId, ts)->{
			long differenceTime = nowTimeMills - ts;
			//在10秒內表示還是處於live(注意:存活秒數有改,在RequireAllClientListPacket的composer也要改)
			if(differenceTime < 10000) {
				if(!isOnlyClear) {
					byte shab[] = SHA256Util.getSHA256(composerId.getBytes());
					String encodeComposerId = Base64Util.base64URLEncode(shab);
					rtnList.add(encodeComposerId);
				}
			}else {
				//超過10秒就準備移除
				removeList.add(composerId);
			}
		});
		
		for(String composerId : removeList) {
			tempComposerLiveMap.remove(composerId);
		}
		
		return rtnList;
	}
}
