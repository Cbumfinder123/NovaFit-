package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cibertec.novafit.R
import com.cibertec.novafit.databinding.FragmentActividadBinding

class ActividadFragment : Fragment() {

    private var _binding: FragmentActividadBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentActividadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.niveles_actividad,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerNivelActividad.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.tipos_cuerpo,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerTipoCuerpo.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.niveles_experiencia,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerNivelExperiencia.adapter = adapter
        }

        binding.btnFinalizar.setOnClickListener {
            val nivelActividad = binding.spinnerNivelActividad.selectedItem.toString()
            val tipoCuerpo = binding.spinnerTipoCuerpo.selectedItem.toString()
            val nivelExperiencia = binding.spinnerNivelExperiencia.selectedItem.toString()

            if (nivelActividad == "Selecciona un nivel" || tipoCuerpo == "Selecciona un tipo" || nivelExperiencia == "Selecciona un nivel") {
                Toast.makeText(requireContext(), "Selecciona opciones válidas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = arguments ?: return@setOnClickListener
            val newBundle = Bundle().apply {
                putString("nombre", bundle.getString("nombre", ""))
                putLong("fechaNacimiento", bundle.getLong("fechaNacimiento"))
                putInt("edad", bundle.getInt("edad"))
                putString("genero", bundle.getString("genero", ""))
                putDouble("peso", bundle.getDouble("peso"))
                putDouble("altura", bundle.getDouble("altura"))
                putDouble("circunferenciaCuello", bundle.getDouble("circunferenciaCuello"))
                putDouble("circunferenciaCintura", bundle.getDouble("circunferenciaCintura"))
                if (bundle.containsKey("circunferenciaCadera")) putDouble("circunferenciaCadera", bundle.getDouble("circunferenciaCadera"))
                putString("nivelActividad", nivelActividad)
                putString("objetivo", bundle.getString("objetivo", ""))
                putString("tipoCuerpo", tipoCuerpo)
                putString("nivelExperiencia", nivelExperiencia)
            }

            (requireActivity() as MainActivity).openFragment(CredencialesFragment().apply { arguments = newBundle })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}