interface GameControlsProps {
  gameId: string | null;
  status: string;
  aiSuggestion: string | null;
  onAiRequest: () => void;
}

export const GameControls = ({ gameId, status, aiSuggestion, onAiRequest }: GameControlsProps) => {
  return (
    <div className="mt-8 text-center max-w-md w-full">
      <div className="flex justify-between items-center mb-6">
         <p className="text-[#002244] font-medium opacity-80">Use arrow keys to join the numbers.</p>
         <button 
           onClick={onAiRequest} 
           disabled={!gameId || status !== 'playing'} 
           className="bg-[#00509a] text-white font-bold py-2 px-6 rounded-lg shadow-md hover:bg-[#003d7a] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
           title="Ask AI (A)"
         >
           Ask AI <span className="opacity-70 font-normal text-xs ml-1">(A)</span>
         </button>
      </div>
      {aiSuggestion && (
        <div className="bg-white border-l-4 border-[#00509a] text-[#002244] p-4 rounded-r-lg shadow-md font-bold text-left flex items-center">
          <span className="text-[#00509a] mr-2">🤖 AI Suggests:</span> {aiSuggestion}
        </div>
      )}
    </div>
  );
};
