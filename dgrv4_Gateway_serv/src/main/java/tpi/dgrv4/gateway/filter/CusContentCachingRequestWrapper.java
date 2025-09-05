package tpi.dgrv4.gateway.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class CusContentCachingRequestWrapper extends CusContentCachingRequestWrapper2 {
    private byte[] body;

    private String token;

    private Map<String, String[]> parameterMap;

    public CusContentCachingRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        super.getParameterMap(); // init cache in ContentCachingRequestWrapper
        body = super.getContentAsByteArray();// first option for application/x-www-form-urlencoded
        if (body.length == 0) {
            try {
                body = super.getInputStream().readAllBytes(); // second option for other body formats
            } catch (IOException ex) {
                body = new byte[0];
            }
        }
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getHeader(String key) {
        if ("authorization".equalsIgnoreCase(key)) {
            String authorization = super.getHeader(key);
            if (StringUtils.hasLength(authorization)) {
                return authorization;
            }
            return token;
        }
        return super.getHeader(key);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        if (StringUtils.hasLength(token)) {
            names.add("authorization");
        }
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = Collections.list(super.getHeaders(name));
        if ("authorization".equalsIgnoreCase(name)) {
            if (CollectionUtils.isEmpty(values) || values.contains("")) {
                values = Arrays.asList(token);
            }
        }
        return Collections.enumeration(values);
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new RequestCachingInputStream(body);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    private static class RequestCachingInputStream extends ServletInputStream {

        private final ByteArrayInputStream inputStream;

        public RequestCachingInputStream(byte[] bytes) {
            inputStream = new ByteArrayInputStream(bytes);
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readlistener) {
        }

    }


}