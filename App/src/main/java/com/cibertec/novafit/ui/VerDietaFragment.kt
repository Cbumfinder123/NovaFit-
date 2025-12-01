package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.novafit.data.DietaRepository
import com.cibertec.novafit.databinding.FragmentVerDietaBinding
import com.cibertec.novafit.model.Dieta

class VerDietaFragment : Fragment() {

    private var _binding: FragmentVerDietaBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DietaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerDietaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        cargarDieta()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.rvDias.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = DietaAdapter().also { this@VerDietaFragment.adapter = it }
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        binding.fabEditar.setOnClickListener {
            (requireActivity() as MainActivity).openFragment(CrearDietaFragment())
        }
    }

    private fun cargarDieta() {
        val email = (requireActivity() as MainActivity).emailActual
        val dieta = DietaRepository(requireContext()).obtenerDieta(email)

        if (dieta != null) {
            adapter.submitList(dieta.planSemanal)
        } else {

            Toast.makeText(requireContext(), "No hay dieta guardada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}