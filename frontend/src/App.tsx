import { useEffect, useState, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Client } from '@stomp/stompjs';
import type { RootState, AppDispatch } from './store/store';
import { setGameId, updateGameState, setAiSuggestion, resetGame } from './store/gameSlice';
import type { Direction } from './store/gameSlice';
import { startNewGame, makeMove, requestAiSuggestion, getCurrentGame, getClientId, startAutoPlay, stopAutoPlay, undoMove } from './services/api';
import { SettingsModal } from './components/SettingsModal';
import { GameHeader } from './components/GameHeader';
import { GameGrid } from './components/GameGrid';
import { GameControls } from './components/GameControls';
import { GameOverOverlay, StartOverlay } from './components/Overlays';
import { GameContainer } from './components/GameContainer';
import { BoardWrapper } from './components/BoardWrapper';
import './index.css';

function App() {
  const dispatch = useDispatch<AppDispatch>();
  const game = useSelector((state: RootState) => state.game);
  const [showSettings, setShowSettings] = useState(false);
  const [isAutoPlaying, setIsAutoPlaying] = useState(false);

  const isStarting = useRef(false);
  const wsConnected = useRef(false);
  const clientId = getClientId();

  const handleStartAndMove = async (direction: Direction) => {
    if (isStarting.current) return;
    isStarting.current = true;
    try {
      await startNewGame();
      dispatch(setGameId(clientId));
      const moveResult = await makeMove(direction);
      if (moveResult) {
        dispatch(updateGameState(moveResult));
      }
    } catch (e) {
      console.error('[App] Failed to start and move:', e);
    } finally {
      isStarting.current = false;
    }
  };

  const handleReset = async () => {
    handleStopAutoPlay();
    dispatch(resetGame());
  };

  const handleStartAutoPlay = async () => {
    setIsAutoPlaying(true);
    await startAutoPlay();
  };

  const handleStopAutoPlay = async () => {
    setIsAutoPlaying(false);
    await stopAutoPlay();
  };

  const handleUndo = async () => {
    if (game.gameId && !isAutoPlaying) {
      const result = await undoMove();
      if (result) {
        dispatch(updateGameState(result));
      }
    }
  };

  useEffect(() => {
    const initSession = async () => {
      const activeGame = await getCurrentGame();
      if (activeGame) {
        dispatch(setGameId(clientId));
        dispatch(updateGameState(activeGame));
      }
    };
    initSession();
  }, [dispatch, clientId]);

  useEffect(() => {
    if (game.gameId) {
      const client = new Client({
        brokerURL: `ws://${window.location.host}/ws-game`,
        onConnect: () => {
          client.subscribe(`/topic/game/${clientId}`, (message) => {
            const event = JSON.parse(message.body);
            if (event.type === 'MOVE') {
              dispatch(updateGameState(event.payload));
              if (event.payload.gameOver || event.payload.won) {
                setIsAutoPlaying(false);
              }
            } else if (event.type === 'AI_SUGGESTION') {
              dispatch(setAiSuggestion(event.payload));
            }
          });
          wsConnected.current = true;
        },
      });
      client.activate();
      return () => {
        wsConnected.current = false;
        client.deactivate();
      };
    }
  }, [game.gameId, dispatch, clientId]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key.toLowerCase() === 'n') {
        handleReset();
        return;
      }
      
      let direction: Direction | null = null;
      if (e.key === 'ArrowUp') direction = 'UP';
      else if (e.key === 'ArrowDown') direction = 'DOWN';
      else if (e.key === 'ArrowLeft') direction = 'LEFT';
      else if (e.key === 'ArrowRight') direction = 'RIGHT';

      if (direction) {
        e.preventDefault();
        if (isAutoPlaying) return; 

        if (!game.gameId || game.status === 'idle') {
          handleStartAndMove(direction);
          return;
        }

        if (game.status === 'playing' && game.boardState) {
          makeMove(direction).then(res => {
            if (res) dispatch(updateGameState(res));
          });
        }
        return;
      }

      if (e.key.toLowerCase() === 'u') {
        e.preventDefault();
        handleUndo();
      } else if (e.key.toLowerCase() === 'a') {
        e.preventDefault();
        handleAi();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [game.gameId, game.status, game.boardState, isAutoPlaying]);

  const handleAi = () => {
    if (game.gameId) {
      requestAiSuggestion();
    }
  };

  return (
    <GameContainer>
      <GameHeader 
        score={game.score} 
        highScore={game.highScore}
        gameId={game.gameId} 
        onReset={handleReset} 
        onOpenSettings={() => setShowSettings(true)}
      />

      <BoardWrapper>
        {(game.gameOver || game.won) && <GameOverOverlay won={game.won} />}
        {(!game.boardState) && <StartOverlay />}
        
        <GameGrid boardState={game.boardState} />
      </BoardWrapper>

      <div className="mt-4 text-xs font-bold text-slate-400 uppercase tracking-widest flex flex-col items-center gap-4">
          <div className="flex gap-4">
            <button onClick={handleUndo} className="hover:text-slate-600 transition-colors">(U) Undo</button>
            <button onClick={handleAi} className="hover:text-slate-600 transition-colors">(A) AI Suggest</button>
            <button onClick={handleReset} className="hover:text-slate-600 transition-colors">(N) New Game</button>
          </div>
          
          <button 
            onClick={isAutoPlaying ? handleStopAutoPlay : handleStartAutoPlay}
            className={`px-6 py-2 rounded-full font-black text-sm transition-all shadow-lg ${
              isAutoPlaying 
                ? "bg-red-500 text-white hover:bg-red-600 animate-pulse" 
                : "bg-[#002244] text-white hover:bg-[#003366]"
            }`}
          >
            {isAutoPlaying ? "STOP AUTO-PLAY" : "START AUTO-PLAY"}
          </button>
      </div>

      <GameControls 
        gameId={game.gameId}
        status={game.status}
        aiSuggestion={game.aiSuggestion}
        onAiRequest={handleAi}
      />

      {showSettings && <SettingsModal onClose={() => setShowSettings(false)} />}
    </GameContainer>
  );
}

export default App;
