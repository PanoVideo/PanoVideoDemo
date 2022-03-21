/* eslint-disable @typescript-eslint/no-var-requires */
import PanoRtc, { RtcEngine } from '@pano.video/panortc';

window.rtcEngine = new RtcEngine() as any;
window.RtcEngine = RtcEngine as any;
window.QResult = PanoRtc.QResult as any;
window.JoinChannelType = PanoRtc.Constants.JoinChannelType as any;
window.VideoProfileType = PanoRtc.Constants.VideoProfileName as any;

const initPanoRtc = require('./pano').default;
const initApp = require('./main').default;

initPanoRtc();
initApp();
