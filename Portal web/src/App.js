import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Dashboard from './components/Dashboard';
import Usuarios from './components/Usuarios';
import Alimentos from './components/Alimentos';
import Ejercicios from './components/Ejercicios';
import Reportes from './components/Reportes';
import './App.css';


function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/usuarios" element={<Usuarios />} />
        <Route path="/alimentos" element={<Alimentos />} />
        <Route path="/ejercicios" element={<Ejercicios />} />
        <Route path="/reportes" element={<Reportes />} />
      </Routes>
    </Router>
  );
}

export default App;
