package tpi.dgrv4.gateway.service;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

public interface I302 {

	void sendRedirect(HttpServletResponse httpResp, String redirectUrl) throws IOException;

}
