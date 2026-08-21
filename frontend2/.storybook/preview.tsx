import { mswLoader } from 'msw-storybook-addon/csf3';

import { withQuery } from './decorators/with-query';
import { withRouter } from './decorators/with-router';

import { handlers } from '@/mocks/handlers';
import '@/styles.css';

import type { Preview } from '@storybook/react-vite';

const preview: Preview = {
  parameters: {
    controls: { matchers: { color: /(background|color)$/i, date: /Date$/i } },
    // Story-level `parameters.msw.handlers` replaces this list for that story.
    msw: { handlers },
    a11y: { test: 'error' },
  },
  loaders: [mswLoader()],
  decorators: [withQuery, withRouter],
};

export default preview;
