package lva.spatialindex.index;

import com.google.common.util.concurrent.UncheckedExecutionException;
import lva.spatialindex.storage.StorageSpace;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.awt.Rectangle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author vlitvinenko
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeStorageTest {
    private NodeStorage storage;
    private Node node;
    private byte[] serializedNode;

    @Mock
    private StorageSpace storageSpace;

    @Mock
    private NodeStorage.NodeSerializer nodeSerializer;

    @Before
    public void setUp() {
        reset(storageSpace);
        reset(nodeSerializer);

        storage = new NodeStorage(storageSpace) {
            @Override
            public @NotNull NodeSerializer getSerializer() {
                return nodeSerializer;
            }
        };


        node = new Node(mock(NodeStorage.class), -1);
        node.setOffset(123);
        node.addEntry(new Entry(mock(NodeStorage.class), new Rectangle(1, 2, 3, 4), -1));

        serializedNode = new NodeStorage.NodeSerializer(storage)
                .serialize(node);

        when(nodeSerializer.serialize(any())).thenReturn(serializedNode);
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

    @Test(expected = IllegalStateException.class)
    public void should_throw_if_allocation_size_exceeds_record_size() {
        Node bigNode = mock(Node.class);
        when(nodeSerializer.serialize(bigNode)).thenReturn(new byte[NodeStorage.RECORD_SIZE + 1]);
        storage.add(bigNode);

        fail();
    }


    @Test
    public void should_write_to_storage() {
        storage.write(0, node);
        verify(storageSpace).writeBytes(anyLong(), eq(serializedNode));
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_for_negative_offset_when_writing() {
        storage.write(-1, node);
        fail();
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_if_node_size_exceeds_record_size_when_writing() {
        Node bigNode = mock(Node.class);
        when(nodeSerializer.serialize(bigNode)).thenReturn(new byte[NodeStorage.RECORD_SIZE + 1]);
        storage.write(0, bigNode);

        fail();
    }

    @Test
    public void should_read_from_storage() {
        long offset = node.getOffset();
        NodeStorage nodeStorage = new NodeStorage(storageSpace);

        when(storageSpace.readBytes(eq(offset), eq(NodeStorage.RECORD_SIZE)))
            .thenReturn(serializedNode);

        Node newNode  = nodeStorage.read(offset);

        assertEquals(newNode.getEntries(), node.getEntries());
        assertEquals(newNode.getParentOffset(), node.getParentOffset());
        assertEquals(newNode.getOffset(), node.getOffset());
        assertEquals(newNode.isLeaf(), node.isLeaf());
        assertEquals(newNode.isFull(), node.isFull());
    }

    @Test(expected = UncheckedExecutionException.class)
    public void should_throw_if_out_of_bounds_when_reading() {
        NodeStorage nodeStorage = new NodeStorage(storageSpace);

        nodeStorage.read(0L);

        fail();
    }

    @Test
    public void should_use_cache_when_reading() {
        NodeStorage nodeStorage = new NodeStorage(storageSpace);

        nodeStorage.add(node);
        Node newNode = nodeStorage.read(node.getOffset());

        assertSame(newNode, node);
        verify(storageSpace, never()).readBytes(anyLong(), anyInt());
    }

    @Test
    public void should_clear_storage_space_when_clearing() {
        NodeStorage nodeStorage = new NodeStorage(storageSpace);
        nodeStorage.clear();
        verify(storageSpace, times(1)).clear();
    }



}