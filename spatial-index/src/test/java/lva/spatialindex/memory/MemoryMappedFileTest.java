package lva.spatialindex.memory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author vlitvinenko
 */
public class MemoryMappedFileTest {
    private static final Path FILE_PATH = Paths.get(System.getProperty("java.io.tmpdir"),
        MemoryMappedFileTest.class.getName() + ".test.bin");

    @Before
    public void setUp() throws IOException {
        Files.deleteIfExists(FILE_PATH);
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(FILE_PATH);
    }

    @Test
    public void should_write_buffer() throws IOException {
        byte [] buffToWrite = new byte[] {0, 1, 2, 3, 4};
        long offset = 123L;

        try (MemoryMappedFile mmf = new MemoryMappedFile(FILE_PATH.toString(), 4096)) {
            mmf.writeBytes(offset, buffToWrite);
        }

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH.toString(), "r")) {
            byte [] buffToRead = new byte[buffToWrite.length];
            raf.seek(offset);
            raf.read(buffToRead, 0, buffToRead.length);

            assertArrayEquals(buffToWrite, buffToRead);
        }
    }

    @Test
    public void should_read_buffer() throws IOException {
        byte [] buffToWrite = new byte[] {0, 1, 2, 3, 4};
        long offset = 123L;

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH.toString(), "rw")) {
            raf.setLength(4096);
            raf.seek(offset);
            raf.write(buffToWrite, 0, buffToWrite.length);
        }

        try (MemoryMappedFile mmf = new MemoryMappedFile(FILE_PATH.toString(), 4096)) {
            byte[] buff = mmf.readBytes(offset, buffToWrite.length);

            assertArrayEquals(buffToWrite, buff);
        }
    }

    @Test
    public void should_round_file_size_to_page_size() throws IOException {
        try (MemoryMappedFile mmf = new MemoryMappedFile(FILE_PATH.toString(), 1)) {
        }

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH.toString(), "r")) {
            assertEquals(MemoryMappedFile.PAGE_SIZE, raf.length());
        }
    }

    @Test
    public void should_realocate_capacity_if_capacity_exceeds() throws IOException {
        try (MemoryMappedFile mmf = new MemoryMappedFile(FILE_PATH.toString(), 1)) {
            mmf.allocate(MemoryMappedFile.PAGE_SIZE + 1);

        }

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH.toString(), "r")) {
            assertEquals(MemoryMappedFile.PAGE_SIZE * 2, raf.length());
        }
    }

    @Test
    public void should_realocate_data() throws IOException {
        try (MemoryMappedFile mmf = new MemoryMappedFile(FILE_PATH.toString(), MemoryMappedFile.PAGE_SIZE)) {
            mmf.writeBytes(MemoryMappedFile.PAGE_SIZE - 1, new byte[] {12});
            mmf.allocate(MemoryMappedFile.PAGE_SIZE + 1);
            mmf.writeBytes(MemoryMappedFile.PAGE_SIZE, new byte[] {34});
        }

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH.toString(), "r")) {
            raf.seek(MemoryMappedFile.PAGE_SIZE - 1);
            byte [] buffToRead = new byte[2];
            raf.read(buffToRead, 0, buffToRead.length);

            assertArrayEquals(new byte[]{12, 34}, buffToRead);
        }
    }


    @Test
    public void should_return_capacity() {
        try (MemoryMappedFile mmf = new MemoryMappedFile(FILE_PATH.toString(), 1)) {
            assertEquals(MemoryMappedFile.PAGE_SIZE, mmf.getCapacity());
            mmf.allocate(MemoryMappedFile.PAGE_SIZE + 1);
            assertEquals(MemoryMappedFile.PAGE_SIZE * 2, mmf.getCapacity());
        }

    }

    @Test
    public void should_return_size() {
        try (MemoryMappedFile mmf = new MemoryMappedFile(FILE_PATH.toString(), 1)) {
            assertEquals(0, mmf.getSize());
            mmf.allocate(10);
            assertEquals(10, mmf.getSize());
        }
    }

}