package com.example.jjsj.ui.navigation

/**
 * 导航路由
 */
sealed class Screen(val route: String) {
    object Market : Screen("market")
    object Favorite : Screen("favorite")
    object Position : Screen("position")
    object Transaction : Screen("transaction")  // 交易记录
    object Visual : Screen("visual")
    object FundDetail : Screen("fund_detail/{fundCode}") {
        fun createRoute(fundCode: String) = "fund_detail/$fundCode"
    }
    object RankingList : Screen("ranking_list/{type}") {
        fun createRoute(type: String) = "ranking_list/$type"
    }
}

/**
 * 底部导航项
 */
data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * 榜单类型
 */
object RankingType {
    const val TOP_GAINERS = "top_gainers"  // 涨幅榜
    const val TOP_LOSERS = "top_losers"    // 跌幅榜
}
