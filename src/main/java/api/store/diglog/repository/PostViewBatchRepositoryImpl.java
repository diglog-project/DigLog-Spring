package api.store.diglog.repository;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class PostViewBatchRepositoryImpl implements PostViewBatchRepository {

	private static final String SQL_UPDATE_VIEW_COUNT_PREFIX = "UPDATE post SET view_count = CASE id ";
	private static final String SQL_WHEN_CLAUSE_FORMAT = "WHEN '%s' THEN '%d' ";
	private static final String SQL_UPDATE_VIEW_COUNT_SUFFIX = "END WHERE id IN (%s);";
	private static final String SQL_STRING_WRAPPER = "'";
	private static final String SQL_IN_CLAUSE_DELIMITER = ",";

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	public void bulkUpdateViewCounts(Map<UUID, Long> viewCounts) {
		if (viewCounts.isEmpty()) {
			return;
		}

		String sql = makeUpdateViewCountSql(viewCounts);
		entityManager.createNativeQuery(sql).executeUpdate();
	}

	private String makeUpdateViewCountSql(Map<UUID, Long> viewCounts) {
		StringBuilder sql = new StringBuilder(SQL_UPDATE_VIEW_COUNT_PREFIX);
		for (Map.Entry<UUID, Long> entry : viewCounts.entrySet()) {
			sql.append(String.format(SQL_WHEN_CLAUSE_FORMAT, entry.getKey(), entry.getValue()));
		}
		String idList = viewCounts.keySet().stream()
			.map(uuid -> SQL_STRING_WRAPPER + uuid + SQL_STRING_WRAPPER)
			.collect(Collectors.joining(SQL_IN_CLAUSE_DELIMITER));
		sql.append(String.format(SQL_UPDATE_VIEW_COUNT_SUFFIX, idList));
		return sql.toString();
	}
}
