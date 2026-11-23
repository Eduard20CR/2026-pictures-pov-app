// Public guest link of an event (RF-ORG-02); also encoded in its QR code (RF-ORG-03).
export function eventLink(publicCode: string): string {
    return `${window.location.origin}/events/${publicCode}`
}
