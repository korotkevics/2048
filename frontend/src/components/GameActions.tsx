import React from 'react';

interface GameActionsProps {
  isAutoPlaying: boolean;
  onUndo: () => void;
  onAi: () => void;
  onReset: () => void;
  onToggleAutoPlay: () => void;
}

export const GameActions: React.FC<GameActionsProps> = ({
  isAutoPlaying,
  onUndo,
  onAi,
  onReset,
  onToggleAutoPlay
}) => {
  return (
    <div className="mt-4 text-xs font-bold text-slate-400 uppercase tracking-widest flex flex-col items-center gap-4">
      <div className="flex gap-4">
        <button onClick={onUndo} className="hover:text-slate-600 transition-colors uppercase">(U) Undo</button>
        <button onClick={onAi} className="hover:text-slate-600 transition-colors uppercase">(A) AI Suggest</button>
        <button onClick={onReset} className="hover:text-slate-600 transition-colors uppercase">(N) New Game</button>
      </div>

      <button
        onClick={onToggleAutoPlay}
        className={`px-6 py-2 rounded-full font-black text-sm transition-all shadow-lg ${
          isAutoPlaying
            ? "bg-red-500 text-white hover:bg-red-600 animate-pulse"
            : "bg-[#002244] text-white hover:bg-[#003366]"
        }`}
      >
        {isAutoPlaying ? "STOP AUTO-PLAY (P)" : "START AUTO-PLAY (P)"}
      </button>
    </div>
  );
};
