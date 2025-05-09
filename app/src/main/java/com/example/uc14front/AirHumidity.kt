package com.example.uc14front

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class AirHumidity {
    fun obterClima(latitude: Double, longitude: Double, onResult: (clima: Clima?) -> Unit) {
        val client = OkHttpClient()
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature,weather_code,wind_speed_10m,relative_humidity_2m,apparent_temperature,precipitation_sum,air_quality_index&timezone=auto"

        val request = Request.Builder()
            .url(url)
            .build()

        // Fazendo a requisição de forma assíncrona
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                println("Erro na requisição: ${e.message}")
                onResult(null)
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (!response.isSuccessful) {
                    println("Erro na requisição: ${response.code}")
                    onResult(null)
                    return
                }

                val responseData = response.body?.string()

                if (responseData != null) {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val current = jsonResponse.getJSONObject("current")

                        // Extração dos dados necessários
                        val temperatura = current.getDouble("temperature")
                        val umidade = current.getDouble("relative_humidity_2m")
                        val sensacaoTermica = current.getDouble("apparent_temperature")
                        val vento = current.getDouble("wind_speed_10m")
                        val chuva = current.getDouble("precipitation_sum")
                        val qualidadeAr = current.getDouble("air_quality_index")

                        // Envia os dados para a interface ou outra classe que os utilizará
                        val clima = Clima(
                            temperatura,
                            umidade,
                            sensacaoTermica,
                            vento,
                            chuva,
                            qualidadeAr
                        )
                        onResult(clima)
                    } catch (e: Exception) {
                        println("Erro ao processar dados: ${e.message}")
                        onResult(null)
                    }
                } else {
                    println("Erro: dados não encontrados.")
                    onResult(null)
                }
            }
        })
    }

    data class Clima(
        val temperatura: Double,
        val umidade: Double,
        val sensacaoTermica: Double,
        val vento: Double,
        val chuva: Double,
        val qualidadeAr: Double
    )
}
