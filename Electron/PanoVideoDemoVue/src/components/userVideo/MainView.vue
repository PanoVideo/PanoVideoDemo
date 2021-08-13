<template>
  <div class="panoMainViewVideo">
    <div
      class="videoDomRef"
      :style="{
        visibility:
          (mainViewUser.isScreenInMainView && !mainViewUser.screenOpen) ||
          (!mainViewUser.isScreenInMainView && mainViewUser.videoMuted)
            ? 'hidden'
            : 'visible',
        transform:
          isUserMe && videoAnnotationOpen ? 'rotateY(180deg)' : 'rotateY(0deg)'
      }"
      ref="domRef"
    />

    <div class="mv__top-btns">
      <div
        class="mv__top-btns__btn"
        @click="onUnLock"
        v-if="mainViewUser.locked"
      >
        <span class="iconfont icon-lock1" />
        <span>取消锁定</span>
      </div>
      <div
        class="mv__top-btns__btn"
        @click="toggleAnnotation"
        v-if="isUserMe && !userMe.videoMuted"
      >
        <span>{{ userMe.videoAnnotationOpen ? '关闭' : '打开' }}视频标注</span>
      </div>
      <!-- <div
        class="mv__top-btns__btn"
        v-if="mainViewUser.screenOpen"
        @click="toggleRemoteControl"
      >
        <i
          v-if="isRemoteControling"
          class="iconfont icon-remote-ctrl-off"
          :style="{ color: 'red' }"
        >
          <span class="">停止远程控制</span>
        </i>
        <i v-else class="iconfont icon-remote-ctrl-on">
          <span>请求远程控制</span>
        </i>
      </div> -->
    </div>

    <!-- 远程控制蒙层 -->
    <div
      :style="{
        width: '100%',
        height: '100%',
        position: 'absolute',
        zIndex: 11,
        userSelect: 'none',
        pointerEvents: this.isRemoteControling ? 'auto' : 'none'
      }"
      ref="remoteControlRef"
    />

    <!-- 显示在 view 中部的大号用户名 -->
    <div
      class="userNameMainView"
      :style="{ fontSize: '80px' }"
      v-show="mainViewUser.videoMuted && !mainViewUser.isScreenInMainView"
    >
      {{ mainViewUser.userName || mainViewUser.userId }}
      {{ isUserMe && '(Me)' }}
    </div>

    <!-- 底部的状态条和用户名  -->
    <div class="userVideoStatus">
      <div
        class="userVideoUserName"
        v-show="!mainViewUser.videoMuted || mainViewUser.isScreenInMainView"
      >
        {{ mainViewUser.userName || mainViewUser.userId }}
        {{ mainViewUser.isScreenInMainView ? '(Share)' : '' }}
        {{ isUserMe ? '(Me)' : '' }}
      </div>
      <i
        class="iconfont icon-microphone-slash"
        v-if="mainViewUser.audioMuted"
      />
      <i
        class="iconfont icon-microphone"
        v-else
        :style="{
          color: mainViewUser.isSpeaking ? '#00ff00' : '#fff',
          fontSize: '14px'
        }"
      />
    </div>

    <!-- 视频标注 -->
    <div class="annotation" ref="annotationRef" />

    <AnnotationToolbar
      v-if="
        (videoAnnotationOpen || shareAnnotationOpen) && annotationWhiteboard
      "
      :whiteboard="annotationWhiteboard"
    />
  </div>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex';
import { RtsService, RemoteControl } from '@pano.video/whiteboard';
import AnnotationToolbar from '@/components/annotation/AnnotationToolbar';

const electron = window.require('electron');

