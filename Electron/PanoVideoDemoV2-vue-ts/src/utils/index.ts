import { Log } from '@pano.video/panorts';
import CryptoJS from 'crypto-js';

export const genPocToken = (
  appId: string,
  channelId: string,
  userId: string,
  appSecret: string,
  duration = 86400,
  delayClose = 0,
  size = 25
) => {
  const version = '02';
  const timestamp = Math.floor(Date.now() / 1000);
  const params = CryptoJS.enc.Base64.stringify(
    CryptoJS.enc.Utf8.parse(
      JSON.stringify({
        channelId,
        userId,
        channelDuration: 0,
        privileges: 0,
        duration,
        size,
        delayClose,
      })
    )
  );
  const signContent = `${version}${appId}${timestamp}${params}`;
  const signatureValue = CryptoJS.enc.Base64.stringify(
    CryptoJS.HmacSHA256(signContent, appSecret)
  );
  return `${version}.${appId}.${timestamp}.${params}.${signatureValue}`;
};

export function genUserVideoDomId(
  type: 'video' | 'screen',
  userId: string,
  userName: string
) {
  return `${
    type === 'video' ? 'videoDomRef' : 'screenDomRef'
  }-${userId}-${userName}`;
}

export function getVideoInfo(domRef: HTMLElement) {
  const videoDom: any =
    domRef.getElementsByTagName('video')[0] ||
    domRef.getElementsByTagName('canvas')[0];
  if (videoDom) {
    return {
      width:
        videoDom instanceof HTMLVideoElement
          ? videoDom.videoWidth
          : videoDom.width,
      height:
        videoDom instanceof HTMLVideoElement
          ? videoDom.videoHeight
          : videoDom.height,
    };
  }
  return undefined;
}

export function LogUtil(...args: any[]) {
  console.log(`${new Date().toISOString()} PVC log:`, ...args);
  Log.logInfo('PVC log:', ...args);
}

export function hasProperty(obj: any, key: string): boolean {
  return Object.prototype.hasOwnProperty.call(obj, key);
}
