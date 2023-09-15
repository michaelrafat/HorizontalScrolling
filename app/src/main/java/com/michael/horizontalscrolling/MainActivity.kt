package com.michael.horizontalscrolling

import android.content.Context
import android.os.Bundle
import android.util.LayoutDirection
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.layoutDirection
import com.michael.horizontalscrolling.databinding.ActivityMainBinding
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var customGridLayoutManager: CustomGridLayoutManager<String, ItemsAdapter.ViewHolder>
    private lateinit var itemsAdapter: ItemsAdapter

    companion object {

        private const val MAX_ITEMS = 90
        private const val COLUMNS = 5
        private const val ROWS = 2

        fun getItems(): MutableList<String> {
            val list = mutableListOf<String>()
            for (i in 1..MAX_ITEMS) {
                list.add("$i")
            }
            return list
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Language.EnglishLTR.language))
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