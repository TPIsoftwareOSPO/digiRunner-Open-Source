package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxyDiversion;

@Repository
public interface DgrSmartOnFhirProxyDiversionDao
        extends JpaRepository<DgrSmartOnFhirProxyDiversion, Long>, DgrSmartOnFhirProxyDiversionSuperDao {

    /**
     * 根據 sofProxyId 查詢 Diversion 資料
     * 
     * @param sofProxyId Proxy ID
     * @return Diversion 列表
     */
    List<DgrSmartOnFhirProxyDiversion> findBySofProxyId(Long sofProxyId);

    /**
     * 根據多個 sofProxyId 批次查詢 Diversion 資料
     * 
     * @param sofProxyIds Proxy ID 列表
     * @return Diversion 列表
     */
    List<DgrSmartOnFhirProxyDiversion> findBySofProxyIdIn(List<Long> sofProxyIds);

}
