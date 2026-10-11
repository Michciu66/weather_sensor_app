import type { Series } from "../types/series";

export async function getSeries(): Promise<Series[]> {
    const response = await fetch("/api/series");

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }

    return response.json();
}