package tpi.dgrv4.entity.component;

import java.io.IOException;
import java.security.KeyPair;

public interface ITsmpCoreTokenHelperCacheProxy {

	KeyPair deserializeKeyPair(byte[] content);
}
