import { hasProperty } from '@/utils';
import * as mutations from '../mutations';
import * as actions from '../actions';
import { cloneDeep } from 'lodash-es';
import { RtcMessage } from '@pano.video/panorts';
import { PROP_WHITEBOARD } from '@/constants';

const initialState = {
  penetrable: false, //是否开启透明白板
  whiteboardAvailable: false, //  白板是否已经连接
  isWhiteboardOpen: false, // 白板面板是否打开
};

function getInitialState() {
  return cloneDeep(initialState);
}

type WhiteboardState = typeof initialState;

export default {
  state: getInitialState(),
  getters: {
    penetrable: (state: WhiteboardState) => state.penetrable,
    whiteboardAvailable: (state: WhiteboardState) => state.whiteboardAvailable,
    isWhiteboardOpen: (state: WhiteboardState) => state.isWhiteboardOpen,
  },
  mutations: {
    [mutations.UPDATE_WHITEBOARD](
      state: WhiteboardState,
      payload: {
        penetrable?: boolean;
        whiteboardAvailable?: boolean;
        isWhiteboardOpen?: boolean;
      }
    ) {
      if (hasProperty(payload, 'whiteboardAvailable')) {
        state.whiteboardAvailable = payload.whiteboardAvailable!;
      }
      if (hasProperty(payload, 'isWhiteboardOpen')) {
        state.isWhiteboardOpen = payload.isWhiteboardOpen!;
      }
    },
    [mutations.RESET_WHITEBOARD_STORE](state: WhiteboardState) {
      Object.assign(state, getInitialState());
    },
  },
  actions: {
    [actions.SET_IS_WHITEBOARD_OPEN](
      ctx: {
        state: WhiteboardState;
        getters: any;
        commit: any;
      },
      isWhiteboardOpen: boolean
    ) {
      ctx.commit(mutations.UPDATE_WHITEBOARD, {
        isWhiteboardOpen,
      });
      if (ctx.getters.isHost) {
        RtcMessage.getInstance().setProperty(PROP_WHITEBOARD, {
          on: isWhiteboardOpen,
        });
      }
    },
  },
};
