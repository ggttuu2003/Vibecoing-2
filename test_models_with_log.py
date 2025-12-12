#!/usr/bin/env python3
"""
测试 zenmux.ai API 并记录实际的 HTTP 请求
启用详细的 HTTP 日志来查看 SDK 真正使用的路径
"""

import logging
import sys
from google import genai
from google.genai import types

# ==========================================================
# 启用详细的 HTTP 日志
# ==========================================================
logging.basicConfig(
    stream=sys.stdout,
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

# 设置 httpx 和 httpcore 的日志级别为 DEBUG
logging.getLogger("httpx").setLevel(logging.DEBUG)
logging.getLogger("httpcore").setLevel(logging.DEBUG)
logging.getLogger("httpcore.connection").setLevel(logging.DEBUG)
logging.getLogger("httpcore.http11").setLevel(logging.DEBUG)

# API 配置
ZENMUX_API_KEY = "sk-ai-v1-04a5921ea316f19aa4d44d7c6ef2bf34ef02a3cb85fa117c6a88f13254149b51"
ZENMUX_BASE_URL = "https://zenmux.ai/api/vertex-ai"

# 只测试一个模型
TEST_MODEL = "google/gemini-2.5-flash-image"
TEST_PROMPT = "Create a simple red circle"


def main():
    print("\n" + "="*60)
    print("查看 SDK 实际使用的 HTTP 路径")
    print("="*60)

    # 创建客户端
    print("\n正在初始化 Genai Client...")
    client = genai.Client(
        api_key=ZENMUX_API_KEY,
        vertexai=True,
        http_options=types.HttpOptions(
            api_version='v1',
            base_url=ZENMUX_BASE_URL
        ),
    )
    print("✅ Client 初始化成功\n")

    # 发送请求
    print(f"正在调用模型: {TEST_MODEL}")
    print("请注意下面的 HTTP 日志，查找实际的请求 URL\n")
    print("="*60)

    try:
        response = client.models.generate_content(
            model=TEST_MODEL,
            contents=[TEST_PROMPT],
            config=types.GenerateContentConfig(
                response_modalities=["TEXT", "IMAGE"]
            )
        )

        print("="*60)
        print("\n✅ 请求成功！")
        print("\n请在上面的日志中查找类似这样的行：")
        print("  - 'POST https://...' - 这是实际的请求 URL")
        print("  - 'Request headers' - 查看请求头")
        print("\n对比 Java 代码构建的 URL，找出差异")

    except Exception as e:
        print("="*60)
        print(f"\n❌ 请求失败: {e}")
        print("\n即使失败，也请查看上面的日志，找出实际的请求 URL")


if __name__ == "__main__":
    main()
