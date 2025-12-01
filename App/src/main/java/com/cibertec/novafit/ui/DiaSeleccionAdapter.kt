package com.cibertec.novafit.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.novafit.databinding.ItemDiaSeleccionBinding
import com.cibertec.novafit.model.Rutina

class DiaSeleccionAdapter(
    private val dias: List<Rutina.DiaSesion>,
    private val onDiaClick: (Int) -> Unit
) : RecyclerView.Adapter<DiaSeleccionAdapter.DiaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaViewHolder {
        val binding = ItemDiaSeleccionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DiaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiaViewHolder, position: Int) {
        holder.bind(dias[position], position)
    }

    override fun getItemCount() = dias.size

    inner class DiaViewHolder(private val binding: ItemDiaSeleccionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dia: Rutina.DiaSesion, index: Int) {
            binding.txtNombreDia.text = dia.nombre
            binding.txtGrupoMuscular.text = dia.grupoMuscular
            binding.txtCantidadEjercicios.text = "${dia.ejercicios.size} ejercicios"

            binding.root.setOnClickListener {
                onDiaClick(index)
            }
        }
    }
}