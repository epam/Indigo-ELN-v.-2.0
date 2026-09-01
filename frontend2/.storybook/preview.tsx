import { mswLoader } from 'msw-storybook-addon/csf3';

import { withQuery } from './decorators/with-query';
import { withRouter } from './decorators/with-router';
import { withToast } from './decorators/with-toast';

import { handlers } from '@/mocks/handlers';
import '@/styles.css';
// Loaded here for the same reason it is loaded in the app: CSS from the sign-in route is never
// unloaded, so every screen renders with it in the cascade. Without it Storybook runs under a
// cascade the app never has — which is exactly how an Amplify rule that reset `font-size` on
// every input in the app went unnoticed while the font-size assertions passed here.
import '@/amplify-styles.css';

import type { Preview } from '@storybook/react-vite';

const preview: Preview = {
  parameters: {
    controls: { matchers: { color: /(background|color)$/i, date: /Date$/i } },
    // Story-level `parameters.msw.handlers` replaces this list for that story.
    msw: { handlers },
    a11y: { test: 'error' },
  },
  loaders: [mswLoader()],
  decorators: [withQuery, withRouter, withToast],
};

export default preview;
