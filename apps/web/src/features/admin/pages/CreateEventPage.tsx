import { useCreateEvent } from '../hooks/useCreateEvent'
import { CreateEventError } from '../api/CreateEventError'
import type { SubmitEvent } from 'react'

// Admin page: creates an event in DRAFT and assigns it to the owner's email.
export default function CreateEventPage() {
    const createEvent = useCreateEvent()

    const fieldErrors = createEvent.error instanceof CreateEventError
        ? createEvent.error.fieldErrors
        : {}
    const hasFieldErrors = Object.keys(fieldErrors).length > 0

    function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault()
        const form = new FormData(event.currentTarget)
        createEvent.mutate({
            name: String(form.get('name')),
            eventDate: String(form.get('eventDate')),
            ownerEmail: String(form.get('ownerEmail')),
        })
    }

    return (
        <main className="mx-auto flex w-full flex-1 max-w-md flex-col gap-4 p-4">
            <h1 className="text-2xl font-bold">Crear evento</h1>

            <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-3">
                <label className="flex flex-col gap-1">
                    Nombre
                    <input name="name" required maxLength={100} className="rounded border px-3 py-2" />
                    {fieldErrors.name && <span role="alert" className="text-sm text-red-600">{fieldErrors.name}</span>}
                </label>

                <label className="flex flex-col gap-1">
                    Fecha
                    <input name="eventDate" type="date" required className="rounded border px-3 py-2" />
                    {fieldErrors.eventDate && <span role="alert" className="text-sm text-red-600">{fieldErrors.eventDate}</span>}
                </label>

                <label className="flex flex-col gap-1">
                    Correo del dueño
                    <input name="ownerEmail" type="email" required className="rounded border px-3 py-2" />
                    {fieldErrors.ownerEmail && <span role="alert" className="text-sm text-red-600">{fieldErrors.ownerEmail}</span>}
                </label>

                <button
                    type="submit"
                    disabled={createEvent.isPending}
                    className="self-start rounded border px-4 py-2 disabled:opacity-50"
                >
                    {createEvent.isPending ? 'Creando...' : 'Crear evento'}
                </button>
            </form>

            {createEvent.isError && !hasFieldErrors && (
                <p role="alert">No se pudo crear el evento. Intenta de nuevo.</p>
            )}

            {createEvent.data && (
                <p role="status">
                    Evento creado en borrador. Código: <strong>{createEvent.data.publicCode}</strong>
                </p>
            )}
        </main>
    )
}
