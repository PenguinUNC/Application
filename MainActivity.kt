package com.example.marketplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.SeekBar
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

data class WishItem(
    val name: String,
    val price: Int,
    val url: String,
    val priority: String,
    val trackPrice: Boolean,
    var isInTransit: Boolean = false
)

class MainActivity : AppCompatActivity()
{
    private val wishList = mutableListOf<WishItem>()
    private val maxBudget = 5000 // Твой баланс 5000 ₽

    private fun getID(name: String, type: String): Int
    {
        return resources.getIdentifier(name, type, packageName)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        val resActivityMain = getID("activity_main", "layout")
        setContentView(resActivityMain)

        val etName = findViewById<EditText>(getID("etWishName", "id"))
        val etUrl = findViewById<EditText>(getID("etWishUrl", "id"))
        val seekBarPrice = findViewById<SeekBar>(getID("seekBarPrice", "id"))
        val tvPriceValue = findViewById<TextView>(getID("tvPriceValue", "id"))
        val rgPriority = findViewById<RadioGroup>(getID("rgPriority", "id"))
        val switchTrack = findViewById<Switch>(getID("switchTrack", "id"))
        val btnAddWish = findViewById<Button>(getID("btnAddWish", "id"))
        val btnClearPurchased = findViewById<Button>(getID("btnClearPurchased", "id"))
        val wishListContainer = findViewById<LinearLayout>(getID("wishListContainer", "id"))

        seekBarPrice.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
            {
                tvPriceValue.text = "Цена: $progress ₽"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnAddWish.setOnClickListener {
            val name = etName.text.toString().trim()
            val url = etUrl.text.toString().trim()
            val price = seekBarPrice.progress
            val trackPrice = switchTrack.isChecked

            if (name.isEmpty())
            {
                Toast.makeText(this, "Заполните название!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedId = rgPriority.checkedRadioButtonId
            val radioButton = findViewById<RadioButton>(selectedId)
            val priority = radioButton?.text?.toString() ?: "Хочу"

            wishList.add(WishItem(name, price, url, priority, trackPrice, isInTransit = false))

            etName.setText("")
            etUrl.setText("")
            seekBarPrice.progress = 0
            switchTrack.isChecked = false

            renderList(wishListContainer)
        }

        btnClearPurchased.setOnClickListener {
            if (wishList.none { it.isInTransit })
            {
                Toast.makeText(this, "Нет товаров в пути!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Очистка")
            builder.setMessage("Удалить из списка все товары со статусом «В пути»?")
            builder.setPositiveButton("Да") { dialog, _ ->
                wishList.removeAll { it.isInTransit }
                renderList(wishListContainer)
            }
            builder.setNegativeButton("Нет", null)
            builder.create().show()
        }

        recalculateStats()
    }

    private fun renderList(container: LinearLayout)
    {
        container.removeAllViews()

        val resLayoutId = getID("item_wish", "layout")
        if (resLayoutId == 0) return

        val inflater = LayoutInflater.from(this)
        val resTitleId = getID("tvWishTitle", "id")
        val resUrlId = getID("tvWishUrl", "id")
        val resPriceId = getID("tvWishPrice", "id")
        val resStatusId = getID("tvWishStatus", "id")
        val resBtnTransitId = getID("btnTransitStatus", "id")

        for (item in wishList)
        {
            val itemView = inflater.inflate(resLayoutId, container, false)
            val tvTitle = itemView.findViewById<TextView>(resTitleId)
            val tvUrl = itemView.findViewById<TextView>(resUrlId)
            val tvPrice = itemView.findViewById<TextView>(resPriceId)
            val tvStatus = itemView.findViewById<TextView>(resStatusId)
            val btnTransit = itemView.findViewById<Button>(resBtnTransitId)

            tvTitle.text = item.name
            tvUrl.text = if (item.url.isEmpty()) "Ссылка отсутствует" else item.url
            tvPrice.text = "${item.price} ₽"

            if (item.isInTransit) {
                tvStatus.text = "Статус: В пути 🚚"
                btnTransit.text = "Вернуть"
            } else {
                tvStatus.text = "Статус: Ожидает"
                btnTransit.text = "В пути"
            }

            btnTransit.setOnClickListener {
                item.isInTransit = !item.isInTransit
                renderList(container)
            }

            itemView.setOnLongClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Удаление товара")
                builder.setMessage("Удалить «${item.name}»?")
                builder.setPositiveButton("Удалить") { dialog, _ ->
                    wishList.remove(item)
                    renderList(container)
                }
                builder.setNegativeButton("Отмена", null)
                builder.create().show()
                true
            }

            container.addView(itemView)
        }

        recalculateStats()
    }

    private fun recalculateStats()
    {
        var totalCost = 0
        var transitCost = 0

        for (item in wishList)
        {
            totalCost += item.price
            if (item.isInTransit)
            {
                transitCost += item.price
            }
        }

        val tvTotalCost = findViewById<TextView>(getID("tvTotalCost", "id"))
        if (tvTotalCost != null)
        {
            tvTotalCost.text = "$totalCost ₽"
        }

        val progressPercent = if (maxBudget > 0) (transitCost * 100) / maxBudget else 0
        val finalPercent = Math.min(100, progressPercent)

        val tvProgressPercent = findViewById<TextView>(getID("tvProgressPercent", "id"))
        if (tvProgressPercent != null)
        {
            tvProgressPercent.text = "В пути: $finalPercent% из 5000 ₽"
        }

        val progressBar = findViewById<ProgressBar>(getID("progressBar", "id"))
        if (progressBar != null)
        {
            progressBar.progress = finalPercent
        }
    }
}
