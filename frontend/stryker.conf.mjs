/** @type {import('@stryker-mutator/api/core').PartialStrykerOptions} */
export default {
  testRunner: 'command',
  checkers: ['typescript'],
  tsconfigFile: 'tsconfig.app.json',
  mutate: ['src/app/**/*.ts', '!src/**/*.spec.ts'],
  coverageAnalysis: 'off',
  reporters: ['clear-text', 'progress', 'html'],
  commandRunner: {
    command: 'bun run test -- --watch=false',
  },
  htmlReporter: {
    fileName: 'reports/mutation/mutation.html',
  },
};
