import { useState, useEffect } from 'react';
import { getSettings, updateSettings } from '../services/api';
import type { Settings } from '../services/api';

interface SettingsModalProps {
  onClose: () => void;
}

const ALLOWED_TILES = [2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048];

export function SettingsModal({ onClose }: SettingsModalProps) {
  const [settings, setSettings] = useState<Settings | null>(null);
  const [selectedAddValue, setSelectedAddValue] = useState<number>(2);

  useEffect(() => {
    const fetchSettings = async () => {
      try {
        const backendSettings = await getSettings();
        setSettings(backendSettings);
      } catch (e) {
        console.error("Failed to load settings", e);
      }
    };

    fetchSettings();
  }, []);

  const handleChangeAiType = async (type: string) => {
    if (!settings) return;
    const newSettings = { ...settings, aiType: type };
    setSettings(newSettings);
    await updateSettings({ aiType: type });
  };

  const handleChangeInitialCount = async (count: number) => {
    if (!settings) return;
    const newSettings = { ...settings, initialTileCount: count };
    setSettings(newSettings);
    await updateSettings({ initialTileCount: count });
  };

  const handleUpdateProbabilities = async (newProbs: Record<string, number>) => {
    if (!settings) return;
    const newSettings = { ...settings, tileProbabilities: newProbs };
    setSettings(newSettings);
    
    // Only send to backend if sum is exactly 1.0 to avoid 500 errors
    const sum = Object.values(newProbs).reduce((a, b) => a + b, 0);
    if (Math.abs(sum - 1.0) < 0.001) {
       try {
           await updateSettings({ tileProbabilities: newProbs });
       } catch (e) {
           console.error("Failed to update probabilities", e);
       }
    }
  };

  const handleProbChange = (key: string, prob: number) => {
    if (!settings) return;
    const newProbs = { ...settings.tileProbabilities, [key]: prob };
    handleUpdateProbabilities(newProbs);
  };

  const handleRemoveProb = (key: string) => {
    if (!settings) return;
    const newProbs = { ...settings.tileProbabilities };
    delete newProbs[key];
    handleUpdateProbabilities(newProbs);
  };

  const handleAddProb = () => {
    if (!settings) return;
    const valStr = String(selectedAddValue);
    if (settings.tileProbabilities[valStr] !== undefined) return;

    const newProbs = { ...settings.tileProbabilities, [valStr]: 0.0 };
    handleUpdateProbabilities(newProbs);
  };

  if (!settings) return null;

  const currentSum = Object.values(settings.tileProbabilities || {}).reduce((a, b) => a + b, 0);
  const isValidSum = Math.abs(currentSum - 1.0) < 0.001;

  return (
    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white p-6 rounded-2xl shadow-2xl w-full max-w-sm text-[#002244] transform transition-all">
        <h2 className="text-3xl font-extrabold mb-1 tracking-tight text-[#002244]">Settings</h2>
        <div className="mb-6">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block">Version {settings.version}</span>
        </div>
        
        <div className="mb-5">
          <label className="block text-sm font-bold text-slate-700 mb-2">AI Strategy</label>
          <select 
            className="w-full p-2.5 border border-slate-300 rounded-lg bg-slate-50 text-[#002244] font-medium focus:ring-2 focus:ring-[#00509a] focus:border-[#00509a] transition-shadow outline-none"
            value={settings.aiType} 
            onChange={(e) => handleChangeAiType(e.target.value)}
          >
            <option value="DETERMINISTIC">Algo based AI</option>
            <option value="LLM">LLM based AI</option>
          </select>
        </div>

        <div className="mb-5">
          <label className="block text-sm font-bold text-slate-700 mb-2">Initial Tile Count</label>
          <select 
            className="w-full p-2.5 border border-slate-300 rounded-lg bg-slate-50 text-[#002244] font-medium focus:ring-2 focus:ring-[#00509a] focus:border-[#00509a] transition-shadow outline-none"
            value={settings.initialTileCount} 
            onChange={(e) => handleChangeInitialCount(Number(e.target.value))}
          >
            <option value={0}>Random (2-6)</option>
            <option value={2}>2</option>
            <option value={4}>4</option>
            <option value={6}>6</option>
            <option value={8}>8</option>
          </select>
        </div>

        <div className="mb-8">
          <div className="flex gap-2 items-center mb-3">
            <label className="block text-sm font-bold text-slate-700 flex-1">Tile Probabilities</label>
            <select 
              className="text-xs p-1 border border-slate-300 rounded bg-white outline-none focus:ring-1 focus:ring-[#00509a]"
              value={selectedAddValue}
              onChange={(e) => setSelectedAddValue(Number(e.target.value))}
            >
              {ALLOWED_TILES.map(v => (
                <option key={v} value={v} disabled={settings.tileProbabilities[String(v)] !== undefined}>
                  {v}
                </option>
              ))}
            </select>
            <button onClick={handleAddProb} className="text-xs bg-[#00509a] text-white px-3 py-1.5 rounded-md hover:bg-[#003d7a] transition-colors font-bold shadow-sm">Add</button>
          </div>
          <div className="max-h-48 overflow-y-auto border border-slate-200 rounded-lg p-3 bg-slate-50 shadow-inner">
             {Object.entries(settings.tileProbabilities || {}).map(([val, prob]) => (
               <div key={val} className="flex items-center gap-3 mb-2 last:mb-0">
                 <div className="w-12 h-8 bg-[#002244] text-white font-bold rounded flex items-center justify-center text-sm shadow-sm">{val}</div>
                 <input 
                   type="number" 
                   step="0.05"
                   min="0"
                   max="1"
                   className="border border-slate-300 rounded-md w-24 p-1.5 flex-1 text-center font-medium text-[#002244] focus:ring-2 focus:ring-[#00509a] outline-none"
                   value={prob}
                   onChange={(e) => handleProbChange(val, parseFloat(e.target.value))}
                 />
                 <button onClick={() => handleRemoveProb(val)} className="text-red-500 hover:text-white font-bold ml-1 w-8 h-8 flex items-center justify-center hover:bg-red-500 rounded transition-colors" title="Remove">✕</button>
               </div>
             ))}
             {Object.keys(settings.tileProbabilities || {}).length === 0 && (
                 <div className="text-sm text-slate-500 font-medium text-center py-4">No tiles configured.</div>
             )}
          </div>
          {!isValidSum && (
            <p className="text-xs mt-2 text-red-600 font-bold bg-red-50 p-2 rounded border border-red-100 flex items-center">
               <span className="mr-1">⚠️</span> Sum must be 1.0 (currently {currentSum.toFixed(2)}). Not saved.
            </p>
          )}
        </div>

        <button 
          onClick={onClose}
          className="w-full bg-[#002244] text-white font-bold py-3 px-4 rounded-lg shadow-md hover:bg-[#001833] transition-colors focus:ring-2 focus:ring-offset-2 focus:ring-[#002244]"
        >
          Close
        </button>
      </div>
    </div>
  );
}
