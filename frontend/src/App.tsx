import { useEffect, useState, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Client } from '@stomp/stompjs';
import type { RootState, AppDispatch } from './store/store';
import { setGameId, updateGameState, setAiSuggestion, resetGame } from './store/gameSlice';
import type { Direction } from './store/gameSlice';
import { startNewGame, makeMove, requestAiSuggestion, getCurrentGame, getClientId, undoMove } from './services/api';
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
  const pendingMoveRef = useRef<Direction | null>(null);
  const wsConnected = useRef(false);
  const clientId = getClientId();

  useEffect(() => {
    const resumeSession = async () => {
      const activeGame = await getCurrentGame();
      if (activeGame) {
        dispatch(setGameId(clientId));
        dispatch(updateGameState(activeGame));
      }
    };
    resumeSession();
  }, [dispatch, clientId]);

  const handleStartGame = async (initialDirection?: Direction) => {
    if (isStarting.current) return;
    isStarting.current = true;
    try {
      if (initialDirection) {
        pendingMoveRef.current = initialDirection;
      }
      await startNewGame();
      dispatch(setGameId(clientId));
    } catch (e) {
      console.error(e);
    } finally {
      isStarting.current = false;
    }
  };

  useEffect(() => {
    if (game.gameId) {
      wsConnected.current = false;
      const client = new Client({
        brokerURL: `ws://${window.location.host}/ws-game`,
        onConnect: () => {
          console.log('Connected to WS');
          client.subscribe(`/topic/game/${clientId}`, (message) => {
            const event = JSON.parse(message.body);
            if (event.type === 'STARTED') {
              if (event.payload) {
                dispatch(updateGameState({
                  direction: "UP" as Direction,
                  moved: true,
                  scoreGained: 0,
                  score: 0,
                  gameOver: false,
                  won: false,
                  boardState: event.payload,
                  deltas: []
                }));
              }
            } else if (event.type === 'MOVE') {
              dispatch(updateGameState(event.payload));
            } else if (event.type === 'AI_SUGGESTION') {
              dispatch(setAiSuggestion(event.payload));
            }
          });
          
          wsConnected.current = true;
          
          if (pendingMoveRef.current) {
             makeMove(pendingMoveRef.current);
             pendingMoveRef.current = null;
          }
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
      if (e.key.toLowerCase() === 'n' && game.gameId) {
        dispatch(resetGame());
        return;
      }
      if (e.key.toLowerCase() === 'a' && game.gameId && game.status === 'playing') {
        handleAi();
        return;
      }
      if (e.key.toLowerCase() === 'u' && game.gameId && game.status === 'playing') {
        handleUndo();
        return;
      }

      let direction: Direction | null = null;
      if (e.key === 'ArrowUp') direction = 'UP';
      else if (e.key === 'ArrowDown') direction = 'DOWN';
      else if (e.key === 'ArrowLeft') direction = 'LEFT';
      else if (e.key === 'ArrowRight') direction = 'RIGHT';

      if (!direction) return;

      e.preventDefault();

      if (game.status !== 'playing' || !game.gameId) {
        handleStartGame(direction);
        return;
      }

      if (!wsConnected.current) return;

      makeMove(direction);
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [game.gameId, game.status]);

  const handleAi = () => {
    if (game.gameId) {
      requestAiSuggestion();
    }
  };

  const handleUndo = async () => {
    if (game.gameId) {
      const result = await undoMove();
      if (result) {
        dispatch(updateGameState(result));
      }
    }
  };

  return (
    <GameContainer>
      <GameHeader 
        score={game.score} 
        gameId={game.gameId} 
        onReset={() => dispatch(resetGame())} 
        onOpenSettings={() => setShowSettings(true)}
      />

      <BoardWrapper>
        {(game.gameOver || game.won) && <GameOverOverlay won={game.won} />}
        {!game.gameId && <StartOverlay />}
        
        <GameGrid boardState={game.boardState} />
      </BoardWrapper>

      <div className="mt-4 text-xs font-bold text-slate-400 uppercase tracking-widest flex gap-4">
          <span>(Arrows) Move</span>
          <span>(A) AI</span>
          <span>(U) Undo</span>
          <span>(N) New</span>
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
