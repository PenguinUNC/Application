package com.example.myapplication
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
data class WishItem(
    val title: String,
    val price: Int,
    val url: String,
    val priority: String,
    val trackPrice: Boolean,
    var isPurchased: Boolean = false
)
class MainActivity : AppCompatActivity()
{
    private val wishlist = mutableListOf<WishItem>()
    private var currentSelectedPrice = 0
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etUrl = findViewById<EditText>(R.id.etUrl)
        val tvPriceLabel = findViewById<TextView>(R.id.tvPriceLabel)
        val sbPrice = findViewById<SeekBar>(R.id.sbPrice)
        val rgPriority = findViewById<RadioGroup>(R.id.rgPriority)
        val switchTrackPrice = findViewById<MaterialSwitch>(R.id.switchTrackPrice)
        val btnAddWish = findViewById<Button>(R.id.btnAddWish)
        val listContainer = findViewById<LinearLayout>(R.id.listContainer)
        val tvTotalCost = findViewById<TextView>(R.id.tvTotalCost)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btnClearPurchased = findViewById<Button>(R.id.btnClearPurchased)
        sbPrice.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
            {
                currentSelectedPrice = progress
                tvPriceLabel.text = "Желаемая цена: $currentSelectedPrice руб."
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        btnAddWish.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val url = etUrl.text.toString().trim()
            if (title.isEmpty())
            {
                Toast.makeText(this@MainActivity, "Введите название товара!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedRadioId = rgPriority.checkedRadioButtonId
            val radioButton = findViewById<RadioButton>(selectedRadioId)
            val priority = radioButton?.text.toString()
            val isTracking = switchTrackPrice.isChecked
            val newItem = WishItem(title, currentSelectedPrice, url, priority, isTracking)
            wishlist.add(newItem)
            etTitle.text.clear()
            etUrl.text.clear()
            sbPrice.progress = 0
            switchTrackPrice.isChecked = false
            updateWishListUI(listContainer, tvTotalCost, progressBar)
            Toast.makeText(this@MainActivity, "Добавлено: $title", Toast.LENGTH_SHORT).show()
        }
        btnClearPurchased.setOnClickListener {
            if (wishlist.none { it.isPurchased })
            {
                Toast.makeText(this@MainActivity, "Нет купленных товаров для удаления", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Подтверждение удаления")
                .setMessage("Вы уверены, что хотите удалить все купленные товары из списка?")
                .setPositiveButton("Удалить") { dialog, _ ->
                    wishlist.removeAll { it.isPurchased }
                    updateWishListUI(listContainer, tvTotalCost, progressBar)
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }
    private fun updateWishListUI(container: LinearLayout, tvTotal: TextView, progress: ProgressBar)
    {
        container.removeAllViews()
        var totalSum = 0
        var purchasedSum = 0
        val inflater = LayoutInflater.from(this)
        for (item in wishlist)
        {
            totalSum += item.price
            if (item.isPurchased)
            {
                purchasedSum += item.price
            }
            val itemView = inflater.inflate(R.layout.item_wish, container, false)
            val cbPurchased = itemView.findViewById<CheckBox>(R.id.cbPurchased)
            val tvItemTitle = itemView.findViewById<TextView>(R.id.tvItemTitle)
            val tvItemDetails = itemView.findViewById<TextView>(R.id.tvItemDetails)
            val tvItemUrl = itemView.findViewById<TextView>(R.id.tvItemUrl)
            tvItemTitle.text = item.title
            tvItemDetails.text = "Цена: ${item.price} руб. | Приоритет: ${item.priority}"
            if (item.url.isNotEmpty())
            {
                tvItemUrl.text = item.url
                tvItemUrl.visibility = View.VISIBLE
            }
            cbPurchased.isChecked = item.isPurchased
            cbPurchased.setOnCheckedChangeListener { _, isChecked ->
                item.isPurchased = isChecked
                recalculateProgress(wishlist, tvTotal, progress)
            }
            container.addView(itemView)
        }
        recalculateProgress(wishlist, tvTotal, progress)
    }
    private fun recalculateProgress(list: List<WishItem>, tvTotal: TextView, progress: ProgressBar)
    {
        val totalSum = list.sumOf { it.price }
        val purchasedSum = list.sumOf { if (it.isPurchased) it.price else 0 }
        val percentage = if (totalSum > 0) ((purchasedSum.toFloat() / totalSum.toFloat()) * 100).toInt() else 0
        tvTotal.text = "Куплено: $purchasedSum руб. из $totalSum руб."
        progress.progress = percentage
    }
}
