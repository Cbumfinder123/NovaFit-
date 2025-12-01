import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { db } from '../firebase/config';
import { collection, getDocs, query, where } from 'firebase/firestore';
import './Dashboard.css';

function Dashboard() {
  const [stats, setStats] = useState({
    totalUsuarios: 0,
    totalAlimentos: 0,
    totalEjercicios: 0,
    usuariosHoy: 0,
    loading: true
  });

  const [topObjetivos, setTopObjetivos] = useState([]);

  useEffect(() => {
    cargarEstadisticas();
  }, []);

  const cargarEstadisticas = async () => {
    try {
      setStats(prev => ({ ...prev, loading: true }));

    
      const usuariosSnap = await getDocs(collection(db, 'usuarios'));
      const totalUsuarios = usuariosSnap.size;

    
      const hoy = new Date();
      hoy.setHours(0, 0, 0, 0);
      const usuariosHoySnap = await getDocs(
        query(
          collection(db, 'usuarios'),
          where('fechaRegistro', '>=', hoy)
        )
      );
      const usuariosHoy = usuariosHoySnap.size;

     
      const alimentosSnap = await getDocs(
        query(collection(db, 'alimentos'), where('activo', '==', true))
      );
      const totalAlimentos = alimentosSnap.size;

     
      const ejerciciosSnap = await getDocs(
        query(collection(db, 'ejercicios'), where('activo', '==', true))
      );
      const totalEjercicios = ejerciciosSnap.size;

     
      const objetivos = {};
      usuariosSnap.docs.forEach(doc => {
        const objetivo = doc.data().objetivo || 'Sin objetivo';
        objetivos[objetivo] = (objetivos[objetivo] || 0) + 1;
      });

      const topObjetivosArray = Object.entries(objetivos)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 3)
        .map(([nombre, cantidad]) => ({ nombre, cantidad }));

      setStats({
        totalUsuarios,
        totalAlimentos,
        totalEjercicios,
        usuariosHoy,
        loading: false
      });

      setTopObjetivos(topObjetivosArray);

    } catch (error) {
      console.error('Error cargando estadísticas:', error);
      setStats(prev => ({ ...prev, loading: false }));
    }
  };

  return (
    <div className="dashboard">
      <div className="sidebar">
        <h1>🔥 NovaFit Admin</h1>
        <nav>
          <Link to="/" className="nav-link active">📊 Dashboard</Link>
          <Link to="/usuarios" className="nav-link">👥 Usuarios</Link>
          <Link to="/alimentos" className="nav-link">🍗 Alimentos</Link>
          <Link to="/ejercicios" className="nav-link">💪 Ejercicios</Link>
           <Link to="/reportes" className="nav-link">📈 Reportes</Link>
        </nav>
      </div>
      
      <div className="content">
        <div className="header">
          <h1>📊 Panel de Administración</h1>
          <button onClick={cargarEstadisticas} className="btn-refresh">
            🔄 Actualizar
          </button>
        </div>

        {stats.loading ? (
          <div className="loading">
            <div className="spinner"></div>
            <p>Cargando estadísticas...</p>
          </div>
        ) : (
          <>
            {/* Estadísticas Principales */}
            <div className="stats-section">
              <h2>📈 Estadísticas Generales</h2>
              <div className="stats-grid">
                <div className="stat-card stat-usuarios">
                  <div className="stat-icon">👥</div>
                  <div className="stat-info">
                    <h3>Usuarios Registrados</h3>
                    <p className="stat-number">{stats.totalUsuarios}</p>
                    <span className="stat-subtitle">
                      {stats.usuariosHoy > 0 ? `+${stats.usuariosHoy} hoy` : 'Ninguno hoy'}
                    </span>
                  </div>
                </div>

                <div className="stat-card stat-alimentos">
                  <div className="stat-icon">🍗</div>
                  <div className="stat-info">
                    <h3>Alimentos Disponibles</h3>
                    <p className="stat-number">{stats.totalAlimentos}</p>
                    <span className="stat-subtitle">En base de datos</span>
                  </div>
                </div>

                <div className="stat-card stat-ejercicios">
                  <div className="stat-icon">💪</div>
                  <div className="stat-info">
                    <h3>Ejercicios Disponibles</h3>
                    <p className="stat-number">{stats.totalEjercicios}</p>
                    <span className="stat-subtitle">Con detalles completos</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Top Objetivos */}
            {topObjetivos.length > 0 && (
              <div className="top-section">
                <h2>🎯 Objetivos Más Comunes</h2>
                <div className="top-list">
                  {topObjetivos.map((obj, index) => (
                    <div key={index} className="top-item">
                      <span className="top-rank">#{index + 1}</span>
                      <span className="top-name">{obj.nombre}</span>
                      <span className="top-count">{obj.cantidad} usuarios</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Accesos Rápidos */}
            <div className="quick-actions">
              <h2>⚡ Accesos Rápidos</h2>
              <div className="actions-grid">
                <Link to="/usuarios" className="action-card">
                  <div className="action-icon">👥</div>
                  <h3>Gestionar Usuarios</h3>
                  <p>Ver y administrar usuarios registrados</p>
                  <div className="action-badge">{stats.totalUsuarios} registrados</div>
                </Link>

                <Link to="/alimentos" className="action-card">
                  <div className="action-icon">🍗</div>
                  <h3>Gestionar Alimentos</h3>
                  <p>Agregar, editar o eliminar alimentos</p>
                  <div className="action-badge">{stats.totalAlimentos} activos</div>
                </Link>

                <Link to="/ejercicios" className="action-card">
                  <div className="action-icon">💪</div>
                  <h3>Gestionar Ejercicios</h3>
                  <p>Administrar ejercicios y subir imágenes</p>
                  <div className="action-badge">{stats.totalEjercicios} activos</div>
                </Link>
              </div>
            </div>

            {/* Distribución */}
            <div className="distribution-section">
              <div className="dist-card">
                <h3>🍽️ Alimentos por Categoría</h3>
                <div className="dist-items">
                  <div className="dist-item">
                    <span className="dist-emoji">🍗</span>
                    <span className="dist-name">Proteínas</span>
                  </div>
                  <div className="dist-item">
                    <span className="dist-emoji">🥑</span>
                    <span className="dist-name">Grasas</span>
                  </div>
                  <div className="dist-item">
                    <span className="dist-emoji">🍚</span>
                    <span className="dist-name">Carbohidratos</span>
                  </div>
                </div>
              </div>

              <div className="dist-card">
                <h3>🏋️ Ejercicios por Equipamiento</h3>
                <div className="dist-items">
                  <div className="dist-item">
                    <span className="dist-emoji">🏠</span>
                    <span className="dist-name">Sin equipo</span>
                  </div>
                  <div className="dist-item">
                    <span className="dist-emoji">🎽</span>
                    <span className="dist-name">Equipamiento básico</span>
                  </div>
                  <div className="dist-item">
                    <span className="dist-emoji">🏋️</span>
                    <span className="dist-name">Gimnasio</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Info del Sistema */}
            <div className="system-info">
              <h2>ℹ️ Información del Sistema</h2>
              <div className="info-grid">
                <div className="info-item">
                  <span className="info-label">📊 Base de datos:</span>
                  <span className="info-value">Firebase Firestore</span>
                </div>
                <div className="info-item">
                  <span className="info-label">🔄 Sincronización:</span>
                  <span className="info-value">Tiempo Real</span>
                </div>
                <div className="info-item">
                  <span className="info-label">🖼️ Almacenamiento:</span>
                  <span className="info-value">Firebase Storage</span>
                </div>
                <div className="info-item">
                  <span className="info-label">📱 App móvil:</span>
                  <span className="info-value">Android (Kotlin)</span>
                </div>
                <div className="info-item">
                  <span className="info-label">💻 Panel Admin:</span>
                  <span className="info-value">React + Vite</span>
                </div>
                <div className="info-item">
                  <span className="info-label">🌐 Estado:</span>
                  <span className="info-value status-online">● Conectado</span>
                </div>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default Dashboard;