type Props = {
    title: string
    text: string
}

export function EventStatusScreen({ title, text }: Props) {
    return (
        <main className="flex min-h-screen flex-col items-center justify-center gap-2 p-4 text-center">
            <h1 className="text-2xl font-bold">{title}</h1>
            <p>{text}</p>
        </main>
    )
}
