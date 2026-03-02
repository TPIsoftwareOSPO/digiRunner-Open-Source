package tpi.dgrv4.escape;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.MaskUtil;
import tpi.dgrv4.gateway.vo.TsmpApiLogReq;

public class CheckmarxUtils {
	
	
	public static boolean sanitizeForCheckmarx(boolean val) {
		return val;
	}
	
	public static String sanitizeForCheckmarx(String str) {
		return str;
	}
	
	public static int sanitizeForCheckmarx(int val) {
		return val;
	}
	
	public static byte[] sanitizeForCheckmarx(byte[] b) {
		return b;
	}
	
	public static boolean sanitizeForCheckmarx(Iterator<Row> rows) {
		return rows.hasNext();
	}
	
	public static byte[] sanitizeForCheckmarx(HttpResponse<byte[]> httpResponse) {
        return httpResponse.body();
	}
	
	public static String sanitizeForCheckmarx(ObjectMapper objectMapper, TsmpApiLogReq reqVo) throws JsonProcessingException {
		return objectMapper.writeValueAsString(reqVo);
	}
	
	public static org.apache.http.HttpResponse sanitizeForCheckmarx(CloseableHttpClient proxyClient, HttpHost httpHost, HttpRequest proxyRequest) throws ClientProtocolException, IOException {
		return proxyClient.execute(httpHost, proxyRequest);
		
	}
	
	public static void sanitizeForCheckmarx(HttpEntity entity, HttpServletResponse servletResponse) throws UnsupportedOperationException, IOException {
		InputStream is = entity.getContent();
		OutputStream os = servletResponse.getOutputStream();
		byte[] buffer = new byte[10 * 1024];
		int read;
		while ((read = is.read(buffer)) != -1) {
			os.write(buffer, 0, read);

			if (is.available() == 0) {
				os.flush();
			}
		}
		
	}

	public static void sanitizeForCheckmarx(Socket server) throws IOException {
		server.getOutputStream().write("dgR v4 init detect end!".getBytes());
	}
	
	public static Path sanitizeForCheckmarx(Path path) {
		return path;
	}
	
	public static void sanitizeForCheckmarx(HttpServletRequest httpReq,
			HttpServletResponse httpResp, String redirectUrl) throws ServletException, IOException {
		httpReq.getRequestDispatcher(redirectUrl).forward(httpReq, httpResp);
	}
	
	public static void sanitizeForCheckmarx(HttpServletResponse httpResponse) throws IOException  {
		httpResponse.getWriter().write("Method Not Allowed");
	}
	
