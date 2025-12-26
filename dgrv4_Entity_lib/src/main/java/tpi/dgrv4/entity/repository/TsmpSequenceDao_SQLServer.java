package tpi.dgrv4.entity.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import tpi.dgrv4.common.constant.TsmpDpDBMSType;
import tpi.dgrv4.common.utils.ServiceUtil;

import java.math.BigInteger;

@ConditionalOnProperty(name = TsmpDpDBMSType.KEY //
		, havingValue = TsmpDpDBMSType.SQLServer)
@Repository
public class TsmpSequenceDao_SQLServer implements TsmpSequenceDao {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Long nextSequence(String sequenceName, Long initial, Long increment) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT NEXT VALUE FOR " + sequenceName);
		Query query = em.createNativeQuery(sb.toString());

		Object seq = query.getSingleResult();
		Long result = ServiceUtil.parseSequenceToLong(seq);
		
        return result;
    }

	@Override
	public boolean checkSequence(String sequenceName) {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT COUNT(1)");
		sb.append(" FROM sys.sequences");
		sb.append(" WHERE name = :name");
		sb.append(" AND type_desc = :typeDesc");

		Query query = em.createNativeQuery(sb.toString());
		query.setParameter("name", sequenceName);
		query.setParameter("typeDesc", "SEQUENCE_OBJECT");
		final Integer cnt = (Integer) query.getSingleResult();

		return (cnt != null && cnt > 0);
	}

}
