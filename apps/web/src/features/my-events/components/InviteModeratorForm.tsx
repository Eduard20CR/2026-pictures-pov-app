import type { SubmitEvent } from 'react'
import { InviteModeratorError } from '../api/InviteModeratorError'
import { useInviteModerator } from '../hooks/useInviteModerator'

const ERROR_MESSAGES: Record<string, string> = {
    INVITATION_ALREADY_EXISTS: 'Ese correo ya tiene una invitación a este evento.',
    VALIDATION_FAILED: 'Escribe un correo válido.',
}

// Organizer only: the server answers 403 to a moderator (RN-05).
export default function InviteModeratorForm({ eventId }: { eventId: string }) {
    const invite = useInviteModerator(eventId)

    const errorCode = invite.error instanceof InviteModeratorError ? invite.error.code : ''

    function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault()
        const form = event.currentTarget
        invite.mutate(String(new FormData(form).get('email')), { onSuccess: () => form.reset() })
    }

    return (
        <section className="flex flex-col gap-3">
            <h2 className="text-xl font-semibold">Invitar moderador</h2>

            <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-3">
                <label className="flex flex-col gap-1">
                    Correo del moderador
                    <input name="email" type="email" required maxLength={320} className="rounded border px-3 py-2" />
                </label>

                <button
                    type="submit"
                    disabled={invite.isPending}
                    className="self-start rounded border px-4 py-2 disabled:opacity-50"
                >
                    {invite.isPending ? 'Invitando...' : 'Invitar moderador'}
                </button>
            </form>

            {invite.isError && (
                <p role="alert">{ERROR_MESSAGES[errorCode] ?? 'No se pudo enviar la invitación. Intenta de nuevo.'}</p>
            )}

            {invite.data && (
                <p role="status">
                    Invitación pendiente para <strong>{invite.data.email}</strong>.
                </p>
            )}
        </section>
    )
}
