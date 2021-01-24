package async

import kotlinx.coroutines.*
import kotlin.Exception
import kotlin.system.measureTimeMillis

/**
 * This class is a BackgroundTask, that can be used to execute certain Actions in Background via Coroutines and Fluent-API.
 * It was developed during the Development of Homium by MademDevs and later improved. It was inspired by AsyncTask for Android.
 *
 * This class can be used for Applications with an UI, preferred Android.
 * Every CoroutineBackgroundTask has a generic type for Progress (Reporting Progress to UI) and Output to be returned
 * when Background-Action is over.
 *
 * CAUTION: Set the mainDispatcher to another value than Dispatchers.Main when you are not operating on Android
 * **/
class CoroutineBackgroundTask<Progress,Output>(private val scope: CoroutineScope = GlobalScope,
                                               private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main) {
    //fields
    /**
     * IMPORTANT NOTE: All Callbacks, except onExecute, are executed in the Main-/UI-Thread using the given scope.
     * OnExecute is executed in IO-Thread.
     * **/

    /**Callback to be executed before the Background Action in UI Thread**/
    private var onPreExecute : suspend (CoroutineScope) -> Unit = { }
    /**
     * Callback to be executed after the Background Action in UI Thread and handling the Result
     * of the Background Action. E.g. apply it to certain UI Components
     * **/
    private var onDone : suspend (Output, CoroutineScope) -> Unit = { _, _ -> }
    /**
     * This Callback should be executed in Background and is necessary to be set else
     * CoroutineBackgroundTaskInitializationException is thrown. This function will return a certain Output
     * which then can be processed by onDone
     * **/
    private lateinit var onExecute : suspend (CoroutineBackgroundTask<Progress, Output>, CoroutineScope) -> Output
    /**
     * This Callback is executed in UI Thread to e.g. notify certain UI-Components about progress changes.
     * **/
    private var onProgressUpdate : (Progress) -> Unit = {}
    /**
     * This Callback is called when this CoroutineBackgroundTask is cancelled.
     * **/
    private var onCanceled : () -> Unit = {}

    /**
     * This Callback is called when there is an error
     * **/
    private var onError : (Throwable) -> Unit = {}

    /**
     * Job that is executed to realize this execution
     * **/
    private var job : Job? = null

    //functions
    /**This function is a Fluent-API-like Setter for onExecute**/
    fun executeInBackground(function: suspend (CoroutineBackgroundTask<Progress, Output>, CoroutineScope) -> Output) : CoroutineBackgroundTask<Progress, Output> {
        onExecute = function
        return this
    }

    /**This function is a Fluent-API-like Setter for onDone**/
    fun onDone(function: suspend (Output, CoroutineScope) -> Unit) : CoroutineBackgroundTask<Progress, Output> {
        onDone = function
        return this
    }

    /**This function is a Fluent-API-like Setter for onPreExecute**/
    fun onPreExecute(function: suspend (CoroutineScope) -> Unit) : CoroutineBackgroundTask<Progress, Output> {
        onPreExecute = function
        return this
    }

    /**This function is a Fluent-API-like Setter for onProgressUpdate**/
    fun onProgressUpdate(function: (Progress) -> Unit) : CoroutineBackgroundTask<Progress, Output> {
        onProgressUpdate = function
        return this
    }

    /**This function is a Fluent-API-like Setter for onCanceled**/
    fun onCanceled(function: () -> Unit) : CoroutineBackgroundTask<Progress, Output> {
        onCanceled = function
        return this
    }

    /**This function is a Fluent-API-like Setter for OnError**/
    fun onError(function: (Throwable) -> Unit) : CoroutineBackgroundTask<Progress, Output>{
        onError = function
        return this
    }

    /**This function can be used to notify the UI about an updated progress by calling onProgressUpdate**/
    suspend fun publishProgress(progress: Progress) = coroutineScope{
        withContext(mainDispatcher){
            onProgressUpdate.invoke(progress)
        }
    }

    /**This function starts the CoroutineBackgroundTask**/
    fun start(){
        if(this::onExecute.isInitialized){
            //execute task
            job = scope.launch {
                val time = measureTimeMillis {
                    withContext(mainDispatcher){
                        onPreExecute.invoke(this)
                    }

                    try {
                        val result = withContext(Dispatchers.IO){
                            onExecute.invoke(this@CoroutineBackgroundTask,this)
                        }

                        withContext(mainDispatcher){
                            onDone.invoke(result,this)
                        }
                    }
                    catch(ex : Exception){
                        withContext(mainDispatcher){
                            onError.invoke(ex)
                        }
                    }
                }
                println("Coroutine Backgroundtask ended. Total time: $time ms")
            }
        }
        else{
            throw CoroutineBackgroundTaskInitializationException("onExecute was not initialized")
        }
    }

    /**
     * This function can be used to cancel the execution of this CoroutineBackgroundTask
     **/
    fun cancel() : Boolean{
        job?.cancel()
        onCanceled()
        return true
    }
}

/**
 * This class defines an exception to be thrown when CoroutineBackgroundTask is not initialized correctly
 * **/
class CoroutineBackgroundTaskInitializationException(msg : String) : Exception(msg) {
    constructor() : this("")
}