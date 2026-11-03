import type { UploadItem } from '../types'

const ERROR_MESSAGES: Record<string, string> = {
    UPLOAD_LIMIT_REACHED: 'Ya subiste todas las fotos permitidas.',
    UNSUPPORTED_CONTENT_TYPE: 'Solo se aceptan fotos JPEG, PNG, WebP o HEIC.',
    FILE_TOO_LARGE: 'La foto pesa más de 20 MB.',
    EVENT_NOT_FOUND: 'Este evento ya no está disponible.',
    EVENT_CLOSED: 'Este evento ya no recibe fotos.',
}

const PERMANENT_ERRORS = new Set([
    'UPLOAD_LIMIT_REACHED',
    'UNSUPPORTED_CONTENT_TYPE',
    'FILE_TOO_LARGE',
    'EVENT_NOT_FOUND',
    'EVENT_CLOSED',
])

const STATUS_LABELS: Record<UploadItem['status'], string> = {
    waiting: 'En espera',
    uploading: 'Subiendo...',
    done: 'Lista ✓',
    error: 'Falló',
}

function canRetry(code: string): boolean {
    return !PERMANENT_ERRORS.has(code)
}

function errorMessage(code: string): string {
    return ERROR_MESSAGES[code] ?? 'No pudimos subir la foto. Intenta de nuevo.'
}

type UploadListProps = {
    items: UploadItem[]
    onRetry: (item: UploadItem) => void
    onRemove: (id: string) => void
}

export function UploadList({ items, onRetry, onRemove }: UploadListProps) {
    return (
        <ul className="flex flex-col gap-2">
            {items.map((item) => (
                <UploadRow key={item.id} item={item} onRetry={onRetry} onRemove={onRemove} />
            ))}
        </ul>
    )
}

type RowProps = {
    item: UploadItem
    onRetry: (item: UploadItem) => void
    onRemove: (id: string) => void
}

function UploadRow({ item, onRetry, onRemove }: RowProps) {
    const failed = item.status === 'error'
    const showRetry = failed && item.error !== undefined && canRetry(item.error)

    return (
        <li className="flex flex-col">
            <div className="flex justify-between">
                <span className="truncate">{item.file.name}</span>
                <span className="flex gap-2">
                    {STATUS_LABELS[item.status]}
                    {failed && (
                        <button
                            type="button"
                            aria-label={`Quitar ${item.file.name}`}
                            onClick={() => onRemove(item.id)}
                        >
                            ✕
                        </button>
                    )}
                </span>
            </div>
            {item.error && (
                <p role="alert" className="text-sm text-red-600">
                    {errorMessage(item.error)}
                </p>
            )}
            {showRetry && (
                <button
                    type="button"
                    className="self-start text-sm underline"
                    onClick={() => onRetry(item)}
                >
                    Reintentar
                </button>
            )}
        </li>
    )
}
