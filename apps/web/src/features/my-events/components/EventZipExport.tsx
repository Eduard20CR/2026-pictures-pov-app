import { ZipExportError } from '../api/ZipExportError'
import { useRequestZipExport } from '../hooks/useRequestZipExport'
import { useZipExport } from '../hooks/useZipExport'

const ERROR_MESSAGES: Record<string, string> = {
    NO_PHOTOS_TO_EXPORT: 'El evento aún no tiene fotos para descargar.',
}

function formatSize(bytes: number): string {
    return `${(bytes / 1_000_000).toLocaleString('es', { maximumFractionDigits: 1 })} MB`
}

// Organizer only: the server answers 403 to a moderator (RN-05). Nothing is sent by email: the status lives here.
export default function EventZipExport({ eventId }: { eventId: string }) {
    const zip = useZipExport(eventId)
    const request = useRequestZipExport(eventId)

    const errorCode = request.error instanceof ZipExportError ? request.error.code : ''
    const status = zip.data?.status
    // null: never requested; undefined: still loading.
    const hasZip = zip.data != null
    const canRequest = zip.data !== undefined && status !== 'IN_PROGRESS'
    const requestLabel = hasZip ? 'Generar ZIP de nuevo' : 'Generar ZIP'

    // Each read signs a new link (RNF-SEG-05); one kept on the page could outlive the signer's credentials.
    async function download() {
        const fresh = await zip.refetch()
        // A failed read keeps the old data, with its old link; the query's error alert shows instead.
        if (fresh.isError) return
        if (fresh.data?.status === 'AVAILABLE' && fresh.data.downloadUrl) window.location.assign(fresh.data.downloadUrl)
    }

    return (
        <section className="flex flex-col gap-3">
            <h2 className="text-xl font-semibold">Descargar todas las fotos</h2>

            {zip.isError && <p role="alert">No se pudo consultar el ZIP. Intenta de nuevo.</p>}

            {status === 'IN_PROGRESS' && (
                <p>ZIP en preparación. Puedes salir de esta página; aquí verás cuando esté listo.</p>
            )}

            {status === 'AVAILABLE' && zip.data?.downloadUrl && (
                <div className="flex flex-wrap items-center gap-2">
                    <button
                        type="button"
                        onClick={download}
                        disabled={zip.isFetching}
                        className="rounded border px-4 py-2 disabled:opacity-50"
                    >
                        Descargar ZIP
                    </button>
                    <span className="text-sm">
                        {zip.data.imageCount} fotos, {formatSize(zip.data.sizeBytes ?? 0)}
                    </span>
                </div>
            )}

            {status === 'FAILED' && <p>No se pudo generar el ZIP. Intenta de nuevo.</p>}

            {canRequest && (
                <button
                    type="button"
                    onClick={() => request.mutate()}
                    disabled={request.isPending}
                    className="self-start rounded border px-4 py-2 disabled:opacity-50"
                >
                    {request.isPending ? 'Solicitando...' : requestLabel}
                </button>
            )}

            {request.isError && (
                <p role="alert">{ERROR_MESSAGES[errorCode] ?? 'No se pudo solicitar el ZIP. Intenta de nuevo.'}</p>
            )}
        </section>
    )
}
