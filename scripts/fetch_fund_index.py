#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
基金索引库生成脚本
从天天基金网获取所有公募基金的基本信息，生成Kotlin数据类文件
"""

import requests
import re
import json
from typing import List, Dict

def fetch_fund_list() -> List[Dict[str, str]]:
    """
    从天天基金网获取基金列表
    返回格式: [{"code": "000001", "name": "华夏成长混合", "type": "混合型-灵活", "pinyin": "HUAXIACHENGZHANGHUNHE"}, ...]
    """
    url = "https://fund.eastmoney.com/js/fundcode_search.js"
    
    print(f"正在获取基金列表: {url}")
    
    try:
        response = requests.get(url, timeout=10)
        response.encoding = 'utf-8'
        content = response.text
        
        # 提取 var r = [...] 中的数组内容
        match = re.search(r'var r = (\[.*\]);', content, re.DOTALL)
        if not match:
            print("❌ 无法解析基金数据")
            return []
        
        # 解析JSON数组
        fund_array_str = match.group(1)
        fund_array = json.loads(fund_array_str)
        
        print(f"✅ 成功获取 {len(fund_array)} 只基金")
        
        # 转换为字典格式
        fund_list = []
        for item in fund_array:
            if len(item) >= 5:
                fund_info = {
                    "code": item[0],
                    "name": item[2],
                    "type": item[3],
                    "pinyin": item[4]
                }
                fund_list.append(fund_info)
        
        return fund_list
        
    except Exception as e:
        print(f"❌ 获取基金列表失败: {e}")
        return []

def generate_json_file(fund_list: List[Dict[str, str]], output_path: str):
    """
    生成JSON资源文件（更小的体积）
    """
    print(f"\n正在生成JSON文件: {output_path}")
    
    # 压缩数据：使用数组而不是对象，减小体积
    compact_data = [[f['code'], f['name'], f['type'], f['pinyin']] for f in fund_list]
    
    # 写入JSON文件
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(compact_data, f, ensure_ascii=False, separators=(',', ':'))
    
    import os
    file_size = os.path.getsize(output_path) / (1024 * 1024)  # MB
    
    print(f"✅ JSON文件生成成功!")
    print(f"   文件路径: {output_path}")
    print(f"   基金总数: {len(fund_list)}")
    print(f"   文件大小: {file_size:.2f} MB")

def generate_kotlin_loader(output_path: str, total_count: int):
    """
    生成Kotlin加载器类（轻量级）
    """
    print(f"\n正在生成Kotlin加载器: {output_path}")
    
    kotlin_code = f'''package com.example.jjsj.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * 基金索引数据加载器
 * 从资源文件加载全国所有公募基金的基本信息
 * 总数: {total_count} 只
 */
object FundIndexData {{
    
    data class FundBasicInfo(
        val code: String,
        val name: String,
        val type: String,
        val pinyin: String
    )
    
    private var cachedFunds: List<FundBasicInfo>? = null
    
    /**
     * 加载基金索引数据
     * 首次调用时从资源文件加载，后续使用缓存
     */
    suspend fun loadFunds(context: Context): List<FundBasicInfo> = withContext(Dispatchers.IO) {{
        // 如果已缓存，直接返回
        cachedFunds?.let {{ return@withContext it }}
        
        try {{
            // 从raw资源读取JSON文件
            val inputStream = context.resources.openRawResource(R.raw.fund_index)
            val jsonString = inputStream.bufferedReader().use {{ it.readText() }}
            
            // 解析JSON数组
            val jsonArray = JSONArray(jsonString)
            val funds = mutableListOf<FundBasicInfo>()
            
            for (i in 0 until jsonArray.length()) {{
                val item = jsonArray.getJSONArray(i)
                funds.add(
                    FundBasicInfo(
                        code = item.getString(0),
                        name = item.getString(1),
                        type = item.getString(2),
                        pinyin = item.getString(3)
                    )
                )
            }}
            
            cachedFunds = funds
            funds
        }} catch (e: Exception) {{
            e.printStackTrace()
            emptyList()
        }}
    }}
    
    /**
     * 搜索基金
     * @param context Android上下文
     * @param keyword 搜索关键词（支持代码、名称、拼音）
     * @return 匹配的基金列表
     */
    suspend fun search(context: Context, keyword: String): List<FundBasicInfo> {{
        if (keyword.isBlank()) return emptyList()
        
        val allFunds = loadFunds(context)
        val lowerKeyword = keyword.lowercase()
        
        return allFunds.filter {{ fund ->
            fund.code.contains(lowerKeyword) ||
            fund.name.lowercase().contains(lowerKeyword) ||
            fund.pinyin.lowercase().contains(lowerKeyword)
        }}.take(50) // 限制返回数量
    }}
}}
'''
    
    # 写入文件
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(kotlin_code)
    
    print(f"✅ Kotlin加载器生成成功!")
    print(f"   文件路径: {output_path}")

def main():
    print("=" * 60)
    print("基金索引库生成工具")
    print("=" * 60)
    
    # 获取基金列表
    fund_list = fetch_fund_list()
    
    if not fund_list:
        print("\n❌ 未获取到基金数据，退出程序")
        return
    
    # 生成文件
    import os
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # 1. 生成JSON资源文件
    json_output_path = os.path.join(script_dir, "../app/src/main/res/raw/fund_index.json")
    json_output_path = os.path.abspath(json_output_path)
    os.makedirs(os.path.dirname(json_output_path), exist_ok=True)
    generate_json_file(fund_list, json_output_path)
    
    # 2. 生成Kotlin加载器
    kotlin_output_path = os.path.join(script_dir, "../app/src/main/java/com/example/jjsj/data/local/FundIndexData.kt")
    kotlin_output_path = os.path.abspath(kotlin_output_path)
    os.makedirs(os.path.dirname(kotlin_output_path), exist_ok=True)
    generate_kotlin_loader(kotlin_output_path, len(fund_list))
    
    # 统计信息
    print("\n" + "=" * 60)
    print("统计信息:")
    print(f"  - 基金总数: {len(fund_list)}")
    
    # 按类型统计
    type_count = {}
    for fund in fund_list:
        fund_type = fund['type']
        type_count[fund_type] = type_count.get(fund_type, 0) + 1
    
    print(f"  - 基金类型数: {len(type_count)}")
    for fund_type, count in sorted(type_count.items(), key=lambda x: x[1], reverse=True)[:10]:
        print(f"    • {fund_type}: {count}")
    
    print("=" * 60)
    print("✅ 全部完成!")

if __name__ == "__main__":
    main()
