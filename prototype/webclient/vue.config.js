const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: false,
  publicPath: './',
  chainWebpack: config =>
    config.externals = {
    myDataFileVariable: './config/mqtt.json'
  }
})


