#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试今日榜单和成立以来榜单
验证API参数是否正确
"""

import requests
import re

def test_ranking(sort_type, ascending, title):
    """测试排行榜API"""
    sort_order = "asc" if ascending else "desc"
    url = (
        f"https://fund.eastmoney.com/data/rankhandler.aspx?"
        f"op=ph&dt=kf&ft=all&sc={sort_type}&st={sort_order}&"
        f"pi=1&pn=10&fl=0&isab=1"
    )
    
    print(f"\n{'='*60}")
    print(f"测试: {title}")
    print(f"排序字段: {sort_type}, 排序方向: {sort_order}")
    print(f"URL: {url}")
    print(f"{'='*60}")
    
    try:
        response = requests.get(url, headers={
            'Referer': 'https://fund.eastmoney.com/data/fundranking.html',
            'User-Agent': 'Mozilla/5.0'
        }, timeout=10)
        
        if response.status_code != 200:
            print(f"❌ HTTP错误: {response.status_code}")
            return False
        
        body = response.text
        
        # 提取总记录数
        total_match = re.search(r'allRecords:(\d+)', body)
        total = total_match.group(1) if total_match else "0"
        
        # 提取数据
        datas_match = re.search(r'datas:\[(.*?)\]', body, re.DOTALL)
        if not datas_match:
            print("❌ 无法提取数据")
            return False
        
        # 提取基金数据
        fund_pattern = r'"([^"]+)"'
        funds = re.findall(fund_pattern, datas_match.group(1))
        
        print(f"\n✅ 成功获取数据")
        print(f"总记录数: {total}")
        print(f"返回数量: {len(funds)}")
        
        # 显示前5条数据
        print(f"\n前5条数据:")
        for i, fund_str in enumerate(funds[:5], 1):
            fields = fund_str.split(',')
            if len(fields) >= 7:
                code = fields[0]
                name = fields[1]
                nav = fields[4]
                day_change = fields[6] if len(fields) > 6 else "N/A"
                week_change = fields[7] if len(fields) > 7 else "N/A"
                inception_change = fields[13] if len(fields) > 13 else "N/A"
                
                print(f"{i}. {code} {name}")
                print(f"   净值: {nav}, 日涨跌: {day_change}%, 周涨跌: {week_change}%, 成立以来: {inception_change}%")
        
        return True
        
    except Exception as e:
        print(f"❌ 异常: {e}")
        return False

def main():
    print("开始测试今日榜单和成立以来榜单...")
    
    # 测试今日涨幅榜
    test_ranking("rzdf", False, "今日涨幅榜 (rzdf, desc)")
    
    # 测试今日跌幅榜
    test_ranking("rzdf", True, "今日跌幅榜 (rzdf, asc)")
    
    # 测试成立以来涨幅榜
    test_ranking("lnzf", False, "成立以来涨幅榜 (lnzf, desc)")
    
    # 测试成立以来跌幅榜
    test_ranking("lnzf", True, "成立以来跌幅榜 (lnzf, asc)")
    
    print(f"\n{'='*60}")
    print("测试完成！")
    print(f"{'='*60}")

if __name__ == "__main__":
    main()
