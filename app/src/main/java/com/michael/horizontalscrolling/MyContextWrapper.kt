package com.michael.horizontalscrolling

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import java.util.Locale


class MyContextWrapper(base: Context?) : ContextWrapper(base) {

    companion object {

        fun wrap(context: Context, language: String): ContextWrapper {
            val config = context.resources.configuration
            val sysLocale = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                getSystemLocale(config)
            } else {
                getSystemLocaleLegacy(config)
            }
            if (language != "" && sysLocale.language != language) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                setSystemLocale(config, locale)
            }
            return MyContextWrapper(context.createConfigurationContext(config))
        }

        private fun getSystemLocaleLegacy(config: Configuration): Locale {
            return config.locale
        }

        private fun getSystemLocale(config: Configuration): Locale {
            return config.locales[0]
        }

        private fun setSystemLocaleLegacy(config: Configuration, locale: Locale?) {
            config.locale = locale
        }

        private fun setSystemLocale(config: Configuration, locale: Locale?) {
            config.setLocale(locale)
        }

    }

}