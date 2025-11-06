package com.example.weatherapp

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var loadingIndicator: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Основной LinearLayout
        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.parseColor("#E3F2FD")) // нежно-голубой фон
            setPadding(40)
        }

        // Индикатор загрузки
        loadingIndicator = ProgressBar(this).apply {
            isIndeterminate = true
            visibility = View.VISIBLE
        }

        mainLayout.addView(loadingIndicator)
        setContentView(mainLayout)

        loadWeather()
    }

    private fun loadWeather() {
        val apiKey = "fa8b3df74d4042b9aa7135114252304"
        val coordinates = "55.7569,37.6151"

        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getForecast(apiKey, coordinates, 3)
                }

                showLoading(false)
                showWeather(response)

            } catch (e: Exception) {
                showLoading(false)
                showErrorDialog()
            }
        }
    }

    private fun showWeather(data: com.example.weatherapp.network.WeatherResponse) {
        mainLayout.removeAllViews()

        // Текущая погода
        val title = TextView(this).apply {
            text = "Погода в ${data.location.name}"
            textSize = 22f
            setTextColor(Color.parseColor("#0D47A1"))
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 20)
        }

        val current = TextView(this).apply {
            text = "${data.current.temp_c}°C, ${data.current.condition.text}"
            textSize = 18f
            setTextColor(Color.parseColor("#01579B"))
            gravity = Gravity.CENTER
        }

        // Почасовой прогноз
        val hoursLabel = TextView(this).apply {
            text = "\nПочасовой прогноз:"
            textSize = 18f
            setTextColor(Color.parseColor("#0D47A1"))
            setPadding(0, 40, 0, 10)
        }

        val scroll = HorizontalScrollView(this)
        val hourlyLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val hours = data.forecast.forecastday[0].hour.take(8)
        hours.forEach { hour ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(20)
            }
            val time = hour.time.substringAfter(' ')
            val temp = hour.temp_c
            val label = TextView(this).apply {
                text = "$time\n${temp}°C"
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#01579B"))
            }
            item.addView(label)
            hourlyLayout.addView(item)
        }
        scroll.addView(hourlyLayout)

        // Прогноз на 3 дня
        val forecastLabel = TextView(this).apply {
            text = "\nПрогноз на 3 дня:"
            textSize = 18f
            setTextColor(Color.parseColor("#0D47A1"))
            setPadding(0, 40, 0, 20)
        }

        val forecastLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        data.forecast.forecastday.forEach { day ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(30)
                setBackgroundColor(Color.parseColor("#BBDEFB"))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 10, 0, 10)
                layoutParams = params
            }

            val text = TextView(this).apply {
                text = "${day.date}: ${day.day.avgtemp_c}°C, ${day.day.condition.text}"
                textSize = 16f
                setTextColor(Color.parseColor("#0D47A1"))
            }

            card.addView(text)
            forecastLayout.addView(card)
        }

        mainLayout.apply {
            addView(title)
            addView(current)
            addView(hoursLabel)
            addView(scroll)
            addView(forecastLabel)
            addView(forecastLayout)
        }
    }

    private fun showErrorDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage("Не удалось загрузить данные. Повторить попытку?")
            .setPositiveButton("Повторить") { _, _ -> loadWeather() }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        loadingIndicator.visibility = if (show) View.VISIBLE else View.GONE
    }
}
