import type { ReactNode } from 'react';

export const BoardWrapper = ({ children }: { children: ReactNode }) => {
  return (
    <div className="bg-[#002244] p-4 rounded-xl shadow-2xl relative">
      {children}
    </div>
  );
};
