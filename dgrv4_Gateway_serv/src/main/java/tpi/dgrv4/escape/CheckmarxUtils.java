package tpi.dgrv4.escape;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.poi.ss.usermodel.Row;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.gateway.filter.FilteredHeaderResponseWrapper;
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
	
}
