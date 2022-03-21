<template>
  <div class="about-wrapper">
    <img src="../../assets/img/logo.png" alt="logo" />
    <strong>构建云上的每一次美好互动</strong>
    <em>版本号</em>
    <em>{{ version }}</em>
    <span>杭州拍乐云科技有限公司</span>
  </div>
</template>

<script>
import PackageInfo from '../../../package.json';

export default {
  data() {
    return {
      version: `V${PackageInfo.version} (${
        this.$IS_ELECTRON
          ? PackageInfo.dependencies[
              '@pano.video/panortc-electron-sdk'
            ].replace('^', '')
          : PackageInfo.dependencies['@pano.video/panortc'].replace('^', '')
      })`,
    };
  },
};
</script>

<style lang="less" scoped>
@import '~ant-design-vue/es/style/themes/default.less';

.about-wrapper {
  padding-top: 100px;
  text-align: center;
  > img {
    display: block;
    width: 200px;
    margin: 0 auto;
  }
  > strong {
    display: block;
    margin: 6px 0 34px;
    font-size: 19px;
    color: #0899f9;
  }
  > em {
    display: block;
    line-height: 24px;
    font-style: normal;
  }
  > span {
    display: block;
    margin-top: 125px;
    font-size: 12px;
    color: #999;
    line-height: 24px;
  }
}
</style>
