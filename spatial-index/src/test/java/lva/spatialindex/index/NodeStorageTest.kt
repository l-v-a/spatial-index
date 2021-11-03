package lva.spatialindex.index

import com.google.common.util.concurrent.UncheckedExecutionException
import lva.spatialindex.index.NodeStorage.NodeSerializer
import lva.spatialindex.storage.StorageSpace
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.awt.Rectangle
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * @author vlitvinenko
 */
@RunWith(MockitoJUnitRunner::class)
class NodeStorageTest {
    private lateinit var storage: NodeStorage
    private lateinit var node: Node
    private lateinit var serializedNode: ByteArray

    private val storageSpace: StorageSpace = mock()
    private val nodeSerializer: NodeSerializer = mock()

    @BeforeTest
    fun setUp() {
        reset(storageSpace)
        reset(nodeSerializer)

        storage = object : NodeStorage(storageSpace) {
            override val serializer: NodeSerializer
                get() = nodeSerializer
        }

        node = Node(mock<NodeStorage>(), -1).also {
            it.offset = 123
            it.addEntry(Entry(mock<NodeStorage>(), Rectangle(1, 2, 3, 4), -1))
            serializedNode = NodeSerializer(storage).serialize(it)
        }

        whenever(nodeSerializer.serialize(any())).thenReturn(serializedNode)
    }

    @Test
    fun should_add_to_storage() {
        storage.add(node)
        verify(storageSpace).writeBytes(anyLong(), eq(serializedNode))
    }

    @Test
    fun should_allocate_space_rounded_to_record_size() {
        storage.add(node)
        argumentCaptor<Int>().apply {
            verify(storageSpace).allocate(capture())
            assertTrue(firstValue % NodeStorage.RECORD_SIZE == 0)
        }
    }

    @Test
    fun should_return_allocated_offset() {
        whenever(storageSpace.allocate(anyInt()))
            .thenReturn(123L)
        assertEquals(storage.add(node), 123L)
    }

    @Test
    fun should_set_allocated_offset_to_node() {
        whenever(storageSpace.allocate(anyInt()))
            .thenReturn(123L)
        storage.add(node)
        assertEquals(node.offset, 123L)
    }

    @Test(expected = IllegalStateException::class)
    fun should_throw_if_allocation_size_exceeds_record_size() {
        val bigNode = mock<Node>()
        whenever(nodeSerializer.serialize(bigNode))
            .thenReturn(ByteArray(NodeStorage.RECORD_SIZE + 1))
        storage.add(bigNode)
        fail()
    }

    @Test
    fun should_write_to_storage() {
        storage.write(0, node)
        verify(storageSpace).writeBytes(anyLong(), eq(serializedNode))
    }

    @Test(expected = IllegalStateException::class)
    fun should_throw_for_negative_offset_when_writing() {
        storage.write(-1, node)
        fail()
    }

    @Test(expected = IllegalStateException::class)
    fun should_throw_if_node_size_exceeds_record_size_when_writing() {
        val bigNode = mock<Node>()
        whenever(nodeSerializer.serialize(bigNode)).thenReturn(ByteArray(NodeStorage.RECORD_SIZE + 1))
        storage.write(0, bigNode)
        fail()
    }

    @Test
    fun should_read_from_storage() {
        val offset = node.offset
        val nodeStorage = NodeStorage(storageSpace)
        whenever(storageSpace.readBytes(eq(offset), eq(NodeStorage.RECORD_SIZE)))
            .thenReturn(serializedNode)
        val newNode = nodeStorage.read(offset)

        assertEquals(newNode.getEntries(), node.getEntries())
        assertEquals(newNode.parentOffset, node.parentOffset)
        assertEquals(newNode.offset, node.offset)
        assertEquals(newNode.isLeaf, node.isLeaf)
        assertEquals(newNode.isFull, node.isFull)
    }

    @Test(expected = UncheckedExecutionException::class)
    fun should_throw_if_out_of_bounds_when_reading() {
        val nodeStorage = NodeStorage(storageSpace)
        nodeStorage.read(0L)
        fail()
    }

    @Test
    fun should_use_cache_when_reading() {
        val nodeStorage = NodeStorage(storageSpace)
        nodeStorage.add(node)
        val newNode = nodeStorage.read(node.offset)

        assertSame(newNode, node)
        verify(storageSpace, never()).readBytes(anyLong(), anyInt())
    }

    @Test
    fun should_clear_storage_space_when_clearing() {
        val nodeStorage = NodeStorage(storageSpace)
        nodeStorage.clear()
        verify(storageSpace, times(1)).clear()
    }
}