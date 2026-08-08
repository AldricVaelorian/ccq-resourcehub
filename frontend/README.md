# ResourceHub Frontend

React frontend application for ResourceHub booking and lending system.

## Tech Stack

- React 18+
- TypeScript
- Vite (build tool)
- Tailwind CSS
- TanStack Query (data fetching)
- Axios (HTTP client)

## Development

### Prerequisites

- Node.js 20+ (LTS)
- npm 10+

### Install Dependencies

```bash
npm install
```

### Run Development Server

```bash
npm start
```

The application will start on `http://localhost:5173`

### Build for Production

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

## Project Structure

```
frontend/
├── src/
│   ├── components/   # Reusable UI components
│   ├── pages/        # Page components
│   ├── hooks/        # Custom React hooks
│   ├── services/     # API client services
│   ├── types/        # TypeScript type definitions
│   ├── utils/        # Utility functions
│   ├── App.tsx
│   └── main.tsx
├── public/           # Static assets
├── vite.config.ts
├── tsconfig.json
└── package.json
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_URL` | Backend API URL | `http://localhost:8080/api` |

## Available Scripts

- `npm start` - Start development server with hot reload
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run type-check` - Run TypeScript type checking

## Development Guidelines

- Follow React best practices and hooks conventions
- Use TypeScript for type safety
- Implement responsive design with mobile-first approach
- Write unit tests for components and hooks
- Use TanStack Query for data fetching and caching

## Integration with Backend

The frontend communicates with the ResourceHub backend API. Ensure the backend is running and accessible before starting the frontend development server.