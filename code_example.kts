val url = "https://fund.eastmoney.com/data/rankhandler.aspx?op=ph&dt=kf&ft=all&sc=1nzf&st=desc&pi=1&pn=50&fl=0&isab=1"

val request = Request.Builder()
    .url(url)
    .header("Referer", "https://fund.eastmoney.com/data/fundranking.html")
    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    .header("Accept", "*/*")
    .build()

val response = okHttpClient.newCall(request).execute()
val body = response.body?.string()
// 正常情况下会返回 rankData = { "datas": [...], "allRecords": 12345 };