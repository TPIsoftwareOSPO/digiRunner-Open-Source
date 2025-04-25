package tpi.dgrv4.gateway.service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IKibanaService2 {

	void clearCacheMap();

	HttpResponse<byte[]> login_withUrl(String kibanaUser, String kibanaPwd) throws
    IOException, NoSuchAlgorithmException, KeyManagementException, InterruptedException;

	void login(HttpHeaders httpHeaders, String reportURL, HttpServletRequest request,
            HttpServletResponse response);
	
	void resource(HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response,
            String payload);
}
