import React, { useState } from 'react';
import { Plus, Trash2, AlertCircle, FileText, ListPlus } from 'lucide-react';
import type { CreateAnalysisRequest } from '../types/CreateAnalysisRequest';

interface CreateAnalysisFormProps {
  onSubmit: (data: CreateAnalysisRequest) => Promise<void>;
  isLoading: boolean;
}

export const CreateAnalysisForm: React.FC<CreateAnalysisFormProps> = ({ onSubmit, isLoading }) => {
  const [title, setTitle] = useState('');
  const [comments, setComments] = useState<string[]>(['']);
  const [bulkText, setBulkText] = useState('');
  const [importMode, setImportMode] = useState<'individual' | 'bulk'>('individual');
  const [validationErrors, setValidationErrors] = useState<{ title?: string; comments?: string }>({});

  const handleAddCommentField = () => {
    setComments((prev) => [...prev, '']);
    setValidationErrors((prev) => ({ ...prev, comments: undefined }));
  };

  const handleRemoveCommentField = (index: number) => {
    if (comments.length === 1) return;
    setComments((prev) => prev.filter((_, i) => i !== index));
  };

  const handleCommentChange = (index: number, value: string) => {
    setComments((prev) => {
      const copy = [...prev];
      copy[index] = value;
      return copy;
    });
    if (value.trim()) {
      setValidationErrors((prev) => ({ ...prev, comments: undefined }));
    }
  };

  const handleBulkImport = () => {
    if (!bulkText.trim()) return;
    const parsedLines = bulkText
      .split('\n')
      .map((line) => line.trim())
      .filter((line) => line.length > 0);

    if (parsedLines.length > 0) {
      setComments(parsedLines);
      setImportMode('individual');
      setBulkText('');
      setValidationErrors((prev) => ({ ...prev, comments: undefined }));
    }
  };

  const validate = (): boolean => {
    const errors: { title?: string; comments?: string } = {};

    if (!title.trim()) {
      errors.title = 'El título del análisis es obligatorio.';
    } else if (title.length > 255) {
      errors.title = 'El título no puede superar los 255 caracteres.';
    }

    const activeComments = importMode === 'bulk'
      ? bulkText.split('\n').map((l) => l.trim()).filter((l) => l.length > 0)
      : comments.map((c) => c.trim()).filter((c) => c.length > 0);

    if (activeComments.length === 0) {
      errors.comments = 'Debe proporcionar al menos un comentario válido.';
    } else if (importMode === 'individual' && comments.some((c) => !c.trim())) {
      errors.comments = 'No se permiten comentarios vacíos en la lista.';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    const finalComments = importMode === 'bulk'
      ? bulkText.split('\n').map((l) => l.trim()).filter((l) => l.length > 0)
      : comments.map((c) => c.trim()).filter((c) => c.length > 0);

    const payload: CreateAnalysisRequest = {
      title: title.trim(),
      feedbackItems: finalComments.map((content) => ({ content })),
    };

    await onSubmit(payload);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Título */}
      <div className="space-y-2">
        <label htmlFor="analysis-title" className="block text-sm font-medium text-slate-300">
          Título del Análisis <span className="text-violet-400">*</span>
        </label>
        <input
          type="text"
          id="analysis-title"
          value={title}
          onChange={(e) => {
            setTitle(e.target.value);
            if (e.target.value.trim()) {
              setValidationErrors((prev) => ({ ...prev, title: undefined }));
            }
          }}
          className={`w-full bg-slate-950 border ${
            validationErrors.title ? 'border-rose-500 focus:ring-rose-500' : 'border-slate-800 focus:ring-violet-500'
          } rounded-lg px-4 py-2.5 text-white text-sm focus:outline-none focus:ring-2`}
          placeholder="Ej. Análisis de Feedback Semanal, Sprint 2..."
          maxLength={255}
          disabled={isLoading}
        />
        {validationErrors.title && (
          <p className="text-xs text-rose-400 flex items-center mt-1">
            <AlertCircle className="w-3.5 h-3.5 mr-1 shrink-0" />
            {validationErrors.title}
          </p>
        )}
      </div>

      {/* Selector de Modo de Carga */}
      <div className="space-y-4">
        <div className="flex items-center justify-between border-b border-slate-800 pb-2">
          <label className="block text-sm font-medium text-slate-300">
            Opiniones de Clientes <span className="text-violet-400">*</span>
          </label>
          <div className="flex space-x-2">
            <button
              type="button"
              onClick={() => setImportMode('individual')}
              className={`inline-flex items-center space-x-1.5 px-3 py-1 rounded-md text-xs font-medium transition cursor-pointer ${
                importMode === 'individual'
                  ? 'bg-violet-600/20 text-violet-400 border border-violet-500/30'
                  : 'bg-slate-950 text-slate-400 border border-slate-800 hover:text-slate-200'
              }`}
            >
              <ListPlus className="w-3.5 h-3.5" />
              <span>Lista Dinámica</span>
            </button>
            <button
              type="button"
              onClick={() => setImportMode('bulk')}
              className={`inline-flex items-center space-x-1.5 px-3 py-1 rounded-md text-xs font-medium transition cursor-pointer ${
                importMode === 'bulk'
                  ? 'bg-violet-600/20 text-violet-400 border border-violet-500/30'
                  : 'bg-slate-950 text-slate-400 border border-slate-800 hover:text-slate-200'
              }`}
            >
              <FileText className="w-3.5 h-3.5" />
              <span>Pegar en Bloque</span>
            </button>
          </div>
        </div>

        {validationErrors.comments && (
          <div className="bg-rose-950/20 border border-rose-900/50 text-rose-400 px-3 py-2 rounded-lg text-xs flex items-center">
            <AlertCircle className="w-4 h-4 mr-2 shrink-0" />
            {validationErrors.comments}
          </div>
        )}

        {/* Modo 1: Lista Dinámica */}
        {importMode === 'individual' && (
          <div className="space-y-3 max-h-[300px] overflow-y-auto pr-1">
            {comments.map((comment, index) => (
              <div key={index} className="flex items-center space-x-2">
                <input
                  type="text"
                  value={comment}
                  onChange={(e) => handleCommentChange(index, e.target.value)}
                  className="flex-1 bg-slate-950 border border-slate-800 rounded-lg px-4 py-2 text-white text-sm focus:outline-none focus:ring-2 focus:ring-violet-500"
                  placeholder={`Opinión #${index + 1}`}
                  disabled={isLoading}
                />
                <button
                  type="button"
                  onClick={() => handleRemoveCommentField(index)}
                  disabled={comments.length === 1 || isLoading}
                  className="p-2 text-slate-500 hover:text-rose-400 hover:bg-rose-950/20 rounded-lg transition disabled:opacity-30 disabled:hover:bg-transparent cursor-pointer"
                  title="Eliminar opinión"
                >
                  <Trash2 className="w-4.5 h-4.5" />
                </button>
              </div>
            ))}

            <button
              type="button"
              onClick={handleAddCommentField}
              disabled={isLoading}
              className="w-full py-2 bg-slate-950 hover:bg-slate-900 text-slate-300 font-medium rounded-lg text-xs border border-dashed border-slate-800 hover:border-slate-700 transition flex items-center justify-center space-x-1 cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Agregar otra opinión</span>
            </button>
          </div>
        )}

        {/* Modo 2: Pegar en Bloque */}
        {importMode === 'bulk' && (
          <div className="space-y-3">
            <textarea
              value={bulkText}
              onChange={(e) => {
                setBulkText(e.target.value);
                if (e.target.value.trim()) {
                  setValidationErrors((prev) => ({ ...prev, comments: undefined }));
                }
              }}
              rows={6}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-3 text-white text-sm focus:outline-none focus:ring-2 focus:ring-violet-500"
              placeholder="Pega las opiniones aquí, una por línea. Ejemplo:&#10;Muy buena atención&#10;La comida llegó fría&#10;Excelente servicio"
              disabled={isLoading}
            />
            <div className="flex justify-between items-center text-xs text-slate-500">
              <span>{bulkText.split('\n').filter(l => l.trim()).length} opiniones detectadas</span>
              <button
                type="button"
                onClick={handleBulkImport}
                disabled={!bulkText.trim() || isLoading}
                className="text-violet-400 hover:text-violet-300 font-semibold cursor-pointer disabled:opacity-40"
              >
                Cargar en lista individual
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Botones de Envío */}
      <div className="flex justify-end space-x-3 pt-4 border-t border-slate-800/80">
        <button
          type="submit"
          disabled={isLoading}
          className="inline-flex items-center space-x-2 px-5 py-2.5 bg-violet-600 hover:bg-violet-700 text-white font-medium rounded-lg text-sm transition shadow-lg shadow-violet-600/20 cursor-pointer disabled:opacity-50"
        >
          <span>Guardar Análisis</span>
        </button>
      </div>
    </form>
  );
};
