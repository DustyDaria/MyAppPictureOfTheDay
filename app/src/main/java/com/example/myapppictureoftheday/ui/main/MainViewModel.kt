package com.example.myapppictureoftheday.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException

class MainViewModel(
    private val liveData: MutableLiveData<PictureOfTheDayData> = MutableLiveData(),
    //LiveData - один из архитектурных компонентов android (пересылка определенных событий на наш экран через .value)
    private val retrofit: PODRetrofitImpl = PODRetrofitImpl()
    ) : ViewModel() {

    fun getData():LiveData<PictureOfTheDayData>{
        sendServerRequest()
        return liveData
    }

    private fun sendServerRequest(){
        liveData.value = PictureOfTheDayData.Loading(process = 0) //Процесс загрузки событий на наш экран
        retrofit.getRetrofitImpl().getPictureOfTheDay(apiKey = "OhH1zPWjMnnlr4Rlny6fwYTADVmZTe4jc3P0vcFm").enqueue(object :
        Callback<ServerResponse>{

            override fun onResponse(
                call: Call<ServerResponse>,
                response: retrofit2.Response<ServerResponse>
            ) {
                if(response.isSuccessful && response.body() != null){ //Если ответ с сервера получен и какие-то данные пришли, тогда обновляем значение liveData
                    liveData.value = PictureOfTheDayData.Success(response.body()!!)
                } else{
                    liveData.value = PictureOfTheDayData.Error(Throwable("Error")) //Иначе передаем неизвестную ошибку
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) { //Передача более корректной ошибки
                liveData.value = PictureOfTheDayData.Error(t)

            }
        }
        )
    }

}

data class ServerResponse( //Получение данных с удаленного сервера NASA
var explanation:String?,
var url:String?
)

sealed class PictureOfTheDayData{ // классы базовых состояний приложения
    data class Success(val serverResponse: ServerResponse) : PictureOfTheDayData()
    data class Error(val error: Throwable) : PictureOfTheDayData()
    data class Loading(val process: Int) : PictureOfTheDayData() //Класс для процесса загрузки (можно еще доработать)
}

interface PictureOfTheDayApi{ //Создание интерфейса для выхода в интернет
    @GET("planetary/apod")
    fun getPictureOfTheDay(@Query("api_key") apiKey:String) : Call<ServerResponse> //Запрос на передачу api_key
}

class PODRetrofitImpl{ //Этот класс отвечает за выход в интернет/получение данных из интернета

    private val baseUrl = "https://api.nasa.gov/"

    fun getRetrofitImpl() : PictureOfTheDayApi{
        val podRetrofit = Retrofit.Builder() //Создание экземпляра библиотеки для выхода в интернет
        .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create())) //Получение конвертированных данных
            .client(createOkHttpClient(PODInterceptor())) // перехват конвертированных данных и передает их в BODY (explanation, url)
            .build()
        return podRetrofit.create((PictureOfTheDayApi::class.java)) //для корректной распарсировки данных передаем функцию с запросом на подключение по api_key
    }

    private fun createOkHttpClient(interceptor: Interceptor) :OkHttpClient{
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(interceptor)
        httpClient.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        return httpClient.build()
    }

    inner class PODInterceptor: Interceptor{

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            return chain.proceed(chain.request())
        }
    }
}