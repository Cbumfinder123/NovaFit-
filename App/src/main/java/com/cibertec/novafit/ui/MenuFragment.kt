package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cibertec.novafit.data.DietaRepository
import com.cibertec.novafit.data.PerfilRepository
import com.cibertec.novafit.data.RutinaRepository
import com.cibertec.novafit.databinding.FragmentMenuBinding
import java.security.MessageDigest

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = (requireActivity() as MainActivity).emailActual
        val perfil = PerfilRepository(requireContext()).getPerfilByEmail(email)

        if (perfil != null) {
            binding.txtPerfil.text = "¡Hola, ${perfil.nombre}!"
        }

        binding.cardCalcular.setOnClickListener {
            (requireActivity() as MainActivity).openFragment(CalcularMacrosFragment())
        }


        binding.cardDieta.setOnClickListener {
            val dietaRepo = DietaRepository(requireContext())
            val dieta = dietaRepo.obtenerDieta(email)

            if (dieta != null) {
                (requireActivity() as MainActivity).openFragment(VerDietaFragment())
            } else {
                (requireActivity() as MainActivity).openFragment(CrearDietaFragment())
            }
        }


        binding.cardRutina.setOnClickListener {
            val rutinaRepo = RutinaRepository(requireContext())
            val rutina = rutinaRepo.obtenerRutina(email)

            if (rutina != null) {

                (requireActivity() as MainActivity).openFragment(VerRutinaFragment())
            } else {

                (requireActivity() as MainActivity).openFragment(CrearRutinaFragment())
            }
        }

        binding.cardEmpezarRutina.setOnClickListener {
            val rutinaRepo = RutinaRepository(requireContext())
            val rutina = rutinaRepo.obtenerRutina(email)

            if (rutina != null) {
                (requireActivity() as MainActivity).openFragment(EmpezarRutinaFragment())
            } else {
                android.widget.Toast.makeText(
                    requireContext(),
                    "⚠️ Necesitas crear una rutina primero.\nVe a 'Mi Rutina' para generar tu plan de entrenamiento.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.cardSeguimiento.setOnClickListener {
            (requireActivity() as MainActivity).openFragment(SeguimientoFragment())
        }
    }


    private fun calcularHashPerfil(perfil: com.cibertec.novafit.model.Perfil): String {
        val data = "${perfil.peso}-${perfil.altura}-${perfil.edad}-${perfil.genero}-" +
                "${perfil.objetivo}-${perfil.nivelExperiencia}-${perfil.nivelActividad}-" +
                "${perfil.circunferenciaCuello}-${perfil.circunferenciaCintura}-${perfil.circunferenciaCadera}-" +
                "${perfil.tipoCuerpo}"

        return MessageDigest.getInstance("MD5")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}