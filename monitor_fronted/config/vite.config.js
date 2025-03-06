import vue from "@vitejs/plugin-vue";
import {defineConfig} from "vite";
import {visualizer} from "rollup-plugin-visualizer"; // Changed: added curly braces
import viteCompression from "vite-plugin-compression";
import {resolve} from 'path';

const viteCompressionFilter = /\.(js|mjs|json|css|html|svg)$/i;

// https://vitejs.dev/config/
export default defineConfig({
    server: {
        port: 3000,
    },
    define: {
        "FRONTEND_VERSION": JSON.stringify(process.env.npm_package_version),
        "process.env": {},
    },
    plugins: [
        vue(),
        visualizer({
            filename: "tmp/dist-stats.html"
        }),
        viteCompression({
            algorithm: "gzip",
            filter: viteCompressionFilter,
        }),
        viteCompression({
            algorithm: "brotliCompress",
            filter: viteCompressionFilter,
        }),
    ],
    css: {
        preprocessorOptions: {
            scss: {
                // SCSS options here if needed
            }
        }
    },
    resolve: {
        alias: {
            '@': resolve(__dirname, '../src')
        }
    },
    optimizeDeps: {
        exclude: ['postcss-scss']
    },
    build: {
        commonjsOptions: {
            include: [/.js$/],
            transformMixedEsModules: true
        },
        rollupOptions: {
            output: {
                manualChunks(id) {
                    if (id.includes('node_modules')) {
                        return 'vendor';
                    }
                }
            }
        }
    }
});
