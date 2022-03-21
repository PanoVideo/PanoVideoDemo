import electron from 'electron';

export default {
  /**
   * 发送消息到主窗口
   * @param data
   */
  sendToMainWindow(data: { command: string; payload?: any }) {
    return electron.ipcRenderer.invoke('messageMainWindow', data);
  },
  /**
   * 发送消息到共享控制窗口
   * @param data
   */
  sendToShareCtrlWindow(data: { command: string; payload?: any }) {
    return electron.ipcRenderer.invoke('messageToShareCtrlWindow', data);
  },
  /**
   * 发送消息到electron主进程
   * @param data
   */
  sendToMainProcess(data: { command: string; payload?: any }) {
    return electron.ipcRenderer.invoke('messageToMainProcess', data);
  },
};
