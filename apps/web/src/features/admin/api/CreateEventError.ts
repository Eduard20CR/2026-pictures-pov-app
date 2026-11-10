export type FieldErrors = Record<string, string>

export class CreateEventError extends Error {
    readonly code: string
    readonly fieldErrors: FieldErrors

    constructor(code: string, fieldErrors: FieldErrors = {}) {
        super(code)
        this.code = code
        this.fieldErrors = fieldErrors
    }
}