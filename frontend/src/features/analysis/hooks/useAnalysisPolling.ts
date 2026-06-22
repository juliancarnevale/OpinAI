import { useEffect, useRef, useState, useCallback } from 'react';
import { useAnalysisStore } from '../../../store/analysisStore';
import type { AnalysisDetail } from '../types/Analysis';
import axios from 'axios';

export interface UseAnalysisPollingOptions {
  analysisId: string | undefined;
  enabled?: boolean;
  onSuccess?: (analysis: AnalysisDetail) => void;
  onError?: (error: Error) => void;
}

export interface UseAnalysisPollingResult {
  analysis: AnalysisDetail | null;
  isLoading: boolean;
  error: string | null;
  consecutiveErrors: number;
  isPolling: boolean;
  triggerReprocess: () => Promise<void>;
}

export function useAnalysisPolling({
  analysisId,
  enabled = true,
  onSuccess,
  onError,
}: UseAnalysisPollingOptions): UseAnalysisPollingResult {
  const {
    selectedAnalysis,
    fetchAnalysis,
    reprocessAnalysis,
    isLoading: storeLoading,
  } = useAnalysisStore();

  const [errorState, setErrorState] = useState<string | null>(null);
  const [consecutiveErrors, setConsecutiveErrors] = useState(0);
  const [isPollingActive, setIsPollingActive] = useState(false);

  const isMountedRef = useRef(true);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const onSuccessRef = useRef(onSuccess);
  const onErrorRef = useRef(onError);

  // Mark component mounting state
  useEffect(() => {
    isMountedRef.current = true;
    return () => {
      isMountedRef.current = false;
    };
  }, []);

  // Keep callback references updated to avoid putting them in useEffect dependency arrays
  useEffect(() => {
    onSuccessRef.current = onSuccess;
    onErrorRef.current = onError;
  }, [onSuccess, onError]);

  // Clears the timer safely
  const clearTimer = useCallback(() => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
  }, []);

  useEffect(() => {
    return () => {
      clearTimer();
    };
  }, [clearTimer]);

  // Initial load and polling activation decision
  useEffect(() => {
    if (!analysisId || !enabled) {
      setIsPollingActive(false);
      clearTimer();
      return;
    }

    let isSubscribed = true;

    const loadInitial = async () => {
      try {
        setErrorState(null);
        setConsecutiveErrors(0);
        const data = await fetchAnalysis(analysisId);
        
        if (!isMountedRef.current) return;
        if (!isSubscribed) return;

        if (data.status === 'PENDING' || data.status === 'PROCESSING') {
          setIsPollingActive(true);
        } else {
          setIsPollingActive(false);
        }
      } catch (err: unknown) {
        if (!isMountedRef.current) return;
        if (!isSubscribed) return;
        
        let errMsg = 'Error al cargar los detalles del análisis.';
        if (axios.isAxiosError(err) && err.response?.data?.message) {
          errMsg = err.response.data.message;
        } else if (err instanceof Error) {
          errMsg = err.message;
        }
        setErrorState(errMsg);
      }
    };

    loadInitial();

    return () => {
      isSubscribed = false;
    };
  }, [analysisId, enabled, fetchAnalysis, clearTimer]);

  // Recursive polling loop active when isPollingActive is true
  useEffect(() => {
    if (!isPollingActive || !analysisId || !enabled) {
      clearTimer();
      return;
    }

    const poll = async () => {
      try {
        const data = await fetchAnalysis(analysisId);
        
        if (!isMountedRef.current) return;

        // Reset consecutive errors count on successful update
        setConsecutiveErrors(0);
        setErrorState(null);

        if (onSuccessRef.current) {
          onSuccessRef.current(data);
        }

        if (data.status === 'COMPLETED' || data.status === 'FAILED') {
          setIsPollingActive(false);
          return;
        }

        // Program next poll with adaptive interval
        const interval = data.status === 'PROCESSING' ? 2000 : 3000;
        timerRef.current = setTimeout(poll, interval);
      } catch (err: unknown) {
        if (!isMountedRef.current) return;

        let status: number | undefined;
        if (axios.isAxiosError(err)) {
          status = err.response?.status;
        }

        setConsecutiveErrors((prev) => {
          if (!isMountedRef.current) return prev;
          
          const nextErrors = prev + 1;

          // Polling is stopped immediately on 4xx/500 backend errors, or upon the 3rd consecutive network error.
          if (status === 401 || status === 403 || status === 404 || status === 500 || nextErrors >= 3) {
            setIsPollingActive(false);
            let errMsg = 'Error al actualizar el análisis.';
            if (status === 401 || status === 403) {
              errMsg = 'Sesión expirada o acceso denegado.';
            } else if (status === 404) {
              errMsg = 'El análisis ya no existe o fue eliminado.';
            } else if (status === 500) {
              errMsg = 'Error interno del servidor (HTTP 500).';
            } else if (nextErrors >= 3) {
              errMsg = 'Se detuvo el sondeo automático tras 3 fallos de red consecutivos.';
            }
            setErrorState(errMsg);
            if (onErrorRef.current) {
              onErrorRef.current(new Error(errMsg));
            }
            clearTimer();
          } else {
            // Temporary error (e.g. error 1 or 2) -> schedule next poll attempt
            const currentStatus = selectedAnalysis?.status || 'PENDING';
            const interval = currentStatus === 'PROCESSING' ? 2000 : 3000;
            timerRef.current = setTimeout(poll, interval);
          }
          return nextErrors;
        });
      }
    };

    // Schedule the first poll in background
    const currentStatus = selectedAnalysis?.status || 'PENDING';
    const interval = currentStatus === 'PROCESSING' ? 2000 : 3000;
    timerRef.current = setTimeout(poll, interval);

    return () => {
      clearTimer();
    };
  }, [isPollingActive, analysisId, enabled, fetchAnalysis, clearTimer, selectedAnalysis?.status]);

  // Reprocess action handler
  const triggerReprocess = useCallback(async () => {
    if (!analysisId) return;
    try {
      setErrorState(null);
      setConsecutiveErrors(0);
      await reprocessAnalysis(analysisId);
      
      if (!isMountedRef.current) return;
      setIsPollingActive(true);
    } catch (err: unknown) {
      if (!isMountedRef.current) return;
      
      let errMsg = 'Error al iniciar el reprocesamiento.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        errMsg = err.response.data.message;
      } else if (err instanceof Error) {
        errMsg = err.message;
      }
      setErrorState(errMsg);
      throw err;
    }
  }, [analysisId, reprocessAnalysis]);

  return {
    analysis: selectedAnalysis,
    isLoading: storeLoading && !selectedAnalysis,
    error: errorState,
    consecutiveErrors,
    isPolling: isPollingActive,
    triggerReprocess,
  };
}
