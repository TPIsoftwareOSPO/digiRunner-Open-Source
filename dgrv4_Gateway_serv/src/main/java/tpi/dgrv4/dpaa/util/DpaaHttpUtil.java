package tpi.dgrv4.dpaa.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;

public class DpaaHttpUtil {

	private final static TPILogger logger = TPILogger.tl;

	public static final boolean isShowHeader = true; // 顯示Header ?
	public static final boolean isShowHttpCode = true; // 顯示HttpCode ?
 
	public static ResponseEntity<String> httpReqByFile(String reqUrl, String method, MultiValueMap<String, Object> formData,
			String authorization) throws IOException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.add("Authorization", authorization);
		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(formData, headers);
		
		if (isShowHeader) {
			logger.debug("Request Header:");
			request.getHeaders().forEach((k, v) ->logger.debug("\tKey: " + k + ", Value: " + v));
			logger.debug("-------------------------------------------");
		}
		// send	
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.postForEntity( reqUrl, request , String.class );
		
		if (isShowHttpCode) {
			logger.debug("Key: http code, Value: " + response.getStatusCodeValue());
		}
		
		if (isShowHeader) {
			logger.debug("Response Header:");
			response.getHeaders().forEach((k, v) ->logger.debug("\tKey: " + k + ", Value: " + v));
			
			logger.debug("Get Response Header By Key...");
			List<String> serverList = response.getHeaders().get("Server");
			
			if (serverList == null || serverList.size() == 0) {
				logger.debug("Key 'Server' is not found!");
			} else {
				logger.debug("Server - " + serverList);
			}
			logger.debug("-------------------------------------------");
		}

		return response;
	}

	/**
	 * 傳入 Request body, 由此方法加入 Request header 後再轉成 Json
	 * 
	 * @param <T>
	 * @param reqPayload
	 * @param clazz
	 * @return
	 */
	public static final <T> String toReqPayloadJson(T req, String cId) {
		String reqJson = "";

		try {
			ObjectMapper om = new ObjectMapper();
			om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

			ObjectNode root = om.createObjectNode();

			String keyOfHeader = TsmpBaseReq.KEY_OF_HEADER;
			ReqHeader reqHeader = new ReqHeader();
			reqHeader.setTxSN(genTxSN());
			reqHeader.setTxDate(genTxDate());
			reqHeader.setTxID(getTxID(req));
			reqHeader.setcID(cId);
			reqHeader.setLocale(getLocale());
			root.putPOJO(keyOfHeader, reqHeader);

//			String keyOfBody = req.getClass().getSimpleName();
//			root.putPOJO(keyOfBody, req);
			root.putPOJO("ReqBody", req);

			reqJson = om.writerWithDefaultPrettyPrinter().writeValueAsString(root);
		} catch (IOException e) {
			logger.debug("" + e);
		}

		return reqJson;
	}

	/**
	 * 格式為 ServerNo(1) + Date(yyMMddHHmmss) + AlphaNumber(6) ex.
	 * "1180823173301000001" (不可重複) (ServerNo + 2018/8/23 17:33:01 + 最後6碼可英數字)
	 */
	private static final String genTxSN() {
		String txSN = "1";

		txSN += new SimpleDateFormat("yyMMddHHmmss"). //
				format(Calendar.getInstance().getTime());

		// TODO REVIEW 需產生每次測試皆唯一的值
		txSN += "000001";

		return txSN;
	}

	/**
	 * 格式為 "YYYYMMDDThhmmssTZD", ex. "20180812T173301+0800"
	 */
	private static final String genTxDate() {
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String txDate = sdf.format(now.getTime());

		sdf = new SimpleDateFormat("hhmmss");
		txDate += "T" + sdf.format(now.getTime());

		int offset = now.get(Calendar.ZONE_OFFSET) / 1000 / 60 / 60;
		int dst = now.get(Calendar.DST_OFFSET);
		txDate += (String.format("%+03d", offset)) + //
				(String.format("%02d", dst));

		return txDate;
	}

	private static final <T> String getTxID(T req) {
		String requestBodyClassName = req.getClass().getSimpleName();
		Pattern pattern = Pattern.compile("(DPF\\d{4})Req");
		Matcher matcher = pattern.matcher(requestBodyClassName);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return requestBodyClassName;
	}

	/**
	 * 語言地區 ex. "zh-TW"
	 * 
	 * @return
	 */
	private static final String getLocale() {
		return "zh-TW";
	}
 
	public static String byte2Hex(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++)
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		return result;
	}
}
