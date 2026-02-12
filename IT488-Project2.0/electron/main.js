const { app, BrowserWindow } = require('electron');
const path = require('path');
const { spawn } = require('child_process');

let serverProc = null;

function startServerIfPresent() {
  try {
    const serverIndex = path.resolve(__dirname, '..', 'server', 'web-api', 'index.js');
    // Only attempt to spawn if the file exists
    const fs = require('fs');
    if (fs.existsSync(serverIndex)) {
      serverProc = spawn(process.execPath, [serverIndex], {
        env: Object.assign({}, process.env, { PORT: process.env.PORT || '4000' }),
        stdio: 'inherit'
      });
      serverProc.on('exit', (code) => {
        console.log('Background API process exited with', code);
      });
    }
  } catch (e) {
    console.warn('Could not start embedded API server:', e && e.message);
  }
}

function createWindow() {
  const win = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegration: false,
      contextIsolation: true
    }
  });

  const indexPath = path.resolve(__dirname, '..', 'client', 'dist', 'index.html');
  win.loadFile(indexPath).catch((err) => {
    console.error('Failed to load client bundle:', err && err.message);
  });
}

app.on('ready', () => {
  startServerIfPresent();
  createWindow();
});

app.on('window-all-closed', () => {
  if (serverProc) {
    try { serverProc.kill(); } catch (e) { }
  }
  if (process.platform !== 'darwin') app.quit();
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow();
});
