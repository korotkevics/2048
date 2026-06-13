import { useState, useEffect } from 'react';
import { getSettings, updateSettings } from '../services/api';
import type { Settings } from '../services/api';

interface SettingsModalProps {
  onClose: () => void;
}

export function SettingsModal({ onClose }: SettingsModalProps) {
  const [settings, setSettings] = useState<Settings | null>(null);

  useEffect(() => {
    const fetchAndSyncSettings = async () => {
      try {
        const backendSettings = await getSettings();
        const localSettingsStr = localStorage.getItem('play2048_settings');
        
        let mergedSettings = backendSettings;

        if (localSettingsStr) {
          const localSettings = JSON.parse(localSettingsStr) as Settings;
          const backendMajor = backendSettings.version.split('.')[0];
          const localMajor = localSettings?.version?.split('.')[0];

          if (backendMajor !== localMajor) {
            // Major version mismatch: discard local, use backend
            console.log('Settings version major mismatch. Resetting local settings.');
          } else {
            // Compatible: merge, keeping local preferences, updating version
            mergedSettings = { ...localSettings, version: backendSettings.version };
            // Push local preferences to backend to ensure they are active
            await updateSettings({ 
                aiType: mergedSettings.aiType, 
                initialTileCount: mergedSettings.initialTileCount,
                tileProbabilities: mergedSettings.tileProbabilities
            });
          }
        }
        
        localStorage.setItem('play2048_settings', JSON.stringify(mergedSettings));
        setSettings(mergedSettings);
      } catch (e) {
        console.error("Failed to load settings", e);
      }
    };

    fetchAndSyncSettings();
  }, []);

  const handleChangeAiType = async (type: string) => {
    if (!settings) return;
    const newSettings = { ...settings, aiType: type };
    setSettings(newSettings);
    localStorage.setItem('play2048_settings', JSON.stringify(newSettings));
    await updateSettings({ aiType: type });
  };

  const handleChangeInitialCount = async (count: number) => {
    if (!settings) return;
    const newSettings = { ...settings, initialTileCount: count };
    setSettings(newSettings);
    localStorage.setItem('play2048_settings', JSON.stringify(newSettings));
    await updateSettings({ initialTileCount: count });
  };

  const handleUpdateProbabilities = async (newProbs: Record<string, number>) => {
    if (!settings) return;
    const newSettings = { ...settings, tileProbabilities: newProbs };
    setSettings(newSettings);
    localStorage.setItem('play2048_settings', JSON.stringify(newSettings));
    
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
    const newProbs = { ...settings.tileProbabilities };
    let val = 2;
    while(newProbs[String(val)] !== undefined && val <= 2048) {
       val *= 2;
    }
    if (val <= 2048) {
       newProbs[String(val)] = 0.0;
       handleUpdateProbabilities(newProbs);
    }
  };

  if (!settings) return null;

  const currentSum = Object.values(settings.tileProbabilities || {}).reduce((a, b) => a + b, 0);
  const isValidSum = Math.abs(currentSum - 1.0) < 0.001;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-[#faf8ef] p-6 rounded-lg shadow-xl w-96 text-[#776e65]">
        <h2 className="text-2xl font-bold mb-4">Settings</h2>
        <div className="mb-4">
            <span className="text-sm font-bold opacity-80 block mb-2">Version: {settings.version}</span>
        </div>
        
        <div className="mb-4">
          <label className="block text-sm font-bold mb-2">AI Strategy</label>
          <select 
            className="w-full p-2 border rounded bg-white"
            value={settings.aiType} 
            onChange={(e) => handleChangeAiType(e.target.value)}
          >
            <option value="DETERMINISTIC">Deterministic (Fast)</option>
            <option value="LLM">LLM (Ollama / LLaMA3)</option>
          </select>
        </div>

        <div className="mb-4">
          <label className="block text-sm font-bold mb-2">Initial Tile Count</label>
          <select 
            className="w-full p-2 border rounded bg-white"
            value={settings.initialTileCount} 
            onChange={(e) => handleChangeInitialCount(Number(e.target.value))}
          >
            <option value={2}>2</option>
            <option value={4}>4</option>
            <option value={6}>6</option>
            <option value={8}>8</option>
          </select>
        </div>

        <div className="mb-6">
          <div className="flex justify-between items-center mb-2">
            <label className="block text-sm font-bold">Tile Probabilities</label>
            <button onClick={handleAddProb} className="text-xs bg-[#8f7a66] text-white px-2 py-1 rounded hover:bg-[#9f8b77]">Add Tile</button>
          </div>
          <div className="max-h-40 overflow-y-auto border rounded p-2 bg-white">
             {Object.entries(settings.tileProbabilities || {}).map(([val, prob]) => (
               <div key={val} className="flex items-center gap-2 mb-2">
                 <span className="font-bold w-12 text-right">{val} :</span>
                 <input 
                   type="number" 
                   step="0.05"
                   min="0"
                   max="1"
                   className="border rounded w-20 p-1 flex-1 text-center"
                   value={prob}
                   onChange={(e) => handleProbChange(val, parseFloat(e.target.value))}
                 />
                 <button onClick={() => handleRemoveProb(val)} className="text-red-500 font-bold ml-2 w-6 text-center hover:bg-red-100 rounded">X</button>
               </div>
             ))}
             {Object.keys(settings.tileProbabilities || {}).length === 0 && (
                 <div className="text-sm opacity-70 text-center py-2">No tiles configured.</div>
             )}
          </div>
          {!isValidSum && (
            <p className="text-xs mt-1 text-red-500 font-bold">Sum must be 1.0 (currently {currentSum.toFixed(2)}). Not saved to server.</p>
          )}
        </div>

        <button 
          onClick={onClose}
          className="w-full bg-[#8f7a66] text-white font-bold py-2 px-4 rounded hover:bg-[#9f8b77] transition-colors"
        >
          Close
        </button>
      </div>
    </div>
  );
}
