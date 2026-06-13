interface GameOverOverlayProps {
  won: boolean;
}

export const GameOverOverlay = ({ won }: GameOverOverlayProps) => {
  return (
    <div className="absolute inset-0 bg-slate-50/90 backdrop-blur-sm flex flex-col items-center justify-center z-10 rounded-xl">
      <h2 className="text-4xl font-extrabold mb-2 text-[#002244]">{won ? "You Win!" : "Game Over!"}</h2>
      <p className="text-[#00509a] font-bold text-xl mt-4 animate-pulse">Press any Arrow Key to restart</p>
    </div>
  );
};

export const StartOverlay = () => {
  return (
    <div className="absolute inset-0 flex items-center justify-center z-10 pointer-events-none rounded-xl">
      <p className="text-white/40 font-bold tracking-widest uppercase animate-pulse">Press Arrow Key</p>
    </div>
  );
};
