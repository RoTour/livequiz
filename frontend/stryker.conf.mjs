/** @type {import('@stryker-mutator/api/core').PartialStrykerOptions} */
export default {
  testRunner: 'command',
  checkers: ['typescript'],
  concurrency: 1,
  tsconfigFile: 'tsconfig.app.json',
  mutate: ['src/app/**/*.ts', '!src/**/*.spec.ts'],
  coverageAnalysis: 'off',
  reporters: ['clear-text', 'progress', 'html'],
  commandRunner: {
    command: './scripts/with-node.sh ./node_modules/.bin/ng test --watch=false',
  },
  htmlReporter: {
    fileName: 'reports/mutation/mutation.html',
  },
};
