import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { db, storage } from '../firebase/config';
import { collection, getDocs, addDoc, updateDoc, doc, serverTimestamp } from 'firebase/firestore';
import { ref, uploadBytesResumable, getDownloadURL } from 'firebase/storage';
import './Ejercicios.css';

function Ejercicios() {
  const [ejercicios, setEjercicios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [modoEdicion, setModoEdicion] = useState(false);
  const [ejercicioActual, setEjercicioActual] = useState(null);
  const [filtroEquipamiento, setFiltroEquipamiento] = useState('todos');
  const [busqueda, setBusqueda] = useState(''); // ✅ NUEVO
  

  const [imagenFile, setImagenFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [uploadingImage, setUploadingImage] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  

  const [formData, setFormData] = useState({
    nombre: '',
    grupoMuscular: '',
    dificultad: 'Intermedio',
    equipamiento: 'ninguno'
  });

  useEffect(() => {
    cargarEjercicios();
  }, []);

  const cargarEjercicios = async () => {
    try {
      setLoading(true);
      const querySnapshot = await getDocs(collection(db, 'ejercicios'));
      const ejerciciosData = querySnapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      })).filter(e => e.activo);
      setEjercicios(ejerciciosData);
      setLoading(false);
    } catch (error) {
      console.error('Error cargando ejercicios:', error);
      setLoading(false);
    }
  };

  const abrirModalNuevo = () => {
    setModoEdicion(false);
    setEjercicioActual(null);
    setImagenFile(null);
    setImagePreview(null);
    setFormData({
      nombre: '',
      grupoMuscular: '',
      dificultad: 'Intermedio',
      equipamiento: 'ninguno'
    });
    setModalAbierto(true);
  };

  const abrirModalEditar = (ejercicio) => {
    setModoEdicion(true);
    setEjercicioActual(ejercicio);
    setImagenFile(null);
    setImagePreview(ejercicio.imagenUrl || null);
    setFormData({
      nombre: ejercicio.nombre,
      grupoMuscular: ejercicio.grupoMuscular,
      dificultad: ejercicio.dificultad,
      equipamiento: ejercicio.equipamiento
    });
    setModalAbierto(true);
  };

  const cerrarModal = () => {
    setModalAbierto(false);
    setModoEdicion(false);
    setEjercicioActual(null);
    setImagenFile(null);
    setImagePreview(null);
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) {
        alert('⚠️ La imagen no debe superar 2MB');
        return;
      }
      setImagenFile(file);
      setImagePreview(URL.createObjectURL(file));
    }
  };

  const subirImagen = async () => {
    if (!imagenFile) return null;

    return new Promise((resolve, reject) => {
      const storageRef = ref(storage, `ejercicios/${Date.now()}_${imagenFile.name}`);
      const uploadTask = uploadBytesResumable(storageRef, imagenFile);

      uploadTask.on(
        'state_changed',
        (snapshot) => {
          const progress = (snapshot.bytesTransferred / snapshot.totalBytes) * 100;
          setUploadProgress(progress);
        },
        (error) => {
          console.error('Error subiendo imagen:', error);
          reject(error);
        },
        async () => {
          const downloadURL = await getDownloadURL(uploadTask.snapshot.ref);
          resolve(downloadURL);
        }
      );
    });
  };

  const guardarEjercicio = async (e) => {
    e.preventDefault();

    if (!formData.nombre.trim() || !formData.grupoMuscular.trim()) {
      alert('⚠️ Nombre y grupo muscular son obligatorios');
      return;
    }

    try {
      setUploadingImage(true);

      let imagenUrl = modoEdicion && ejercicioActual ? ejercicioActual.imagenUrl : '';
      
      if (imagenFile) {
        imagenUrl = await subirImagen();
      }

      if (modoEdicion && ejercicioActual) {
        const ejercicioDataEditar = {
          nombre: formData.nombre.trim(),
          grupoMuscular: formData.grupoMuscular.trim(),
          dificultad: formData.dificultad,
          equipamiento: formData.equipamiento,
          tieneImagen: !!imagenUrl,
          imagenUrl: imagenUrl || ''
        };

        await updateDoc(doc(db, 'ejercicios', ejercicioActual.id), ejercicioDataEditar);
        alert('✅ Ejercicio actualizado correctamente');
      } else {
        const ejercicioDataNuevo = {
          nombre: formData.nombre.trim(),
          grupoMuscular: formData.grupoMuscular.trim(),
          dificultad: formData.dificultad,
          equipamiento: formData.equipamiento,
          descripcion: '',
          tieneImagen: !!imagenUrl,
          imagenUrl: imagenUrl || '',
          activo: true,
          fechaCreacion: serverTimestamp()
        };

        await addDoc(collection(db, 'ejercicios'), ejercicioDataNuevo);
        alert('✅ Ejercicio creado correctamente');
      }

      cerrarModal();
      cargarEjercicios();
      setUploadingImage(false);
      setUploadProgress(0);
    } catch (error) {
      console.error('Error guardando ejercicio:', error);
      alert('❌ Error al guardar ejercicio');
      setUploadingImage(false);
    }
  };

  const eliminarEjercicio = async (id) => {
    if (window.confirm('¿Estás seguro de eliminar este ejercicio?')) {
      try {
        await updateDoc(doc(db, 'ejercicios', id), { activo: false });
        alert('✅ Ejercicio eliminado correctamente');
        cargarEjercicios();
      } catch (error) {
        console.error('Error eliminando ejercicio:', error);
        alert('❌ Error al eliminar ejercicio');
      }
    }
  };

  const getEquipamientoEmoji = (equipamiento) => {
    switch(equipamiento) {
      case 'ninguno': return '🏠';
      case 'basico': return '🎽';
      case 'gym': return '🏋️';
      default: return '💪';
    }
  };

  const getDificultadColor = (dificultad) => {
    switch(dificultad) {
      case 'Principiante': return 'badge-principiante';
      case 'Intermedio': return 'badge-intermedio';
      case 'Avanzado': return 'badge-avanzado';
      default: return '';
    }
  };


  const ejerciciosFiltrados = ejercicios.filter(ejercicio => {
    const cumpleEquipamiento = filtroEquipamiento === 'todos' || ejercicio.equipamiento === filtroEquipamiento;
    const cumpleBusqueda = 
      ejercicio.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
      ejercicio.grupoMuscular.toLowerCase().includes(busqueda.toLowerCase());
    
    return cumpleEquipamiento && cumpleBusqueda;
  });

  return (
    <div className="dashboard">
      <div className="sidebar">
        <h1>🔥 NovaFit Admin</h1>
        <nav>
          <Link to="/" className="nav-link">📊 Dashboard</Link>
          <Link to="/usuarios" className="nav-link">👥 Usuarios</Link>
          <Link to="/alimentos" className="nav-link">🍗 Alimentos</Link>
          <Link to="/ejercicios" className="nav-link active">💪 Ejercicios</Link>
          <Link to="/reportes" className="nav-link">📈 Reportes</Link>
        </nav>
      </div>

      <div className="content">
        <div className="header">
          <h1>💪 Gestión de Ejercicios</h1>
          <div className="header-actions">
            <button onClick={cargarEjercicios} className="btn-refresh">🔄 Actualizar</button>
            <button onClick={abrirModalNuevo} className="btn-add">➕ Nuevo Ejercicio</button>
          </div>
        </div>

        {/* ✅ FILTROS MEJORADOS */}
        <div className="filters">
          {/* ✅ NUEVO: Buscador */}
          <div className="search-box">
            <input
              type="text"
              placeholder="🔍 Buscar por nombre o grupo muscular..."
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
              className="search-input"
            />
          </div>

          {/* Filtro de equipamiento */}
          <div className="filter-group">
            <label>Equipamiento:</label>
            <select 
              value={filtroEquipamiento} 
              onChange={(e) => setFiltroEquipamiento(e.target.value)}
              className="filter-select"
            >
              <option value="todos">🌐 Todos</option>
              <option value="ninguno">🏠 Casa sin equipamiento</option>
              <option value="basico">🎽 Casa con equipamiento</option>
              <option value="gym">🏋️ Gym</option>
            </select>
          </div>

          <span className="result-count">
            {ejerciciosFiltrados.length} ejercicio(s) encontrado(s)
          </span>
        </div>

        {loading ? (
          <div className="loading">Cargando ejercicios...</div>
        ) : ejerciciosFiltrados.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🔍</div>
            <h3>No se encontraron ejercicios</h3>
            <p>Intenta con otros filtros de búsqueda</p>
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Grupo Muscular</th>
                  <th>Dificultad</th>
                  <th>Equipamiento</th>
                  <th>Imagen</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {ejerciciosFiltrados.map(ejercicio => (
                  <tr key={ejercicio.id}>
                    <td>{ejercicio.nombre}</td>
                    <td>
                      <span className="badge badge-muscular">
                        {ejercicio.grupoMuscular}
                      </span>
                    </td>
                    <td>
                      <span className={`badge ${getDificultadColor(ejercicio.dificultad)}`}>
                        {ejercicio.dificultad}
                      </span>
                    </td>
                    <td>
                      {getEquipamientoEmoji(ejercicio.equipamiento)} {ejercicio.equipamiento}
                    </td>
                    <td>
                      {ejercicio.tieneImagen && ejercicio.imagenUrl ? (
                        <span className="badge badge-success">✅ Sí</span>
                      ) : (
                        <span className="badge badge-warning">⚠️ Placeholder</span>
                      )}
                    </td>
                    <td>
                      <button 
                        className="btn-edit"
                        onClick={() => abrirModalEditar(ejercicio)}
                      >
                        ✏️ Editar
                      </button>
                      <button 
                        className="btn-delete"
                        onClick={() => eliminarEjercicio(ejercicio.id)}
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

        {/* Modal (sin cambios) */}
        {modalAbierto && (
          <div className="modal-overlay" onClick={cerrarModal}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>{modoEdicion ? '✏️ Editar' : '➕ Nuevo'} Ejercicio</h2>
                <button className="btn-close" onClick={cerrarModal}>✖️</button>
              </div>
              <form onSubmit={guardarEjercicio}>
                <div className="modal-body">
                  <div className="form-group">
                    <label>Nombre del ejercicio *</label>
                    <input
                      type="text"
                      name="nombre"
                      value={formData.nombre}
                      onChange={handleChange}
                      placeholder="Ej: Press de banca"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Grupo muscular *</label>
                    <select
                      name="grupoMuscular"
                      value={formData.grupoMuscular}
                      onChange={handleChange}
                      required
                    >
                      <option value="">Seleccionar...</option>
                      <option value="Pecho">Pecho</option>
                      <option value="Espalda">Espalda</option>
                      <option value="Piernas">Piernas</option>
                      <option value="Hombros">Hombros</option>
                      <option value="Bíceps">Bíceps</option>
                      <option value="Tríceps">Tríceps</option>
                      <option value="Core">Core</option>
                    </select>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>Dificultad *</label>
                      <select
                        name="dificultad"
                        value={formData.dificultad}
                        onChange={handleChange}
                        required
                      >
                        <option value="Principiante">Principiante</option>
                        <option value="Intermedio">Intermedio</option>
                        <option value="Avanzado">Avanzado</option>
                      </select>
                    </div>

                    <div className="form-group">
                      <label>Equipamiento *</label>
                      <select
                        name="equipamiento"
                        value={formData.equipamiento}
                        onChange={handleChange}
                        required
                      >
                        <option value="ninguno">🏠 Ninguno</option>
                        <option value="basico">🎽 Básico</option>
                        <option value="gym">🏋️ Gym</option>
                      </select>
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Imagen del ejercicio</label>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleImageChange}
                      style={{padding: '10px', border: '2px dashed #ccc', borderRadius: '8px'}}
                    />
                    <small style={{color: '#666', display: 'block', marginTop: '5px'}}>
                      Opcional. Máximo 2MB. Si no subes, se usará placeholder.
                    </small>
                    
                    {imagePreview && (
                      <div className="image-preview">
                        <img 
                          src={imagePreview} 
                          alt="Preview" 
                        />
                      </div>
                    )}

                    {uploadingImage && uploadProgress > 0 && (
                      <div className="upload-progress">
                        Subiendo imagen: {Math.round(uploadProgress)}%
                      </div>
                    )}
                  </div>
                </div>

                <div className="modal-footer">
                  <button type="button" className="btn-cancel" onClick={cerrarModal}>
                    Cancelar
                  </button>
                  <button type="submit" className="btn-save" disabled={uploadingImage}>
                    {uploadingImage ? '⏳ Subiendo...' : (modoEdicion ? '💾 Actualizar' : '➕ Crear')}
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

export default Ejercicios;