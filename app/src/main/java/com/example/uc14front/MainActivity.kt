package com.example.uc14front

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import android.widget.TextView
import android.widget.Toast
import android.os.Build
import android.os.Looper
import android.widget.Button
import retrofit2.http.Query

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var apiService: ApiService
    private lateinit var tvCidade: TextView
    private lateinit var tvTemperatura: TextView
    private lateinit var btnAtualizar: Button
    private lateinit var tvSensacaoTermica: TextView
    private lateinit var tvQualidadeAr: TextView

    companion object {
        private const val REQUEST_CODE_LOCATION = 1001
        private const val MIN_TIME_MS = 10000L // 10 segundos
        private const val MIN_DISTANCE_M = 10f // 10 metros
        private const val BASE_URL = "http://10.0.2.2:3000/" // Para emulador
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        tvCidade = findViewById(R.id.tvCidade)
        tvTemperatura = findViewById(R.id.tvTemperatura)
        tvSensacaoTermica = findViewById(R.id.textView2)
        tvQualidadeAr = findViewById(R.id.textView)


        // Inicializa o Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkLocationPermission()
        btnAtualizar = findViewById(R.id.btnAtualizar)
        btnAtualizar.setOnClickListener {
            atualizarLocalizacao()
        }
    }

    private fun checkLocationPermission() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        // Verifica se todas as permissões necessárias foram concedidas
        val allPermissionsGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                REQUEST_CODE_LOCATION
            )
        }
    }

    private fun startLocationUpdates() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_MS,
                    MIN_DISTANCE_M,
                    this
                )
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_MS,
                    MIN_DISTANCE_M,
                    this
                )

                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                lastLocation?.let { onLocationChanged(it) }
            }
        } catch (e: SecurityException) {
            Log.e("LOCATION", "Erro de permissão: ${e.message}")
        } catch (e: Exception) {
            Log.e("LOCATION", "Erro geral: ${e.message}")
        }
    }


    // Adicione também o onRequestPermissionsResult
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                } else {
                    Toast.makeText(
                        this,
                        "Permissão de localização necessária para o funcionamento",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    override fun onLocationChanged(location: Location) {
        Log.d("LOCATION", "Nova localização: Lat=${location.latitude}, Long=${location.longitude}")
        fetchLocationData(location.latitude, location.longitude)
    }

    private fun fetchLocationData(latitude: Double, longitude: Double) {
        apiService.getLocationData(latitude, longitude).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    tvCidade.text = data?.localInfo?.address?.city ?: "Local desconhecido"
                    tvTemperatura.text = "%.1f°C".format(data?.previsao?.current?.temperature ?: 0.0)
                    tvSensacaoTermica.text = "Sensação térmica: %.1f°C".format(data?.previsao?.current?.apparent_temperature ?: 0.0)
                    tvQualidadeAr.text = "Qualidade do ar: ${data?.previsao?.current?.weather_code ?: 0}"
                } else {
                    tvCidade.text = "Erro: ${response.code()}"
                    Log.e("API", "Erro na resposta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                tvCidade.text = "Falha na conexão"
                Log.e("API", "Falha na chamada: ${t.message}")
            }
        })
    }

    private fun atualizarLocalizacao() {
        if (!::locationManager.isInitialized) {
            checkLocationPermission()
            return
        }

        try {
            val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LocationManager.GPS_PROVIDER
            } else {
                LocationManager.NETWORK_PROVIDER
            }

            val lastLocation = locationManager.getLastKnownLocation(provider)
            if (lastLocation != null) {
                onLocationChanged(lastLocation)
            } else {
                Toast.makeText(this, "Obtendo nova localização...", Toast.LENGTH_SHORT).show()
                locationManager.requestSingleUpdate(
                    provider,
                    { location -> onLocationChanged(location) },
                    Looper.getMainLooper()
                )
            }
        } catch (e: SecurityException) {
            Log.e("LOCATION", "Erro de permissão: ${e.message}")
            checkLocationPermission()
        } catch (e: Exception) {
            Log.e("LOCATION", "Erro: ${e.message}")
            Toast.makeText(this, "Erro ao buscar localização", Toast.LENGTH_SHORT).show()
        }
    }


    interface ApiService {
        @GET("api/local/resumo/{latitude}/{longitude}")
        fun getLocationData(
            @Path("latitude") latitude: Double,
            @Path("longitude") longitude: Double
        ): Call<WeatherResponse>
    }

    data class WeatherResponse(
        val localInfo: LocalInfo,
        val previsao: Previsao
    )

    data class LocalInfo(
        val display_name: String?,
        val address: Address?
    )

    data class Address(
        val city: String?,
        val state: String?,
        val country: String?
    )

    data class Previsao(
        val current: CurrentWeather
    )

    data class CurrentWeather(
        val temperature: Double,
        val weather_code: Int,
        val wind_speed_10m: Double,
        val apparent_temperature: Double
    )
}