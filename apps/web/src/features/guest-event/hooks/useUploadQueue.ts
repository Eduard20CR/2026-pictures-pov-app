import { useState } from 'react';
import type { UploadItem } from '../types';
import { createPresignedUpload } from '../api/createPresignedUpload'
import { uploadPhotoToS3 } from '../api/uploadPhotoToS3'
import { useQueryClient } from '@tanstack/react-query'
import { UploadError } from '../api/UploadError';

export function useUploadQueue(publicCode: string) {
    const queryClient = useQueryClient()
    const [uploadItems, setUploadItems] = useState<UploadItem[]>([]);


    function addFiles(files: File[]) {
        const newItems: UploadItem[] = files.map((file) => ({
            id: crypto.randomUUID(),
            file,
            status: 'waiting',
        }))
        setUploadItems((prev) => [...prev, ...newItems])
        newItems.forEach((item) => uploadItem(item))
    }

    function updateItem(id: string, changes: Partial<UploadItem>) {
        setUploadItems((prev) => prev.map((item) =>
            (item.id === id ? { ...item, ...changes } : item)
        ))
    }

    async function uploadItem(item: UploadItem) {
        updateItem(item.id, { status: 'uploading', error: undefined })

        try {
            let presignedUpload = item.presignedUpload
            if (!presignedUpload || new Date(presignedUpload.expiresAt).getTime() <= Date.now()) {
                presignedUpload = await createPresignedUpload(publicCode, item.file)
                updateItem(item.id, { presignedUpload })
            }

            await uploadPhotoToS3(presignedUpload, item.file)
            updateItem(item.id, { status: 'done' })
        } catch (reason) {
            const code = reason instanceof UploadError ? reason.code : 'UNKNOWN'
            updateItem(item.id, { status: 'error', error: code })
        } finally {
            queryClient.invalidateQueries({ queryKey: ['events', publicCode, 'quota'] })
        }
    }

    function retryItem(item: UploadItem) {
        uploadItem(item)
    }

    function removeItem(id: string) {
        setUploadItems((prev) => prev.filter((item) => item.id !== id))
    }

    return { uploadItems, addFiles, retryItem, removeItem };
}