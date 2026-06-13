import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Client } from '@stomp/stompjs';
import type { RootState, AppDispatch } from './store/store';
import { setGameId, updateGameState, setAiSuggestion } from './store/gameSlice';
import type { Direction } from './store/gameSlice';
import { startNewGame, makeMove, requestAiSuggestion } from './services/api';
import { SettingsModal } from './components/SettingsModal';
import './index.css';

function App() {
  const dispatch = useDispatch<AppDispatch>();
  const game = useSelector((state: RootState) => state.game);
  const [showSettings, setShowSettings] = useState(false);

  useEffect(() => {
    if (game.gameId) {
      const client = new Client({
        brokerURL: `ws://${window.location.host}/ws-game`,
        onConnect: () => {
          console.log('Connected to WS');
          client.subscribe(`/topic/game/${game.gameId}`, (message) => {
            const event = JSON.parse(message.body);
            if (event.type === 'STARTED') {
              // initial board might be in event.payload
              if (event.payload) {
                dispatch(updateGameState({
                  direction: "UP" as Direction,
                  moved: true,
                  scoreGained: 0,
                  score: 0,
                  gameOver: false,
                  won: false,
                  boardState: event.payload
                }));
              }
            } else if (event.type === 'MOVE') {
              dispatch(updateGameState(event.payload));
            } else if (event.type === 'AI_SUGGESTION') {
              dispatch(setAiSuggestion(event.payload));
            }
          });
        },
      });
      client.activate();

      return () => {
        client.deactivate();
      };
    }
  }, [game.gameId, dispatch]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (game.status !== 'playing' || !game.gameId) return;

      let direction: Direction | null = null;
      if (e.key === 'ArrowUp') direction = 'UP';
      else if (e.key === 'ArrowDown') direction = 'DOWN';
      else if (e.key === 'ArrowLeft') direction = 'LEFT';
      else if (e.key === 'ArrowRight') direction = 'RIGHT';

      if (direction) {
        makeMove(game.gameId, direction);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [game.gameId, game.status]);

  const handleStartGame = async () => {
    try {
      const id = await startNewGame();
      dispatch(setGameId(id));
    } catch (e) {
      console.error(e);
    }
  };

  const handleAi = () => {
    if (game.gameId) {
      requestAiSuggestion(game.gameId);
    }
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-md flex justify-between items-center mb-4">
        <h1 className="text-5xl font-bold text-[#776e65]">2048</h1>
        <button 
          onClick={() => setShowSettings(true)}
          className="text-3xl hover:opacity-80 transition-opacity"
          title="Settings"
        >
          ⚙️
        </button>
      </div>
      
      <div className="flex w-full max-w-md justify-between items-center mb-6">
        <div className="flex gap-4">
          <div className="bg-[#bbada0] rounded p-2 px-6 text-white text-center">
            <div className="text-sm font-bold opacity-80">SCORE</div>
            <div className="text-xl font-bold">{game.score}</div>
          </div>
        </div>
        <button 
          onClick={handleStartGame}
          className="bg-[#8f7a66] text-white font-bold py-3 px-6 rounded hover:bg-[#9f8b77] transition-colors"
        >
          New Game
        </button>
      </div>

      <div className="bg-[#bbada0] p-3 rounded-lg relative">
        {(game.gameOver || game.won) && (
          <div className="absolute inset-0 bg-[#eee4da]/70 flex flex-col items-center justify-center z-10 rounded-lg">
            <h2 className="text-4xl font-bold mb-4">{game.won ? "You Win!" : "Game Over!"}</h2>
            <button 
              onClick={handleStartGame}
              className="bg-[#8f7a66] text-white font-bold py-2 px-4 rounded"
            >
              Try Again
            </button>
          </div>
        )}
        
        <div className="grid grid-cols-4 gap-3 bg-[#bbada0]">
          {game.boardState ? (
            game.boardState.grid.map((row, rIdx) => 
              row.map((cell, cIdx) => (
                <div key={`${rIdx}-${cIdx}`} className={`w-20 h-20 flex items-center justify-center rounded-sm text-3xl font-bold ${getCellClass(cell)}`}>
                  {cell !== 0 ? cell : ''}
                </div>
              ))
            )
          ) : (
             Array(16).fill(0).map((_, i) => (
               <div key={i} className="w-20 h-20 rounded-sm bg-[#cdc1b4]"></div>
             ))
          )}
        </div>
      </div>

      <div className="mt-8 text-center max-w-md w-full">
        <div className="flex justify-between items-center mb-4">
           <p className="text-[#776e65]">Use arrow keys to join the numbers.</p>
           <button onClick={handleAi} disabled={!game.gameId || game.status !== 'playing'} className="bg-[#f67c5f] text-white font-bold py-2 px-4 rounded disabled:opacity-50">
             Ask AI
           </button>
        </div>
        {game.aiSuggestion && (
          <div className="bg-[#edc22e] text-white p-3 rounded font-bold">
            AI Suggests: {game.aiSuggestion}
          </div>
        )}
      </div>

      {showSettings && <SettingsModal onClose={() => setShowSettings(false)} />}
    </div>
  );
}

function getCellClass(value: number) {
  if (value === 0) return 'bg-[#cdc1b4] text-[#776e65]';
  if (value === 2) return 'bg-[#eee4da] text-[#776e65]';
  if (value === 4) return 'bg-[#ede0c8] text-[#776e65]';
  if (value === 8) return 'bg-[#f2b179] text-white';
  if (value === 16) return 'bg-[#f59563] text-white';
  if (value === 32) return 'bg-[#f67c5f] text-white';
  if (value === 64) return 'bg-[#f65e3b] text-white';
  if (value === 128) return 'bg-[#edcf72] text-white text-2xl';
  if (value === 256) return 'bg-[#edcc61] text-white text-2xl';
  if (value === 512) return 'bg-[#edc850] text-white text-2xl';
  if (value === 1024) return 'bg-[#edc53f] text-white text-xl';
  if (value === 2048) return 'bg-[#edc22e] text-white text-xl shadow-[0_0_30px_10px_rgba(243,215,116,0.55)]';
  return 'bg-[#3c3a32] text-[#f9f6f2] text-xl';
}

export default App;
