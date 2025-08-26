import { useEffect, useRef, useState } from 'react';

export function useSSECommentary(gameId, baseUrl = '') {
  const [events, setEvents] = useState([]);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState(null);
  const esRef = useRef(null);

  useEffect(() => {
    if (!gameId) return;
    const url = `${baseUrl}/api/sse/games/${encodeURIComponent(gameId)}/commentary`;
    const es = new EventSource(url, { withCredentials: true });
    esRef.current = es;

    function onOpen() {
      setConnected(true);
      setError(null);
    }
    function onError(e) {
      setConnected(false);
      setError('disconnected');
    }
    function onMessage(e) {
      try {
        const data = JSON.parse(e.data);
        setEvents(prev => [data, ...prev].slice(0, 200));
      } catch (_) {
        // ignore
      }
    }

    es.addEventListener('open', onOpen);
    es.addEventListener('error', onError);
    es.addEventListener('commentary', onMessage);

    return () => {
      es.removeEventListener('open', onOpen);
      es.removeEventListener('error', onError);
      es.removeEventListener('commentary', onMessage);
      es.close();
      esRef.current = null;
    };
  }, [gameId, baseUrl]);

  return { events, connected, error };
}


