package tpi.dgrv4.entity.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.math.BigInteger;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import tpi.dgrv4.common.constant.TsmpDpDBMSType;
import tpi.dgrv4.common.utils.ServiceUtil;


@ConditionalOnProperty(name = TsmpDpDBMSType.KEY //
		, havingValue = TsmpDpDBMSType.H2)
@Repository
public class TsmpSequenceDao_H2 implements TsmpSequenceDao {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Long nextSequence(String sequenceName, Long initial, Long increment) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT NEXT VALUE FOR " + sequenceName);

		Query query = getEm().createNativeQuery(sb.toString());
		Object seq = query.getSingleResult();
		Long result = ServiceUtil.parseSequenceToLong(seq);

		return result;
	}

	@Override
	public boolean checkSequence(String sequenceName) {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT COUNT(1)");
		sb.append(" FROM information_schema.sequences");
		sb.append(" WHERE sequence_name = :name");

		Query query = getEm().createNativeQuery(sb.toString());
		query.setParameter("name", sequenceName);
		final Long cnt = ((Number) query.getSingleResult()).longValue();

		return (cnt != null && cnt > 0);
	}

	protected EntityManager getEm() {
		return em;
	}

}
