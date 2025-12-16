import axios from 'axios';
// import responseData from '@/views/response.json';
import responseData from '@/views/json_response.json';

/**
 * 模拟 axios 请求的工具函数
 * 模拟向服务器发送请求但实际上不真正连接服务器，而是直接返回本地准备好的 response.json 数据
 * 后期跟服务器联调时再请求服务端
 * 
 * @param {string} url - 请求 URL（模拟用，实际不发送请求）
 * @param {string} method - 请求方法（GET/POST/PUT/DELETE）
 * @param {Object} data - 请求数据（模拟用）
 * @param {Object} headers - 请求头（模拟用）
 * @returns {Promise} - 返回 Promise，解析为本地 response.json 数据
 */
const mockAxiosRequest = async (url, method = 'GET', data = null, headers = {}) => {
  try {
    // 1. 模拟请求延迟（150ms ~ 500ms）
    const delay = Math.floor(Math.random() * 350) + 150;
    
    // 2. 创建模拟的 axios 配置对象
    const config = {
      url,
      method: method.toUpperCase(),
      data,
      headers: {
        'Content-Type': 'application/json',
        ...headers,
      },
    };

    // 3. 模拟网络请求延迟
    await new Promise(resolve => setTimeout(resolve, delay));

    // 4. 检查请求方法是否支持
    const supportedMethods = ['GET', 'POST', 'PUT', 'DELETE'];
    if (!supportedMethods.includes(config.method)) {
      throw new Error(`Unsupported HTTP method: ${config.method}`);
    }

    // 5. 模拟成功响应 - 直接返回本地 response.json 数据
    const mockResponse = {
      status: 200,
      statusText: 'OK',
      data: {
        success: true,
        message: '请求成功',
        data: responseData, // 导入本地 JSON 数据
        timestamp: new Date().toISOString(),
      },
      config,
      headers: {
        'content-type': 'application/json',
        'x-powered-by': 'Mock Server',
      },
    };

    return mockResponse;
  } catch (error) {
    // 6. 错误处理
    console.error('Mock axios request failed:', error);
    
    // 模拟错误响应
    const errorResponse = {
      status: 500,
      statusText: 'Internal Server Error',
      data: {
        success: false,
        message: error.message || '请求失败',
        error: error.toString(),
        timestamp: new Date().toISOString(),
      },
      config: {
        url,
        method: method.toUpperCase(),
        data,
        headers,
      },
    };

    throw errorResponse;
  }
};

/**
 * 创建模拟的 axios 实例
 * 提供与真实 axios 相同的 API 接口
 */
const mockAxios = {
  // GET 请求
  get: (url, config = {}) => {
    return mockAxiosRequest(url, 'GET', null, config.headers);
  },
  
  // POST 请求
  post: (url, data = null, config = {}) => {
    return mockAxiosRequest(url, 'POST', data, config.headers);
  },
  
  // PUT 请求
  put: (url, data = null, config = {}) => {
    return mockAxiosRequest(url, 'PUT', data, config.headers);
  },
  
  // DELETE 请求
  delete: (url, config = {}) => {
    return mockAxiosRequest(url, 'DELETE', null, config.headers);
  },
  
  // 创建实例（模拟）
  create: (config = {}) => {
    return {
      ...mockAxios,
      defaults: {
        ...config,
      },
    };
  },
};

/**
 * 使用示例：
 * 
 * // 导入
 * import mockAxios from '@/utils/mockAxios';
 * 
 * // GET 请求
 * mockAxios.get('/api/report')
 *   .then(response => {
 *     console.log('Success:', response.data);
 *   })
 *   .catch(error => {
 *     console.error('Error:', error);
 *   });
 * 
 * // POST 请求
 * mockAxios.post('/api/report', { region: '中部大区' })
 *   .then(response => {
 *     console.log('Success:', response.data);
 *   })
 *   .catch(error => {
 *     console.error('Error:', error);
 *   });
 * 
 * // 后期联调时，只需将 mockAxios 替换为真实的 axios 即可
 * // import axios from 'axios';
import responseData from '@/views/response.json';
 */

export default mockAxios;