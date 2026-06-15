package com.example.wishlistapp
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    data class WishItem(
        val name: String,
        val price: Int,
        val priority: String,
        var isBought: Boolean = false
    )
    private val items = mutableListOf<WishItem>()
    private var currentPrice = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val nameInput = findViewById<EditText>(R.id.nameInput)
        val priceSeek = findViewById<SeekBar>(R.id.priceSeek)
        val priceText = findViewById<TextView>(R.id.priceText)
        val priorityGroup = findViewById<RadioGroup>(R.id.priorityGroup)
        val addButton = findViewById<Button>(R.id.addButton)
        val listContainer = findViewById<LinearLayout>(R.id.listContainer)
        val totalText = findViewById<TextView>(R.id.totalText)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        priceSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar?, progress: Int, fromUser: Boolean) {
                currentPrice = progress
                priceText.text = "Цена: $currentPrice руб"
            }
            override fun onStartTrackingTouch(seek: SeekBar?) {}
            override fun onStopTrackingTouch(seek: SeekBar?) {}
        })
        addButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (name.isNotEmpty()) {
                val priority = when (priorityGroup.checkedRadioButtonId) {
                    R.id.priorityHigh -> "Высокий"
                    R.id.priorityMedium -> "Средний"
                    else -> "Низкий"
                }
                items.add(WishItem(name, currentPrice, priority))
                nameInput.text.clear()
                priceSeek.progress = 0
                updateList(listContainer, totalText, progressBar)
            } else {
                Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show()
            }
        }
        deleteButton.setOnClickListener {
            if (items.any { it.isBought }) {
                AlertDialog.Builder(this)
                    .setTitle("Подтверждение")
                    .setMessage("Удалить купленные товары?")
                    .setPositiveButton("Да") { _, _ ->
                        items.removeAll { it.isBought }
                        updateList(listContainer, totalText, progressBar)
                    }
                    .setNegativeButton("Нет", null)
                    .show()
            } else {
                Toast.makeText(this, "Нет купленных товаров", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateList(container: LinearLayout, totalText: TextView, progressBar: ProgressBar) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)
        for (item in items) {
            val view = inflater.inflate(android.R.layout.simple_list_item_multiple_choice, container, false)
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            text1.text = "${item.name} — ${item.price} руб [${item.priority}]"
            container.addView(view)
        }
        updateTotals(totalText, progressBar)
    }
    private fun updateTotals(totalText: TextView, progressBar: ProgressBar) {
        val total = items.sumOf { it.price }
        val bought = items.sumOf { if (it.isBought) it.price else 0 }
        val percent = if (total > 0) (bought * 100 / total) else 0
        totalText.text = "Куплено: $bought | Всего: $total"
        progressBar.progress = percent
    }
}
