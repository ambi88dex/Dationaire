package com.example.dationaire

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dationaire.ui.theme.DationaireTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

object AppConfig {
    var questionDurationMillis: Long = 5000L // Default to 5 seconds
}

@Preview
@Composable
fun MyApp() {
    val navController = rememberNavController()
    AppNavigation(navController)
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = "onboarding1") {
        composable("onboarding1") {
            OnboardingPage1(navController)
        }
        composable("onboarding2") {
            OnboardingPage2(navController)
        }
        composable("questionnaire/{questionIndex}",
            arguments = listOf(navArgument("questionIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val questionIndex = backStackEntry.arguments?.getInt("questionIndex") ?: 0
            QuestionnairePage(navController, questionIndex)
        }
        composable("summary") {
            SummaryPage()
        }
    }
}

object GameRepository {
    var player1: Player = Player()
    var player2: Player = Player()
    var mergedQuestions: List<String> = emptyList()
    private val answers = mutableMapOf<Int, String>()

    fun savePlayer1(player: Player) {
        player1 = player
    }

    fun savePlayer2(player: Player) {
        player2 = player
        mergeQuestions()
    }

    private fun mergeQuestions() {
        mergedQuestions = player1.questions + player2.questions
    }

    fun saveAnswer(questionIndex: Int, answer: String) {
        answers[questionIndex] = answer
    }

    fun getAnswer(questionIndex: Int): String {
        return answers[questionIndex] ?: ""
    }

    fun getAllAnswers(): Map<Int, String> {
        return answers
    }
}


data class Player(
    val name: String = "",
    val questions: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingPage1(navController: NavController) {
    var name by rememberSaveable{ mutableStateOf("") }
    val questions = rememberSaveable(
        saver = listSaver(
            save={stateList -> stateList.toList()},
            restore = { restoredList -> mutableStateListOf(*restoredList.toTypedArray()) }
        )
    )  { mutableStateListOf("", "", "", "", "") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Player 1 Setup") })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Enter your name:")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Enter up to 5 questions:")
                for (i in 0 until 5) {
                    OutlinedTextField(
                        value = questions[i],
                        onValueChange = { questions[i] = it },
                        label = { Text("Question ${i + 1}") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val player = Player(
                            name = name,
                            questions = questions.filter { it.isNotBlank() }
                        )
                        GameRepository.savePlayer1(player)
                        navController.navigate("onboarding2")
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Next")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingPage2(navController: NavController) {
     var name2 by rememberSaveable { mutableStateOf("") }
    val questions2 = rememberSaveable(
        saver = listSaver(
            save={stateList -> stateList.toList()},
            restore = { restoredList -> mutableStateListOf(*restoredList.toTypedArray()) }
        )
    )  { mutableStateListOf("", "", "", "", "") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Player 2 Setup") })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Enter your name:")
                OutlinedTextField(
                    value = name2,
                    onValueChange = { name2 = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Enter up to 5 questions:")
                for (i in 0 until 5) {
                    OutlinedTextField(
                        value = questions2[i],
                        onValueChange = { questions2[i] = it },
                        label = { Text("Question ${i + 1}") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val player = Player(
                            name = name2,
                            questions = questions2.filter { it.isNotBlank() }
                        )
                        GameRepository.savePlayer2(player)
                        // Start questionnaire with the first question
                        navController.navigate("questionnaire/0")
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Next")
                }
            }
        }
    )
}


@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuestionnairePage(navController: NavController, questionIndex: Int) {
    val questions = GameRepository.mergedQuestions
    if (questionIndex >= questions.size) {
        // All questions answered, navigate to summary
        navController.navigate("summary") {
            popUpTo("questionnaire/$questionIndex") { inclusive = true }
        }
        return
    }

    var answer by rememberSaveable { mutableStateOf(GameRepository.getAnswer(questionIndex)) }
    var timeRemaining by remember { mutableStateOf(AppConfig.questionDurationMillis / 1000) }
    val scope = rememberCoroutineScope()

    // Start the timer when the composable enters the composition
    LaunchedEffect(questionIndex) {
        timeRemaining = AppConfig.questionDurationMillis / 1000
        for (i in timeRemaining downTo 1) {
            delay(1000L)
            timeRemaining -= 1
        }
        // Save the answer and navigate to the next question
        GameRepository.saveAnswer(questionIndex, answer)
        navController.navigate("questionnaire/${questionIndex + 1}") {
            popUpTo("questionnaire/$questionIndex") { inclusive = true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Question ${questionIndex + 1}") })
        },
        content = { paddingValues ->
            AnimatedContent(
                targetState = questionIndex,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() with
                            slideOutHorizontally { width -> -width } + fadeOut()
                }
            ) { targetQuestionIndex ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = questions[targetQuestionIndex],
                            style = MaterialTheme.typography.displayLarge
                        )
                        OutlinedTextField(
                            value = answer,
                            onValueChange = { answer = it },
                            label = { Text("Your Answer") },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.padding(top=8.dp)
                    ) {
                        Text(
                            text = "Time Remaining: $timeRemaining seconds",
                            style = MaterialTheme.typography.displaySmall,
                            modifier = Modifier.padding(top=8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (questionIndex > 0) {
                                Button(onClick = {
                                    GameRepository.saveAnswer(questionIndex, answer)
                                    navController.navigate("questionnaire/${questionIndex - 1}") {
                                        popUpTo("questionnaire/$questionIndex") { inclusive = true }
                                    }
                                }) {
                                    Text("Previous")
                                }
                            }
                            Button(onClick = {
                                GameRepository.saveAnswer(questionIndex, answer)
                                navController.navigate("questionnaire/${questionIndex + 1}") {
                                    popUpTo("questionnaire/$questionIndex") { inclusive = true }
                                }
                            }) {
                                Text("Next")
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryPage() {
    val answers = GameRepository.getAllAnswers()
    val questions = GameRepository.mergedQuestions

    // To close the app, we need to access the current activity
    val context = LocalContext.current
    val activity = context as? Activity

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Summary") })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.weight(1f), // Take up available space
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Thank you for participating!",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom=16.dp)
                    )
                    for (i in questions.indices) {
                        Text(
                            text = "Q${i + 1}: ${questions[i]}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom=4.dp)
                        )
                        Text(
                            text = "Answer: ${answers[i] ?: "No answer"}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom=8.dp)
                        )
                    }
                }
                Button(
                    onClick = {
                        activity?.finishAffinity() // Close the app
                    },
                    modifier = Modifier
                        .padding(bottom=16.dp)
                    .fillMaxWidth(0.5f)
                ) {
                Text("Close")
            }
            }
        }
    )
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    DationaireTheme {
//        Greeting("Android")
//    }
//}