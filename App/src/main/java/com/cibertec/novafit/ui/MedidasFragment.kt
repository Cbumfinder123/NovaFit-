package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cibertec.novafit.databinding.FragmentMedidasBinding

class MedidasFragment : Fragment() {

    private var _binding: FragmentMedidasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMedidasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val genero = arguments?.getString("genero") ?: "Otro"
        if (genero == "Femenino") {
            binding.tilCadera.visibility = View.VISIBLE
        }

        binding.btnSiguiente.setOnClickListener {
            val cuello = binding.etCuello.text.toString().toDoubleOrNull()
            val cintura = binding.etCintura.text.toString().toDoubleOrNull()
            val cadera = if (genero == "Femenino") binding.etCadera.text.toString().toDoubleOrNull() else null

            if (cuello == null || cintura == null || (genero == "Femenino" && cadera == null)) {
                Toast.makeText(requireContext(), "Completa todos los campos con valores válidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = arguments ?: Bundle()
            bundle.putDouble("circunferenciaCuello", cuello)
            bundle.putDouble("circunferenciaCintura", cintura)
            if (cadera != null) bundle.putDouble("circunferenciaCadera", cadera)
            (requireActivity() as MainActivity).openFragment(ActividadFragment().apply { arguments = bundle })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}