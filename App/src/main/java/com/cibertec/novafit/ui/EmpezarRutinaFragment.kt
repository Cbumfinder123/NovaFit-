package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.novafit.data.RutinaRepository
import com.cibertec.novafit.databinding.FragmentEmpezarRutinaBinding

class EmpezarRutinaFragment : Fragment() {

    private var _binding: FragmentEmpezarRutinaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmpezarRutinaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        cargarDias()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun cargarDias() {
        val email = (requireActivity() as MainActivity).emailActual
        val rutina = RutinaRepository(requireContext()).obtenerRutina(email)

        if (rutina != null) {
            val adapter = DiaSeleccionAdapter(rutina.planSemanal) { diaIndex ->
                // Al seleccionar un día
                val bundle = Bundle().apply {
                    putInt("diaIndex", diaIndex)
                }
                (requireActivity() as MainActivity).openFragment(
                    EjecutarEjercicioFragment().apply { arguments = bundle }
                )
            }

            binding.rvDias.layoutManager = LinearLayoutManager(requireContext())
            binding.rvDias.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}