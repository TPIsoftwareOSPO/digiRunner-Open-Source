package tpi.dgrv4.entity.repository;

import java.math.BigInteger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.constant.TsmpDpDBMSType;
import tpi.dgrv4.common.utils.ServiceUtil;

@ConditionalOnProperty(name = TsmpDpDBMSType.KEY //
, havingValue = TsmpDpDBMSType.DB2)
@Repository
public class TsmpSequenceDao_DB2 implements TsmpSequenceDao{
	@PersistenceContext
	private EntityManager em;

	@Override
	public Long nextSequence(String sequenceName, Long initial, Long increment) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT NEXT VALUE FOR " + sequenceName + " FROM SYSIBM.SYSDUMMY1");

		Query query = getEm().createNativeQuery(sb.toString());
		Object seq = query.getSingleResult();
		return ServiceUtil.parseSequenceToLong(seq);
	}

	@Override
	public boolean checkSequence(String sequenceName) {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT COUNT(1)");
		sb.append(" FROM SYSIBM.SYSSEQUENCES");
		sb.append(" WHERE SEQNAME = :name");

		Query query = getEm().createNativeQuery(sb.toString());
		query.setParameter("name", sequenceName);
		final BigInteger cnt = (BigInteger) query.getSingleResult();

		final BigInteger zero = new BigInteger("0");
		return (cnt != null && cnt.compareTo(zero) > 0);
	}

	protected EntityManager getEm() {
		return em;
	}
}