export default {
  data() {
    return {
      controller: undefined,
      annotationWhiteboard: undefined
    };
  },
  components: {
    AnnotationToolbar
  },
  computed: {
    ...mapGetters([
      'mainViewUser',
      'userMe',
      'isWhiteboardOpen',
      'isRemoteControling'
    ]),
    isUserMe() {
      return this.mainViewUser.userId === this.userMe.userId;
    },
    userViewRef() {
      return this.mainViewUser.isScreenInMainView
        ? this.mainViewUser.screenDomRef
        : this.mainViewUser.videoDomRef;
    },
    videoAnnotationOpen() {
      return this.mainViewUser.videoAnnotationOpen;
    },
    shareAnnotationOpen() {
      return this.mainViewUser.shareAnnotationOpen;
    }
  },
  watch: {
    ['mainViewUser.userId']() {
      this.updateVideoView();
      this.checkAnnotation();
    },
    ['mainViewUser.isScreenInMainView']() {
      this.updateVideoView();
      this.checkAnnotation();
    },
    ['mainViewUser.videoMuted']() {
      this.updateVideoView();
    },
    ['mainViewUser.videoAnnotationOpen']() {
      this.checkAnnotation();
    },
    ['mainViewUser.shareAnnotationOpen']() {
      this.checkAnnotation();
    },
    isWhiteboardOpen() {
      this.updateVideoView();
    }
  },
  methods: {
    ...mapMutations(['updateUser']),
    toggleAnnotation() {
      this.updateUser({
        userId: this.mainViewUser.userId,
        videoAnnotationOpen: !this.mainViewUser.videoAnnotationOpen
      });
      if (!this.mainViewUser.videoAnnotationOpen) {
        this.annotationWhiteboard?.close();
      }
    },
    checkAnnotation() {
      if (
        this.annotationWhiteboard &&
        this.annotationWhiteboard.tragetUserId !== this.mainViewUser.userId
      ) {
        // 如果标注白板对象存在，这种情况是主视图用户切换的情况，需要先关闭之前的标注
        this.annotationWhiteboard.close();
        this.annotationWhiteboard = undefined;
      }
      if (
        (this.mainViewUser.isScreenInMainView &&
          this.mainViewUser.shareAnnotationOpen) ||
        (!this.mainViewUser.isScreenInMainView &&
          this.mainViewUser.videoAnnotationOpen)
      ) {
        this.openAnnotation();
      } else {
        this.closeAnnotation();
      }
    },
    openAnnotation() {
      this.annotationWhiteboard = RtsService.getInstance().getAnnotation(
        this.mainViewUser.userId,
        this.mainViewUser.isScreenInMainView ? 'share' : 'video'
      );
      if (!this.annotationWhiteboard.isWhiteboardOpen) {
        if (
          !this.mainViewUser.isScreenInMainView &&
          this.mainViewUser.videoDomRef
        ) {
          // 视频标注的情况，需要主动设置自己的分辨率信息
          const canvasDom = this.mainViewUser.videoDomRef.getElementsByTagName(
            'canvas'
          )[0];
          if (!canvasDom) {
            return;
          }
          this.annotationWhiteboard.setAnnotationViewSize({
            width: canvasDom.width,
            height: canvasDom.height
          });
        }
        this.annotationWhiteboard.open(this.$refs.annotationRef);
      }
    },
    closeAnnotation() {
      this.annotationWhiteboard?.close();
      if (this.annotationWhiteboard?.tragetUserId === this.userMe.userId) {
        this.annotationWhiteboard?.stop();
      }
      this.annotationWhiteboard = undefined;
    },
    onUnLock() {
      this.updateUser({ userId: this.mainViewUser.userId, locked: false });
    },
    updateVideoView() {
      if (!this.$refs.domRef) return;
      this.$refs.domRef.innerHTML = '';
      this.$refs.domRef.appendChild(this.userViewRef);
    },
    toggleRemoteControl() {
      if (this.isRemoteControling) {
        this.controller?.cancelControl(this.mainViewUser.userId);
        this.resetControlStatus();
        electron.remote.getCurrentWindow().setFullScreen(false);
      } else {
        if (!RtsService.getInstance().isConnected) {
          this.$message.warning('Rts尚未连接成功，请稍后重试');
          return;
        }
        this.removeObservers();
        this.controller = new RemoteControl(this.mainViewUser.userId);
        this.controller.once(
          RemoteControl.Events.onControlResponse,
          this.onControlResponse
        );
        this.controller.once(
          RemoteControl.Events.onControlCancelled,
          this.onControlCancelled
        );
        this.controller.once(
          RemoteControl.Events.onConnectDisconnected,
          this.onConnectDisconnected
        );
        this.controller?.requestControl(this.mainViewUser.userId);
        this.$message.info('远程控制请求已发起');
      }
    },
    resetControlStatus() {
      this.removeObservers();
      this.controller = undefined;
      this.$emit('toggleRemoteControl', false);
    },
    onControlCancelled() {
      this.$message.info('对方取消了远程控制');
      this.resetControlStatus();
    },
    onConnectDisconnected() {
      this.$message.info('远程控制已断开...');
      this.resetControlStatus();
    },
    onControlResponse(result) {
      const success = result === 0;
      this.$emit('toggleRemoteControl', success);
      let msg = '';
      switch (result) {
        case 0:
          msg = `您正在控制${this.mainViewUser.userName}的屏幕`;
          break;
        case -16:
          msg = `${this.mainViewUser.userName} 长时间没有响应你的请求控制，请稍后重试`;
          break;
        default:
          msg = '对方拒绝了你的远程控制请求';
          break;
      }
      this.$message.info(msg);
      if (success) {
        this.controller?.startControl(this.$refs.remoteControlRef);
        const rect = this.mainViewUser.shareResolution;
        if (rect) {
          this.controller?.setShareRect(rect);
        }
      } else {
        this.resetControlStatus();
      }
    },
    removeObservers() {
      this.controller?.off(
        RemoteControl.Events.onControlResponse,
        this.onControlResponse
      );
      this.controller?.off(
        RemoteControl.Events.onControlCancelled,
        this.onControlCancelled
      );
      this.controller?.off(
        RemoteControl.Events.onConnectDisconnected,
        this.onConnectDisconnected
      );
    },
    onUserScreenResolutionChanged(userId, width, height) {
      if (this.controller?.remoteUserId === userId) {
        this.captureRect = { x: 0, y: 0, width, height };
        this.controller?.setShareRect(this.captureRect);
      }
    }
  },
  mounted() {
    this.updateVideoView();
    this.checkAnnotation();
    window.rtcEngine.on(
      'userScreenResolutionChanged',
      this.onUserScreenResolutionChanged
    );
  },
  beforeDestroy() {
    window.rtcEngine.off(
      'userScreenResolutionChanged',
      this.onUserScreenResolutionChanged
    );
    this.closeAnnotation();
  }
};
</script>

<style lang="scss" src="./uservideo.scss"></style>

<style lang="scss">
.mv__top-btns {
  position: absolute;
  top: 20px;
  left: 20px;
  height: 28px;
  display: inline-flex;
  line-height: 28px;

  &__btn {
    width: auto !important;
    height: 100%;
    display: flex;
    align-items: center;
    border-radius: 4px;
    background-color: rgba(0, 0, 0, 0.4);
    z-index: 20;
    color: #fff;
    width: auto;
    padding: 0 10px;
    font-size: 12px;
    cursor: pointer;
    user-select: none;
    span {
      padding-left: 5px;
      font-size: 12px;
    }
  }
}
.annotation {
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  margin: auto;
  display: block;
  position: absolute;
  z-index: 11;
}
</style>
