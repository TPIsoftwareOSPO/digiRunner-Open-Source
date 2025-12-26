package tpi.dgrv4.gateway.ifs;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tpi.dgrv4.common.keeper.IHttpUtilLogHelper;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@Service
public class HttpUtilLogHelper implements IHttpUtilLogHelper {
    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private TsmpSettingService service;

    @PostConstruct
    public void init() {
        IHttpUtilLogHelper.ConfigHelper.h1.set(this);
    }

    @Override
    public boolean isLogResponseTime() {
        if (getService() == null) {
            return false; // 或者回傳一個安全的預設值
        }
        return getService().getVal_HTTPUTIL_RESP_TIME_LOG();
    }
}
