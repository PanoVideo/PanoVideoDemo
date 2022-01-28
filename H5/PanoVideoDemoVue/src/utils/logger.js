export function Logger(prefix) {
  this.prefix = `pvch5:: ${prefix}::`;
}

Logger.prototype.info = function info(...args) {
  console.info(this.prefix, ...args);
};
