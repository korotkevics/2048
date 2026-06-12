import axios from 'axios';

// We rely on Vite proxy for local dev:
const API_BASE = '/api/game';

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
