package api.store.diglog.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public class BatchPartition<T> {

	private final List<List<T>> batches;

	private BatchPartition(List<List<T>> source) {
		this.batches = source;
	}

	public static <T> BatchPartition<T> of(List<T> source, int batchSize) {
		if (source == null || source.isEmpty()) {
			return new BatchPartition<>(List.of());
		}

		List<List<T>> batches = new ArrayList<>();
		for (int i = 0; i < source.size(); i += batchSize) {
			batches.add(source.subList(i, Math.min(i + batchSize, source.size())));
		}

		return new BatchPartition<>(batches);
	}

	public Stream<List<T>> stream() {
		return batches.stream();
	}
}
