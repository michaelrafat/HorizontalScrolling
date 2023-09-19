package com.michael.horizontalscrolling

import android.content.Context
import android.os.Bundle
import android.util.LayoutDirection
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.layoutDirection
import com.michael.horizontalscrolling.databinding.ActivityMainBinding
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var customGridLayoutManager: CustomGridLayoutManager<String, ItemsAdapter.ViewHolder>
    private lateinit var itemsAdapter: ItemsAdapter

    private var isMoreExpanded = false

    companion object {

        private const val MAX_ITEMS = 90
        private const val COLUMNS = 5
        private const val ROWS = 2

        private var isRTL = false

        fun getItems(): MutableList<String> {
            val list = mutableListOf<String>()
            for (i in 1..MAX_ITEMS) {
                list.add("$i")
            }
            return list
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val language = if (isRTL) {
            Language.ArabicRTL.language
        } else {
            Language.EnglishLTR.language
        }
        super.attachBaseContext(MyContextWrapper.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * init recyclerview adapter
         **/
        itemsAdapter = ItemsAdapter(getItems())

        /**
         * init custom grid layout manager
         **/
        customGridLayoutManager = CustomGridLayoutManager(
            adapter = itemsAdapter,
            columns = COLUMNS,
            rows = ROWS,
            reverseLayout = isRTL()
        )

        /**
         * init recyclerview with snapHelper
         **/
        binding.rvItems.apply {
            layoutManager = customGridLayoutManager
            adapter = itemsAdapter
        }

        binding.btnMore.setOnClickListener {
            if (isMoreExpanded.not()) {
                binding.btnNext.visibility = View.VISIBLE
                binding.btnPrevious.visibility = View.VISIBLE
                binding.btnRTL.visibility = View.VISIBLE
                binding.btnAdd.visibility = View.VISIBLE
                binding.btnMore.rotationX = 180f
                isMoreExpanded = true
            } else {
                binding.btnNext.visibility = View.GONE
                binding.btnPrevious.visibility = View.GONE
                binding.btnRTL.visibility = View.GONE
                binding.btnAdd.visibility = View.GONE
                binding.btnMore.rotationX = 180f
                isMoreExpanded = false
            }
        }

        binding.btnNext.setOnClickListener {
            customGridLayoutManager.nextPage(binding.rvItems)
        }

        binding.btnPrevious.setOnClickListener {
            customGridLayoutManager.previousPage(binding.rvItems)
        }

        binding.btnAdd.setOnClickListener {
            itemsAdapter.onItemAdded(
                itemsAdapter.baseItems.size,
                (itemsAdapter.baseItems.size + 1).toString()
            )
            Toast.makeText(this, "Item Added!", Toast.LENGTH_LONG).show()
        }

        binding.btnRTL.setOnClickListener {
            isRTL = isRTL.not()
            recreate()
        }

    }

    /**
     * RTL, LTR and language support
     **/

    private fun isRTL(): Boolean {
        return Locale.getDefault().layoutDirection == LayoutDirection.RTL
    }

    private fun getMyContext(language: String): Context {
        return MyContextWrapper.wrap(this, language)
    }

    sealed class Language(val language: String) {
        object ArabicRTL : Language("ar")
        object EnglishLTR : Language("en")
    }

}