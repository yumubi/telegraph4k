package io.goji.io.goji.telegraph

import io.goji.io.goji.telegraph.models.NodeElement
import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*

object NodeSerializer : JsonContentPolymorphicSerializer<Any>(Any::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Any> = when {
        element is JsonPrimitive -> String.serializer()
        element is JsonObject -> NodeElement.serializer()
        else -> throw SerializationException("Unknown element type")
    }
}
