import { useEffect, useMemo, useState, type FormEvent, type KeyboardEvent } from "react";
import {
    CartesianGrid,
    Line,
    LineChart,
    ReferenceDot,
    ResponsiveContainer,
    Scatter,
    XAxis,
    YAxis,
} from "recharts";
import { getSeries } from "../api/seriesApi";
import { getMeasurements } from "../api/measurementsApi";
import type { Measurement } from "../types/measurement";
import type { Series } from "../types/series";
import "./MeasurementsPage.css";

const MAX_AXIS_RANGE_MULTIPLIER = 3;
const LINE_DASH_PATTERNS = [undefined, "6 3", "2 3", "8 3 2 3"] as const;
const POINT_SHAPES = ["circle", "square", "diamond", "triangle", "cross"] as const;
type PointShape = (typeof POINT_SHAPES)[number];

type ChartLine = {
    id: string;
    dataKey: string;
    name: string;
    seriesId: number;
    sensorId: number | null;
    color: string;
    styleIndex: number;
};

function getSeriesDataKey(seriesId: number): string {
    return `plot_series_${seriesId}`;
}

function getSensorDataKey(seriesId: number, sensorId: number | null | undefined): string {
    return `plot_series_${seriesId}_sensor_${sensorId ?? "unassigned"}`;
}

function SeriesLegendSymbol({
                                color,
                                dash,
                                shape,
                            }: {
    color: string;
    dash: (typeof LINE_DASH_PATTERNS)[number];
    shape: PointShape;
}) {
    return (
        <svg width="44" height="16" viewBox="0 0 44 16" aria-hidden="true">
            <line
                x1="1"
                y1="8"
                x2="43"
                y2="8"
                stroke={color}
                strokeWidth={2}
                strokeDasharray={dash}
            />

            {shape === "circle" && (
                <circle cx="22" cy="8" r="3" fill={color} />
            )}

            {shape === "square" && (
                <rect x="19" y="5" width="6" height="6" fill={color} />
            )}

            {shape === "diamond" && (
                <polygon points="22,3 27,8 22,13 17,8" fill={color} />
            )}

            {shape === "triangle" && (
                <polygon points="22,3 27,12 17,12" fill={color} />
            )}

            {shape === "cross" && (
                <path
                    d="M18 4 L26 12 M26 4 L18 12"
                    stroke={color}
                    strokeWidth={2}
                    fill="none"
                />
            )}
        </svg>
    );
}

function getSeriesRange(item: Series): number {
    return Number(item.maxValue) - Number(item.minValue);
}

function areSeriesCompatible(items: Series[]): boolean {
    if (items.length <= 1) {
        return true;
    }

    const ranges = items.map((item) => ({
        min: Number(item.minValue),
        max: Number(item.maxValue),
        span: getSeriesRange(item),
    }));

    if (ranges.some((range) =>
        !Number.isFinite(range.min) ||
        !Number.isFinite(range.max) ||
        !Number.isFinite(range.span) ||
        range.span <= 0
    )) {
        return false;
    }

    const overallMin = Math.min(...ranges.map((range) => range.min));
    const overallMax = Math.max(...ranges.map((range) => range.max));
    const narrowestRange = Math.min(...ranges.map((range) => range.span));

    return overallMax - overallMin <= MAX_AXIS_RANGE_MULTIPLIER * narrowestRange;
}

function formatTimestamp(timestamp: string): string {
    return new Date(timestamp).toLocaleString("pl-PL");
}

function toApiTimestamp(value: string): string | undefined {
    return value ? new Date(value).toISOString() : undefined;
}

