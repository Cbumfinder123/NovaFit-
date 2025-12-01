package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cibertec.novafit.databinding.FragmentCalcularMacrosBinding
import com.cibertec.novafit.data.PerfilRepository
import com.cibertec.novafit.logic.GeneradorDieta

class CalcularMacrosFragment : Fragment() {

    private var _binding: FragmentCalcularMacrosBinding? = null
    private val binding get() = _binding!!
    private lateinit var repo: PerfilRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalcularMacrosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        repo = PerfilRepository(requireContext())
        val email = (requireActivity() as MainActivity).emailActual
        val perfil = repo.getPerfilByEmail(email)

        if (perfil != null) {

            val macros = GeneradorDieta.calcularMacrosExactos(perfil)

            binding.txtCalorias.text = "${macros.calorias} kcal"
            binding.txtProteinas.text = "Proteínas: ${macros.proteinas} g"
            binding.txtGrasas.text = "Grasas: ${macros.grasas} g"
            binding.txtCarbohidratos.text = "Carbohidratos: ${macros.carbohidratos} g"
        } else {
            binding.txtCalorias.text = "No hay perfil"
            binding.txtProteinas.text = ""
            binding.txtGrasas.text = ""
            binding.txtCarbohidratos.text = ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}