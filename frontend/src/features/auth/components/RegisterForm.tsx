import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../../../store/authStore';
import { Loader2 } from 'lucide-react';
import { ROUTES } from '../../../config/constants';

const RegisterForm: React.FC = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();
  const { register, login, isLoading, error, clearError } = useAuthStore();

  // Limpiamos errores al montar
  useEffect(() => {
    clearError();
  }, [clearError]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Dividimos el campo 'name' en firstName y lastName para que coincida con el backend DTO
    const nameParts = name.trim().split(/\s+/);
    const firstName = nameParts[0];
    const lastName = nameParts.slice(1).join(' ') || undefined;

    try {
      await register({
        email,
        password,
        firstName,
        lastName,
      });

      setSuccess(true);
      
      // Auto-login tras registro exitoso
      await login({ email, password });
      navigate(ROUTES.DASHBOARD);
    } catch (err) {
      // Error manejado en Zustand
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {error && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/20 text-rose-400 rounded-lg text-sm text-center">
          {error}
        </div>
      )}

      {success && (
        <div className="p-4 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-lg text-sm text-center">
          Registro exitoso. Iniciando sesión...
        </div>
      )}

      <div>
        <label htmlFor="name" className="block text-sm font-medium text-slate-300">
          Nombre Completo
        </label>
        <input
          id="name"
          type="text"
          required
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Julian FullStack"
          className="mt-1.5 block w-full rounded-lg bg-slate-950 border border-slate-800 px-4 py-2.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent placeholder-slate-600"
        />
      </div>

      <div>
        <label htmlFor="email" className="block text-sm font-medium text-slate-300">
          Correo Electrónico
        </label>
        <input
          id="email"
          type="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="ejemplo@opinai.com"
          className="mt-1.5 block w-full rounded-lg bg-slate-950 border border-slate-800 px-4 py-2.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent placeholder-slate-600"
        />
      </div>

      <div>
        <label htmlFor="password" className="block text-sm font-medium text-slate-300">
          Contraseña
        </label>
        <input
          id="password"
          type="password"
          required
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Mínimo 6 caracteres"
          minLength={6}
          className="mt-1.5 block w-full rounded-lg bg-slate-950 border border-slate-800 px-4 py-2.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent placeholder-slate-600"
        />
      </div>

      <button
        type="submit"
        disabled={isLoading || success}
        className="w-full flex items-center justify-center space-x-2 px-4 py-2.5 bg-violet-600 hover:bg-violet-700 disabled:bg-violet-800/50 text-white font-medium rounded-lg text-sm transition shadow-lg shadow-violet-600/20 cursor-pointer disabled:cursor-not-allowed"
      >
        {isLoading ? (
          <>
            <Loader2 className="w-5 h-5 animate-spin" />
            <span>Registrando cuenta...</span>
          </>
        ) : (
          <span>Registrarse</span>
        )}
      </button>

      <p className="text-center text-sm text-slate-400">
        ¿Ya tienes una cuenta?{' '}
        <Link to={ROUTES.LOGIN} className="text-violet-400 hover:text-violet-300 font-semibold underline decoration-violet-500/30">
          Inicia sesión
        </Link>
      </p>
    </form>
  );
};

export default RegisterForm;
