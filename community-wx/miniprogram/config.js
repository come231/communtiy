/**
 * 星云社区小程序 — 运行环境配置（与 Web 端共用网关 community-gateway，默认 8095）
 *
 * 真机调试：将 baseUrl 改为本机局域网 IP，例如 http://192.168.1.10:8095
 * 生产环境：改为 HTTPS 网关域名，并在小程序后台配置「服务器域名」request 合法域名
 */
var ENV = 'development'

var CONFIG = {
  development: {
    baseUrl: 'http://localhost:8095',
    requestTimeoutMs: 30000,
  },
  production: {
    baseUrl: 'https://your-api.example.com',
    requestTimeoutMs: 30000,
  },
}

function getConfig() {
  return CONFIG[ENV] || CONFIG.development
}

module.exports = {
  ENV: ENV,
  getConfig: getConfig,
}
