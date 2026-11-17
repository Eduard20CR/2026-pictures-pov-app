# Web

React + Vite + TypeScript frontend ([ADR 0003](../../docs/adr/0003-frontend-react.md)). Setup in the [root README](../../README.md#getting-started).

| Command | Does |
|---|---|
| `npm run dev` | Dev server on port 5173; proxies `/api` to `localhost:8080` |
| `npm run lint && npm test && npm run build` | Same checks as CI |
| `npm run api:types` | Regenerates `src/api/schema.d.ts` from `src/api/openapi.yaml` |

`src/api/openapi.yaml` and `src/api/schema.d.ts` are generated from the API's OpenAPI contract; never edit them by hand. `src/api/client.ts` is the hand-written client. Sign-in uses Cognito through `oidc-client-ts`; copy `.env.example` to `.env` for its values.
