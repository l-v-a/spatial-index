package lva.spatialindex.index

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import lva.spatialindex.memory.SegmentStorageSpace
import lva.spatialindex.storage.AbstractStorage
import lva.spatialindex.storage.Storage
import lva.spatialindex.storage.StorageSpace
import java.io.InputStream
import java.io.OutputStream

/**
 * @author vlitvinenko
 */
internal open class NodeStorage(storageSpace: StorageSpace, recordSize: Int) :
    AbstractStorage<Node>(storageSpace, recordSize) {

    constructor(fileName: String, initialSize: Long) :
            this(SegmentStorageSpace(fileName, initialSize))

    // TODO: remove it
    internal constructor(storageSpace: StorageSpace) :
            this(storageSpace, RECORD_SIZE)

    class NodeSerializer(storage: Storage<Node>) : AbstractSerializer<Node>() {
        private val kryo = Kryo()
        init {
            kryo.addDefaultSerializer(Node::class.java, Node.Ser(storage))
            kryo.addDefaultSerializer(Entry::class.java, Entry.Ser(storage))
        }

        override fun write(outputStream: OutputStream, node: Node) =
            Output(outputStream).use {
                kryo.writeObject(it, node)
                it.flush()
            }

        override fun read(inputStream: InputStream): Node =
            Input(inputStream).use { kryo.readObject(it, Node::class.java) }
    }

    override val serializer: Serializer<Node> = NodeSerializer(this)

    private val cache = CacheBuilder.newBuilder()
        .softValues()
        .build(object : CacheLoader<Long, Node>() {
            override fun load(offset: Long): Node {
                return this@NodeStorage.load(offset)
            }
        })

    override fun add(node: Node): Long =
        super.add(node).also { offset ->
            node.offset = offset
            cache.put(offset, node)
        }

    override fun read(offset: Long): Node =
        cache.getUnchecked(offset)

    private fun load(offset: Long): Node =
        super.read(offset).also { it.offset = offset }

    companion object {
        const val RECORD_SIZE = 4096 * 2
    }
}