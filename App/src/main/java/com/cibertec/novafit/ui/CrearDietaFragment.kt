package com.cibertec.novafit.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cibertec.novafit.data.AlimentoRepository
import com.cibertec.novafit.data.DietaRepository
import com.cibertec.novafit.data.PerfilRepository
import com.cibertec.novafit.databinding.FragmentCrearDietaBinding
import com.cibertec.novafit.model.Dieta
import com.cibertec.novafit.logic.GeneradorDieta

class CrearDietaFragment : Fragment() {

    private var _binding: FragmentCrearDietaBinding? = null
    private val binding get() = _binding!!

    private lateinit var alimentoRepo: AlimentoRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearDietaBinding.inflate(inflater, container, false)
        alimentoRepo = AlimentoRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        validarYCargarAlimentos()

        binding.btnCrearDieta.setOnClickListener { crearDieta() }
    }


    private fun validarYCargarAlimentos() {
        val proteinas = alimentoRepo.obtenerProteinas()
        val grasas = alimentoRepo.obtenerGrasas()
        val carbohidratos = alimentoRepo.obtenerCarbohidratos()


        Log.d("CrearDieta", "Proteínas: ${proteinas.size}, Grasas: ${grasas.size}, Carbos: ${carbohidratos.size}")


        if (proteinas.size < 2 || grasas.size < 2 || carbohidratos.size < 2) {
            Toast.makeText(
                requireContext(),
                "⚠️ No hay suficientes alimentos en la base de datos.\n" +
                        "Se necesitan al menos 2 por categoría.\n\n" +
                        "Proteínas: ${proteinas.size}\n" +
                        "Grasas: ${grasas.size}\n" +
                        "Carbohidratos: ${carbohidratos.size}",
                Toast.LENGTH_LONG
            ).show()

            binding.btnCrearDieta.isEnabled = false
            return
        }


        setupAlimentos(proteinas, grasas, carbohidratos)
        binding.btnCrearDieta.isEnabled = true

        Toast.makeText(
            requireContext(),
            "✅ ${proteinas.size + grasas.size + carbohidratos.size} alimentos cargados correctamente",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun setupAlimentos(
        proteinas: List<com.cibertec.novafit.model.Alimento>,
        grasas: List<com.cibertec.novafit.model.Alimento>,
        carbohidratos: List<com.cibertec.novafit.model.Alimento>
    ) {
        binding.llProteinas.removeAllViews()
        binding.llGrasas.removeAllViews()
        binding.llCarbohidratos.removeAllViews()

        proteinas.forEach { alimento ->
            addCheckBox(binding.llProteinas, alimento.id, alimento.nombre)
        }

        grasas.forEach { alimento ->
            addCheckBox(binding.llGrasas, alimento.id, alimento.nombre)
        }

        carbohidratos.forEach { alimento ->
            addCheckBox(binding.llCarbohidratos, alimento.id, alimento.nombre)
        }
    }

    private fun addCheckBox(container: android.widget.LinearLayout, id: Int, text: String) {
        val cb = CheckBox(requireContext()).apply {
            this.id = id
            this.text = text
            this.textSize = 16f
            this.setPadding(8, 12, 8, 12)
        }
        container.addView(cb)
    }

    private fun crearDieta() {
        val comidas = when (binding.rgComidas.checkedRadioButtonId) {
            binding.rb2.id -> 2
            binding.rb3.id -> 3
            binding.rb4.id -> 4
            binding.rb5.id -> 5
            binding.rb6.id -> 6
            else -> 3
        }

        val pIds = getCheckedIds(binding.llProteinas)
        val gIds = getCheckedIds(binding.llGrasas)
        val cIds = getCheckedIds(binding.llCarbohidratos)

        // ✅ Validación
        if (pIds.isEmpty() || gIds.isEmpty() || cIds.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Selecciona al menos un alimento por categoría",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (pIds.size < 2 || gIds.size < 2 || cIds.size < 2) {
            Toast.makeText(
                requireContext(),
                "⚠️ Selecciona al menos 2 alimentos por categoría para mayor variedad",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val email = (requireActivity() as MainActivity).emailActual
        val perfil = PerfilRepository(requireContext()).getPerfilByEmail(email) ?: run {
            Toast.makeText(requireContext(), "Error: Perfil no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        try {

            val macrosDiarios = GeneradorDieta.calcularMacrosExactos(perfil)


            val plan = GeneradorDieta.crear(
                requireContext(),
                macrosDiarios,
                comidas,
                pIds,
                gIds,
                cIds
            )

            val dieta = Dieta(
                email = email,
                numeroComidas = comidas,
                proteinasIds = pIds,
                grasasIds = gIds,
                carbohidratosIds = cIds,
                planSemanal = plan,
                macros = macrosDiarios,
                fechaCreacion = System.currentTimeMillis()
            )

            DietaRepository(requireContext()).guardarDieta(dieta)

            Toast.makeText(
                requireContext(),
                "✅ ¡Dieta creada exitosamente!",
                Toast.LENGTH_SHORT
            ).show()

            (requireActivity() as MainActivity).openFragment(VerDietaFragment())
        } catch (e: Exception) {
            Log.e("CrearDieta", "Error: ${e.message}", e)
            Toast.makeText(
                requireContext(),
                "❌ Error al crear dieta: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun getCheckedIds(container: android.widget.LinearLayout): List<Int> {
        return (0 until container.childCount).mapNotNull {
            val cb = container.getChildAt(it) as? CheckBox
            if (cb?.isChecked == true) cb.id else null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}