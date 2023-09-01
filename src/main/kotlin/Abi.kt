import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.KClass

typealias Abi = List<AbiElement>

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = AbiElement.Constructor::class, name = "constructor"),
    JsonSubTypes.Type(value = AbiElement.Event::class, name = "event"),
    JsonSubTypes.Type(value = AbiElement.Function::class, name = "function"),
    JsonSubTypes.Type(value = AbiElement.Impl::class, name = "impl"),
    JsonSubTypes.Type(value = AbiElement.Interface::class, name = "interface"),
    JsonSubTypes.Type(value = AbiElement.L1Handler::class, name = "l1_handler"),
    JsonSubTypes.Type(value = AbiElement.Struct::class, name = "struct"),
)
sealed class AbiElement {
    data class Function(
        val name: String,
        val inputs: List<Input>,
        val outputs: List<Output>,
        val state_mutability: StateMutability,
    ) : AbiElement() {
        val type: String = "function"

        data class Input(val name: String, val type: String)

        data class Output(val type: String?)

        enum class StateMutability(@JsonValue private val value: String) {
            VIEW("view"),
            EXTERNAL("external"),
        }
    }

    data class Constructor(val inputs: List<Input>) : AbiElement() {
        val type: String = "constructor"
        val name: String = "constructor"

        data class Input(val name: String, val type: String)
    }

    data class L1Handler(
        val name: String,
        val inputs: List<Input>,
        val outputs: List<Output>,
        val state_mutability: StateMutability,
    ) : AbiElement() {

        val type: String = "l1_handler"

        data class Input(val name: String, val type: String)

        data class Output(val output: String?)

        enum class StateMutability(@JsonValue private val value: String) {
            VIEW("view"),
            EXTERNAL("external"),
        }
    }

    data class EventField(
        val name: String,
        /** The Cairo type of the member or variant, including namespacing. */
        val type: String,
        val kind: Kind,
    ) {

        /**
         * Specifies how the field should be serialized, via the <starknet::Event> trait or the <serde::Serde> trait.
         */
        enum class Kind(@JsonValue private val value: String) {
            KEY("key"),
            DATA("data"),
            NESTED("nested"),
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "kind")
    @JsonSubTypes(
        JsonSubTypes.Type(value = Event.EnumEvent::class, name = "enum"),
        JsonSubTypes.Type(value = Event.StructEvent::class, name = "struct"),
    )
    sealed class Event : AbiElement() {
        abstract val name: String
        val type: String = "event"

        data class EnumEvent(override val name: String, val variants: List<EventField>) : Event() {
            /** Determines the serialization of the corresponding type. */
            val kind: String = "enum"
        }

        data class StructEvent(override val name: String, val members: List<EventField>) : Event() {
            /** Determines the serialization of the corresponding type. */
            val kind: String = "struct"
        }

        object Deserializer : JsonDeserializer<Event>() {
            override fun deserialize(parser: JsonParser, context: DeserializationContext): Event {
                val node = parser.codec.readTree<JsonNode>(parser)
                val clazz: KClass<out Event> = when (val type = node["kind"].asText()) {
                    "enum" -> EnumEvent::class
                    "struct" -> StructEvent::class
                    else -> throw IllegalArgumentException("$type didn't match a concrete class.")
                }
                return parser.codec.treeToValue(node, clazz.java)
            }
        }
    }

    data class Struct(
        /** The (Cairo) struct name, including namespacing. */
        val name: String,
        val members: List<Member>,
    ) : AbiElement() {

        val type: String = "struct"

        data class Member(
            val name: String,
            /** The member type, including namespacing. */
            val type: String,
        )
    }

    data class Enum(
        /** The (Cairo) enum name, including namespacing. */
        val name: String,
        val variants: List<Variant>,
    ) : AbiElement() {

        val type: String = "enum"

        data class Variant(
            val name: String,
            /** The variant type, including namespacing. */
            val type: String,
        )
    }

    data class Interface(
        /** The name of the trait which defines the contract interface. */
        val name: String,
        val items: List<Function>,
    ) : AbiElement() {

        val type: String = "interface"
    }

    data class Impl(
        /** The name of an implementation containing contract entry points. */
        val name: String,
        /** The name of the trait corresponding to this implementation. */
        val interface_name: String,
    ) : AbiElement() {

        val type: String = "impl"
    }
}
