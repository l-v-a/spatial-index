package lva.spatialindex;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author vlitvinenko
 */
// TODO: rename to IndexStorage
class NodeStorage extends AbstractStorage<Node> {
    private static final int RECORD_SIZE = 4096;

    private class NodeSerializer implements Serializer<Node> {
        @Override
        public byte[] serialize(Node node) throws IOException {
            return node.serialize();
        }

        @Override
        public Node deserialize(byte[] bytes) throws IOException {
            return new Node(NodeStorage.this, -1)
                .deserialize(bytes);
        }
    };

    private final NodeSerializer serializer;
    private final LoadingCache<Long, Node> cache;

    public NodeStorage(String fileName, long initialSize) throws Exception {
        super(fileName, initialSize, RECORD_SIZE);
        serializer = new NodeSerializer();
        cache = CacheBuilder.newBuilder()
            .softValues()
            //.maximumSize(10000)
            //.expireAfterWrite(10, TimeUnit.MINUTES)
            .build(
                new CacheLoader<Long, Node>() {
                    @Override
                    public Node load(@Nonnull Long offset) throws Exception {
                        return read(offset);
                    }
                });
    }

    @Override
    Serializer<Node> getSerializer() {
        return serializer;
    }

    @Override
    public long add(Node node) throws Exception {
        long offset = super.add(node);
        node.setOffset(offset);
        cache.put(offset, node);
        return offset;
    }

    @Override
    public Node read(long offset) throws Exception {
        Node node = super.read(offset);
        node.setOffset(offset);
        return node;
    }

    @Override
    public Node get(long offset) throws ExecutionException {
        return cache.get(offset);
    }

    public Node newNode() throws Exception {
        Node node = new Node(this, -1);
        add(node);
        return node;
    }
}
