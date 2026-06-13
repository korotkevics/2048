import axios from 'axios';

// We rely on Vite proxy for local dev:
const API_BASE = '/api/game';
const SETTINGS_API_BASE = '/api/settings';

const getClientId = () => {
    let id = sessionStorage.getItem('play2048_client_id');
    if (!id || id === 'default') {
        id = crypto.randomUUID();
        sessionStorage.setItem('play2048_client_id', id);
        console.log(`[API] Generated new Session Client ID: ${id}`);
    }
    return id;
};

// Create an instance
const api = axios.create();

// Add a request interceptor to always get the LATEST ID from sessionStorage
api.interceptors.request.use((config) => {
    const id = getClientId();
    config.headers['X-Client-ID'] = id;
    return config;
});

export const startNewGame = async (): Promise<any> => {
    console.log('[API] Starting new game...');
    const response = await api.post(`${API_BASE}`);
    return response.data;
};

export const getCurrentGame = async (): Promise<any> => {
    try {
        const response = await api.get(`${API_BASE}/current`);
        return response.data;
    } catch (e) {
        return null;
    }
};

export const makeMove = async (direction: string): Promise<any> => {
    const response = await api.post(`${API_BASE}/move`, { direction });
    return response.data;
};

export const requestAiSuggestion = async (): Promise<void> => {
    await api.post(`${API_BASE}/ai`);
};

export const undoMove = async (): Promise<any> => {
    try {
        const response = await api.post(`${API_BASE}/undo`);
        return response.data;
    } catch (e) {
        return null;
    }
};

export interface Settings {
    version: string;
    aiType: string;
    initialTileCount: number;
    tileProbabilities: Record<string, number>;
    highScore: number;
}

export const getSettings = async (): Promise<Settings> => {
    const response = await api.get(SETTINGS_API_BASE);
    return response.data;
};

export const updateSettings = async (settings: Partial<Settings>): Promise<void> => {
    await api.put(SETTINGS_API_BASE, settings);
};

export { getClientId };
