package de.rogallab.mobile.domain.utilities

//sealed class Resource<T> (
//    val data:    T?      = null,
//    val message: String? = null
//){
//    class Success<T>(message: String, data: T)         : Resource<T>(data, message)
//    class Error<T>  (message: String, data: T? = null) : Resource<T>(data, message)
//    class Loading<T> : Resource<T>()
//}