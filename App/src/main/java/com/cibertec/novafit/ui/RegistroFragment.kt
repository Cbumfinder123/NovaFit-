package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cibertec.novafit.R
import com.cibertec.novafit.databinding.FragmentRegistroBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Calendar

class RegistroFragment : Fragment() {

    private var _binding: FragmentRegistroBinding? = null
    private val binding get() = _binding!!
    private var fechaNacimientoMillis: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.objetivos,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerObjetivo.adapter = adapter
        }

        binding.tilFecha.setEndIconOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Fecha de Nacimiento")
                .build()
            picker.addOnPositiveButtonClickListener { selection ->
                fechaNacimientoMillis = selection
                binding.etFecha.setText(picker.headerText)
            }
            picker.show(parentFragmentManager, "date_picker")
        }

        binding.btnSiguiente.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val genero = when {
                binding.rbMasculino.isChecked -> "Masculino"
                binding.rbFemenino.isChecked -> "Femenino"
                binding.rbOtro.isChecked -> "Otro"
                else -> ""
            }
            val peso = binding.etPeso.text.toString().toDoubleOrNull() ?: 0.0


            val altura = binding.etAltura.text.toString().toDoubleOrNull() ?: 0.0
            if (altura <= 0) {
                Toast.makeText(requireContext(), "Ingresa una altura válida en cm", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val objetivo = binding.spinnerObjetivo.selectedItem.toString()

            if (nombre.isEmpty() || fechaNacimientoMillis == 0L || genero.isEmpty() || peso <= 0 || altura <= 0 || objetivo.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val edad = calcularEdad(fechaNacimientoMillis)
            val bundle = Bundle().apply {
                putString("nombre", nombre)
                putLong("fechaNacimiento", fechaNacimientoMillis)
                putInt("edad", edad)
                putString("genero", genero)
                putDouble("peso", peso)
                putDouble("altura", altura)
                putString("objetivo", objetivo)
            }
            (requireActivity() as MainActivity).openFragment(MedidasFragment().apply { arguments = bundle })
        }
    }

    private fun calcularEdad(fechaMillis: Long): Int {
        val nacimiento = Calendar.getInstance().apply { timeInMillis = fechaMillis }
        val hoy = Calendar.getInstance()
        var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)
        if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) edad--
        return edad
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}