import { useState } from "react";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import { LoginForm } from "./components/LoginForm";
import { AdminPage } from "./pages/AdminPage";
import { MeasurementsPage } from "./pages/MeasurementsPage";
import { ChangePasswordForm } from "./components/ChangePasswordForm";
import "./App.css";

type AppView = "measurements" | "admin";

function AppContent() {
  const { isAuthenticated, logout, authNotice, clearAuthNotice } = useAuth();
  const [activeView, setActiveView] = useState<AppView>("measurements");
  const [loginOpen, setLoginOpen] = useState(false);
  const [logoutError, setLogoutError] = useState<string | null>(null);
  const [passwordChangeOpen, setPasswordChangeOpen] = useState(false);

  async function handleLogout() {
    setLogoutError(null);

    try {
      await logout();
    } catch {
      setLogoutError(
          "The local session was cleared, but the server did not confirm logout. " +
          "The token may remain valid until it expires.",
      );
    } finally {
      setActiveView("measurements");
      setPasswordChangeOpen(false);
    }
  }

  return (
      <>
        <nav className="auth-toolbar no-print" aria-label="Main navigation">

          {authNotice && (
              <div className="auth-session-notice no-print" role="alert">
                <span>{authNotice}</span>
                <button type="button" onClick={clearAuthNotice}>
                  Dismiss
                </button>
              </div>
          )}

          <div className="auth-status">
            <strong>Weather Sensors</strong>
            <span>
                        {isAuthenticated
                            ? "Authenticated administrator"
                            : "Public read-only access"}
                    </span>
          </div>

          <div className="auth-navigation">
            <button
                type="button"
                aria-current={activeView === "measurements" ? "page" : undefined}
                onClick={() => setActiveView("measurements")}
            >
              Measurements
            </button>

            {isAuthenticated && (
                <button
                    type="button"
                    aria-current={activeView === "admin" ? "page" : undefined}
                    onClick={() => setActiveView("admin")}
                >
                  Admin panel
                </button>
            )}

            {isAuthenticated ? (
                <button type="button" onClick={() => void handleLogout()}>
                  Log out
                </button>
            ) : (
                <button
                    type="button"
                    onClick={() => setLoginOpen((current) => !current)}
                >
                  Log in
                </button>
            )}
            {isAuthenticated && (
                <button
                    type="button"
                    aria-expanded={passwordChangeOpen}
                    onClick={() => setPasswordChangeOpen((current) => !current)}
                >
                  Change password
                </button>
            )}
          </div>
        </nav>

        {logoutError && (
            <p className="auth-error no-print" role="alert">
              {logoutError}
            </p>
        )}

        {passwordChangeOpen && isAuthenticated && (
            <ChangePasswordForm
                onCancel={() => setPasswordChangeOpen(false)}
                onSuccess={() => setPasswordChangeOpen(false)}
            />
        )}

        {loginOpen && !isAuthenticated && (
            <LoginForm
                onSuccess={() => {
                  setLoginOpen(false);
                  setActiveView("admin");
                }}
                onCancel={() => setLoginOpen(false)}
            />
        )}

        {activeView === "admin" && isAuthenticated
            ? <AdminPage />
            : <MeasurementsPage />}
      </>
  );
}

export default function App() {
  return (
      <AuthProvider>
        <AppContent />
      </AuthProvider>
  );
}