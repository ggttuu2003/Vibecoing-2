#!/usr/bin/env python3
"""
测试 zenmux.ai API 支持的图像生成模型
使用官方 Google Genai SDK（按照官方文档的正确方式）
"""

from google import genai
from google.genai import types
import sys
from typing import List, Dict

# API 配置
ZENMUX_API_KEY = "sk-ai-v1-04a5921ea316f19aa4d44d7c6ef2bf34ef02a3cb85fa117c6a88f13254149b51"
ZENMUX_BASE_URL = "https://zenmux.ai/api/vertex-ai"

# 需要测试的模型列表
MODELS_TO_TEST = [
    "google/gemini-3-pro-image-preview",
    "google/gemini-3-pro-image-preview-free",
    "google/gemini-2.5-flash-image",
    "google/gemini-2.5-flash-image-free"
]

# 测试提示词（简单的，避免超时）
TEST_PROMPT = "Create a simple red circle on white background"


def test_model(client: genai.Client, model: str) -> Dict:
    """
    测试单个模型是否可用

    Args:
        client: Google Genai Client 实例
        model: 模型名称

    Returns:
        包含测试结果的字典
    """
    print(f"\n{'='*60}")
    print(f"测试模型: {model}")
    print(f"{'='*60}")

    try:
        print("正在调用 SDK 生成内容...")

        # 按照官方文档的方式调用
        response = client.models.generate_content(
            model=model,
            contents=[TEST_PROMPT],
            config=types.GenerateContentConfig(
                response_modalities=["TEXT", "IMAGE"]
            )
        )

        # 检查响应
        has_text = False
        has_image = False

        for part in response.parts:
            if part.text is not None:
                has_text = True
                print(f"   ✅ 收到文本响应: {part.text[:100]}...")
            elif part.inline_data is not None:
                has_image = True
                print(f"   ✅ 收到图像数据 (mime_type: {part.inline_data.mime_type})")

        if has_image or has_text:
            print(f"✅ 成功 - 模型可用")
            result = {
                "model": model,
                "success": True,
                "has_text": has_text,
                "has_image": has_image,
                "error": None
            }
        else:
            print(f"⚠️  警告 - 响应为空")
            result = {
                "model": model,
                "success": False,
                "has_text": False,
                "has_image": False,
                "error": "响应为空"
            }

    except Exception as e:
        error_msg = str(e)
        print(f"❌ 失败 - {error_msg[:200]}")
        result = {
            "model": model,
            "success": False,
            "has_text": False,
            "has_image": False,
            "error": error_msg
        }

    return result


def main():
    """主函数：测试所有模型并汇总结果"""
    print("\n" + "="*60)
    print("ZenMux.ai 图像生成模型可用性测试")
    print("使用官方 Google Genai SDK")
    print("="*60)
    print(f"Base URL: {ZENMUX_BASE_URL}")
    print(f"待测试模型数量: {len(MODELS_TO_TEST)}")

    # 创建客户端（按照官方文档的正确方式）
    print("\n正在初始化 Genai Client...")
    try:
        client = genai.Client(
            api_key=ZENMUX_API_KEY,  # ✅ 使用 api_key 参数
            vertexai=True,           # ✅ 启用 Vertex AI 协议
            http_options=types.HttpOptions(
                api_version='v1',
                base_url=ZENMUX_BASE_URL
            ),
            # ✅ 不设置 project 和 location（避免触发 Google Cloud 认证）
        )
        print("✅ Client 初始化成功")
    except Exception as e:
        print(f"❌ Client 初始化失败: {e}")
        sys.exit(1)

    # 测试所有模型
    results = []
    for model in MODELS_TO_TEST:
        result = test_model(client, model)
        results.append(result)

    # 汇总结果
    print("\n" + "="*60)
    print("测试结果汇总")
    print("="*60)

    available_models = []
    unavailable_models = []

    for result in results:
        if result["success"]:
            available_models.append(result["model"])
            image_flag = "🖼️ " if result["has_image"] else ""
            text_flag = "📝" if result["has_text"] else ""
            print(f"✅ {result['model']} - {image_flag}{text_flag}")
        else:
            unavailable_models.append(result["model"])
            error_preview = result["error"][:80] if result["error"] else "未知错误"
            print(f"❌ {result['model']} - {error_preview}")

    # 输出可用模型列表
    print("\n" + "="*60)
    print(f"可用模型 ({len(available_models)}/{len(MODELS_TO_TEST)})")
    print("="*60)
    if available_models:
        for model in available_models:
            print(f"  - {model}")
    else:
        print("  无可用模型")

    # 输出不可用模型列表
    if unavailable_models:
        print("\n" + "="*60)
        print(f"不可用模型 ({len(unavailable_models)}/{len(MODELS_TO_TEST)})")
        print("="*60)
        for model in unavailable_models:
            print(f"  - {model}")

    # 生成建议
    print("\n" + "="*60)
    print("建议")
    print("="*60)
    if available_models:
        print(f"✅ 找到 {len(available_models)} 个可用模型")
        print(f"\n推荐配置：")
        print(f"  前端默认值: {available_models[0]}")
        print(f"  后端默认值: {available_models[0]}")
        print(f"\n下一步：")
        print(f"  1. 更新前端 ImageGenerator.vue 的模型选项")
        print(f"  2. 更新后端 AIImageGenConfig.java 的 SUPPORTED_MODELS")
        print(f"  3. 更新 application.properties 的默认模型")
    else:
        print("❌ 没有找到可用的模型")
        print("   请检查：")
        print("   1. API Key 是否正确")
        print("   2. Base URL 是否正确")
        print("   3. 网络连接是否正常")
        print("   4. 联系 zenmux.ai 技术支持")


if __name__ == "__main__":
    main()
