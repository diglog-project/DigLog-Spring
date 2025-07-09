package api.store.diglog.repository;

import java.util.Map;
import java.util.UUID;

public interface PostViewBatchRepository {

	void bulkUpdateViewCounts(Map<UUID, Long> viewCounts);

}
