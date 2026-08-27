import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { SchemeEditor } from '@/components/chemistry/scheme-editor';

// The real module pulls in ~28 MB of sketcher and Indigo WASM, neither of which runs
// under jsdom.
vi.mock('@/lib/ketcher', () => ({
  renderStructure: () => Promise.resolve('data:image/svg+xml;base64,PHN2Zy8+'),
}));

describe('SchemeEditor', () => {
  it('offers to draw one when there is no structure', () => {
    render(<SchemeEditor value={null} onChange={() => {}} />);

    expect(screen.getByRole('button', { name: 'Draw Structure' })).toBeInTheDocument();
    expect(screen.queryByRole('img')).not.toBeInTheDocument();
  });

  it('renders the structure and an edit affordance once there is one', async () => {
    render(<SchemeEditor value="a-molfile" onChange={() => {}} />);

    expect(await screen.findByRole('img', { name: 'Chemical structure' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Edit structure' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Draw Structure' })).not.toBeInTheDocument();
  });
});
