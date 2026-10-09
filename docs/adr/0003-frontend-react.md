# 0003. React frontend

- **Status:** Accepted
- **Date:** 2026-10-09

## Context and problem statement

The web application (`apps/web`) is a single-page application used by guests to upload photos and by organizers to moderate and download them. A UI library must be chosen for it. As with [decision 0001](0001-backend-spring-boot-en-lambda.md), one of the project's goals is for the experience gained to have the greatest possible value in the job market.

## Decision drivers

- Job market demand for the technology.
- Maturity of the ecosystem and availability of libraries for routing and server data management.
- Learning value for the team.

## Considered options

1. React.
2. Angular.
3. Vue.

## Decision

**Option 1 is chosen: React with TypeScript**, built with Vite.

- Routing is handled with React Router.
- Server data (fetching, caching and synchronization with the API) is handled with TanStack Query.

The main reason is that React is the most widely used frontend library in the job market and has the largest ecosystem of libraries, which also makes it a good technology to learn.

## Pros and cons of the options

### Option 1: React

- Pro: highest job market demand; large ecosystem with mature libraries such as React Router and TanStack Query; extensive documentation and community.
- Con: it is a library rather than a complete framework, so routing and data management require choosing and integrating additional libraries.

### Option 2: Angular

- Pro: complete framework with routing, forms and HTTP client included.
- Con: steeper learning curve and more structure than an application of this size requires.

### Option 3: Vue

- Pro: gentle learning curve and good official tooling.
- Con: lower job market demand and a smaller ecosystem than React.

## Consequences

### Positive

- The frontend is developed with a technology in high job market demand.
- Common needs are covered by widely adopted libraries instead of custom code.

### Negative and risks

- The selected libraries (React Router, TanStack Query) must be kept up to date and their major version upgrades managed.

## Related decisions

- [0001. Spring Boot backend on AWS Lambda](0001-backend-spring-boot-en-lambda.md)
- [0002. npm without workspaces](0002-npm-sin-workspaces.md)
