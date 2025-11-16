package ua.pp.lumivoid.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder

enum class OS {
    WINDOWS, MACOS, LINUX;

    companion object {
        private val curr = System.getProperty("os.name").lowercase()

        fun current(): OS = when {
            curr.contains("win") -> WINDOWS
            curr.contains("mac") -> MACOS
            else -> LINUX
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Serializer(forClass = OS::class)
    object OsSerializer : KSerializer<OS> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OS", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): OS = OS.current()
    }
}