import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from "react";
import { AUTH_INVALID_EVENT, getErrorMessage, loginRequest } from "../api/authApi";

const STORAGE_KEY = "weatherSensors.authSession";

export type AuthSession = {
    accessToken: string;
    tokenType: string;
    expiresAt: number;
};

type AuthContextValue = {
    session: AuthSession | null;
    isAuthenticated: boolean;
    login: (username: string, password: string) => Promise<void>;
    logout: () => Promise<void>;
    authNotice: string | null;
    clearAuthNotice: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function readStoredSession(): AuthSession | null {
    try {
        const stored = sessionStorage.getItem(STORAGE_KEY);

        if (!stored) {
            return null;
        }

        const session = JSON.parse(stored) as AuthSession;

        if (
            !session.accessToken ||
            !session.tokenType ||
            !Number.isFinite(session.expiresAt) ||
            session.expiresAt <= Date.now()
        ) {
            sessionStorage.removeItem(STORAGE_KEY);
            return null;
        }

        return session;
    } catch {
        sessionStorage.removeItem(STORAGE_KEY);
        return null;
    }
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const [session, setSession] = useState<AuthSession | null>(readStoredSession);
    const [authNotice, setAuthNotice] = useState<string | null>(null);


    const clearAuthNotice = useCallback(() => {
        setAuthNotice(null);
    }, []);

    const login = useCallback(async (username: string, password: string) => {
        const response = await loginRequest(username, password);

        if (!response.accessToken || response.expiresIn <= 0) {
            throw new Error("The server returned an invalid login response.");
        }

        const nextSession: AuthSession = {
            accessToken: response.accessToken,
            tokenType: response.tokenType || "Bearer",
            expiresAt: Date.now() + response.expiresIn * 1000,
        };

        setAuthNotice(null);

        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(nextSession));
        setSession(nextSession);
    }, []);

    const logout = useCallback(async () => {
        try {
            if (session && session.expiresAt > Date.now()) {
                const response = await fetch("/api/auth/logout", {
                    method: "POST",
                    headers: {
                        Authorization: `${session.tokenType} ${session.accessToken}`,
                        Accept: "application/json",
                    },
                });

                if (!response.ok && response.status !== 401) {
                    throw new Error(await getErrorMessage(response));
                }
            }
        } finally {
            sessionStorage.removeItem(STORAGE_KEY);
            setSession(null);
            setAuthNotice(null);
        }
    }, [session]);

    useEffect(() => {
        if (!session) {
            return;
        }

        const timeout = window.setTimeout(() => {
            sessionStorage.removeItem(STORAGE_KEY);
            setSession(null);
            setAuthNotice("Your session has expired. Please log in again.");
        }, Math.max(0, session.expiresAt - Date.now()));

        return () => window.clearTimeout(timeout);
    }, [session]);

    const value = useMemo<AuthContextValue>(
        () => ({
            session,
            isAuthenticated: session !== null && session.expiresAt > Date.now(),
            login,
            logout,
            authNotice,
            clearAuthNotice,
        }),
        [session, login, logout, authNotice, clearAuthNotice],
    );

    useEffect(() => {
        const handleAuthInvalidated = () => {
            sessionStorage.removeItem(STORAGE_KEY);
            setSession(null);
            setAuthNotice("Your session has expired or was invalidated. Please log in again.");
        };

        window.addEventListener(AUTH_INVALID_EVENT, handleAuthInvalidated);

        return () => {
            window.removeEventListener(AUTH_INVALID_EVENT, handleAuthInvalidated);
        };
    }, []);


    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider.");
    }

    return context;
}