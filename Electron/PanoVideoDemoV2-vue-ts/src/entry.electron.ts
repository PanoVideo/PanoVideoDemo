/* eslint-disable @typescript-eslint/no-var-requires */
import electron from 'electron';
import * as remote from '@electron/remote';
import {
  RtcEngineAdapter,
  QResult,
  JoinChannelType,
  VideoProfileType,
} from '@/pano/ElectronSdkAdapter';
import ipc from '@/utils/ipc';
import { RtcMessage } from '@pano.video/panorts';

window.IS_ELECTRON = true;
window.electron = electron;
window.remote = remote;
window.RtcEngine = RtcEngineAdapter;
window.QResult = QResult;
window.JoinChannelType = JoinChannelType;
window.VideoProfileType = VideoProfileType;
window.rtcEngine = new RtcEngineAdapter();
window.process = process;

window.addEventListener('beforeunload', () => {
  window.rtcEngine.dropCall('');
  window.rtcEngine.leaveChannel();
  window.rtcWhiteboard.leaveChannel();
});

window.ipc = ipc;

const initPanoRtc = require('./pano').default;
const initApp = require('./main').default;

initPanoRtc();
initApp();

declare global {
  interface Window {
    IS_ELECTRON: boolean;
    electron: typeof electron;
    remote: typeof remote;
    RtcEngine: typeof RtcEngineAdapter;
    rtcEngine: RtcEngineAdapter;
    rtcMessage: RtcMessage;
    QResult: typeof QResult;
    JoinChannelType: typeof JoinChannelType;
    VideoProfileType: typeof VideoProfileType;
    ipc: typeof ipc;
  }
}
