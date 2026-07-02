package world.hachimi.app.nav

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import world.hachimi.app.model.SearchViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalSerializationApi::class)
class RoutePathDecoderTest {
    @Test
    fun `decode route without parameters`() {
        assertEquals(
            Route.Root.Home.Main,
            decodeFromBrowserPathString("/home/main")
        )
    }

    @Test
    fun `decode route with parameters`() {
        assertEquals(
            Route.Root.Home.Category("rock"),
            decodeFromBrowserPathString("/home/category?category=rock")
        )
    }

    @Test
    fun `decode defaulted parameters when omitted`() {
        assertEquals(
            Route.Auth(),
            decodeFromBrowserPathString("/auth")
        )
    }

    @Test
    fun `decode duplicate path based on presence of parameters`() {
        assertEquals(
            Route.Root.Events.Feed,
            decodeFromBrowserPathString("/events/feed")
        )
        assertEquals(
            Route.Root.Events.Detail(42),
            decodeFromBrowserPathString("/events/detail?postId=42")
        )
    }

    @Test
    fun `round trip encoded browser path`() {
        val route: Route = Route.Root.Search(
            query = "rock & roll",
            type = SearchViewModel.SearchType.USER
        )
        val result = encodeToBrowserPath(route)

        assertEquals(
            route,
            decodeFromBrowserPathString(result.encodeToString())
        )
    }

    @Test
    fun `decode enum by name for compatibility`() {
        assertEquals(
            Route.Root.Search(
                query = "alice",
                type = SearchViewModel.SearchType.PLAYLIST
            ),
            decodeFromBrowserPathString("/search?query=alice&type=PLAYLIST")
        )
    }

    @Test
    fun `reject invalid required parameters`() {
        assertFailsWith<SerializationException> {
            decodeFromBrowserPathString("/playlist?playlistId=oops")
        }
    }

    @Test
    fun `reject unknown paths`() {
        assertFailsWith<SerializationException> {
            decodeFromBrowserPathString("/unknown")
        }
    }
}

