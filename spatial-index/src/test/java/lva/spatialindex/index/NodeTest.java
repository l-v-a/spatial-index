package lva.spatialindex.index;

import lva.spatialindex.storage.Storage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.awt.*;

import static com.google.common.collect.Iterables.getLast;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author vlitvinenko
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeTest {
    @Mock
    private Storage<Node> storage;

    @Test
    public void should_serialize_deserialize() {
        Node node = Node.newNode(storage)
            .addEntry(new Entry(storage, new Rectangle(1, 2, 3, 4), 1))
            .addEntry(new Entry(storage, new Rectangle(2, 3, 4, 5), 2));

        byte[] serialized = node.serialize();
        Node restoredNode = Node.newNode(storage)
            .deserialize(serialized);

        assertEquals(restoredNode, node);
        assertNotSame(restoredNode, node);
    }

    @Test
    public void should_create_entry_with_mbr_when_new_node_is_added() {
        Rectangle mbr = new Rectangle(1, 2, 3, 4);
        Node node = Node.newNode(storage);
        Node newNode = Node.newNode(storage)
            .addEntry(new Entry(storage, mbr, 1));

        node.addNode(newNode);

        assertEquals(1, node.getEntries().size());
        assertEquals(mbr, node.getEntries().get(0).getMbr());
    }

    @Test
    public void should_create_entry_with_mbr_offset_when_new_node_is_added() {
        Rectangle mbr = new Rectangle(1, 2, 3, 4);
        long offset = 1;
        Node node = Node.newNode(storage);
        Node newNode = Node.newNode(storage)
            .setOffset(offset)
            .addEntry(new Entry(storage, mbr, 123));

        node.addNode(newNode);

        assertEquals(1, node.getEntries().size());
        assertEquals(offset, node.getEntries().get(0).getChildOffset());
    }

    @Test
    public void should_set_parent_offset_to_this_offset_when_new_node_is_added() {
        long thisOffset = 123;
        Node node = Node.newNode(storage)
            .setOffset(thisOffset);

        Node newNode = Node.newNode(storage).setOffset(123L);
        when(storage.read(123L)).thenReturn(newNode);

        node.addNode(newNode);

        assertEquals(thisOffset, newNode.getParentOffset());
    }

    @Test
    public void should_recalculate_mbr_when_new_node_is_added() {
        Node node = Node.newNode(storage)
            .addEntry(new Entry(storage, new Rectangle(0, 0, 3, 4), 1));
        Node newNode = Node.newNode(storage)
            .addEntry(new Entry(storage, new Rectangle(3, 4, 7, 6), 1));

        assertEquals(new Rectangle(0, 0, 3, 4), node.getMbr());

        node.addNode(newNode);

        assertEquals(new Rectangle(0, 0, 10, 10), node.getMbr());
    }

    @Test
    public void should_save_this_node_when_new_node_is_added() {
        long thisOffset = 123;
        Node node = Node.newNode(storage)
            .setOffset(thisOffset);

        Node newNode = Node.newNode(storage).setOffset(111L);
        when(storage.read(111L)).thenReturn(newNode);

        node.addNode(newNode);

        verify(storage).write(thisOffset, node);
    }

    @Test
    public void should_save_new_node_when_new_node_is_added() {
        long newNodeOffset = 456;
        Node node = Node.newNode(storage);

        Node newNode = Node.newNode(storage).setOffset(newNodeOffset);
        when(storage.read(newNodeOffset)).thenReturn(newNode);

        node.addNode(newNode);

        verify(storage).write(newNodeOffset, newNode);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_exception_if_full_when_new_node_is_added() {
        Node node = Node.newNode(storage);
        for (int i = 0; i < Node.MAX_ENTRIES; i++) {
            node.addEntry(new Entry(storage, new Rectangle(i, i, i + 1, i + 2), i));
        }
        Node newNode = Node.newNode(storage);

        node.addNode(newNode);

        fail();
    }

    @Test
    public void should_add_entry_to_tail() {
        Node node = Node.newNode(storage)
            .addEntry(new Entry(storage, new Rectangle(0, 0, 3, 4), 1));

        Entry newEntry = new Entry(storage, new Rectangle(3, 4, 7, 6), 1);
        node.addEntry(newEntry);

        assertEquals(newEntry, getLast(node.getEntries()));
    }


    @Test
    public void should_recalculate_mbr_when_new_entry_is_added() {
        Node node = Node.newNode(storage)
            .addEntry(new Entry(storage, new Rectangle(0, 0, 3, 4), 1));
        assertEquals(new Rectangle(0, 0, 3, 4), node.getMbr());

        node.addEntry(new Entry(storage, new Rectangle(3, 4, 7, 6), 1));

        assertEquals(new Rectangle(0, 0, 10, 10), node.getMbr());
    }

    @Test
    public void should_reset_parent_offset_for_entry_node_when_new_entry_is_added() {
        long thisOffset = 123;
        long childOffset = 456;
        Node node = Node.newNode(storage)
            .setOffset(thisOffset);
        Node childNode = Node.newNode(storage);
        when(storage.read(eq(childOffset))).thenReturn(childNode);

        node.addEntry(new Entry(storage, new Rectangle(3, 4, 7, 6), childOffset));

        assertEquals(thisOffset, childNode.getParentOffset());
    }

    @Test
    public void should_save_entry_node_when_new_entry_is_added() {
        long childOffset = 456;
        long childNodeOffset = 789;
        Node node = Node.newNode(storage);
        Node childNode = Node.newNode(storage)
            .setOffset(childNodeOffset);

        when(storage.read(eq(childOffset))).thenReturn(childNode);

        node.addEntry(new Entry(storage, new Rectangle(3, 4, 7, 6), childOffset));

        verify(storage).write(childNodeOffset, childNode);
    }

    @Test
    public void should_save_this_node_when_new_entry_is_added() {
        long thisOffset = 123;
        Node node = Node.newNode(storage)
            .setOffset(thisOffset);

        node.addEntry(new Entry(storage, new Rectangle(3, 4, 7, 6), -1));

        verify(storage).write(thisOffset, node);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_exception_if_full_when_new_entry_is_added() {
        Node node = Node.newNode(storage);
        for (int i = 0; i < Node.MAX_ENTRIES; i++) {
            node.addEntry(new Entry(storage, new Rectangle(i, i, i + 1, i + 2), i));
        }

        node.addEntry(new Entry(storage, new Rectangle(3, 4, 7, 6), -1));

        fail();
    }

    @Test
    public void should_be_leaf_node_if_have_no_entries() {
        Node node = Node.newNode(storage);

        assertEquals(0, node.getEntries().size());
        assertTrue(node.isLeaf());
    }

    @Test
    public void should_be_leaf_node_if_first_entry_is_leaf() {
        Entry entry = new Entry(storage, new Rectangle(3, 4, 7, 6), -1);
        Node node = Node.newNode(storage)
            .addEntry(entry);

        assertTrue(entry.isLeaf());
        assertTrue(node.isLeaf());
    }

    @Test
    public void should_reset_entries() {
        Entry entry1= new Entry(storage, new Rectangle(3, 4, 7, 6), -1);
        Entry entry2= new Entry(storage, new Rectangle(7, 8, 9, 10), -1);
        Entry entry3= new Entry(storage, new Rectangle(11, 12, 13, 14), -1);
        Entry entry4= new Entry(storage, new Rectangle(15, 16, 17, 18), -1);

        Node node = Node.newNode(storage)
            .addEntry(entry1)
            .addEntry(entry2);
        assertEquals(asList(entry1, entry2), node.getEntries());

        node.setEntries(asList(entry3, entry4));

        assertNotEquals(asList(entry1, entry2), node.getEntries());
        assertEquals(asList(entry3, entry4), node.getEntries());
    }

    @Test
    public void should_recalculate_mbr_when_resetting_entries() {
        Entry entry1= new Entry(storage, new Rectangle(3, 4, 7, 6), -1);
        Entry entry2= new Entry(storage, new Rectangle(0, 0, 1, 1), -1);
        Entry entry3= new Entry(storage, new Rectangle(10, 10, 1, 1), -1);

        Node node = Node.newNode(storage)
            .addEntry(entry1);

        assertEquals(new Rectangle(3, 4, 7, 6), node.getMbr());

        node.setEntries(asList(entry2, entry3));

        assertEquals(new Rectangle(0, 0, 11, 11), node.getMbr());
    }

    @Test
    public void should_save_this_node_when_resetting_entries() {
        long thisOffset = 123;
        Entry entry1= new Entry(storage, new Rectangle(3, 4, 7, 6), -1);
        Entry entry2= new Entry(storage, new Rectangle(0, 0, 1, 1), -1);
        Entry entry3= new Entry(storage, new Rectangle(10, 10, 1, 1), -1);

        Node node = Node.newNode(storage)
            .setOffset(thisOffset);

        node.setEntries(asList(entry2, entry3));

        verify(storage).write(thisOffset, node);
    }
}