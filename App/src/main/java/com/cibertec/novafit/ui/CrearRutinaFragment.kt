package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cibertec.novafit.data.EjercicioRepository
import com.cibertec.novafit.data.PerfilRepository
import com.cibertec.novafit.data.RutinaRepository
import com.cibertec.novafit.databinding.FragmentCrearRutinaBinding
import com.cibertec.novafit.logic.GeneradorRutina
import java.security.MessageDigest

class CrearRutinaFragment : Fragment() {

    private var _binding: FragmentCrearRutinaBinding? = null
    private val binding get() = _binding!!


    private lateinit var ejercicioRepo: EjercicioRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearRutinaBinding.inflate(inflater, container, false)
        ejercicioRepo = EjercicioRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCrearRutina.setOnClickListener { crearRutina() }
    }


    private fun crearRutina() {
        val dias = when (binding.rgDias.checkedRadioButtonId) {
            binding.rb3dias.id -> 3
            binding.rb4dias.id -> 4
            binding.rb5dias.id -> 5
            binding.rb6dias.id -> 6
            binding.rb7dias.id -> 7
            else -> 3
        }

        val lugar = when (binding.rgLugar.checkedRadioButtonId) {
            binding.rbCasaSinEquipamiento.id -> "Casa sin equipamiento"
            binding.rbCasaConEquipamiento.id -> "Casa con equipamiento"
            binding.rbGym.id -> "Gym"
            else -> {
                Toast.makeText(
                    requireContext(),
                    "Selecciona un lugar de entrenamiento",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        val email = (requireActivity() as MainActivity).emailActual
        val perfil = PerfilRepository(requireContext()).getPerfilByEmail(email) ?: run {
            Toast.makeText(
                requireContext(),
                "Error: Perfil no encontrado",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (perfil.objetivo.isNullOrBlank() || perfil.nivelExperiencia.isNullOrBlank()) {
            Toast.makeText(
                requireContext(),
                "Error: Completa tu perfil (objetivo y nivel de experiencia)",
                Toast.LENGTH_LONG
            ).show()
            return
        }


        val validacion = ejercicioRepo.validarParaRutina(lugar, perfil.nivelExperiencia)
        if (!validacion.isValid) {
            Toast.makeText(
                requireContext(),
                "⚠️ ${validacion.message}\n\nNo se puede crear una rutina en este momento.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {

            val rutina = GeneradorRutina.generar(
                requireContext(),  // ⭐ NUEVO
                perfil,
                dias,
                lugar
            )

            val hashPerfil = calcularHashPerfil(perfil)
            RutinaRepository(requireContext()).guardarRutina(rutina, hashPerfil)

            Toast.makeText(
                requireContext(),
                "✅ ¡Rutina creada exitosamente!",
                Toast.LENGTH_SHORT
            ).show()

            (requireActivity() as MainActivity).openFragment(VerRutinaFragment())
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "❌ Error al crear rutina: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun calcularHashPerfil(perfil: com.cibertec.novafit.model.Perfil): String {
        val data = "${perfil.peso}-${perfil.altura}-${perfil.edad}-${perfil.genero}-" +
                "${perfil.objetivo}-${perfil.nivelExperiencia}-${perfil.nivelActividad}"
        return MessageDigest.getInstance("MD5")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}