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
        val (name, parameters) = encodeToRoutePath(route)
        println(name)
        println(parameters)
        assertEquals("/home/main", name)
        assertEquals(emptyMap<String, String?>(), parameters)
    }

    @Test
    fun `encode with class discriminator`() {
        val route: Route = Route.Root.Search(
            query = "test",
            type = SearchViewModel.SearchType.USER
        )
        val (name, parameters) = encodeToRoutePath(route)
        println(name)
        println(parameters)
        assertEquals("/search", name)
        assertEquals(
            mapOf(
                "query" to "test",
                "type" to "1"
            ),
            parameters
        )
    }

    @Test
    fun `encode without polymorphism`() {
        val route: Route.Root.Home.Category = Route.Root.Home.Category("rock")
        val (name, parameters) = encodeToRoutePath(route)
        println(name)
        println(parameters)
        assertEquals("/home/category", name)
        assertEquals(mapOf("category" to "rock"), parameters)
    }

    @Test
    fun `build url fragments`() {
        assertEquals(
            "home/category?category=rock",
            buildBrowserPath("home/category", mapOf("category" to "rock"))
        )

        assertEquals(
            "home/category?order=desc",
            buildBrowserPath("home/category", mapOf("category" to null, "order" to "desc"))
        )

        assertEquals(
            "home/category",
            buildBrowserPath("home/category", mapOf("category" to null))
        )

        assertEquals(
            "home/category",
            buildBrowserPath("home/category", emptyMap())
        )
    }
}