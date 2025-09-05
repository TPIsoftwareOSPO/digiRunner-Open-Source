package tpi.dgrv4.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tpi.dgrv4.entity.entity.DgrMtlsClientCert;

@Repository
public interface DgrMtlsClientCertDao extends JpaRepository<DgrMtlsClientCert, Long> ,DgrMtlsClientCertSuperDao{
    public  DgrMtlsClientCert findByHostAndPort(String host, int port);
    public    boolean existsByHostAndPort(String host, int port);
    public   DgrMtlsClientCert findByHostAndPortAndEnable(String host, int port, String enable);
}
