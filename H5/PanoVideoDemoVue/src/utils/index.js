export function formatTime(remain) {
  let remainTime = '';
  try {
    const seconds = Math.floor(remain / 1000);
    if (seconds >= 0) {
      let remainHours = Math.floor(seconds / 3600);
      remainHours = remainHours < 10 ? `0${remainHours}` : remainHours;
      let remainMinutes = Math.floor((seconds % 3600) / 60);
      remainMinutes = remainMinutes < 10 ? `0${remainMinutes}` : remainMinutes;
      let remainSeconds = (seconds % 60);
      remainSeconds = remainSeconds < 10 ? `0${remainSeconds}` : remainSeconds;
      remainTime = `${remainHours}:${remainMinutes}:${remainSeconds}`;
    }
  } catch (e) {
    // ignore
  }
  return remainTime;
}

export function parseSearch(search, name) {
  if (!search) {
    return name ? undefined : {};
  }
  const strList = search.split('&');
  const result = {};
  for (let i = 0, len = strList.length; i < len; i++) {
    const str = strList[i];
    if (str === '') {
      continue;
    }
    const index = str.indexOf('=');
    let key;
    let value;
    if (index === -1) {
      key = str;
      value = '';
    } else {
      key = str.substring(0, index);
      value = decodeURIComponent(str.substring(index + 1));
    }
    if (name) {
      if (name === key) {
        return value;
      }
    } else {
      result[key] = value;
    }
  }
  return name ? undefined : result;
}

export const searchObj = parseSearch(window.location.search.substring(1));

// 把 channelId 写到 url 中去，用于微信分享和复制url
export function addChannelIdToUrl(channelId) {
  if (!searchObj.channelId || searchObj.channelId !== channelId) {
    if (searchObj.channelId) {
      searchObj.channelId = channelId; // 更新channelId
    }
    // 编码一下，pvc只允许channelId为数字和字母可以不用编码，为了代码的完备性这里加上了一下编码
    channelId = encodeURIComponent(channelId);
    let url;
    if (searchObj.channelId === undefined) {
      url = window.location.href.replace(/#|$/, `${window.location.search ? '&' : '?'}channelId=${channelId}$&`);
    } else {
      url = window.location.href.replace(/([?&]channelId)(=?)[^&#]*/, (_, p1, p2) => `${p1}${p2 || '='}${channelId}`);
    }
    window.history.replaceState(null, '', url);
  }
}

const openStatusList = ['open', 'unmute'];
export function isOpen(status) {
  return openStatusList.includes(status);
}

/* eslint-disable camelcase */
function is_iOS_15_1(ua) {
  return (/iPod touch|iPhone|iPad/.test(ua) && /OS (15_1|15_1_1)/.test(ua)) ||
    (ua.includes('Macintosh') && ua.includes('Safari') && ua.includes('Version/15.1'));
}
/* eslint-disable camelcase */
export const iOS_15_1 = is_iOS_15_1(navigator.userAgent);
