#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试天天基金网排行榜API
"""

import requests
import re
import json

def test_ranking_api():
    """测试排行榜API"""
    
    # 测试获取涨幅榜前50名
    url = "https://fund.eastmoney.com/data/rankhandler.aspx"
    
    params = {
        'op': 'ph',        # 操作类型
        'dt': 'kf',        # 数据类型（开放式基金）
        'ft': 'all',       # 基金类型（全部）
        'sc': '1nzf',      # 排序字段（1年涨跌幅，也可以用 'zzf' 表示日涨跌幅）
        'st': 'desc',      # 排序方向（降序）
        'pi': '1',         # 页码
        'pn': '50',        # 每页数量
        'fl': '0',         # 过滤条件
        'isab': '1'        # 是否AB份额
    }
    
    headers = {
        'Referer': 'https://fund.eastmoney.com/data/fundranking.html',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': '*/*'
    }
    
    print("=" * 60)
    print("测试天天基金网排行榜API")
    print("=" * 60)
    print(f"URL: {url}")
    print(f"参数: {params}")
    print()
    
    try:
        response = requests.get(url, params=params, headers=headers, timeout=10)
        print(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            content = response.text
            print(f"响应长度: {len(content)} 字符")
            print(f"响应前200字符: {content[:200]}")
            print()
            
            # 解析响应 - 格式：var rankData = {datas:[...],allRecords:12345,...};
            # 注意：这不是标准JSON，需要特殊处理
            match = re.search(r'var rankData\s*=\s*\{datas:\[(.*?)\],allRecords:(\d+)', content, re.DOTALL)
            if match:
                datas_str = match.group(1)
                all_records = int(match.group(2))
                
                # 提取所有基金数据字符串
                fund_items = re.findall(r'"([^"]+)"', datas_str)
                
                data = {
                    'datas': fund_items,
                    'allRecords': all_records
                }
                
                print("✅ API响应成功!")
                print(f"总记录数: {data.get('allRecords', 0)}")
                print(f"当前页记录数: {len(data.get('datas', []))}")
                print()
                
                # 解析数据格式
                if data.get('datas'):
                    print("数据字段示例（前3条）:")
                    for i, item in enumerate(data['datas'][:3]):
                        fields = item.split(',')
                        print(f"\n记录 {i+1}:")
                        print(f"  原始数据: {item[:150]}...")
                        if len(fields) >= 6:
                            print(f"  基金代码: {fields[0]}")
                            print(f"  基金名称: {fields[1]}")
                            print(f"  日期: {fields[3]}")
                            print(f"  单位净值: {fields[4]}")
                            print(f"  累计净值: {fields[5]}")
                
                return True
            else:
                print("❌ 无法解析响应数据")
                print(f"原始响应: {content}")
                return False
        else:
            print(f"❌ 请求失败，状态码: {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ 请求异常: {e}")
        return False

def test_different_sorting():
    """测试不同的排序方式"""
    
    print("\n" + "=" * 60)
    print("测试不同排序字段")
    print("=" * 60)
    
    sorting_options = [
        ('zzf', '日涨跌幅'),
        ('1yzf', '近1月涨跌幅'),
        ('3yzf', '近3月涨跌幅'),
        ('6yzf', '近6月涨跌幅'),
        ('1nzf', '近1年涨跌幅'),
    ]
    
    url = "https://fund.eastmoney.com/data/rankhandler.aspx"
    headers = {
        'Referer': 'https://fund.eastmoney.com/data/fundranking.html',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        'Accept': '*/*'
    }
    
    for sort_key, sort_name in sorting_options:
        params = {
            'op': 'ph',
            'dt': 'kf',
            'ft': 'all',
            'sc': sort_key,
            'st': 'desc',
            'pi': '1',
            'pn': '10',
            'fl': '0',
            'isab': '1'
        }
        
        try:
            response = requests.get(url, params=params, headers=headers, timeout=10)
            if response.status_code == 200:
                match = re.search(r'var rankData\s*=\s*\{datas:\[(.*?)\],allRecords:(\d+)', response.text, re.DOTALL)
                if match:
                    all_records = int(match.group(2))
                    print(f"✅ {sort_name}: {all_records} 条记录")
                else:
                    print(f"❌ {sort_name}: 解析失败")
            else:
                print(f"❌ {sort_name}: 请求失败")
        except Exception as e:
            print(f"❌ {sort_name}: {e}")

if __name__ == "__main__":
    success = test_ranking_api()
    
    if success:
        test_different_sorting()
        print("\n" + "=" * 60)
        print("✅ API验证成功，可以用于Android项目")
        print("=" * 60)
    else:
        print("\n" + "=" * 60)
        print("❌ API验证失败")
        print("=" * 60)
