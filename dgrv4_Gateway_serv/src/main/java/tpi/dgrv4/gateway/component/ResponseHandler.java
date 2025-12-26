package tpi.dgrv4.gateway.component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.utils.CheckmarxCommUtils;

@Component
public class ResponseHandler {

    public sealed interface ResponseHandlerInterface {

        private void writeBytes(OutputStream os, byte[] bytes) throws IOException {
            int offset = 0;
            int bufferSize = 4096;
            while (offset < bytes.length) {
                int bytesToWrite = Math.min(bufferSize, bytes.length - offset);
              //checkmarx, Missing HSTS Header
                CheckmarxCommUtils.sanitizeForCheckmarxWriteBytes(os,bytes,offset,bytesToWrite);
                offset += bytesToWrite;
            }
        }

        default void write() {
            switch (this) {
                case ByteArrayPayload(HttpServletResponse response, byte[] bytes) -> {
                    response.setContentLength(bytes.length);
                    //checkmarx, Missing HSTS Header
                    response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
                    
                    try (OutputStream os = response.getOutputStream()) {
                        writeBytes(os, bytes);
                        response.flushBuffer();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                case ResponseEntityPayload(HttpServletResponse response, ResponseEntity<?> responseEntity) -> {
                    response.setStatus(responseEntity.getStatusCode().value());
                    //checkmarx, Missing HSTS Header
                    response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
                    
                    responseEntity.getHeaders().forEach((name, values) -> {
                        values.forEach(value -> response.addHeader(name, value));
                    });

                    try {
                        Object body = responseEntity.getBody();
                        if (body != null) {
                            try (OutputStream os = response.getOutputStream()) {
                                switch (body) {
                                    case byte[] bytes -> {
                                        response.setContentLength(bytes.length);
                                        writeBytes(os, bytes);
                                    }
                                    case String string -> {
                                        response.setContentLength(string.getBytes().length);
                                      //checkmarx, Missing HSTS Header
                                        CheckmarxCommUtils.sanitizeForCheckmarxWriteBytes(os,string.getBytes());
                                    }
                                    case Object object -> {
                                        ObjectMapper mapper = new ObjectMapper();
                                        mapper.writeValue(os, object);
                                    }
                                }
                            }
                        }

                        response.flushBuffer();
                    } catch (Exception e ) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public ResponseCarrier response(HttpServletResponse response) {
        return new ResponseCarrier(response);
    }

    public record ResponseCarrier(HttpServletResponse response) {

        public ResponseHandlerInterface handle(ResponseEntity<?> responseEntity) {
            return new ResponseEntityPayload(response, responseEntity);
        }

        public ResponseHandlerInterface handle(byte[] bytes) {
            return new ByteArrayPayload(response, bytes);
        }
    }

    private record ResponseEntityPayload
            (HttpServletResponse response, ResponseEntity<?> responseEntity) implements ResponseHandlerInterface
    { }

   
    private record ByteArrayPayload(HttpServletResponse response, byte[] bytes) implements ResponseHandlerInterface {
    	//Overwriting is to correct Reliability
    	
    	@Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ByteArrayPayload that = (ByteArrayPayload) obj;
            return Objects.equals(response, that.response) && 
                   Arrays.equals(bytes, that.bytes);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(response, Arrays.hashCode(bytes));
        }
        
        @Override
        public String toString() {
            return "ByteArrayPayload[" +
                   "response=" + response + 
                   ", bytes=" + Arrays.toString(bytes) + 
                   ']';
        }
    }

}
