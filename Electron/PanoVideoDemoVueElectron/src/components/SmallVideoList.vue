<template>
  <div class="subVideoScrollHider">
    <div class="smallVideoShadowRight" />
    <div class="smallVideoShadowLeft" />

    <!-- 左右横移按钮 -->
    <div v-if="subvideoOverflowContainer" class="leftScroll" @click="showLeft">
      <span class="iconfont icon-circle-left" />
    </div>

    <div class="subvideos" ref="subvideos">
      <div class="userVideos" ref="subvideoInner">
        <template v-for="user in allUsers">
          <SmallView
            :key="user.userId"
            :user="user"
            :forceUseSrcView="isWhiteboardOpen"
          />
          <SmallView
            :key="`${user.userId}-screen`"
            :user="user"
            v-if="user.screenOpen && user !== userMe"
            :forceUseSrcView="isWhiteboardOpen"
            :showScreenShare="true"
          />
        </template>
      </div>
    </div>

    <!-- 左右横移按钮 -->
    <div
      v-if="subvideoOverflowContainer"
      class="rightScroll"
      @click="showRight"
    >
      <span class="iconfont icon-circle-right" />
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import SmallView from '../components/userVideo/SmallView';

export default {
  data() {
    return { subvideoOverflowContainer: false };
  },
  computed: {
    ...mapGetters(['allUsers', 'isWhiteboardOpen', 'userMe'])
  },
  watch: {
    ['allUsers.length']() {
      this.checkIfOverflow();
    }
  },
  components: { SmallView },
  methods: {
    showRight() {
      this.$refs.subvideos.scrollLeft =
        (this.$refs.subvideos.scrollLeft || 0) + 120;
    },
    showLeft() {
      this.$refs.subvideos.scrollLeft =
        (this.$refs.subvideos.scrollLeft || 0) - 120;
    },
    checkIfOverflow() {
      this.subvideoOverflowContainer =
        this.$refs.subvideoInner.getBoundingClientRect().width >
        this.$refs.subvideos.getBoundingClientRect().width;
    }
  },
  mounted() {
    this.checkIfOverflow();
  }
};
</script>

<style lang="scss" scoped>
.subVideoScrollHider {
  overflow: hidden;
  width: 100%;
  height: 120px;
  padding: 10px 0;
  background-color: black;
  transition: height 0.5s ease;
  position: relative;
}
.subVideoScrollHiderEmpty {
  height: 0;
  transition: height 0.5s ease;
  padding: 0;
}
.smallVideoShadowRight {
  position: absolute;
  top: 0px;
  right: 100px;
  z-index: 6;
  width: 0px;
  height: 120px;
  box-shadow: 0px 0px 20px 20px rgba(0, 0, 0, 0.65);
}
.smallVideoShadowLeft {
  position: absolute;
  top: 0px;
  left: 100px;
  z-index: 6;
  width: 0px;
  height: 120px;
  box-shadow: 0px 0px 20px 20px rgba(0, 0, 0, 0.65);
}
.subvideos {
  position: absolute;
  left: 100px;
  right: 100px;
  height: 140px;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
  text-align: center;
}
.userVideos {
  display: inline-block;
}

.leftScroll,
.rightScroll {
  height: 50px;
  width: 50px;
  position: absolute;
  top: 30px;
  z-index: 7;
  line-height: 30px;
  text-align: center;
  font-size: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #a7a2a2;
  cursor: pointer;
  &:hover {
    color: #fff;
  }
  span {
    font-size: 40px;
  }
}

.rightScroll {
  right: 25px;
}
.leftScroll {
  left: 25px;
}

$mobile-v-h: 80px;

@media all and (max-width: 768px) {
  .subVideoScrollHider {
    height: $mobile-v-h;
  }
  .subvideos {
    left: 50px;
    right: 50px;
    height: $mobile-v-h + 20px;
    .panoTinyVideo {
      height: $mobile-v-h - 20px;
      width: ($mobile-v-h - 20px) * 16 / 9 - 3px;
    }
  }

  .leftScroll,
  .rightScroll {
    top: 16px;
  }
  .leftScroll {
    left: 0;
  }
  .rightScroll {
    right: 0;
  }
  .smallVideoShadowRight {
    right: 40px;
    height: $mobile-v-h - 20px;
  }
  .smallVideoShadowLeft {
    left: 40px;
    height: $mobile-v-h - 20px;
  }
}
</style>
