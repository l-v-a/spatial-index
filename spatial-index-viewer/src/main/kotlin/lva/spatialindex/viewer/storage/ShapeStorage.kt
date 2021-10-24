package lva.spatialindex.viewer.storage

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import lva.spatialindex.memory.MemoryMappedFile
import lva.spatialindex.storage.AbstractStorage
import java.awt.Rectangle
import java.io.InputStream
import java.io.OutputStream

/**
 * @author vlitvinenko
 */
class ShapeStorage(fileName: String, initialSize: Long) :
    AbstractStorage<Shape>(MemoryMappedFile(fileName, initialSize), RECORD_SIZE) {

    private val serializer = object : AbstractSerializer<Shape>() {
        private val kryo = Kryo()

        init {
            kryo.register(Shape::class.java)
            kryo.register(AbstractShape::class.java)
            kryo.register(RectangleShape::class.java)
            kryo.register(CircleShape::class.java)
            kryo.register(Rectangle::class.java)
        }

        override fun write(os: OutputStream, shape: Shape) = Output(os).use {
            kryo.writeClassAndObject(it, shape)
            it.flush()
        }

        override fun read(input: InputStream) = Input(input).use {
            kryo.readClassAndObject(it) as Shape
        }
    }

    override fun getSerializer(): Serializer<Shape> = serializer

    override fun add(shape: Shape): Long =
        super.add(shape).also { shape.offset = it }

    override fun read(offset: Long): Shape =
        super.read(offset).also { it.offset = offset }

    private companion object {
        private const val RECORD_SIZE = 128
    }
}