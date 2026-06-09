package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy;

@Repository
public interface DgrSmartOnFhirProxyDao extends JpaRepository<DgrSmartOnFhirProxy, Long>, DgrSmartOnFhirProxySuperDao {

    /**
     * 根據 Proxy 名稱查詢
     * 
     * @param sofProxyName Proxy 名稱
     * @return 符合條件的 Proxy 列表
     */
    List<DgrSmartOnFhirProxy> findBySofProxyName(String sofProxyName);
}
