import { setProjectAnnotations } from '@storybook/react-vite';
import { beforeAll } from 'vitest';

import previewAnnotations from './preview';

const annotations = setProjectAnnotations([previewAnnotations]);

beforeAll(annotations.beforeAll);
