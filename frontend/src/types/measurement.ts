export type Measurement = {
    id: number;
    seriesId: number;
    value: number;
    timestamp: string;
    sensorId?: number | null;
};