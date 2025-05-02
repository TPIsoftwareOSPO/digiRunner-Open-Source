package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.ExpireKeyUtil;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController

public class SeqLongIdController {
	TPILogger logger = TPILogger.tl;

	@GetMapping(value = "/dgrv4/seqlongid" , produces = "text/plain")
	public ResponseEntity<?> createSeqlongId(
			HttpServletRequest req
			) throws Exception {
		
		int num = -1;
		String sid = null;
		String capikeyExpireTime = null;
		
		Map<String, String[]> reqMap = req.getParameterMap();
		if (reqMap.size() > 0 ) {
			if (reqMap.containsKey("num")) {
				num = Integer.parseInt(reqMap.get("num")[0]);
			}
			if (reqMap.containsKey("sid")) {
				sid = reqMap.get("sid")[0];
			}
			if (reqMap.containsKey("capikeyExpireTime")) {
				capikeyExpireTime = reqMap.get("capikeyExpireTime")[0];
			}
		}
		

		// 沒有任何參數, 產生一個 seq long id
		if (sid == null && num == -1 && capikeyExpireTime == null) {
			//印出狀態
			String respText = "\n";
			respText += "[Gateway Name] = " + TPILogger.lc.param.get(TPILogger.nodeInfo) + "\n";
			respText += "[Query String] = {num, sid, capikeyExpireTime}\n";
			respText += "=====================================\n";
			respText += "num= Output {num} numbers in a row\n";
			respText += "sid= LongId and DateString format conversion. \n";
			respText += "=====================================\n";
			respText += "one longid =\n";
			respText += RandomSeqLongUtil.getRandomLongByDefault();
			return new ResponseEntity<Object>(respText, HttpStatus.OK);
		}
		
		
		// 產生 100 個 seq long id
		if (num != -1) {
			List<Long> list = new ArrayList<>();
			if (num > 100) {
				num = 100;
			}

			if (num > 0 && num <= 100) {
				for (int i = 0; i < num; i++) {
					list.add(Long.valueOf(RandomSeqLongUtil.getRandomLongByDefault()));
				}
				String respTxt = list.toString();
				return new ResponseEntity<Object>(respTxt, HttpStatus.OK);
			}
			return new ResponseEntity<Object>("num Error", HttpStatus.BAD_REQUEST);
		}

		// Transformation
		if (sid != null) {
			if (StringUtils.isNotBlank(sid) && sid.contains("_")==false) {
				String dateStr = null;
				dateStr = RandomSeqLongUtil.toHexString(Long.valueOf(sid), RandomLongTypeEnum.YYYYMMDD);
				return new ResponseEntity<Object>(dateStr, HttpStatus.OK); //轉日期字串
			}
			
			if (StringUtils.isNotBlank(sid) && sid.contains("_") && sid.length()==17) {
				long longid = RandomSeqLongUtil.toLongValue(sid);
				String respTxt = longid + "";
				return new ResponseEntity<Object>(respTxt, HttpStatus.OK); //轉為 Long
			}
			
			return new ResponseEntity<Object>("id Error", HttpStatus.BAD_REQUEST);
		}
		
		// ExpireKey JWS String
		if (capikeyExpireTime != null) {
			long expireTime = Long.parseLong(capikeyExpireTime);
			String jws = ExpireKeyUtil.getExpireKey(expireTime);
			return new ResponseEntity<Object>(jws, HttpStatus.OK); 
		}
		
		return new ResponseEntity<Object>("TPI Error", HttpStatus.BAD_REQUEST);
	}

}
