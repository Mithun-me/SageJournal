// Base URL for the Aura backend.
//
// Web (Vite dev server or static hosting): left empty, so every call is
// same-origin and hits the Express routes in server.ts directly.
//
// Android (Capacitor): the bundled web assets are served from https://localhost
// inside the WebView, and that origin has no backend of its own. VITE_API_BASE_URL
// must therefore point at a reachable server, and it is inlined at build time.
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '');

export const apiUrl = (path: string) => `${API_BASE_URL}${path}`;

export async function postJson<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(apiUrl(path), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    throw new Error(`${path} responded ${res.status}`);
  }
  return (await res.json()) as T;
}
