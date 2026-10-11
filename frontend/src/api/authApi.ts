export type LoginResponse = {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
};

export async function getErrorMessage(response: Response): Promise<string> {
    const problem = await response.json().catch(() => null) as {
        detail?: string;
        title?: string;
    } | null;

    return problem?.detail ?? problem?.title ?? `Request failed: HTTP ${response.status}`;
}

export async function loginRequest(
    username: string,
    password: string,
): Promise<LoginResponse> {
    const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
        },
        body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
        throw new Error(await getErrorMessage(response));
    }

    return response.json() as Promise<LoginResponse>;
}

export async function changePasswordRequest(
    accessToken: string,
    tokenType: string,
    currentPassword: string,
    newPassword: string,
): Promise<void> {
    const response = await fetch("/api/auth/password", {
        method: "PUT",
        headers: {
            Authorization: `${tokenType} ${accessToken}`,
            "Content-Type": "application/json",
            Accept: "application/json",
        },
        body: JSON.stringify({ currentPassword, newPassword }),
    });

    if (response.status === 401) {
        notifyAuthInvalidated();
    }

    if (!response.ok) {
        const problem = await response.json().catch(() => null) as {
            detail?: string;
            title?: string;
            errors?: Array<{ field?: string; message?: string }>;
        } | null;

        if (problem?.errors?.length) {
            throw new Error(
                problem.errors
                    .map((item) => `${item.field ?? "Field"}: ${item.message ?? "Invalid value"}`)
                    .join("; "),
            );
        }

        throw new Error(
            problem?.detail ?? problem?.title ?? `Password change failed: HTTP ${response.status}`,
        );
    }
}

export const AUTH_INVALID_EVENT = "weather-sensors:auth-invalidated";

export function notifyAuthInvalidated(): void {
    if (typeof window !== "undefined") {
        window.dispatchEvent(new Event(AUTH_INVALID_EVENT));
    }
}