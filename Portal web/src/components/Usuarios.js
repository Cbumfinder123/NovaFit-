import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { db } from '../firebase/config';
import { collection, getDocs, doc, updateDoc } from 'firebase/firestore';
import './Usuarios.css';

function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [usuarioSeleccionado, setUsuarioSeleccionado] = useState(null);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [filtroObjetivo, setFiltroObjetivo] = useState('todos');
  const [filtroEstado, setFiltroEstado] = useState('activos'); 
  const [busqueda, setBusqueda] = useState('');

  useEffect(() => {
    cargarUsuarios();
  }, []);

  const cargarUsuarios = async () => {
    try {
      setLoading(true);
      const querySnapshot = await getDocs(collection(db, 'usuarios'));
      const usuariosData = querySnapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      
      setUsuarios(usuariosData);
      setLoading(false);
    } catch (error) {
      console.error('Error cargando usuarios:', error);
      setLoading(false);
    }
  };

  const abrirModal = (usuario) => {
    setUsuarioSeleccionado(usuario);
    setModalAbierto(true);
  };

  const cerrarModal = () => {
    setUsuarioSeleccionado(null);
    setModalAbierto(false);
  };

 
  const cambiarEstadoUsuario = async (id, email, estadoActual) => {
    const nuevoEstado = !estadoActual;
    const accion = nuevoEstado ? 'activar' : 'desactivar';
    
    if (window.confirm(`¿Estás seguro de ${accion} a ${email}?`)) {
      try {
        await updateDoc(doc(db, 'usuarios', id), { 
          activo: nuevoEstado 
        });
        alert(`✅ Usuario ${nuevoEstado ? 'activado' : 'desactivado'} correctamente`);
        cargarUsuarios();
        if (modalAbierto) {
          cerrarModal();
        }
      } catch (error) {
        console.error('Error cambiando estado del usuario:', error);
        alert('❌ Error al cambiar estado del usuario');
      }
    }
  };

 
  const usuariosFiltrados = usuarios.filter(usuario => {
    
    if (filtroEstado === 'activos' && usuario.activo === false) return false;
    if (filtroEstado === 'inactivos' && usuario.activo !== false) return false;
    
  
    const cumpleFiltro = filtroObjetivo === 'todos' || usuario.objetivo === filtroObjetivo;
    
   
    const cumpleBusqueda = 
      usuario.email.toLowerCase().includes(busqueda.toLowerCase()) ||
      usuario.nombre.toLowerCase().includes(busqueda.toLowerCase());
    
    return cumpleFiltro && cumpleBusqueda;
  });


  const objetivosUnicos = [...new Set(usuarios.map(u => u.objetivo))].filter(Boolean);

  return (
    <div className="dashboard">
      <div className="sidebar">
        <h1>🔥 NovaFit Admin</h1>
        <nav>
          <Link to="/" className="nav-link">📊 Dashboard</Link>
          <Link to="/usuarios" className="nav-link active">👥 Usuarios</Link>
          <Link to="/alimentos" className="nav-link">🍗 Alimentos</Link>
          <Link to="/ejercicios" className="nav-link">💪 Ejercicios</Link>
          <Link to="/reportes" className="nav-link">📈 Reportes</Link>
        </nav>
      </div>

      <div className="content">
        <div className="header">
          <h1>👥 Gestión de Usuarios</h1>
          <button onClick={cargarUsuarios} className="btn-refresh">🔄 Actualizar</button>
        </div>

        {/* Filtros y Búsqueda */}
        <div className="filters-section">
          <div className="search-box">
            <input
              type="text"
              placeholder="🔍 Buscar por email o nombre..."
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
              className="search-input"
            />
          </div>

          {/* ✅ NUEVO: Filtro por estado */}
          <div className="filter-box">
            <label>Estado:</label>
            <select 
              value={filtroEstado} 
              onChange={(e) => setFiltroEstado(e.target.value)}
              className="filter-select"
            >
              <option value="todos">Todos</option>
              <option value="activos">✅ Activos</option>
              <option value="inactivos">⛔ Inactivos</option>
            </select>
          </div>

          <div className="filter-box">
            <label>Objetivo:</label>
            <select 
              value={filtroObjetivo} 
              onChange={(e) => setFiltroObjetivo(e.target.value)}
              className="filter-select"
            >
              <option value="todos">Todos</option>
              {objetivosUnicos.map(obj => (
                <option key={obj} value={obj}>{obj}</option>
              ))}
            </select>
          </div>

          <div className="result-count">
            {usuariosFiltrados.length} usuario(s) encontrado(s)
          </div>
        </div>

        {loading ? (
          <div className="loading">
            <div className="spinner"></div>
            <p>Cargando usuarios...</p>
          </div>
        ) : usuarios.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">👥</div>
            <h3>No hay usuarios registrados</h3>
            <p>Los usuarios aparecerán aquí cuando se registren desde la app móvil</p>
          </div>
        ) : usuariosFiltrados.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🔍</div>
            <h3>No se encontraron resultados</h3>
            <p>Intenta con otros filtros de búsqueda</p>
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Estado</th> {/* ✅ NUEVA COLUMNA */}
                  <th>Email</th>
                  <th>Nombre</th>
                  <th>Edad</th>
                  <th>Género</th>
                  <th>Objetivo</th>
                  <th>Nivel</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {usuariosFiltrados.map(usuario => (
                  <tr key={usuario.id} className={usuario.activo === false ? 'row-inactive' : ''}>
                    {/* ✅ COLUMNA DE ESTADO */}
                    <td>
                      {usuario.activo === false ? (
                        <span className="badge badge-inactive">⛔ Inactivo</span>
                      ) : (
                        <span className="badge badge-active">✅ Activo</span>
                      )}
                    </td>
                    
                    <td>
                      <span className="user-email">{usuario.email}</span>
                    </td>
                    <td>{usuario.nombre || 'Sin nombre'}</td>
                    <td>{usuario.edad || '-'} años</td>
                    <td>
                      <span className={`badge badge-${usuario.genero?.toLowerCase()}`}>
                        {usuario.genero === 'Masculino' ? '👨' : '👩'} {usuario.genero || '-'}
                      </span>
                    </td>
                    <td>
                      <span className="badge badge-objetivo">
                        {usuario.objetivo || '-'}
                      </span>
                    </td>
                    <td>{usuario.nivelExperiencia || '-'}</td>
                    <td>
                      <button 
                        className="btn-ver"
                        onClick={() => abrirModal(usuario)}
                      >
                        👁️ Ver
                      </button>
                      
                      {/* ✅ BOTÓN DINÁMICO: Activar o Desactivar */}
                      {usuario.activo === false ? (
                        <button 
                          className="btn-activate"
                          onClick={() => cambiarEstadoUsuario(usuario.id, usuario.email, usuario.activo)}
                        >
                          ✅ Activar
                        </button>
                      ) : (
                        <button 
                          className="btn-deactivate"
                          onClick={() => cambiarEstadoUsuario(usuario.id, usuario.email, usuario.activo)}
                        >
                          ⛔ Desactivar
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Modal de detalles */}
        {modalAbierto && usuarioSeleccionado && (
          <div className="modal-overlay" onClick={cerrarModal}>
            <div className="modal-content modal-large" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>👤 Detalles del Usuario</h2>
                <button className="btn-close" onClick={cerrarModal}>✖️</button>
              </div>
              
              <div className="modal-body">
                {/* ✅ MOSTRAR ESTADO EN EL HEADER */}
                <div className="user-profile-header">
                  <div className="user-avatar">
                    {usuarioSeleccionado.genero === 'Masculino' ? '👨' : '👩'}
                  </div>
                  <div className="user-profile-info">
                    <h3>{usuarioSeleccionado.nombre}</h3>
                    <p>{usuarioSeleccionado.email}</p>
                    {usuarioSeleccionado.activo === false && (
                      <span className="badge badge-inactive" style={{marginTop: '8px'}}>
                        ⛔ Usuario Inactivo
                      </span>
                    )}
                  </div>
                </div>

                <div className="info-sections">
                  <div className="info-section">
                    <h4>📊 Información Personal</h4>
                    <div className="info-grid">
                      <div className="info-item">
                        <span className="label">Edad:</span>
                        <span className="value">{usuarioSeleccionado.edad} años</span>
                      </div>
                      <div className="info-item">
                        <span className="label">Género:</span>
                        <span className="value">{usuarioSeleccionado.genero}</span>
                      </div>
                      <div className="info-item">
                        <span className="label">Peso:</span>
                        <span className="value">{usuarioSeleccionado.peso} kg</span>
                      </div>
                      <div className="info-item">
                        <span className="label">Altura:</span>
                        <span className="value">{usuarioSeleccionado.altura} cm</span>
                      </div>
                    </div>
                  </div>

                  <div className="info-section">
                    <h4>🎯 Objetivos y Nivel</h4>
                    <div className="info-grid">
                      <div className="info-item">
                        <span className="label">Objetivo:</span>
                        <span className="value">{usuarioSeleccionado.objetivo}</span>
                      </div>
                      <div className="info-item">
                        <span className="label">Nivel de Actividad:</span>
                        <span className="value">{usuarioSeleccionado.nivelActividad || '-'}</span>
                      </div>
                      <div className="info-item">
                        <span className="label">Tipo de Cuerpo:</span>
                        <span className="value">{usuarioSeleccionado.tipoCuerpo || '-'}</span>
                      </div>
                      <div className="info-item">
                        <span className="label">Experiencia:</span>
                        <span className="value">{usuarioSeleccionado.nivelExperiencia || '-'}</span>
                      </div>
                    </div>
                  </div>

                  {(usuarioSeleccionado.circunferenciaCuello || 
                    usuarioSeleccionado.circunferenciaCintura || 
                    usuarioSeleccionado.circunferenciaCadera) && (
                    <div className="info-section">
                      <h4>📏 Medidas Corporales</h4>
                      <div className="info-grid">
                        {usuarioSeleccionado.circunferenciaCuello > 0 && (
                          <div className="info-item">
                            <span className="label">Cuello:</span>
                            <span className="value">{usuarioSeleccionado.circunferenciaCuello} cm</span>
                          </div>
                        )}
                        {usuarioSeleccionado.circunferenciaCintura > 0 && (
                          <div className="info-item">
                            <span className="label">Cintura:</span>
                            <span className="value">{usuarioSeleccionado.circunferenciaCintura} cm</span>
                          </div>
                        )}
                        {usuarioSeleccionado.circunferenciaCadera > 0 && (
                          <div className="info-item">
                            <span className="label">Cadera:</span>
                            <span className="value">{usuarioSeleccionado.circunferenciaCadera} cm</span>
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              </div>

              <div className="modal-footer">
                <button className="btn-cancel" onClick={cerrarModal}>
                  Cerrar
                </button>
                
                {/* ✅ BOTÓN DINÁMICO EN EL MODAL */}
                {usuarioSeleccionado.activo === false ? (
                  <button 
                    className="btn-activate"
                    onClick={() => cambiarEstadoUsuario(usuarioSeleccionado.id, usuarioSeleccionado.email, usuarioSeleccionado.activo)}
                  >
                    ✅ Activar Usuario
                  </button>
                ) : (
                  <button 
                    className="btn-deactivate"
                    onClick={() => cambiarEstadoUsuario(usuarioSeleccionado.id, usuarioSeleccionado.email, usuarioSeleccionado.activo)}
                  >
                    ⛔ Desactivar Usuario
                  </button>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Usuarios;