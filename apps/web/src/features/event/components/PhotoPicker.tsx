import type { ChangeEvent } from 'react'

const ACCEPTED_TYPES = 'image/jpeg,image/png,image/webp,image/heic'

type Props = {
    color: string
    disabled: boolean
    maxFiles: number
    onSelect: (files: File[]) => void
}

export function PhotoPicker({ color, disabled, maxFiles, onSelect }: Props) {
    function handleChange(event: ChangeEvent<HTMLInputElement>) {
        const files = Array.from(event.target.files ?? []).slice(0, maxFiles)
        event.target.value = ''
        if (files.length > 0) {
            onSelect(files)
        }
    }

    const buttonClass =
        'flex-1 cursor-pointer rounded-lg py-3 text-center font-semibold text-white has-[:disabled]:cursor-not-allowed has-[:disabled]:opacity-60'

    return (
        <div className="mt-auto flex gap-3">
            <label className={buttonClass} style={{ backgroundColor: color }}>
                Tomar foto
                <input
                    type="file"
                    accept={ACCEPTED_TYPES}
                    capture="environment"
                    className="hidden"
                    disabled={disabled}
                    onChange={handleChange}
                />
            </label>
            <label className={buttonClass} style={{ backgroundColor: color }}>
                Elegir de galería
                <input
                    type="file"
                    accept={ACCEPTED_TYPES}
                    multiple
                    className="hidden"
                    disabled={disabled}
                    onChange={handleChange}
                />
            </label>
        </div>
    )
}
