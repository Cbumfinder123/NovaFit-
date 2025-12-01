import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { db } from '../firebase/config';
import { collection, getDocs, query, where } from 'firebase/firestore';
import './Reportes.css';

function Reportes() {
  const [loading, setLoading] = useState(true);
  const [fechaGeneracion, setFechaGeneracion] = useState('');
  
  const [estadisticas, setEstadisticas] = useState({
  
    totalUsuarios: 0,
    porGenero: {},
    porObjetivo: {},
    porNivel: {},
    porActividad: {},
    porTipoCuerpo: {},
    promedios: {
      peso: 0,
      altura: 0,
      edad: 0,
      imc: 0
    },
    
  
    totalAlimentos: 0,
    alimentosPorCategoria: {},
    totalEjercicios: 0,
    ejerciciosPorEquipamiento: {},
    ejerciciosPorGrupo: {}
  });

  useEffect(() => {
    cargarEstadisticas();
    setFechaGeneracion(new Date().toLocaleString('es-PE', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }));
  }, []);

  const cargarEstadisticas = async () => {
    try {
      setLoading(true);

     
      const usuariosSnap = await getDocs(collection(db, 'usuarios'));
      const usuarios = usuariosSnap.docs
        .map(doc => doc.data())
        .filter(u => u.activo !== false);

      const totalUsuarios = usuarios.length;

     
      const porGenero = usuarios.reduce((acc, u) => {
        const genero = u.genero || 'No especificado';
        acc[genero] = (acc[genero] || 0) + 1;
        return acc;
      }, {});

      
      const porObjetivo = usuarios.reduce((acc, u) => {
        const objetivo = u.objetivo || 'Sin objetivo';
        acc[objetivo] = (acc[objetivo] || 0) + 1;
        return acc;
      }, {});

      
      const porNivel = usuarios.reduce((acc, u) => {
        const nivel = u.nivelExperiencia || 'No especificado';
        acc[nivel] = (acc[nivel] || 0) + 1;
        return acc;
      }, {});

      
      const porActividad = usuarios.reduce((acc, u) => {
        const actividad = u.nivelActividad || 'No especificado';
        acc[actividad] = (acc[actividad] || 0) + 1;
        return acc;
      }, {});

      
      const porTipoCuerpo = usuarios.reduce((acc, u) => {
        const tipo = u.tipoCuerpo || 'No especificado';
        acc[tipo] = (acc[tipo] || 0) + 1;
        return acc;
      }, {});

      
      const pesoPromedio = usuarios.reduce((sum, u) => sum + (u.peso || 0), 0) / totalUsuarios;
      const alturaPromedio = usuarios.reduce((sum, u) => sum + (u.altura || 0), 0) / totalUsuarios;
      const edadPromedio = usuarios.reduce((sum, u) => sum + (u.edad || 0), 0) / totalUsuarios;
      
      
      const imcPromedio = pesoPromedio / Math.pow(alturaPromedio / 100, 2);

      
      const alimentosSnap = await getDocs(
        query(collection(db, 'alimentos'), where('activo', '==', true))
      );
      const alimentos = alimentosSnap.docs.map(doc => doc.data());
      const totalAlimentos = alimentos.length;

      const alimentosPorCategoria = alimentos.reduce((acc, a) => {
        const cat = a.categoria || 'Sin categoría';
        acc[cat] = (acc[cat] || 0) + 1;
        return acc;
      }, {});

     
      const ejerciciosSnap = await getDocs(
        query(collection(db, 'ejercicios'), where('activo', '==', true))
      );
      const ejercicios = ejerciciosSnap.docs.map(doc => doc.data());
      const totalEjercicios = ejercicios.length;

      const ejerciciosPorEquipamiento = ejercicios.reduce((acc, e) => {
        const equip = e.equipamiento || 'No especificado';
        acc[equip] = (acc[equip] || 0) + 1;
        return acc;
      }, {});

      const ejerciciosPorGrupo = ejercicios.reduce((acc, e) => {
        const grupo = e.grupoMuscular || 'No especificado';
        acc[grupo] = (acc[grupo] || 0) + 1;
        return acc;
      }, {});

      
      setEstadisticas({
        totalUsuarios,
        porGenero,
        porObjetivo,
        porNivel,
        porActividad,
        porTipoCuerpo,
        promedios: {
          peso: pesoPromedio.toFixed(1),
          altura: alturaPromedio.toFixed(1),
          edad: Math.round(edadPromedio),
          imc: imcPromedio.toFixed(1)
        },
        totalAlimentos,
        alimentosPorCategoria,
        totalEjercicios,
        ejerciciosPorEquipamiento,
        ejerciciosPorGrupo
      });

      setLoading(false);
    } catch (error) {
      console.error('Error cargando estadísticas:', error);
      setLoading(false);
    }
  };

  const imprimirReporte = () => {
    window.print();
  };

  const getIMCCategoria = (imc) => {
    if (imc < 18.5) return { texto: 'Bajo peso', color: '#3498db' };
    if (imc < 25) return { texto: 'Normal', color: '#2ecc71' };
    if (imc < 30) return { texto: 'Sobrepeso', color: '#f39c12' };
    return { texto: 'Obesidad', color: '#e74c3c' };
  };

  const imcCategoria = getIMCCategoria(parseFloat(estadisticas.promedios.imc));

  return (
    <div className="dashboard">
      <div className="sidebar no-print">
        <h1>🔥 NovaFit Admin</h1>
        <nav>
          <Link to="/" className="nav-link">📊 Dashboard</Link>
          <Link to="/usuarios" className="nav-link">👥 Usuarios</Link>
          <Link to="/alimentos" className="nav-link">🍗 Alimentos</Link>
          <Link to="/ejercicios" className="nav-link">💪 Ejercicios</Link>
          <Link to="/reportes" className="nav-link active">📈 Reportes</Link>
        </nav>
      </div>

      <div className="content reportes-content">
        <div className="header no-print">
          <h1>📈 Reportes y Estadísticas</h1>
          <button onClick={imprimirReporte} className="btn-print">
            🖨️ Imprimir / Guardar PDF
          </button>
        </div>

        {loading ? (
          <div className="loading">
            <div className="spinner"></div>
            <p>Generando reporte...</p>
          </div>
        ) : (
          <div className="reporte-container">
            {/* Header del Reporte */}
            <div className="reporte-header">
              <div className="reporte-logo">
                <h1>🔥 NovaFit</h1>
                <p>Sistema de Gestión Fitness</p>
              </div>
              <div className="reporte-info">
                <h2>Reporte de Estadísticas</h2>
                <p>Generado el: {fechaGeneracion}</p>
              </div>
            </div>

            {/* Resumen Ejecutivo */}
            <div className="reporte-section">
              <h2>📊 Resumen Ejecutivo</h2>
              <div className="resumen-grid">
                <div className="resumen-card">
                  <div className="resumen-icon">👥</div>
                  <div className="resumen-data">
                    <h3>{estadisticas.totalUsuarios}</h3>
                    <p>Usuarios Activos</p>
                  </div>
                </div>
                <div className="resumen-card">
                  <div className="resumen-icon">🍗</div>
                  <div className="resumen-data">
                    <h3>{estadisticas.totalAlimentos}</h3>
                    <p>Alimentos</p>
                  </div>
                </div>
                <div className="resumen-card">
                  <div className="resumen-icon">💪</div>
                  <div className="resumen-data">
                    <h3>{estadisticas.totalEjercicios}</h3>
                    <p>Ejercicios</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Análisis de Usuarios */}
            <div className="reporte-section">
              <h2>👥 Análisis Demográfico</h2>
              
              <div className="stats-row">
                <div className="stat-box">
                  <h3>Distribución por Género</h3>
                  <table className="data-table-mini">
                    <thead>
                      <tr>
                        <th>Género</th>
                        <th>Cantidad</th>
                        <th>%</th>
                      </tr>
                    </thead>
                    <tbody>
                      {Object.entries(estadisticas.porGenero).map(([genero, count]) => (
                        <tr key={genero}>
                          <td>{genero === 'Masculino' ? '🚹' : '🚺'} {genero}</td>
                          <td>{count}</td>
                          <td>{((count / estadisticas.totalUsuarios) * 100).toFixed(1)}%</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div className="stat-box">
                  <h3>Promedios Físicos</h3>
                  <div className="promedios-list">
                    <div className="promedio-item">
                      <span className="promedio-label">⚖️ Peso Promedio:</span>
                      <span className="promedio-value">{estadisticas.promedios.peso} kg</span>
                    </div>
                    <div className="promedio-item">
                      <span className="promedio-label">📐 Altura Promedio:</span>
                      <span className="promedio-value">{estadisticas.promedios.altura} cm</span>
                    </div>
                    <div className="promedio-item">
                      <span className="promedio-label">🎂 Edad Promedio:</span>
                      <span className="promedio-value">{estadisticas.promedios.edad} años</span>
                    </div>
                    <div className="promedio-item">
                      <span className="promedio-label">📊 IMC Promedio:</span>
                      <span className="promedio-value" style={{color: imcCategoria.color}}>
                        {estadisticas.promedios.imc} ({imcCategoria.texto})
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Objetivos y Actividad */}
            <div className="reporte-section">
              <h2>🎯 Objetivos y Actividad</h2>
              
              <div className="stats-row">
                <div className="stat-box">
                  <h3>Objetivos Principales</h3>
                  <table className="data-table-mini">
                    <thead>
                      <tr>
                        <th>Objetivo</th>
                        <th>Usuarios</th>
                        <th>%</th>
                      </tr>
                    </thead>
                    <tbody>
                      {Object.entries(estadisticas.porObjetivo)
                        .sort((a, b) => b[1] - a[1])
                        .map(([objetivo, count]) => (
                          <tr key={objetivo}>
                            <td>💪 {objetivo}</td>
                            <td>{count}</td>
                            <td>{((count / estadisticas.totalUsuarios) * 100).toFixed(1)}%</td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>

                <div className="stat-box">
                  <h3>Nivel de Experiencia</h3>
                  <table className="data-table-mini">
                    <thead>
                      <tr>
                        <th>Nivel</th>
                        <th>Usuarios</th>
                        <th>%</th>
                      </tr>
                    </thead>
                    <tbody>
                      {Object.entries(estadisticas.porNivel).map(([nivel, count]) => (
                        <tr key={nivel}>
                          <td>
                            {nivel === 'Principiante' && '🟢'}
                            {nivel === 'Intermedio' && '🟡'}
                            {nivel === 'Avanzado' && '🔴'}
                            {' '}{nivel}
                          </td>
                          <td>{count}</td>
                          <td>{((count / estadisticas.totalUsuarios) * 100).toFixed(1)}%</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            {/* Recursos Disponibles */}
            <div className="reporte-section">
              <h2>📚 Recursos del Sistema</h2>
              
              <div className="stats-row">
                <div className="stat-box">
                  <h3>🍗 Alimentos por Categoría</h3>
                  <table className="data-table-mini">
                    <thead>
                      <tr>
                        <th>Categoría</th>
                        <th>Cantidad</th>
                      </tr>
                    </thead>
                    <tbody>
                      {Object.entries(estadisticas.alimentosPorCategoria).map(([cat, count]) => (
                        <tr key={cat}>
                          <td>
                            {cat === 'proteina' && '🍗 Proteínas'}
                            {cat === 'grasa' && '🥑 Grasas'}
                            {cat === 'carbohidrato' && '🍚 Carbohidratos'}
                          </td>
                          <td>{count}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <div className="stat-box">
                  <h3>💪 Ejercicios por Equipamiento</h3>
                  <table className="data-table-mini">
                    <thead>
                      <tr>
                        <th>Equipamiento</th>
                        <th>Cantidad</th>
                      </tr>
                    </thead>
                    <tbody>
                      {Object.entries(estadisticas.ejerciciosPorEquipamiento).map(([equip, count]) => (
                        <tr key={equip}>
                          <td>
                            {equip === 'ninguno' && '🏠 Sin equipamiento'}
                            {equip === 'basico' && '🎽 Básico'}
                            {equip === 'gym' && '🏋️ Gimnasio'}
                          </td>
                          <td>{count}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            {/* Footer del Reporte */}
            <div className="reporte-footer">
              <p>Este reporte fue generado automáticamente por el sistema NovaFit</p>
              <p>© 2025 NovaFit - Todos los derechos reservados</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Reportes;