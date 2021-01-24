import async.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
/***
 * This is a small Test Program for testing the features of this project.
 * You can use this as a playground.
 * **/
fun main(): Unit = runBlocking{
    extensionDemo()
    coroutineBackgroundTaskDemo()
}

/**
 * This function demonstrates examples for the extensions form KotlinCoreExtensions.
 * Change the value of nullTest to see how the null-extensions work. Change the type of
 * castTest to test castedAs-extensions. Change strTest to test String-Extensions.
 * **/
private suspend fun extensionDemo() = coroutineScope{
    val nullTest : String? = "Hello World"
    // not null
    nullTest.notNull {
        println("NotNull: $it is not null")
    }

    //not null suspending
    nullTest.notNullSuspending {
        delay(500)
        println("NotNullSuspending: $it is not null")
    }

    //is null
    nullTest.isNull {
        println("nullTest seems to be null")
    }


    val castTest : Any = 12345

    //castedAs
    castTest.castedAs<Int> {
        println("Cast was successful: Value of castTest is $it")
    }

    val castRes = castTest.castedAs<Int,Int> {
        return@castedAs it * 2
    }
    println("castRes is $castRes")

    //String extensions
    val strTest = "This is a small test"
    println(strTest.capitalizeEachWord())
    println(strTest.capitalizeEachWordExcept("small", "a"))

}

/**
 * This function showns a demo of CoroutineBackgroundTask
 * CAUTION: Set the mainDispatcher to another value than Dispatchers.Main when you are not operating on Android**/
private suspend fun coroutineBackgroundTaskDemo() = coroutineScope{
    val task = CoroutineBackgroundTask<Int,Boolean>(mainDispatcher = Dispatchers.Default).executeInBackground { task, _ ->
        repeat(5){
            delay(1000)
            task.publishProgress(it+1)
        }
        //throw Exception("test")
        return@executeInBackground true
    }.onPreExecute {
        println("Starting Background action:")
    }.onProgressUpdate {
        println("Progress: $it")
    }.onCanceled {
        println("task was cancelled")
    }.onError {
        println("there was an error: $it")
    }.onDone { result, _ ->
        println("Task has completed! Result is $result.")
    }

    task.start()

    //delay(2000)
    //task.cancel()

    delay(7000)
}