package lva.spatialindex.index;

import com.google.common.util.concurrent.UncheckedExecutionException;
import lva.spatialindex.storage.Storage;
import lva.spatialindex.storage.StorageSpace;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.awt.Rectangle;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author vlitvinenko
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeStorageTest {
    private Storage<Node> storage;
    private Node node;
    private byte[] serializedNode;

    @Mock
    private StorageSpace storageSpace;

    @Before
    public void setUp() {
        storage = new NodeStorage(storageSpace);
        when(storageSpace.getSize()).thenReturn((long)NodeStorage.RECORD_SIZE);
        node = Node.newNode(storage)
            .setOffset(123)
            .addEntry(new Entry(storage, new Rectangle(1, 2, 3, 4), -1));
        serializedNode = node.serialize();

        reset(storageSpace);
    }

    @Test
    public void should_add_to_storage() {
        storage.add(node);
        verify(storageSpace).writeBytes(anyLong(), eq(serializedNode));
    }

    @Test
    public void should_allocate_space_rounded_to_record_size() {
        storage.add(node);

        ArgumentCaptor<Long> allocatedSizeCaptor = ArgumentCaptor.forClass(Long.class);
        verify(storageSpace).allocate(allocatedSizeCaptor.capture());

        assertTrue(allocatedSizeCaptor.getValue() % NodeStorage.RECORD_SIZE == 0);
    }

    @Test
    public void should_return_allocated_offset() {
        when(storageSpace.allocate(anyLong()))
            .thenReturn(123L);

        assertEquals(storage.add(node), 123L);
    }

    @Test
    public void should_set_allocated_offset_to_node() {
        when(storageSpace.allocate(anyLong()))
            .thenReturn(123L);

        storage.add(node);

        assertEquals(node.getOffset(), 123L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_if_allocation_size_exceeds_record_size() {
        when(storageSpace.allocate(anyLong()))
            .thenReturn(123L);

        Node bigNode = mock(Node.class);
        when(bigNode.serialize()).thenReturn(new byte[NodeStorage.RECORD_SIZE + 1]);
        storage.add(bigNode);

        fail();
    }

    @Test
    public void should_write_to_storage() {
        when(storageSpace.getSize())
            .thenReturn((long)NodeStorage.RECORD_SIZE);

        storage.write(0, node);

        verify(storageSpace).writeBytes(anyLong(), eq(serializedNode));
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_for_negative_offset_when_writing() {
        when(storageSpace.getSize())
            .thenReturn((long)NodeStorage.RECORD_SIZE);

        storage.write(-1, node);

        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_if_space_size_exceeds_when_writing() {
            when(storageSpace.getSize())
            .thenReturn((long)serializedNode.length - 1);

        storage.write(0, node);

        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_if_node_size_exceeds_record_size_when_writing() {
        when(storageSpace.getSize())
            .thenReturn((long)serializedNode.length + 1);

        Node bigNode = mock(Node.class);
        when(bigNode.serialize()).thenReturn(new byte[NodeStorage.RECORD_SIZE + 1]);
        storage.write(0, bigNode);

        fail();
    }

    @Test
    public void should_read_from_storage() {
        long offset = node.getOffset();
        NodeStorage nodeStorage = new NodeStorage(storageSpace);

        when(storageSpace.getSize())
            .thenReturn((long) NodeStorage.RECORD_SIZE + offset);
        when(storageSpace.readBytes(eq(offset), eq(NodeStorage.RECORD_SIZE)))
            .thenReturn(serializedNode);

        Node newNode  = nodeStorage.read(offset);
        assertEquals(newNode, node);
    }

    @Test(expected = UncheckedExecutionException.class)
    public void should_throw_if_out_of_bounds_when_reading() {
        NodeStorage nodeStorage = new NodeStorage(storageSpace);
        when(storageSpace.getSize())
            .thenReturn((long) NodeStorage.RECORD_SIZE - 1);

        nodeStorage.read(0L);

        fail();
    }

    @Test
    public void should_use_cache_when_reading() {
        NodeStorage nodeStorage = new NodeStorage(storageSpace);
        when(storageSpace.getSize())
            .thenReturn((long) NodeStorage.RECORD_SIZE);

        nodeStorage.add(node);
        Node newNode = nodeStorage.read(node.getOffset());

        assertSame(newNode, node);
        verify(storageSpace, never()).readBytes(anyLong(), anyInt());
    }

    @Test
    public void should_close_storage_space_when_closing() {
        NodeStorage nodeStorage = new NodeStorage(storageSpace);
        nodeStorage.close();
        verify(storageSpace, times(1)).close();
    }



}