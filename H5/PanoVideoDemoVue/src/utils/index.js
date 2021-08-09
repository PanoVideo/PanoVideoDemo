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
    const str = strList[0];
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
      value = str.substring(index + 1);
    }
    if (name) {
      if (name === key) {
        return value;
      }
    } else {
      result[name] = value;
    }
  }
  return name ? undefined : result;
}
