// Record decoded Wasm sizes in the entry bundle before it starts any downloads.
// This works for both direct development runs and the renamed compatibility bundles.
//
// The Kotlin Gradle plugin appends every file in `webApp/webpack.config.d/` to the
// generated `build/{js,wasm}/packages/HachimiWorld-webApp/webpack.config.js`, where
// `config` is the webpack config it built. So this runs at build time, in every JS or
// Wasm webpack task (dev server, production webpack, compatibility distribution).
//
// Why: the loading page in `src/webMain/resources/index.html` shows a percentage.
// Response streams yield decoded bytes, but CDNs that compress (Brotli/gzip) send no
// Content-Length, or one that counts encoded bytes. The page reads
// `globalThis.__hachimiWasmSizes` as the total instead.
config.plugins.push({
    apply(compiler) {
        const pluginName = "HachimiWasmProgress";
        const { Compilation, sources } = compiler.webpack;
        compiler.hooks.thisCompilation.tap(pluginName, (compilation) => {
            compilation.hooks.processAssets.tap({
                name: pluginName,
                // Read the final filenames after Webpack's real content hash pass.
                stage: Compilation.PROCESS_ASSETS_STAGE_REPORT
            }, () => {
                const sizes = Object.fromEntries(compilation.getAssets()
                    .filter(asset => asset.name.endsWith(".wasm"))
                    .map(asset => [asset.name, asset.source.size()]));
                const banner = `globalThis.__hachimiWasmSizes = ${JSON.stringify(sizes)};\n`;
                const entryFiles = new Set(Array.from(compilation.entrypoints.values())
                    .flatMap(entry => entry.getFiles())
                    .filter(name => name.endsWith(".js")));
                for (const name of entryFiles) {
                    compilation.updateAsset(name, source =>
                        new sources.ConcatSource(banner, source));

                    // Source maps were already generated at this stage. The banner adds one
                    // generated line, and each ";" in `mappings` stands for one line.
                    const related = compilation.getAsset(name)?.info.related?.sourceMap;
                    const mapNames = related ? [].concat(related) : [`${name}.map`];
                    for (const mapName of mapNames) {
                        const mapAsset = compilation.getAsset(mapName);
                        if (!mapAsset) continue;
                        const map = JSON.parse(mapAsset.source.source().toString());
                        if (typeof map.mappings !== "string") continue;
                        map.mappings = ";" + map.mappings;
                        compilation.updateAsset(mapName, new sources.RawSource(JSON.stringify(map)));
                    }
                }
            });
        });
    }
});
