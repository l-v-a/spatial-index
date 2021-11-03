package lva.spatialindex.index

import lva.spatialindex.index.Node.Companion.newNode
import lva.spatialindex.storage.Storage
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.awt.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertTrue
import kotlin.test.fail


/**
 * @author vlitvinenko
 */
@RunWith(MockitoJUnitRunner::class)
class NodeTest {
    private val storage: Storage<Node> = mock()

    @Test
    fun should_serialize_deserialize() {
        val serializer = NodeStorage(mock()).serializer
        val node = Node(storage, -1)
            .addEntry(Entry(storage, Rectangle(1, 2, 3, 4), -1))
            .addEntry(Entry(storage, Rectangle(2, 3, 4, 5), -1))
        val serialized = serializer.serialize(node)
        val restoredNode = serializer.deserialize(serialized)

        assertEquals(restoredNode.getEntries(), node.getEntries())
        assertEquals(restoredNode.parentOffset, node.parentOffset)
        assertEquals(restoredNode.offset, node.offset)
        assertEquals(restoredNode.isLeaf, node.isLeaf)
        assertEquals(restoredNode.isFull, node.isFull)
        assertNotSame(restoredNode, node)
    }

    @Test
    fun should_create_entry_with_mbr_when_new_node_is_added() {
        val mbr = Rectangle(1, 2, 3, 4)
        val node = newNode(storage)
        val newNode = newNode(storage)
            .addEntry(Entry(storage, mbr, 1))
        node.addNode(newNode)

        assertEquals(1, node.getEntries().size)
        assertEquals(mbr, node.getEntries().first().mbr)
    }

    @Test
    fun should_create_entry_with_mbr_offset_when_new_node_is_added() {
        val mbr = Rectangle(1, 2, 3, 4)
        val offset = 1L
        val node = newNode(storage)
        val newNode = newNode(storage)
        newNode.offset = offset
        newNode.addEntry(Entry(storage, mbr, 123))
        node.addNode(newNode)

        assertEquals(1, node.getEntries().size)
        assertEquals(offset, node.getEntries().first().childOffset)
    }

    @Test
    fun should_set_parent_offset_to_this_offset_when_new_node_is_added() {
        val thisOffset = 123L
        val node = newNode(storage)
        node.offset = thisOffset
        val newNode = newNode(storage)
        newNode.offset = 123L
        whenever(storage.read(123L)).thenReturn(newNode)
        node.addNode(newNode)

        assertEquals(thisOffset, newNode.parentOffset)
    }

    @Test
    fun should_recalculate_mbr_when_new_node_is_added() {
        val node = newNode(storage)
            .addEntry(Entry(storage, Rectangle(0, 0, 3, 4), 1))
        val newNode = newNode(storage)
            .addEntry(Entry(storage, Rectangle(3, 4, 7, 6), 1))
        assertEquals(Rectangle(0, 0, 3, 4), node.getMbr())

        node.addNode(newNode)
        assertEquals(Rectangle(0, 0, 10, 10), node.getMbr())
    }

    @Test
    fun should_save_this_node_when_new_node_is_added() {
        val thisOffset = 123L
        val node = newNode(storage)
        node.offset = thisOffset
        val newNode = newNode(storage)
        newNode.offset = 111L
        whenever(storage.read(111L)).thenReturn(newNode)

        node.addNode(newNode)
        verify(storage).write(thisOffset, node)
    }

    @Test
    fun should_save_new_node_when_new_node_is_added() {
        val newNodeOffset = 456L
        val node = newNode(storage)
        val newNode = newNode(storage)
        newNode.offset = newNodeOffset
        whenever(storage.read(newNodeOffset)).thenReturn(newNode)

        node.addNode(newNode)
        verify(storage).write(newNodeOffset, newNode)
    }

    @Test(expected = IllegalStateException::class)
    fun should_throw_exception_if_full_when_new_node_is_added() {
        val node = newNode(storage)
        (0 until Node.MAX_ENTRIES).forEach{ i ->
            node.addEntry(Entry(storage, Rectangle(i, i, i + 1, i + 2), i.toLong()))
        }
        val newNode = newNode(storage)
        node.addNode(newNode)
        fail()
    }

    @Test
    fun should_add_entry_to_tail() {
        val node = newNode(storage)
            .addEntry(Entry(storage, Rectangle(0, 0, 3, 4), 1))
        val newEntry = Entry(storage, Rectangle(3, 4, 7, 6), 1)

        node.addEntry(newEntry)
        assertEquals(newEntry, node.getEntries().last())
    }

