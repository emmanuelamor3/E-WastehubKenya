package com.example.e_wastehubkenya.ui.theme.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.e_wastehubkenya.databinding.FragmentSettingsBinding
import com.example.e_wastehubkenya.ui.theme.ThemeManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchDarkMode.isChecked = ThemeManager.isDarkTheme(requireContext())

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            ThemeManager.setTheme(requireContext(), mode)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
