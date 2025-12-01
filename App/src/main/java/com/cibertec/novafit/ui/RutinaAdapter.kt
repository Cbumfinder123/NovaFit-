package com.cibertec.novafit.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.novafit.databinding.ItemEjercicioBinding
import com.cibertec.novafit.databinding.ItemSesionRutinaBinding
import com.cibertec.novafit.model.Rutina

class RutinaAdapter : ListAdapter<Rutina.DiaSesion, RutinaAdapter.SesionViewHolder>(SesionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SesionViewHolder {
        val binding = ItemSesionRutinaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SesionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SesionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SesionViewHolder(private val binding: ItemSesionRutinaBinding) : RecyclerView.ViewHolder(binding.root) {
        private val ejerciciosAdapter = EjerciciosAdapter()

        init {
            binding.rvEjercicios.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvEjercicios.adapter = ejerciciosAdapter
        }

        fun bind(sesion: Rutina.DiaSesion) {
            binding.txtNombreDia.text = sesion.nombre
            binding.txtGrupoMuscular.text = sesion.grupoMuscular
            ejerciciosAdapter.submitList(sesion.ejercicios)
        }
    }
}

class EjerciciosAdapter : ListAdapter<Rutina.Ejercicio, EjerciciosAdapter.EjercicioViewHolder>(EjercicioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val binding = ItemEjercicioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EjercicioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EjercicioViewHolder(private val binding: ItemEjercicioBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ejercicio: Rutina.Ejercicio) {
            binding.txtNombreEjercicio.text = ejercicio.nombre
            binding.txtSeries.text = "${ejercicio.series} series"
            binding.txtRepeticiones.text = "${ejercicio.repeticiones} reps"
            binding.txtDescanso.text = "Descanso: ${ejercicio.descanso}"

            if (ejercicio.notas != null) {
                binding.txtNotas.text = "💡 ${ejercicio.notas}"
                binding.txtNotas.visibility = android.view.View.VISIBLE
            } else {
                binding.txtNotas.visibility = android.view.View.GONE
            }
        }
    }
}

class SesionDiffCallback : DiffUtil.ItemCallback<Rutina.DiaSesion>() {
    override fun areItemsTheSame(oldItem: Rutina.DiaSesion, newItem: Rutina.DiaSesion) = oldItem.nombre == newItem.nombre
    override fun areContentsTheSame(oldItem: Rutina.DiaSesion, newItem: Rutina.DiaSesion) = oldItem == newItem
}

class EjercicioDiffCallback : DiffUtil.ItemCallback<Rutina.Ejercicio>() {
    override fun areItemsTheSame(oldItem: Rutina.Ejercicio, newItem: Rutina.Ejercicio) = oldItem.nombre == newItem.nombre
    override fun areContentsTheSame(oldItem: Rutina.Ejercicio, newItem: Rutina.Ejercicio) = oldItem == newItem
}