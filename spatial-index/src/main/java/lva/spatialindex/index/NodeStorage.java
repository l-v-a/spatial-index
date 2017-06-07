package lva.spatialindex.index;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lva.spatialindex.AbstractStorage;

import javax.annotation.Nonnull;

/**
 * @author vlitvinenko
 */
// TODO: rename to IndexStorage
class NodeStorage extends AbstractStorage<Node> {
    private static final int RECORD_SIZE = 4096;

    private class NodeSerializer implements Serializer<Node> {
        @Override
        public byte[] serialize(Node node) {
            return node.serialize();
        }

        @Override
        public Node deserialize(byte[] bytes) {
            return Node.newNode(NodeStorage.this)
                .deserialize(bytes);
        }
    };

    private final NodeSerializer serializer;
    private final LoadingCache<Long, Node> cache;

    public NodeStorage(String fileName, long initialSize) {
        super(fileName, initialSize, RECORD_SIZE);
        serializer = new NodeSerializer();
        cache = CacheBuilder.newBuilder()
            .softValues()
            //.maximumSize(10000)
            //.expireAfterWrite(10, TimeUnit.MINUTES)
            .build(
                new CacheLoader<Long, Node>() {
                    @Override
                    public Node load(@Nonnull Long offset) {
                        return read(offset);
                    }
                });
    }

    @Override
    protected Serializer<Node> getSerializer() {
        return serializer;
    }

    @Override
    public long add(Node node) {
        long offset = super.add(node);
        node.setOffset(offset);
        cache.put(offset, node);
        return offset;
    }

    @Override
    public Node read(long offset) {
        Node node = super.read(offset);
        node.setOffset(offset);
        return node;
    }

    @Override
    public Node get(long offset) {
        return cache.getUnchecked(offset);
    }
}
