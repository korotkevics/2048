import { TileCell } from './TileCell';
import type { BoardState } from '../store/gameSlice';

interface GameGridProps {
  boardState: BoardState | null;
}

export const GameGrid = ({ boardState }: GameGridProps) => {
  return (
    <div className="grid grid-cols-4 gap-3 bg-[#002244]">
      {boardState ? (
        boardState.grid.map((row, rIdx) => 
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
  );
};
