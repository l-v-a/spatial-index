package lva.spatialindex.memory;

import lva.spatialindex.storage.StorageSpace;
import lva.spatialindex.utils.Exceptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author vlitvinenko
 */
public class SegmentStorageSpace implements StorageSpace  {
    private final Path segmentsRoot;
    private final long segmentSize;
    private final List<Segment> segments = new ArrayList<>();

    public SegmentStorageSpace(String segmentsRoot, long segmentSize) {
        this.segmentsRoot = Path.of(segmentsRoot);
        this.segmentSize = segmentSize;

        Exceptions.toRuntime(() ->
            Files.createDirectories(this.segmentsRoot)
        );
    }

    @Override
    public byte[] readBytes(long pos, int size) {
        return segments.get(toSegment(pos)).readBytes(toOffset(pos), size);
    }

    @Override
    public void readBytes(long pos, byte[] buff) {
        segments.get(toSegment(pos)).readBytes(toOffset(pos), buff);
    }

    @Override
    public void writeBytes(long pos, byte[] data) {
        segments.get(toSegment(pos)).writeBytes(toOffset(pos), data);
    }

    @Override
    public long allocate(long sizeOf) {
        checkArgument(sizeOf <= segmentSize, String.format("Unable to allocate more than segment size. " +
                "Segment size: %d, sizeOf: %d", segmentSize, sizeOf));

        Segment segment = null;
        if (!segments.isEmpty()) {
            Segment current = segments.get(segments.size() - 1);
            if (current.getSize() + sizeOf <= current.getCapacity()) {
                segment = current;
            }
        }

        if (segment == null) {
            Path segmentFile = segmentsRoot.resolve(String.format("segment_%d.bin", segments.size()));
            segment = new Segment(segmentFile.toString(), segmentSize);
            segments.add(segment);
        }


        long offset = segment.allocate(sizeOf);
        return toPosition(segments.size() - 1, offset);
    }

    @Override
    public long getSize() {
        long size = 0;
        if (!segments.isEmpty()) {
            long fullCapacity = getCapacity();
            Segment current = segments.get(segments.size() - 1);
            size = fullCapacity - current.getCapacity() + current.getSize();
        }
        return size;
    }

    @Override
    public long getCapacity() {
        return segments.stream()
                .mapToLong(Segment::getCapacity).sum();
    }

    @Override
    public void clear() {
        segments.forEach(segment -> {
            try {
                segment.clear();
                Files.deleteIfExists(Path.of(segment.getFilePath()));
            } catch (Exception e) {
                e.printStackTrace(); // TODO: add logging
            }
        });

        segments.clear();

        Exceptions.toRuntime(() ->
            Files.deleteIfExists(segmentsRoot)
        );
    }

    private static long toPosition(long segment, long offset) {
        return (segment << 32) | (0xFFFF_FFFFL & offset);
    }

    private static int toSegment(long pos) {
        return (int) (pos >>> 32);
    }

    private static long toOffset(long pos) {
        return 0xFFFF_FFFFL & pos;
    }
}
