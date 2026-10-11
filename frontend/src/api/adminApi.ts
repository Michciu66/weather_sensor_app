import type { Series } from "../types/series";
import { notifyAuthInvalidated } from "./authApi";

export type Sensor = {
    id: number;
    name: string;
    seriesId: number;
    createdAt: string;
    lastMeasurementAt: string | null;
};

export type SensorCreated = Sensor & {
    apiKey: string;
};

export type SeriesInput = {
    name: string;
    minValue: number;
    maxValue: number;
    color: string;
    icon: string | null;
    unit: string | null;
};

export type SensorInput = {
    name: string;
    seriesId: number;
};

export async function adminRequest(
    path: string,
    token: string,
    init: RequestInit = {},
): Promise<Response> {
    const headers = new Headers(init.headers);

    headers.set("Authorization", `Bearer ${token}`);
    headers.set("Accept", "application/json");

    if (init.body) {
        headers.set("Content-Type", "application/json");
    }

    const response = await fetch(path, { ...init, headers });

    if (response.status === 401) {
        notifyAuthInvalidated();
    }

    return response;
}

export async function readApiError(response: Response): Promise<string> {
    const problem = await response.json().catch(() => null) as {
        detail?: string;
        title?: string;
        errors?: Array<{ field?: string; message?: string }>;
    } | null;

    if (problem?.errors?.length) {
        return problem.errors
            .map((error) => `${error.field ?? "Field"}: ${error.message ?? "Invalid value"}`)
            .join("; ");
    }

    return problem?.detail ?? problem?.title ?? `Request failed: HTTP ${response.status}`;
}

export async function fetchAdminSeries(token: string): Promise<Series[]> {
    const response = await adminRequest("/api/series", token);

    if (!response.ok) {
        throw new Error(await readApiError(response));
    }

    return response.json() as Promise<Series[]>;
}

export async function fetchSensors(token: string): Promise<Sensor[]> {
    const response = await adminRequest("/api/sensors", token);

    if (!response.ok) {
        throw new Error(await readApiError(response));
    }

    return response.json() as Promise<Sensor[]>;
}

export async function saveSeries(
    token: string,
    input: SeriesInput,
    id?: number,
): Promise<void> {
    const response = await adminRequest(
        id ? `/api/series/${id}` : "/api/series",
        token,
        {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(input),
        },
    );

    if (!response.ok) {
        throw new Error(await readApiError(response));
    }
}

export async function deleteSeries(token: string, id: number): Promise<void> {
    const response = await adminRequest(`/api/series/${id}`, token, {
        method: "DELETE",
    });

    if (!response.ok) {
        throw new Error(await readApiError(response));
    }
}

export async function createSensor(
    token: string,
    input: SensorInput,
): Promise<SensorCreated> {
    const response = await adminRequest("/api/sensors", token, {
        method: "POST",
        body: JSON.stringify(input),
    });

    if (!response.ok) {
        throw new Error(await readApiError(response));
    }

    return response.json() as Promise<SensorCreated>;
}

export async function deleteSensor(token: string, id: number): Promise<void> {
    const response = await adminRequest(`/api/sensors/${id}`, token, {
        method: "DELETE",
    });

    if (!response.ok) {
        throw new Error(await readApiError(response));
    }
}