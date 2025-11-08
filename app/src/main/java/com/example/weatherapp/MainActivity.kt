package com.example.weatherapp

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var loadingIndicator: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.parseColor("#E3F2FD"))
            setPadding(40)
        }

        val loaderContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        loadingIndicator = ProgressBar(this).apply {
            isIndeterminate = true
            visibility = View.VISIBLE

            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            layoutParams = params
            indeterminateDrawable.setColorFilter(
                Color.parseColor("#0D47A1"),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        loaderContainer.addView(loadingIndicator)
        mainLayout.addView(loaderContainer)
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

        val title = TextView(this).apply {
            text = "Погода в ${data.location.name}"
            textSize = 26f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#0D47A1"))
            gravity = Gravity.CENTER
            setPadding(0, 80, 0, 50)
        }

        val roundedTemp = data.current.temp_c.roundToInt()
        val currentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val size = 350
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(0, 40, 0, 40)
            layoutParams = params
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#BBDEFB"))
            }
        }

        val tempText = TextView(this).apply {
            text = "$roundedTemp°C"
            textSize = 32f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#01579B"))
        }

        val condText = TextView(this).apply {
            text = data.current.condition.text
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#01579B"))
        }

        currentLayout.addView(tempText)
        currentLayout.addView(condText)

        val hoursLabel = TextView(this).apply {
            text = "\nПочасовой прогноз:"
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#0D47A1"))
            setPadding(0, 60, 0, 20)
        }

        val scroll = HorizontalScrollView(this)
        val hourlyLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val hours = data.forecast.forecastday[0].hour.filter {
            val hour = it.time.substringAfter(" ").substringBefore(":").toInt()
            hour in 0..23
        }
        hours.forEach { hour ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(25)
            }

            val time = hour.time.substringAfter(' ')
            val temp = hour.temp_c.roundToInt()
            val label = TextView(this).apply {
                text = "$time\n${temp}°C"
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#01579B"))
            }
            item.addView(label)
            hourlyLayout.addView(item)
        }
        scroll.addView(hourlyLayout)

        val forecastLabel = TextView(this).apply {
            text = "\nПрогноз на 3 дня:"
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#0D47A1"))
            setPadding(0, 60, 0, 20)
        }

        val forecastLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        data.forecast.forecastday.forEach { day ->

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 50, 50, 50)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 20, 0, 20)
                layoutParams = params
                background = GradientDrawable().apply {
                    cornerRadius = 40f
                    setColor(Color.parseColor("#BBDEFB"))
                }
            }

            val headerLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 0, 0, 0)
            }

            val dateText = TextView(this).apply {
                text = day.date
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.parseColor("#0D47A1"))
                val params =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                layoutParams = params
            }

            val arrowIcon = ImageView(this).apply {
                setImageResource(android.R.drawable.arrow_down_float)
                setColorFilter(Color.parseColor("#0D47A1"))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 20, 0)
                layoutParams = params
            }

            headerLayout.addView(dateText)
            headerLayout.addView(arrowIcon)

            val detailsLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE
                setPadding(0, 20, 0, 0)
            }
            val tempText = TextView(this).apply {
                text = "${day.day.avgtemp_c.roundToInt()}°C"
                textSize = 16f
                setTextColor(Color.parseColor("#01579B"))
            }

            val conditionText = TextView(this).apply {
                text = day.day.condition.text
                textSize = 16f
                setTextColor(Color.parseColor("#01579B"))
            }

            detailsLayout.addView(tempText)
            detailsLayout.addView(conditionText)

            card.addView(headerLayout)
            card.addView(detailsLayout)

            headerLayout.setOnClickListener {
                val isExpanded = detailsLayout.visibility == View.VISIBLE
                detailsLayout.visibility = if (isExpanded) View.GONE else View.VISIBLE
                arrowIcon.setImageResource(
                    if (isExpanded) android.R.drawable.arrow_down_float
                    else android.R.drawable.arrow_up_float
                )
            }

            forecastLayout.addView(card)

        }
        mainLayout.apply {
            addView(title)
            addView(currentLayout)
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
