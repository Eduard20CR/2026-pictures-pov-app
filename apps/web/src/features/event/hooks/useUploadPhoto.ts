import { useMutation, useQueryClient } from '@tanstack/react-query'
import { requestUpload } from '../api/requestUpload'
import { uploadToS3 } from '../api/uploadToS3'

export function useUploadPhoto(publicCode: string) {
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: async (file: File) => {
            const ticket = await requestUpload(publicCode, file)
            await uploadToS3(ticket, file)
        },
        onSettled: () =>
            queryClient.invalidateQueries({ queryKey: ['events', publicCode, 'quota'] }),
    })
}