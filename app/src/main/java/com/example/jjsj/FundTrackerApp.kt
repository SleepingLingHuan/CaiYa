package com.example.jjsj

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jjsj.data.local.AppDatabase
import com.example.jjsj.data.repository.FundRepository
import com.example.jjsj.data.repository.PositionRepository
import com.example.jjsj.ui.navigation.BottomNavItem
import com.example.jjsj.ui.navigation.Screen
import com.example.jjsj.ui.screen.detail.FundDetailScreen
import com.example.jjsj.ui.screen.favorite.FavoriteScreen
import com.example.jjsj.ui.screen.market.MarketScreen
import com.example.jjsj.ui.screen.position.PositionScreen
import com.example.jjsj.ui.screen.visual.VisualScreen
import com.example.jjsj.viewmodel.FundViewModel
import com.example.jjsj.viewmodel.FundViewModelFactory
import com.example.jjsj.viewmodel.PositionViewModel
import com.example.jjsj.viewmodel.PositionViewModelFactory

/**
 * 基金追踪器主应用
 */
@Composable
fun FundTrackerApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // 初始化数据库和Repository
    val database = remember { AppDatabase.getDatabase(context) }
    val fundRepository = remember {
        FundRepository(
            context = context,
            fundCacheDao = database.fundCacheDao(),
            favoriteFundDao = database.favoriteFundDao(),
            fundDetailCacheDao = database.fundDetailCacheDao(),
            fundNavCacheDao = database.fundNavCacheDao()
        )
    }
    val positionRepository = remember {
        PositionRepository(
            positionDao = database.positionDao(),
            fundCacheDao = database.fundCacheDao()
        )
    }
    val transactionRepository = remember {
        com.example.jjsj.data.repository.TransactionRepository(
            transactionDao = database.transactionDao(),
            favoriteFundDao = database.favoriteFundDao(),
            positionDao = database.positionDao(),
            fundCacheDao = database.fundCacheDao()
        )
    }
    
    // 初始化ViewModel
    val fundViewModel: FundViewModel = viewModel(
        factory = FundViewModelFactory(fundRepository)
    )
    val positionViewModel: PositionViewModel = viewModel(
        factory = PositionViewModelFactory(positionRepository, fundRepository)
    )
    val transactionViewModel: com.example.jjsj.viewmodel.TransactionViewModel = viewModel(
        factory = com.example.jjsj.viewmodel.TransactionViewModelFactory(transactionRepository)
    )
    
    // 底部导航项
    val bottomNavItems = listOf(
        BottomNavItem(Screen.Market, "行情", Icons.AutoMirrored.Filled.TrendingUp),
        BottomNavItem(Screen.Position, "持仓", Icons.Default.AccountBalance),
        BottomNavItem(Screen.Favorite, "自选", Icons.Default.Star),
        BottomNavItem(Screen.Transaction, "记录", Icons.Default.Receipt),
        BottomNavItem(Screen.Visual, "可视化", Icons.Default.BarChart)
    )
    
    Scaffold(
        bottomBar = {
            // 只在主要页面显示底部导航栏
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute in listOf(
                    Screen.Market.route,
                    Screen.Favorite.route,
                    Screen.Position.route,
                    Screen.Transaction.route,
                    Screen.Visual.route
                )) {
                BottomNavigationBar(
                    items = bottomNavItems,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Market.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 行情页面
            composable(Screen.Market.route) {
                MarketScreen(
                    viewModel = fundViewModel,
                    positionViewModel = positionViewModel,
                    onFundClick = { fundCode ->
                        navController.navigate(Screen.FundDetail.createRoute(fundCode))
                    },
                    onRankingClick = { type ->
                        navController.navigate(Screen.RankingList.createRoute(type))
                    }
                )
            }
            
            // 自选页面
            composable(Screen.Favorite.route) {
                FavoriteScreen(
                    viewModel = fundViewModel,
                    onFundClick = { fundCode ->
                        navController.navigate(Screen.FundDetail.createRoute(fundCode))
                    }
                )
            }
            
            // 持仓页面
            composable(Screen.Position.route) {
                PositionScreen(
                    viewModel = positionViewModel,
                    onFundClick = { fundCode ->
                        navController.navigate(Screen.FundDetail.createRoute(fundCode))
                    }
                )
            }
            
            // 交易记录页面
            composable(Screen.Transaction.route) {
                com.example.jjsj.ui.screen.transaction.TransactionScreen(
                    viewModel = transactionViewModel,
                    onFundClick = { fundCode ->
                        navController.navigate(Screen.FundDetail.createRoute(fundCode))
                    }
                )
            }
            
            // 可视化页面
            composable(Screen.Visual.route) {
                VisualScreen(
                    positionViewModel = positionViewModel,
                    fundViewModel = fundViewModel
                )
            }
            
            // 基金详情页面
            composable(Screen.FundDetail.route) { backStackEntry ->
                val fundCode = backStackEntry.arguments?.getString("fundCode") ?: ""
                FundDetailScreen(
                    fundCode = fundCode,
                    viewModel = fundViewModel,
                    positionViewModel = positionViewModel,
                    transactionViewModel = transactionViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // 榜单页面
            composable(Screen.RankingList.route) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: ""
                com.example.jjsj.ui.screen.ranking.RankingListScreen(
                    type = type,
                    viewModel = fundViewModel,
                    onBackClick = { navController.popBackStack() },
                    onFundClick = { fundCode ->
                        navController.navigate(Screen.FundDetail.createRoute(fundCode))
                    }
                )
            }
        }
    }
}

/**
 * 底部导航栏
 */
@Composable
private fun BottomNavigationBar(
    items: List<BottomNavItem>,
    navController: NavHostController
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.screen.route,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            // 避免重复导航到同一个页面
                            popUpTo(Screen.Market.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

