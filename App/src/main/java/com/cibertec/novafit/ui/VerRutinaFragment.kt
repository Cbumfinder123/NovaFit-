package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.novafit.data.RutinaRepository
import com.cibertec.novafit.databinding.FragmentVerRutinaBinding

class VerRutinaFragment : Fragment() {

    private var _binding: FragmentVerRutinaBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RutinaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerRutinaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        cargarRutina()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.rvSesiones.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = RutinaAdapter().also { this@VerRutinaFragment.adapter = it }
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        binding.fabEditar.setOnClickListener {
            (requireActivity() as MainActivity).openFragment(CrearRutinaFragment())
        }
    }

    private fun cargarRutina() {
        val email = (requireActivity() as MainActivity).emailActual
        val rutina = RutinaRepository(requireContext()).obtenerRutina(email)

        if (rutina != null) {
            adapter.submitList(rutina.planSemanal)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}