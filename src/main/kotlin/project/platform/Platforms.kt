package ua.pp.lumivoid.project.platform

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder

enum class Platforms(val platformName: String, val realisation: Platform) {
//    ForgePlatform("forge", Forge),
    FabricPlatform("fabric", Fabric);
//    NeoForgePlatform("neoforge", NeoForge),
//    QuiltPlatform("quilt", Quilt);

    @OptIn(ExperimentalSerializationApi::class)
    @Serializer(forClass = Platforms::class)
    object PlatformsSerializer : KSerializer<Platforms> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Platforms", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): Platforms {
            return Platforms.entries.find { it.platformName == decoder.decodeString().lowercase() }!!
        }
    }
}