export function MeasurementsPage() {
    const [series, setSeries] = useState<Series[]>([]);
    const [selectedSeriesIds, setSelectedSeriesIds] = useState<number[]>([]);
    const [measurements, setMeasurements] = useState<Measurement[]>([]);
    const [activeMeasurementId, setActiveMeasurementId] = useState<number | null>(null);
    const [hoveredMeasurementId, setHoveredMeasurementId] = useState<number | null>(null);
    const [groupBySensor, setGroupBySensor] = useState(false);
    const [fromInput, setFromInput] = useState("");
    const [toInput, setToInput] = useState("");
    const [appliedFrom, setAppliedFrom] = useState("");
    const [appliedTo, setAppliedTo] = useState("");

    const [seriesLoading, setSeriesLoading] = useState(true);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const selectedSeriesKey = selectedSeriesIds
        .slice()
        .sort((a, b) => a - b)
        .join(",");

    const selectedIds = useMemo(
        () => selectedSeriesKey ? selectedSeriesKey.split(",").map(Number) : [],
        [selectedSeriesKey],
    );

    const selectedSeries = useMemo(
        () => series.filter((item) => selectedIds.includes(item.id)),
        [series, selectedIds],
    );

    const chartMinValue = selectedSeries.length > 0
        ? Math.min(...selectedSeries.map((item) => Number(item.minValue)))
        : 0;
    const chartMaxValue = selectedSeries.length > 0
        ? Math.max(...selectedSeries.map((item) => Number(item.maxValue)))
        : 100;

    useEffect(() => {
        let cancelled = false;

        getSeries()
            .then((items) => {
                if (!cancelled) {
                    setSeries(items);
                    setSelectedSeriesIds(items.length > 0 ? [items[0].id] : []);
                }
            })
            .catch((err: unknown) => {
                if (!cancelled) {
                    setError(err instanceof Error ? err.message : "Could not load series.");
                }
            })
            .finally(() => {
                if (!cancelled) {
                    setSeriesLoading(false);
                }
            });

        return () => {
            cancelled = true;
        };
    }, []);

    useEffect(() => {
        if (selectedIds.length === 0) {
            setMeasurements([]);
            setLoading(false);
            return;
        }

        if (appliedFrom && appliedTo && new Date(appliedFrom) > new Date(appliedTo)) {
            setError("The start time must not be later than the end time.");
            setMeasurements([]);
            setLoading(false);
            return;
        }

        let cancelled = false;

        setLoading(true);
        setError(null);

        const filters = {
            from: toApiTimestamp(appliedFrom),
            to: toApiTimestamp(appliedTo),
            limit: 1000,
        };

        Promise.all(selectedIds.map((id) => getMeasurements(id, filters)))
            .then((results) => {
                if (cancelled) {
                    return;
                }

                const combined = results
                    .flat()
                    .sort((a, b) => Date.parse(a.timestamp) - Date.parse(b.timestamp));

                setMeasurements(combined);
                setActiveMeasurementId(null);

                if (!appliedFrom && !appliedTo && combined.length > 0) {
                    const earliest = combined[0];
                    const latest = combined[combined.length - 1];

                    setFromInput(toDateTimeLocalValue(earliest.timestamp));
                    setToInput(toDateTimeLocalValue(latest.timestamp));
                }
            })

            .catch((err: unknown) => {
                if (!cancelled) {
                    setMeasurements([]);
                    setError(err instanceof Error ? err.message : "Could not load measurements.");
                }
            })
            .finally(() => {
                if (!cancelled) {
                    setLoading(false);
                }
            });

        return () => {
            cancelled = true;
        };
    }, [selectedIds, appliedFrom, appliedTo]);

    useEffect(() => {
        setFromInput("");
        setToInput("");
        setAppliedFrom("");
        setAppliedTo("");
        setActiveMeasurementId(null);
        setHoveredMeasurementId(null);
    }, [selectedSeriesKey]);

    const chartLines = useMemo<ChartLine[]>(() => {
        if (!groupBySensor) {
            return selectedSeries.map((item) => {
                const styleIndex = Math.max(
                    0,
                    series.findIndex((candidate) => candidate.id === item.id),
                );

                return {
                    id: `series-${item.id}`,
                    dataKey: getSeriesDataKey(item.id),
                    name: `${item.name}${item.unit ? ` (${item.unit})` : ""}`,
                    seriesId: item.id,
                    sensorId: null,
                    color: item.color || "#8884d8",
                    styleIndex,
                };
            });
        }

        // Keep unassigned readings separate for each series, since they cannot be tied to a sensor.
        const groups = new Map<string, { seriesId: number; sensorId: number | null }>();

        for (const measurement of measurements) {
            const sensorId = measurement.sensorId ?? null;
            const groupKey = `${measurement.seriesId}:${sensorId ?? "unassigned"}`;

            if (!groups.has(groupKey)) {
                groups.set(groupKey, { seriesId: measurement.seriesId, sensorId });
            }
        }

        return [...groups.values()]
            .sort((a, b) => a.seriesId - b.seriesId || (a.sensorId ?? -1) - (b.sensorId ?? -1))
            .map((group, styleIndex) => {
                const item = series.find((candidate) => candidate.id === group.seriesId);
                const seriesName = item
                    ? `${item.name}${item.unit ? ` (${item.unit})` : ""}`
                    : `Series ${group.seriesId}`;
                const sensorName = group.sensorId === null
                    ? "Unassigned sensor"
                    : `Sensor ${group.sensorId}`;

                return {
                    id: `sensor-${group.seriesId}-${group.sensorId ?? "unassigned"}`,
                    dataKey: getSensorDataKey(group.seriesId, group.sensorId),
                    name: `${sensorName} — ${seriesName}`,
                    seriesId: group.seriesId,
                    sensorId: group.sensorId,
                    color: item?.color || "#8884d8",
                    styleIndex,
                };
            });
    }, [groupBySensor, selectedSeries, series, measurements]);

    type ChartDataPoint = {
        measurementKey: string;
        time: number;
        [key: string]: string | number | null;
    };

    const chartData = useMemo<ChartDataPoint[]>(() => {
        return [...measurements]
            .sort((a, b) => Date.parse(a.timestamp) - Date.parse(b.timestamp) || a.id - b.id)
            .map((measurement) => {
                const point: ChartDataPoint = {
                    measurementKey: String(measurement.id),
                    time: Date.parse(measurement.timestamp),
                };

                for (const line of chartLines) {
                    point[line.dataKey] = null;
                }

                const dataKey = groupBySensor
                    ? getSensorDataKey(measurement.seriesId, measurement.sensorId)
                    : getSeriesDataKey(measurement.seriesId);
                point[dataKey] = measurement.value;
                return point;
            });
    }, [measurements, chartLines, groupBySensor]);

    const scatterData = useMemo(
        () => measurements
            .map((measurement) => ({
                time: Date.parse(measurement.timestamp),
                value: measurement.value,
                measurementId: measurement.id,
                seriesId: measurement.seriesId,
                timestamp: measurement.timestamp,
                sensorId: measurement.sensorId,
            }))
            .filter((point) => Number.isFinite(point.time)),
        [measurements],
    );

    const navigableMeasurements = useMemo(
        () =>
            [...measurements].sort(
                (a, b) =>
                    Date.parse(a.timestamp) - Date.parse(b.timestamp) ||
                    a.seriesId - b.seriesId ||
                    a.id - b.id,
            ),
        [measurements],
    );

    function handleChartKeyDown(event: KeyboardEvent<HTMLDivElement>) {
        if (navigableMeasurements.length === 0) {
            return;
        }

        const currentId = activeMeasurementId ?? hoveredMeasurementId;
        const currentIndex = navigableMeasurements.findIndex(
            (measurement) => measurement.id === currentId,
        );

        if (event.key === "Escape") {
            event.preventDefault();
            setHoveredMeasurementId(null);
            setActiveMeasurementId(null);
            return;
        }

        let nextIndex: number;

        switch (event.key) {
            case "ArrowLeft":
                nextIndex = currentIndex <= 0 ? 0 : currentIndex - 1;
                break;

            case "ArrowRight":
                nextIndex = currentIndex < 0
                    ? 0
                    : Math.min(currentIndex + 1, navigableMeasurements.length - 1);
                break;

            case "Home":
                nextIndex = 0;
                break;

            case "End":
                nextIndex = navigableMeasurements.length - 1;
                break;

            default:
                return;
        }

        event.preventDefault();
        setActiveMeasurementId(navigableMeasurements[nextIndex].id);
        setHoveredMeasurementId(null);
    }

    function handlePointHover(point: unknown) {
        const entry = point as {
            payload?: { measurementId?: number };
            measurementId?: number;
        };

        const measurementId = entry.payload?.measurementId ?? entry.measurementId;

        if (typeof measurementId === "number") {
            setHoveredMeasurementId(measurementId);
        }
    }

    const highlightedMeasurement = measurements.find(
        (measurement) => measurement.id === (hoveredMeasurementId ?? activeMeasurementId),
    );

    const highlightedSeries = highlightedMeasurement
        ? series.find((item) => item.id === highlightedMeasurement.seriesId)
        : undefined;

    const highlightedValue = highlightedMeasurement?.value ?? null;

    function toggleSeries(id: number) {
        setSelectedSeriesIds((current) => {
            if (current.includes(id)) {
                return current.filter((selectedId) => selectedId !== id);
            }

            const itemToAdd = series.find((item) => item.id === id);

            if (!itemToAdd) {
                return current;
            }

            const currentItems = series.filter((item) => current.includes(item.id));

            if (!areSeriesCompatible([...currentItems, itemToAdd])) {
                return current;
            }

            return [...current, id];
        });

        setActiveMeasurementId(null);
    }

    function applyFilters(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        if (fromInput && toInput && new Date(fromInput) > new Date(toInput)) {
            setError("The start time must not be later than the end time.");
            return;
        }

        setError(null);
        setActiveMeasurementId(null);
        setAppliedFrom(fromInput);
        setAppliedTo(toInput);
    }

    function clearFilters() {
        setFromInput("");
        setToInput("");
        setAppliedFrom("");
        setAppliedTo("");
        setError(null);
        setActiveMeasurementId(null);
    }

    function selectCompatibleSeries() {
        const selectedIds = [...selectedSeriesIds];

        for (const item of series) {
            if (selectedIds.includes(item.id)) {
                continue;
            }

            const currentItems = series.filter((candidate) => selectedIds.includes(candidate.id));

            if (areSeriesCompatible([...currentItems, item])) {
                selectedIds.push(item.id);
            }
        }

        setSelectedSeriesIds(selectedIds);
        setActiveMeasurementId(null);
    }

    function toDateTimeLocalValue(timestamp: string): string {
        const date = new Date(timestamp);
        const pad = (value: number) => String(value).padStart(2, "0");

        return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
    }

    const tableMeasurements = [...measurements].sort(
        (a, b) => Date.parse(b.timestamp) - Date.parse(a.timestamp),
    );

    const printSeriesSummary = selectedSeries
        .map((item) =>
            `${item.name} (${item.minValue}–${item.maxValue}${item.unit ? ` ${item.unit}` : ""})`,
        )
        .join(", ");

    const printPeriodStart = appliedFrom
        ? formatTimestamp(new Date(appliedFrom).toISOString())
        : "Earliest available measurement";

    const printPeriodEnd = appliedTo
        ? formatTimestamp(new Date(appliedTo).toISOString())
        : "Latest available measurement";

    return (
        <main className="measurements-page">
            <header className="page-header">
                <div>
                    <h1>Weather Sensors</h1>
                    <p>Explore measurements from your sensor series.</p>
                </div>

                <button
                    type="button"
                    className="no-print"
                    onClick={() => window.print()}
                >
                    Print chart and table
                </button>
            </header>

            <section className="panel no-print" aria-labelledby="series-heading">
                <h2 id="series-heading">Series</h2>

                {seriesLoading && <p>Loading series...</p>}

                {!seriesLoading && series.length === 0 && !error && (
                    <p>No series available.</p>
                )}

                <div className="series-options">
                    {series.map((item) => {
                        const isSelected = selectedSeriesIds.includes(item.id);

                        const canAdd = isSelected || areSeriesCompatible([
                            ...series.filter((candidate) => selectedSeriesIds.includes(candidate.id)),
                            item,
                        ]);

                        return (
                            <label
                                className={`series-option ${!isSelected && !canAdd ? "series-option-disabled" : ""}`}
                                key={item.id}
                            >
                                <input
                                    type="checkbox"
                                    checked={isSelected}
                                    disabled={!isSelected && !canAdd}
                                    onChange={() => toggleSeries(item.id)}
                                    title={!isSelected && !canAdd
                                        ? "This series would make the combined chart scale too wide."
                                        : undefined}
                                />

                                <span
                                    className="series-color"
                                    style={{backgroundColor: item.color || "#8884d8"}}
                                    aria-hidden="true"
                                />

                                <span className="series-option-label">
                                <span>
                                    {item.name}
                                    {item.unit ? ` (${item.unit})` : ""}
                                </span>

                                <small className="series-range">
                                    Min: {item.minValue} · Max: {item.maxValue}
                                    {item.unit ? ` ${item.unit}` : ""}
                                </small>

                                    {!isSelected && !canAdd && (
                                        <small className="series-disabled-reason">
                                            Incompatible scale
                                        </small>
                                    )}
                                </span>
                            </label>
                        );
                    })}
                </div>

                <div className="series-actions">
                    <button type="button" onClick={selectCompatibleSeries}>
                        Select compatible series
                    </button>
                    <button
                        type="button"
                        onClick={() => {
                            setSelectedSeriesIds([]);
                            setActiveMeasurementId(null);
                        }}
                    >
                        Clear selection
                    </button>
                </div>
            </section>

            <section className="panel no-print" aria-labelledby="filters-heading">
                <h2 id="filters-heading">Time range</h2>

                <form className="time-filters" onSubmit={applyFilters}>
                    <label>
                        From
                        <input
                            type="datetime-local"
                            step={1}
                            value={fromInput}
                            onChange={(event) => setFromInput(event.target.value)}
                        />
                    </label>

                    <label>
                        To
                        <input
                            type="datetime-local"
                            step={1}
                            value={toInput}
                            onChange={(event) => setToInput(event.target.value)}
                        />
                    </label>

                    <div className="filter-actions">
                        <button type="submit">Apply filters</button>
                        <button type="button" onClick={clearFilters}>
                            Clear dates
                        </button>
                    </div>
                </form>
            </section>

            {error && <p className="error-message" role="alert">{error}</p>}

            <div className="print-summary">
                <p>
                    <strong>Selected series:</strong>{" "}
                    {printSeriesSummary || "None"}
                </p>

                <p>
                    <strong>Time range:</strong>{" "}
                    {printPeriodStart} – {printPeriodEnd}
                </p>
            </div>

            <section className="panel chart-panel" aria-labelledby="chart-heading">
                <div className="section-heading">
                    <h2 id="chart-heading">Measurements chart</h2>
                    <span>{measurements.length} measurements</span>
                </div>

                <p className="chart-note">
                    The chart displays actual measurement values. To keep the shared Y-axis readable,
                    the total selected value range cannot exceed three times the narrowest series range.
                </p>

                {loading && <p aria-live="polite">Loading measurements...</p>}

                {!loading && !error && selectedSeriesIds.length === 0 && (
                    <p>Select at least one series to display the chart.</p>
                )}

                {!loading && !error && selectedSeriesIds.length > 0 && measurements.length === 0 && (
                    <p>No measurements found for the selected series and time range.</p>
                )}

                <div
                    className={`selected-point-slot ${highlightedMeasurement && highlightedSeries ? "has-selection" : "is-empty"}`}
                    aria-live="polite"
                    aria-atomic="true"
                >
                    {highlightedMeasurement && highlightedSeries ? (
                        <div className="selected-point-details" role="status">
                            <strong>{highlightedSeries.name}</strong>
                            <span>
                                Value: {highlightedMeasurement.value}
                                {highlightedSeries.unit ? ` ${highlightedSeries.unit}` : ""}
                            </span>
                            <span>{formatTimestamp(highlightedMeasurement.timestamp)}</span>
                            <span>Sensor: {highlightedMeasurement.sensorId ?? "—"}</span>
                            <span>Measurement ID: {highlightedMeasurement.id}</span>
                        </div>
                    ) : (
                        <p className="selected-point-placeholder">
                            Hover over a chart point or select a measurement in the table.
                        </p>
                    )}
                </div>

                <div className="chart-display-options no-print">
                    <label className="chart-mode-toggle" htmlFor="group-by-sensor">
                        <input
                            id="group-by-sensor"
                            type="checkbox"
                            checked={groupBySensor}
                            onChange={(event) => setGroupBySensor(event.target.checked)}
                        />
                        <span>Show a separate line for each sensor</span>
                    </label>
                    <p className="chart-mode-description">
                        {groupBySensor
                            ? "Each sensor has its own line. Readings without a sensor are grouped separately for each series."
                            : "Measurements are grouped into one line per selected series."}
                    </p>
                </div>

                <div className="chart-legend" aria-label="Displayed measurement lines">
                    {chartLines.map((line) => (
                        <div className="chart-legend-item" key={line.id}>
                            <SeriesLegendSymbol
                                color={line.color}
                                dash={LINE_DASH_PATTERNS[line.styleIndex % LINE_DASH_PATTERNS.length]}
                                shape={POINT_SHAPES[line.styleIndex % POINT_SHAPES.length]}
                            />
                            <span>{line.name}</span>
                        </div>
                    ))}
                </div>

                <p id="chart-keyboard-help" className="chart-keyboard-help">
                    Keyboard controls: focus the chart and use Left/Right to select the previous or next
                    measurement. Home/End selects the first or last measurement. Escape clears the selection.
                    Mouse hover previews a point; selecting a point with the keyboard or from the table
                    keeps that point highlighted.
                </p>

                {!loading && !error && selectedSeries.length > 0 && (
                    <div
                        className="chart-container"
                        tabIndex={0}
                        role="group"
                        aria-labelledby="chart-heading"
                        aria-describedby="chart-keyboard-help"
                        onKeyDown={handleChartKeyDown}
                        onMouseLeave={() => setHoveredMeasurementId(null)}
                    >
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart
                                data={chartData}
                                margin={{ top: 10, right: 24, bottom: 8, left: 8 }}
                            >
                                <CartesianGrid strokeDasharray="3 3" stroke="#454955" />

                                <XAxis
                                    dataKey="time"
                                    type="number"
                                    domain={chartData.length > 0 ? ["dataMin", "dataMax"] : [0, 1]}
                                    tick={chartData.length > 0 ? undefined : false}
                                    tickFormatter={(value: number) =>
                                        new Date(value).toLocaleString("pl-PL", {
                                            day: "2-digit",
                                            month: "2-digit",
                                            hour: "2-digit",
                                            minute: "2-digit",
                                        })
                                    }
                                    minTickGap={24}
                                />

                                <YAxis
                                    domain={[chartMinValue, chartMaxValue]}
                                    ticks={[
                                        chartMinValue,
                                        chartMinValue + (chartMaxValue - chartMinValue) / 4,
                                        chartMinValue + (chartMaxValue - chartMinValue) / 2,
                                        chartMinValue + 3 * (chartMaxValue - chartMinValue) / 4,
                                        chartMaxValue,
                                    ]}
                                    allowDataOverflow
                                    tickFormatter={(value: number) =>
                                        new Intl.NumberFormat("pl-PL", { maximumFractionDigits: 2 }).format(value)
                                    }
                                />

                                {chartLines.map((line) => (
                                    <Line
                                        key={line.id}
                                        type="monotone"
                                        dataKey={line.dataKey}
                                        name={line.name}
                                        stroke={line.color}
                                        strokeDasharray={LINE_DASH_PATTERNS[line.styleIndex % LINE_DASH_PATTERNS.length]}
                                        strokeWidth={2}
                                        dot={false}
                                        activeDot={false}
                                        connectNulls
                                    />
                                ))}

                                {chartLines.map((line) => (
                                    <Scatter
                                        key={`points-${line.id}`}
                                        name={`${line.name} points`}
                                        data={scatterData.filter((point) =>
                                            point.seriesId === line.seriesId &&
                                            (!groupBySensor || (point.sensorId ?? null) === line.sensorId),
                                        )}
                                        dataKey="value"
                                        fill={line.color}
                                        shape={POINT_SHAPES[line.styleIndex % POINT_SHAPES.length]}
                                        activeShape={{ r: 7 }}
                                        onMouseEnter={(point) => handlePointHover(point)}
                                        onMouseMove={(point) => handlePointHover(point)}
                                        onMouseLeave={() => setHoveredMeasurementId(null)}
                                    />
                                ))}

                                {highlightedMeasurement && highlightedSeries && highlightedValue !== null && (
                                    <ReferenceDot
                                        x={Date.parse(highlightedMeasurement.timestamp)}
                                        y={highlightedValue}
                                        r={7}
                                        fill={highlightedSeries.color || "#8884d8"}
                                        stroke="#ffffff"
                                        strokeWidth={2}
                                        ifOverflow="extendDomain"
                                    />
                                )}
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                )}
            </section>

            <section className="panel table-panel" aria-labelledby="table-heading">
                <div className="section-heading">
                    <h2 id="table-heading">Measurement table</h2>
                    <span>{tableMeasurements.length} rows</span>
                </div>

                {tableMeasurements.length === 0 ? (
                    <p>There are no measurements to display.</p>
                ) : (
                    <div className="table-container">
                        <table className="measurement-table">
                            <thead>
                            <tr>
                                <th scope="col">Series</th>
                                <th scope="col">Timestamp</th>
                                <th scope="col">Value</th>
                                <th scope="col">Sensor ID</th>
                                <th scope="col">Chart</th>
                            </tr>
                            </thead>

                            <tbody>
                            {tableMeasurements.map((measurement) => {
                                const item = series.find(
                                    (candidate) => candidate.id === measurement.seriesId,
                                );
                                const isActive = measurement.id === activeMeasurementId;

                                return (
                                    <tr
                                        key={measurement.id}
                                        className={isActive ? "selected-measurement" : ""}
                                        onClick={() => setActiveMeasurementId(measurement.id)}
                                        aria-selected={isActive}
                                    >
                                        <td>
                                                <span className="table-series">
                                                    <span
                                                        className="series-color"
                                                        style={{
                                                            backgroundColor: item?.color || "#8884d8",
                                                        }}
                                                        aria-hidden="true"
                                                    />
                                                    {item?.name ?? `Series ${measurement.seriesId}`}
                                                </span>
                                        </td>
                                        <td>{formatTimestamp(measurement.timestamp)}</td>
                                        <td>
                                            {measurement.value}
                                            {item?.unit ? ` ${item.unit}` : ""}
                                        </td>
                                        <td>{measurement.sensorId ?? "—"}</td>
                                        <td>
                                            <button
                                                type="button"
                                                aria-pressed={isActive}
                                                aria-label={`${isActive ? "Selected" : "Highlight"} measurement ${measurement.id}, ${item?.name ?? `series ${measurement.seriesId}`}, ${formatTimestamp(measurement.timestamp)}, value ${measurement.value}${item?.unit ? ` ${item.unit}` : ""}`}
                                                onClick={(event) => {
                                                    event.stopPropagation();
                                                    setActiveMeasurementId(measurement.id);
                                                    setHoveredMeasurementId(null);
                                                }}
                                            >
                                                {isActive ? "Selected" : "Show point"}
                                            </button>
                                        </td>
                                    </tr>
                                );
                            })}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>
        </main>
    );
}