import { useQuery } from '@tanstack/react-query'

type Health = { status: string }

const API_URL = import.meta.env.VITE_API_URL ?? ''

async function fetchHealth(): Promise<Health> {
  const response = await fetch(`${API_URL}/actuator/health`)
  if (!response.ok) {
    throw new Error(`Health check failed: ${response.status}`)
  }
  return response.json()
}

function HealthCheck() {
  const { data, error, isPending, refetch, isFetching } = useQuery({
    queryKey: ['health'],
    queryFn: fetchHealth,
  })

  return (
    <div className="p-4">
      <h1 className="text-xl font-semibold">API health</h1>
      {isPending && <p>Checking…</p>}
      {error && <p className="text-red-600">{error.message}</p>}
      {data && (
        <p className={data.status === 'UP' ? 'text-green-600' : 'text-red-600'}>
          Status: {data.status}
        </p>
      )}
      <button
        type="button"
        className="mt-2 rounded border px-3 py-1 disabled:opacity-50"
        onClick={() => refetch()}
        disabled={isFetching}
      >
        Retry
      </button>
    </div>
  )
}

export default HealthCheck
