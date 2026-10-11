import type { Measurement } from "../types/measurement";

export type MeasurementFilters = {
    from?: string;
    to?: string;
    limit?: number;
};

export async function getMeasurements(
    seriesId: number,
    filters: MeasurementFilters = {},
): Promise<Measurement[]> {
    const params = new URLSearchParams({
        series: String(seriesId),
        sort: "timestamp",
        limit: String(filters.limit ?? 1000),
    });

    if (filters.from) {
        params.set("from", filters.from);
    }

    if (filters.to) {
        params.set("to", filters.to);
    }

    const response = await fetch(`/api/measurements?${params}`);

    if (!response.ok) {
        const problem = await response.json().catch(() => null) as { detail?: string } | null;
        throw new Error(problem?.detail ?? `HTTP ${response.status}`);
    }

    return response.json() as Promise<Measurement[]>;
}