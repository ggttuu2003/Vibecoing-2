import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 120000 // 2 分钟超时
})

export const analyzeImage = async (file, config) => {
  const formData = new FormData()
  formData.append('image', file)
  formData.append('enableAI', config.enableAI)
  formData.append('enableOCR', config.enableOCR)
  formData.append('enableCV', config.enableCV)

  try {
    const response = await api.post('/analyze', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.data.code === 200) {
      return response.data.data
    } else {
      throw new Error(response.data.message || '分析失败')
    }
  } catch (error) {
    if (error.response) {
      throw new Error(error.response.data.message || '服务器错误')
    } else if (error.request) {
      throw new Error('网络错误，请检查后端服务是否启动')
    } else {
      throw new Error(error.message)
    }
  }
}

// 获取图片生成历史记录
export const getImageHistory = async (page = 1, size = 10, style = '') => {
  try {
    const params = { page, size }
    if (style) {
      params.style = style
    }
    const response = await api.get('/history/list', { params })
    if (response.data.code === 200) {
      return response.data.data
    } else {
      throw new Error(response.data.message || '获取历史记录失败')
    }
  } catch (error) {
    throw new Error(error.response?.data?.message || error.message)
  }
}

// 删除图片生成历史记录
export const deleteImageHistory = async (historyId) => {
  try {
    const response = await api.delete(`/history/${historyId}`)
    if (response.data.code === 200) {
      return true
    } else {
      throw new Error(response.data.message || '删除失败')
    }
  } catch (error) {
    throw new Error(error.response?.data?.message || error.message)
  }
}

// 获取设计稿解析历史记录
export const getAnalysisHistory = async (page = 1, size = 10) => {
  try {
    const response = await api.get('/history/analysis/list', {
      params: { page, size }
    })
    if (response.data.code === 200) {
      return response.data.data
    } else {
      throw new Error(response.data.message || '获取解析历史失败')
    }
  } catch (error) {
    throw new Error(error.response?.data?.message || error.message)
  }
}

// 删除设计稿解析历史记录
export const deleteAnalysisHistory = async (historyId) => {
  try {
    const response = await api.delete(`/history/analysis/${historyId}`)
    if (response.data.code === 200) {
      return true
    } else {
      throw new Error(response.data.message || '删除失败')
    }
  } catch (error) {
    throw new Error(error.response?.data?.message || error.message)
  }
}
