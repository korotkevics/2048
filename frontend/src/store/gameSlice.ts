import { createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';

export type Direction = "UP" | "RIGHT" | "DOWN" | "LEFT";

export interface BoardState {
    grid: number[][];
}

export interface MoveResult {
    direction: Direction;
    moved: boolean;
    scoreGained: number;
    score: number;
    gameOver: boolean;
    won: boolean;
    boardState: BoardState;
}

interface GameState {
    gameId: string | null;
    boardState: BoardState | null;
    score: number;
    gameOver: boolean;
    won: boolean;
    aiSuggestion: Direction | null;
    status: 'idle' | 'playing' | 'ended';
}

const initialState: GameState = {
    gameId: null,
    boardState: null,
    score: 0,
    gameOver: false,
    won: false,
    aiSuggestion: null,
    status: 'idle'
};

const gameSlice = createSlice({
    name: 'game',
    initialState,
    reducers: {
        setGameId(state, action: PayloadAction<string>) {
            state.gameId = action.payload;
            state.status = 'playing';
            state.boardState = null;
            state.score = 0;
            state.gameOver = false;
            state.won = false;
            state.aiSuggestion = null;
        },
        updateGameState(state, action: PayloadAction<MoveResult>) {
            if (action.payload.boardState) {
                state.boardState = action.payload.boardState;
            }
            state.score = action.payload.score;
            state.gameOver = action.payload.gameOver;
            state.won = action.payload.won;
            state.aiSuggestion = null;
            if (state.gameOver || state.won) {
                state.status = 'ended';
            }
        },
        setAiSuggestion(state, action: PayloadAction<Direction>) {
            state.aiSuggestion = action.payload;
        },
        resetGame(state) {
            state.gameId = null;
            state.boardState = null;
            state.score = 0;
            state.gameOver = false;
            state.won = false;
            state.aiSuggestion = null;
            state.status = 'idle';
        }
    }
});

export const { setGameId, updateGameState, setAiSuggestion, resetGame } = gameSlice.actions;
export default gameSlice.reducer;
