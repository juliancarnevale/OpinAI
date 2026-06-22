import React from 'react';
import RegisterForm from '../components/RegisterForm';
import { BarChart3 } from 'lucide-react';

const RegisterPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-center items-center p-4">
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-xl space-y-8">
        
        {/* Cabecera / Logo */}
        <div className="text-center space-y-2">
          <div className="flex justify-center items-center space-x-2">
            <BarChart3 className="w-10 h-10 text-violet-500" />
            <span className="font-bold text-2xl tracking-wider text-white">OpinAI</span>
          </div>
          <h2 className="text-xl font-bold text-white tracking-tight pt-2">
            Crear una cuenta
          </h2>
          <p className="text-sm text-slate-400">
            Empieza a optimizar tu negocio hoy
          </p>
        </div>

        {/* Formulario */}
        <RegisterForm />
      </div>
    </div>
  );
};

export default RegisterPage;
