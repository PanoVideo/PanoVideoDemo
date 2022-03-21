<template>
  <Popover placement="right" trigger="click">
    <template slot="content">
      <div class="pano-wb-popup">
        <div class="pano-wb-popup__item pano-wb-popup__item--fixedWith">
          <div
            class="pano-wb-popup__item__list"
            v-for="stamp in stamps"
            :key="stamp.id"
          >
            <div
              :class="{
                'pano-wb-popup__item__list__item': true,
                'pano-wb-popup__item__list__item--resizable': stamp.resizable,
                'pano-wb-popup__item__list__item--selected':
                  selectedStamp === stamp.id,
              }"
              @click="selectStamp(stamp.id)"
            >
              <img :src="stamp.url" :alt="stamp.id" />
            </div>
          </div>
        </div>
      </div>
    </template>
    <div
      :class="{
        'pano-withtip': true,
        'pano-wb-tb__item': true,
        'pano-wb-tb__item--selected': selected,
      }"
      data-tip="图章"
    >
      <span class="iconfont icon-stamp" />
      <div
        :class="
          selected
            ? 'pano-wb-tb__item__triangle--selected'
            : 'pano-wb-tb__item__triangle'
        "
      />
    </div>
  </Popover>
</template>

<script>
import { Popover } from 'ant-design-vue';
import { stamps } from '@/constants';
import { RtcWhiteboard } from '@pano.video/panorts';

export default {
  data() {
    return {
      stamps,
      selectedStamp: '',
    };
  },
  props: {
    whiteboard: RtcWhiteboard,
    selected: Boolean,
  },
  components: {
    Popover,
  },
  methods: {
    selectStamp(id) {
      this.selectedStamp = id;
      this.whiteboard.setStamp(id);
      this.$emit('select');
    },
  },
  mounted() {
    stamps.forEach((stamp) => {
      this.whiteboard.addStamp(stamp.id, stamp.url, stamp.resizable);
    });
  },
};
</script>

<style lang="less" scoped>
.pano-wb-tb {
  &__item {
    font-size: 18px;
    display: inline-flex;
    width: 30px;
    height: 30px;
    padding: 2px;
    justify-content: center;
    align-items: center;
    border-radius: 5px;
    cursor: pointer;
    margin: 1px 0;
    color: #333;
    position: relative;
    &:hover {
      background-color: lightgray;
    }
    &--selected {
      // background-color: @deep-grey;
      color: #0899f9;
      &:hover {
        background-color: lightgray;
      }
    }
    &__triangle {
      position: absolute;
      right: 2px;
      bottom: 3px;
      border: 3px solid transparent;
      border-right-color: #333;
      border-bottom-color: #333;
      &--selected {
        position: absolute;
        right: 2px;
        bottom: 3px;
        border: 3px solid transparent;
        border-right-color: #0899f9;
        border-bottom-color: #0899f9;
      }
    }
  }
}

.pano-wb-popup {
  font-size: 12px;
  pointer-events: all;
  button:hover {
    background-color: #eee;
  }
  &__item {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    button {
      font-size: 12px !important;
    }
    &__label {
      font-size: 12px;
      padding-right: 5px;
    }
    &__list {
      width: 25%;
      display: flex;
      justify-content: center;
      padding: 8px 0;
      &__item {
        cursor: pointer;
        width: 30px;
        height: 30px;
        position: relative;
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 3px;
        &--resizable {
          &::before,
          &::after {
            position: absolute;
            content: '';
            width: 0;
            height: 0;
            border: 3px solid rgba(0, 0, 0, 0.3);
          }
          &::before {
            left: 3px;
            top: 3px;
            border-bottom-color: transparent;
            border-right-color: transparent;
          }
          &::after {
            right: 3px;
            bottom: 3px;
            border-top-color: transparent;
            border-left-color: transparent;
          }
        }
        &--selected {
          box-shadow: 0 0 1px 2px rgba(0, 0, 0, 0.1);
        }
        img {
          width: 16px;
          height: 16px;
        }
      }
    }
    &--no-flex {
      display: block;
    }
    &--fixedWith {
      width: 160px;
    }
  }

  &__item ~ &__item {
    margin-top: 5px;
    position: relative;
    line-height: 32px;
    padding-top: 5px;
    &::before {
      position: absolute;
      content: '';
      width: 100%;
      height: 1px;
      background-color: #eee;
      top: 0;
    }
  }

  &__item2 {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    width: 160px;
  }
}
</style>
