import { createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';

export type Direction = "UP" | "RIGHT" | "DOWN" | "LEFT";

export interface BoardState {
    grid: number[][];
}

export interface TileMove {
    fromRow: number;
    fromCol: number;
    toRow: number;
    toCol: number;
    value: number;
    merged: boolean;
    isNew: boolean;
}

export interface MoveResult {
    direction: Direction | null;
    moved: boolean;
    scoreGained: number;
    score: number;
    highScore: number;
    gameOver: boolean;
    won: boolean;
    boardState: BoardState;
    deltas: TileMove[];
}

interface GameState {
    gameId: string | null;
    boardState: BoardState | null;
    score: number;
    highScore: number;
    gameOver: boolean;
    won: boolean;
    aiSuggestion: Direction | null;
    status: 'idle' | 'playing' | 'ended';
    lastMove: MoveResult | null; // For animation
}

const initialState: GameState = {
    gameId: null,
    boardState: null,
    score: 0,
    highScore: 0,
    gameOver: false,
    won: false,
    aiSuggestion: null,
    status: 'idle',
    lastMove: null,
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
            state.lastMove = null;
        },
        updateGameState(state, action: PayloadAction<MoveResult>) {
            state.lastMove = action.payload;
            if (action.payload.boardState) {
                state.boardState = action.payload.boardState;
            }
            state.score = action.payload.score;
            state.highScore = action.payload.highScore;
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
