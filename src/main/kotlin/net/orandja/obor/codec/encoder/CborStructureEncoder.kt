package net.orandja.obor.codec.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborInfinite
import net.orandja.obor.annotations.CborRawBytes
import net.orandja.obor.codec.Descriptors
import net.orandja.obor.codec.HEADER_MAP_INFINITE
import net.orandja.obor.codec.HEADER_MAP_START
import net.orandja.obor.codec.writer.CborWriter

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal class CborStructureEncoder(out: CborWriter, serializersModule: SerializersModule, chunkSize: Int) :
    CborCollectionEncoder(out, serializersModule, chunkSize) {
    override val finiteToken: UByte = HEADER_MAP_START
    override val infiniteToken: UByte = HEADER_MAP_INFINITE

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        super.encodeString(descriptor.getElementName(index))
        chunkSize = (descriptor.getElementAnnotations(index).find { it is CborInfinite } as? CborInfinite)?.chunkSize ?: -1
        isRawBytes = descriptor.getElementAnnotations(index).any { it is CborRawBytes }
        return true
    }

    override fun encodeString(value: String) {
        val chunkSize = if(chunkSize == 0) 1 else chunkSize
        if (chunkSize in value.indices) {
            encodeStructure(Descriptors.infiniteText) {
                value.chunked(chunkSize).forEachIndexed { idx, it ->
                    this.encodeStringElement(Descriptors.string, idx, it)
                }
            }
        }
        else super.encodeString(value)
    }
}