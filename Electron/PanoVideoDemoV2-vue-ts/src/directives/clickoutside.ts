let handler;
const clickOutside = {
  bind(el: any, binding: any) {
    const boundExpression = binding.value.handler || binding.value;

    handler = (e: any) => {
      const { exclude = [] } = binding.value;
      const isExcludedElement = exclude.some((className: any) =>
        e.target.classList.contains(className)
      );
      const isOutsideClick = el !== e.target && !el.contains(e.target);
      if (isOutsideClick && !isExcludedElement) {
        boundExpression();
      }
    };

    el.handler = handler;
    document.addEventListener('click', el.handler);
  },

  unbind(el: any) {
    document.removeEventListener('click', el.handler);
  },
};

export default clickOutside;
