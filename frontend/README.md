# ResourceHub Frontend

React frontend application for ResourceHub booking and lending system.

## Tech Stack

- React 19
- TypeScript 6
- Vite 8
- Tailwind CSS 4

## Development

### Prerequisites

- Node.js 24 (LTS)
- npm 11+

### Install Dependencies

```bash
npm ci --include=dev
```

### Run Development Server

```bash
npm run dev
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

`.env.example` reserves the intended API base URL variable for the later API
integration work. The current frontend scaffold does not read this variable
yet. When integration is added, copy `.env.example` to `.env.local` to provide
local values. Only variables prefixed with `VITE_` are exposed to frontend
code.

| Variable | Description | Example value |
|----------|-------------|---------------|
| `VITE_API_URL` | Reserved backend API base URL | `/api` |

## Available Scripts

- `npm run dev` - Start development server with hot reload
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run typecheck` - Run TypeScript type checking

## Development Guidelines

- Follow React best practices and hooks conventions
- Use TypeScript for type safety
- Implement responsive design with mobile-first approach
- Keep backend URLs in `VITE_` environment variables

## Integration with Backend

The frontend communicates with the ResourceHub backend API. Ensure the backend is running and accessible before starting the frontend development server.
