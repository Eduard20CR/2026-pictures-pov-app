# 0003. React frontend

- **Status:** Accepted
- **Date:** 2026-10-09

## Context

`apps/web` is a single-page app for guests (upload photos) and organizers (moderate and download). We need a UI library with high job market value.

## Options

| Option | Pro | Con |
|---|---|---|
| React | Highest demand; large ecosystem (React Router, TanStack Query) | Routing and data need extra libraries |
| Angular | Complete framework | Steeper learning curve; too much structure for this app |
| Vue | Gentle learning curve | Lower demand; smaller ecosystem |

## Decision

**React + TypeScript with Vite**, React Router for routing and TanStack Query for server data. It is the most used frontend library in the job market.

## Consequences

- ✅ High-demand technology; common needs covered by mature libraries.
- ⚠️ Library major upgrades must be kept up with.
