interface GameHeaderProps {
  score: number;
  highScore: number;
  gameId: string | null;
  onReset: () => void;
  onOpenSettings: () => void;
}

export const GameHeader = ({ score, highScore, gameId, onReset, onOpenSettings }: GameHeaderProps) => {
  return (
    <>
      <div className="w-full max-w-md flex justify-between items-center mb-4">
        <h1 className="text-5xl font-extrabold text-[#002244] tracking-tight">2048</h1>
        <button 
          onClick={onOpenSettings}
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
            <div className="text-2xl font-bold">{score}</div>
          </div>
          <div className="bg-[#002244] rounded-lg p-2 px-6 text-white text-center shadow-md">
            <div className="text-xs font-bold tracking-widest opacity-90 uppercase">Best</div>
            <div className="text-2xl font-bold">{highScore}</div>
          </div>
        </div>
        
        {gameId && (
          <button 
            onClick={onReset}
            className="bg-[#00509a] text-white font-bold py-3 px-6 rounded-lg shadow-md hover:bg-[#003d7a] transition-colors focus:ring-2 focus:ring-[#00509a] focus:ring-offset-2 focus:ring-offset-slate-50"
            title="New Game (N)"
          >
            New Game <span className="opacity-60 font-normal text-xs ml-1">(N)</span>
          </button>
        )}
      </div>
    </>
  );
};
