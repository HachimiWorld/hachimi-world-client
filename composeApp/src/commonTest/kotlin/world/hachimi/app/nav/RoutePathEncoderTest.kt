package world.hachimi.app.nav

import kotlinx.serialization.ExperimentalSerializationApi
import world.hachimi.app.model.SearchViewModel
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSerializationApi::class)
class RoutePathEncoderTest {
    @Test
    fun `encode without parameters`() {
        val route: Route = Route.Root.Home.Main
        val result = encodeToBrowserPath(route)

        assertEquals("/home/main", result.path)
        assertEquals(emptyMap<String, String?>(), result.parameters)
    }

    @Test
    fun `encode with class discriminator`() {
        val route: Route = Route.Root.Search(
            query = "test",
            type = SearchViewModel.SearchType.USER
        )
        val result = encodeToBrowserPath(route)
        assertEquals("/search", result.path)
        assertEquals(
            mapOf(
                "query" to "test",
                "type" to "USER"
            ),
            result.parameters
        )
        val encoded = result.encodeToString()
        assertEquals(
            "/search?query=test&type=USER",
            encoded
        )
    }

    @Test
    fun `encode without polymorphism`() {
        val route: Route.Root.Home.Category = Route.Root.Home.Category("rock")
        val result = encodeToBrowserPath(route)
        assertEquals("/home/category", result.path)
        assertEquals(mapOf("category" to "rock"), result.parameters)
    }

    @Test
    fun `encode browser path`() {
        assertEquals(
            "home/category?category=rock%20%26%20ha",
            BrowserPath("home/category", mapOf("category" to "rock & ha")).encodeToString()
        )

        assertEquals(
            "home/category?order=desc&param3=value3",
            BrowserPath(
                "home/category",
                mapOf("category" to null, "order" to "desc", "param3" to "value3")
            ).encodeToString()
        )


        assertEquals(
            "home/category",
            BrowserPath("home/category", mapOf("category" to null)).encodeToString()
        )

        assertEquals(
            "home/category",
            BrowserPath("home/category", emptyMap()).encodeToString()
        )
    }

    @Test
    fun `decode browser path`() {
        assertEquals(
            BrowserPath("home/category", mapOf("category" to "rock")),
            BrowserPath.decodeFromString("home/category?category=rock")
        )

        assertEquals(
            BrowserPath("home/category", mapOf("category" to null, "order" to "desc")),
            BrowserPath.decodeFromString("home/category?category&order=desc")
        )

        assertEquals(
            BrowserPath("home/category", mapOf("category" to null)),
            BrowserPath.decodeFromString("home/category?category")
        )

        assertEquals(
            BrowserPath("home/category", emptyMap()),
            BrowserPath.decodeFromString("home/category")
        )
    }
}