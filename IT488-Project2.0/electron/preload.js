// Preload script — expose safe APIs here if needed
const { contextBridge } = require('electron');

contextBridge.exposeInMainWorld('__ats_bridge__', {
  // placeholder for future IPC bridges
});
