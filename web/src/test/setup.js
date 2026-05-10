import { expect, afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import * as matchers from '@testing-library/jest-dom/matchers';

// This is the missing link that connects the two!
expect.extend(matchers);

afterEach(() => {
  cleanup();
});