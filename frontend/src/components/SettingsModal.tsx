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
          const localMajor = localSettings.version.split('.')[0];

          if (backendMajor !== localMajor) {
            // Major version mismatch: discard local, use backend
            console.log('Settings version major mismatch. Resetting local settings.');
          } else {
            // Compatible: merge, keeping local preferences, updating version
            mergedSettings = { ...localSettings, version: backendSettings.version };
            // Push local preferences to backend to ensure they are active
            await updateSettings({ aiType: mergedSettings.aiType, initialTileCount: mergedSettings.initialTileCount });
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

  if (!settings) return null;

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

        <div className="mb-6">
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
