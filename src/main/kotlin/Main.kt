import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.lang.ClassLoader.getSystemClassLoader

private fun buildMapper(): ObjectMapper {
    val module = SimpleModule().addDeserializer(AbiElement.Event::class.java, AbiElement.Event.Deserializer)
    return jacksonObjectMapper()
        .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .registerModule(module)
}

private fun handleAbi(mapper: ObjectMapper) {
    val json = getSystemClassLoader().getResource("abi.json")!!.readText()
    val abi = mapper.readValue(json, object : TypeReference<List<AbiElement>>() {})
    println("ABI: $abi")
}

private fun handleOpenApi(mapper: ObjectMapper) {
    val openApi = OpenApi(
        OpenApi.Version.`3_0_3`,
        OpenApi.Info(
            version = "0.1.0",
            name = "FranCairo",
            description = "JSON RPC spec for interacting with FranCairo.",
            OpenApi.Info.Contact("Franco Barpp Gomes", "https://github.com/Hyodar/", "franco@nethermind.io"),
            OpenApi.Info.License("MIT", "https://mit-license.org/"),
        ),
        listOf(
            OpenApi.Server("https://alpha-mainnet.starknet.io/feeder_gateway/call_contract", "Mainnet"),
            OpenApi.Server("https://alpha4.starknet.io/feeder_gateway/call_contract", "Goerli"),
            OpenApi.Server("https://alpha4-2.starknet.io/feeder_gateway/call_contract", "Goerli 2"),
        ),
        tags = listOf(
            OpenApi.Tag("function"),
            OpenApi.Tag("constructor"),
            OpenApi.Tag("l1_handler"),
            OpenApi.Tag("event"),
            OpenApi.Tag("struct"),
            OpenApi.Tag("enum"),
            OpenApi.Tag("interface"),
            OpenApi.Tag("impl"),
        ),
        paths = mapOf(
            "/" to OpenApi.Path(
                description = "Counter",
                OpenApi.Path.Post(
                    tags = listOf("function"),
                    description = "Get the count.",
                    operationId = "getCount",
                    requestBody = OpenApi.Ref("#/components/requestBodies/GetCount"),
                    responses = mapOf("200" to OpenApi.Ref("#/components/responses/GetCount")),
                ),
            ),
        ),
        OpenApi.Components(
            mapOf(
                "GetCount" to
            ),
        ),
    )
    val json = mapper.writeValueAsString(openApi)
    File("openapi.yaml").writeText(json)
    println("Wrote the OpenAPI spec to openapi.yaml.")
}

fun main() {
    val mapper = buildMapper()
    handleAbi(mapper)
    handleOpenApi(mapper)
}
