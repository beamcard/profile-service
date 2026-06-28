package com.beamcard.profile.rest.utils;

import io.nayuki.qrcodegen.QrCode;
import java.util.Objects;

/**
 * Renders a {@link QrCode} to a self-contained, scalable SVG document.
 */
public final class QrSvgUtil {

    /**
     * QR spec quiet zone: at least 4 light modules must surround the symbol so scanners can
     * locate it
     */
    public static final int QUIET_ZONE_MODULES = 4;

    private static final String DARK_COLOR = "#000000";
    private static final String LIGHT_COLOR = "#ffffff";
    private static final int CHARS_PER_MODULE = 12;

    private QrSvgUtil() {}

    public static String toSvg(QrCode qr) {
        return toSvg(qr, QUIET_ZONE_MODULES);
    }

    /**
     * Render with an explicit quiet-zone border.
     *
     * @param qr the encoded QR symbol
     * @param quietZoneModules light-module margin on each side; must be {@code >= 0}
     * @return a complete, standalone SVG document
     */
    public static String toSvg(QrCode qr, int quietZoneModules) {
        Objects.requireNonNull(qr, "qr must not be null");
        if (quietZoneModules < 0) {
            throw new IllegalArgumentException("quietZoneModules must be >= 0, was " + quietZoneModules);
        }

        int dimension = qr.size + quietZoneModules * 2;
        StringBuilder svg = new StringBuilder(qr.size * qr.size * CHARS_PER_MODULE)
                .append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
                .append(dimension)
                .append(' ')
                .append(dimension)
                .append("\" shape-rendering=\"crispEdges\" role=\"img\">")
                .append("<rect width=\"100%\" height=\"100%\" fill=\"")
                .append(LIGHT_COLOR)
                .append("\"/><path fill=\"")
                .append(DARK_COLOR)
                .append("\" d=\"");
        appendDarkModules(svg, qr, quietZoneModules);
        return svg.append("\"/></svg>").toString();
    }

    /** Appends each dark module as a 1×1 sub-path ({@code M x,y h1 v1 h-1 z}). */
    private static void appendDarkModules(StringBuilder path, QrCode qr, int offset) {
        for (int y = 0; y < qr.size; y++) {
            for (int x = 0; x < qr.size; x++) {
                if (qr.getModule(x, y)) {
                    path.append('M')
                            .append(x + offset)
                            .append(',')
                            .append(y + offset)
                            .append("h1v1h-1z");
                }
            }
        }
    }
}
