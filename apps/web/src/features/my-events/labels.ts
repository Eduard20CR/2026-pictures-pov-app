import type { MyEvent } from './types'

export const STATUS_LABELS: Record<MyEvent['status'], string> = {
    OPEN: 'Abierto',
    CLOSED: 'Cerrado',
    ARCHIVED: 'Archivado',
}

export const ROLE_LABELS: Record<MyEvent['role'], string> = {
    OWNER: 'Organizador',
    MODERATOR: 'Moderador',
}
