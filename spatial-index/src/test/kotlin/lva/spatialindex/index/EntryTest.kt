package lva.spatialindex.index

import lva.spatialindex.index.Node.Companion.newNode
import lva.spatialindex.storage.Storage
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.awt.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * @author vlitvinenko
 */
@RunWith(MockitoJUnitRunner::class)
class EntryTest {
    private val storage: Storage<Node> = mock()

    @Test
    fun should_be_leaf_if_child_offset_is_negative() {
        assertTrue(Entry(storage, Rectangle(), -1).isLeaf)
    }

    @Test
    fun should_return_child_node_if_offset_is_positive() {
        val childNodeOffset: Long = 123
        val childNode = newNode(storage)
        childNode.offset = childNodeOffset
        whenever(storage.read(childNodeOffset))
            .thenReturn(childNode)

        val entry = Entry(storage, Rectangle(), childNodeOffset)
        assertEquals(childNode, entry.childNode)
    }

    @Test
    fun should_return_null_for_child_node_if_offset_is_negative() {
        val entry = Entry(storage, Rectangle(), -123)
        assertNull(entry.childNode)
    }

    @Test
    fun should_calculate_union_of_entries_lists() {
        val entry1 = Entry(storage, Rectangle(0, 0, 1, 1), -1)
        val entry2 = Entry(storage, Rectangle(1, 1, 2, 2), -1)

        assertEquals(Rectangle(0, 0, 3, 3), listOf(entry1, entry2).union())
    }

    @Test
    fun should_calculate_union_of_empty_list_of_entries() {
        assertEquals(Rectangle(0, 0, 0, 0), emptyList<Entry>().union())
    }

    @Test
    fun should_calculate_margin_of_entries_lists() {
        val entry1 = Entry(storage, Rectangle(0, 0, 1, 1), -1)
        val entry2 = Entry(storage, Rectangle(1, 1, 2, 2), -1)

        assertEquals(12, listOf(entry1, entry2).margin())
    }

    @Test
    fun should_calculate_margin_of_empty_list_of_entries() {
        assertEquals(0, emptyList<Entry>().margin())
    }

    @Test
    fun test_sides() {
        val entry = Entry(storage, Rectangle(1, 2, 3, 4), -1)

        assertEquals(1, entry.left)
        assertEquals(4, entry.right)
        assertEquals(2, entry.top)
        assertEquals(6, entry.bottom)
    }

}