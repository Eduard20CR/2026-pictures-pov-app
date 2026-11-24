import { useAcceptInvitation } from '../hooks/useAcceptInvitation'
import { usePendingInvitations } from '../hooks/usePendingInvitations'
import { ROLE_LABELS } from '../labels'

// Invitations for my verified email; hidden when there are none.
export default function PendingInvitations() {
    const invitations = usePendingInvitations()
    const accept = useAcceptInvitation()

    if (!invitations.data?.length) {
        return null
    }

    return (
        <section aria-labelledby="pending-invitations" className="flex flex-col gap-2">
            <h2 id="pending-invitations" className="text-xl font-semibold">Invitaciones pendientes</h2>
            <ul className="flex flex-col gap-2">
                {invitations.data.map((invitation) => (
                    <li key={invitation.id} className="flex items-center justify-between gap-4 border-b py-2">
                        <span>
                            <strong>{invitation.eventName}</strong> · <span>{ROLE_LABELS[invitation.role]}</span>
                        </span>
                        <button
                            type="button"
                            onClick={() => accept.mutate(invitation.id)}
                            disabled={accept.isPending}
                            className="rounded border px-4 py-2 disabled:opacity-50"
                        >
                            Aceptar
                        </button>
                    </li>
                ))}
            </ul>
            {accept.isError && <p role="alert">No se pudo aceptar la invitación. Intenta de nuevo.</p>}
        </section>
    )
}
