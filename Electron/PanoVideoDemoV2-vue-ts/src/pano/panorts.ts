import { message } from 'ant-design-vue';
import {
  RtcWhiteboard,
  RtsService,
  RtcMessage,
  Constants as WbConstants,
} from '@pano.video/panorts';
import store from '../store';
import * as mutations from '@/store/mutations';
import * as actions from '@/store/actions';
import { startAudioDump, stopAudioDump } from './index';
import { info } from '@/utils/common';
import PdfPlugin from '@pano.video/panorts/lib/PdfPlugin';
import { LogUtil } from '@/utils';
import { PROP_WHITEBOARD, PROP_HOST_ID, PROP_SETTING } from '@/constants';
import { MediaType } from '@/store/modules/user';

RtcWhiteboard.usePdf(PdfPlugin);

const rtcWhiteboard = new RtcWhiteboard();
window.rtcWhiteboard = rtcWhiteboard;

/**
 * 初始化pano rts相关逻辑，pano rts包括 Whiteboard, Annotation, RtcMessage 等
 */
export default function initPanoRts() {
  const rtcMessage = RtcMessage.getInstance();
  window.rtcMessage = rtcMessage;
  rtcMessage.on(
    RtcMessage.Events.propertyChanged,
    (
      type: 'update' | 'delete',
      propName: string,
      propValue: Record<string, any>
    ) => {
      LogUtil('propertyChanged:', type, propName, propValue);
      if (propName === PROP_HOST_ID) {
        store.commit(mutations.SET_HOST_ID, propValue.id);
        if (propValue.id === store.getters.userMe.userId) {
          // 自己是主持人时，申请白板admin角色
          window.rtcWhiteboard.setRoleType(WbConstants.WBRoleType.Admin);
        }
      } else if (propName === PROP_WHITEBOARD) {
        if (!store.getters.isHost) {
          // 主持人会通过property控制白板的开关，其他成员根据这个property开关白板
          store.dispatch(actions.SET_IS_WHITEBOARD_OPEN, propValue.on);
        }
        if (
          store.getters.userMe.screenOpen &&
          !store.getters.isHost &&
          propValue.on
        ) {
          store.dispatch(actions.STOP_SHARE_SCREEN);
          message.info('您的桌面共享已停止');
        }
      } else if (propName === PROP_SETTING) {
        store.commit(mutations.SET_SCREEN_SHARE_MODE, propValue.share);
      }
    }
  );
  rtcMessage.on(
    RtcMessage.Events.messageReceived,
    (data: {
      from: string;
      message: {
        type: string;
        payload: any;
        command: string;
        description: string;
      };
    }) => {
      try {
        LogUtil('RtcMessage.Events.messageReceived', data);
        const { type, payload } = data.message;
        const msg = data.message;
        if (msg.type === 'command') {
          if (msg.command === 'startDump') {
            if (window.IS_ELECTRON) {
              message.info(
                '接收到开启Audio Dump消息，将开启Audio Dump1分钟，结束后自动上传相关日志文件'
              );
            }
            startAudioDump(msg.description, store.getters.userMe.userName);
          } else if (msg.payload.command === 'stopDump') {
            message.info('接收到停止Audio Dump消息，将终止Audio Dump');
            stopAudioDump();
          }
        }
        setTimeout(() => {
          if (type === 'screenSourceType') {
            const user = store.getters.getUserById(data.from);
            if (!user) {
              return;
            }
            store.commit(mutations.UPDATE_USER, {
              userId: data.from,
              screenShareType: payload,
            });
          }
        }, 1000);
      } catch (error) {
        console.error(error);
      }
    }
  );

  rtcMessage.on(RtcMessage.Events.userJoin, (userId: string) => {
    LogUtil('RtcMessage.Events.userJoin', userId);
    if (store.getters.userMe.screenOpen) {
      rtcMessage.sendMessage(userId, {
        type: 'screenSourceType',
        payload: store.getters.userMe.screenShareType,
      });
    }
  });

  rtcWhiteboard.on(
    RtcWhiteboard.Events.whiteboardFailover,
    (data: { state: 'Reconnecting' | 'Success' | 'Failed' }) => {
      switch (data.state) {
        case 'Reconnecting':
          info(
            {
              key: 'rtsServiceFailover',
              content: '与Rts Server连接已断开，正在重连...',
              duration: 0,
            },
            true
          );
          break;
        case 'Success':
          info(
            {
              key: 'rtsServiceFailover',
              content: '已重新连接到Rts Server',
              duration: 3,
            },
            true
          );
          break;
        case 'Failed':
          if (store.getters.meetingStatus !== 'countdownover') {
            info(
              {
                key: 'rtsServiceFailover',
                content: '无法连接到服务器，请退出重试',
                duration: 3,
              },
              true
            );
            store.commit(mutations.SET_MEETING_STATUS, {
              meetingEndReason: '无法连接到服务器',
              meetingStatus: 'ended',
            });
          }
          break;
        default:
          break;
      }
    }
  );

  rtcWhiteboard.on(RtcWhiteboard.Events.readyStateChanged, (payload) => {
    LogUtil('whiteboard ready state changed,', payload);
    store.commit(mutations.UPDATE_WHITEBOARD, {
      whiteboardAvailable: payload.ready,
    });
  });

  // 课件加载超时通知
  rtcWhiteboard.on(RtcWhiteboard.Events.coursewareReload, () => {
    message.info('课件加载超时，正在尝试重新加载，请稍候...');
  });

  rtcWhiteboard.on(RtcWhiteboard.Events.userRoleTypeChanged, (role) => {
    if (role !== RtcWhiteboard.WBRoleType.Attendee) {
      // 仅admin角色可以通过键盘操作课件
      rtcWhiteboard.enableCoursewareInteraction();
    } else {
      rtcWhiteboard.disableCoursewareInteraction();
    }
  });

  rtcWhiteboard.on(
    RtcWhiteboard.Events.userVisionShareStart,
    (userId: string, name: string) => {
      rtcWhiteboard.startFollowVision();
      info(`正在跟随${name || userId}的视角`, 3);
    }
  );

  RtsService.getInstance().on(
    RtsService.Events.shareAnnotationStart,
    (userId: string) => {
      store.dispatch(actions.START_SHARE_ANNOTATION, userId);
    }
  );
  RtsService.getInstance().on(
    RtsService.Events.externalAnnotationStart,
    (userId: string) => {
      LogUtil('externalAnnotationStart', userId);
      store.commit(mutations.UPDATE_USER, {
        userId,
        externalAnnotationOn: true,
      });
      store.commit(mutations.UPDATE_ANNOTATION_STATUS, {
        userId,
        on: true,
        type: MediaType.screen,
      });
    }
  );
  RtsService.getInstance().on(
    RtsService.Events.externalAnnotationStop,
    (userId: string) => {
      LogUtil('externalAnnotationStop', userId);
      store.commit(mutations.UPDATE_USER, {
        userId,
        externalAnnotationOn: false,
      });
      store.commit(mutations.UPDATE_ANNOTATION_STATUS, {
        on: false,
      });
    }
  );
  RtsService.getInstance().on(
    RtsService.Events.shareAnnotationStop,
    (userId: string) => {
      store.dispatch(actions.STOP_SHARE_ANNOTATION, userId);
    }
  );
  RtsService.getInstance().on(
    RtsService.Events.videoAnnotationStart,
    (userId: string) => {
      LogUtil('videoAnnotationStart', userId);
      store.dispatch(actions.START_VIDEO_ANNOTATION, userId);
      if (store.getters.lockedUser) {
        store.commit(mutations.LOCK_USER, { userId: '' });
        message.info('已解除锁定');
      }
      if (
        store.getters.isUserAvailable(userId) &&
        store.getters.userMe.screenOpen
      ) {
        store.dispatch(actions.STOP_SHARE_SCREEN);
        message.info('您的桌面共享已停止');
      }
    }
  );
  RtsService.getInstance().on(
    RtsService.Events.videoAnnotationStop,
    (userId: string) => {
      LogUtil('videoAnnotationStop', userId);
      store.dispatch(actions.STOP_VIDEO_ANNOTATION, userId);
    }
  );
}

