import { readFileSync } from 'node:fs';

import { describe, expect, it } from 'vitest';

const read = (path: string) => readFileSync(new URL(path, import.meta.url), 'utf8');

/**
 * Amplify's stylesheet is global and unlayered — `input, button, textarea, select { font: inherit }`
 * resets the font size of every form control on the page, and `html { font-family: … }` replaces
 * the app's typeface. It reached the whole app because CSS imported by a route chunk is injected
 * once and never removed, so one visit to `/login` styled every screen for the life of the tab.
 *
 * Two independent things now stop that, and this pins both. They are worth pinning because each
 * fails *silently*: nothing throws, the sign-in screen still looks correct, and the damage shows
 * up somewhere else entirely as a control ignoring a class that the Elements panel says applies.
 *
 * Asserted against the source rather than the imported stylesheet because the unit project does
 * not process CSS — an `import '…css?inline'` resolves to an empty string here, which would make
 * the obvious version of this test pass for the wrong reason.
 */
describe('containing the Amplify stylesheet', () => {
  const login = read('./login.tsx');

  it('imports it as text, so the route can mount and unmount it', () => {
    expect(login).toMatch(/import\s+\w+\s+from\s+'@\/amplify-styles\.css\?inline'/);
  });

  it('never imports it for its side effect, which would inject it permanently', () => {
    // The failure mode: drop `?inline` and the bundler injects the sheet, the imported binding is
    // undefined, and the <style> this route mounts is empty.
    expect(login).not.toMatch(/^\s*import\s+'[^']*(amplify-styles|ui-react\/styles)\.css'/m);
  });

  it('removes it again when the screen goes away', () => {
    expect(login).toContain('style.remove()');
  });

  it('keeps it in a cascade layer as well, for if that cleanup ever fails', () => {
    expect(read('../amplify-styles.css')).toContain('layer(amplify)');
    // Layers rank in declaration order, so ours has to be declared before Tailwind's.
    const styles = read('../styles.css');
    expect(styles.indexOf('@layer amplify;')).toBeGreaterThanOrEqual(0);
    expect(styles.indexOf('@layer amplify;')).toBeLessThan(styles.indexOf("@import 'tailwindcss'"));
  });
});
