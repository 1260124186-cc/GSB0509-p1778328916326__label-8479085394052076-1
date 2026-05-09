const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    host: '0.0.0.0',
    port: 80,
    allowedHosts: 'all',
    client: {
      webSocketURL: 'auto://0.0.0.0:0/ws'
    },
    proxy: {
      '/api': {
        target: 'http://backend:8080',
        changeOrigin: true
      }
    }
  }
})
