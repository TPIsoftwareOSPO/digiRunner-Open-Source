package tpi.dgrv4.entity.repository;

public interface TsmpSequenceDao {

	public default Long nextSequence(String sequenceName) {
		return nextSequence(sequenceName, 2000000000L, 1L);
	}

	public Long nextSequence(String sequenceName, Long initial, Long increment);

	public boolean checkSequence(String sequenceName);

}