export class UploadError extends Error {
    readonly code: string

    constructor(code: string) {
        super(code)
        this.code = code
    }
}