import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue

data class OpenApi(
    @JsonProperty("openapi") val version: Version,
    val info: Info,
    val servers: List<Server>,
    val tags: List<Tag>,
    val paths: Map<String, Path>,
    val components: Components,
) {

    data class Ref(val `$ref`: String)

    enum class Version(@JsonValue private val value: String) {
        `3_0_3`("3.0.3"),
    }

    data class Info(
        val version: String,
        val name: String,
        val description: String,
        val contact: Contact,
        val license: License,
    ) {

        data class Contact(val name: String, val url: String, val email: String)

        data class License(val name: String, val url: String)
    }

    data class Server(val url: String, val description: String)

    data class Tag(val name: String)

    data class Path(val description: String, val post: Post) {
        data class Post(
            val tags: List<String>,
            val description: String,
            val operationId: String,
            val requestBody: Ref,
            val responses: Map<String, Ref>,
        )
    }

    data class Components(val schemas: Map<String, Schema>) {
        data class Schema(val properties: Map<String, Property>) {
            val type: String = "object"

            data class Property(val type: String, val items: List<Item>) {
                data class Item(val type: String)
            }
        }
    }
}
