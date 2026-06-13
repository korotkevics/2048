import { useEffect, useState, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Client } from '@stomp/stompjs';
import type { RootState, AppDispatch } from './store/store';
import { setGameId, updateGameState, setAiSuggestion } from './store/gameSlice';
import type { Direction } from './store/gameSlice';
import { startNewGame, makeMove, requestAiSuggestion } from './services/api';
import { SettingsModal } from './components/SettingsModal';
import './index.css';

const CrownIcon = () => (
  <svg viewBox="0 0 24 24" fill="currentColor" className="w-12 h-12 text-yellow-400 drop-shadow-[0_2px_4px_rgba(0,0,0,0.4)]">
    <path d="M5 16L3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5zm14 3c0 .6-.4 1-1 1H6c-.6 0-1-.4-1-1v-1h14v1z"/>
  </svg>
);

const TileCell = ({ value }: { value: number }) => {
  const [merged, setMerged] = useState(false);
  const prevValue = useRef(value);

  useEffect(() => {
    // If the value increased and the previous value was not 0, assume it was a merge or a larger tile slid in
    // Either way, it's a "collapse/merge" or progression effect that feels good to the user
    if (value > prevValue.current && prevValue.current !== 0) {
      setMerged(true);
      const timer = setTimeout(() => setMerged(false), 600);
      return () => clearTimeout(timer);
    }
    prevValue.current = value;
  }, [value]);

  return (
    <div className={`w-20 h-20 relative flex items-center justify-center rounded-lg text-3xl font-bold transition-all duration-200 ${getCellClass(value)}`}>
      {value !== 0 ? value : ''}
      {merged && (
        <div className="absolute inset-0 flex items-center justify-center animate-flash-crown pointer-events-none z-20">
           <CrownIcon />
        </div>
      )}
    </div>
  );
};

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

  const isStarting = useRef(false);

  const handleStartGame = async (initialDirection?: Direction) => {
    if (isStarting.current) return;
    isStarting.current = true;
    try {
      const id = await startNewGame();
      dispatch(setGameId(id));
      if (initialDirection) {
        await makeMove(id, initialDirection);
      }
    } catch (e) {
      console.error(e);
    } finally {
      isStarting.current = false;
    }
  };

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      let direction: Direction | null = null;
      if (e.key === 'ArrowUp') direction = 'UP';
      else if (e.key === 'ArrowDown') direction = 'DOWN';
      else if (e.key === 'ArrowLeft') direction = 'LEFT';
      else if (e.key === 'ArrowRight') direction = 'RIGHT';

      if (!direction) return;

      e.preventDefault(); // Prevent scrolling

      if (game.status !== 'playing' || !game.gameId) {
        handleStartGame(direction);
        return;
      }

      makeMove(game.gameId, direction);
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [game.gameId, game.status]);

  const handleAi = () => {
    if (game.gameId) {
      requestAiSuggestion(game.gameId);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-4 font-sans">
      <div className="w-full max-w-md flex justify-between items-center mb-4">
        <h1 className="text-5xl font-extrabold text-[#002244] tracking-tight">2048</h1>
        <button 
          onClick={() => setShowSettings(true)}
          className="text-3xl text-[#002244] hover:opacity-70 transition-opacity"
          title="Settings"
        >
          ⚙️
        </button>
      </div>
      
      <div className="flex w-full max-w-md justify-between items-center mb-8">
        <div className="flex gap-4">
          <div className="bg-[#00509a] rounded-lg p-2 px-6 text-white text-center shadow-md">
            <div className="text-xs font-bold tracking-widest opacity-90 uppercase">Score</div>
            <div className="text-2xl font-bold">{game.score}</div>
          </div>
        </div>
      </div>

      <div className="bg-[#002244] p-4 rounded-xl shadow-2xl relative">
        {(game.gameOver || game.won) && (
          <div className="absolute inset-0 bg-slate-50/90 backdrop-blur-sm flex flex-col items-center justify-center z-10 rounded-xl">
            <h2 className="text-4xl font-extrabold mb-2 text-[#002244]">{game.won ? "You Win!" : "Game Over!"}</h2>
            <p className="text-[#00509a] font-bold text-xl mt-4 animate-pulse">Press any Arrow Key to restart</p>
          </div>
        )}

        {!game.gameId && (
          <div className="absolute inset-0 flex items-center justify-center z-10 pointer-events-none rounded-xl">
            <p className="text-white/40 font-bold tracking-widest uppercase animate-pulse">Press Arrow Key</p>
          </div>
        )}
        
        <div className="grid grid-cols-4 gap-3 bg-[#002244]">
          {game.boardState ? (
            game.boardState.grid.map((row, rIdx) => 
              row.map((cell, cIdx) => (
                <TileCell key={`${rIdx}-${cIdx}`} value={cell} />
              ))
            )
          ) : (
             Array(16).fill(0).map((_, i) => (
               <TileCell key={i} value={0} />
             ))
          )}
        </div>
      </div>

      <div className="mt-8 text-center max-w-md w-full">
        <div className="flex justify-between items-center mb-6">
           <p className="text-[#002244] font-medium opacity-80">Use arrow keys to join the numbers.</p>
           <button onClick={handleAi} disabled={!game.gameId || game.status !== 'playing'} className="bg-[#00509a] text-white font-bold py-2 px-6 rounded-lg shadow-md hover:bg-[#003d7a] transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
             Ask AI
           </button>
        </div>
        {game.aiSuggestion && (
          <div className="bg-white border-l-4 border-[#00509a] text-[#002244] p-4 rounded-r-lg shadow-md font-bold text-left flex items-center">
            <span className="text-[#00509a] mr-2">🤖 AI Suggests:</span> {game.aiSuggestion}
          </div>
        )}
      </div>

      {showSettings && <SettingsModal onClose={() => setShowSettings(false)} />}
    </div>
  );
}

function getCellClass(value: number) {
  if (value === 0) return 'bg-[#003366] text-transparent';
  if (value === 2) return 'bg-[#f8fafc] text-[#002244] shadow-sm';
  if (value === 4) return 'bg-[#e2e8f0] text-[#002244] shadow-sm';
  if (value === 8) return 'bg-[#cbd5e1] text-[#002244] shadow-sm';
  if (value === 16) return 'bg-[#94a3b8] text-white shadow-md';
  if (value === 32) return 'bg-[#64748b] text-white shadow-md';
  if (value === 64) return 'bg-[#475569] text-white shadow-md';
  if (value === 128) return 'bg-[#00509a] text-white shadow-lg text-2xl';
  if (value === 256) return 'bg-[#004080] text-white shadow-lg text-2xl';
  if (value === 512) return 'bg-[#003060] text-white shadow-lg text-2xl';
  if (value === 1024) return 'bg-[#eab308] text-[#002244] text-xl shadow-[0_0_15px_rgba(234,179,8,0.4)] ring-1 ring-yellow-400';
  if (value === 2048) return 'bg-[#ca8a04] text-white text-xl shadow-[0_0_20px_rgba(202,138,4,0.6)] ring-2 ring-yellow-500';
  return 'bg-[#1e293b] text-white text-xl shadow-xl';
}

export default App;
