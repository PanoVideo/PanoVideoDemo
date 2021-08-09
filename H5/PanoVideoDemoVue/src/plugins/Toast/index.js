import Toast from './Toast.vue';

export default {
  install(Vue) {
    const elem = document.createElement('div');
    document.body.appendChild(elem);
    const Plugin = Vue.extend(Toast);
    Object.assign(Vue.prototype, {
      $toast: new Plugin({ el: elem }),
    });
  },
};
