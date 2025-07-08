package tpi.dgrv4.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tpi.dgrv4.entity.entity.DgrGrpcProxyMap;

import java.util.List;

public interface DgrGrpcProxyMapDao extends JpaRepository<DgrGrpcProxyMap, Long>, DgrGrpcProxyMapSuperDao {
    public List<DgrGrpcProxyMap> findByCreateUser(String createUser);
    public List<DgrGrpcProxyMap> findByServiceNameContainingIgnoreCaseOrProxyHostNameContainingIgnoreCaseOrTargetHostNameContainingIgnoreCase(String serviceName, String proxyHostName, String targetHostname);
    public int deleteDgrGrpcProxyMapByGrpcproxyMapId(long grpcproxyMapId);
    public DgrGrpcProxyMap findById(long dgrGrpcProxyMapId);
    public DgrGrpcProxyMap findByProxyHostNameAndTargetHostNameAndTargetPort(String proxyHostName, String targetHostname, int targetPort);
    public List<DgrGrpcProxyMap> findByEnable(String enable);
}