export const allWbColors = [
  'rgba(224, 44, 11, 1)',
  'rgba(220, 0, 129, 1)',
  'rgba(228, 100, 8, 1)',
  'rgba(231, 189, 14, 1)',
  'rgba(86, 19, 216, 1)',
  'rgba(22, 49, 211, 1)',
  'rgba(12, 142, 229, 1)',
  'rgba(32, 216, 8, 1)',
  'rgba(144, 222, 15, 1)',
  'rgba(1, 2, 4, 1)',
  'rgba(153, 153, 153, 1)',
  'rgba(255, 255, 255, 1)',
];

export function randomWbColor() {
  const res = allWbColors[Math.floor(Math.random() * (allWbColors.length - 1))];
  return res;
}

/**
 * 判断自己是否是白板 admin 用户。
 *
 * 注意：
 * 1. admin 角色权限是 pano whiteboard sdk 赋予的，此角色在sdk中仅用来控制当前用户是否可以操作（更新、移动、删除等）别人绘制的图形
 * 2. 在 app 层赋予了 admin 角色一些其他的控制权限，例如翻页、上传文件转码，设置背景图等，所以在执行这些操作前调用了 checkIsAdmin
 */
export function checkIsAdmin() {
  if (store.getters.userMe !== store.getters.hostUser) {
    message.info('该操作需要主持人权限，请点击用户列表成为主持人');
    return false;
  }
  return true;
}

/**
 * 通过RtcMessage的setProperty设置主持人id(hostId)
 */
export function setHostId(hostId: any) {
  const hostInfo = { id: hostId };
  RtcMessage.getInstance().setProperty(PROP_HOST_ID, hostInfo);
}
