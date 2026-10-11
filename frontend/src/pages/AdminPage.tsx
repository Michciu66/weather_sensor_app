import { useCallback, useEffect, useState, type FormEvent } from "react";
import { useAuth } from "../auth/AuthContext";
import {
    createSensor,
    deleteSensor,
    deleteSeries,
    fetchAdminSeries,
    fetchSensors,
    saveSeries,
    type Sensor,
    type SensorCreated,
    type SeriesInput,
} from "../api/adminApi";
import type { Series } from "../types/series";
import "./AdminPage.css";

type SeriesForm = {
    name: string;
    minValue: string;
    maxValue: string;
    color: string;
    icon: string;
    unit: string;
};

type SensorForm = {
    name: string;
    seriesId: string;
};

const EMPTY_SERIES_FORM: SeriesForm = {
    name: "",
    minValue: "0",
    maxValue: "100",
    color: "#3366FF",
    icon: "",
    unit: "",
};

const EMPTY_SENSOR_FORM: SensorForm = {
    name: "",
    seriesId: "",
};

function formatTimestamp(value: string | null): string {
    return value ? new Date(value).toLocaleString("pl-PL") : "Never";
}

export function AdminPage() {
    const { session } = useAuth();
    const token = session?.accessToken;

    const [series, setSeries] = useState<Series[]>([]);
    const [sensors, setSensors] = useState<Sensor[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [notice, setNotice] = useState<string | null>(null);

    const [seriesForm, setSeriesForm] = useState<SeriesForm>(EMPTY_SERIES_FORM);
    const [editingSeriesId, setEditingSeriesId] = useState<number | null>(null);

    const [sensorForm, setSensorForm] = useState<SensorForm>(EMPTY_SENSOR_FORM);
    const [createdSensor, setCreatedSensor] = useState<SensorCreated | null>(null);
    const [keyCopied, setKeyCopied] = useState(false);

    const refreshData = useCallback(async () => {
        if (!token) {
            setSeries([]);
            setSensors([]);
            setLoading(false);
            return;
        }

        setLoading(true);

        try {
            const [loadedSeries, loadedSensors] = await Promise.all([
                fetchAdminSeries(token),
                fetchSensors(token),
            ]);

            setSeries(loadedSeries);
            setSensors(loadedSensors);
            setError(null);
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "Could not load administration data.");
        } finally {
            setLoading(false);
        }
    }, [token]);

    useEffect(() => {
        void refreshData();
    }, [refreshData]);

    function resetSeriesForm() {
        setSeriesForm(EMPTY_SERIES_FORM);
        setEditingSeriesId(null);
    }

    function startEditingSeries(item: Series) {
        setSeriesForm({
            name: item.name,
            minValue: String(item.minValue),
            maxValue: String(item.maxValue),
            color: item.color || "#3366FF",
            icon: item.icon ?? "",
            unit: item.unit ?? "",
        });

        setEditingSeriesId(item.id);
        setNotice(null);
        setError(null);

        document.getElementById("series-form-heading")?.scrollIntoView({
            behavior: "smooth",
            block: "start",
        });
    }

    async function handleSaveSeries(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        if (!token) {
            setError("You must be logged in to manage series.");
            return;
        }

        const minValue = Number(seriesForm.minValue);
        const maxValue = Number(seriesForm.maxValue);

        if (
            !seriesForm.name.trim() ||
            !Number.isFinite(minValue) ||
            !Number.isFinite(maxValue) ||
            minValue >= maxValue
        ) {
            setError("Enter a name and valid numeric bounds where minValue is less than maxValue.");
            return;
        }

        const input: SeriesInput = {
            name: seriesForm.name.trim(),
            minValue,
            maxValue,
            color: seriesForm.color,
            icon: seriesForm.icon.trim() || null,
            unit: seriesForm.unit.trim() || null,
        };

        setSaving(true);
        setError(null);
        setNotice(null);

        try {
            await saveSeries(token, input, editingSeriesId ?? undefined);
            setNotice(editingSeriesId === null ? "Series created." : "Series updated.");
            resetSeriesForm();
            await refreshData();
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "Could not save series.");
        } finally {
            setSaving(false);
        }
    }

    async function handleDeleteSeries(item: Series) {
        if (!token || !window.confirm(
            `Delete series "${item.name}"? Its measurements and associated sensors may also be deleted.`,
        )) {
            return;
        }

        setError(null);
        setNotice(null);

        try {
            await deleteSeries(token, item.id);

            if (editingSeriesId === item.id) {
                resetSeriesForm();
            }

            setNotice(`Series "${item.name}" deleted.`);
            await refreshData();
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "Could not delete series.");
        }
    }

    async function handleCreateSensor(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        if (!token) {
            setError("You must be logged in to register sensors.");
            return;
        }

        if (!sensorForm.name.trim() || !sensorForm.seriesId) {
            setError("Enter a sensor name and select a series.");
            return;
        }

        setSaving(true);
        setError(null);
        setNotice(null);
        setCreatedSensor(null);
        setKeyCopied(false);

        try {
            const created = await createSensor(token, {
                name: sensorForm.name.trim(),
                seriesId: Number(sensorForm.seriesId),
            });

            setCreatedSensor(created);
            setSensorForm(EMPTY_SENSOR_FORM);
            setNotice("Sensor registered. Copy its API key now; it will not be shown again.");
            await refreshData();
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "Could not register sensor.");
        } finally {
            setSaving(false);
        }
    }

    async function handleDeleteSensor(sensor: Sensor) {
        if (!token || !window.confirm(
            `Unregister sensor "${sensor.name}"? Its API key will stop working.`,
        )) {
            return;
        }

        setError(null);
        setNotice(null);

        try {
            await deleteSensor(token, sensor.id);
            setNotice(`Sensor "${sensor.name}" unregistered.`);
            await refreshData();
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : "Could not unregister sensor.");
        }
    }

    async function copyApiKey() {
        if (!createdSensor) {
            return;
        }

        try {
            await navigator.clipboard.writeText(createdSensor.apiKey);
            setKeyCopied(true);
        } catch {
            setError("Clipboard access failed. Select and copy the key manually.");
        }
    }

    const seriesNames = new Map(series.map((item) => [item.id, item.name]));

    return (
        <main className="admin-page">
            <header className="admin-header">
                <div>
                    <h1>Administration</h1>
                    <p>Manage measurement series and registered sensors.</p>
                </div>

                <button type="button" onClick={() => void refreshData()} disabled={loading}>
                    Refresh
                </button>
            </header>

            {error && <div className="admin-message admin-error" role="alert">{error}</div>}
            {notice && <div className="admin-message admin-notice" role="status">{notice}</div>}
            {loading && <p aria-live="polite">Loading administration data...</p>}

            {createdSensor && (
                <section className="admin-panel api-key-panel" aria-labelledby="api-key-heading">
                    <h2 id="api-key-heading">Sensor API key — copy it now</h2>
                    <p>
                        This key is shown only after registration. Store it securely;
                        you will not be able to retrieve it from the application later.
                    </p>

                    <p><strong>Sensor:</strong> {createdSensor.name}</p>

                    <label htmlFor="created-api-key">API key</label>
                    <textarea
                        id="created-api-key"
                        readOnly
                        rows={3}
                        value={createdSensor.apiKey}
                        onFocus={(event) => event.currentTarget.select()}
                    />

                    <div className="admin-actions">
                        <button type="button" onClick={() => void copyApiKey()}>
                            {keyCopied ? "Copied" : "Copy API key"}
                        </button>

                        <button
                            type="button"
                            onClick={() => {
                                setCreatedSensor(null);
                                setKeyCopied(false);
                            }}
                        >
                            Hide key
                        </button>
                    </div>
                </section>
            )}

            <section className="admin-panel" aria-labelledby="series-heading">
                <h2 id="series-heading">Measurement series</h2>

                <form className="admin-form" onSubmit={handleSaveSeries}>
                    <h3 id="series-form-heading">
                        {editingSeriesId === null ? "Create series" : `Edit series #${editingSeriesId}`}
                    </h3>

                    <label htmlFor="series-name">Name</label>
                    <input
                        id="series-name"
                        required
                        maxLength={100}
                        value={seriesForm.name}
                        onChange={(event) =>
                            setSeriesForm({ ...seriesForm, name: event.target.value })
                        }
                    />

                    <div className="admin-form-grid">
                        <div>
                            <label htmlFor="series-min">Minimum value</label>
                            <input
                                id="series-min"
                                type="number"
                                step="any"
                                required
                                value={seriesForm.minValue}
                                onChange={(event) =>
                                    setSeriesForm({ ...seriesForm, minValue: event.target.value })
                                }
                            />
                        </div>

                        <div>
                            <label htmlFor="series-max">Maximum value</label>
                            <input
                                id="series-max"
                                type="number"
                                step="any"
                                required
                                value={seriesForm.maxValue}
                                onChange={(event) =>
                                    setSeriesForm({ ...seriesForm, maxValue: event.target.value })
                                }
                            />
                        </div>
                    </div>

                    <div className="admin-form-grid">
                        <div>
                            <label htmlFor="series-color">Color</label>
                            <input
                                id="series-color"
                                type="color"
                                value={seriesForm.color}
                                onChange={(event) =>
                                    setSeriesForm({ ...seriesForm, color: event.target.value })
                                }
                            />
                        </div>

                        <div>
                            <label htmlFor="series-icon">Icon (optional)</label>
                            <input
                                id="series-icon"
                                maxLength={100}
                                value={seriesForm.icon}
                                onChange={(event) =>
                                    setSeriesForm({ ...seriesForm, icon: event.target.value })
                                }
                            />
                        </div>

                        <div>
                            <label htmlFor="series-unit">Unit (optional)</label>
                            <input
                                id="series-unit"
                                maxLength={20}
                                value={seriesForm.unit}
                                onChange={(event) =>
                                    setSeriesForm({ ...seriesForm, unit: event.target.value })
                                }
                            />
                        </div>
                    </div>

                    <div className="admin-actions">
                        <button type="submit" disabled={saving}>
                            {saving
                                ? "Saving..."
                                : editingSeriesId === null
                                    ? "Create series"
                                    : "Save changes"}
                        </button>

                        {editingSeriesId !== null && (
                            <button type="button" onClick={resetSeriesForm} disabled={saving}>
                                Cancel edit
                            </button>
                        )}
                    </div>
                </form>

                <div className="admin-table-container">
                    <table>
                        <thead>
                        <tr>
                            <th scope="col">Name</th>
                            <th scope="col">Range</th>
                            <th scope="col">Color</th>
                            <th scope="col">Unit</th>
                            <th scope="col">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {series.map((item) => (
                            <tr key={item.id}>
                                <td>
                                        <span className="admin-series-name">
                                            <span
                                                className="admin-color-swatch"
                                                style={{ backgroundColor: item.color }}
                                                aria-hidden="true"
                                            />
                                            {item.name}
                                        </span>
                                </td>
                                <td>{item.minValue} – {item.maxValue}</td>
                                <td>{item.color}</td>
                                <td>{item.unit || "—"}</td>
                                <td>
                                    <div className="admin-actions">
                                        <button
                                            type="button"
                                            onClick={() => startEditingSeries(item)}
                                        >
                                            Edit
                                        </button>
                                        <button
                                            type="button"
                                            className="danger-button"
                                            onClick={() => void handleDeleteSeries(item)}
                                        >
                                            Delete
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    {!loading && series.length === 0 && (
                        <p>No series have been created yet.</p>
                    )}
                </div>
            </section>

            <section className="admin-panel" aria-labelledby="sensors-heading">
                <h2 id="sensors-heading">Sensors</h2>

                <form className="admin-form" onSubmit={handleCreateSensor}>
                    <h3>Register sensor</h3>

                    <label htmlFor="sensor-name">Name</label>
                    <input
                        id="sensor-name"
                        required
                        maxLength={100}
                        value={sensorForm.name}
                        onChange={(event) =>
                            setSensorForm({ ...sensorForm, name: event.target.value })
                        }
                    />

                    <label htmlFor="sensor-series">Series</label>
                    <select
                        id="sensor-series"
                        required
                        value={sensorForm.seriesId}
                        onChange={(event) =>
                            setSensorForm({ ...sensorForm, seriesId: event.target.value })
                        }
                    >
                        <option value="">Select a series</option>
                        {series.map((item) => (
                            <option key={item.id} value={item.id}>
                                {item.name} ({item.minValue}–{item.maxValue}{item.unit ? ` ${item.unit}` : ""})
                            </option>
                        ))}
                    </select>

                    <div className="admin-actions">
                        <button type="submit" disabled={saving || series.length === 0}>
                            {saving ? "Registering..." : "Register sensor"}
                        </button>
                    </div>
                </form>

                <div className="admin-table-container">
                    <table>
                        <thead>
                        <tr>
                            <th scope="col">Name</th>
                            <th scope="col">Series</th>
                            <th scope="col">Created</th>
                            <th scope="col">Last measurement</th>
                            <th scope="col">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {sensors.map((sensor) => (
                            <tr key={sensor.id}>
                                <td>{sensor.name}</td>
                                <td>{seriesNames.get(sensor.seriesId) ?? `Series ${sensor.seriesId}`}</td>
                                <td>{formatTimestamp(sensor.createdAt)}</td>
                                <td>{formatTimestamp(sensor.lastMeasurementAt)}</td>
                                <td>
                                    <button
                                        type="button"
                                        className="danger-button"
                                        onClick={() => void handleDeleteSensor(sensor)}
                                    >
                                        Unregister
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    {!loading && sensors.length === 0 && (
                        <p>No sensors have been registered yet.</p>
                    )}
                </div>
            </section>
        </main>
    );
}