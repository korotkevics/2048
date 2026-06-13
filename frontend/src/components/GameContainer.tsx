import type { ReactNode } from 'react';

export const GameContainer = ({ children }: { children: ReactNode }) => {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-4 font-sans text-[#002244]">
      {children}
    </div>
  );
};
