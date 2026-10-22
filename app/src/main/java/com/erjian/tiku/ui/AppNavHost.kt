package com.erjian.tiku.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.erjian.tiku.TiKuApp

object Routes {
    const val HOME = "home"
    const val CHAPTERS = "chapters"
    const val PRACTICE = "practice/{mode}?chapter={chapter}"
    const val EXAM = "exam"
    const val EXAM_RUN = "examRun/{count}/{minutes}"
    const val EXAM_RESULT = "examResult"
    const val WRONG = "wrong"
    const val FAVORITE = "favorite"
    const val STATS = "stats"

    fun practice(mode: String, chapter: String? = null): String =
        "practice/$mode?chapter=${Uri.encode(chapter ?: "")}"

    fun examRun(count: Int, minutes: Int): String = "examRun/$count/$minutes"
}

@Composable
fun AppNavHost(examVm: ExamViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userStore = (context.applicationContext as TiKuApp).userStore
    val userData by userStore.data.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { userStore.load() }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                userData = userData,
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(Routes.CHAPTERS) {
            ChapterScreen(
                onBack = { navController.popBackStack() },
                onOpenChapter = { chapter ->
                    navController.navigate(Routes.practice("chapter", chapter)) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }
        composable(
            route = Routes.PRACTICE,
            arguments = listOf(
                navArgument("mode") { defaultValue = "sequential" },
                navArgument("chapter") { defaultValue = "" },
            ),
        ) { entry ->
            val chapterArg = entry.arguments?.getString("chapter")?.let { Uri.decode(it) }
            PracticeScreen(
                mode = entry.arguments?.getString("mode") ?: "sequential",
                chapter = chapterArg?.takeIf { it.isNotBlank() },
                userStore = userStore,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.EXAM) {
            ExamSetupScreen(
                examVm = examVm,
                onBack = { navController.popBackStack() },
                onStart = { count, minutes ->
                    navController.navigate(Routes.examRun(count, minutes))
                },
            )
        }
        composable(
            route = Routes.EXAM_RUN,
            arguments = listOf(
                navArgument("count") { type = NavType.IntType },
                navArgument("minutes") { type = NavType.IntType },
            ),
        ) {
            ExamScreen(
                examVm = examVm,
                onFinished = {
                    navController.navigate(Routes.EXAM_RESULT) {
                        popUpTo(Routes.HOME)
                    }
                },
                onExit = { navController.popBackStack() },
            )
        }
        composable(Routes.EXAM_RESULT) {
            ExamResultScreen(
                examVm = examVm,
                userStore = userStore,
                onBack = { navController.popBackStack() },
                onPracticeWrong = {
                    navController.navigate(Routes.practice("wrong")) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }
        composable(Routes.WRONG) {
            WrongScreen(
                userStore = userStore,
                onBack = { navController.popBackStack() },
                onPractice = {
                    navController.navigate(Routes.practice("wrong")) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }
        composable(Routes.FAVORITE) {
            FavoriteScreen(
                userStore = userStore,
                onBack = { navController.popBackStack() },
                onPractice = {
                    navController.navigate(Routes.practice("favorite")) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }
        composable(Routes.STATS) {
            StatsScreen(
                userStore = userStore,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