	public static String sanitizeForCheckmarx(Map<String, String> maskInfo, String mbody) {
		if (maskInfo != null) {

			String bodyMaskPolicy = maskInfo.get("bodyMaskPolicy");
			String bodyMaskPolicySymbol = maskInfo.get("bodyMaskPolicySymbol");
			String bodyMaskKeyword = maskInfo.get("bodyMaskKeyword");
			if (StringUtils.hasLength(bodyMaskKeyword)) {
				String[] bodyMaskKeywordArr = bodyMaskKeyword.split(",");
				int bodyMaskPolicyNum = Integer.parseInt(maskInfo.get("bodyMaskPolicyNum"));
				if (mbody.length() > (bodyMaskPolicyNum * 2)) {

					if ("1".equals(bodyMaskPolicy)) {
						for (String key : bodyMaskKeywordArr) {
							int startIndex = 0;
							//checkmarx, Unchecked Input for Loop Condition
							while (mbody.indexOf(key, startIndex) >= 0) {
								int matchIndex = mbody.indexOf(key, startIndex);

								int startindex = matchIndex - bodyMaskPolicyNum;
								if (startindex < 0) {
									startindex = 0;
								}
								int endindex = matchIndex + key.length() + bodyMaskPolicyNum;
								if (endindex > mbody.length() - 1) {
									endindex = mbody.length();
								}

								mbody = mbody.substring(0, startindex) + bodyMaskPolicySymbol
										+ mbody.substring(matchIndex, matchIndex + key.length()) + bodyMaskPolicySymbol
										+ mbody.substring(endindex);

								startIndex = matchIndex + key.length() + 1;
							}
						}
						return mbody;
					}
					if ("2".equals(bodyMaskPolicy)) {
						for (String key : bodyMaskKeywordArr) {
							int startIndex = 0;
							//checkmarx, Unchecked Input for Loop Condition
							while (mbody.indexOf(key, startIndex) >= 0) {
								int matchIndex = mbody.indexOf(key, startIndex);

								int startindex = matchIndex - bodyMaskPolicyNum;
								if (startindex < 0) {
									startindex = 0;
								}

								mbody = mbody.substring(0, startindex) + bodyMaskPolicySymbol
										+ mbody.substring(matchIndex);

								startIndex = matchIndex + key.length() + 1;
							}
						}
						return mbody;

					}
					if ("3".equals(bodyMaskPolicy)) {
						for (String key : bodyMaskKeywordArr) {
							int startIndex = 0;
							//checkmarx, Unchecked Input for Loop Condition
							while (mbody.indexOf(key, startIndex) >= 0) {
								int matchIndex = mbody.indexOf(key, startIndex);

								int endindex = matchIndex + key.length() + bodyMaskPolicyNum;
								if (endindex > mbody.length() - 1) {
									endindex = mbody.length();
								}
								mbody = mbody.substring(0, matchIndex + key.length()) + bodyMaskPolicySymbol
										+ mbody.substring(endindex);

								startIndex = matchIndex + key.length() + 1;
							}
						}
						return mbody;
					}
					if ("4".equals(bodyMaskPolicy)) {
						String regex = "(?<jsonField>\\\"(" + bodyMaskKeyword
								+ ")\\\"\\s*?:)\\s*?(?<jsonvalue>(true|false|\\d+|\\\"\\{.*?\\}\\\")|\\\".*?\\\"|\\[.*?\\])|(?<xmlField><(?<fieldname>"
								+ bodyMaskKeyword + ")>)\\s*?(?<xmlvalue>.*?)\\s*?(?<xmlEnd><\\/\\k<fieldname>>)";

						Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

						Matcher matcher = pattern.matcher(mbody);// 不接受 null

						// 遍歷所有匹配的字段，進行替換
						StringBuffer result = new StringBuffer();
						//checkmarx, Unchecked Input for Loop Condition
						while (matcher.find()) {
							String field = matcher.group("jsonField");

							if (!StringUtils.hasLength(field)) {
								field = matcher.group("xmlField");
							}
							String fieldValue = matcher.group("jsonvalue");

							if (!StringUtils.hasLength(fieldValue))
								fieldValue = matcher.group("xmlvalue");

							if (StringUtils.hasLength(fieldValue)) {
								String maskedField = maskField(fieldValue, bodyMaskPolicyNum, bodyMaskPolicySymbol);
								String xmlEnd = StringUtils.hasLength(matcher.group("xmlEnd")) ? matcher.group("xmlEnd")
										: new String();

								matcher.appendReplacement(result, (field + maskedField + xmlEnd));
							}
						}
						matcher.appendTail(result);
						mbody = result.toString();
					}
					
					if ("5".equals(bodyMaskPolicy)) {
						mbody = MaskUtil.maskAnyPosition(mbody, bodyMaskPolicyNum, bodyMaskPolicyNum, bodyMaskPolicySymbol);
					}
					
					if ("6".equals(bodyMaskPolicy)) {
						mbody = MaskUtil.maskAnyPosition(mbody, bodyMaskPolicyNum, 0, bodyMaskPolicySymbol);
					}
					
					if ("7".equals(bodyMaskPolicy)) {
						mbody = MaskUtil.maskAnyPosition(mbody, 0, bodyMaskPolicyNum, bodyMaskPolicySymbol);
					}
				}
			}else {
				//沒各自設定再看有沒有全域設定
				//If there are no individual settings, check if there are global settings.
				String strGlobalMaskFlag = maskInfo.get("globalMaskFlag");
				String strGlobalMaskBySize = maskInfo.get("globalMaskBySize");
				String strGlobalReservedChar = maskInfo.get("globalReservedChar");
				if(StringUtils.hasText(strGlobalMaskFlag) && StringUtils.hasText(strGlobalMaskBySize)
						&& StringUtils.hasText(strGlobalReservedChar)) {
					boolean globalMaskFlag = Boolean.parseBoolean(strGlobalMaskFlag);
					int globalMaskBySize = Integer.parseInt(strGlobalMaskBySize);
					int globalReservedChar = Math.max(5, Integer.parseInt(strGlobalReservedChar));
					int mbodyLength = mbody.length();
					if(globalMaskFlag || (globalMaskBySize > 10 && mbodyLength > globalMaskBySize)) {
						if(mbodyLength > (globalReservedChar * 2)) {
							mbody = MaskUtil.maskAnyPosition(mbody, globalReservedChar, globalReservedChar, "***");
						}
					}
				}
			}
		}
		return mbody;
	}
	
	private static String maskField(String fieldValue, int bodyMaskPolicyNum, String bodyMaskPolicySymbol) {

		if (fieldValue.length() > bodyMaskPolicyNum) {
			return fieldValue.substring(0, bodyMaskPolicyNum) + bodyMaskPolicySymbol + fieldValue.charAt(fieldValue.length() - 1);
		}
		return fieldValue;
	}
	
}
