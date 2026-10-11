import { useState, type FormEvent } from "react";
import { useAuth } from "../auth/AuthContext";
import { changePasswordRequest } from "../api/authApi";

type ChangePasswordFormProps = {
    onCancel: () => void;
    onSuccess: () => void;
};

export function ChangePasswordForm({ onCancel, onSuccess }: ChangePasswordFormProps) {
    const { session } = useAuth();

    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError(null);
        setSuccess(null);

        if (!session) {
            setError("You must be logged in to change your password.");
            return;
        }

        if (newPassword.length < 8) {
            setError("The new password must contain at least 8 characters.");
            return;
        }

        if (newPassword !== confirmPassword) {
            setError("The new passwords do not match.");
            return;
        }

        setLoading(true);

        try {
            await changePasswordRequest(
                session.accessToken,
                session.tokenType,
                currentPassword,
                newPassword,
            );

            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");
            onSuccess();
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "Could not change password.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <section className="auth-panel no-print" aria-labelledby="change-password-heading">
            <h2 id="change-password-heading">Change password</h2>

            <form onSubmit={handleSubmit}>
                <label htmlFor="current-password">Current password</label>
                <input
                    id="current-password"
                    name="currentPassword"
                    type="password"
                    autoComplete="current-password"
                    value={currentPassword}
                    onChange={(event) => setCurrentPassword(event.target.value)}
                    required
                />

                <label htmlFor="new-password">New password</label>
                <input
                    id="new-password"
                    name="newPassword"
                    type="password"
                    autoComplete="new-password"
                    minLength={8}
                    value={newPassword}
                    onChange={(event) => setNewPassword(event.target.value)}
                    required
                />

                <label htmlFor="confirm-password">Confirm new password</label>
                <input
                    id="confirm-password"
                    name="confirmPassword"
                    type="password"
                    autoComplete="new-password"
                    minLength={8}
                    value={confirmPassword}
                    onChange={(event) => setConfirmPassword(event.target.value)}
                    required
                />

                {error && <p className="auth-error" role="alert">{error}</p>}

                {success && <p className="auth-notice" role="status">{success}</p>}

                <div className="auth-form-actions">
                    <button type="submit" disabled={loading}>
                        {loading ? "Changing password..." : "Change password"}
                    </button>

                    <button type="button" onClick={onCancel} disabled={loading}>
                        Cancel
                    </button>
                </div>
            </form>
        </section>
    );
}