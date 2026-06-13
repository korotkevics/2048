import { useEffect, useState, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Client } from '@stomp/stompjs';
import type { RootState, AppDispatch } from './store/store';
import { setGameId, updateGameState, setAiSuggestion, resetGame } from './store/gameSlice';
import type { Direction } from './store/gameSlice';
import { startNewGame, makeMove, requestAiSuggestion, getCurrentGame, getClientId } from './services/api';
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

  const isStarting = useRef(false);
  const wsConnected = useRef(false);
  const clientId = getClientId();

  const handleStartAndMove = async (direction: Direction) => {
    if (isStarting.current) return;
    isStarting.current = true;
    try {
      console.log('[App] Starting and moving:', direction);
      // 1. Create the game
      await startNewGame();
      dispatch(setGameId(clientId));
      
      // 2. We could dispatch startResult here to show the tiles for a split second,
      // but usually we want to just show the moved state.
      
      // 3. Immediately execute the move
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
    console.log('[App] Resetting to idle');
    dispatch(resetGame());
  };

  useEffect(() => {
    const initSession = async () => {
      console.log('[App] Checking for existing session...');
      const activeGame = await getCurrentGame();
      if (activeGame) {
        console.log('[App] Found active game, resuming.');
        dispatch(setGameId(clientId));
        dispatch(updateGameState(activeGame));
      } else {
        console.log('[App] No active game. Showing start screen.');
      }
    };
    initSession();
  }, [dispatch, clientId]);

  useEffect(() => {
    if (game.gameId) {
      const client = new Client({
        brokerURL: `ws://${window.location.host}/ws-game`,
        onConnect: () => {
          console.log('[WS] Connected to channel:', clientId);
          client.subscribe(`/topic/game/${clientId}`, (message) => {
            const event = JSON.parse(message.body);
            if (event.type === 'MOVE') {
              dispatch(updateGameState(event.payload));
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

      if (!direction) return;
      e.preventDefault();

      // If game not started or in idle state, start AND move
      if (!game.gameId || game.status === 'idle') {
        handleStartAndMove(direction);
        return;
      }

      if (game.status !== 'playing' || !game.boardState) return;

      // Regular move
      makeMove(direction).then(res => {
        if (res) dispatch(updateGameState(res));
      });
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [game.gameId, game.status, game.boardState]);

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
        {!game.boardState && <StartOverlay />}
        
        <GameGrid boardState={game.boardState} />
      </BoardWrapper>

      <div className="mt-4 text-xs font-bold text-slate-400 uppercase tracking-widest flex gap-4">
          <span>(Arrows) Move</span>
          <span>(A) AI</span>
          <span>(N) New Game</span>
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
