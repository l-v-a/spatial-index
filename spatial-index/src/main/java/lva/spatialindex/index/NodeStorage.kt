package lva.spatialindex.index;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lva.spatialindex.memory.SegmentStorageSpace;
import lva.spatialindex.storage.AbstractStorage;
import lva.spatialindex.storage.Storage;
import lva.spatialindex.storage.StorageSpace;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author vlitvinenko
 */
class NodeStorage extends AbstractStorage<Node> {

    static class NodeSerializer extends AbstractSerializer<Node> {
        private final Kryo kryo = new Kryo();

        NodeSerializer(Storage<Node> storage) {
            kryo.addDefaultSerializer(Node.class, new Node.Ser(storage));
            kryo.addDefaultSerializer(Entry.class, new Entry.Ser(storage));
        }

        @Override
        public void write(OutputStream outputStream, @NotNull Node node) {
            try (Output output = new Output(outputStream)) {
                kryo.writeObject(output, node);
                output.flush();
            }
        }

        @Override
        public @NotNull Node read(InputStream in) {
            try (Input input = new Input(in)) {
                return kryo.readObject(input, Node.class);
            }
        }
    }

    static final int RECORD_SIZE = 4096 * 2;

    private final Serializer<Node> serializer;
    private final LoadingCache<Long, Node> cache;

    NodeStorage(String fileName, long initialSize) {
        this(new SegmentStorageSpace(fileName, initialSize), RECORD_SIZE);
    }

    NodeStorage(StorageSpace storageSpace) {
        this(storageSpace, RECORD_SIZE);
    }

    private NodeStorage(StorageSpace storageSpace, int recordSize) {
        super(storageSpace, recordSize);
        cache = CacheBuilder.newBuilder()
                .softValues()
//                .maximumSize(100)
                //.expireAfterWrite(10, TimeUnit.MINUTES)
                .build(new CacheLoader<Long, Node>() {
                    @Override
                    public Node load(@Nonnull Long offset) {
                        return NodeStorage.this.load(offset);
                    }
                });
        this.serializer = new NodeSerializer(this);
    }

    @Override
    protected @NotNull Serializer<Node> getSerializer() {
        return serializer;
    }

    @Override
    public long add(@NotNull Node node) {
        long offset = super.add(node);
        node.setOffset(offset);
        cache.put(offset, node);
        return offset;
    }

    @Override
    public void write(long offset, @NotNull Node node) {
        super.write(offset, node);
    }

    @Override
    public @NotNull Node read(long offset) {
        return cache.getUnchecked(offset);
    }

    private Node load(long offset) {
        Node node = super.read(offset);
        node.setOffset(offset);
        return node;
    }
}
