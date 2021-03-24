package lva.spatialindex.index;

import lva.spatialindex.storage.Storage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.awt.Rectangle;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author vlitvinenko
 */
@RunWith(MockitoJUnitRunner.class)
public class EntryTest {
    @Mock
    private Storage<Node> storage;

    @Test
    public void should_be_leaf_if_child_offset_is_negative() {
        assertTrue(new Entry(storage, new Rectangle(), -1).isLeaf());
    }

    @Test
    public void should_return_child_node_if_offset_is_positive() {
        long childNodeOffset = 123;
        Node childNode = Node.newNode(storage)
            .setOffset(childNodeOffset);
        Mockito.when(storage.read(childNodeOffset))
            .thenReturn(childNode);

        Entry entry = new Entry(storage, new Rectangle(), childNodeOffset);

        assertEquals(childNode, entry.getChildNode().orElse(null));
    }

    @Test
    public void should_return_null_for_child_node_if_offset_is_negative() {
        Entry entry = new Entry(storage, new Rectangle(), -123);
        assertFalse(entry.getChildNode().isPresent());
    }

    @Test
    public void should_calculate_union_of_entries_lists() {
        Entry entry1 = new Entry(storage, new Rectangle(0, 0, 1, 1), -1);
        Entry entry2 = new Entry(storage, new Rectangle(1, 1, 2, 2), -1);
        assertEquals(new Rectangle(0, 0, 3, 3), Entry.union(asList(entry1, entry2)));
    }

    @Test
    public void should_calculate_union_of_empty_list_of_entries() {
        assertEquals(new Rectangle(0, 0, 0, 0), Entry.union(emptyList()));
    }


    @Test
    public void should_calculate_margin_of_entries_lists() {
        Entry entry1 = new Entry(storage, new Rectangle(0, 0, 1, 1), -1);
        Entry entry2 = new Entry(storage, new Rectangle(1, 1, 2, 2), -1);
        assertEquals(12, Entry.margin(asList(entry1, entry2)));
    }

    @Test
    public void should_calculate_margin_of_empty_list_of_entries() {
        assertEquals(0, Entry.margin(emptyList()));
    }

    @Test
    public void test_left_to_right_by_left_comparator() {
        Entry entry1 = new Entry(storage, new Rectangle(0, 0, 1, 1), -1);
        Entry entry2 = new Entry(storage, new Rectangle(1, 1, 1, 1), -1);

        assertEquals(-1, Entry.LEFT_TO_RIGHT_BY_LEFT_COMPARATOR.compare(entry1, entry2));
        assertEquals(1, Entry.LEFT_TO_RIGHT_BY_LEFT_COMPARATOR.compare(entry2, entry1));
        assertEquals(0, Entry.LEFT_TO_RIGHT_BY_LEFT_COMPARATOR.compare(entry1, entry1));
    }

    @Test
    public void test_left_to_right_by_right_comparator() {
        Entry entry1 = new Entry(storage, new Rectangle(0, 0, 1, 1), -1);
        Entry entry2 = new Entry(storage, new Rectangle(1, 1, 1, 1), -1);

        assertEquals(-1, Entry.LEFT_TO_RIGHT_BY_RIGHT_COMPARATOR.compare(entry1, entry2));
        assertEquals(1, Entry.LEFT_TO_RIGHT_BY_RIGHT_COMPARATOR.compare(entry2, entry1));
        assertEquals(0, Entry.LEFT_TO_RIGHT_BY_RIGHT_COMPARATOR.compare(entry1, entry1));
    }

    @Test
    public void test_top_to_bottom_by_bottom_comparator() {
        Entry entry1 = new Entry(storage, new Rectangle(0, 0, 1, 1), -1);
        Entry entry2 = new Entry(storage, new Rectangle(1, 1, 1, 1), -1);

        assertEquals(-1, Entry.TOP_TO_BOTTOM_BY_BOTTOM_COMPARATOR.compare(entry1, entry2));
        assertEquals(1, Entry.TOP_TO_BOTTOM_BY_BOTTOM_COMPARATOR.compare(entry2, entry1));
        assertEquals(0, Entry.TOP_TO_BOTTOM_BY_BOTTOM_COMPARATOR.compare(entry1, entry1));
    }

    @Test
    public void test_top_to_bottom_by_top_comparator() {
        Entry entry1 = new Entry(storage, new Rectangle(0, 0, 1, 1), -1);
        Entry entry2 = new Entry(storage, new Rectangle(1, 1, 1, 1), -1);

        assertEquals(-1, Entry.TOP_TO_BOTTOM_BY_TOP_COMPARATOR.compare(entry1, entry2));
        assertEquals(1, Entry.TOP_TO_BOTTOM_BY_TOP_COMPARATOR.compare(entry2, entry1));
        assertEquals(0, Entry.TOP_TO_BOTTOM_BY_TOP_COMPARATOR.compare(entry1, entry1));
    }
}