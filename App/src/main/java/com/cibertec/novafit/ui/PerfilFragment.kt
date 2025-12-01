package com.cibertec.novafit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cibertec.novafit.data.DietaRepository
import com.cibertec.novafit.data.PerfilRepository
import com.cibertec.novafit.data.RutinaRepository
import com.cibertec.novafit.databinding.FragmentPerfilBinding
import com.cibertec.novafit.model.Perfil
import com.cibertec.novafit.R
import java.security.MessageDigest
import java.util.Calendar
import android.util.Log

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var repo: PerfilRepository
    private var perfil: Perfil? = null
    private var enEdicion = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repo = (requireActivity() as MainActivity).repo
        val emailActual = (requireActivity() as MainActivity).emailActual
        perfil = repo.getPerfilByEmail(emailActual)


        val generos = arrayOf("Masculino", "Femenino", "Otro")
        val adapterGenero = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, generos)
        adapterGenero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGenero.adapter = adapterGenero

        val objetivos = resources.getStringArray(R.array.objetivos)
        val adapterObjetivo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, objetivos)
        adapterObjetivo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerObjetivo.adapter = adapterObjetivo


        binding.spinnerGenero.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (enEdicion) {
                    val generoSeleccionado = parent?.getItemAtPosition(position).toString()
                    actualizarVisibilidadCaderaEdicion(generoSeleccionado)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        actualizarVistaLectura()


        binding.btnEditar.setOnClickListener {
            enEdicion = true
            binding.llDatos.visibility = View.GONE
            binding.llEdicion.visibility = View.VISIBLE
            binding.btnEditar.visibility = View.GONE
            binding.btnGuardar.visibility = View.VISIBLE
            cargarDatosEdicion()
        }


        binding.btnGuardar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val genero = binding.spinnerGenero.selectedItem.toString()
            val peso = binding.etPeso.text.toString().toDoubleOrNull() ?: 0.0
            val altura = binding.etAltura.text.toString().toDoubleOrNull() ?: 0.0

            if (altura <= 0) {
                Toast.makeText(requireContext(), "Ingresa una altura válida en cm", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cuello = binding.etCuello.text.toString().toDoubleOrNull()
            val cintura = binding.etCintura.text.toString().toDoubleOrNull()


            val cadera = if (genero == "Femenino" || genero == "Otro") {
                binding.etCadera.text.toString().toDoubleOrNull()
            } else {
                null
            }

            val objetivo = binding.spinnerObjetivo.selectedItem.toString()

            if (nombre.isEmpty() || genero.isEmpty() || peso <= 0 || altura <= 0 || objetivo.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            perfil?.let { perfilOriginal ->
                val edadActual = calcularEdad(perfilOriginal.fechaNacimiento)

                val perfilActualizado = Perfil(
                    id = perfilOriginal.id,
                    email = perfilOriginal.email,
                    password = perfilOriginal.password,
                    nombre = nombre,
                    fechaNacimiento = perfilOriginal.fechaNacimiento,
                    edad = edadActual,
                    genero = genero,
                    peso = peso,
                    altura = altura,
                    circunferenciaCuello = cuello,
                    circunferenciaCintura = cintura,
                    circunferenciaCadera = cadera,
                    nivelActividad = perfilOriginal.nivelActividad,
                    objetivo = objetivo,
                    tipoCuerpo = perfilOriginal.tipoCuerpo,
                    nivelExperiencia = perfilOriginal.nivelExperiencia
                )


                val cambioRelevante =
                    perfilOriginal.peso != peso ||
                            perfilOriginal.altura != altura ||
                            perfilOriginal.edad != edadActual ||
                            perfilOriginal.genero != genero ||
                            perfilOriginal.objetivo != objetivo ||
                            perfilOriginal.circunferenciaCuello != cuello ||
                            perfilOriginal.circunferenciaCintura != cintura ||
                            perfilOriginal.circunferenciaCadera != cadera

                Log.d("PerfilFragment", "¿Cambió algo relevante? $cambioRelevante")

                if (cambioRelevante) {
                    Log.d("PerfilFragment", "Eliminando dieta para: $emailActual")
                    DietaRepository(requireContext()).eliminarDieta(emailActual)

                    val hashNuevo = calcularHashPerfil(perfilActualizado)
                    val hashViejo = calcularHashPerfil(perfilOriginal)

                    if (hashNuevo != hashViejo) {
                        Log.d("PerfilFragment", "Eliminando rutina para: $emailActual")
                        RutinaRepository(requireContext()).eliminarRutina(emailActual)
                    }

                    Toast.makeText(
                        requireContext(),
                        "✅ Perfil actualizado.\n⚠️ Tu dieta y rutina fueron eliminadas. Deberás crearlas nuevamente con tus nuevos datos.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(requireContext(), "✅ Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                }

                repo.guardarPerfil(perfilActualizado)

                enEdicion = false
                this.perfil = repo.getPerfilByEmail(emailActual)
                actualizarVistaLectura()
                binding.llDatos.visibility = View.VISIBLE
                binding.llEdicion.visibility = View.GONE
                binding.btnEditar.visibility = View.VISIBLE
                binding.btnGuardar.visibility = View.GONE
            }
        }


        binding.btnCerrarSesion.setOnClickListener {
            (requireActivity() as MainActivity).volverAOnboarding()
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarVistaLectura() {
        perfil?.let {
            binding.txtEmail.text = it.email
            binding.txtNombre.text = it.nombre
            binding.txtEdad.text = "${it.edad} años"
            binding.txtGenero.text = it.genero
            binding.txtPeso.text = "${it.peso} kg"
            binding.txtAltura.text = "${it.altura} cm"
            binding.txtCuello.text = "${it.circunferenciaCuello?.toString() ?: "N/A"} cm"
            binding.txtCintura.text = "${it.circunferenciaCintura?.toString() ?: "N/A"} cm"
            binding.txtObjetivo.text = it.objetivo


            if (it.genero == "Femenino" || it.genero == "Otro") {
                binding.llCaderaSection.visibility = View.VISIBLE
                binding.txtCadera.text = "${it.circunferenciaCadera?.toString() ?: "N/A"} cm"
            } else {
                binding.llCaderaSection.visibility = View.GONE
            }
        }
    }

    private fun cargarDatosEdicion() {
        perfil?.let {
            binding.etNombre.setText(it.nombre)
            val generoPos = arrayOf("Masculino", "Femenino", "Otro").indexOf(it.genero)
            if (generoPos >= 0) binding.spinnerGenero.setSelection(generoPos)
            binding.etPeso.setText(it.peso.toString())
            binding.etAltura.setText(it.altura.toString())
            binding.etCuello.setText(it.circunferenciaCuello?.toString() ?: "")
            binding.etCintura.setText(it.circunferenciaCintura?.toString() ?: "")
            binding.etCadera.setText(it.circunferenciaCadera?.toString() ?: "")
            val objetivoPos = resources.getStringArray(R.array.objetivos).indexOf(it.objetivo)
            if (objetivoPos >= 0) binding.spinnerObjetivo.setSelection(objetivoPos)


            actualizarVisibilidadCaderaEdicion(it.genero)
        }
    }


    private fun actualizarVisibilidadCaderaEdicion(genero: String) {
        if (genero == "Femenino" || genero == "Otro") {
            binding.llCaderaContainer.visibility = View.VISIBLE
        } else {

            binding.llCaderaContainer.visibility = View.GONE
            binding.etCadera.setText("")
        }
    }

    private fun calcularEdad(fechaMillis: Long): Int {
        val nacimiento = Calendar.getInstance().apply { timeInMillis = fechaMillis }
        val hoy = Calendar.getInstance()
        var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)
        if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) edad--
        return edad
    }

    private fun calcularHashPerfil(perfil: Perfil): String {
        val data = "${perfil.peso}-${perfil.altura}-${perfil.edad}-${perfil.genero}-" +
                "${perfil.objetivo}-${perfil.nivelExperiencia}-${perfil.nivelActividad}-" +
                "${perfil.circunferenciaCuello}-${perfil.circunferenciaCintura}-${perfil.circunferenciaCadera}"
        return MessageDigest.getInstance("MD5")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}