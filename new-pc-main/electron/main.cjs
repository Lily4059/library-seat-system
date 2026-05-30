const { app, BrowserWindow, Menu, shell, dialog } = require('electron')
const path = require('path')
const { spawn } = require('child_process')

let mainWindow = null
let serverProcess = null

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 1000,
    minHeight: 700,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      webSecurity: true
    },
    title: 'Library Seat System',
    show: false
  })

  mainWindow.once('ready-to-show', () => {
    mainWindow.show()
  })

  if (app.isPackaged) {
    mainWindow.loadFile(path.join(__dirname, '../dist/index.html'))
  } else {
    mainWindow.loadURL('http://localhost:5173')
  }

  mainWindow.on('closed', () => {
    mainWindow = null
  })

  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url)
    return { action: 'deny' }
  })

  const menuTemplate = [
    {
      label: 'File',
      submenu: [
        { label: 'Refresh', accelerator: 'CmdOrCtrl+R', click: () => mainWindow?.reload() },
        { type: 'separator' },
        { label: 'Exit', accelerator: 'CmdOrCtrl+Q', click: () => app.quit() }
      ]
    },
    {
      label: 'View',
      submenu: [
        { label: 'Zoom In', accelerator: 'CmdOrCtrl+Plus', click: () => mainWindow?.webContents?.zoomIn() },
        { label: 'Zoom Out', accelerator: 'CmdOrCtrl+-', click: () => mainWindow?.webContents?.zoomOut() },
        { label: 'Reset Zoom', accelerator: 'CmdOrCtrl+0', click: () => mainWindow?.webContents?.setZoomLevel(0) },
        { type: 'separator' },
        { label: 'Fullscreen', accelerator: 'F11', click: () => mainWindow?.setFullScreen(!mainWindow?.isFullScreen()) }
      ]
    },
    {
      label: 'Help',
      submenu: [
        { label: 'About', click: showAboutDialog }
      ]
    }
  ]

  if (!app.isPackaged) {
    menuTemplate.push({
      label: 'Dev',
      submenu: [
        { label: 'DevTools', accelerator: 'F12', click: () => mainWindow?.webContents?.openDevTools() }
      ]
    })
  }

  const menu = Menu.buildFromTemplate(menuTemplate)
  Menu.setApplicationMenu(menu)
}

function showAboutDialog() {
  dialog.showMessageBox(mainWindow, {
    type: 'info',
    title: 'About',
    message: 'Library Seat System',
    detail: 'Version: 1.0.0\n\nLibrary Seat Reservation System\n\nCopyright 2026'
  })
}

function startServer() {
  const serverPath = app.isPackaged
    ? path.join(process.resourcesPath, 'server/index.js')
    : path.join(__dirname, '../server/index.js')

  const serverDir = path.dirname(serverPath)

  serverProcess = spawn('node', [serverPath], {
    cwd: serverDir,
    stdio: ['ignore', 'pipe', 'pipe'],
    env: { ...process.env, PORT: '3000' }
  })

  serverProcess.stdout.on('data', (data) => {
    console.log(`[Server] ${data}`)
  })

  serverProcess.stderr.on('data', (data) => {
    console.error(`[Server Error] ${data}`)
  })

  serverProcess.on('error', (err) => {
    console.error('Failed to start server:', err)
  })
}

function stopServer() {
  if (serverProcess) {
    try {
      if (process.platform === 'win32') {
        spawn('taskkill', ['/pid', serverProcess.pid, '/f', '/t'])
      } else {
        serverProcess.kill('SIGTERM')
      }
    } catch (e) {
      console.error('Error stopping server:', e)
    }
    serverProcess = null
  }
}

app.whenReady().then(() => {
  startServer()

  setTimeout(() => {
    createWindow()
  }, app.isPackaged ? 2000 : 1000)

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

app.on('window-all-closed', () => {
  stopServer()
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('before-quit', () => {
  stopServer()
})

app.on('will-quit', () => {
  stopServer()
})
