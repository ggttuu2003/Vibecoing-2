/**
 * HTML 导出工具
 * 将 JSON 模板导出为独立的 HTML 文件
 */

/**
 * 生成文字组件的 HTML
 */
function generateTextHTML(component) {
  const { position, size, style, content } = component

  const styleStr = `
    position: absolute;
    left: ${position.x}px;
    top: ${position.y}px;
    width: ${size.width}px;
    height: ${size.height}px;
    font-size: ${style?.fontSize || 16}px;
    font-weight: ${style?.fontWeight || 400};
    color: ${style?.color || '#000000'};
    text-align: ${style?.textAlign || 'left'};
    line-height: ${style?.lineHeight || 1.5};
    font-family: ${style?.fontFamily || 'PingFang SC, -apple-system, sans-serif'};
    text-shadow: ${style?.textShadow || 'none'};
    display: flex;
    align-items: center;
    word-wrap: break-word;
  `.trim()

  return `<div style="${styleStr}">${content}</div>`
}

/**
 * 生成按钮组件的 HTML
 */
function generateButtonHTML(component) {
  const { position, size, style, text, backgroundImage } = component

  // 基础样式
  let styleStr = `
    position: absolute;
    left: ${position.x}px;
    top: ${position.y}px;
    width: ${size.width}px;
    height: ${size.height}px;
    color: ${style?.textColor || '#FFFFFF'};
    font-size: ${style?.fontSize || 16}px;
    font-weight: ${style?.fontWeight || 600};
    border-radius: ${style?.borderRadius || 8}px;
    border: ${style?.border || 'none'};
    box-shadow: ${style?.boxShadow || '0 2px 4px rgba(0,0,0,0.1)'};
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
    font-family: -apple-system, sans-serif;
  `.trim()

  // 如果有背景图片，使用背景图；否则使用背景色
  if (backgroundImage) {
    styleStr += `; background-image: url(${backgroundImage}); background-size: cover; background-position: center;`
  } else {
    styleStr += `; background-color: ${style?.backgroundColor || '#3B82F6'};`
  }

  return `<button style="${styleStr}">${text}</button>`
}

/**
 * 生成图片组件的 HTML
 */
function generateImageHTML(component) {
  const { position, size, style, placeholder } = component

  const containerStyle = `
    position: absolute;
    left: ${position.x}px;
    top: ${position.y}px;
    width: ${size.width}px;
    height: ${size.height}px;
    border-radius: ${style?.borderRadius || 0}px;
    overflow: hidden;
    filter: ${style?.filter || 'none'};
  `.trim()

  if (placeholder?.url) {
    const imgStyle = `
      width: 100%;
      height: 100%;
      object-fit: ${style?.objectFit || 'cover'};
    `.trim()

    return `
      <div style="${containerStyle}">
        <img src="${placeholder.url}" alt="${placeholder?.alt || '图片'}" style="${imgStyle}">
      </div>
    `
  } else {
    // 使用占位符颜色
    const placeholderStyle = `
      width: 100%;
      height: 100%;
      background-color: ${placeholder?.dominantColor || '#E5E7EB'};
      display: flex;
      align-items: center;
      justify-content: center;
    `.trim()

    return `
      <div style="${containerStyle}">
        <div style="${placeholderStyle}">
          <svg width="48" height="48" fill="none" stroke="#9CA3AF" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
        </div>
      </div>
    `
  }
}

/**
 * 生成组件 HTML
 */
function generateComponentHTML(component) {
  switch (component.type) {
    case 'text':
      return generateTextHTML(component)
    case 'button':
      return generateButtonHTML(component)
    case 'image':
      return generateImageHTML(component)
    default:
      return ''
  }
}

/**
 * 导出为 HTML 文件
 * @param {Object} template - JSON 模板
 * @param {string} filename - 文件名（可选）
 */
export function exportToHTML(template, filename = 'preview.html') {
  if (!template || !template.page || !template.components) {
    throw new Error('Invalid template format')
  }

  const { page, components } = template

  // 生成所有组件的 HTML
  const componentsHTML = components
    .map(comp => generateComponentHTML(comp))
    .join('\n    ')

  // 生成完整的 HTML
  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>AI 设计稿预览</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
    }

    .container {
      background: white;
      border-radius: 12px;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
      overflow: auto;
      max-height: 95vh;
    }

    .page-container {
      position: relative;
      width: ${page.width}px;
      height: ${page.height}px;
      background-color: ${page.backgroundColor};
      background-image: ${page.backgroundImage ? `url(${page.backgroundImage})` : 'none'};
      background-size: cover;
      background-position: center;
      background-repeat: no-repeat;
      overflow: hidden;
      margin: 0 auto;
    }

    .info {
      background: #f8f9fa;
      padding: 12px 20px;
      border-bottom: 1px solid #e9ecef;
      text-align: center;
      font-size: 14px;
      color: #6c757d;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="info">
      📐 ${page.width} × ${page.height}px |
      📦 ${components.length} 个组件 |
      🎨 由 AI 自动生成
    </div>
    <div class="page-container">
      ${componentsHTML}
    </div>
  </div>
</body>
</html>`

  // 创建 Blob 并下载
  const blob = new Blob([html], { type: 'text/html;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 生成 HTML 字符串（不下载）
 * @param {Object} template - JSON 模板
 * @returns {string} HTML 字符串
 */
export function generateHTML(template) {
  if (!template || !template.page || !template.components) {
    throw new Error('Invalid template format')
  }

  const { page, components } = template

  const componentsHTML = components
    .map(comp => generateComponentHTML(comp))
    .join('\n    ')

  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>AI 设计稿预览</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: -apple-system, sans-serif;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
    }
    .page-container {
      position: relative;
      width: ${page.width}px;
      height: ${page.height}px;
      background-color: ${page.backgroundColor};
      background-image: ${page.backgroundImage ? `url(${page.backgroundImage})` : 'none'};
      background-size: cover;
      background-position: center;
      background-repeat: no-repeat;
      margin: 0 auto;
      box-shadow: 0 20px 60px rgba(0,0,0,0.3);
      border-radius: 12px;
      overflow: hidden;
    }
  </style>
</head>
<body>
  <div class="page-container">
    ${componentsHTML}
  </div>
</body>
</html>`
}
