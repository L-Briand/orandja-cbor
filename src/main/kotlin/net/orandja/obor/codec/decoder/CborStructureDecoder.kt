package net.orandja.obor.codec.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import net.orandja.obor.annotations.CborRawBytes
import net.orandja.obor.annotations.CborTag
import net.orandja.obor.codec.MAJOR_MAP
import net.orandja.obor.codec.reader.CborReader

@ExperimentalSerializationApi
@InternalSerializationApi
@ExperimentalUnsignedTypes
internal class CborStructureDecoder(reader: CborReader, serializersModule: SerializersModule) :
    CborCollectionDecoder(reader, serializersModule) {
    override val major: UByte = MAJOR_MAP

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (super.decodeElementIndex(descriptor) == CompositeDecoder.DECODE_DONE) return CompositeDecoder.DECODE_DONE
        var index = descriptor.getElementIndex(decodeString())
        // element decoded not inside kotlin object representation
        while (index == CompositeDecoder.UNKNOWN_NAME) {
            decodeValue() // discard read element.
            if (super.decodeElementIndex(descriptor) == CompositeDecoder.DECODE_DONE) return CompositeDecoder.DECODE_DONE
            index = descriptor.getElementIndex(decodeString())
        }

        requiredTag = (descriptor.getElementAnnotations(index).find { it is CborTag } as? CborTag)?.tag ?: -1

        return index
    }
}


