import { Constants } from '@pano.video/panortc';

export const VideoProfileType = {
  ...Constants.VideoProfileType
};

export const ChannelMode = {
  Mode_1v1: 0,
  Mode_Meeting: 1
};

export const routes = {
  login: '/',
  home: '/meeting'
};

export const localCacheKeyAppId = 'pano.democlient.appId';
export const localCacheKeyToken = 'pano.democlient.token';
export const localCacheKeyChannelId = 'pano.democlient.channelId';
export const localCacheKeyUserName = 'pano.democlient.userName';
export const localCacheKeyUserId = 'pano.democlient.userId';
export const localCacheKeyMuteAtStart = 'pano.democlient.muteatstart';
export const localCacheKeyAutoStart = 'pano.democlient.autostart';
export const localCacheKeyMuteMicAtStart = 'pano.democlient.mutemicatstart';
export const localCacheKeyMuteCamAtStart = 'pano.democlient.mutecamatstart';
export const localCacheKeySelectedCam = 'pano.democlient.selectedCam';
export const localCacheKeyWbNewShapeIndicator =
  'pano.democlient.WbNewShapeIndicator';
export const localCacheKeySelectedMic = 'pano.democlient.selectedMic';
export const localCacheKeySelectedSpeaker = 'pano.democlient.selectedSpeaker';
export const localCacheKeyVideoProfileType =
  'pano.democlient.localCacheKeyVideoProfileType';
export const localCacheKeyDefaultWb = 'pano.democlient.defaultWb';
export const MOMENT_FOR_UNSUBSCRIBE = 0;
/**
 * 最多同时订阅 8 个人
 */
export const subscribeVideoQuota = 8;
