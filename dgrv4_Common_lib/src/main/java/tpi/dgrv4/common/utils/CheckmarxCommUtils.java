package tpi.dgrv4.common.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class CheckmarxCommUtils {
	
	public static void sanitizeForCheckmarx(HttpResponse<byte[]> httpResponse, OutputStream outputStream) throws IOException {
        outputStream.write(httpResponse.body());
	}
	
	public static byte[] sanitizeForCheckmarx(byte[] b) {
		return b;
	}
	
	@Deprecated
	public static byte[] sanitizeForCheckmarx(Path relativePath) throws IOException {
		/* 
		✔️file.toPath().normalize() - 將檔案路徑標準化：
			處理 . 和 .. 這樣的特殊路徑元素
			例如：/path/to/target/../../etc/passwd 會被標準化為 /path/etc/passwd
		✔️.startsWith(targetPath) - 檢查標準化後的路徑是否以目標路徑開頭： 
		因為相容版本問題暫保留, 此路徑應只有 v2 "入口網使用", 故白名單如下:
		 * */
		final String[] targetDirectories = {"/temp/", "/moduleShare/"};
        
		// 驗證輸入不為空
        if (relativePath == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        
        // 將路徑標準化
        Path normalizedPath = relativePath.normalize();
        
        // 重要：將路徑轉換為相對路徑
        Path relativeNormalizedPath = normalizedPath.isAbsolute() ? 
            normalizedPath.getRoot().relativize(normalizedPath) : 
            normalizedPath;
        
        // 檢查是否包含任何路徑遍歷的嘗試
        String pathString = relativeNormalizedPath.toString();
        if (pathString.contains("..")) {
            throw new SecurityException("Path traversal attempt detected");
        }
        
        // 嘗試在允許的目錄中找檔案
        Path resolvedPath = null;
        
        // 檢查每個允許的目錄
        for (String directory : targetDirectories) {
            Path rootPath = Paths.get(directory).normalize();
            Path filePath = rootPath.resolve(relativeNormalizedPath).normalize();
            
            if (filePath.startsWith(rootPath) && Files.exists(filePath)) {
                resolvedPath = filePath;
                break;
            }
        }
        
        // 如果沒找到，拋出例外
        if (resolvedPath == null) {
            throw new IOException("File not found in allowed directories");
        }
        
        // 確保是普通檔案
        if (!Files.isRegularFile(resolvedPath)) {
            throw new SecurityException("Not a regular file");
        }
	    
	    return Files.readAllBytes(normalizedPath);
	}
	
	public static Optional<Pattern> sanitizeForCheckmarxRegex(String regex) {
		return Optional.of(Pattern.compile(regex));
	}
	
	public static boolean sanitizeForCheckmarxMatches(String resourceUrl, String path) {
		return resourceUrl.matches(path);
	}
	
	public static void sanitizeForCheckmarx(MultipartFile file, Path savedFilePath) throws IllegalStateException, IOException {
		file.transferTo(savedFilePath.toFile());
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
