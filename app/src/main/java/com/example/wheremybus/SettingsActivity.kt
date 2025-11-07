
package com.example.wheremybus

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wheremybus.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val PREFS_NAME = "user_settings"
    private val KEY_MAP_TYPE = "map_type"
    private val KEY_NOTIFICATIONS = "notifications"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // --- Setup Map Type Spinner ---
        val mapTypes = arrayOf("Normal", "Satellite", "Terrain")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mapTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.mapTypeSpinner.adapter = adapter

        // Load saved map type
        val savedMapType = sharedPref.getString(KEY_MAP_TYPE, "Normal")
        binding.mapTypeSpinner.setSelection(mapTypes.indexOf(savedMapType))

        binding.mapTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = mapTypes[position]
                sharedPref.edit().putString(KEY_MAP_TYPE, selected).apply()
                Toast.makeText(this@SettingsActivity, "Map type set to $selected", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // --- Setup Notification Switch ---
        val notificationsEnabled = sharedPref.getBoolean(KEY_NOTIFICATIONS, true)
        binding.notificationSwitch.isChecked = notificationsEnabled

        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply()
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Notifications $status", Toast.LENGTH_SHORT).show()
        }
    }
}
