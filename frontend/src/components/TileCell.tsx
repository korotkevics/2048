import { useEffect, useState, useRef } from 'react';
import { CrownIcon } from './Icons';
import { getCellClass } from '../utils/styleUtils';

export const TileCell = ({ value }: { value: number }) => {
  const [merged, setMerged] = useState(false);
  const prevValue = useRef(value);

  useEffect(() => {
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
