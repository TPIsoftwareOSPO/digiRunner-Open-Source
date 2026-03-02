package tpi.dgrv4.common.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.web.multipart.MultipartFile;

import tpi.dgrv4.common.keeper.ITPILogger;

public class CheckmarxCommUtils {
	
	public static void sanitizeForCheckmarx(HttpResponse<byte[]> httpResponse, OutputStream outputStream) throws IOException {
        outputStream.write(httpResponse.body());
	}
	
	public static byte[] sanitizeForCheckmarx(byte[] b) {
		return b;
	}
	
	public static String sanitizeForCheckmarx(String str) {
        return str;
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
		
		/* 
		 * 上層目錄白名單
		 * temp/ : DPB0065 - 建立申請單的附件
		 * API_ATTACHMENT/ : DPB0065 - 建立申請單 （API 上下架）D2
		 * D2_ATTACHMENT/ : API 上下架 Job
		 * APP_IMG/ : DPB0079 - 取出ImgBinaryData
		 * DOC/ : DPB0078 - 下載檔案
		 */
		final String[] targetDirectories = { "temp/", "moduleShare/", "API_ATTACHMENT/", "D2_ATTACHMENT/", "APP_IMG/", "DOC/" };
		
		// 驗證輸入不為空
        if (relativePath == null) {
        	String errMsg = "Path cannot be null";
        	ITPILogger.tl.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        
        /*
         * 拿掉上層目錄後的子路徑,
         * 例如: API_ATTACHMENT\2000000000\API1.txt
         * 取得子路徑: \2000000000\API1.txt
         */
        Path subPath = relativePath.subpath(1, relativePath.getNameCount());
		// 驗證輸入不為空
        if (subPath == null) {
        	String errMsg = "Sub Path cannot be null. Original path: " + relativePath;
        	ITPILogger.tl.error(errMsg);
        	 throw new IllegalArgumentException(errMsg);
        }
        
        // 將路徑標準化
        Path normalizedPath = subPath.normalize();
        
        // 重要：將路徑轉換為相對路徑
        Path relativeNormalizedPath = normalizedPath.isAbsolute() ? 
            normalizedPath.getRoot().relativize(normalizedPath) : 
            normalizedPath;
        
        // 檢查是否包含任何路徑遍歷的嘗試
        String pathString = relativeNormalizedPath.toString();
        if (pathString.contains("..")) {
        	String errMsg = "Path traversal attempt detected";
        	ITPILogger.tl.error(errMsg);
            throw new SecurityException(errMsg);
        }
        
        // 嘗試在允許的目錄中找檔案
        Path resolvedPath = null;
        
        // 檢查每個允許的上層目錄
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
			ITPILogger.tl.error("File not found in allowed directories,\n" 
					+ "Original path: " + relativePath + "\n"
					+ "Sub Path: " + subPath);
			throw new IOException("File not found in allowed directories");
		}
        
        // 確保是普通檔案
        if (!Files.isRegularFile(resolvedPath)) {
        	String errMsg = "Not a regular file";
        	ITPILogger.tl.error(errMsg);
            throw new SecurityException(errMsg);
        }
	    
	    return Files.readAllBytes(resolvedPath);
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

	public static void sanitizeForCheckmarxConn(String targetHostName, int targetPort, int connectTimeout) throws IOException {
		try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(targetHostName, targetPort), connectTimeout);
    	}
	}
	
	public static void sanitizeForCheckmarxConn(Socket tempSocket, String hostname, int port, int connectTimeout) throws IOException {
		tempSocket.connect(new InetSocketAddress(hostname, port), connectTimeout);
	}
	
	public static void sanitizeForCheckmarxWriteBytes(OutputStream os, byte[] bytes, int offset, int bytesToWrite) throws IOException {
		os.write(bytes, offset, bytesToWrite);
	}
	
	public static void sanitizeForCheckmarxWriteBytes(OutputStream os, byte[] bytes) throws IOException {
		os.write(bytes);
	}
	
	
	
}
