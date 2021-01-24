# KotlinUtils
This project contains utility classes and extensions for Kotlin.

## CoroutineBackgroundTask
The CoroutineBackgroundTask can be used to execute certain Actions in Background via Coroutines and Fluent-API.
It was developed during the Development of Homium by MademDevs and later improved. It was inspired by AsyncTask for Android.
Every CoroutineBackgroundTask has a generic type for Progress (Reporting Progress to UI) and Output to be returned
when Background-Action is over.

CAUTION: Set the mainDispatcher to another value than Dispatchers.Main when you are not operating on Android
