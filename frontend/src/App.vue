<template>
  <div class="min-h-screen">
    <!-- 导航栏 -->
    <nav class="bg-white/95 backdrop-blur-lg border-b border-gray-200 shadow-sm sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-8">
            <h1 class="text-2xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
              AI 工具集
            </h1>
            <div class="flex space-x-2">
              <router-link
                to="/"
                class="px-4 py-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-xl transition font-medium"
                active-class="bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-sm"
              >
                设计稿解析
              </router-link>
              <router-link
                to="/generate"
                class="px-4 py-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-xl transition font-medium"
                active-class="bg-gradient-to-r from-indigo-500 to-purple-500 text-white shadow-sm"
              >
                图片生成
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- 路由视图 -->
    <router-view />
  </div>
</template>

<script setup>
// 无需额外逻辑
</script>

<style>
/* 全局样式保持不变 */
</style>
