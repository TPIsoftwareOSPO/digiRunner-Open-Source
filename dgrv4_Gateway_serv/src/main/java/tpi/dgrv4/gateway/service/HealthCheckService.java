package tpi.dgrv4.gateway.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.concurrent.CompletableFuture;

@Service
public class HealthCheckService {

    private static final String LIVENESS_MSG_TEMPLATE = "dgrv4 .... alive !\n</br> %s... dgR-linkerClient-Node-Name\n</br>%s... httpReq.getRemoteAddr()\n</br>";

    @Async("async-workers-healthcheck")
    public CompletableFuture<ResponseEntity<?>> liveness(HttpServletRequest httpReq) {

        String msg = String.format(LIVENESS_MSG_TEMPLATE, TPILogger.lcUserName, httpReq.getRemoteAddr());

        return CompletableFuture.completedFuture(new ResponseEntity<>(msg, HttpStatus.OK));
    }
}
