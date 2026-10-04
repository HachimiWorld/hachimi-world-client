// Local Web development against remote backends without CORS.
//
// The release API/storage only allow https://hachimi.world, so a page served from
// localhost gets blocked. Like the Vite/CRA proxy, this makes every backend request
// same-origin and lets the dev server forward it:
//
//   fetch("https://api.hachimi.world/song/x")
//     -> fetch("/__proxy/api.hachimi.world/song/x")      (same origin, no CORS)
//     -> dev server -> https://api.hachimi.world/song/x  (server to server)
//
// The app builds absolute URLs (BuildKonfig.API_BASE_URL, cover URLs from API
// responses), so instead of switching base URLs per platform, a banner wraps
// `fetch` and rewrites them. Ktor's browser engine and Coil both go through fetch.
// Audio plays via <audio> (Howler html5 mode), which needs no CORS, so it stays direct.
//
// Only active for the dev server (`*BrowserDevelopmentRun`); webpack bundles for
// deployment are left untouched.
if (process.argv.includes("serve")) {
    const PREFIX = "/__proxy/";
    // Hosts the proxy may forward to. Anything else falls through to a 404, so the
    // dev server never acts as an open proxy on the LAN.
    const ALLOWED_HOST_SUFFIXES = ["hachimi.world", "konyaco.com", "r2.dev"];

    const isAllowedHost = (hostname) =>
        ALLOWED_HOST_SUFFIXES.some(s => hostname === s || hostname.endsWith("." + s));
    // "/__proxy/api.hachimi.world/song/x?a=1" -> ["api.hachimi.world", "/song/x?a=1"]
    const splitProxyPath = (path) => {
        const rest = path.slice(PREFIX.length);
        const slash = rest.indexOf("/");
        return slash < 0 ? [rest, "/"] : [rest.slice(0, slash), rest.slice(slash)];
    };

    config.devServer = config.devServer || {};
    config.devServer.proxy = [
        ...(config.devServer.proxy || []),
        {
            pathFilter: (path) => path.startsWith(PREFIX)
                && isAllowedHost(splitProxyPath(path)[0].split(":")[0]),
            router: (req) => "https://" + splitProxyPath(req.url)[0],
            pathRewrite: (path) => splitProxyPath(path)[1],
            changeOrigin: true,
            // Look like the deployed site to the backends (native clients send the same Referer).
            headers: { Referer: "https://hachimi.world/" },
            on: {
                proxyReq: (proxyReq) => proxyReq.removeHeader("origin"),            },
        },
    ];

    const fetchRewrite = `;(() => {
    const PREFIX = ${JSON.stringify(PREFIX)};
    const SUFFIXES = ${JSON.stringify(ALLOWED_HOST_SUFFIXES)};
    const rewrite = (raw) => {
        let url;
        try { url = new URL(raw, location.href); } catch (e) { return null; }
        if (url.protocol !== "https:" || url.origin === location.origin) return null;
        if (!SUFFIXES.some(s => url.hostname === s || url.hostname.endsWith("." + s))) return null;
        return location.origin + PREFIX + url.host + url.pathname + url.search;
    };
    const originalFetch = globalThis.fetch.bind(globalThis);
    globalThis.fetch = (input, init) => {
        if (input instanceof Request) {
            const proxied = rewrite(input.url);
            if (proxied) return originalFetch(new Request(proxied, input), init);
        } else {
            const proxied = rewrite(String(input));
            if (proxied) return originalFetch(proxied, init);
        }
        return originalFetch(input, init);
    };
    console.info("[dev-proxy] cross-origin backend requests go through " + PREFIX);
})();
`;
    const webpack = require("webpack");
    config.plugins.push(new webpack.BannerPlugin({
        banner: fetchRewrite,
        raw: true,
        entryOnly: true,
        test: /\.m?js$/,
    }));
}
