import axios from 'axios';

// We rely on Vite proxy for local dev:
const API_BASE = '/api/game';
const SETTINGS_API_BASE = '/api/settings';

const getClientId = () => {
    let id = localStorage.getItem('play2048_client_id');
    if (!id) {
        id = crypto.randomUUID();
        localStorage.setItem('play2048_client_id', id);
    }
    return id;
};

const api = axios.create({
    headers: {
        'X-Client-ID': getClientId()
    }
});

export const startNewGame = async (): Promise<string> => {
    const response = await api.post(`${API_BASE}`);
    return response.data.gameId;
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
    return response.data; // Sync return
};

export const requestAiSuggestion = async (): Promise<void> => {
    await api.post(`${API_BASE}/ai`);
};

export interface Settings {
    version: string;
    aiType: string;
    initialTileCount: number;
    tileProbabilities: Record<string, number>;
}

export const getSettings = async (): Promise<Settings> => {
    const response = await api.get(SETTINGS_API_BASE);
    return response.data;
};

export const updateSettings = async (settings: Partial<Settings>): Promise<void> => {
    await api.put(SETTINGS_API_BASE, settings);
};

export { getClientId };

