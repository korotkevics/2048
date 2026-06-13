import type { ReactNode } from 'react';

export const GameContainer = ({ children }: { children: ReactNode }) => {
  return (
    <div className="min-h-screen w-full flex justify-center relative bg-transparent overflow-x-hidden">
      {/* 
          The White Strip:
          Even narrower (max-w-2xl) to maximize background visibility.
          Solid white in the center, smoothly fading to transparent on the sides.
      */}
      <div 
        className="relative w-[75%] max-w-2xl min-h-screen bg-white flex flex-col items-center justify-center p-4 font-sans text-[#002244]"
        style={{
          maskImage: 'linear-gradient(to right, transparent 0%, black 10%, black 90%, transparent 100%)',
          WebkitMaskImage: 'linear-gradient(to right, transparent 0%, black 10%, black 90%, transparent 100%)'
        }}
      >
        <div className="w-full max-w-md flex flex-col items-center">
            {children}
        </div>
      </div>
    </div>
  );
};
