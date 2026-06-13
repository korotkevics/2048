import axios from 'axios';

// We rely on Vite proxy for local dev:
const API_BASE = '/api/game';
const SETTINGS_API_BASE = '/api/settings';

export const startNewGame = async (): Promise<string> => {
    const response = await axios.post(`${API_BASE}`);
    return response.data.gameId;
};

export const makeMove = async (gameId: string, direction: string): Promise<void> => {
    await axios.post(`${API_BASE}/${gameId}/move`, { direction });
};

export const requestAiSuggestion = async (gameId: string): Promise<void> => {
    await axios.post(`${API_BASE}/${gameId}/ai`);
};

export interface Settings {
    version: string;
    aiType: string;
    initialTileCount: number;
}

export const getSettings = async (): Promise<Settings> => {
    const response = await axios.get(SETTINGS_API_BASE);
    return response.data;
};

export const updateSettings = async (settings: Partial<Settings>): Promise<void> => {
    await axios.put(SETTINGS_API_BASE, settings);
};

