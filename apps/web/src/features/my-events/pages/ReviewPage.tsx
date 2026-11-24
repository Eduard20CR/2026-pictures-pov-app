import { Link, useParams } from 'react-router'
import { useMyEvent } from '../hooks/useMyEvent'
import { useReviewImages } from '../hooks/useReviewImages'
import { useDownloadImage } from '../hooks/useDownloadImage'

// Private page: RequireAuth guarantees there is a session here.
// The review: every photo of the event, hidden ones included (RF-ORG-10, RF-MOD-02).
export default function ReviewPage() {
    const { id = '' } = useParams()
    const event = useMyEvent(id)
    const photos = useReviewImages(id)
    const items = photos.data?.pages.flatMap((page) => page.items) ?? []
    const downloadImage = useDownloadImage(id)
    // Downloading originals is the organizer's only (RN-05); a photo still processing has none to offer yet.
    const canDownload = event.data?.role === 'OWNER'

    return (
        <main className="mx-auto flex w-full max-w-5xl flex-1 flex-col gap-4 p-4">
            <Link to={`/my-events/${id}`} className="self-start text-sm underline">
                Volver al evento
            </Link>

            {event.isPending && <p>Cargando evento...</p>}
            {event.isError && <p role="alert">No se pudo cargar el evento. Intenta de nuevo.</p>}
            {event.data === null && <h1 className="text-2xl font-bold">Evento no encontrado</h1>}
            {event.data && (
                <>
                    <div>
                        <h1 className="text-2xl font-bold">Revisar fotos</h1>
                        <p>{event.data.name}</p>
                    </div>

                    {photos.isPending && <p>Cargando fotos...</p>}
                    {photos.isError && <p role="alert">No se pudieron cargar las fotos. Intenta de nuevo.</p>}
                    {downloadImage.isError && <p role="alert">No se pudo descargar la foto. Intenta de nuevo.</p>}
                    {photos.data && items.length === 0 && <p>Este evento aún no tiene fotos.</p>}

                    <ul className="grid grid-cols-3 gap-2 sm:grid-cols-4 md:grid-cols-6">
                        {items.map((photo, index) => (
                            <li key={photo.id} className="relative aspect-square overflow-hidden rounded bg-gray-200">
                                {photo.thumbnailUrl ? (
                                    <img src={photo.thumbnailUrl} alt={`Foto ${index + 1}`} loading="lazy"
                                         className="h-full w-full object-cover" />
                                ) : (
                                    <span className="flex h-full items-center justify-center p-1 text-center text-xs text-gray-600">
                                        Procesando foto
                                    </span>
                                )}
                                {photo.hidden && (
                                    <span className="absolute left-1 top-1 rounded bg-black/75 px-1.5 py-0.5 text-xs font-semibold text-white">
                                        Oculta
                                    </span>
                                )}
                                {canDownload && photo.thumbnailUrl && (
                                    <button type="button" onClick={() => downloadImage.mutate(photo.id)}
                                            aria-label={`Descargar foto ${index + 1}`}
                                            className="absolute bottom-1 right-1 rounded bg-white/90 px-1.5 py-0.5 text-xs font-semibold">
                                        Descargar
                                    </button>
                                )}
                            </li>
                        ))}
                    </ul>

                    {photos.hasNextPage && (
                        <button type="button" onClick={() => photos.fetchNextPage()} disabled={photos.isFetchingNextPage}
                                className="self-center rounded border px-3 py-1 text-sm">
                            {photos.isFetchingNextPage ? 'Cargando...' : 'Cargar más fotos'}
                        </button>
                    )}
                </>
            )}
        </main>
    )
}
