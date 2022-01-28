module.exports = {
  root: true,
  env: {
    node: true,
  },
  extends: [
    'plugin:vue/essential',
    '@vue/airbnb',
  ],
  parserOptions: {
    parser: 'babel-eslint',
  },
  rules: {
    "operator-linebreak": [2, "after"],
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-underscore-dangle': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-alert': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-lonely-if': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-confusing-arrow': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'consistent-return': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-plusplus': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-continue': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-param-reassign': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'prefer-destructuring': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'import/prefer-default-export': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
  },
};
