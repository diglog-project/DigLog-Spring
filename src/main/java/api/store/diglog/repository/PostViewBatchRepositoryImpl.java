package api.store.diglog.repository;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class PostViewBatchRepositoryImpl implements PostViewBatchRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	public void bulkUpdateViewCounts(Map<UUID, Long> viewCounts) {
		if (viewCounts.isEmpty()) {
			return;
		}

		String sql = buildUpdateSql(viewCounts.size());
		Query query = entityManager.createNativeQuery(sql);

		int paramIndex = 1;
		for (Map.Entry<UUID, Long> entry : viewCounts.entrySet()) {
			query.setParameter(paramIndex++, entry.getKey());
			query.setParameter(paramIndex++, entry.getValue());
		}

		// IN 절의 파라미터들
		for (UUID uuid : viewCounts.keySet()) {
			query.setParameter(paramIndex++, uuid);
		}

		query.executeUpdate();
	}

	private String buildUpdateSql(int size) {
		StringBuilder sql = new StringBuilder("UPDATE post SET view_count = CASE id ");

		for (int i = 0; i < size; i++) {
			sql.append("WHEN ? THEN ? ");
		}

		sql.append("END WHERE id IN (");
		for (int i = 0; i < size; i++) {
			if (i > 0) sql.append(",");
			sql.append("?");
		}
		sql.append(")");

		return sql.toString();
	}
}