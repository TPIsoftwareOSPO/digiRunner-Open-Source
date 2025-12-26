package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpApiExt;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExtId;

@Repository
public interface TsmpApiExtDao extends JpaRepository<TsmpApiExt, TsmpApiExtId> {

	public TsmpApiExt queryByApiUid(String apiUid);

	public List<TsmpApiExt> findByCreateUser(String createUser);

}