    @Test
    fun should_recalculate_mbr_when_new_entry_is_added() {
        val node = newNode(storage)
            .addEntry(Entry(storage, Rectangle(0, 0, 3, 4), 1))
        assertEquals(Rectangle(0, 0, 3, 4), node.getMbr())

        node.addEntry(Entry(storage, Rectangle(3, 4, 7, 6), 1))
        assertEquals(Rectangle(0, 0, 10, 10), node.getMbr())
    }

    @Test
    fun should_reset_parent_offset_for_entry_node_when_new_entry_is_added() {
        val thisOffset= 123L
        val childOffset = 456L
        val node = newNode(storage)
        node.offset = thisOffset
        val childNode = newNode(storage)
        whenever(storage.read(eq(childOffset))).thenReturn(childNode)

        node.addEntry(Entry(storage, Rectangle(3, 4, 7, 6), childOffset))
        assertEquals(thisOffset, childNode.parentOffset)
    }

    @Test
    fun should_save_entry_node_when_new_entry_is_added() {
        val childOffset = 456L
        val childNodeOffset = 789L
        val node = newNode(storage)
        val childNode = newNode(storage)
        childNode.offset = childNodeOffset
        whenever(storage.read(eq(childOffset))).thenReturn(childNode)

        node.addEntry(Entry(storage, Rectangle(3, 4, 7, 6), childOffset))
        verify(storage).write(childNodeOffset, childNode)
    }

    @Test
    fun should_save_this_node_when_new_entry_is_added() {
        val thisOffset = 123L
        val node = newNode(storage)
        node.offset = thisOffset

        node.addEntry(Entry(storage, Rectangle(3, 4, 7, 6), -1))
        verify(storage).write(thisOffset, node)
    }

    @Test(expected = IllegalStateException::class)
    fun should_throw_exception_if_full_when_new_entry_is_added() {
        val node = newNode(storage)
        (0 until Node.MAX_ENTRIES).forEach { i ->
            node.addEntry(Entry(storage, Rectangle(i, i, i + 1, i + 2), i.toLong()))
        }
        node.addEntry(Entry(storage, Rectangle(3, 4, 7, 6), -1))
        fail()
    }

    @Test
    fun should_be_leaf_node_if_have_no_entries() {
        val node = newNode(storage)
        assertEquals(0, node.getEntries().size)
        assertTrue(node.isLeaf)
    }

    @Test
    fun should_be_leaf_node_if_first_entry_is_leaf() {
        val entry = Entry(storage, Rectangle(3, 4, 7, 6), -1)
        val node = newNode(storage)
            .addEntry(entry)
        assertTrue(entry.isLeaf)
        assertTrue(node.isLeaf)
    }

    @Test
    fun should_reset_entries() {
        val entry1 = Entry(storage, Rectangle(3, 4, 7, 6), -1)
        val entry2 = Entry(storage, Rectangle(7, 8, 9, 10), -1)
        val entry3 = Entry(storage, Rectangle(11, 12, 13, 14), -1)
        val entry4 = Entry(storage, Rectangle(15, 16, 17, 18), -1)
        val node = newNode(storage)
            .addEntry(entry1)
            .addEntry(entry2)
        
        assertEquals(listOf(entry1, entry2), node.getEntries())
        node.setEntries(listOf(entry3, entry4))
        assertNotEquals(listOf(entry1, entry2), node.getEntries())
        assertEquals(listOf(entry3, entry4), node.getEntries())
    }

    @Test
    fun should_recalculate_mbr_when_resetting_entries() {
        val entry1 = Entry(storage, Rectangle(3, 4, 7, 6), -1)
        val entry2 = Entry(storage, Rectangle(0, 0, 1, 1), -1)
        val entry3 = Entry(storage, Rectangle(10, 10, 1, 1), -1)
        val node = newNode(storage)
            .addEntry(entry1)
        
        assertEquals(Rectangle(3, 4, 7, 6), node.getMbr())

        node.setEntries(listOf(entry2, entry3))
        assertEquals(Rectangle(0, 0, 11, 11), node.getMbr())
    }

    @Test
    fun should_save_this_node_when_resetting_entries() {
        val thisOffset = 123L
        val entry2 = Entry(storage, Rectangle(0, 0, 1, 1), -1)
        val entry3 = Entry(storage, Rectangle(10, 10, 1, 1), -1)
        val node = newNode(storage)
        node.offset = thisOffset

        node.setEntries(listOf(entry2, entry3))
        verify(storage).write(thisOffset, node)
    }
}