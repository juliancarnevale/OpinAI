import { Component } from 'react';
import type { ErrorInfo, ReactNode } from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Uncaught error in ErrorBoundary:', error, errorInfo);
  }

  private handleReload = () => {
    window.location.reload();
  };

  public render() {
    if (this.state.hasError) {
      const isDev = import.meta.env.DEV;
      return (
        <div className="min-h-screen bg-[#020617] text-slate-100 flex flex-col items-center justify-center p-6 select-none font-sans">
          <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-indigo-950/20 via-slate-950 to-slate-950 pointer-events-none" />
          
          <div className="relative max-w-lg w-full bg-slate-900/60 backdrop-blur-xl border border-slate-800/80 rounded-3xl p-8 text-center space-y-6 shadow-2xl">
            <div className="w-16 h-16 mx-auto bg-rose-500/10 border border-rose-500/20 rounded-2xl flex items-center justify-center text-rose-500 shadow-lg shadow-rose-500/5">
              <AlertTriangle className="w-8 h-8" />
            </div>

            <div className="space-y-2">
              <h2 className="text-2xl font-extrabold tracking-tight text-white">Algo salió mal</h2>
              <p className="text-sm text-slate-400 leading-relaxed">
                Ha ocurrido un error inesperado al renderizar la aplicación. Hemos registrado el incidente para solucionarlo.
              </p>
            </div>

            {isDev && this.state.error && (
              <div className="text-left bg-slate-950/90 border border-slate-850 p-4 rounded-2xl max-h-40 overflow-y-auto font-mono text-[10px] text-rose-300 leading-normal scrollbar-thin">
                <p className="font-bold text-slate-200 mb-1">{this.state.error.name}: {this.state.error.message}</p>
                <pre className="whitespace-pre-wrap">{this.state.error.stack}</pre>
              </div>
            )}

            <div className="pt-2">
              <button
                onClick={this.handleReload}
                className="w-full inline-flex items-center justify-center space-x-2 px-6 py-3 bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 active:scale-[0.98] text-white font-semibold text-sm rounded-xl cursor-pointer shadow-lg shadow-indigo-600/20 transition-all duration-300"
              >
                <RefreshCw className="w-4 h-4" />
                <span>Recargar Aplicación</span>
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
