import { HmacSHA256, enc } from 'crypto-js';

export function genToken(appId, appSecret, channelId, userId) {
  const version = '02';
  const timestamp = Math.floor(Date.now() / 1000);
  const params = enc.Base64.stringify(
    enc.Utf8.parse(
      JSON.stringify({
        channelId,
        userId,
        channelDuration: 0,
        privileges: 0,
        duration: 86400,
        size: 25,
        delayClose: 60,
      }),
    ),
  );
  const signContent = `${version}${appId}${timestamp}${params}`;
  const signatureValue = enc.Base64.stringify(
    HmacSHA256(signContent, appSecret),
  );
  return `${version}.${appId}.${timestamp}.${params}.${signatureValue}`;
}
