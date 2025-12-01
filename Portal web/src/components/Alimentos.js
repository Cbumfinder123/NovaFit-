import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { db } from '../firebase/config';
import { 
  collection, 
  getDocs, 
  addDoc, 
  updateDoc, 
  doc
} from 'firebase/firestore';
import './Alimentos.css';

function Alimentos() {
  const [alimentos, setAlimentos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [modoEdicion, setModoEdicion] = useState(false);
  const [alimentoActual, setAlimentoActual] = useState(null);
  const [advertencias, setAdvertencias] = useState([]);
  
  const [formData, setFormData] = useState({
    nombre: '',
    categoria: 'proteina',
    proteinas: '',
    grasas: '',
    carbohidratos: '',
    calorias: ''
  });

  useEffect(() => {
    cargarAlimentos();
  }, []);

  const cargarAlimentos = async () => {
    try {
      setLoading(true);
      const querySnapshot = await getDocs(collection(db, 'alimentos'));
      const alimentosData = querySnapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      })).filter(a => a.activo);
      
      console.log('📦 Alimentos cargados:', alimentosData.length);
      setAlimentos(alimentosData);
      setLoading(false);
    } catch (error) {
      console.error('Error cargando alimentos:', error);
      setLoading(false);
    }
  };

  const abrirModalNuevo = () => {
    setModoEdicion(false);
    setAlimentoActual(null);
    setFormData({
      nombre: '',
      categoria: 'proteina',
      proteinas: '',
      grasas: '',
      carbohidratos: '',
      calorias: ''
    });
    setAdvertencias([]);
    setModalAbierto(true);
  };

  const abrirModalEditar = (alimento) => {
    setModoEdicion(true);
    setAlimentoActual(alimento);
    setFormData({
      nombre: alimento.nombre,
      categoria: alimento.categoria,
      proteinas: alimento.proteinas.toString(),
      grasas: alimento.grasas.toString(),
      carbohidratos: alimento.carbohidratos.toString(),
      calorias: alimento.calorias.toString()
    });
    setAdvertencias([]);
    setModalAbierto(true);
  };

  const cerrarModal = () => {
    setModalAbierto(false);
    setModoEdicion(false);
    setAlimentoActual(null);
    setAdvertencias([]);
  };

  const handleChange = (e) => {
    const newFormData = {
      ...formData,
      [e.target.name]: e.target.value
    };
    setFormData(newFormData);
    
   
    validarAlimento(newFormData, false);
  };

  
  const validarAlimento = (datos, mostrarErrores = true) => {
    const proteinas = parseFloat(datos.proteinas) || 0;
    const grasas = parseFloat(datos.grasas) || 0;
    const carbohidratos = parseFloat(datos.carbohidratos) || 0;
    const calorias = parseFloat(datos.calorias) || 0;

    const advertenciasNuevas = [];
    let tieneErrores = false;

    
    if (proteinas === 0 && grasas === 0 && carbohidratos === 0) {
      advertenciasNuevas.push({
        tipo: 'error',
        mensaje: '❌ El alimento debe tener al menos UN macro (proteínas, grasas o carbohidratos)'
      });
      tieneErrores = true;
    }

   
    const totalMacros = proteinas + grasas + carbohidratos;
    if (totalMacros > 100) {
      advertenciasNuevas.push({
        tipo: 'error',
        mensaje: `❌ IMPOSIBLE: La suma de macros (${totalMacros.toFixed(1)}g) supera 100g. Los valores son por cada 100g de alimento.`
      });
      tieneErrores = true;
    }

   
    const caloriasCalculadas = (proteinas * 4) + (grasas * 9) + (carbohidratos * 4);
    const diferenciaCalorias = Math.abs(calorias - caloriasCalculadas);
    
    if (calorias > 0 && diferenciaCalorias > 50) {
      advertenciasNuevas.push({
        tipo: 'warning',
        mensaje: `⚠️ Las calorías ingresadas (${calorias}) no coinciden con el cálculo (${caloriasCalculadas.toFixed(0)}). Diferencia: ${diferenciaCalorias.toFixed(0)} kcal`
      });
    }

   
    const categoria = datos.categoria;
    
    if (categoria === 'proteina') {
      const porcentajeProteina = (proteinas / totalMacros) * 100;
      
      if (proteinas < 10) {
        advertenciasNuevas.push({
          tipo: 'error',
          mensaje: `❌ Una PROTEÍNA debe tener mínimo 10g de proteína por 100g. Actual: ${proteinas}g`
        });
        tieneErrores = true;
      } else if (porcentajeProteina < 50) {
        advertenciasNuevas.push({
          tipo: 'warning',
          mensaje: `⚠️ Este alimento tiene solo ${porcentajeProteina.toFixed(0)}% de proteína. Puede afectar la precisión de las dietas (80-95% en vez de 90-95%).`
        });
      }

      if (carbohidratos > 20) {
        advertenciasNuevas.push({
          tipo: 'warning',
          mensaje: `⚠️ Alto contenido de carbohidratos (${carbohidratos}g) para una proteína. Puede causar variaciones en las dietas.`
        });
      }
    }

    if (categoria === 'grasa') {
      const porcentajeGrasa = (grasas / totalMacros) * 100;
      
      if (grasas < 5) {
        advertenciasNuevas.push({
          tipo: 'error',
          mensaje: `❌ Una GRASA debe tener mínimo 5g de grasa por 100g. Actual: ${grasas}g`
        });
        tieneErrores = true;
      } else if (porcentajeGrasa < 50) {
        advertenciasNuevas.push({
          tipo: 'warning',
          mensaje: `⚠️ Este alimento tiene solo ${porcentajeGrasa.toFixed(0)}% de grasa. Puede afectar la precisión de las dietas (80-95% en vez de 90-95%).`
        });
      }

      if (carbohidratos > 25) {
        advertenciasNuevas.push({
          tipo: 'warning',
          mensaje: `⚠️ Alto contenido de carbohidratos (${carbohidratos}g) para una grasa. El algoritmo compensará pero reducirá precisión.`
        });
      }
    }

    if (categoria === 'carbohidrato') {
      const porcentajeCarbohidratos = (carbohidratos / totalMacros) * 100;
      
      if (carbohidratos < 15) {
        advertenciasNuevas.push({
          tipo: 'error',
          mensaje: `❌ Un CARBOHIDRATO debe tener mínimo 15g de carbohidratos por 100g. Actual: ${carbohidratos}g`
        });
        tieneErrores = true;
      } else if (porcentajeCarbohidratos < 50) {
        advertenciasNuevas.push({
          tipo: 'warning',
          mensaje: `⚠️ Este alimento tiene solo ${porcentajeCarbohidratos.toFixed(0)}% de carbohidratos. Puede afectar la precisión de las dietas.`
        });
      }

      if (grasas > 20) {
        advertenciasNuevas.push({
          tipo: 'warning',
          mensaje: `⚠️ Alto contenido de grasa (${grasas}g) para un carbohidrato. El algoritmo compensará pero reducirá precisión.`
        });
      }
    }

    
    if (!tieneErrores) {
      const pureza = categoria === 'proteina' ? (proteinas / totalMacros) * 100 :
                     categoria === 'grasa' ? (grasas / totalMacros) * 100 :
                     (carbohidratos / totalMacros) * 100;

      if (pureza >= 80) {
        advertenciasNuevas.push({
          tipo: 'success',
          mensaje: `✅ EXCELENTE: Alimento muy puro (${pureza.toFixed(0)}% macro principal). Precisión esperada: 95-98%`
        });
      } else if (pureza >= 60) {
        advertenciasNuevas.push({
          tipo: 'info',
          mensaje: `✓ BUENO: Pureza ${pureza.toFixed(0)}%. Precisión esperada: 88-95%`
        });
      } else if (pureza >= 40) {
        advertenciasNuevas.push({
          tipo: 'info',
          mensaje: `ℹ️ ACEPTABLE: Pureza ${pureza.toFixed(0)}%. Precisión esperada: 80-90%`
        });
      }
    }

    setAdvertencias(advertenciasNuevas);
    return !tieneErrores;
  };

  const guardarAlimento = async (e) => {
    e.preventDefault();

    if (!formData.nombre.trim()) {
      alert('⚠️ El nombre es obligatorio');
      return;
    }

  
    const esValido = validarAlimento(formData, true);
    
  
    const tieneErrores = advertencias.some(a => a.tipo === 'error');
    if (tieneErrores) {
      alert('❌ No se puede guardar: Hay errores críticos en los datos. Por favor corrígelos.');
      return;
    }

  
    const tieneWarnings = advertencias.some(a => a.tipo === 'warning');
    if (tieneWarnings && !window.confirm(
      '⚠️ Este alimento tiene advertencias que pueden reducir la precisión de las dietas.\n\n' +
      advertencias.filter(a => a.tipo === 'warning').map(a => a.mensaje).join('\n\n') +
      '\n\n¿Deseas continuar de todos modos?'
    )) {
      return;
    }

    const proteinas = parseFloat(formData.proteinas) || 0;
    const grasas = parseFloat(formData.grasas) || 0;
    const carbohidratos = parseFloat(formData.carbohidratos) || 0;
    const calorias = parseFloat(formData.calorias) || 0;

    console.log('📝 Guardando alimento:', {
      nombre: formData.nombre.trim(),
      categoria: formData.categoria,
      proteinas,
      grasas,
      carbohidratos,
      calorias
    });

    try {
      if (modoEdicion && alimentoActual) {
        const alimentoDataEditar = {
          nombre: formData.nombre.trim(),
          categoria: formData.categoria,
          proteinas,
          grasas,
          carbohidratos,
          calorias
        };

        await updateDoc(doc(db, 'alimentos', alimentoActual.id), alimentoDataEditar);
        console.log('✅ Alimento actualizado');
        alert('✅ Alimento actualizado correctamente');
      } else {
        const alimentoDataNuevo = {
          nombre: formData.nombre.trim(),
          categoria: formData.categoria,
          proteinas,
          grasas,
          carbohidratos,
          calorias,
          activo: true,
          fechaCreacion: new Date()
        };

        console.log('➕ Creando alimento:', alimentoDataNuevo);
        const docRef = await addDoc(collection(db, 'alimentos'), alimentoDataNuevo);
        console.log('✅ Alimento creado con ID:', docRef.id);
        
        alert('✅ Alimento creado correctamente');
      }
      
      cerrarModal();
      await cargarAlimentos();
      
    } catch (error) {
      console.error('❌ Error completo:', error);
      console.error('❌ Mensaje:', error.message);
      console.error('❌ Code:', error.code);
      alert(`❌ Error: ${error.message}`);
    }
  };

  const eliminarAlimento = async (id) => {
    if (window.confirm('¿Estás seguro de eliminar este alimento?')) {
      try {
        await updateDoc(doc(db, 'alimentos', id), { activo: false });
        alert('✅ Alimento eliminado correctamente');
        cargarAlimentos();
      } catch (error) {
        console.error('❌ Error eliminando alimento:', error);
        alert('❌ Error al eliminar alimento');
      }
    }
  };

  const getCategoriaEmoji = (categoria) => {
    switch(categoria) {
      case 'proteina': return '🍗';
      case 'grasa': return '🥑';
      case 'carbohidrato': return '🍚';
      default: return '🍽️';
    }
  };

  const getCategoriaNombre = (categoria) => {
    switch(categoria) {
      case 'proteina': return 'Proteínas';
      case 'grasa': return 'Grasas';
      case 'carbohidrato': return 'Carbohidratos';
      default: return categoria;
    }
  };

  return (
    <div className="dashboard">
      <div className="sidebar">
        <h1>🔥 NovaFit Admin</h1>
        <nav>
          <Link to="/" className="nav-link">📊 Dashboard</Link>
          <Link to="/usuarios" className="nav-link">👥 Usuarios</Link>
          <Link to="/alimentos" className="nav-link active">🍗 Alimentos</Link>
          <Link to="/ejercicios" className="nav-link">💪 Ejercicios</Link>
          <Link to="/reportes" className="nav-link">📈 Reportes</Link>
        </nav>
      </div>

      <div className="content">
        <div className="header">
          <h1>🍗 Gestión de Alimentos</h1>
          <div className="header-actions">
            <button onClick={cargarAlimentos} className="btn-refresh">🔄 Actualizar</button>
            <button onClick={abrirModalNuevo} className="btn-add">➕ Nuevo Alimento</button>
          </div>
        </div>

        {loading ? (
          <div className="loading">Cargando alimentos...</div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Categoría</th>
                  <th>Proteínas (g)</th>
                  <th>Grasas (g)</th>
                  <th>Carbohidratos (g)</th>
                  <th>Calorías</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {alimentos.map(alimento => (
                  <tr key={alimento.id}>
                    <td>
                      {getCategoriaEmoji(alimento.categoria)} {alimento.nombre}
                    </td>
                    <td>
                      <span className={`badge badge-${alimento.categoria}`}>
                        {getCategoriaNombre(alimento.categoria)}
                      </span>
                    </td>
                    <td>{alimento.proteinas}g</td>
                    <td>{alimento.grasas}g</td>
                    <td>{alimento.carbohidratos}g</td>
                    <td>{alimento.calorias} kcal</td>
                    <td>
                      <button 
                        className="btn-edit"
                        onClick={() => abrirModalEditar(alimento)}
                      >
                        ✏️ Editar
                      </button>
                      <button 
                        className="btn-delete"
                        onClick={() => eliminarAlimento(alimento.id)}
                      >
                        🗑️ Eliminar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {modalAbierto && (
          <div className="modal-overlay" onClick={cerrarModal}>
            <div className="modal-content modal-large" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>{modoEdicion ? '✏️ Editar Alimento' : '➕ Nuevo Alimento'}</h2>
                <button className="btn-close" onClick={cerrarModal}>✖️</button>
              </div>
              <form onSubmit={guardarAlimento}>
                <div className="modal-body">
                  <div className="form-group">
                    <label>Nombre del alimento *</label>
                    <input
                      type="text"
                      name="nombre"
                      value={formData.nombre}
                      onChange={handleChange}
                      placeholder="Ej: Pechuga de pollo"
                      required
                      disabled={modoEdicion} 
                    />
                  </div>

                  <div className="form-group">
                    <label>Categoría *</label>
                    <select
                      name="categoria"
                      value={formData.categoria}
                      onChange={handleChange}
                      required
                    >
                      <option value="proteina">🍗 Proteínas</option>
                      <option value="grasa">🥑 Grasas</option>
                      <option value="carbohidrato">🍚 Carbohidratos</option>
                    </select>
                  </div>

                  <div className="info-box">
                    <p><strong>ℹ️ Los valores son por cada 100g de alimento</strong></p>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>Proteínas (g) *</label>
                      <input
                        type="number"
                        step="0.1"
                        name="proteinas"
                        value={formData.proteinas}
                        onChange={handleChange}
                        placeholder="0"
                        min="0"
                        max="100"
                      />
                    </div>

                    <div className="form-group">
                      <label>Grasas (g) *</label>
                      <input
                        type="number"
                        step="0.1"
                        name="grasas"
                        value={formData.grasas}
                        onChange={handleChange}
                        placeholder="0"
                        min="0"
                        max="100"
                      />
                    </div>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>Carbohidratos (g) *</label>
                      <input
                        type="number"
                        step="0.1"
                        name="carbohidratos"
                        value={formData.carbohidratos}
                        onChange={handleChange}
                        placeholder="0"
                        min="0"
                        max="100"
                      />
                    </div>

                    <div className="form-group">
                      <label>Calorías (kcal) *</label>
                      <input
                        type="number"
                        step="0.1"
                        name="calorias"
                        value={formData.calorias}
                        onChange={handleChange}
                        placeholder="0"
                        min="0"
                        max="900"
                      />
                    </div>
                  </div>

                  {/* ✅ PANEL DE ADVERTENCIAS */}
                  {advertencias.length > 0 && (
                    <div className="advertencias-panel">
                      {advertencias.map((adv, index) => (
                        <div key={index} className={`advertencia advertencia-${adv.tipo}`}>
                          {adv.mensaje}
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                <div className="modal-footer">
                  <button type="button" className="btn-cancel" onClick={cerrarModal}>
                    Cancelar
                  </button>
                  <button type="submit" className="btn-save">
                    {modoEdicion ? '💾 Actualizar' : '➕ Crear'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Alimentos;