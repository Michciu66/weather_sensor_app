package com.chudzik.weather_sensors.specification;

import com.chudzik.weather_sensors.dto.MeasurementQueryDto;
import com.chudzik.weather_sensors.exception.MalformedQueryException;
import com.chudzik.weather_sensors.model.Measurement;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class MeasurementQuerySpecification {

    private MeasurementQuerySpecification() {
    }

    public static Specification<Measurement> hasSeriesIds(List<Long> seriesIds) {
        return (root, query, criteriaBuilder) ->
                root.get("series").get("id").in(seriesIds);
    }

    public static Specification<Measurement> timestampFrom(
            Instant from
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("timestamp"),
                        from
                );
    }

    public static Specification<Measurement> timestampTo(
            Instant to
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("timestamp"),
                        to
                );
    }

    private static List<Long> parseSeriesIds(String series) {
        try {
            return Arrays.stream(series.split(","))
                    .map(String::trim)
                    .map(Long::valueOf)
                    .toList();
        } catch (NumberFormatException e) {
            throw new MalformedQueryException(
                    "Series must contain comma-separated numeric IDs"
            );
        }
    }

    public static Specification<Measurement> buildSpecification(
            MeasurementQueryDto query
    ) {
        Specification<Measurement> specification =
                (root, cq, cb) -> cb.conjunction();

        if (query.series() != null) {
            List<Long> seriesIds = parseSeriesIds(query.series());

            specification = specification.and(
                    MeasurementQuerySpecification.hasSeriesIds(seriesIds)
            );
        }

        if (query.from() != null) {
            specification = specification.and(
                    MeasurementQuerySpecification.timestampFrom(query.from())
            );
        }

        if (query.to() != null) {
            specification = specification.and(
                    MeasurementQuerySpecification.timestampTo(query.to())
            );
        }

        return specification;
    }

}
