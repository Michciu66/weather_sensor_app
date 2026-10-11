import { useState, type FormEvent } from "react";
import { useAuth } from "../auth/AuthContext";

type LoginFormProps = {
    onSuccess: () => void;
    onCancel: () => void;
};

export function LoginForm({ onSuccess, onCancel }: LoginFormProps) {
    const { login } = useAuth();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setLoading(true);
        setError(null);

        try {
            await login(username, password);
            setPassword("");
            onSuccess();
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "Login failed.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <section className="auth-panel no-print" aria-labelledby="login-heading">
            <h2 id="login-heading">Administrator login</h2>

            <form onSubmit={handleSubmit}>
                <label htmlFor="login-username">Username</label>
                <input
                    id="login-username"
                    name="username"
                    type="text"
                    autoComplete="username"
                    value={username}
                    onChange={(event) => setUsername(event.target.value)}
                    required
                />

                <label htmlFor="login-password">Password</label>
                <input
                    id="login-password"
                    name="password"
                    type="password"
                    autoComplete="current-password"
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    required
                />

                {error && <p className="auth-error" role="alert">{error}</p>}

                <div className="auth-form-actions">
                    <button type="submit" disabled={loading}>
                        {loading ? "Logging in..." : "Log in"}
                    </button>

                    <button type="button" onClick={onCancel} disabled={loading}>
                        Cancel
                    </button>
                </div>
            </form>
        </section>
    );
}